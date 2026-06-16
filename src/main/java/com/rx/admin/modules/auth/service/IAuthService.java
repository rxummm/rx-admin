package com.rx.admin.modules.auth.service;

import com.rx.admin.modules.auth.vo.LoginResponseVO;
import com.rx.admin.modules.auth.vo.UserInfoVO;

import java.util.Map;

public interface IAuthService {
    LoginResponseVO login(String username, String password);

    void register(String username, String password, String nickname);

    void logout();

    UserInfoVO getUserInfo();

    void updateProfile(String nickname, String email, String phone, Integer gender,
                       String newPassword, String oldPassword);

    Map<String, Object> getRouters();
}
