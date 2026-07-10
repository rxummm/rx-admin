package com.rx.admin.modules.content.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体
 */
@Data
@TableName("sys_comment")
public class SysComment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关联的业务类型（如：notice, message, log等） */
    private String targetType;
    
    /** 关联的业务ID */
    private Long targetId;
    
    /** 评论内容 */
    private String content;
    
    /** 评论者ID */
    private Long userId;
    
    /** 评论者用户名 */
    private String username;
    
    /** 父评论ID（用于回复） */
    private Long parentId;
    
    /** 状态：0-待审核，1-已发布，2-已删除 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}
