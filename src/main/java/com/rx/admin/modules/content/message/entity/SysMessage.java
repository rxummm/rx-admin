package com.rx.admin.modules.content.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sys_message")
public class SysMessage extends BaseEntity {
    private Long senderId;
    private Long receiverId;
    @TableField(exist = false)
    private String senderName;
    @TableField(exist = false)
    private String receiverUsername;
    private String title;
    private String content;
    private String messageType;
    private Integer isRead;
    private LocalDateTime readTime;
    private String linkPath;
}
