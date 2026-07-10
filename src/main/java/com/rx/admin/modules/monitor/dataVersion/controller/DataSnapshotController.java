package com.rx.admin.modules.monitor.dataVersion.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.dataVersion.entity.SysDataSnapshot;
import com.rx.admin.modules.monitor.dataVersion.service.DataSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据快照控制器
 */
@Tag(name = "数据快照")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/data-snapshot")
@RequiredArgsConstructor
public class DataSnapshotController {

    private final DataSnapshotService snapshotService;

    @Operation(summary = "获取数据快照列表")
    @GetMapping("/list")
    public Result<List<SysDataSnapshot>> getSnapshots(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) Long recordId) {
        return Result.ok(snapshotService.getSnapshots(tableName, recordId));
    }

    @Operation(summary = "获取回滚数据")
    @GetMapping("/{id}/rollback-data")
    public Result<Map<String, Object>> getRollbackData(@PathVariable Long id) {
        return Result.ok(snapshotService.getRollbackData(id));
    }

    @Operation(summary = "标记为已回滚")
    @PostMapping("/{id}/mark-rollback")
    public Result<Void> markAsRolledBack(@PathVariable Long id) {
        snapshotService.markAsRolledBack(id);
        return Result.ok();
    }
}
