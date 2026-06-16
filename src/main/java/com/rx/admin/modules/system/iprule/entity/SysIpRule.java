package com.rx.admin.modules.system.iprule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_ip_rule")
public class SysIpRule extends BaseEntity {
    private String ipAddress;
    private String ruleType;
    private String description;
    private Integer status;
}
