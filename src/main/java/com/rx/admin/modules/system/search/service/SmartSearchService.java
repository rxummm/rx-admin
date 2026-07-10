package com.rx.admin.modules.system.search.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 智能搜索服务
 * 支持模糊匹配、拼音搜索、同义词扩展
 */
@Slf4j
@Service
public class SmartSearchService {

    // 同义词映射表
    private static final Map<String, List<String>> SYNONYM_MAP = new HashMap<>();
    
    static {
        // 常见同义词
        SYNONYM_MAP.put("用户", Arrays.asList("user", "账号", "账户"));
        SYNONYM_MAP.put("角色", Arrays.asList("role", "权限组"));
        SYNONYM_MAP.put("菜单", Arrays.asList("menu", "导航", "栏目"));
        SYNONYM_MAP.put("配置", Arrays.asList("config", "设置", "参数"));
        SYNONYM_MAP.put("日志", Arrays.asList("log", "记录", "操作记录"));
        SYNONYM_MAP.put("通知", Arrays.asList("notice", "公告", "消息"));
        SYNONYM_MAP.put("文件", Arrays.asList("file", "文档", "附件"));
        SYNONYM_MAP.put("部门", Arrays.asList("dept", "组织", "机构"));
        SYNONYM_MAP.put("字典", Arrays.asList("dict", "数据字典", "编码"));
    }

    /**
     * 智能搜索
     * 支持模糊匹配、同义词扩展
     */
    public List<String> smartSearch(String keyword, List<String> candidates) {
        if (keyword == null || keyword.isEmpty()) {
            return candidates;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        // 1. 获取同义词
        List<String> synonyms = getSynonyms(keyword);
        
        // 2. 模糊匹配
        return candidates.stream()
                .filter(candidate -> {
                    String lowerCandidate = candidate.toLowerCase();
                    // 直接匹配
                    if (lowerCandidate.contains(lowerKeyword)) {
                        return true;
                    }
                    // 同义词匹配
                    for (String synonym : synonyms) {
                        if (lowerCandidate.contains(synonym.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取同义词
     */
    public List<String> getSynonyms(String keyword) {
        List<String> synonyms = new ArrayList<>();
        
        // 精确匹配
        if (SYNONYM_MAP.containsKey(keyword)) {
            synonyms.addAll(SYNONYM_MAP.get(keyword));
        }
        
        // 部分匹配
        for (Map.Entry<String, List<String>> entry : SYNONYM_MAP.entrySet()) {
            if (keyword.contains(entry.getKey()) || entry.getKey().contains(keyword)) {
                synonyms.addAll(entry.getValue());
            }
        }
        
        return synonyms.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 拼音匹配（简化实现）
     * 实际项目中应使用 pinyin4j 库
     */
    public boolean pinyinMatch(String keyword, String candidate) {
        if (keyword == null || candidate == null) {
            return false;
        }
        
        // 简化实现：直接字符串包含匹配
        return candidate.toLowerCase().contains(keyword.toLowerCase());
    }

    /**
     * 高亮搜索关键词
     */
    public String highlight(String text, String keyword) {
        if (text == null || keyword == null || keyword.isEmpty()) {
            return text;
        }
        
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).replaceAll(m -> "<mark>" + m.group() + "</mark>");
    }
}
