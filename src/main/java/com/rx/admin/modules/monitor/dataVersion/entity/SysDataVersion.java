package com.rx.admin.modules.monitor.dataVersion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_data_version")
public class SysDataVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tableName;
    private Long recordId;
    private Integer version;
    private String operation;
    private String oldData;
    private String newData;
    private String diffData;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
}
