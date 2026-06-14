package com.rx.admin.modules.tool.export.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysExportConfigVO {
    private Long id;
    private Long menuId;
    private String exportTypes;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}