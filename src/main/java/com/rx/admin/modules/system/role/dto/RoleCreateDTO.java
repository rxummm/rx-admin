package com.rx.admin.modules.system.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/** 角色创建请求 */
@Data
public class RoleCreateDTO {
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    private String description;
    private Integer sort;
    private Integer status;
    private Integer dataScope;
    private String dataDeptIds;
    private List<Long> menuIds;
}
