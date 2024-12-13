package com.example.taba_project.repository;

import com.example.taba_project.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ImageRepository extends JpaRepository<Image, Long> {
    // 최신 이미지를 가져오는 쿼리 메서드
    @Query(value = "SELECT * FROM image i ORDER BY i.id_images DESC LIMIT 1", nativeQuery = true)
    Image findLatestImage();
}