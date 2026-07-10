package com.rx.admin.modules.system.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户标签关联实体
 */
@Data
@TableName("sys_user_tag_relation")
public class SysUserTagRelation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 标签ID */
    private Long tagId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
