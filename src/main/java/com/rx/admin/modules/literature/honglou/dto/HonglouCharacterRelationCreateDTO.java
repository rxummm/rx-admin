package com.rx.admin.modules.literature.honglou.dto;

import lombok.Data;

@Data
public class HonglouCharacterRelationCreateDTO {

    private Long fromCharacterId;
    private Long toCharacterId;
    private String relationType;
    private String relationDesc;
}