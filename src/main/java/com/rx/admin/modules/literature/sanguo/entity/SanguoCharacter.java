package com.rx.admin.modules.literature.sanguo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 三国演义人物
 */
@Data
@TableName("sanguo_characters")
public class SanguoCharacter {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人物姓名 */
    private String name;

    /** 字 */
    private String courtesyName;

    /** 号 */
    private String styleName;

    /** 绰号 */
    private String nickname;

    /** 角色（君主/武将/谋士/文官/女性等） */
    private String role;

    /** 所属国家 */
    private String country;

    /** 官职/地位 */
    private String position;

    /** 武器 */
    private String weapon;

    /** 籍贯 */
    private String hometown;

    /** 外貌描述 */
    private String appearanceDescription;

    /** 性格特点 */
    private String personalityTraits;

    /** 命运概述 */
    private String fateSummary;

    /** 著名事迹 */
    private String notableEvents;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}