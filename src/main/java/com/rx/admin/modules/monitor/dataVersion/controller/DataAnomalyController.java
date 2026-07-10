package com.rx.admin.modules.monitor.dataVersion.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.dataVersion.service.DataAnomalyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据异常检测控制器
 */
@Tag(name = "数据异常检测")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/data-anomaly")
@RequiredArgsConstructor
public class DataAnomalyController {

    private final DataAnomalyService anomalyService;

    @Operation(summary = "获取异常检测报告")
    @GetMapping("/report")
    public Result<Map<String, Object>> getAnomalyReport() {
        return Result.ok(anomalyService.getAnomalyReport());
    }

    @Operation(summary = "手动触发异常检测")
    @PostMapping("/detect")
    public Result<Void> detectAnomalies() {
        anomalyService.detectAnomalies();
        return Result.ok();
    }
}
