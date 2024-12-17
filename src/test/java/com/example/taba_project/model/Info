package com.example.taba_project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Entity
public class Info {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer age;

    private String emotion;

    private String gender;

    private Double percentage;

    private LocalDateTime createdAt;

    // 생성 시간 자동 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
