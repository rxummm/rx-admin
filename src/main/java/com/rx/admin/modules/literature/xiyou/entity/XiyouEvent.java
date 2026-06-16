package com.rx.admin.modules.literature.xiyou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 西游记九九八十一难
 */
@Data
@TableName("xiyou_events")
public class XiyouEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 第几难 */
    private Integer difficultyNum;

    /** 对应章节号 */
    private Integer chapterNum;

    /** 事件标题 */
    private String title;

    /** 发生地点 */
    private String location;

    /** 妖怪名称 */
    private String monster;

    /** 妖怪武器/法宝 */
    private String monsterWeapon;

    /** 帮手/救兵 */
    private String helper;

    /** 解决方式 */
    private String resolution;

    /** 详细描述 */
    private String detail;

    /** 难度等级 */
    private Integer difficultyLevel;

    /** 事件类型 */
    private String eventType;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}