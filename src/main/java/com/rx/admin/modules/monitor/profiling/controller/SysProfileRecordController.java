package com.rx.admin.modules.monitor.profiling.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.profiling.service.SysProfileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "性能分析")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/profiling")
@RequiredArgsConstructor
public class SysProfileRecordController {

    private final SysProfileRecordService service;

    @SaCheckPermission("monitor:profiling:query")
    @GetMapping("/stats")
    @Operation(summary = "获取性能统计数据")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) String startDate) {
        return Result.ok(service.getProfilingStats(startDate));
    }
}
