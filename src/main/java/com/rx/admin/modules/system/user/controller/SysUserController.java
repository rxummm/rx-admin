package com.rx.admin.modules.system.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.user.dto.UserCreateDTO;
import com.rx.admin.modules.system.user.dto.UserUpdateDTO;
import com.rx.admin.modules.system.user.service.ISysUserService;
import com.rx.admin.modules.system.user.convert.UserConvert;
import com.rx.admin.modules.system.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/user")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SysUserController {

    private final ISysUserService userService;
    private final UserConvert userConvert;

    @Operation(summary = "用户列表(分页)")
    @GetMapping("/page")
    @SaCheckPermission(PermissionConstants.User.QUERY)
    public Result<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<SysUser> pr = userService.pageQuery(page, size, keyword);
        return Result.ok(userConvert.toPageResult(pr));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @SaCheckPermission(PermissionConstants.User.ADD)
    @OperateLog(module = "用户管理", operation = "新增用户")
    public Result<?> add(@RequestBody @Valid UserCreateDTO dto) {
        userService.addUser(dto);
        return Result.ok();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    @SaCheckPermission(PermissionConstants.User.EDIT)
    @OperateLog(module = "用户管理", operation = "修改用户")
    public Result<?> update(@RequestBody @Valid UserUpdateDTO dto) {
        userService.updateUser(dto);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.User.DELETE)
    @OperateLog(module = "用户管理", operation = "删除用户")
    public Result<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @SaCheckPermission(PermissionConstants.User.QUERY)
    public Result<UserVO> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) return Result.fail("用户不存在");
        return Result.ok(userConvert.toVO(user));
    }
}
