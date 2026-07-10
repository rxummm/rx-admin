package com.rx.admin.modules.monitor.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_user_activity")
public class SysUserActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String activityType;
    private String module;
    private String detail;
    private String ipAddress;
    private LocalDate activityDate;
    private Integer hour;
    private LocalDateTime createTime;
}
