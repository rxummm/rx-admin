package com.rx.admin.modules.system.favorite.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 收藏创建请求 */
@Data
public class FavoriteCreateDTO {
    @NotNull(message = "菜单ID不能为空")
    private Long menuId;
    private String name;
    private String path;
    private String icon;
    private Integer sortOrder;
}
