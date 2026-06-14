package com.rx.admin.modules.content.notify.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysMessageTemplateVO {
    private Long id;
    private String name;
    private String code;
    private String titleTemplate;
    private String contentTemplate;
    private String channels;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}