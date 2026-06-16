package com.rx.admin.modules.monitor.logAnalysis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.logAnalysis.service.ApiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API接口分析工具
 * 输入菜单名称，输出该菜单对应的前后端交互分析（数据流、调用链、流程图等）
 */
@Tag(name = "API接口分析")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/analysis")
@RequiredArgsConstructor
public class ApiAnalysisController {

    private final ApiAnalysisService analysisService;

    /**
     * 获取所有可分析的菜单/模块列表
     */
    @Operation(summary = "获取所有可分析的菜单列表")
    @GetMapping("/menus")
    @SaCheckLogin
    public Result<List<Map<String, String>>> getMenus() {
        return Result.ok(analysisService.getAvailableMenus());
    }

    /**
     * 分析指定菜单的完整交互链路
     * @param menuName 菜单名称，如"红楼人物"、"三国诗词"等
     */
    @Operation(summary = "分析菜单交互链路")
    @GetMapping("/analyze")
    @SaCheckLogin
    public Result<Map<String, Object>> analyze(@RequestParam String menuName) {
        Map<String, Object> result = analysisService.analyze(menuName);
        if (result == null) {
            return Result.fail("未找到匹配的菜单：" + menuName + "，请检查菜单名称");
        }
        return Result.ok(result);
    }

    /**
     * 搜索菜单（模糊匹配）
     */
    @Operation(summary = "搜索菜单")
    @GetMapping("/search")
    @SaCheckLogin
    public Result<List<Map<String, String>>> search(@RequestParam String keyword) {
        return Result.ok(analysisService.searchMenus(keyword));
    }
}
