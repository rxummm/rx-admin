package com.rx.admin.modules.as400.techblog.dto;

import lombok.Data;

/** 技术博客查询参数 */
@Data
public class TechBlogQueryDTO {
    private String title;
    private String category;
    private String tags;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
