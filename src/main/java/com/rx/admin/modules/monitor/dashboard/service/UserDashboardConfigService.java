package com.rx.admin.modules.monitor.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.monitor.dashboard.entity.SysUserDashboardConfig;
import com.rx.admin.modules.monitor.dashboard.mapper.SysUserDashboardConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户仪表盘配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDashboardConfigService {

    private final SysUserDashboardConfigMapper configMapper;

    /**
     * 获取用户的仪表盘配置
     */
    public List<SysUserDashboardConfig> getUserConfig(Long userId) {
        LambdaQueryWrapper<SysUserDashboardConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserDashboardConfig::getUserId, userId)
               .eq(SysUserDashboardConfig::getEnabled, 1)
               .orderByAsc(SysUserDashboardConfig::getSortOrder);
        return configMapper.selectList(wrapper);
    }

    /**
     * 保存用户的仪表盘配置
     */
    public void saveConfig(Long userId, List<SysUserDashboardConfig> configs) {
        // 先删除用户原有配置
        LambdaQueryWrapper<SysUserDashboardConfig> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysUserDashboardConfig::getUserId, userId);
        configMapper.delete(deleteWrapper);
        
        // 保存新配置
        int sortOrder = 0;
        for (SysUserDashboardConfig config : configs) {
            config.setUserId(userId);
            config.setSortOrder(sortOrder++);
            config.setEnabled(1);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            configMapper.insert(config);
        }
        
        log.info("保存用户仪表盘配置: userId={}, count={}", userId, configs.size());
    }

    /**
     * 更新单个组件配置
     */
    public void updateWidget(Long userId, SysUserDashboardConfig config) {
        LambdaQueryWrapper<SysUserDashboardConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserDashboardConfig::getUserId, userId)
               .eq(SysUserDashboardConfig::getWidgetType, config.getWidgetType());
        
        SysUserDashboardConfig existing = configMapper.selectOne(wrapper);
        if (existing != null) {
            config.setId(existing.getId());
            config.setUpdateTime(LocalDateTime.now());
            configMapper.updateById(config);
        } else {
            config.setUserId(userId);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            configMapper.insert(config);
        }
    }
}
