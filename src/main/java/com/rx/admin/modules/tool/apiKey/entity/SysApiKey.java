package com.rx.admin.modules.tool.apiKey.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_api_key")
public class SysApiKey extends BaseEntity {
    private String name;
    private String apiKey;
    private String apiSecret;
    private String permissions;
    private Integer rateLimit;
    private String ipWhitelist;
    private LocalDateTime expireTime;
    private LocalDateTime lastUsedTime;
    private Long useCount;
    private Integer status;
    private String description;
    private Long createdBy;
}
