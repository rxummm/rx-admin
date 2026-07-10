package com.rx.admin.modules.tool.wiki.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.wiki.service.DocumentClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文档智能分类控制器
 */
@Tag(name = "文档智能分类")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/doc-classification")
@RequiredArgsConstructor
public class DocumentClassificationController {

    private final DocumentClassificationService classificationService;

    @Operation(summary = "对文档进行分类")
    @PostMapping("/classify")
    public Result<Map<String, Object>> classifyDocument(@RequestBody Map<String, String> params) {
        String title = params.get("title");
        String content = params.get("content");
        return Result.ok(classificationService.classifyDocument(title, content));
    }

    @Operation(summary = "提取文档标签")
    @PostMapping("/tags")
    public Result<List<String>> extractTags(@RequestBody Map<String, String> params) {
        String title = params.get("title");
        String content = params.get("content");
        return Result.ok(classificationService.extractTags(title, content));
    }

    @Operation(summary = "批量分类文档")
    @PostMapping("/batch")
    public Result<List<Map<String, Object>>> batchClassify(@RequestBody List<Map<String, String>> documents) {
        return Result.ok(classificationService.batchClassify(documents));
    }
}
