package com.rx.admin.modules.literature.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dynasty")
public class Dynasty {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 朝代名称（如：唐、宋、元、明、清等） */
    private String name;

    /** 编码 */
    private String code;

    /** 区域类型 */
    private String regionType;

    /** 起始年份 */
    private Integer startYear;

    /** 结束年份 */
    private Integer endYear;

    /** 简介 */
    private String description;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0禁用 1启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}