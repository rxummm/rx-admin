package com.rx.admin.modules.system.role.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/** 角色视图对象（不含敏感字段） */
@Data
public class RoleVO {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer sort;
    private Integer status;
    private Integer dataScope;
    private String dataDeptIds;
    private List<Long> menuIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
