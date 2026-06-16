package com.rx.admin.modules.literature.xiyou.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class XiyouCharacterVO {

    private Long id;
    private String name;
    private String alias;
    private String identity;
    private String weapon;
    private String race;
    private String mainDeeds;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}