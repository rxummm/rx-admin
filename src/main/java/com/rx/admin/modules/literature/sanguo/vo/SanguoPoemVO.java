package com.rx.admin.modules.literature.sanguo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SanguoPoemVO {

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
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}