package com.rx.admin.modules.literature.honglou.dto;

import lombok.Data;

@Data
public class HonglouPoemCreateDTO {

    private String title;
    private String author;
    private String content;
    private String poemType;
    private String relatedCharacter;
    private String relatedScene;
    private String appreciation;
}