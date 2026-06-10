package com.rx.admin.modules.system.ipRule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** IP规则更新请求 */
@Data
public class IpRuleUpdateDTO {
    @NotNull(message = "规则ID不能为空")
    private Long id;
    @NotBlank(message = "IP地址不能为空")
    private String ipAddress;
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;
    private String description;
    private Integer status;
}
