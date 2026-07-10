package com.rx.admin.modules.system.search.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.search.service.SmartSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能搜索控制器
 */
@Tag(name = "智能搜索")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/search")
@RequiredArgsConstructor
public class SmartSearchController {

    private final SmartSearchService smartSearchService;

    @Operation(summary = "智能搜索")
    @PostMapping("/smart")
    public Result<List<String>> smartSearch(
            @RequestParam String keyword,
            @RequestBody List<String> candidates) {
        return Result.ok(smartSearchService.smartSearch(keyword, candidates));
    }

    @Operation(summary = "获取同义词")
    @GetMapping("/synonyms")
    public Result<List<String>> getSynonyms(@RequestParam String keyword) {
        return Result.ok(smartSearchService.getSynonyms(keyword));
    }

    @Operation(summary = "高亮搜索关键词")
    @PostMapping("/highlight")
    public Result<String> highlight(
            @RequestParam String text,
            @RequestParam String keyword) {
        return Result.ok(smartSearchService.highlight(text, keyword));
    }
}
