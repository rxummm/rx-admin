package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.handler.AesTypeHandler;
import com.rx.admin.common.base.BaseEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user", autoResultMap = true)
public class SysUser extends BaseEntity {
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @TableField(typeHandler = AesTypeHandler.class)
    @Email(message = "邮箱格式不正确")
    private String email;

    @TableField(typeHandler = AesTypeHandler.class)
    private String phone;

    private String avatar;
    private Integer gender;
    private Integer status;
    private Long deptId;
    private LocalDateTime passwordUpdateTime;
    private Integer loginFailCount;
    private LocalDateTime lockUntil;
    private String remark;
}