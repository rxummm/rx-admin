package com.rx.admin.modules.tool.wiki.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 文档智能分类服务
 * AI自动对上传的文档进行分类和标签提取
 */
@Slf4j
@Service
public class DocumentClassificationService {

    // 分类关键词映射
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    
    static {
        CATEGORY_KEYWORDS.put("技术文档", Arrays.asList("API", "接口", "框架", "架构", "设计模式", "代码", "开发", "编程", "Java", "Spring", "Vue"));
        CATEGORY_KEYWORDS.put("需求文档", Arrays.asList("需求", "功能", "用户故事", "验收标准", "PRD", "原型"));
        CATEGORY_KEYWORDS.put("设计文档", Arrays.asList("设计", "UI", "UX", "原型", "交互", "视觉", "样式"));
        CATEGORY_KEYWORDS.put("测试文档", Arrays.asList("测试", "用例", "缺陷", "Bug", "回归", "自动化"));
        CATEGORY_KEYWORDS.put("运维文档", Arrays.asList("部署", "运维", "监控", "日志", "备份", "恢复", "Docker", "K8s"));
        CATEGORY_KEYWORDS.put("产品文档", Arrays.asList("产品", "规划", "路线图", "版本", "发布", "迭代"));
        CATEGORY_KEYWORDS.put("会议纪要", Arrays.asList("会议", "纪要", "决议", "行动项", "参与人"));
        CATEGORY_KEYWORDS.put("培训资料", Arrays.asList("培训", "教程", "指南", "入门", "学习", "课程"));
    }

    /**
     * 对文档进行分类
     */
    public Map<String, Object> classifyDocument(String title, String content) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        String text = (title != null ? title : "") + " " + (content != null ? content : "");
        text = text.toLowerCase();
        
        // 计算每个分类的匹配分数
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword.toLowerCase())) {
                    score++;
                }
            }
            if (score > 0) {
                scores.put(entry.getKey(), score);
            }
        }
        
        // 获取最佳分类
        String bestCategory = "其他";
        int bestScore = 0;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }
        
        result.put("category", bestCategory);
        result.put("confidence", bestScore > 0 ? Math.min(bestScore * 0.2, 1.0) : 0.0);
        result.put("scores", scores);
        
        return result;
    }

    /**
     * 提取文档标签
     */
    public List<String> extractTags(String title, String content) {
        Set<String> tags = new LinkedHashSet<>();
        
        String text = (title != null ? title : "") + " " + (content != null ? content : "");
        
        // 提取技术关键词
        String[] techKeywords = {"Java", "Spring", "Vue", "MySQL", "Redis", "Docker", "K8s", 
                                "API", "REST", "GraphQL", "WebSocket", "HTTP", "HTTPS"};
        for (String keyword : techKeywords) {
            if (text.contains(keyword)) {
                tags.add(keyword);
            }
        }
        
        // 提取内容关键词
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (text.toLowerCase().contains(keyword.toLowerCase())) {
                    tags.add(keyword);
                }
            }
        }
        
        // 限制标签数量
        return new ArrayList<>(tags).stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 批量分类文档
     */
    public List<Map<String, Object>> batchClassify(List<Map<String, String>> documents) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Map<String, String> doc : documents) {
            String title = doc.get("title");
            String content = doc.get("content");
            
            Map<String, Object> classification = classifyDocument(title, content);
            classification.put("title", title);
            classification.put("tags", extractTags(title, content));
            
            results.add(classification);
        }
        
        return results;
    }
}
