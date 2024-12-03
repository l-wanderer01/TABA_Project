package com.example.taba_project.handler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // <객체 type, PK type>
public interface UserRepository extends JpaRepository<User, String> {

}