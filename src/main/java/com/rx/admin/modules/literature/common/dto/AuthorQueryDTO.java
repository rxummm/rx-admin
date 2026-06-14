package com.rx.admin.modules.literature.common.dto;

import lombok.Data;

@Data
public class AuthorQueryDTO {
    private String keyword;
    private String authorType;
    private Integer status;
}