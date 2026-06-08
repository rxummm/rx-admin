package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.PageResult;
import com.rx.admin.common.Result;
import com.rx.admin.entity.SysExportLog;
import com.rx.admin.service.ExportLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "导出审计")
@RestController
@RequestMapping("/api/monitor/export-log")
@SaCheckRole("admin")
public class SysExportLogController {

    private final ExportLogService exportLogService;

    public SysExportLogController(ExportLogService exportLogService) {
        this.exportLogService = exportLogService;
    }

    @Operation(summary = "导出日志列表(分页)")
    @GetMapping("/page")
    public Result<PageResult<SysExportLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String exportType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.ok(exportLogService.pageQuery(page, size, username, exportType, startTime, endTime));
    }
}
