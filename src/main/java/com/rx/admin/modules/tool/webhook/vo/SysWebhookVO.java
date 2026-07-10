package com.rx.admin.modules.tool.webhook.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysWebhookVO {
    private Long id;
    private String name;
    private String url;
    private String secret;
    private String events;
    private String headers;
    private Integer status;
    private Integer retryCount;
    private Integer timeoutMs;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
