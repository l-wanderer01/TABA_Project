package com.example.taba_project.controller;

import com.example.taba_project.handler.FileStorageHandler;
import com.example.taba_project.model.Image;
import com.example.taba_project.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageRepository imageRepository;
    private final FileStorageHandler fileStorageHandler;
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Value("${file.storage.directory")
    private String directoryPath;

    public ImageController(ImageRepository imageRepository, FileStorageHandler fileStorageHandler) {
        this.imageRepository = imageRepository;
        this.fileStorageHandler = fileStorageHandler;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("fileName") String fileName,
                                              @RequestBody byte[] fileData) {
        try {
            // 파일 저장
            String savedFilePath = fileStorageHandler.saveFile(directoryPath, fileName, fileData);

            // DB에 저장
            Image image = new Image();
            image.setUrl(savedFilePath); // 파일 경로를 url로 설정
            imageRepository.save(image);

            return ResponseEntity.ok("파일 업로드 및 DB 저장 성공: " + savedFilePath);

        } catch (IOException e) {
            logger.error("파일 저장 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 저장 실패: " + e.getMessage());
        }
    }
}
