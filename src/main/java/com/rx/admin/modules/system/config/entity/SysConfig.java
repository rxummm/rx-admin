package com.rx.admin.modules.system.config.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private String groupName;
    private Integer sortOrder;
}