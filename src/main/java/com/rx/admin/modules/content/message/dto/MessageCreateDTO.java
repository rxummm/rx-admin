package com.rx.admin.modules.content.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 消息创建请求 */
@Data
public class MessageCreateDTO {
    private Long receiverId;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String messageType;
    private String linkPath;
}
