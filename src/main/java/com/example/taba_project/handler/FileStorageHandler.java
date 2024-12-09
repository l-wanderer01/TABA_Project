//package com.example.taba_project.handler;
//
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@Component
//public class FileStorageHandler {
//
//    public String saveFile(String directoryPath, String fileName, byte[] data) throws IOException {
//        // 디렉토리 경로를 생성
//        Path dirPath = Paths.get(directoryPath);
//        if (!Files.exists(dirPath)) {
//            Files.createDirectories(dirPath); // 디렉토리가 없으면 생성
//        }
//
//        // 파일 경로 생성 및 저장
//        Path filePath = Paths.get(directoryPath, fileName);
//        Files.write(filePath, data);
//        System.out.println("파일 저장 완료: " + filePath.toAbsolutePath());
//
//        // 저장된 파일의 절대 경로 반환
//        return filePath.toAbsolutePath().toString();
//    }
//}

package com.example.taba_project.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileStorageHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageHandler.class);

    public String saveFile(String directoryPath, String fileName, byte[] data) throws IOException {
        try {
            // 디렉토리 경로 생성
            Path dirPath = Paths.get(directoryPath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath); // 디렉토리가 없으면 생성
                logger.info("디렉토리 생성: {}", dirPath.toAbsolutePath());
            }

            // 고유 파일 이름 생성 (중복 방지)
            String uniqueFileName = generateUniqueFileName(fileName);
            Path filePath = Paths.get(directoryPath, uniqueFileName);

            // 파일 데이터 저장
            Files.write(filePath, data);
            logger.info("파일 저장 완료: {}", filePath.toAbsolutePath());

            // 저장된 파일의 절대 경로 반환
            return filePath.toAbsolutePath().toString();
        } catch (IOException e) {
            logger.error("파일 저장 실패 - 디렉토리: {}, 파일: {}", directoryPath, fileName, e);
            throw new IOException("파일 저장 중 오류 발생", e);
        }
    }

    // 고유 파일 이름 생성 (UUID 기반)
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex); // 확장자 추출
        }
        return UUID.randomUUID().toString() + extension;
    }
}