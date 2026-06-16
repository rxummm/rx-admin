package com.rx.admin.modules.monitor.log.controller;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import com.rx.admin.modules.monitor.log.service.SysLogService;
import com.rx.admin.modules.monitor.log.convert.OperateLogConvert;
import com.rx.admin.modules.monitor.log.vo.OperateLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "日志管理")
@RestController
@RequestMapping("/api/monitor/log")
public class SysLogController {

    private final SysLogService logService;
    private final OperateLogConvert logConvert;

    public SysLogController(SysLogService logService, OperateLogConvert logConvert) {
        this.logService = logService;
        this.logConvert = logConvert;
    }

    @Operation(summary = "日志列表(分页)")
    @GetMapping("/page")
    public Result<PageResult<OperateLogVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<SysLog> pr = logService.pageQuery(page, size, keyword, status, startTime, endTime);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), logConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "删除日志")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        logService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除日志")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        logService.removeByIds(ids);
        return Result.ok();
    }
}
