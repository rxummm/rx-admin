package com.rx.admin.modules.as400.techblog.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 技术博客视图对象 */
@Data
public class TechBlogVO {
    private Long id;
    private String title;
    private String slug;
    private String sourceUrl;
    private String author;
    private String publishDate;
    private String categories;
    private String excerptText;
    private String contentHtml;
    private String contentText;
    private String coverImage;
    private Integer sort;
    private Integer viewCount;
    private String source;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}