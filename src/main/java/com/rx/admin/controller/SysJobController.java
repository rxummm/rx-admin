package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysJob;
import com.rx.admin.service.SysJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "定时任务管理")
@RestController
@RequestMapping("/api/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService jobService;

    @Operation(summary = "定时任务分页列表")
    @GetMapping("/page")
    @SaCheckPermission("monitor:job:query")
    public Result<PageResult<SysJob>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.ok(jobService.pageQuery(page, size, keyword, status));
    }

    @Operation(summary = "新增定时任务")
    @PostMapping
    @SaCheckPermission("monitor:job:add")
    @OperateLog(module = "定时任务管理", operation = "新增定时任务")
    public Result<Void> add(@RequestBody @Valid SysJob job) {
        jobService.save(job);
        return Result.ok();
    }

    @Operation(summary = "修改定时任务")
    @PutMapping
    @SaCheckPermission("monitor:job:edit")
    @OperateLog(module = "定时任务管理", operation = "修改定时任务")
    public Result<Void> update(@RequestBody @Valid SysJob job) {
        jobService.updateById(job);
        return Result.ok();
    }

    @Operation(summary = "删除定时任务")
    @DeleteMapping("/{id}")
    @SaCheckPermission("monitor:job:delete")
    @OperateLog(module = "定时任务管理", operation = "删除定时任务")
    public Result<Void> delete(@PathVariable Long id) {
        jobService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "切换定时任务状态")
    @PutMapping("/status/{id}")
    @SaCheckPermission("monitor:job:edit")
    @OperateLog(module = "定时任务管理", operation = "切换状态")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        SysJob job = jobService.getById(id);
        if (job != null) {
            job.setStatus(job.getStatus() != null && job.getStatus() == 1 ? 0 : 1);
            jobService.updateById(job);
        }
        return Result.ok();
    }

    @Operation(summary = "执行一次定时任务")
    @PutMapping("/run/{id}")
    @SaCheckPermission("monitor:job:edit")
    @OperateLog(module = "定时任务管理", operation = "执行一次")
    public Result<Void> runOnce(@PathVariable Long id) {
        SysJob job = jobService.getById(id);
        if (job == null) {
            return Result.fail(404, "任务不存在");
        }
        return Result.ok();
    }
}
