package com.rx.admin.modules.tool.archive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysArchiveConfigCreateDTO {
    @NotBlank(message = "表名不能为空")
    private String tableName;
    @NotBlank(message = "归档表名不能为空")
    private String archiveTable;
    @NotBlank(message = "条件字段不能为空")
    private String conditionField;
    private Integer retainDays;
    private Integer batchSize;
    private Integer status;
}
