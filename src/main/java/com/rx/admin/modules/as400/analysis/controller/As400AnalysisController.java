package com.rx.admin.modules.as400.analysis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.as400.analysis.dto.AnalysisRequest;
import com.rx.admin.modules.as400.analysis.service.As400AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "AS400代码分析")
@RestController
@ApiVersion(1)
@RequestMapping("/as400/analysis")
@RequiredArgsConstructor
public class As400AnalysisController {

    private final As400AnalysisService analysisService;

    @Operation(summary = "分析 AS400 源码（RPGLE/RPG III/CL/DDS/DSPF/PRTF）")
    @PostMapping
    @SaCheckLogin
    public Result<Map<String, Object>> analyze(@Valid @RequestBody AnalysisRequest request) {
        Map<String, Object> result = analysisService.analyze(
                request.getSourceCode(),
                request.getSourceType(),
                request.getFileName()
        );
        if (result.containsKey("_error")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> err = (Map<String, Object>) result.get("_error");
            return Result.fail("分析失败: " + err.get("message"));
        }
        return Result.ok(result);
    }
}
