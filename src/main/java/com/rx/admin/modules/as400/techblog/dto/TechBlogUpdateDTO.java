package com.rx.admin.modules.as400.techblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 技术博客更新请求 */
@Data
public class TechBlogUpdateDTO {
    @NotNull(message = "文章ID不能为空")
    private Long id;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String summary;
    private String category;
    private String tags;
    private String coverUrl;
    private Integer status;
}
