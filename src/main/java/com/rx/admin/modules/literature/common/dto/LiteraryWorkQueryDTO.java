package com.rx.admin.modules.literature.common.dto;

import lombok.Data;

@Data
public class LiteraryWorkQueryDTO {
    private String keyword;
    private Long authorId;
    private Long dynastyId;
    private Long genreId;
    private Integer difficultyLevel;
    private Integer status;
}