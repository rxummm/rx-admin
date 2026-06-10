package com.rx.admin.modules.content.notice.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 通知公告视图对象 */
@Data
public class NoticeVO {
    private Long id;
    private String title;
    private String content;
    private String noticeType;
    private String category;
    private String linkPath;
    private Integer status;
    private Long createBy;
    private String createByName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
