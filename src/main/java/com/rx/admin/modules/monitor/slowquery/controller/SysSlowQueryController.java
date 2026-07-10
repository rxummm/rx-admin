package com.rx.admin.modules.monitor.slowquery.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.slowquery.entity.SysSlowQuery;
import com.rx.admin.modules.monitor.slowquery.service.SysSlowQueryService;
import com.rx.admin.modules.monitor.slowquery.convert.SlowQueryConvert;
import com.rx.admin.modules.monitor.slowquery.vo.SlowQueryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "慢查询监控")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/slow-query")
@RequiredArgsConstructor
public class SysSlowQueryController {

    private final SysSlowQueryService slowQueryService;
    private final SlowQueryConvert slowQueryConvert;

    @Operation(summary = "慢查询分页列表")
    @GetMapping("/page")
    @SaCheckPermission("monitor:slow-query:query")
    public Result<PageResult<SlowQueryVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String queryType) {
        PageResult<SysSlowQuery> pr = slowQueryService.pageQuery(page, size, keyword, queryType);
        return Result.ok(slowQueryConvert.toPageResult(pr));
    }

    @Operation(summary = "删除慢查询记录")
    @DeleteMapping("/{id}")
    @SaCheckPermission("monitor:slow-query:delete")
    @OperateLog(module = "慢查询监控", operation = "删除记录")
    public Result<Void> delete(@PathVariable Long id) {
        slowQueryService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    @SaCheckPermission("monitor:slow-query:delete")
    @OperateLog(module = "慢查询监控", operation = "批量删除")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        slowQueryService.removeByIds(ids);
        return Result.ok();
    }

    @Operation(summary = "清空所有")
    @DeleteMapping("/clear")
    @SaCheckPermission("monitor:slow-query:delete")
    @OperateLog(module = "慢查询监控", operation = "清空记录")
    public Result<Void> clear() {
        slowQueryService.remove(null);
        return Result.ok();
    }
}
