package com.example.taba_project.controller;

import com.example.taba_project.model.Info;
import com.example.taba_project.model.Info2;
import com.example.taba_project.repository.Info2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secondinfo")
public class Info2Controller {

    @Autowired
    private Info2Repository info2Repository;

    // 새로운 Info 저장
    @PostMapping
    public String saveInfo(@RequestBody Info2 info2) {
        try {
            System.out.println("Received info: " + info2);

            // DB에 저장
            info2Repository.save(info2);

            // 경고 메시지 생성
            String warningMessage = generateWarningMessage(info2.getLocation());

            // 로그에 출력
            System.out.println("Generated warning Message: " + warningMessage);

            // 결과 반환
            return warningMessage;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return "Error occurred";
        }
    }

    private String generateWarningMessage(String location) {
        String message = "";

        String[] points  = location.split(",");
        //여기서 좌표들 가지고 거리 대비 메세지 스크립트 작성하기 , 테스트 우선 필요할듯


        return message;
    }
}
