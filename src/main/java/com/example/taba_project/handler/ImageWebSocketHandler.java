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

    //db에 저장하는 함수 사용위해 의존성 추가
    @Autowired
    private ImageRepository imageRepository;

    // 이미지 저장 dir
    @Value("${file.storage.directory}")
    private String directoryPath;

    // 세션별 메시지 조합을 위한 맵
    private final ConcurrentHashMap<String, StringBuilder> sessionData = new ConcurrentHashMap<>();

    // 마지막 수신 시간 기록
    private Instant lastReceivedTime = Instant.now();

    // 디렉토리 삭제 상태 플래그(디렉토리가 한번 삭제되면 이후에 동작하지 않도록 하기 위함)
    private boolean isDirectoryDeleted = false;

    // 이미지가 들어오지 않는 시간(3초 이상 디렉토리에 이미지가 들어오지 않으면 해당 디렉토리 내 파일 삭제)
    private static final long TIMEOUT_SECONDS = 3; // 3초

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ImageWebSocketHandler() {
        // 타이머 스레드 실행
        scheduler.scheduleAtFixedRate(this::checkAndDeleteFiles, 0, 1, TimeUnit.SECONDS);
    }

    // 마지막 chunk 확인 메서드
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

        // 마지막 청크 확인
        if (isLastChunk(payload)) {
            // 데이터 처리
            String base64EncodedData = builder.toString()
                    .replace("<END>", "") // 구분자 제거
                    .replaceAll("\\s", ""); // 공백 제거

            // 데이터 처리 후 세션 데이터 초기화
            sessionData.remove(session.getId());
            // 조합된 Base64 데이터 출력
            System.out.println("조합된 Base64 데이터: " + base64EncodedData.substring(0, Math.min(base64EncodedData.length(), 100)) + "...");
            // Base64 데이터 디코딩 및 처리
            processBase64Data(base64EncodedData, session);

            // 마지막 수신 시간 업데이트 및 삭제 플래그 초기화
            lastReceivedTime = Instant.now();
            // 새로운 이미지가 수신되었으므로 삭제 플래그 초기화
            isDirectoryDeleted = false;
        }
    }

    private void processBase64Data(String base64EncodedData, WebSocketSession session) {
        try {
            // Base64 디코딩
            byte[] decodedData = java.util.Base64.getDecoder().decode(base64EncodedData);

            // 디코딩 후 다시 인코딩하여 검증
            String reEncodedBase64 = java.util.Base64.getEncoder().encodeToString(decodedData);
            if (!reEncodedBase64.equals(base64EncodedData)) {
                System.err.println("Base64 데이터 검증 실패: 디코딩 후 다시 인코딩한 데이터가 일치하지 않습니다.");
                return;
            }
            System.out.println("디코딩된 데이터 크기: " + decodedData.length);

            // 이미지 데이터의 파일 헤더를 확인 -> 유효한 이미지 파일인지 검증 (JPEG 파일 : 'FFD8'로 시작)
            if (decodedData.length < 2 || decodedData[0] != (byte) 0xFF || decodedData[1] != (byte) 0xD8) {
                System.err.println("유효하지 않은 JPEG 파일.");
                return;
            }


            // 이미지 유효성 검증
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedData));
            if (image == null) {
                System.err.println("이미지 유효성 검증 실패: 유효하지 않은 데이터입니다.");
                return;
            }
            System.out.println("이미지 유효성 검증 성공.");

            // 이미지 저장
            saveImage(decodedData);

        } catch (IllegalArgumentException e) {
            System.err.println("Base64 디코딩 실패: " + e.getMessage());
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

            // URL 데이터베이스 저장
            String fileUrl = directoryPath + "/" + fileName;

            // DB에 저장할 Image 객체 생성
            Image image = new Image();
            image.setUrl(fileUrl);
            imageRepository.save(image); // DB에 저장
            System.out.println("이미지 URL 데이터베이스 저장 성공: " + fileUrl);

        } catch (Exception e) {
            System.err.println("이미지 저장 실패: " + e.getMessage());
        }
    }

    // 타이머 기반으로 3초 이상 수신이 없으면 디렉토리 삭제
    private void checkAndDeleteFiles() {
        if (lastReceivedTime == null) {
            return; // 아직 수신된 이미지가 없음
        }

        // 디렉토리가 이미 삭제된 상태라면 더 이상 삭제하지 않음
        if (isDirectoryDeleted) {
            return;
        }

        long secondsSinceLastReceive = Instant.now().getEpochSecond() - lastReceivedTime.getEpochSecond();
        if (secondsSinceLastReceive > TIMEOUT_SECONDS) {
            System.out.println("3초 이상 이미지 수신 없음. 디렉토리 정리 시작...");
            deleteFilesInDirectory(directoryPath);

            // 삭제 완료 후 플래그 설정
            isDirectoryDeleted = true;
        }
    }

    // 디렉토리 내 모든 파일 삭제
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
}