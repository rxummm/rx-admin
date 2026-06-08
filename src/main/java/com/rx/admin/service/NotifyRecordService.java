package com.rx.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysNotifyRecord;

public interface NotifyRecordService extends IService<SysNotifyRecord> {
    PageResult<SysNotifyRecord> pageQuery(int page, int size, String channel, Integer status);
}
