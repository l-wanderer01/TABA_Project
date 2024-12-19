package com.example.taba_project.handler;

import com.example.taba_project.model.Image;
import com.example.taba_project.repository.ImageRepository;
import com.example.taba_project.repository.InfoRepository;
import com.example.taba_project.repository.Info2Repository;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class ImageWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImageWebSocketHandler.class);

    private final ImageRepository imageRepository;
    private final InfoRepository infoRepository;
    private final Info2Repository info2Repository;
    private final ImageSenderService imageSenderService;
    private final FileStorageHandler fileStorageHandler;

    @Value("${file.storage.directory}")
    private String directoryPath;

    private final ConcurrentHashMap<String, ChunkedMessage> sessionChunks = new ConcurrentHashMap<>();
    private static final long TIMEOUT_SECONDS = 3; // 3초
    private Instant lastReceivedTime = Instant.now();
    private boolean isDirectoryDeleted = false;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public ImageWebSocketHandler(ImageRepository imageRepository, InfoRepository infoRepository, Info2Repository info2Repository,
                                 ImageSenderService imageSenderService,
                                 FileStorageHandler fileStorageHandler) {
        this.imageRepository = imageRepository;
        this.infoRepository = infoRepository;
        this.info2Repository = info2Repository;
        this.imageSenderService = imageSenderService;
        this.fileStorageHandler = fileStorageHandler;

        // 타이머 실행: 주기적으로 이미지 수신 상태 확인
        scheduler.scheduleAtFixedRate(this::checkAndDeleteFiles, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            logger.info("수신한 데이터: {}", payload);

            JsonNode jsonNode = new ObjectMapper().readTree(payload);

            String sessionId = session.getId();
            int sequence = jsonNode.get("sequence").asInt();
            int chunk = jsonNode.get("chunk").asInt();
            int totalChunks = jsonNode.get("totalChunks").asInt();
            String data = jsonNode.get("data").asText();
            boolean isLast = jsonNode.get("isLast").asBoolean();

            // 청크 데이터 조합
            sessionChunks.putIfAbsent(sessionId, new ChunkedMessage(sequence, totalChunks));
            ChunkedMessage chunkedMessage = sessionChunks.get(sessionId);
            chunkedMessage.addChunk(chunk, data);

            // 마지막 청크인 경우
            if (isLast) {
                logger.info("마지막 청크 수신 완료. 데이터 조합 중...");
                String completeBase64 = chunkedMessage.combineChunks();
                String mode = jsonNode.get("mode").asText();

                // 모드 정규화
                mode = normalizeMode(mode);

                processBase64Data(completeBase64, mode);
                sessionChunks.remove(sessionId); // 조합 완료 후 청크 삭제
            }
        } catch (Exception e) {
            logger.error("메시지 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    private String normalizeMode(String mode) {
        if ("대화".equals(mode)) return "chat";
        if ("이동".equals(mode)) return "move";
        return mode;
    }

    private void processBase64Data(String base64Image, String mode) {
        try {
            byte[] decodedData = java.util.Base64.getDecoder().decode(base64Image);

            logger.info("디코딩된 이미지 크기: {} bytes", decodedData.length);

            // 이미지 데이터 검증
            if (decodedData.length < 2 || decodedData[0] != (byte) 0xFF || decodedData[1] != (byte) 0xD8) {
                throw new IllegalArgumentException("유효하지 않은 JPEG 파일.");
            }

            // 이미지 저장
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            String savedFilePath = fileStorageHandler.saveFile(directoryPath, fileName, decodedData);
            logger.info("이미지 저장 성공: {}", savedFilePath);

            // DB 저장
            saveImageRecord(savedFilePath);

            // FastAPI로 전송
            imageSenderService.sendLatestImageToFastApi(mode);

        } catch (IllegalArgumentException e) {
            logger.error("이미지 데이터 검증 실패: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("이미지 저장 중 오류: {}", e.getMessage());
        }
    }

    private void processCompleteData(String base64Image, String mode) {
        try {
            byte[] decodedImage = java.util.Base64.getDecoder().decode(base64Image);
            logger.info("데이터 크기: {} bytes, 모드: {}", decodedImage.length, mode);

            // 여기서 이미지 저장 및 모드에 따른 추가 작업 수행
            // 예: 이미지 파일 저장, 데이터베이스 저장 등
        } catch (Exception e) {
            logger.error("이미지 처리 중 오류: {}", e.getMessage());
        }
    }

    // 청크 데이터를 조합하는 내부 클래스
    private static class ChunkedMessage {
        private final int sequence;
        private final int totalChunks;
        private final String[] chunks;

        public ChunkedMessage(int sequence, int totalChunks) {
            this.sequence = sequence;
            this.totalChunks = totalChunks;
            this.chunks = new String[totalChunks];
        }

        public void addChunk(int chunkNumber, String data) {
            if (chunkNumber > 0 && chunkNumber <= totalChunks) {
                chunks[chunkNumber - 1] = data;
            }
        }

        public String combineChunks() {
            StringBuilder combinedData = new StringBuilder();
            for (int i = 0; i < chunks.length; i++) {
                if (chunks[i] != null) {
                    logger.info("청크 [{}] : {}", i + 1, chunks[i].length());
                    combinedData.append(chunks[i]);
                } else {
                    logger.error("청크 [{}] 누락됨", i + 1);
                }
            }
            return combinedData.toString();
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

    // 이미지 디렉토리 및 DB 삭제 로직
    private void checkAndDeleteFiles() {
        long secondsSinceLastReceive = Instant.now().getEpochSecond() - lastReceivedTime.getEpochSecond();
        if (secondsSinceLastReceive > TIMEOUT_SECONDS && !isDirectoryDeleted) {
            logger.info("3초 이상 이미지 수신 없음. 디렉토리 및 DB 정리 시작...");
            deleteFilesInDirectory(directoryPath);
            deleteDatabase();
            isDirectoryDeleted = true;
        }
    }

    // 이미지 디렉토리 삭제 함수
    private void deleteFilesInDirectory(String directoryPath) {
        try {
            java.nio.file.Files.walk(java.nio.file.Paths.get(directoryPath))
                    .filter(java.nio.file.Files::isRegularFile)
                    .map(java.nio.file.Path::toFile)
                    .forEach(file -> {
                        if (file.delete()) {
                            logger.info("파일 삭제 성공: {}", file.getAbsolutePath());
                        } else {
                            logger.error("파일 삭제 실패: {}", file.getAbsolutePath());
                        }
                    });
        } catch (IOException e) {
            logger.error("디렉토리 정리 중 오류 발생: {}", e.getMessage());
        }
    }

    // DB 데이터 삭제 함수
    private void deleteDatabase() {
        try {
            // image 테이블
            imageRepository.deleteAll();
            // info 테이블
            infoRepository.deleteAll();
            // info2 테이블
            info2Repository.deleteAll();
            logger.info("DB에서 image 테이블의 모든 데이터 삭제 완료");
        } catch (Exception e) {
            logger.error("DB 데이터 삭제 중 오류 발생: {}", e.getMessage());
        }
    }
}