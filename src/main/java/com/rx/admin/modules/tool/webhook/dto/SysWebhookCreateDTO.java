package com.rx.admin.modules.tool.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysWebhookCreateDTO {
    @NotBlank(message = "名称不能为空")
    private String name;
    @NotBlank(message = "URL不能为空")
    private String url;
    private String secret;
    @NotBlank(message = "事件不能为空")
    private String events;
    private String headers;
    private Integer status;
    private Integer retryCount;
    private Integer timeoutMs;
    private String description;
}
