package com.rx.admin.modules.system.menu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 菜单创建请求 */
@Data
public class MenuCreateDTO {
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    private Integer menuType;
    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}
