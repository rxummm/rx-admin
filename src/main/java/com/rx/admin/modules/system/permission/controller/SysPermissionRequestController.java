package com.rx.admin.modules.system.permission.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.permission.entity.SysPermissionRequest;
import com.rx.admin.modules.system.permission.service.SysPermissionRequestService;
import com.rx.admin.modules.system.permission.convert.PermissionRequestConvert;
import com.rx.admin.modules.system.permission.dto.EmailPermissionRequestDTO;
import com.rx.admin.modules.system.permission.dto.PermissionSubmitDTO;
import com.rx.admin.modules.system.permission.vo.SysPermissionRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "权限申请")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/permission-request")
@RequiredArgsConstructor
public class SysPermissionRequestController {

    private final SysPermissionRequestService requestService;
    private final PermissionRequestConvert requestConvert;

    @Operation(summary = "提交权限申请")
    @PostMapping
    @SaCheckLogin
    @SaCheckPermission("system:permission:submit")
    public Result<?> submit(@RequestBody @Valid PermissionSubmitDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        requestService.submitRequest(userId, dto.getMenuIds(), dto.getMenuNames());
        return Result.ok("权限申请已提交，请等待管理员审批");
    }

    @Operation(summary = "获取待审批列表（admin）")
    @GetMapping("/pending")
    @SaCheckRole("admin")
    @SaCheckPermission("system:permission:query")
    public Result<Page<SysPermissionRequestVO>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysPermissionRequest> pr = requestService.getPendingRequests(page, size);
        Page<SysPermissionRequestVO> voPage = new Page<>(pr.getCurrent(), pr.getSize(), pr.getTotal());
        voPage.setRecords(requestConvert.toVOList(pr.getRecords()));
        return Result.ok(voPage);
    }

    @Operation(summary = "获取我的申请列表")
    @GetMapping("/my")
    @SaCheckLogin
    @SaCheckPermission("system:permission:query")
    public Result<Page<SysPermissionRequestVO>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysPermissionRequest> pr = requestService.getMyRequests(page, size);
        Page<SysPermissionRequestVO> voPage = new Page<>(pr.getCurrent(), pr.getSize(), pr.getTotal());
        voPage.setRecords(requestConvert.toVOList(pr.getRecords()));
        return Result.ok(voPage);
    }

    @Operation(summary = "审批通过")
    @PutMapping("/{id}/approve")
    @SaCheckRole("admin")
    @SaCheckPermission("system:permission:manage")
    public Result<?> approve(@PathVariable Long id) {
        requestService.approve(id);
        return Result.ok("审批通过");
    }

    @Operation(summary = "审批拒绝")
    @PutMapping("/{id}/reject")
    @SaCheckRole("admin")
    @SaCheckPermission("system:permission:manage")
    public Result<?> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.getOrDefault("remark", "") : "";
        requestService.reject(id, remark);
        return Result.ok("已拒绝");
    }

    @Operation(summary = "邮件申请权限")
    @PostMapping("/email-request")
    @SaCheckLogin
    @SaCheckPermission("system:permission:submit")
    public Result<?> emailRequest(@RequestBody @Valid EmailPermissionRequestDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        requestService.submitEmailRequest(userId, dto.getUserName(), dto.getMenus(), dto.getDescription());
        return Result.ok("邮件申请已发送给管理员，请等待回复");
    }
}
