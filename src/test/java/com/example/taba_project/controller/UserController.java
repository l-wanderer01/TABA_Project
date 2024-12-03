package com.example.taba_project.controller;

import com.example.taba_project.DTO.UserDTO;
import com.example.taba_project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;

// @RestController
@Controller
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;

    // 회원가입 페이지 출력 요청 - GetMapping으로 출력 요청 -> PostMapping에서 form에 대한 action 수행
    @GetMapping("/save") // web에 대한 예시라 app으로 연동하기 위해 수정 필요, AWS 서버 ip 주소 입력하면 될듯
    public String saveForm() {
        return "save";
    }

    @PostMapping("/save") // postmapping 주소 수정 필요
    public String join(@ModelAttribute UserDTO userDTO) {
        System.out.println("UserController.save");
        System.out.println("userDTO = " + userDTO);
        userService.save(userDTO);

        return "index";
    }
}

//package com.example.demo.controller;
//
//import com.example.demo.model.User;
//import com.example.demo.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api")
//public class UserController {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @PostMapping("/user")
//    public User create(@RequestBody User user) {
//        return userRepository.save(user);
//    }
//
//    @GetMapping("/user/{id}")
//    public String read(@PathVariable Long id) {
//
//        Optional<User> userOptional = userRepository.findById(id);
//        userOptional.ifPresent(System.out::println);
//
//        return "successfully executed";
//    }
//}
