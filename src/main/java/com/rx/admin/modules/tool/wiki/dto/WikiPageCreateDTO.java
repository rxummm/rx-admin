package com.rx.admin.modules.tool.wiki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WikiPageCreateDTO {
    @NotNull(message = "空间ID不能为空")
    private Long spaceId;
    private Long parentId;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String slug;
    private Integer isPublished;
}
