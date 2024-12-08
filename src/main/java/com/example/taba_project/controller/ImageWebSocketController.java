package com.example.taba_project.controller;

import com.example.taba_project.model.ImageData;
import com.example.taba_project.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ImageWebSocketController {

    private final ImageProcessingService imageProcessingService;

    @MessageMapping("/upload")
    public void handleImageUpload(@Payload ImageData imageData) {
        try {
            // 서비스 계층으로 이미지 데이터 전달 및 저장
            String savedPath = imageProcessingService.saveImage(imageData);
            System.out.println("이미지 저장 경로: " + savedPath);
        } catch (Exception e) {
            System.err.println("이미지 처리 중 오류: " + e.getMessage());
        }
    }
}

//package com.example.taba_project.controller;
//
//import com.example.taba_project.model.ImageData;
//import com.example.taba_project.service.ImageProcessingService;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@RequiredArgsConstructor
//public class ImageWebSocketController {
//
//    private static final Logger logger = LoggerFactory.getLogger(ImageWebSocketController.class);
//
//    private final ImageProcessingService imageProcessingService;
//    private final SimpMessagingTemplate messagingTemplate; // WebSocket 응답용
//
//    @MessageMapping("/upload")
//    public void handleImageUpload(@Payload ImageData imageData) {
//        try {
//            // 데이터 검증 (예: 이미지 데이터나 이름이 비어 있지 않은지 확인)
//            if (imageData == null || imageData.getFileName() == null || imageData.getFileData() == null) {
//                throw new IllegalArgumentException("유효하지 않은 이미지 데이터");
//            }
//
//            // 서비스 계층으로 이미지 데이터 전달 및 저장
//            String savedPath = imageProcessingService.saveImage(imageData);
//            logger.info("이미지 저장 경로: {}", savedPath);
//
//            // WebSocket 클라이언트로 응답 전송
//            messagingTemplate.convertAndSend("/topic/image-response", "이미지 저장 성공: " + savedPath);
//
//        } catch (Exception e) {
//            logger.error("이미지 처리 중 오류: {}", e.getMessage());
//            messagingTemplate.convertAndSend("/topic/image-response", "이미지 처리 실패: " + e.getMessage());
//        }
//    }
//}