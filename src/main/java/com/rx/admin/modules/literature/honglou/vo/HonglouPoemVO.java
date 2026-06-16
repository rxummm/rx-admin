package com.rx.admin.modules.literature.honglou.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HonglouPoemVO {

    private Long id;
    private String title;
    private String author;
    private String content;
    private String poemType;
    private String relatedCharacter;
    private String relatedScene;
    private String appreciation;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}