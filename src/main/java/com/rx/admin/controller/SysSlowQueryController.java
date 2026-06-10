package com.rx.admin.controller;

import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysSlowQuery;
import com.rx.admin.service.SysSlowQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "慢查询监控")
@RestController
@RequestMapping("/api/monitor/slow-query")
@RequiredArgsConstructor
public class SysSlowQueryController {

    private final SysSlowQueryService slowQueryService;

    @Operation(summary = "慢查询分页列表")
    @GetMapping("/page")
    public Result<PageResult<SysSlowQuery>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String queryType) {
        return Result.ok(slowQueryService.pageQuery(page, size, keyword, queryType));
    }

    @Operation(summary = "删除慢查询记录")
    @DeleteMapping("/{id}")
    @OperateLog(module = "慢查询监控", operation = "删除记录")
    public Result<Void> delete(@PathVariable Long id) {
        slowQueryService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    @OperateLog(module = "慢查询监控", operation = "批量删除")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        slowQueryService.removeByIds(ids);
        return Result.ok();
    }

    @Operation(summary = "清空所有")
    @DeleteMapping("/clear")
    @OperateLog(module = "慢查询监控", operation = "清空记录")
    public Result<Void> clear() {
        slowQueryService.remove(null);
        return Result.ok();
    }
}
