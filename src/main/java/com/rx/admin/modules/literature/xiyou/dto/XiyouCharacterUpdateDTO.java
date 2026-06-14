package com.rx.admin.modules.literature.xiyou.dto;

import lombok.Data;

@Data
public class XiyouCharacterUpdateDTO {

    private Long id;
    private String name;
    private String alias;
    private String identity;
    private String weapon;
    private String race;
    private String mainDeeds;
}