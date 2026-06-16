package com.rx.admin.modules.system.permission.dto;

import lombok.Data;

@Data
public class EmailPermissionRequestDTO {
    private String userName;
    private String description;
    private String menus;
}
