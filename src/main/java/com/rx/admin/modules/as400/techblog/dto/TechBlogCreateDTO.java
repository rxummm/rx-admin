package com.rx.admin.modules.as400.techblog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 技术博客创建请求 */
@Data
public class TechBlogCreateDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String summary;
    private String category;
    private String tags;
    private String coverUrl;
    private Integer status;
}
