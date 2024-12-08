package com.example.taba_project.service;

import com.example.taba_project.model.ImageData;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageProcessingService {

    // 환경 변수 또는 사용자 홈 디렉토리에 저장 디렉토리 설정
    private static final String IMAGE_DIR = System.getProperty("user.home") + "/userimage/";

    public String saveImage(ImageData imageData) throws IOException {
        // 이미지 데이터 유효성 검사
        if (!imageData.isValid()) {
            throw new IllegalArgumentException("유효하지 않은 이미지 데이터입니다.");
        }

        // 저장 디렉토리 생성
        File directory = new File(IMAGE_DIR);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("이미지 저장 디렉토리 생성 실패: " + IMAGE_DIR);
        }

        // 파일 확장자 설정
        String fileExtension = imageData.getFormat().name().toLowerCase();
        String fileName = UUID.randomUUID() + "." + fileExtension;
        File file = new File(directory, fileName);

        // 이미지 데이터를 파일로 저장
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageData.getImageData());
        } catch (IOException e) {
            throw new IOException("이미지 저장 실패: " + file.getAbsolutePath(), e);
        }

        return file.getAbsolutePath(); // 저장된 파일 경로 반환
    }
}