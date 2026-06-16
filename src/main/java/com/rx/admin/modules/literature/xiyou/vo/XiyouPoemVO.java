package com.rx.admin.modules.literature.xiyou.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class XiyouPoemVO {

    private Long id;
    private String title;
    private String author;
    private String poemType;
    private String content;
    private String relatedCharacter;
    private String relatedScene;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}