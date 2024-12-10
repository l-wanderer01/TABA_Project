package com.example.taba_project.controller;

import com.example.taba_project.repository.ImageRepository;
import com.example.taba_project.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @PostMapping
    public String saveUrl(@RequestBody Image image) {
        try {
            // DB에 저장
            imageRepository.save(image);

            // 결과 반환
            return image.getUrl();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
