package com.rx.admin.modules.tool.emailTemplate.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_email_template")
public class SysEmailTemplate extends BaseEntity {
    private String name;
    private String code;
    private String subject;
    private String body;
    private String variables;
    private String category;
    private Integer status;
}
