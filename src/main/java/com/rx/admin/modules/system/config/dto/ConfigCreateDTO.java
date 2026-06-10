package com.rx.admin.modules.system.config.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 系统配置创建请求 */
@Data
public class ConfigCreateDTO {
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private String groupName;
    private Integer sortOrder;
}
