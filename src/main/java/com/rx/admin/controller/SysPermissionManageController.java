package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.Result;
import com.rx.admin.entity.SysMenu;
import com.rx.admin.service.SysPermissionManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户权限管理接口（仅 admin 可操作）
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/sys/permission-manage")
@SaCheckRole("admin")
public class SysPermissionManageController {

    private final SysPermissionManageService permissionManageService;

    public SysPermissionManageController(SysPermissionManageService permissionManageService) {
        this.permissionManageService = permissionManageService;
    }

    @Operation(summary = "获取用户已有菜单权限ID列表")
    @GetMapping("/user/{userId}/menus")
    public Result<Set<Long>> getUserMenuIds(@PathVariable Long userId) {
        return Result.ok(permissionManageService.getUserMenuIds(userId));
    }

    @Operation(summary = "获取可管理的菜单树（排除管理类菜单和已拥有菜单）")
    @GetMapping("/user/{userId}/manageable-tree")
    public Result<List<SysMenu>> getManageableMenuTree(@PathVariable Long userId) {
        return Result.ok(permissionManageService.getManageableMenuTree(userId));
    }

    @Operation(summary = "给用户添加菜单权限")
    @PostMapping("/user/{userId}/add")
    @SuppressWarnings("unchecked")
    public Result<?> addUserMenus(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        List<Number> raw = (List<Number>) body.get("menuIds");
        if (raw == null || raw.isEmpty()) {
            return Result.fail("菜单ID不能为空");
        }
        List<Long> menuIds = raw.stream().map(Number::longValue).collect(Collectors.toList());
        permissionManageService.addUserMenus(userId, menuIds);
        return Result.ok();
    }

    @Operation(summary = "移除用户菜单权限")
    @PostMapping("/user/{userId}/remove")
    @SuppressWarnings("unchecked")
    public Result<?> removeUserMenus(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        List<Number> raw = (List<Number>) body.get("menuIds");
        if (raw == null || raw.isEmpty()) {
            return Result.fail("菜单ID不能为空");
        }
        List<Long> menuIds = raw.stream().map(Number::longValue).collect(Collectors.toList());
        permissionManageService.removeUserMenus(userId, menuIds);
        return Result.ok();
    }

    @Operation(summary = "设置用户菜单权限（替换模式：清空后写入勾选的，只保留选中的权限）")
    @PostMapping("/user/{userId}/set")
    @SuppressWarnings("unchecked")
    public Result<?> setUserMenus(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        List<Number> raw = (List<Number>) body.get("menuIds");
        List<Long> menuIds = (raw == null || raw.isEmpty())
                ? java.util.Collections.emptyList()
                : raw.stream().map(Number::longValue).collect(Collectors.toList());
        permissionManageService.setUserMenus(userId, menuIds);
        return Result.ok();
    }
}
