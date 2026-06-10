package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysPermissionRequest;
import com.rx.admin.service.SysPermissionRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "权限申请")
@RestController
@RequestMapping("/api/sys/permission-request")
public class SysPermissionRequestController {

    private final SysPermissionRequestService requestService;

    public SysPermissionRequestController(SysPermissionRequestService requestService) {
        this.requestService = requestService;
    }

    @Operation(summary = "提交权限申请")
    @PostMapping
    @SaCheckLogin
    @SuppressWarnings("unchecked")
    public Result<?> submit(@RequestBody Map<String, Object> body) {
        long userId = StpUtil.getLoginIdAsLong();
        // 使用 List<Number> 接收，避免 Jackson 反序列化为 Integer 或 Long 不一致导致 ClassCastException
        List<Number> menuIdsRaw = (List<Number>) body.get("menuIds");
        List<String> menuNames = (List<String>) body.get("menuNames");
        List<Long> menuIds = menuIdsRaw.stream().map(Number::longValue).toList();
        requestService.submitRequest(userId, menuIds, menuNames);
        return Result.ok("权限申请已提交，请等待管理员审批");
    }

    @Operation(summary = "获取待审批列表（admin）")
    @GetMapping("/pending")
    @SaCheckRole("admin")
    public Result<Page<SysPermissionRequest>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(requestService.getPendingRequests(page, size));
    }

    @Operation(summary = "获取我的申请列表")
    @GetMapping("/my")
    @SaCheckLogin
    public Result<Page<SysPermissionRequest>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(requestService.getMyRequests(page, size));
    }

    @Operation(summary = "审批通过")
    @PutMapping("/{id}/approve")
    @SaCheckRole("admin")
    public Result<?> approve(@PathVariable Long id) {
        requestService.approve(id);
        return Result.ok("审批通过");
    }

    @Operation(summary = "审批拒绝")
    @PutMapping("/{id}/reject")
    @SaCheckRole("admin")
    public Result<?> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.getOrDefault("remark", "") : "";
        requestService.reject(id, remark);
        return Result.ok("已拒绝");
    }

    @Operation(summary = "邮件申请权限（申请角色范围外的菜单权限）")
    @PostMapping("/email-request")
    @SaCheckLogin
    public Result<?> emailRequest(@RequestBody Map<String, Object> body) {
        long userId = StpUtil.getLoginIdAsLong();
        String userName = body.getOrDefault("userName", "").toString();
        String description = body.getOrDefault("description", "").toString();
        String menus = body.getOrDefault("menus", "").toString();
        requestService.submitEmailRequest(userId, userName, menus, description);
        return Result.ok("邮件申请已发送给管理员，请等待回复");
    }
}
