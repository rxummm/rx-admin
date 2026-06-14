package com.rx.admin.modules.system.permission.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysPermissionRequestVO {
    private Long id;
    private Long userId;
    private String username;
    private String menuIds;
    private String menuNames;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
