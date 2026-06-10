package com.rx.admin.modules.system.ipRule.dto;

import lombok.Data;

/** IP规则查询参数 */
@Data
public class IpRuleQueryDTO {
    private String ipAddress;
    private String ruleType;
    private Integer page = 1;
    private Integer pageSize = 10;
}
