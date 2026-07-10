package com.rx.admin.modules.tool.webhook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_webhook")
public class SysWebhook extends BaseEntity {
    private String name;
    private String url;
    private String secret;
    private String events;
    private String headers;
    private Integer status;
    private Integer retryCount;
    private Integer timeoutMs;
    private String description;
}
