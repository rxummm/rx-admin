package com.rx.admin.modules.monitor.dataVersion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据快照实体
 * 用于数据回滚功能
 */
@Data
@TableName("sys_data_snapshot")
public class SysDataSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 表名 */
    private String tableName;
    
    /** 记录ID */
    private Long recordId;
    
    /** 操作类型：INSERT/UPDATE/DELETE */
    private String operationType;
    
    /** 操作前数据（JSON格式） */
    private String beforeData;
    
    /** 操作后数据（JSON格式） */
    private String afterData;
    
    /** 操作人ID */
    private Long operatorId;
    
    /** 操作人用户名 */
    private String operatorName;
    
    /** 操作时间 */
    private LocalDateTime operateTime;
    
    /** 是否已回滚：0-未回滚，1-已回滚 */
    private Integer rolledBack;
    
    /** 回滚时间 */
    private LocalDateTime rollbackTime;
}
