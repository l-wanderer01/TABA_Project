package com.example.taba_project.controller;

import com.example.taba_project.model.Info2;
import com.example.taba_project.repository.Info2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info2")
public class Info2Controller {

    @Autowired
    private Info2Repository info2Repository;

    // 새로운 Info 저장
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
}
