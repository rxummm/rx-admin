package com.rx.admin.modules.literature.honglou.dto;

import lombok.Data;

@Data
public class HonglouCharacterRelationUpdateDTO {

    private Long id;
    private Long fromCharacterId;
    private Long toCharacterId;
    private String relationType;
    private String relationDesc;
}