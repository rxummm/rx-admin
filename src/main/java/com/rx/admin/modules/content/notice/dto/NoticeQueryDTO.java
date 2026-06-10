package com.rx.admin.modules.content.notice.dto;

import lombok.Data;

/** 通知公告查询参数 */
@Data
public class NoticeQueryDTO {
    private String title;
    private String noticeType;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
