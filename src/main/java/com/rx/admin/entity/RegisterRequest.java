package com.rx.admin.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[A-Za-z](?=.*\\d).{5,}$", message = "密码需以字母开头，包含数字，至少6位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;
}
