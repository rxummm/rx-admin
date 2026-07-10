package com.rx.admin.modules.monitor.loginlog.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.loginlog.service.SmartRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能推荐控制器
 */
@Tag(name = "智能推荐")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/recommendation")
@RequiredArgsConstructor
public class SmartRecommendationController {

    private final SmartRecommendationService recommendationService;

    @Operation(summary = "获取推荐功能")
    @GetMapping("/features")
    public Result<List<Map<String, Object>>> getRecommendedFeatures() {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        return Result.ok(recommendationService.getRecommendedFeatures(userId));
    }

    @Operation(summary = "获取推荐文档")
    @GetMapping("/docs")
    public Result<List<Map<String, Object>>> getRecommendedDocs() {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        return Result.ok(recommendationService.getRecommendedDocs(userId));
    }

    @Operation(summary = "获取相关数据推荐")
    @GetMapping("/related")
    public Result<List<Map<String, Object>>> getRelatedData(
            @RequestParam String dataType,
            @RequestParam Long dataId) {
        return Result.ok(recommendationService.getRelatedData(dataType, dataId));
    }
}
