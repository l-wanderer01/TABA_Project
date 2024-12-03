//package com.example.taba_project.controller;
//
//import com.example.taba_project.model.ImageData;
//import com.example.taba_project.service.ImageProcessingService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.stereotype.Controller;
//
//@Controller
//@RequiredArgsConstructor
//public class ImageWebSocketController {
//
//    private final ImageProcessingService imageProcessingService;
//
//    @MessageMapping("/upload")
//    public void handleImageUpload(ImageData imageData) {
//        try {
//            // Docker 컨테이너로 실시간 업로드
//            imageProcessingService.uploadImageToDocker(imageData.getImageData());
//        } catch (Exception e) {
//            System.err.println("이미지 업로드 실패: " + e.getMessage());
//        }
//    }
//}