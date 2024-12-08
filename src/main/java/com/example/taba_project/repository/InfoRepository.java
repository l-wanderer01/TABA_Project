package com.example.taba_project.repository;

import com.example.taba_project.model.Info;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoRepository extends JpaRepository<Info, Long> {
}