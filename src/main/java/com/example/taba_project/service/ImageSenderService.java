package com.example.taba_project.service;

import com.example.taba_project.model.Image;
import com.example.taba_project.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;

@Service
public class ImageSenderService {

    private final String fastApiUrl = "http://127.0.0.1:8001/predict/";

    @Autowired
    private ImageRepository imageRepository;

    public void sendLatestImageToFastApi() {
        try {
            // 최신 이미지 URL 가져오기
            Image latestImage = imageRepository.findLatestImage();
            if (latestImage == null) {
                System.out.println("이미지가 없습니다.");
                return;
            }

            String imagePath = latestImage.getUrl(); // 이미지 경로 가져오기

            // 기존 경로에서 /home/ubuntu/userimage/ 부분을 /app/userimage/로 변경
            String updatedImagePath = imagePath.replace("/home/ubuntu/userimage/", "/app/userimage/");

            // 변경된 경로 출력 (디버깅용)
            System.out.println("변경된 이미지 경로: " + updatedImagePath);

            // 이미지 파일 준비 (변경된 경로 사용)
            FileSystemResource fileResource = new FileSystemResource(updatedImagePath);
            if (!fileResource.exists()) {
                System.out.println("이미지 파일이 존재하지 않습니다: " + updatedImagePath);
                return;
            }

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 요청 데이터 설정
            org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // FastAPI로 POST 요청 전송
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(fastApiUrl, requestEntity, String.class);
            System.out.println("FastAPI 응답: " + response);

        } catch (Exception e) {
            System.err.println("FastAPI로 이미지 전송 중 오류 발생: " + e.getMessage());
        }
    }
}