package com.rx.admin.modules.tool.wiki.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WikiSpaceVO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String visibility;
    private Long ownerId;
    private LocalDateTime createTime;
}
