package com.rx.admin.modules.monitor.exportlog.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.exportlog.entity.SysExportLog;
import com.rx.admin.modules.monitor.exportlog.service.ExportLogService;
import com.rx.admin.modules.monitor.exportlog.convert.SysExportLogConvert;
import com.rx.admin.modules.monitor.exportlog.vo.SysExportLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "导出审计")
@RestController
@RequestMapping("/api/monitor/export-log")
@SaCheckRole("admin")
public class SysExportLogController {

    private final ExportLogService exportLogService;
    private final SysExportLogConvert exportLogConvert;

    public SysExportLogController(ExportLogService exportLogService, SysExportLogConvert exportLogConvert) {
        this.exportLogService = exportLogService;
        this.exportLogConvert = exportLogConvert;
    }

    @Operation(summary = "导出日志列表(分页)")
    @GetMapping("/page")
    public Result<PageResult<SysExportLogVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String exportType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<SysExportLog> pr = exportLogService.pageQuery(page, size, username, exportType, startTime, endTime);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), exportLogConvert.toVOList(pr.getRecords())));
    }
}
