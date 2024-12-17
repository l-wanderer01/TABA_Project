package com.example.taba_project.controller;

import com.example.taba_project.model.Info2;
import com.example.taba_project.repository.Info2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info2")
public class Info2Controller {

    @Autowired
    private Info2Repository info2Repository;

    // Info2 저장
    @PostMapping
    public void saveInfo(@RequestBody Info2 info2) {
        try {
            System.out.println("Received info: " + info2);

            // DB에 저장
            info2Repository.save(info2);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 최신 Info2 가져오기 및 메시지 반환
    @GetMapping("/move")
    public String getLatestInfo2() {
        Info2 latestInfo2 = info2Repository.findFirstByOrderByCreatedAtDesc();

        if (latestInfo2 == null) {
            return "데이터가 없습니다.";
        }

        // 메시지 생성
        return generateMoveMessage(latestInfo2);
    }

    private String generateMoveMessage(Info2 info2) {
        String message;
        double confidence = info2.getConfidence();

        if (confidence < 0.3) {
            message = "위험한 물체가 없습니다.";
        } else {
            // 객체 타입 확인
            String objectType = info2.getClass_id() == 0 ? "사람" : "사물";

            // 거리 계산
            double averageDistance = Math.abs((info2.getX_max() - info2.getX_min() + info2.getY_max() - info2.getY_min()) / 2.0);
            String distanceDescription = getDistanceDescription(averageDistance);

            message = String.format("감지된 객체는 %s이며, %s 위치에 있습니다.", objectType, distanceDescription);
        }

        return message;
    }

    private String getDistanceDescription(double averageDistance) {
        if (averageDistance < 25.0) {
            return "매우 가까운";
        } else if (averageDistance < 50.0) {
            return "가까운";
        } else if (averageDistance < 100.0) {
            return "중간 거리의";
        } else {
            return "먼";
        }
    }
}
