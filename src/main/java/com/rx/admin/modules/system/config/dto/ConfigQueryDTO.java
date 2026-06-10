package com.rx.admin.modules.system.config.dto;

import lombok.Data;

/** 系统配置查询参数 */
@Data
public class ConfigQueryDTO {
    private String configKey;
    private String configType;
    private Integer page = 1;
    private Integer pageSize = 10;
}
