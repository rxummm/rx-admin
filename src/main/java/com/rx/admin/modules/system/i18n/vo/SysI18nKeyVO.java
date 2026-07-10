package com.rx.admin.modules.system.i18n.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SysI18nKeyVO {
    private Long id;
    private String keyPath;
    private String module;
    private String description;
    private LocalDateTime createTime;
    private Map<String, String> translations;
}
