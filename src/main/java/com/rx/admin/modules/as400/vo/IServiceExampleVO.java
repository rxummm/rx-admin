package com.rx.admin.modules.as400.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IServiceExampleVO {
    private Long id;
    private Long serviceId;
    private String title;
    private String description;
    private String sqlCode;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}