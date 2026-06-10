package com.rx.admin.modules.system.config.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 系统配置视图对象 */
@Data
public class ConfigVO {
    private Long id;
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private String groupName;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
