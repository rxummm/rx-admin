package com.rx.admin.modules.as400.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IServiceAuthorityVO {
    private Long id;
    private Long serviceId;
    private String authority;
    private String context;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}