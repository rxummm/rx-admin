package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysRole;
import com.rx.admin.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/sys/role")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "角色列表")
    @GetMapping("/list")
    @SaCheckPermission("sys:role:query")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.listAll());
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @SaCheckPermission("sys:role:add")
    @OperateLog(module = "角色管理", operation = "新增角色")
    public Result<?> add(@RequestBody @Valid SysRole role, @RequestParam(required = false) List<Long> menuIds) {
        roleService.addRole(role, menuIds);
        return Result.ok();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @SaCheckPermission("sys:role:edit")
    @OperateLog(module = "角色管理", operation = "修改角色")
    public Result<?> update(@RequestBody @Valid SysRole role, @RequestParam(required = false) List<Long> menuIds) {
        roleService.updateRole(role, menuIds);
        return Result.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:role:delete")
    @OperateLog(module = "角色管理", operation = "删除角色")
    public Result<?> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok();
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @SaCheckPermission("sys:role:query")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }
}
