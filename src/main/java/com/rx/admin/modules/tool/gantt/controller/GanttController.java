package com.rx.admin.modules.tool.gantt.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.gantt.dto.GanttProjectCreateDTO;
import com.rx.admin.modules.tool.gantt.dto.GanttTaskCreateDTO;
import com.rx.admin.modules.tool.gantt.entity.GanttProject;
import com.rx.admin.modules.tool.gantt.service.GanttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "甘特图")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/gantt")
@RequiredArgsConstructor
public class GanttController {

    private final GanttService service;

    @SaCheckPermission("tool:gantt:query")
    @GetMapping("/projects")
    @Operation(summary = "获取项目列表")
    public Result<List<GanttProject>> listProjects() {
        return Result.ok(service.listProjects());
    }

    @SaCheckPermission("tool:gantt:query")
    @GetMapping("/projects/{id}")
    @Operation(summary = "获取项目详情")
    public Result<GanttProject> getProject(@PathVariable Long id) {
        return Result.ok(service.getProjectDetail(id));
    }

    @SaCheckPermission("tool:gantt:add")
    @PostMapping("/projects")
    @Operation(summary = "创建项目")
    @OperateLog(module = "甘特图", operation = "创建项目")
    public Result<Void> createProject(@RequestBody @Valid GanttProjectCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        service.createProject(dto, userId);
        return Result.ok();
    }

    @SaCheckPermission("tool:gantt:add")
    @PostMapping("/tasks")
    @Operation(summary = "创建任务")
    @OperateLog(module = "甘特图", operation = "创建任务")
    public Result<Void> createTask(@RequestBody @Valid GanttTaskCreateDTO dto) {
        service.createTask(dto);
        return Result.ok();
    }

    @SaCheckPermission("tool:gantt:edit")
    @PutMapping("/tasks/{id}/progress")
    @Operation(summary = "更新任务进度")
    @OperateLog(module = "甘特图", operation = "更新进度")
    public Result<Void> updateProgress(@PathVariable Long id, @RequestParam Integer progress) {
        service.updateTaskProgress(id, progress);
        return Result.ok();
    }

    @SaCheckPermission("tool:gantt:delete")
    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "删除任务")
    @OperateLog(module = "甘特图", operation = "删除任务")
    public Result<Void> deleteTask(@PathVariable Long id) {
        service.deleteTask(id);
        return Result.ok();
    }

    @SaCheckPermission("tool:gantt:delete")
    @DeleteMapping("/projects/{id}")
    @Operation(summary = "删除项目")
    @OperateLog(module = "甘特图", operation = "删除项目")
    public Result<Void> deleteProject(@PathVariable Long id) {
        service.removeById(id);
        return Result.ok();
    }
}
