package com.example.taba_project.service;

import com.example.taba_project.model.Info;
import com.example.taba_project.model.Info2;
import com.example.taba_project.repository.InfoRepository;
import com.example.taba_project.repository.Info2Repository;
import com.example.taba_project.repository.ImageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;

@Service
public class ImageSenderService {

    private final String chatModeUrl = "http://3.38.206.234:5001/predict/"; // AI 모델 ip로 수정 필요
    private final String moveModeUrl = "http://3.38.206.234:8001/predict/"; // AI 모델 ip로 수정 필요

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private InfoRepository infoRepository;

    @Autowired
    private Info2Repository info2Repository;

    /**
     * 이미지를 FastAPI로 전송하고 응답 데이터를 DB에 저장.
     * @param mode 전송할 모드 ("move" 또는 "chat")
     */
    public void sendLatestImageToFastApi(String mode) {
        try {
            // 최신 이미지 가져옴
            var latestImage = imageRepository.findLatestImage();
            if (latestImage == null) {
                System.out.println("이미지가 없습니다.");
                return;
            }

            // 이미지 경로 가져옴
            String imagePath = latestImage.getUrl();
            System.out.println("이미지 경로: " + imagePath);

            // 이미지 파일 준비
            var fileResource = new FileSystemResource(imagePath);
            if (!fileResource.exists()) {
                System.out.println("이미지 파일이 존재하지 않습니다: " + imagePath);
                return;
            }

            // HTTP 요청 준비
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
            body.add("file", fileResource);

            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 모드에 따른 FastAPI URL 설정
            String apiUrl = mode.equalsIgnoreCase("move") ? moveModeUrl : chatModeUrl;

            // FastAPI로 요청 전송
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(apiUrl, requestEntity, String.class);

            System.out.println("FastAPI 응답: " + response);

            // 응답 데이터를 DB에 저장
            saveToDatabase(response, mode);

        } catch (Exception e) {
            System.err.println("FastAPI로 이미지 전송 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * FastAPI 응답 데이터를 모드에 따라 각각 다른 테이블에 저장.
     * @param response FastAPI 응답 데이터 (JSON 문자열)
     * @param mode 모드 ("move" 또는 "chat") // "이동", "대화" 인지 "move", "chat"인지 확인 필요
     */
    private void saveToDatabase(String response, String mode) {
        try {
            // JSON 응답 데이터 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            if (mode.equalsIgnoreCase("대화")) {
                // Info 테이블에 저장
                Info info = new Info();
                info.setAge(jsonNode.get("age").asInt());
                info.setEmotion(jsonNode.get("emotion").asText());
                info.setGender(jsonNode.get("gender").asText());
                info.setPercentage(jsonNode.get("percentage").asDouble());
                infoRepository.save(info);

                System.out.println("Info 데이터 저장 성공: " + info);

            } else if (mode.equalsIgnoreCase("이동")) {
                // Info2 테이블에 저장
                Info2 info2 = new Info2();
                info2.setClass_id(jsonNode.get("class_id").asLong());
                info2.setConfidence(jsonNode.get("confidence").asDouble());
                info2.setX_min(jsonNode.get("x_min").asDouble());
                info2.setY_min(jsonNode.get("y_min").asDouble());
                info2.setX_max(jsonNode.get("x_max").asDouble());
                info2.setY_max(jsonNode.get("y_max").asDouble());
                info2Repository.save(info2);

                System.out.println("Info2 데이터 저장 성공: " + info2);
            }

        } catch (Exception e) {
            System.err.println("DB 저장 중 오류 발생: " + e.getMessage());
        }
    }
}