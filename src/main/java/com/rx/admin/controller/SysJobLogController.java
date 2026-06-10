package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysJobLog;
import com.rx.admin.service.JobLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "任务日志")
@RestController
@RequestMapping("/api/monitor/job-log")
@SaCheckRole("admin")
public class SysJobLogController {

    private final JobLogService jobLogService;

    public SysJobLogController(JobLogService jobLogService) {
        this.jobLogService = jobLogService;
    }

    @Operation(summary = "任务执行日志列表")
    @GetMapping("/page")
    public Result<PageResult<SysJobLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.ok(jobLogService.pageQuery(page, size, jobId, status, startTime, endTime));
    }

    @Operation(summary = "删除日志")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        jobLogService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        jobLogService.removeByIds(ids);
        return Result.ok();
    }
}
