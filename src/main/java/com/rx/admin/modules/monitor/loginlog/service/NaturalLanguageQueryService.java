package com.rx.admin.modules.monitor.loginlog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 自然语言查询服务
 * 将自然语言转换为SQL查询
 */
@Slf4j
@Service
public class NaturalLanguageQueryService {

    // 关键词到SQL的映射
    private static final Map<String, String> KEYWORD_MAP = new LinkedHashMap<>();
    
    static {
        // 表名映射
        KEYWORD_MAP.put("用户", "sys_user");
        KEYWORD_MAP.put("角色", "sys_role");
        KEYWORD_MAP.put("菜单", "sys_menu");
        KEYWORD_MAP.put("日志", "sys_log");
        KEYWORD_MAP.put("通知", "sys_notice");
        KEYWORD_MAP.put("配置", "sys_config");
        KEYWORD_MAP.put("部门", "sys_dept");
        
        // 操作映射
        KEYWORD_MAP.put("查询", "SELECT");
        KEYWORD_MAP.put("统计", "SELECT COUNT(*)");
        KEYWORD_MAP.put("删除", "DELETE");
        KEYWORD_MAP.put("更新", "UPDATE");
    }

    /**
     * 将自然语言转换为SQL
     */
    public Map<String, Object> convertToSql(String naturalLanguage) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try {
            // 解析自然语言
            String sql = parseNaturalLanguage(naturalLanguage);
            
            result.put("success", true);
            result.put("sql", sql);
            result.put("naturalLanguage", naturalLanguage);
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("naturalLanguage", naturalLanguage);
        }
        
        return result;
    }

    /**
     * 解析自然语言
     */
    private String parseNaturalLanguage(String input) {
        String lowerInput = input.toLowerCase();
        
        // 确定操作类型
        String operation = "SELECT";
        if (lowerInput.contains("统计") || lowerInput.contains("多少")) {
            operation = "SELECT COUNT(*)";
        } else if (lowerInput.contains("删除")) {
            operation = "DELETE";
        } else if (lowerInput.contains("更新") || lowerInput.contains("修改")) {
            operation = "UPDATE";
        }
        
        // 确定表名
        String tableName = "sys_user"; // 默认表
        for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
            if (lowerInput.contains(entry.getKey()) && !entry.getValue().startsWith("SELECT")) {
                tableName = entry.getValue();
                break;
            }
        }
        
        // 构建SQL
        StringBuilder sql = new StringBuilder();
        sql.append(operation);
        
        if (!operation.startsWith("DELETE") && !operation.startsWith("UPDATE")) {
            sql.append(" * FROM ").append(tableName);
        }
        
        // 添加WHERE条件
        if (lowerInput.contains("今天")) {
            sql.append(" WHERE DATE(create_time) = CURDATE()");
        } else if (lowerInput.contains("最近")) {
            sql.append(" WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)");
        } else if (lowerInput.contains("管理员")) {
            sql.append(" WHERE role = 'admin'");
        }
        
        // 添加排序
        if (lowerInput.contains("最新") || lowerInput.contains("最近")) {
            sql.append(" ORDER BY create_time DESC");
        }
        
        // 添加限制
        if (lowerInput.contains("前10") || lowerInput.contains("top10")) {
            sql.append(" LIMIT 10");
        } else if (lowerInput.contains("前5") || lowerInput.contains("top5")) {
            sql.append(" LIMIT 5");
        }
        
        return sql.toString();
    }

    /**
     * 获取支持的查询示例
     */
    public List<Map<String, String>> getExamples() {
        List<Map<String, String>> examples = new ArrayList<>();
        
        examples.add(createExample("查询所有用户", "SELECT * FROM sys_user"));
        examples.add(createExample("统计用户数量", "SELECT COUNT(*) FROM sys_user"));
        examples.add(createExample("查询最近7天的日志", "SELECT * FROM sys_log WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)"));
        examples.add(createExample("查询管理员用户", "SELECT * FROM sys_user WHERE role = 'admin'"));
        examples.add(createExample("查询最新的10条通知", "SELECT * FROM sys_notice ORDER BY create_time DESC LIMIT 10"));
        
        return examples;
    }

    private Map<String, String> createExample(String naturalLanguage, String sql) {
        Map<String, String> example = new LinkedHashMap<>();
        example.put("naturalLanguage", naturalLanguage);
        example.put("sql", sql);
        return example;
    }
}
