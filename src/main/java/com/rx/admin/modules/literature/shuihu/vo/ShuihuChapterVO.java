package com.rx.admin.modules.literature.shuihu.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShuihuChapterVO {

    private Long id;
    private Integer chapterNumber;
    private String chapterTitle;
    private String chapterSubtitle;
    private String chapterContent;
    private String highlights;
    private String characters;
    private String locations;
    private String themes;
    private String keywords;
    private String readingDifficulty;
    private Integer estimatedReadingTime;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}