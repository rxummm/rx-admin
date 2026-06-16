package com.rx.admin.modules.literature.honglou.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HonglouCharacterVO {

    private Long id;
    private String name;
    private String nickname;
    private String role;
    private String appearanceDescription;
    private String personalityTraits;
    private String fateSummary;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}