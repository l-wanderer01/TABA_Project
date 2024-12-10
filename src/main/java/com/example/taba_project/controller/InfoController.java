package com.example.taba_project.controller;

import com.example.taba_project.repository.InfoRepository;
import com.example.taba_project.model.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/info")
public class InfoController {

    @Autowired
    private InfoRepository infoRepository;

    // POST 요청으로 새로운 Info 저장 및 텍스트 생성
    @PostMapping
    public String saveInfo(@RequestBody Info info) {
        try {
            System.out.println("Received info: " + info);

            // DB에 저장
            infoRepository.save(info);

            // 감정 메시지 생성
            String emotionMessage = generateEmotionMessage(info.getEmotion(), info.getPercentage());

            // 로그에 출력
            System.out.println("Generated emotion message: " + emotionMessage);

            // 결과 반환
            return emotionMessage;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return "Error occurred";
        }
    }

    // GET 요청으로 ID 기반 Info 데이터 조회 및 메시지 생성
    @GetMapping("/result")
    public String getLatestInfo() {
        Info latestInfo = infoRepository.findFirstByOrderByCreatedAtDesc();

        if (latestInfo == null) {
            return "데이터가 없습니다.";
        }

        // 감정 메시지 생성
        String emotionMessage = generateEmotionMessage(latestInfo.getEmotion(), latestInfo.getPercentage());
        return emotionMessage;
    }

    private String generateEmotionMessage(String emotion, Double percentage) {
        String message = "";
        String intensity = getIntensity(percentage);

        switch (emotion.toLowerCase()) {
            case "happy":
                message = "상대가 " + intensity + " 행복해 합니다.";
                break;
            case "surprise":
                message = "상대가 " + intensity + " 놀랐습니다.";
                break;
            case "무표정":
                message = "상대가 " + intensity + " 무표정입니다.";
                break;
            case "gross":
                message = "상대가 " + intensity + " 불쾌한 상태입니다.";
                break;
            case "angry":
                message = "상대가 " + intensity + " 화가 난 상태입니다.";
                break;
            case "sad":
                message = "상대가 " + intensity + " 슬퍼합니다.";
                break;
            default:
                message = "알 수 없는 감정입니다.";
        }

        return message;
    }

    private String getIntensity(Double percentage) {
        if (percentage >= 90) {
            return "극도로";
        } else if (percentage >= 75) {
            return "매우";
        } else if (percentage >= 60) {
            return "상당히";
        } else if (percentage >= 50) {
            return "조금";
        } else {
            return "미미하게";
        }
    }
}