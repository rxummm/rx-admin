package com.rx.admin.modules.system.user.dto;

import lombok.Data;

/**
 * 用户查询 DTO
 */
@Data
public class UserQueryDTO {

    private String keyword;
    private String username;
    private Integer status;
    private Long deptId;
}
