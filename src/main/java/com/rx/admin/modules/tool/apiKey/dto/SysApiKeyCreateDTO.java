package com.rx.admin.modules.tool.apiKey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysApiKeyCreateDTO {
    @NotBlank(message = "名称不能为空")
    private String name;
    private String permissions;
    private Integer rateLimit;
    private String ipWhitelist;
    private String description;
}
