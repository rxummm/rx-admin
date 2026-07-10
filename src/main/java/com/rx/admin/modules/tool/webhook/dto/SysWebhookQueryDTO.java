package com.rx.admin.modules.tool.webhook.dto;

import lombok.Data;

@Data
public class SysWebhookQueryDTO {
    private String keyword;
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
}
