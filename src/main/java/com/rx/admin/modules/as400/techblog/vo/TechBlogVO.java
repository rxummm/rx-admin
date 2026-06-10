package com.rx.admin.modules.as400.techblog.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 技术博客视图对象 */
@Data
public class TechBlogVO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String category;
    private String tags;
    private String coverUrl;
    private Integer status;
    private Long viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
