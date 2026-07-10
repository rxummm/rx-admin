package com.rx.admin.modules.tool.webhook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_webhook_log")
public class SysWebhookLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long webhookId;
    private String event;
    private String payload;
    private Integer responseCode;
    private String responseBody;
    private String status;
    private String errorMsg;
    private Integer retryCount;
    private LocalDateTime createTime;
}
