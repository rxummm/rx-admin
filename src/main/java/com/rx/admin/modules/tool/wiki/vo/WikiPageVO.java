package com.rx.admin.modules.tool.wiki.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WikiPageVO {
    private Long id;
    private Long spaceId;
    private Long parentId;
    private String title;
    private String content;
    private String slug;
    private Integer sortOrder;
    private Integer isPublished;
    private Long authorId;
    private String authorName;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
