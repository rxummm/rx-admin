package com.rx.admin.modules.monitor.loginlog.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.loginlog.service.NaturalLanguageQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 自然语言查询控制器
 */
@Tag(name = "自然语言查询")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/nlq")
@RequiredArgsConstructor
public class NaturalLanguageQueryController {

    private final NaturalLanguageQueryService nlqService;

    @Operation(summary = "自然语言转SQL")
    @PostMapping("/convert")
    public Result<Map<String, Object>> convertToSql(@RequestBody Map<String, String> params) {
        String naturalLanguage = params.get("query");
        return Result.ok(nlqService.convertToSql(naturalLanguage));
    }

    @Operation(summary = "获取查询示例")
    @GetMapping("/examples")
    public Result<List<Map<String, String>>> getExamples() {
        return Result.ok(nlqService.getExamples());
    }
}
