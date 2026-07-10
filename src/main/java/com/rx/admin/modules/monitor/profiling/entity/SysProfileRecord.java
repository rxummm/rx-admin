package com.rx.admin.modules.monitor.profiling.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_profile_record")
public class SysProfileRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private String methodName;
    private Long executionTime;
    private String params;
    private String exception;
    private String threadName;
    private String traceId;
    private LocalDateTime createTime;
}
