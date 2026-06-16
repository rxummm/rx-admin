package com.rx.admin.modules.system.menu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.menu.dto.MenuCreateDTO;
import com.rx.admin.modules.system.menu.dto.MenuUpdateDTO;
import com.rx.admin.modules.system.menu.service.ISysMenuService;
import com.rx.admin.modules.system.menu.convert.MenuConvert;
import com.rx.admin.modules.system.menu.vo.MenuVO;
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

    private final ISysMenuService menuService;
    private final MenuConvert menuConvert;

    public SysMenuController(ISysMenuService menuService, MenuConvert menuConvert) {
        this.menuService = menuService;
        this.menuConvert = menuConvert;
    }

    @Operation(summary = "菜单树列表")
    @GetMapping("/tree")
    @SaCheckPermission(PermissionConstants.Menu.QUERY)
    public Result<List<MenuVO>> tree() {
        return Result.ok(menuConvert.toVOList(menuService.getAllMenuTree()));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @SaCheckPermission(PermissionConstants.Menu.ADD)
    @OperateLog(module = "菜单管理", operation = "新增菜单")
    @CacheEvict(value = "menu", allEntries = true)
    public Result<?> add(@RequestBody @Valid MenuCreateDTO dto) {
        menuService.addMenu(dto);
        return Result.ok();
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    @SaCheckPermission(PermissionConstants.Menu.EDIT)
    @OperateLog(module = "菜单管理", operation = "修改菜单")
    @CacheEvict(value = "menu", allEntries = true)
    public Result<?> update(@RequestBody @Valid MenuUpdateDTO dto) {
        menuService.updateMenu(dto);
        return Result.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Menu.DELETE)
    @OperateLog(module = "菜单管理", operation = "删除菜单")
    @CacheEvict(value = "menu", allEntries = true)
    public Result<?> delete(@PathVariable Long id) {
        menuService.removeMenu(id);
        return Result.ok();
    }

    @Operation(summary = "获取可申请的菜单树（排除管理类菜单和已有菜单）")
    @GetMapping("/requestable")
    public Result<List<MenuVO>> requestable() {
        return Result.ok(menuConvert.toVOList(menuService.getRequestableMenus()));
    }
}
