package com.rx.admin.modules.content.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.notify.entity.SysNotifyRecord;

public interface NotifyRecordService extends IService<SysNotifyRecord> {
    PageResult<SysNotifyRecord> pageQuery(int page, int size, String channel, Integer status);
}
