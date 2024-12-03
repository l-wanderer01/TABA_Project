package com.example.taba_project.service;

import com.example.taba_project.DTO.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UserService {
    private final UserRepository userRepository; // jpa, MySQL dependency 추가
    public void save(UserDTO userDTO) {
        // request -> DTO -> Entity -> Repository에서 save
        User user = User.toUser(userDTO);
        userRepository.save(user);
        // Repository의 save 메서드 호출 (조건. entity 객체를 넘겨줘야 함)
    }
}