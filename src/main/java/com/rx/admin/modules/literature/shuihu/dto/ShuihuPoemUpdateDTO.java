package com.rx.admin.modules.literature.shuihu.dto;

import lombok.Data;

@Data
public class ShuihuPoemUpdateDTO {

    private Long id;
    private String title;
    private String author;
    private String content;
    private String poemType;
    private String relatedCharacter;
    private String relatedScene;
    private String appreciation;
}