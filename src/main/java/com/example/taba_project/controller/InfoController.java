package com.example.taba_project.controller;

import com.example.taba_project.model.Info;
import com.example.taba_project.repository.InfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info")
public class InfoController {

    @Autowired
    private InfoRepository infoRepository;

    // 새로운 Info 저장
    @PostMapping
    public Info saveInfo(@RequestBody Info info) {
        System.out.println("Received info: " + info);
        return infoRepository.save(info);
    }

}
