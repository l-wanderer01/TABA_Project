package com.example.taba_project.repository;

import com.example.taba_project.model.Info;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoRepository extends JpaRepository<Info, Long> {
    Info findFirstByOrderByCreatedAtDesc(); // createdAt 기준 내림차순 정렬 후 첫 번째 데이터 반환
}