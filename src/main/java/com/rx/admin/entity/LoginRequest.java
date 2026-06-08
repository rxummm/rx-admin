package com.rx.admin.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码 UUID */
    private String captchaUuid;

    /** 验证码内容 */
    private String captchaCode;
}