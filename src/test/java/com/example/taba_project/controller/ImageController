package com.example.taba_project.controller;

import com.example.taba_project.repository.ImageRepository;
import com.example.taba_project.model.Image;
import com.example.taba_project.service.ImageSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageSenderService imageSenderService;

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

    @GetMapping("/send")
    public String sendLatestImage(@RequestParam("mode") String mode) {
        if (!mode.equalsIgnoreCase("move") && !mode.equalsIgnoreCase("chat")) {
            return "유효하지 않은 모드입니다. 'move' 또는 'chat' 중 하나를 선택하세요.";
        }
        imageSenderService.sendLatestImageToFastApi(mode);
        return "이미지 전송 중... 모드: " + mode;
    }
}
