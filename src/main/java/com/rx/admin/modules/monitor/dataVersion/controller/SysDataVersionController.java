package com.rx.admin.modules.monitor.dataVersion.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.dataVersion.service.SysDataVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "数据版本追踪")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/data-version")
@RequiredArgsConstructor
public class SysDataVersionController {

    private final SysDataVersionService service;

    @SaCheckPermission("monitor:data-version:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询数据版本")
    public Result<?> page(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) Long recordId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(service.queryPage(tableName, recordId, page, size));
    }
}
