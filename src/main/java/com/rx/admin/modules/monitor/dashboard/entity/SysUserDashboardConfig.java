package com.rx.admin.modules.monitor.dashboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户仪表盘配置实体
 */
@Data
@TableName("sys_user_dashboard_config")
public class SysUserDashboardConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 组件类型 */
    private String widgetType;
    
    /** 组件标题 */
    private String widgetTitle;
    
    /** 排序序号 */
    private Integer sortOrder;
    
    /** 是否启用 */
    private Integer enabled;
    
    /** 组件配置（JSON格式） */
    private String config;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}
