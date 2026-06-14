package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysUser;
import com.rx.admin.modules.system.user.dto.UserCreateDTO;
import com.rx.admin.modules.system.user.dto.UserUpdateDTO;
import com.rx.admin.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/sys/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户列表(分页)")
    @GetMapping("/page")
    @SaCheckPermission("sys:user:query")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(userService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @SaCheckPermission("sys:user:add")
    @OperateLog(module = "用户管理", operation = "新增用户")
    public Result<?> add(@RequestBody @Valid UserCreateDTO dto) {
        userService.addUser(dto);
        return Result.ok();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    @SaCheckPermission("sys:user:edit")
    @OperateLog(module = "用户管理", operation = "修改用户")
    public Result<?> update(@RequestBody @Valid UserUpdateDTO dto) {
        userService.updateUser(dto);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:user:delete")
    @OperateLog(module = "用户管理", operation = "删除用户")
    public Result<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @SaCheckPermission("sys:user:query")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.ok(user);
    }
}