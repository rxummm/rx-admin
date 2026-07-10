package com.rx.admin.modules.system.notificationPref.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notification_preference")
public class SysNotificationPreference {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String eventType;
    private Integer emailEnabled;
    private Integer websocketEnabled;
    private Integer browserEnabled;
    private String quietStart;
    private String quietEnd;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
