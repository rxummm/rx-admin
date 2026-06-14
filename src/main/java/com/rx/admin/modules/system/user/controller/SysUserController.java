package com.rx.admin.modules.system.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.user.dto.UserCreateDTO;
import com.rx.admin.modules.system.user.dto.UserUpdateDTO;
import com.rx.admin.modules.system.user.service.SysUserService;
import com.rx.admin.modules.system.user.convert.UserConvert;
import com.rx.admin.modules.system.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/sys/user")
public class SysUserController {

    private final SysUserService userService;
    private final UserConvert userConvert;

    public SysUserController(SysUserService userService, UserConvert userConvert) {
        this.userService = userService;
        this.userConvert = userConvert;
    }

    @Operation(summary = "用户列表(分页)")
    @GetMapping("/page")
    @SaCheckPermission("sys:user:query")
    public Result<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<SysUser> pr = userService.pageQuery(page, size, keyword);
        List<UserVO> voList = pr.getRecords().stream().map(userConvert::toVO).toList();
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), voList));
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
    public Result<UserVO> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) return Result.fail("用户不存在");
        return Result.ok(userConvert.toVO(user));
    }
}
