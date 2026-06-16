package com.rx.admin.modules.literature.common.dto;

import lombok.Data;

@Data
public class LiteraryWorkUpdateDTO {

    private Long id;
    private String title;
    private String subtitle;
    private Long authorId;
    private Long dynastyId;
    private Long genreId;
    private String genreCode;
    private String content;
    private String contentHtml;
    private String preface;
    private String epilogue;
    private String annotations;
    private String appreciation;
    private String translation;
    private String keywords;
    private String tags;
    private Integer difficultyLevel;
    private Integer wordCount;
    private String source;
    private String coverUrl;
    private String summary;
    private Integer isFeatured;
    private Integer viewCount;
    private Integer sortOrder;
    private Integer status;
    private String authorName;
    private String dynastyName;
    private String genreName;
}