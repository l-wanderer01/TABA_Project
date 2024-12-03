package com.example.taba_project.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorageHandler {

    public static void saveFile(String directoryPath, String fileName, byte[] data) throws IOException {
        // 디렉토리 경로를 생성
        Path dirPath = Paths.get(directoryPath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath); // 디렉토리가 없으면 생성
        }

        // 파일 경로 생성 및 저장
        Path filePath = Paths.get(directoryPath, fileName);
        Files.write(filePath, data);
        System.out.println("파일 저장 완료: " + filePath.toAbsolutePath());
    }
}