package com.rx.admin.modules.tool.wiki.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 协作文档实体
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_collaborative_doc")
public class SysCollaborativeDoc extends BaseEntity {
    /** 文档标题 */
    private String title;
    
    /** 文档内容（Markdown格式） */
    private String content;
    
    /** 所属空间ID */
    private Long spaceId;
    
    /** 创建者ID */
    private Long creatorId;
    
    /** 创建者姓名 */
    private String creatorName;
    
    /** 最后编辑者ID */
    private Long lastEditorId;
    
    /** 最后编辑者姓名 */
    private String lastEditorName;
    
    /** 最后编辑时间 */
    private LocalDateTime lastEditTime;
    
    /** 文档状态：draft-草稿，published-已发布，archived-已归档 */
    private String status;
    
    /** 浏览次数 */
    private Integer viewCount;
    
    /** 当前编辑锁（用于防止并发编辑冲突） */
    private String editLock;
    
    /** 锁定时间 */
    private LocalDateTime lockTime;
}
