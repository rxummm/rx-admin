package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.PageResult;
import com.rx.admin.common.Result;
import com.rx.admin.entity.SysMessage;
import com.rx.admin.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "站内消息")
@RestController
@RequestMapping("/api/content/message")
@RequiredArgsConstructor
public class SysMessageController {

    private final SysMessageService messageService;

    @GetMapping("/page")
    @Operation(summary = "消息分页查询（管理员可看全部，普通用户只看自己的）")
    public Result<PageResult<SysMessage>> page(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.pageQuery(page, size, currentUserId, messageType, userId));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数")
    public Result<Map<String, Long>> unreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(Map.of("count", messageService.getUnreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部已读")
    public Result<Void> markAllRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        messageService.markAllAsRead(userId);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息（普通用户只能删自己的，管理员可删任意）")
    public Result<Void> delete(@PathVariable Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        messageService.deleteMyMessage(id, currentUserId);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckRole("admin")
    @Operation(summary = "管理员修改消息")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        messageService.updateMessage(id,
                body.get("title"), body.get("content"), body.get("messageType"), body.get("linkPath"));
        return Result.ok();
    }
}
