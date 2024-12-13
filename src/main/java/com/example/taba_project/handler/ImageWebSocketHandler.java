package com.example.taba_project.handler;

import com.example.taba_project.model.Image;
import com.example.taba_project.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ImageWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ImageRepository imageRepository;

    @Value("${file.storage.directory}")
    private String directoryPath;

    private final ConcurrentHashMap<String, StringBuilder> sessionData = new ConcurrentHashMap<>();
    private Instant lastReceivedTime = Instant.now();
    private boolean isDirectoryDeleted = false;

    private static final long TIMEOUT_SECONDS = 3; // 3초
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ImageWebSocketHandler() {
        // 타이머 스레드 실행: 1초마다 체크
        scheduler.scheduleAtFixedRate(this::checkAndDeleteFiles, 0, 1, TimeUnit.SECONDS);
    }

    private boolean isLastChunk(String payload) {
        return payload.endsWith("<END>");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // 세션별 데이터 조합
        sessionData.putIfAbsent(session.getId(), new StringBuilder());
        StringBuilder builder = sessionData.get(session.getId());
        builder.append(payload);

        if (isLastChunk(payload)) {
            String base64EncodedData = builder.toString()
                    .replace("<END>", "")
                    .replaceAll("\\s", "");

            sessionData.remove(session.getId());

            System.out.println("조합된 Base64 데이터: " +
                    base64EncodedData.substring(0, Math.min(base64EncodedData.length(), 100)) + "...");

            processBase64Data(base64EncodedData, session);

            // 마지막 수신 시간 업데이트
            lastReceivedTime = Instant.now();
            isDirectoryDeleted = false; // 삭제 플래그 초기화
        }
    }

    private void processBase64Data(String base64EncodedData, WebSocketSession session) {
        try {
            byte[] decodedData = java.util.Base64.getDecoder().decode(base64EncodedData);

            String reEncodedBase64 = java.util.Base64.getEncoder().encodeToString(decodedData);
            if (!reEncodedBase64.equals(base64EncodedData)) {
                System.err.println("Base64 데이터 검증 실패: 디코딩 후 다시 인코딩한 데이터가 일치하지 않습니다.");
                return;
            }

            if (decodedData.length < 2 || decodedData[0] != (byte) 0xFF || decodedData[1] != (byte) 0xD8) {
                System.err.println("유효하지 않은 JPEG 파일.");
                return;
            }

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedData));
            if (image == null) {
                System.err.println("이미지 유효성 검증 실패: 유효하지 않은 데이터입니다.");
                return;
            }

            System.out.println("이미지 유효성 검증 성공.");
            saveImage(decodedData);

        } catch (Exception e) {
            System.err.println("이미지 처리 중 오류: " + e.getMessage());
        }
    }

    private void saveImage(byte[] imageData) {
        try {
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            FileStorageHandler FS = new FileStorageHandler();
            FS.saveFile(directoryPath, fileName, imageData);
            System.out.println("이미지 저장 성공: " + directoryPath + "/" + fileName);

            String fileUrl = directoryPath + "/" + fileName;

            Image image = new Image();
            image.setUrl(fileUrl);
            imageRepository.save(image);
            System.out.println("이미지 URL 데이터베이스 저장 성공: " + fileUrl);

        } catch (Exception e) {
            System.err.println("이미지 저장 실패: " + e.getMessage());
        }
    }

    private void checkAndDeleteFiles() {
        if (lastReceivedTime == null || isDirectoryDeleted) {
            return; // 이미 삭제되었거나 수신된 이미지가 없음
        }

        long secondsSinceLastReceive = Instant.now().getEpochSecond() - lastReceivedTime.getEpochSecond();
        if (secondsSinceLastReceive > TIMEOUT_SECONDS) {
            System.out.println("3초 이상 이미지 수신 없음. 디렉토리 및 DB 정리 시작...");
            deleteFilesInDirectory(directoryPath);
            deleteDatabase();
            isDirectoryDeleted = true;
        }
    }

    private void deleteFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            System.out.println("파일 삭제 성공: " + file.getAbsolutePath());
                        } else {
                            System.err.println("파일 삭제 실패: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        } else {
            System.err.println("유효하지 않은 디렉토리: " + directoryPath);
        }
    }

    private void deleteDatabase() {
        try {
            imageRepository.deleteAll();
            System.out.println("DB에서 image 테이블의 모든 데이터 삭제 완료");
        } catch (Exception e) {
            System.err.println("DB 데이터 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}