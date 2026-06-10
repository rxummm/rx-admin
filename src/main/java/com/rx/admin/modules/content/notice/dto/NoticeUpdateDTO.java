package com.rx.admin.modules.content.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 通知公告更新请求 */
@Data
public class NoticeUpdateDTO {
    @NotNull(message = "通知ID不能为空")
    private Long id;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String noticeType;
    private String category;
    private String linkPath;
    private Integer status;
}
