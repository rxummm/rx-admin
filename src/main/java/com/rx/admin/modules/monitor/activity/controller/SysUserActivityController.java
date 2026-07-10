package com.rx.admin.modules.monitor.activity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.activity.service.SysUserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "用户活动热力图")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/activity")
@RequiredArgsConstructor
public class SysUserActivityController {

    private final SysUserActivityService service;

    @SaCheckPermission("monitor:activity:query")
    @GetMapping("/heatmap")
    @Operation(summary = "获取热力图数据")
    public Result<Map<String, Object>> heatmap(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(service.getHeatmapData(startDate, endDate));
    }
}
