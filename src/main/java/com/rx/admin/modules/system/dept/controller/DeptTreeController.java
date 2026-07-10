package com.rx.admin.modules.system.dept.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.dept.service.DeptTreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组织架构树形管理控制器
 */
@Tag(name = "组织架构管理")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/dept-tree")
@RequiredArgsConstructor
public class DeptTreeController {

    private final DeptTreeService deptTreeService;

    @Operation(summary = "获取部门树")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getDeptTree() {
        return Result.ok(deptTreeService.getDeptTree());
    }

    @Operation(summary = "更新部门排序")
    @PutMapping("/sort")
    @OperateLog(module = "组织架构管理", operation = "更新部门排序")
    public Result<Void> updateSort(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer sort = Integer.valueOf(params.get("sort").toString());
        deptTreeService.updateSort(id, sort);
        return Result.ok();
    }

    @Operation(summary = "批量更新部门排序")
    @PutMapping("/sort/batch")
    @OperateLog(module = "组织架构管理", operation = "批量更新部门排序")
    public Result<Void> batchUpdateSort(@RequestBody List<Map<String, Object>> sortData) {
        deptTreeService.batchUpdateSort(sortData);
        return Result.ok();
    }

    @Operation(summary = "导出部门数据")
    @GetMapping("/export")
    public Result<List<Map<String, Object>>> exportDepts() {
        return Result.ok(deptTreeService.exportDepts());
    }

    @Operation(summary = "导入部门数据")
    @PostMapping("/import")
    @OperateLog(module = "组织架构管理", operation = "导入部门数据")
    public Result<Integer> importDepts(@RequestBody List<Map<String, Object>> deptData) {
        return Result.ok(deptTreeService.importDepts(deptData));
    }
}
