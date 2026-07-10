package com.rx.admin.modules.system.user.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.user.entity.SysUserTag;
import com.rx.admin.modules.system.user.service.UserTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户标签管理控制器
 */
@Tag(name = "用户标签管理")
@RestController
@ApiVersion(1)
@RequestMapping("/sys/user-tag")
@RequiredArgsConstructor
public class UserTagController {

    private final UserTagService userTagService;

    @Operation(summary = "获取所有标签")
    @GetMapping("/list")
    public Result<List<SysUserTag>> listAll() {
        return Result.ok(userTagService.listAll());
    }

    @Operation(summary = "创建标签")
    @PostMapping
    @OperateLog(module = "用户标签管理", operation = "创建标签")
    public Result<SysUserTag> create(@RequestBody SysUserTag tag) {
        return Result.ok(userTagService.createTag(tag));
    }

    @Operation(summary = "更新标签")
    @PutMapping
    @OperateLog(module = "用户标签管理", operation = "更新标签")
    public Result<Void> update(@RequestBody SysUserTag tag) {
        userTagService.updateTag(tag);
        return Result.ok();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    @OperateLog(module = "用户标签管理", operation = "删除标签")
    public Result<Void> delete(@PathVariable Long id) {
        userTagService.deleteTag(id);
        return Result.ok();
    }

    @Operation(summary = "给用户打标签")
    @PostMapping("/user/{userId}/tag/{tagId}")
    @OperateLog(module = "用户标签管理", operation = "给用户打标签")
    public Result<Void> addUserTag(@PathVariable Long userId, @PathVariable Long tagId) {
        userTagService.addUserTag(userId, tagId);
        return Result.ok();
    }

    @Operation(summary = "移除用户标签")
    @DeleteMapping("/user/{userId}/tag/{tagId}")
    @OperateLog(module = "用户标签管理", operation = "移除用户标签")
    public Result<Void> removeUserTag(@PathVariable Long userId, @PathVariable Long tagId) {
        userTagService.removeUserTag(userId, tagId);
        return Result.ok();
    }

    @Operation(summary = "获取用户的标签列表")
    @GetMapping("/user/{userId}")
    public Result<List<SysUserTag>> getUserTags(@PathVariable Long userId) {
        return Result.ok(userTagService.getUserTags(userId));
    }

    @Operation(summary = "按标签查询用户ID列表")
    @GetMapping("/tag/{tagId}/users")
    public Result<List<Long>> getUserIdsByTag(@PathVariable Long tagId) {
        return Result.ok(userTagService.getUserIdsByTag(tagId));
    }
}
