package com.rx.admin.modules.tool.emailTemplate.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysEmailTemplateVO {
    private Long id;
    private String name;
    private String code;
    private String subject;
    private String body;
    private String variables;
    private String category;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
