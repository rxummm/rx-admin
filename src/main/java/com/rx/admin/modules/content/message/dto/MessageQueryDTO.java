package com.rx.admin.modules.content.message.dto;

import lombok.Data;

/** 消息查询参数 */
@Data
public class MessageQueryDTO {
    private String title;
    private String messageType;
    private Integer isRead;
    private Integer page = 1;
    private Integer pageSize = 10;
}
