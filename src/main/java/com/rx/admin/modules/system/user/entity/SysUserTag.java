package com.rx.admin.modules.system.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户标签实体
 */
@Data
@TableName("sys_user_tag")
public class SysUserTag implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 标签名称 */
    private String tagName;
    
    /** 标签颜色 */
    private String tagColor;
    
    /** 标签描述 */
    private String description;
    
    /** 状态：1-启用，0-禁用 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}
