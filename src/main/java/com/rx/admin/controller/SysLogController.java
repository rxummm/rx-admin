package com.rx.admin.controller;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "日志管理")
@RestController
@RequestMapping("/api/monitor/log")
public class SysLogController {

    private final SysLogService logService;

    public SysLogController(SysLogService logService) {
        this.logService = logService;
    }

    @Operation(summary = "日志列表(分页)")
    @GetMapping("/page")
    public Result<PageResult<?>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.ok(logService.pageQuery(page, size, keyword, status, startTime, endTime));
    }

    @Operation(summary = "删除日志")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        logService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除日志")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        logService.removeByIds(ids);
        return Result.ok();
    }
}