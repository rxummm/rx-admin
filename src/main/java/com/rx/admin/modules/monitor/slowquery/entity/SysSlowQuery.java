package com.rx.admin.modules.monitor.slowquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_slow_query")
public class SysSlowQuery implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sqlText;
    private String params;
    private Long costTimeMs;
    private String queryType;
    private String mapperMethod;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
