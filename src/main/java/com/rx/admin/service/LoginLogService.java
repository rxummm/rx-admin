package com.rx.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysLoginLog;

public interface LoginLogService extends IService<SysLoginLog> {
    PageResult<SysLoginLog> pageQuery(int page, int size, String username, Integer status, String startTime, String endTime);
    void recordLogin(String username, String ip, String browser, String os, boolean success, String failReason);
}
