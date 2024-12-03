//package com.example.demo.handler;
//
//// import org.springframework.security.crypto.codec.Base64;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.util.concurrent.ConcurrentHashMap;
//
///*
//- Websocket Handler 작성
//- 소켓 통신은 서버와 클라이언트가 1:n 관계를 가지기에 한 서버에 여러 클라이언트 접속 가능
//- 서버에서 여러 클라이언트가 발송한 메세지를 받아 처리해줄 핸들러가 필요
//- TextWebSocketHandler를 상속받아 핸들러 작성
// */
//@Component
//public class ImageWebSocketHandler extends TextWebSocketHandler {
//
//    // 세션별 메시지 조합을 위한 맵
//    private final ConcurrentHashMap<String, StringBuilder> sessionData = new ConcurrentHashMap<>();
//
//
//    private boolean isLastChunk(String payload) {
//        // 마지막 청크 확인 로직
//        // 클라이언트에서 마지막 청크를 구분하는 방법에 맞게 구현 (예: 끝에 특수 문자 추가)
//        return payload.endsWith("<END>");
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//
//        // 세션별 데이터 저장
//        sessionData.putIfAbsent(session.getId(), new StringBuilder());
//        StringBuilder builder = sessionData.get(session.getId());
//        builder.append(payload);
//
//        // 마지막 청크 확인
//        if (isLastChunk(payload)) {
//            // 조립된 Base64 데이터
//            String base64EncodedData = builder.toString().replace("<END>", "").trim().replace("\\s", "").trim();
//            System.out.println(base64EncodedData);
//            // byte[] base64EncodedData = base64EncodedData.
//            // Base64 인코딩은 정상적으로 되었다는 것을 확인
////            try {
////                java.util.Base64.getDecoder().decode(base64EncodedData);
////                System.out.println("Base64 데이터 디코딩 가능.");
////            } catch (IllegalArgumentException e) {
////                System.err.println("Base64 디코딩 불가: " + e.getMessage());
////                return;
////            }
//
//            try {
//                // Base64 디코딩
//                byte[] base64DecodedData = java.util.Base64.getDecoder().decode(base64EncodedData);
//
//                // 디코딩된 데이터를 다시 인코딩하여 원본과 비교
//                String reEncodedBase64 = java.util.Base64.getEncoder().encodeToString(base64DecodedData);
//                if (!reEncodedBase64.equals(base64EncodedData)) {
//                    System.err.println("Base64 데이터 검증 실패: 디코딩 후 다시 인코딩한 데이터가 일치하지 않습니다.");
//                    return;
//                }
//
//                // 이미지 유효성 검증
//                BufferedImage img = ImageIO.read(new ByteArrayInputStream(base64DecodedData));
//                if (img == null) {
//                    System.err.println("이미지 유효성 검증 실패: 유효하지 않은 이미지 데이터입니다.");
//                    return;
//                }
//                System.out.println("이미지 유효성 검증 성공.");
//
//                // 로컬에 저장
//                final String DIRECTORY_PATH = "/Users/jaeh/Downloads/socket";
//                FileStorageHandler.saveFile(DIRECTORY_PATH, "image_" + System.currentTimeMillis() + ".jpg", base64DecodedData);
//                System.out.println("이미지 저장 성공: " + DIRECTORY_PATH);
//
//            } catch (IllegalArgumentException e) {
//                System.err.println("Base64 디코딩 실패: " + e.getMessage());
//            } catch (Exception e) {
//                System.err.println("이미지 저장 실패: " + e.getMessage());
//            }
//        }
//    }
//}


/* 수 정 */
package com.example.taba_project.handler;

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
            final String DIRECTORY_PATH = "/home/ubuntu/userimage";
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            FileStorageHandler.saveFile(DIRECTORY_PATH, fileName, imageData);
            System.out.println("이미지 저장 성공: " + DIRECTORY_PATH + "/" + fileName);
        } catch (Exception e) {
            System.err.println("이미지 저장 실패: " + e.getMessage());
        }
    }
}