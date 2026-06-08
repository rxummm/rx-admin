package com.rx.admin.entity.classics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 红楼梦人物关系
 */
@Data
@TableName("honglou_character_relations")
public class HonglouCharacterRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 起始人物ID */
    private Long fromCharacterId;

    /** 目标人物ID */
    private Long toCharacterId;

    /** 关系类型 */
    private String relationType;

    /** 关系描述 */
    private String relationDesc;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
