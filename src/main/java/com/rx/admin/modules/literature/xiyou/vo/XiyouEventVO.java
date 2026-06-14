package com.rx.admin.modules.literature.xiyou.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class XiyouEventVO {

    private Long id;
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
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}