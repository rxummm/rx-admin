package com.rx.admin.modules.auth.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String nickname;
    private String email;
    private String phone;
    private Integer gender;
    private String password;
    private String oldPassword;
}
