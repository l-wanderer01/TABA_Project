package com.example.taba_project.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString

public class UserDTO {
    private String userID;
    private String pwd;
    private String userName;
    private String userEmail;

    public static UserDTO toUserDTO(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setUserID(user.getUserID());
        userDTO.setPwd(user.getPwd());
        userDTO.setUserName(user.getUserName());
        userDTO.setUserEmail(user.getUserEmail());

        return userDTO;
    }
}
