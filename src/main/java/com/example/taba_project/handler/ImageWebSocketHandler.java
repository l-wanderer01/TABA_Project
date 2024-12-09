package com.example.taba_project.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImageWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImageWebSocketHandler.class);

    private final FileStorageHandler fileStorageHandler;

    public ImageWebSocketHandler(FileStorageHandler fileStorageHandler) {
        this.fileStorageHandler = fileStorageHandler;
    }

    private final ConcurrentHashMap<String, StringBuilder> sessionData = new ConcurrentHashMap<>();

    @Value("${file.storage.directory}")
    private String directoryPath;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("메시지 수신 - 세션 ID: {}, 메시지 길이: {}", session.getId(), payload.length());

        sessionData.putIfAbsent(session.getId(), new StringBuilder());
        StringBuilder builder = sessionData.get(session.getId());
        builder.append(payload);

        if (isLastChunk(payload)) {
            String base64EncodedData = builder.toString()
                    .replace("<END>", "")
                    .replaceAll("\\s", "");

            sessionData.remove(session.getId());
            logger.info("완전한 Base64 데이터 조합 완료 - 세션 ID: {}", session.getId());

            processBase64Data(base64EncodedData, session);
        }
    }

    private boolean isLastChunk(String payload) {
        return payload.endsWith("<END>");
    }

    private void processBase64Data(String base64EncodedData, WebSocketSession session) {
        try {
            if (base64EncodedData.isEmpty()) {
                logger.error("Base64 데이터가 비어 있습니다. 세션 ID: {}", session.getId());
                return;
            }

            byte[] decodedData = Base64.getDecoder().decode(base64EncodedData);

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedData));
            if (image == null) {
                logger.error("이미지 유효성 검증 실패 - 세션 ID: {}", session.getId());
                return;
            }

            logger.info("이미지 유효성 검증 성공 - 세션 ID: {}", session.getId());
            saveImage(decodedData, session);
        } catch (Exception e) {
            logger.error("이미지 처리 중 오류 - 세션 ID: {}, 이유: {}", session.getId(), e.getMessage());
        }
    }

    private void saveImage(byte[] imageData, WebSocketSession session) {
        try {
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            String filePath = fileStorageHandler.saveFile(directoryPath, fileName, imageData);

            logger.info("이미지 저장 성공 - 세션 ID: {}, 경로: {}", session.getId(), filePath);
            session.sendMessage(new TextMessage("이미지 저장 성공: " + filePath));
        } catch (Exception e) {
            logger.error("이미지 저장 실패 - 세션 ID: {}, 이유: {}", session.getId(), e.getMessage());
            try {
                session.sendMessage(new TextMessage("이미지 저장 실패: " + e.getMessage()));
            } catch (Exception sendError) {
                logger.error("WebSocket 응답 전송 실패 - 세션 ID: {}", session.getId());
            }
        }
    }
}