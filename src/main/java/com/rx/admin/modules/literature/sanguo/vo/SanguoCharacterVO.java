package com.rx.admin.modules.literature.sanguo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SanguoCharacterVO {

    private Long id;
    private String name;
    private String courtesyName;
    private String styleName;
    private String nickname;
    private String role;
    private String country;
    private String position;
    private String weapon;
    private String hometown;
    private String appearanceDescription;
    private String personalityTraits;
    private String fateSummary;
    private String notableEvents;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}