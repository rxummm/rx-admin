package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 中国行政区划表 (china_regions)
 */
@Data
@TableName("china_regions")
public class ChinaRegion {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 行政区划代码 */
    private String code;

    /** 名称 */
    private String name;

    /** 层级 1=省 2=市 3=区/县 */
    private Integer level;

    /** 上级行政区划代码 */
    private String parentCode;

    /** 拼音 */
    private String pinyin;

    /** 简称 */
    private String abbreviation;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 排序 */
    private Integer sort;

    /** 状态 0=禁用 1=启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
