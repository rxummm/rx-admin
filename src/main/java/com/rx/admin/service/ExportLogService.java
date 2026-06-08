package com.rx.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysExportLog;

public interface ExportLogService extends IService<SysExportLog> {
    PageResult<SysExportLog> pageQuery(int page, int size, String username, String exportType, String startTime, String endTime);
}
