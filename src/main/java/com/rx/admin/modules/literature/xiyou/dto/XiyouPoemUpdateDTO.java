package com.rx.admin.modules.literature.xiyou.dto;

import lombok.Data;

@Data
public class XiyouPoemUpdateDTO {

    private Long id;
    private String title;
    private String author;
    private String poemType;
    private String content;
    private String relatedCharacter;
    private String relatedScene;
}