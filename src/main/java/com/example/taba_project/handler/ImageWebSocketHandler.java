//package com.example.taba_project.handler;
//
//import com.example.taba_project.model.Image;
//import com.example.taba_project.repository.ImageRepository;
//import com.example.taba_project.service.ImageSenderService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.time.Instant;
//import java.util.Objects;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class ImageWebSocketHandler extends TextWebSocketHandler {
//
//    private final ImageRepository imageRepository;
//    private final ImageSenderService imageSenderService;
//
//    @Value("${file.storage.directory}")
//    private String directoryPath;
//
//    private final ConcurrentHashMap<String, StringBuilder> sessionData = new ConcurrentHashMap<>();
//    private Instant lastReceivedTime = Instant.now();
//    private boolean isDirectoryDeleted = false;
//
//    private static final long TIMEOUT_SECONDS = 3; // 3초
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//    @Autowired
//    public ImageWebSocketHandler(ImageRepository imageRepository, ImageSenderService imageSenderService) {
//        this.imageRepository = imageRepository;
//        this.imageSenderService = imageSenderService;
//        // 타이머 스레드 실행: 1초마다 체크 및 3초 동안 이미지 송신이 없을 시 디렉토리 및 DB clear
//        // scheduler.scheduleAtFixedRate(this::checkAndDeleteFiles, 0, 1, TimeUnit.SECONDS);
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) {
//        String payload = message.getPayload();
//
//        // 디버깅: 수신한 청크 데이터 로그
//        System.out.println("수신한 청크: " + payload);
//
//        // 세션별 데이터 조합
//        sessionData.putIfAbsent(session.getId(), new StringBuilder());
//        StringBuilder builder = sessionData.get(session.getId());
//        builder.append(payload);
//
//        // 디버깅: 현재까지 조합된 데이터 로그
//        System.out.println("현재 조합된 데이터: " + builder.toString());
//
//        // 마지막 청크인지 확인
//        if (isLastChunk(payload)) {
//            try {
//                String completePayload = builder.toString().replace("<END>", "");
//                sessionData.remove(session.getId()); // 데이터 조립 완료 후 삭제
//
//                System.out.println("완성된 데이터 : " + completePayload);
//
//                // JSON 유효성 검사 및 처리
//                if (isValidJson(completePayload)) {
//                    processCompletePayload(completePayload);
//                    lastReceivedTime = Instant.now(); // 마지막 수신 시간 업데이트
//                } else {
//                    System.err.println("유효하지 않은 JSON 데이터입니다.");
//                }
//            } catch (Exception e) {
//                System.err.println("메시지 처리 중 오류: " + e.getMessage());
//            }
//        }
//    }
//
//    private boolean isValidJson(String json) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.readTree(json); // JSON 파싱 시도
//            return true; // 유효한 JSON
//        } catch (Exception e) {
//            return false; // JSON 파싱 실패
//        }
//    }
//
//    private boolean isLastChunk(String payload) {
//        return payload.endsWith("<END>");
//    }
//
//    private void processCompletePayload(String payload) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            JsonNode jsonNode = objectMapper.readTree(payload);
//
//            //JSON 데이터 추출
//            String mode = jsonNode.get("mode").asText(); // 모드 추출
//            String base64Image = jsonNode.get("data").asText(); // 이미지 추출
//
//            // 모드 정규화
//            if (Objects.equals(mode, "대화")) {
//                mode = "chat";
//            } else if (Objects.equals(mode, "이동")) {
//                mode = "move";
//            }
//
//            System.out.println("모드: " + mode);
//            System.out.println("이미지 데이터 길이: " + base64Image.length());
//
//            // JSON으로 수신한 이미지 저장 및 mode에 따른 AI 분석 처리
//            processBase64Data(base64Image, mode);
//        } catch (Exception e) {
//            System.err.println("JSON 처리 중 오류: " + e.getMessage());
//        }
//    }
//
//    private void processBase64Data(String base64Image, String mode) {
//        try {
//            byte[] decodedData = java.util.Base64.getDecoder().decode(base64Image);
//
//            // 데이터 검증
//            if (decodedData.length < 2 || decodedData[0] != (byte) 0xFF || decodedData[1] != (byte) 0xD8) {
//                throw new IllegalArgumentException("유효하지 않은 JPEG 파일.");
//            }
//
//            BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedData));
//            if (image == null) {
//                throw new IllegalArgumentException("이미지 유효성 검증 실패.");
//            }
//
//            System.out.println("이미지 유효성 검증 성공.");
//            saveImage(decodedData, mode);
//        } catch (Exception e) {
//            System.err.println("이미지 처리 중 오류: " + e.getMessage());
//        }
//    }
//
//    private void saveImage(byte[] imageData, String mode) {
//        try {
//            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
//            File outputDir = new File(directoryPath);
//            if (!outputDir.exists()) {
//                boolean dirCreated = outputDir.mkdirs();
//                if (!dirCreated) {
//                    throw new IllegalStateException("이미지 저장 디렉토리를 생성할 수 없습니다.");
//                }
//            }
//
//            File outputFile = new File(outputDir, fileName);
//            java.nio.file.Files.write(outputFile.toPath(), imageData);
//
//            System.out.println("이미지 저장 성공: " + outputFile.getAbsolutePath());
//
//            String fileUrl = outputFile.getAbsolutePath();
//
//            // DB에 이미지 URL 저장
//            Image image = new Image();
//            image.setUrl(fileUrl);
//            imageRepository.save(image);
//            System.out.println("이미지 URL 데이터베이스 저장 성공: " + fileUrl);
//
//            // FastAPI로 이미지 전송
//            imageSenderService.sendLatestImageToFastApi(mode);
//        } catch (Exception e) {
//            System.err.println("이미지 저장 실패: " + e.getMessage());
//        }
//    }
//
//
//    // 이미지 디렉토리 및 DB 삭제하는 로직
//    private void checkAndDeleteFiles() {
//        if (lastReceivedTime == null || isDirectoryDeleted) {
//            return; // 이미 삭제되었거나 수신된 이미지가 없음
//        }
//
//        long secondsSinceLastReceive = Instant.now().getEpochSecond() - lastReceivedTime.getEpochSecond();
//        if (secondsSinceLastReceive > TIMEOUT_SECONDS) {
//            System.out.println("3초 이상 이미지 수신 없음. 디렉토리 및 DB 정리 시작...");
//            deleteFilesInDirectory(directoryPath);
//            deleteDatabase();
//            isDirectoryDeleted = true;
//        }
//    }
//
//    // 이미지 디렉토리 삭제하는 함수
//    private void deleteFilesInDirectory(String directoryPath) {
//        File directory = new File(directoryPath);
//        if (directory.exists() && directory.isDirectory()) {
//            File[] files = directory.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isFile()) {
//                        boolean deleted = file.delete();
//                        if (deleted) {
//                            System.out.println("파일 삭제 성공: " + file.getAbsolutePath());
//                        } else {
//                            System.err.println("파일 삭제 실패: " + file.getAbsolutePath());
//                        }
//                    }
//                }
//            }
//        } else {
//            System.err.println("유효하지 않은 디렉토리: " + directoryPath);
//        }
//    }
//
//    // DB 삭제하는 함수
//    private void deleteDatabase() {
//        try {
//            imageRepository.deleteAll();
//            System.out.println("DB에서 image 테이블의 모든 데이터 삭제 완료");
//        } catch (Exception e) {
//            System.err.println("DB 데이터 삭제 중 오류 발생: " + e.getMessage());
//        }
//    }
//}

package com.example.taba_project.handler;

import com.example.taba_project.model.Image;
import com.example.taba_project.repository.ImageRepository;
import com.example.taba_project.service.ImageSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImageWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImageWebSocketHandler.class);

    private final ImageRepository imageRepository;
    private final ImageSenderService imageSenderService;
    private final FileStorageHandler fileStorageHandler;

    @Value("${file.storage.directory}")
    private String directoryPath;

    // 세션별 데이터를 임시로 저장하는 ConcurrentHashMap
    private final ConcurrentHashMap<String, StringBuilder> sessionData = new ConcurrentHashMap<>();

    @Autowired
    public ImageWebSocketHandler(ImageRepository imageRepository,
                                 ImageSenderService imageSenderService,
                                 FileStorageHandler fileStorageHandler) {
        this.imageRepository = imageRepository;
        this.imageSenderService = imageSenderService;
        this.fileStorageHandler = fileStorageHandler;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        logger.info("수신한 청크 데이터: {}", payload);

        // 세션별 데이터 조합
        sessionData.putIfAbsent(session.getId(), new StringBuilder());
        StringBuilder builder = sessionData.get(session.getId());
        builder.append(payload);

        // 마지막 청크인지 확인
        if (isLastChunk(payload)) {
            try {
                String completePayload = builder.toString().replace("<END>", "");
                sessionData.remove(session.getId()); // 데이터 조합 완료 후 삭제

                logger.info("완성된 데이터: {}", completePayload);

                // JSON 유효성 검사 및 처리
                if (isValidJson(completePayload)) {
                    processCompletePayload(completePayload);
                } else {
                    logger.error("유효하지 않은 JSON 데이터");
                }
            } catch (Exception e) {
                logger.error("메시지 처리 중 오류: {}", e.getMessage());
            }
        }
    }

    private boolean isValidJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(json); // JSON 파싱 시도
            return true; // 유효한 JSON
        } catch (Exception e) {
            return false; // JSON 파싱 실패
        }
    }

    private boolean isLastChunk(String payload) {
        return payload.endsWith("<END>");
    }

    private void processCompletePayload(String payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);

            // JSON 데이터 추출
            String mode = jsonNode.get("mode").asText(); // 모드 추출
            String base64Image = jsonNode.get("data").asText(); // 이미지 추출

            // 모드 정규화
            if ("대화".equals(mode)) {
                mode = "chat";
            } else if ("이동".equals(mode)) {
                mode = "move";
            }

            logger.info("모드: {}", mode);
            logger.info("이미지 데이터 길이: {}", base64Image.length());

            // JSON으로 수신한 이미지 저장 및 mode에 따른 AI 분석 처리
            processBase64Data(base64Image, mode);
        } catch (Exception e) {
            logger.error("JSON 처리 중 오류: {}", e.getMessage());
        }
    }

    private void processBase64Data(String base64Image, String mode) {
        try {
            byte[] decodedData = java.util.Base64.getDecoder().decode(base64Image);

            // 데이터 검증
            if (decodedData.length < 2 || decodedData[0] != (byte) 0xFF || decodedData[1] != (byte) 0xD8) {
                throw new IllegalArgumentException("유효하지 않은 JPEG 파일.");
            }

            // 저장된 파일 경로 얻기
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            String savedFilePath = fileStorageHandler.saveFile(directoryPath, fileName, decodedData);

            logger.info("이미지 저장 성공: {}", savedFilePath);

            // DB 저장
            saveImageRecord(savedFilePath);

            // FastAPI로 이미지 전송
            imageSenderService.sendLatestImageToFastApi(mode);

        } catch (IllegalArgumentException e) {
            logger.error("이미지 데이터 검증 실패: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("이미지 저장 중 오류: {}", e.getMessage());
        }
    }

    private void saveImageRecord(String filePath) {
        try {
            Image image = new Image();
            image.setUrl(filePath);
            imageRepository.save(image);
            logger.info("이미지 URL 데이터베이스 저장 성공: {}", filePath);
        } catch (Exception e) {
            logger.error("이미지 URL 데이터베이스 저장 실패: {}", e.getMessage());
        }
    }
}