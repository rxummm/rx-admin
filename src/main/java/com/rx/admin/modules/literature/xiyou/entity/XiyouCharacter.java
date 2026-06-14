package com.rx.admin.modules.literature.xiyou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 西游记人物
 */
@Data
@TableName("xiyou_characters")
public class XiyouCharacter {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人物姓名 */
    private String name;

    /** 别名 */
    private String alias;

    /** 身份 */
    private String identity;

    /** 武器 */
    private String weapon;

    /** 种族 */
    private String race;

    /** 主要事迹 */
    private String mainDeeds;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}