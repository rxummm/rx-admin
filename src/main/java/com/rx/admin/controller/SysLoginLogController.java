package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.OperateLog;
import com.rx.admin.common.PageResult;
import com.rx.admin.common.Result;
import com.rx.admin.entity.SysLoginLog;
import com.rx.admin.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "登录日志")
@RestController
@RequestMapping("/api/monitor/login-log")
@SaCheckRole("admin")
public class SysLoginLogController {

    private final LoginLogService loginLogService;

    public SysLoginLogController(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @Operation(summary = "登录日志列表(分页)")
    @GetMapping("/page")
    public Result<PageResult<SysLoginLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.ok(loginLogService.pageQuery(page, size, username, status, startTime, endTime));
    }

    @Operation(summary = "删除登录日志")
    @DeleteMapping("/{id}")
    @OperateLog(module = "登录日志", operation = "删除")
    public Result<?> delete(@PathVariable Long id) {
        loginLogService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除登录日志")
    @DeleteMapping("/batch")
    @OperateLog(module = "登录日志", operation = "批量删除")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        loginLogService.removeByIds(ids);
        return Result.ok();
    }
}
