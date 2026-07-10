package com.rx.admin.modules.monitor.exportlog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.exportlog.entity.SysExportLog;
import com.rx.admin.modules.monitor.exportlog.service.ExportLogService;
import com.rx.admin.modules.monitor.exportlog.convert.SysExportLogConvert;
import com.rx.admin.modules.monitor.exportlog.vo.SysExportLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "导出审计")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/export-log")
@SaCheckRole("admin")
@RequiredArgsConstructor
public class SysExportLogController {

    private final ExportLogService exportLogService;
    private final SysExportLogConvert exportLogConvert;

    @Operation(summary = "导出日志列表(分页)")
    @GetMapping("/page")
    @SaCheckPermission("monitor:export-log:query")
    public Result<PageResult<SysExportLogVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String exportType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<SysExportLog> pr = exportLogService.pageQuery(page, size, username, exportType, startTime, endTime);
        return Result.ok(exportLogConvert.toPageResult(pr));
    }
}
