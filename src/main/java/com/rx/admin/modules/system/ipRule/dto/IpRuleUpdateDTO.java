package com.rx.admin.modules.system.iprule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IpRuleUpdateDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    private String ipAddress;
    private String ruleType;
    private String description;
    private Integer status;
}