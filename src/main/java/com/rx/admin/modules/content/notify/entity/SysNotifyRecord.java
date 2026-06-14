package com.rx.admin.modules.content.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_notify_record")
public class SysNotifyRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
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
