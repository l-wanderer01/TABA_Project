package com.example.taba_project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {
    private int width;
    private int height;
    private String format;
    private int planes;
    private byte[] imageData; // 이미지 바이너리 데이터
}