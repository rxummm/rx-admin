package com.rx.admin.modules.tool.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_archive_log")
public class SysArchiveLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long configId;
    private String tableName;
    private Integer archivedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String errorMsg;
    private LocalDateTime createTime;
}
