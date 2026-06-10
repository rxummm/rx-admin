package com.rx.admin.modules.monitor.slowquery.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 慢查询视图对象 */
@Data
public class SlowQueryVO {
    private Long id;
    private String sql;
    private Long costTime;
    private String params;
    private LocalDateTime createTime;
}
