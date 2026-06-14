package com.rx.admin.modules.content.notify.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 消息模板创建请求 */
@Data
public class MessageTemplateCreateDTO {
    @NotBlank(message = "模板名称不能为空")
    private String name;
    @NotBlank(message = "模板编码不能为空")
    private String code;
    private String titleTemplate;
    private String contentTemplate;
    /** 渠道，逗号分隔，如 "message,email" */
    private String channels;
    private Integer status;
}
