package com.rx.admin.modules.monitor.log.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 操作日志视图对象 */
@Data
public class OperateLogVO {
    private Long id;
    private String module;
    private String action;
    private String method;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String operatorName;
    private String ip;
    private Long costTime;
    private Integer status;
    private String errorMsg;
    private LocalDateTime createTime;
}
