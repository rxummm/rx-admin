package com.rx.admin.modules.literature.sanguo.dto;

import lombok.Data;

@Data
public class SanguoPoemUpdateDTO {

    private Long id;
    private String title;
    private String author;
    private String dynasty;
    private String content;
    private String translation;
    private String appreciation;
    private String chapter;
    private String category;
    private String relatedCharacter;
}