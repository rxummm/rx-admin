package com.rx.admin.modules.monitor.dataVersion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.dataVersion.entity.SysDataVersion;

public interface SysDataVersionService extends IService<SysDataVersion> {
    PageResult<SysDataVersion> queryPage(String tableName, Long recordId, Integer page, Integer size);
}
