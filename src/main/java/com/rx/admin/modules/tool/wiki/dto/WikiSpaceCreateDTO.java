package com.rx.admin.modules.tool.wiki.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WikiSpaceCreateDTO {
    @NotBlank(message = "空间名称不能为空")
    private String name;
    private String description;
    private String icon;
    private String visibility;
}
