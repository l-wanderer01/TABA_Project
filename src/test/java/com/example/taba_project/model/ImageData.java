package com.example.taba_project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {

    private int width;               // 이미지 너비
    private int height;              // 이미지 높이
    private ImageFormat format;      // 이미지 포맷 (Enum 사용)
    private byte[] imageData;        // 바이너리 이미지 데이터
    //private int state;

    public boolean isValid() {
        return width > 0 && height > 0 && format != null && imageData != null && imageData.length > 0;
    }

    public enum ImageFormat {
        YUV_420, JPEG, PNG;          // 허용 가능한 이미지 포맷
    }
}
