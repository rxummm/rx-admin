package com.rx.admin.modules.monitor.loginlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.loginlog.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推荐服务
 * 根据用户行为推荐相关数据/功能/文档
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartRecommendationService {

    private final SysLoginLogMapper loginLogMapper;

    /**
     * 获取用户推荐的功能
     */
    public List<Map<String, Object>> getRecommendedFeatures(Long userId) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // 基于登录时间推荐
        int hour = LocalDateTime.now().getHour();
        if (hour >= 9 && hour < 12) {
            recommendations.add(createRecommendation("dashboard", "仪表盘", "查看今日系统概览", "high"));
        } else if (hour >= 14 && hour < 18) {
            recommendations.add(createRecommendation("monitor/log", "操作日志", "查看今日操作记录", "medium"));
        }
        
        // 基于使用频率推荐（简化实现）
        recommendations.add(createRecommendation("system/user", "用户管理", "管理系统用户", "medium"));
        recommendations.add(createRecommendation("system/role", "角色管理", "管理系统角色", "low"));
        
        return recommendations;
    }

    /**
     * 获取推荐的文档
     */
    public List<Map<String, Object>> getRecommendedDocs(Long userId) {
        // 简化实现：返回固定推荐
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        recommendations.add(createRecommendation("doc1", "系统使用手册", "RX Admin 系统使用指南", "high"));
        recommendations.add(createRecommendation("doc2", "API 文档", "后端 API 接口文档", "medium"));
        recommendations.add(createRecommendation("doc3", "开发规范", "项目开发规范文档", "low"));
        
        return recommendations;
    }

    /**
     * 获取相关数据推荐
     */
    public List<Map<String, Object>> getRelatedData(String dataType, Long dataId) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // 根据数据类型推荐相关数据
        switch (dataType) {
            case "user":
                recommendations.add(createRecommendation("role", "角色", "分配用户角色", "high"));
                recommendations.add(createRecommendation("dept", "部门", "分配用户部门", "medium"));
                break;
            case "notice":
                recommendations.add(createRecommendation("message", "消息", "发送相关通知", "medium"));
                break;
            default:
                break;
        }
        
        return recommendations;
    }

    /**
     * 创建推荐项
     */
    private Map<String, Object> createRecommendation(String id, String title, String description, String priority) {
        Map<String, Object> recommendation = new LinkedHashMap<>();
        recommendation.put("id", id);
        recommendation.put("title", title);
        recommendation.put("description", description);
        recommendation.put("priority", priority);
        recommendation.put("createTime", LocalDateTime.now());
        return recommendation;
    }
}
