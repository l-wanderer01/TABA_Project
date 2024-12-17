package com.example.taba_project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Entity
public class Info2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long class_id;

    private Double confidence;

    private Double x_min;

    private Double y_min;

    private Double x_max;

    private Double y_max;

    private LocalDateTime created_at;

    // 생성 시간 자동 설정
    @PrePersist
    protected void onCreate() {
        this.created_at = LocalDateTime.now();
    }
}