package com.rx.admin.modules.literature.shuihu.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShuihuPoemVO {

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