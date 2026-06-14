package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.SysIpRule;
import com.rx.admin.mapper.SysIpRuleMapper;
import com.rx.admin.modules.system.iprule.dto.IpRuleCreateDTO;
import com.rx.admin.modules.system.iprule.dto.IpRuleUpdateDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    /**
     * 新增 IP 规则
     */
    public void addIpRule(IpRuleCreateDTO dto) {
        long count = count(new LambdaQueryWrapper<SysIpRule>()
                .eq(SysIpRule::getIpAddress, dto.getIpAddress())
                .eq(SysIpRule::getRuleType, dto.getRuleType()));
        if (count > 0) {
            throw new IllegalArgumentException("该 IP 规则已存在");
        }
        SysIpRule rule = new SysIpRule();
        rule.setIpAddress(dto.getIpAddress());
        rule.setRuleType(dto.getRuleType());
        rule.setDescription(dto.getDescription());
        rule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        save(rule);
    }

    /**
     * 更新 IP 规则
     */
    public void updateIpRule(IpRuleUpdateDTO dto) {
        SysIpRule rule = getById(dto.getId());
        if (rule == null) {
            throw new IllegalArgumentException("IP 规则不存在");
        }
        if (StringUtils.hasText(dto.getIpAddress())) rule.setIpAddress(dto.getIpAddress());
        if (StringUtils.hasText(dto.getRuleType())) rule.setRuleType(dto.getRuleType());
        if (StringUtils.hasText(dto.getDescription())) rule.setDescription(dto.getDescription());
        if (dto.getStatus() != null) rule.setStatus(dto.getStatus());
        updateById(rule);
    }
}
