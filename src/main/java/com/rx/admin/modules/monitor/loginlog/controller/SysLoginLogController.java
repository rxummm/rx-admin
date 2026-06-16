package com.rx.admin.modules.monitor.loginlog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.loginlog.service.LoginLogService;
import com.rx.admin.modules.monitor.loginlog.convert.LoginLogConvert;
import com.rx.admin.modules.monitor.loginlog.vo.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "登录日志")
@RestController
@ApiVersion(1)
@RequestMapping("/monitor/login-log")
@SaCheckRole("admin")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final LoginLogService loginLogService;
    private final LoginLogConvert loginLogConvert;

    @Operation(summary = "登录日志列表(分页)")
    @GetMapping("/page")
    @SaCheckPermission("monitor:login-log:query")
    public Result<PageResult<LoginLogVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<SysLoginLog> pr = loginLogService.pageQuery(page, size, username, status, startTime, endTime);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), loginLogConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "删除登录日志")
    @DeleteMapping("/{id}")
    @SaCheckPermission("monitor:login-log:delete")
    @OperateLog(module = "登录日志", operation = "删除")
    public Result<?> delete(@PathVariable Long id) {
        loginLogService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除登录日志")
    @DeleteMapping("/batch")
    @SaCheckPermission("monitor:login-log:delete")
    @OperateLog(module = "登录日志", operation = "批量删除")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        loginLogService.removeByIds(ids);
        return Result.ok();
    }
}
