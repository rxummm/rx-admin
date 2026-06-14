package com.rx.admin.modules.content.notify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 消息模板更新请求 */
@Data
public class MessageTemplateUpdateDTO {
    @NotNull(message = "模板ID不能为空")
    private Long id;
    @NotBlank(message = "模板名称不能为空")
    private String name;
    @NotBlank(message = "模板编码不能为空")
    private String code;
    private String titleTemplate;
    private String contentTemplate;
    private String channels;
    private Integer status;
}
