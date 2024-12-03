//package com.example.taba_project.service;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Service
//public class ImageProcessingService {
//
//    public void uploadImageToDocker(byte[] imageData) {
//        try {
//            WebClient.create("http://docker-container-url:port/upload") // docker container url 및 port 번호 필요
//                    .post()
//                    .bodyValue(imageData)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//        } catch (Exception e) {
//            throw new RuntimeException("Docker 업로드 실패: " + e.getMessage());
//        }
//    }
//}