package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysSlowQuery;
import com.rx.admin.mapper.SysSlowQueryMapper;
import org.springframework.stereotype.Service;

@Service
public class SysSlowQueryService extends ServiceImpl<SysSlowQueryMapper, SysSlowQuery> {

    @org.springframework.beans.factory.annotation.Value("${app.slow-query.threshold-ms:2000}")
    public long SLOW_THRESHOLD_MS;

    public PageResult<SysSlowQuery> pageQuery(int page, int size, String keyword, String queryType) {
        LambdaQueryWrapper<SysSlowQuery> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysSlowQuery::getSqlText, keyword);
        }
        if (queryType != null && !queryType.isBlank()) {
            wrapper.eq(SysSlowQuery::getQueryType, queryType);
        }
        wrapper.orderByDesc(SysSlowQuery::getCreateTime);
        IPage<SysSlowQuery> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords());
    }

    public void addSlowQuery(String sqlText, String params, long costTimeMs, String queryType, String mapperMethod) {
        if (costTimeMs < SLOW_THRESHOLD_MS) return;
        SysSlowQuery record = new SysSlowQuery();
        record.setSqlText(sqlText != null && sqlText.length() > 2000 ? sqlText.substring(0, 2000) : sqlText);
        record.setParams(params != null && params.length() > 1000 ? params.substring(0, 1000) : params);
        record.setCostTimeMs(costTimeMs);
        record.setQueryType(queryType);
        record.setMapperMethod(mapperMethod);
        save(record);
    }
}
