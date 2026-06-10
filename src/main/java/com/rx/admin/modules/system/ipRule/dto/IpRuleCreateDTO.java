package com.rx.admin.modules.system.ipRule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** IP规则创建请求 */
@Data
public class IpRuleCreateDTO {
    @NotBlank(message = "IP地址不能为空")
    private String ipAddress;
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;
    private String description;
    private Integer status;
}
