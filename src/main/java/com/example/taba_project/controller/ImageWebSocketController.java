//package com.example.demo.controller;
//
//import com.example.demo.model.ImageData;
//import com.example.demo.service.ImageProcessingService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@RequiredArgsConstructor
//public class ImageWebSocketController {
//
//    private final ImageProcessingService imageProcessingService;
//
//    @MessageMapping("/upload")
//    public void handleImageUpload(@Payload ImageData imageData) {
//        try {
//            // 서비스 계층으로 이미지 데이터 전달 및 저장
//            String savedPath = imageProcessingService.saveImage(imageData);
//            System.out.println("이미지 저장 경로: " + savedPath);
//        } catch (Exception e) {
//            System.err.println("이미지 처리 중 오류: " + e.getMessage());
//        }
//    }
//}

package com.example.taba_project.controller;

import com.example.taba_project.model.ImageData;
import com.example.taba_project.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ImageWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ImageWebSocketController.class);

    private final ImageProcessingService imageProcessingService;

    @MessageMapping("/upload") // STOMP 프로토콜을 사용한 메시지 매핑 방식 -> WebSocket API 사용 시 @MessageMapping은 동작 x
    public void handleImageUpload(@Payload ImageData imageData) {
        // 데이터 검증
        if (imageData.getImageData() == null || imageData.getImageData().length == 0) {
            logger.warn("유효하지 않은 이미지 데이터");
            return;
        }

        // 비동기 처리
        try {
            String savedPath = imageProcessingService.saveImage(imageData);
            logger.info("이미지 저장 경로: {}", savedPath);
        } catch (Exception e) {
            logger.error("이미지 처리 중 오류: {}", e.getMessage());
        }
    }
}