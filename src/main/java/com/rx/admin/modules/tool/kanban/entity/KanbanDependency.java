package com.rx.admin.modules.tool.kanban.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 看板任务依赖实体
 */
@Data
@TableName("kanban_dependency")
public class KanbanDependency implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 任务ID */
    private Long cardId;
    
    /** 依赖的任务ID */
    private Long dependsOnId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
