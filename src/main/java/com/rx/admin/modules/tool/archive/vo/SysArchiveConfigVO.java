package com.rx.admin.modules.tool.archive.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysArchiveConfigVO {
    private Long id;
    private String tableName;
    private String archiveTable;
    private String conditionField;
    private Integer retainDays;
    private Integer batchSize;
    private Integer status;
    private LocalDateTime lastArchiveTime;
    private LocalDateTime createTime;
}
