package com.rx.admin.modules.tool.kanban.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.tool.kanban.entity.KanbanCard;
import com.rx.admin.modules.tool.kanban.entity.KanbanDependency;
import com.rx.admin.modules.tool.kanban.mapper.KanbanCardMapper;
import com.rx.admin.modules.tool.kanban.mapper.KanbanDependencyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 看板增强服务
 * 支持任务依赖、工时统计
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KanbanEnhanceService {

    private final KanbanCardMapper cardMapper;
    private final KanbanDependencyMapper dependencyMapper;

    /**
     * 添加任务依赖
     */
    public void addDependency(Long cardId, Long dependsOnId) {
        // 检查是否已存在
        LambdaQueryWrapper<KanbanDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KanbanDependency::getCardId, cardId)
               .eq(KanbanDependency::getDependsOnId, dependsOnId);
        Long count = dependencyMapper.selectCount(wrapper);
        
        if (count == 0) {
            KanbanDependency dependency = new KanbanDependency();
            dependency.setCardId(cardId);
            dependency.setDependsOnId(dependsOnId);
            dependency.setCreateTime(LocalDateTime.now());
            dependencyMapper.insert(dependency);
        }
    }

    /**
     * 移除任务依赖
     */
    public void removeDependency(Long cardId, Long dependsOnId) {
        LambdaQueryWrapper<KanbanDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KanbanDependency::getCardId, cardId)
               .eq(KanbanDependency::getDependsOnId, dependsOnId);
        dependencyMapper.delete(wrapper);
    }

    /**
     * 获取任务的依赖列表
     */
    public List<KanbanCard> getDependencies(Long cardId) {
        LambdaQueryWrapper<KanbanDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KanbanDependency::getCardId, cardId);
        List<KanbanDependency> dependencies = dependencyMapper.selectList(wrapper);
        
        if (dependencies.isEmpty()) {
            return List.of();
        }
        
        List<Long> dependsOnIds = dependencies.stream()
                .map(KanbanDependency::getDependsOnId)
                .collect(Collectors.toList());
        
        return cardMapper.selectBatchIds(dependsOnIds);
    }

    /**
     * 获取依赖此任务的任务列表
     */
    public List<KanbanCard> getDependents(Long cardId) {
        LambdaQueryWrapper<KanbanDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KanbanDependency::getDependsOnId, cardId);
        List<KanbanDependency> dependencies = dependencyMapper.selectList(wrapper);
        
        if (dependencies.isEmpty()) {
            return List.of();
        }
        
        List<Long> cardIds = dependencies.stream()
                .map(KanbanDependency::getCardId)
                .collect(Collectors.toList());
        
        return cardMapper.selectBatchIds(cardIds);
    }

    /**
     * 检查任务是否可以开始（所有依赖任务已存在）
     */
    public boolean canStart(Long cardId) {
        List<KanbanCard> dependencies = getDependencies(cardId);
        return dependencies.isEmpty();
    }

    /**
     * 获取工时统计
     */
    public Map<String, Object> getWorkHoursStats(Long boardId) {
        LambdaQueryWrapper<KanbanCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KanbanCard::getBoardId, boardId);
        List<KanbanCard> cards = cardMapper.selectList(wrapper);
        
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCards", cards.size());
        
        // 按优先级统计
        Map<String, Long> priorityStats = cards.stream()
                .filter(card -> card.getPriority() != null)
                .collect(Collectors.groupingBy(KanbanCard::getPriority, Collectors.counting()));
        stats.put("priorityStats", priorityStats);
        
        return stats;
    }
}
