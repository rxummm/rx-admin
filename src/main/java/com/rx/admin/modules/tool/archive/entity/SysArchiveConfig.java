package com.rx.admin.modules.tool.archive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_archive_config")
public class SysArchiveConfig extends BaseEntity {
    private String tableName;
    private String archiveTable;
    private String conditionField;
    private Integer retainDays;
    private Integer batchSize;
    private Integer status;
    private LocalDateTime lastArchiveTime;
}
