package com.example.taba_project.controller;

import com.example.taba_project.model.Info;
import com.example.taba_project.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/info")
@RequiredArgsConstructor
public class InfoController {

    private static final Logger logger = LoggerFactory.getLogger(InfoController.class);

    private final InfoRepository infoRepository;

    // 새로운 Info 저장
    @PostMapping
    public ResponseEntity<Info> saveInfo(@RequestBody Info info) {
        if (info.getEmotion() == null || info.getPercentage() == null) {
            logger.warn("유효하지 않은 데이터 요청: {}", info);
            return ResponseEntity.badRequest().body(null);
        }

        if (!isValidPercentage(info.getPercentage())) {
            logger.warn("퍼센트 범위 초과: {}", info.getPercentage());
            return ResponseEntity.badRequest().body(null);
        }

        Info savedInfo = infoRepository.save(info);
        logger.info("Info 저장 성공: {}", savedInfo);
        return ResponseEntity.ok(savedInfo);
    }

    // 특정 Info 조회 및 스크립트 반환
    @GetMapping("/{id}")
    public ResponseEntity<String> getInfoWithScript(@PathVariable Long id) {
        Info info = infoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Info not found with id: " + id));

        try {
            String script = generateEmotionScript(info.getEmotion(), info.getPercentage());
            logger.info("생성된 스크립트: {}", script);
            return ResponseEntity.ok(script);
        } catch (IllegalArgumentException e) {
            logger.error("스크립트 생성 중 오류 - id: {}, 이유: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("스크립트 생성 오류: " + e.getMessage());
        }
    }

    private String generateEmotionScript(String emotion, String percentageStr) {
        double percentage = Double.parseDouble(percentageStr);

        String intensity;
        if (percentage >= 90) {
            intensity = "완전히";
        } else if (percentage >= 75) {
            intensity = "매우";
        } else if (percentage >= 60) {
            intensity = "상당히";
        } else if (percentage >= 50) {
            intensity = "약간";
        } else {
            return "50% 미만 감정은 분석되지 않습니다.";
        }

        return switch (emotion.toLowerCase()) {
            case "행복" -> "상대가 " + intensity + " 행복해 합니다.";
            case "놀람" -> "상대가 " + intensity + " 놀랐습니다.";
            case "무표정" -> "상대가 " + intensity + " 무표정입니다.";
            case "혐오" -> "상대가 " + intensity + " 불쾌한 상태입니다.";
            case "분노" -> "상대가 " + intensity + " 화가 난 상태입니다.";
            case "슬픔" -> "상대가 " + intensity + " 슬퍼합니다.";
            default -> throw new IllegalArgumentException("알 수 없는 감정: " + emotion);
        };
    }

    private boolean isValidPercentage(String percentageStr) {
        try {
            double percentage = Double.parseDouble(percentageStr);
            return percentage >= 0 && percentage <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}