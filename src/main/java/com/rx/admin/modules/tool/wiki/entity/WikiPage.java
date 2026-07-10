package com.rx.admin.modules.tool.wiki.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wiki_page")
public class WikiPage extends BaseEntity {
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
}
