package com.rx.admin.modules.monitor.slowquery.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SlowQueryVO {
    private Long id;
    private String sqlText;
    private String params;
    private Long costTimeMs;
    private String queryType;
    private String mapperMethod;
    private LocalDateTime createTime;
}
