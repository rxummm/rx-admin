package com.rx.admin.modules.monitor.job.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.exception.ErrorCode;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.job.entity.SysJob;
import com.rx.admin.modules.monitor.job.dto.JobCreateDTO;
import com.rx.admin.modules.monitor.job.dto.JobUpdateDTO;
import com.rx.admin.modules.monitor.job.service.SysJobService;
import com.rx.admin.modules.monitor.job.convert.JobConvert;
import com.rx.admin.modules.monitor.job.vo.JobVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "定时任务管理")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService jobService;
    private final JobConvert jobConvert;

    @Operation(summary = "定时任务分页列表")
    @GetMapping("/page")
    @SaCheckPermission(PermissionConstants.Monitor.JOB_QUERY)
    public Result<PageResult<JobVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageResult<SysJob> pr = jobService.pageQuery(page, size, keyword, status);
        return Result.ok(jobConvert.toPageResult(pr));
    }

    @Operation(summary = "新增定时任务")
    @PostMapping
    @SaCheckPermission(PermissionConstants.Monitor.JOB_ADD)
    @OperateLog(module = "定时任务管理", operation = "新增定时任务")
    public Result<Void> add(@RequestBody @Valid JobCreateDTO dto) {
        jobService.addJob(dto);
        return Result.ok();
    }

    @Operation(summary = "修改定时任务")
    @PutMapping
    @SaCheckPermission(PermissionConstants.Monitor.JOB_EDIT)
    @OperateLog(module = "定时任务管理", operation = "修改定时任务")
    public Result<Void> update(@RequestBody @Valid JobUpdateDTO dto) {
        jobService.updateJob(dto);
        return Result.ok();
    }

    @Operation(summary = "删除定时任务")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Monitor.JOB_DELETE)
    @OperateLog(module = "定时任务管理", operation = "删除定时任务")
    public Result<Void> delete(@PathVariable Long id) {
        jobService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除定时任务")
    @DeleteMapping("/batch")
    @SaCheckPermission(PermissionConstants.Monitor.JOB_DELETE)
    @OperateLog(module = "定时任务管理", operation = "批量删除")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        jobService.deleteJobBatch(ids);
        return Result.ok();
    }

    @Operation(summary = "切换定时任务状态")
    @PutMapping("/status/{id}")
    @SaCheckPermission(PermissionConstants.Monitor.JOB_EDIT)
    @OperateLog(module = "定时任务管理", operation = "切换状态")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        jobService.toggleStatus(id);
        return Result.ok();
    }

    @Operation(summary = "执行一次定时任务")
    @PutMapping("/run/{id}")
    @SaCheckPermission(PermissionConstants.Monitor.JOB_RUN)
    @OperateLog(module = "定时任务管理", operation = "执行一次")
    public Result<Void> runOnce(@PathVariable Long id) {
        SysJob job = jobService.getById(id);
        if (job == null) {
            return Result.fail(ErrorCode.NOT_FOUND, "任务不存在");
        }
        return Result.ok();
    }
}
