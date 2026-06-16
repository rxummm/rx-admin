package com.rx.admin.modules.system.permission.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.menu.convert.MenuConvert;
import com.rx.admin.modules.system.menu.vo.MenuVO;
import com.rx.admin.modules.system.permission.dto.UserMenuDTO;
import com.rx.admin.modules.system.permission.service.SysPermissionManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/sys/permission-manage")
@SaCheckRole("admin")
public class SysPermissionManageController {

    private final SysPermissionManageService permissionManageService;
    private final MenuConvert menuConvert;

    public SysPermissionManageController(SysPermissionManageService permissionManageService, MenuConvert menuConvert) {
        this.permissionManageService = permissionManageService;
        this.menuConvert = menuConvert;
    }

    @Operation(summary = "获取用户已有菜单权限ID列表")
    @GetMapping("/user/{userId}/menus")
    public Result<Set<Long>> getUserMenuIds(@PathVariable Long userId) {
        return Result.ok(permissionManageService.getUserMenuIds(userId));
    }

    @Operation(summary = "获取可管理的菜单树（排除管理类菜单和已拥有菜单）")
    @GetMapping("/user/{userId}/manageable-tree")
    public Result<List<MenuVO>> getManageableMenuTree(@PathVariable Long userId) {
        return Result.ok(menuConvert.toVOList(permissionManageService.getManageableMenuTree(userId)));
    }

    @Operation(summary = "给用户添加菜单权限")
    @PostMapping("/user/{userId}/add")
    public Result<?> addUserMenus(@PathVariable Long userId, @RequestBody UserMenuDTO dto) {
        List<Long> menuIds = dto.getMenuIds();
        if (menuIds == null || menuIds.isEmpty()) {
            return Result.fail("菜单ID不能为空");
        }
        permissionManageService.addUserMenus(userId, menuIds);
        return Result.ok();
    }

    @Operation(summary = "移除用户菜单权限")
    @PostMapping("/user/{userId}/remove")
    public Result<?> removeUserMenus(@PathVariable Long userId, @RequestBody UserMenuDTO dto) {
        List<Long> menuIds = dto.getMenuIds();
        if (menuIds == null || menuIds.isEmpty()) {
            return Result.fail("菜单ID不能为空");
        }
        permissionManageService.removeUserMenus(userId, menuIds);
        return Result.ok();
    }

    @Operation(summary = "设置用户菜单权限（替换模式：清空后写入勾选的，只保留选中的权限）")
    @PostMapping("/user/{userId}/set")
    public Result<?> setUserMenus(@PathVariable Long userId, @RequestBody UserMenuDTO dto) {
        List<Long> menuIds = dto.getMenuIds() != null ? dto.getMenuIds() : java.util.Collections.emptyList();
        permissionManageService.setUserMenus(userId, menuIds);
        return Result.ok();
    }
}
