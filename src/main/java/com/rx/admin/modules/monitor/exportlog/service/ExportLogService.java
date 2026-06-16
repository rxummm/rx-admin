package com.rx.admin.modules.monitor.exportlog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.exportlog.entity.SysExportLog;

public interface ExportLogService extends IService<SysExportLog> {
    PageResult<SysExportLog> pageQuery(int page, int size, String username, String exportType, String startTime, String endTime);
}
