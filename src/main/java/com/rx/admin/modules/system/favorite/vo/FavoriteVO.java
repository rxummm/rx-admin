package com.rx.admin.modules.system.favorite.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 收藏视图对象 */
@Data
public class FavoriteVO {
    private Long id;
    private Long userId;
    private Long menuId;
    private String name;
    private String path;
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
