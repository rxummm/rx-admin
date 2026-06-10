package com.rx.admin.modules.system.ipRule.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** IP规则视图对象 */
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
