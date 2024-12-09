//package com.example.taba_project.handler;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.util.Base64;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class ImageWebSocketHandler extends TextWebSocketHandler {
//
//    private static final Logger logger = LoggerFactory.getLogger(ImageWebSocketHandler.class);
//
//    private final FileStorageHandler fileStorageHandler;
//
//    public ImageWebSocketHandler(FileStorageHandler fileStorageHandler) {
//        this.fileStorageHandler = fileStorageHandler;
//    }
//
//    private final ConcurrentHashMap<String, StringBuilder> sessionData = new ConcurrentHashMap<>();
//
//    @Value("${file.storage.directory}")
//    private String directoryPath;
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        logger.info("메시지 수신 - 세션 ID: {}, 메시지 길이: {}", session.getId(), payload.length());
//
//        sessionData.putIfAbsent(session.getId(), new StringBuilder());
//        StringBuilder builder = sessionData.get(session.getId());
//        builder.append(payload);
//
//        if (isLastChunk(payload)) {
//            String base64EncodedData = builder.toString()
//                    .replace("<END>", "")
//                    .replaceAll("\\s", "");
//
//            sessionData.remove(session.getId());
//            logger.info("완전한 Base64 데이터 조합 완료 - 세션 ID: {}", session.getId());
//
//            processBase64Data(base64EncodedData, session);
//        }
//    }
//
//    private boolean isLastChunk(String payload) {
//        return payload.endsWith("<END>");
//    }
//
//    private void processBase64Data(String base64EncodedData, WebSocketSession session) {
//        try {
//            if (base64EncodedData.isEmpty()) {
//                logger.error("Base64 데이터가 비어 있습니다. 세션 ID: {}", session.getId());
//                return;
//            }
//
//            byte[] decodedData = Base64.getDecoder().decode(base64EncodedData);
//
//            BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedData));
//            if (image == null) {
//                logger.error("이미지 유효성 검증 실패 - 세션 ID: {}", session.getId());
//                return;
//            }
//
//            logger.info("이미지 유효성 검증 성공 - 세션 ID: {}", session.getId());
//            saveImage(decodedData, session);
//        } catch (Exception e) {
//            logger.error("이미지 처리 중 오류 - 세션 ID: {}, 이유: {}", session.getId(), e.getMessage());
//        }
//    }
//
//    private void saveImage(byte[] imageData, WebSocketSession session) {
//        try {
//            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
//            String filePath = fileStorageHandler.saveFile(directoryPath, fileName, imageData);
//
//            logger.info("이미지 저장 성공 - 세션 ID: {}, 경로: {}", session.getId(), filePath);
//            session.sendMessage(new TextMessage("이미지 저장 성공: " + filePath));
//        } catch (Exception e) {
//            logger.error("이미지 저장 실패 - 세션 ID: {}, 이유: {}", session.getId(), e.getMessage());
//            try {
//                session.sendMessage(new TextMessage("이미지 저장 실패: " + e.getMessage()));
//            } catch (Exception sendError) {
//                logger.error("WebSocket 응답 전송 실패 - 세션 ID: {}", session.getId());
//            }
//        }
//    }
//}

package com.example.taba_project.handler;

import com.example.taba_project.handler.FileStorageHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImageWebSocketHandler extends TextWebSocketHandler {

    // 세션별 메시지 조합을 위한 맵
    private final ConcurrentHashMap<String, StringBuilder> sessionData = new ConcurrentHashMap<>();

    @Value("${file.storage.directory}")
    private String directoryPath;

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
        } catch (Exception e) {
            System.err.println("이미지 저장 실패: " + e.getMessage());
        }
    }
}