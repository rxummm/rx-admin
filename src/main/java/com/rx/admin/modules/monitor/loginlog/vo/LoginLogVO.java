package com.rx.admin.modules.monitor.loginlog.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 登录日志视图对象 */
@Data
public class LoginLogVO {
    private Long id;
    private String username;
    private String ip;
    private String loginLocation;
    private String browser;
    private String os;
    private Integer status;
    private String msg;
    private LocalDateTime loginTime;
}
