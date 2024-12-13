package com.example.taba_project.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_images") // DB의 id_images 컬럼과 매핑

    private Long idImages;

    private String url;
}
