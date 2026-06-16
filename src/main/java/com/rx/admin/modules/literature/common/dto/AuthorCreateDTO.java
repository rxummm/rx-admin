package com.rx.admin.modules.literature.common.dto;

import lombok.Data;

@Data
public class AuthorCreateDTO {

    private String name;
    private String courtesyName;
    private String pseudonym;
    private Long dynastyId;
    private Integer birthYear;
    private Integer deathYear;
    private String birthplace;
    private String biography;
    private String avatarUrl;
    private String representativeWorks;
    private String achievement;
    private String authorType;
    private String tags;
    private Integer sortOrder;
    private Integer status;
}