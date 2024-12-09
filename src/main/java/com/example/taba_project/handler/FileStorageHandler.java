package com.example.taba_project.handler;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStorageHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageHandler.class);

    public String saveFile(String directoryPath, String fileName, byte[] data) throws IOException {
        // 디렉토리 경로를 생성
        Path dirPath = Paths.get(directoryPath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath); // 디렉토리가 없으면 생성
            logger.info("디렉토리 생성 : {}", dirPath.toAbsolutePath());
        }

        // 파일 경로 생성 및 저장
        Path filePath = Paths.get(directoryPath, fileName);
        Files.write(filePath, data);
        logger.info("파일 저장 완료: {}", filePath.toAbsolutePath());

        // 저장된 파일의 절대 경로 반환
        return filePath.toAbsolutePath().toString();
    }
}