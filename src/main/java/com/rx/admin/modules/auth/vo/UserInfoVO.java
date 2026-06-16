package com.rx.admin.modules.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;
    private LocalDateTime createTime;
    private List<String> roles;
    private List<String> perms;
}
