package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("genre")
public class Genre {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 体裁名称（如：五言绝句、七言律诗、词牌名等） */
    private String name;

    /** 编码 */
    private String code;

    /** 父级ID */
    private Long parentId;

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
