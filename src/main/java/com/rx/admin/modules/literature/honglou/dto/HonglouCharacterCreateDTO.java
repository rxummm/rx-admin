package com.rx.admin.modules.literature.honglou.dto;

import lombok.Data;

@Data
public class HonglouCharacterCreateDTO {

    private String name;
    private String nickname;
    private String role;
    private String appearanceDescription;
    private String personalityTraits;
    private String fateSummary;
}