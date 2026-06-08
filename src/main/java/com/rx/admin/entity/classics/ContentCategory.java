package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_category")
public class ContentCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称（如：诗、词、曲、赋、文言文等） */
    private String name;

    /** 编码 */
    private String code;

    /** 描述 */
    private String description;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0禁用 1启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
