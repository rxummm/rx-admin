package com.rx.admin.modules.content.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 通知公告创建请求 */
@Data
public class NoticeCreateDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String noticeType;
    private String category;
    private String linkPath;
    private Integer status;
}
