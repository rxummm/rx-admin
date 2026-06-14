package com.rx.admin.modules.system.iprule.dto;

import lombok.Data;

@Data
public class IpRuleQueryDTO {
    private String ipAddress;
    private String ruleType;
    private Integer page = 1;
    private Integer pageSize = 10;
}