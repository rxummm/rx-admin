package com.rx.admin.modules.tool.emailTemplate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysEmailTemplateCreateDTO {
    @NotBlank(message = "模板名称不能为空")
    private String name;
    @NotBlank(message = "模板编码不能为空")
    private String code;
    @NotBlank(message = "邮件主题不能为空")
    private String subject;
    @NotBlank(message = "邮件正文不能为空")
    private String body;
    private String variables;
    private String category;
    private Integer status;
}
