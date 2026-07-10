package com.rx.admin.modules.system.favorite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteCreateDTO {
    @NotBlank(message = "名称不能为空")
    private String name;
    @NotBlank(message = "路径不能为空")
    private String path;
    private String icon;
    @NotNull(message = "菜单ID不能为空")
    private Long menuId;
}
