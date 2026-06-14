package com.rx.admin.modules.monitor.exportlog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_export_log")
public class SysExportLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
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
