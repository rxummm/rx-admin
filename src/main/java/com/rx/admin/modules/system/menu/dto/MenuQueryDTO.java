package com.rx.admin.modules.system.menu.dto;

import lombok.Data;

/** 菜单查询参数 */
@Data
public class MenuQueryDTO {
    private String menuName;
    private Integer menuType;
    private Integer status;
}
