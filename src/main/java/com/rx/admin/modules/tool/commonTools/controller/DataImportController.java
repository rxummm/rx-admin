package com.rx.admin.modules.tool.commonTools.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.tool.commonTools.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 数据导入控制器
 */
@Tag(name = "数据导入")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/data-import")
@RequiredArgsConstructor
public class DataImportController {

    private final DataImportService importService;

    @Operation(summary = "预览 Excel 文件")
    @PostMapping("/preview")
    public Result<Map<String, Object>> previewExcel(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.ok(importService.previewExcel(file));
    }

    @Operation(summary = "导入数据")
    @PostMapping("/import")
    public Result<Map<String, Object>> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fieldMapping", required = false) Map<String, String> fieldMapping) throws Exception {
        return Result.ok(importService.importData(file, fieldMapping));
    }

    @Operation(summary = "验证导入数据")
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateImportData(
            @RequestBody List<Map<String, String>> data,
            @RequestBody Map<String, String> validationRules) {
        return Result.ok(importService.validateImportData(data, validationRules));
    }
}
