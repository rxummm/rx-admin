package com.rx.admin.modules.content.message.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 消息视图对象 */
@Data
public class MessageVO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String receiverUsername;
    private String title;
    private String content;
    private String messageType;
    private Integer isRead;
    private LocalDateTime readTime;
    private String linkPath;
    private LocalDateTime createTime;
}
