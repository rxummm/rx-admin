package com.rx.admin.modules.literature.xiyou.dto;

import lombok.Data;

@Data
public class XiyouEventCreateDTO {

    private Integer difficultyNum;
    private Integer chapterNum;
    private String title;
    private String location;
    private String monster;
    private String monsterWeapon;
    private String helper;
    private String resolution;
    private String detail;
    private Integer difficultyLevel;
    private String eventType;
}