package com.rx.admin.modules.content.comment.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.content.comment.entity.SysComment;
import com.rx.admin.modules.content.comment.service.SysCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@Tag(name = "评论管理")
@RestController
@ApiVersion(1)
@RequestMapping("/content/comment")
@RequiredArgsConstructor
public class SysCommentController {

    private final SysCommentService commentService;

    @Operation(summary = "分页查询评论")
    @GetMapping("/page")
    public Result<PageResult<SysComment>> page(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(commentService.pageQuery(targetType, targetId, page, size));
    }

    @Operation(summary = "添加评论")
    @PostMapping
    @OperateLog(module = "评论管理", operation = "添加评论")
    public Result<SysComment> add(@RequestBody SysComment comment) {
        // TODO: 从 Sa-Token 获取当前用户信息
        Long userId = 1L;
        String username = "admin";
        
        SysComment result = commentService.addComment(
            comment.getTargetType(),
            comment.getTargetId(),
            comment.getContent(),
            userId,
            username,
            comment.getParentId()
        );
        return Result.ok(result);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    @OperateLog(module = "评论管理", operation = "删除评论")
    public Result<Void> delete(@PathVariable Long id) {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        commentService.deleteComment(id, userId);
        return Result.ok();
    }

    @Operation(summary = "获取评论数量")
    @GetMapping("/count")
    public Result<Long> count(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return Result.ok(commentService.countComments(targetType, targetId));
    }
}
