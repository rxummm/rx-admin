package com.rx.admin.modules.system.iprule.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IpRuleVO {
    private Long id;
    private String ipAddress;
    private String ruleType;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}