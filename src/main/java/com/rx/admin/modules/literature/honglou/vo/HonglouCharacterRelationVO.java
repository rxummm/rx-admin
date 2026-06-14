package com.rx.admin.modules.literature.honglou.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HonglouCharacterRelationVO {

    private Long id;
    private Long fromCharacterId;
    private Long toCharacterId;
    private String relationType;
    private String relationDesc;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}