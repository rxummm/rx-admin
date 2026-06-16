package com.rx.admin.modules.auth.vo;

import lombok.Data;

@Data
public class LoginResponseVO {
    private String token;
    private String tokenName;
    private UserInfoVO userInfo;
}
