package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 红楼梦人物
 */
@Data
@TableName("honglou_characters")
public class HonglouCharacter {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人物姓名 */
    private String name;

    /** 人物昵称/别称 */
    private String nickname;

    /** 人物角色（主角/重要配角/一般角色） */
    private String role;

    /** 外貌描述 */
    private String appearanceDescription;

    /** 性格特点 */
    private String personalityTraits;

    /** 命运概述（对应判词/结局） */
    private String fateSummary;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
