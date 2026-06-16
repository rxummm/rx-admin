package com.rx.admin.modules.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用户创建 DTO
 * 只包含客户端可传入的字段，不暴露 Entity 内部字段
 */
@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private String avatar;
    private Integer gender;
    private Integer status;
    private Long deptId;
    private List<Long> roleIds;
    private String remark;
}
