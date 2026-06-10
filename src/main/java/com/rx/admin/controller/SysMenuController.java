package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysMenu;
import com.rx.admin.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/sys/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    public SysMenuController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "菜单树列表")
    @GetMapping("/tree")
    @SaCheckPermission("sys:menu:query")
    public Result<List<SysMenu>> tree() {
        return Result.ok(menuService.getAllMenuTree());
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @SaCheckPermission("sys:menu:add")
    @OperateLog(module = "菜单管理", operation = "新增菜单")
    @CacheEvict(value = "menu", allEntries = true)  // 菜单变更清除全部 menu 缓存
    public Result<?> add(@RequestBody @Valid SysMenu menu) {
        menuService.save(menu);
        return Result.ok();
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    @SaCheckPermission("sys:menu:edit")
    @OperateLog(module = "菜单管理", operation = "修改菜单")
    @CacheEvict(value = "menu", allEntries = true)
    public Result<?> update(@RequestBody @Valid SysMenu menu) {
        menuService.updateById(menu);
        return Result.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:menu:delete")
    @OperateLog(module = "菜单管理", operation = "删除菜单")
    @CacheEvict(value = "menu", allEntries = true)
    public Result<?> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取可申请的菜单树（排除管理类菜单和已有菜单）")
    @GetMapping("/requestable")
    public Result<List<SysMenu>> requestable() {
        return Result.ok(menuService.getRequestableMenus());
    }
}
