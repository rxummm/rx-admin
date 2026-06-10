package com.rx.admin.modules.system.menu.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/** 菜单视图对象 */
@Data
public class MenuVO {
    private Long id;
    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<MenuVO> children;
}
