package com.rx.admin.modules.system.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.role.dto.RoleCreateDTO;
import com.rx.admin.modules.system.role.dto.RoleUpdateDTO;
import com.rx.admin.modules.system.role.service.ISysRoleService;
import com.rx.admin.modules.system.role.convert.RoleConvert;
import com.rx.admin.modules.system.role.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService roleService;
    private final RoleConvert roleConvert;

    @Operation(summary = "角色列表")
    @GetMapping("/list")
    @SaCheckPermission(PermissionConstants.Role.QUERY)
    public Result<List<RoleVO>> list() {
        return Result.ok(roleConvert.toVOList(roleService.listAll()));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @SaCheckPermission(PermissionConstants.Role.ADD)
    @OperateLog(module = "角色管理", operation = "新增角色")
    public Result<?> add(@RequestBody @Valid RoleCreateDTO dto) {
        roleService.addRole(dto);
        return Result.ok();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @SaCheckPermission(PermissionConstants.Role.EDIT)
    @OperateLog(module = "角色管理", operation = "修改角色")
    public Result<?> update(@RequestBody @Valid RoleUpdateDTO dto) {
        roleService.updateRole(dto);
        return Result.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Role.DELETE)
    @OperateLog(module = "角色管理", operation = "删除角色")
    public Result<?> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除角色")
    @DeleteMapping("/batch")
    @SaCheckPermission(PermissionConstants.Role.DELETE)
    @OperateLog(module = "角色管理", operation = "批量删除角色")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        roleService.deleteRoleBatch(ids);
        return Result.ok();
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Role.QUERY)
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.ok(roleConvert.toVO(roleService.getById(id)));
    }
}
