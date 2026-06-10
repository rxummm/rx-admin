package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.SysIpRule;
import com.rx.admin.mapper.SysIpRuleMapper;
import org.springframework.stereotype.Service;

@Service
public class SysIpRuleService extends ServiceImpl<SysIpRuleMapper, SysIpRule> {

    public PageResult<SysIpRule> pageQuery(int page, int size, String keyword, String ruleType) {
        LambdaQueryWrapper<SysIpRule> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank())
            w.like(SysIpRule::getIpAddress, keyword);
        if (ruleType != null && !ruleType.isBlank())
            w.eq(SysIpRule::getRuleType, ruleType);
        w.orderByDesc(SysIpRule::getCreateTime);
        IPage<SysIpRule> p = page(new Page<>(page, size), w);
        return PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords());
    }
}
