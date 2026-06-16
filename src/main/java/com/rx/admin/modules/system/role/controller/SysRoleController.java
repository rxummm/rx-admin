package com.rx.admin.modules.system.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.role.dto.RoleCreateDTO;
import com.rx.admin.modules.system.role.dto.RoleUpdateDTO;
import com.rx.admin.modules.system.role.service.SysRoleService;
import com.rx.admin.modules.system.role.convert.RoleConvert;
import com.rx.admin.modules.system.role.vo.RoleVO;
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
    private final RoleConvert roleConvert;

    public SysRoleController(SysRoleService roleService, RoleConvert roleConvert) {
        this.roleService = roleService;
        this.roleConvert = roleConvert;
    }

    @Operation(summary = "角色列表")
    @GetMapping("/list")
    @SaCheckPermission("sys:role:query")
    public Result<List<RoleVO>> list() {
        return Result.ok(roleConvert.toVOList(roleService.listAll()));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @SaCheckPermission("sys:role:add")
    @OperateLog(module = "角色管理", operation = "新增角色")
    public Result<?> add(@RequestBody @Valid RoleCreateDTO dto) {
        roleService.addRole(dto);
        return Result.ok();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @SaCheckPermission("sys:role:edit")
    @OperateLog(module = "角色管理", operation = "修改角色")
    public Result<?> update(@RequestBody @Valid RoleUpdateDTO dto) {
        roleService.updateRole(dto);
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
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.ok(roleConvert.toVO(roleService.getById(id)));
    }
}
