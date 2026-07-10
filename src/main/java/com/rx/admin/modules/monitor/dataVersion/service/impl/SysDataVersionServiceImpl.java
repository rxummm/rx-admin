package com.rx.admin.modules.monitor.dataVersion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.dataVersion.entity.SysDataVersion;
import com.rx.admin.modules.monitor.dataVersion.mapper.SysDataVersionMapper;
import com.rx.admin.modules.monitor.dataVersion.service.SysDataVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDataVersionServiceImpl extends ServiceImpl<SysDataVersionMapper, SysDataVersion> implements SysDataVersionService {

    @Override
    public PageResult<SysDataVersion> queryPage(String tableName, Long recordId, Integer page, Integer size) {
        LambdaQueryWrapper<SysDataVersion> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tableName)) {
            w.eq(SysDataVersion::getTableName, tableName);
        }
        if (recordId != null) {
            w.eq(SysDataVersion::getRecordId, recordId);
        }
        w.orderByDesc(SysDataVersion::getCreateTime);
        Page<SysDataVersion> p = page(new Page<>(page, size), w);
        return PageResult.of(p);
    }
}
