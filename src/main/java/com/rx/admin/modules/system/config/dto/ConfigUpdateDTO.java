package com.rx.admin.modules.system.config.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 系统配置更新请求 */
@Data
public class ConfigUpdateDTO {
    @NotNull(message = "配置ID不能为空")
    private Long id;
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private String groupName;
    private Integer sortOrder;
}
