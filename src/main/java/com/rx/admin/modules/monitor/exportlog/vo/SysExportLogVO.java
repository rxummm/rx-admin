package com.rx.admin.modules.monitor.exportlog.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysExportLogVO {
    private Long id;
    private Long userId;
    private String username;
    private String exportType;
    private String exportTitle;
    private Integer recordCount;
    private String fileName;
    private String ip;
    private LocalDateTime createTime;
}