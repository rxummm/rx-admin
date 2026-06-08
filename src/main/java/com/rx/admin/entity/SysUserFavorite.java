package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sys_user_favorite")
public class SysUserFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long menuId;
    private String name;
    private String path;
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
