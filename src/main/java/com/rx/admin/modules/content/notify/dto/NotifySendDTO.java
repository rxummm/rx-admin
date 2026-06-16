package com.rx.admin.modules.content.notify.dto;

import lombok.Data;

/** 通知发送请求 */
@Data
public class NotifySendDTO {
    /** 模板ID（可选） */
    private Long templateId;
    /** 渠道，默认 message */
    private String channel = "message";
    /** 接收者，可以是 userId、用户名或角色名 */
    private String receiver;
    private String title;
    private String content;
}
