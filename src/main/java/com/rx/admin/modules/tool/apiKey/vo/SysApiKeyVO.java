package com.rx.admin.modules.tool.apiKey.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysApiKeyVO {
    private Long id;
    private String name;
    private String apiKey;
    private String permissions;
    private Integer rateLimit;
    private String ipWhitelist;
    private LocalDateTime expireTime;
    private LocalDateTime lastUsedTime;
    private Long useCount;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
}
