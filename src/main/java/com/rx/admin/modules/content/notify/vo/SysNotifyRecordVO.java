package com.rx.admin.modules.content.notify.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysNotifyRecordVO {
    private Long id;
    private Long templateId;
    private String channel;
    private String receiver;
    private String title;
    private String content;
    private Integer status;
    private String errorMsg;
    private Integer retryCount;
    private LocalDateTime createTime;
    private LocalDateTime sendTime;
}