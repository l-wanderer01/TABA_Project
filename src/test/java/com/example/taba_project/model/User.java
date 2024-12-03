package com.example.taba_project.model;


import jakarta.persistence.*;
import lombok.*;
import com.example.taba_project.DTO.UserDTO;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// 왜 오류가 나는 걸까...

// 해당 클래스는 데이터베이스와 실제 매칭되는 역할을 하는 클래스

@Entity
@Getter
// @Setter
@NoArgsConstructor
@Table(name = "user")

public class User {

    @Id // primary key
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동으로 값 1씩 증가
    private String userID;

    @Column(length = 100)
    private String pwd;

    @Column(length = 100)
    private String userName;

    @Column(length = 100)
    private String userEmail;

    @Builder // 모든 멤버변수를 가지는 생성자가 필요 -> lombok을 사용할 경우 @AllArgsConstructor를 사용하거나 생성자 코드 작성이 필요
    // DTO와 달리 내부 속성 보호를 위해 @Setter 사용을 지양
    public static User toUser(UserDTO userDTO) {
        User user = new User();

        user.userID = userDTO.getUserID();
        user.pwd = userDTO.getPwd();
        user.userName = userDTO.getUserName();
        user.userEmail = userDTO.getUserEmail();

        return user;
    }

}