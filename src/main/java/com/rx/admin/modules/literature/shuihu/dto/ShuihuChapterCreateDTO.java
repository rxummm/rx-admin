package com.rx.admin.modules.literature.shuihu.dto;

import lombok.Data;

@Data
public class ShuihuChapterCreateDTO {

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
}