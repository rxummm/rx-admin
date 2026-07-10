package com.rx.admin.modules.tool.kanban.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.kanban.entity.KanbanCard;
import com.rx.admin.modules.tool.kanban.service.KanbanEnhanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 看板增强控制器
 */
@Tag(name = "看板增强")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/kanban-enhance")
@RequiredArgsConstructor
public class KanbanEnhanceController {

    private final KanbanEnhanceService kanbanEnhanceService;

    @Operation(summary = "添加任务依赖")
    @PostMapping("/dependency")
    @OperateLog(module = "看板管理", operation = "添加任务依赖")
    public Result<Void> addDependency(@RequestParam Long cardId, @RequestParam Long dependsOnId) {
        kanbanEnhanceService.addDependency(cardId, dependsOnId);
        return Result.ok();
    }

    @Operation(summary = "移除任务依赖")
    @DeleteMapping("/dependency")
    @OperateLog(module = "看板管理", operation = "移除任务依赖")
    public Result<Void> removeDependency(@RequestParam Long cardId, @RequestParam Long dependsOnId) {
        kanbanEnhanceService.removeDependency(cardId, dependsOnId);
        return Result.ok();
    }

    @Operation(summary = "获取任务依赖列表")
    @GetMapping("/card/{cardId}/dependencies")
    public Result<List<KanbanCard>> getDependencies(@PathVariable Long cardId) {
        return Result.ok(kanbanEnhanceService.getDependencies(cardId));
    }

    @Operation(summary = "获取依赖此任务的任务列表")
    @GetMapping("/card/{cardId}/dependents")
    public Result<List<KanbanCard>> getDependents(@PathVariable Long cardId) {
        return Result.ok(kanbanEnhanceService.getDependents(cardId));
    }

    @Operation(summary = "检查任务是否可以开始")
    @GetMapping("/card/{cardId}/can-start")
    public Result<Boolean> canStart(@PathVariable Long cardId) {
        return Result.ok(kanbanEnhanceService.canStart(cardId));
    }

    @Operation(summary = "获取工时统计")
    @GetMapping("/board/{boardId}/stats")
    public Result<Map<String, Object>> getWorkHoursStats(@PathVariable Long boardId) {
        return Result.ok(kanbanEnhanceService.getWorkHoursStats(boardId));
    }
}
