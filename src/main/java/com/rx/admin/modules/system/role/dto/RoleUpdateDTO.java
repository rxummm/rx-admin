package com.rx.admin.modules.system.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 角色更新请求 */
@Data
public class RoleUpdateDTO {
    @NotNull(message = "角色ID不能为空")
    private Long id;
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
