package com.rx.admin.modules.literature.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContentCategoryVO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}