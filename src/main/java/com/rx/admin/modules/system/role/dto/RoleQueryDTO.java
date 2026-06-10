package com.rx.admin.modules.system.role.dto;

import lombok.Data;

/** 角色查询参数 */
@Data
public class RoleQueryDTO {
    private String roleName;
    private String roleCode;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
