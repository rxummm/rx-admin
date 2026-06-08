package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysJob;
import com.rx.admin.mapper.SysJobMapper;
import org.springframework.stereotype.Service;

@Service
public class SysJobService extends ServiceImpl<SysJobMapper, SysJob> {

    public PageResult<SysJob> pageQuery(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysJob::getJobName, keyword)
                    .or().like(SysJob::getBeanName, keyword)
                    .or().like(SysJob::getRemark, keyword);
        }
        if (status != null) {
            wrapper.eq(SysJob::getStatus, status);
        }
        wrapper.orderByDesc(SysJob::getCreateTime);

        IPage<SysJob> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords());
    }
}
