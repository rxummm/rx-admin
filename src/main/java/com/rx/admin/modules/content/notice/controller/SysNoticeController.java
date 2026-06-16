package com.rx.admin.modules.content.notice.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.content.notice.entity.SysNotice;
import com.rx.admin.modules.content.notice.dto.NoticeCreateDTO;
import com.rx.admin.modules.content.notice.dto.NoticeUpdateDTO;
import com.rx.admin.modules.content.notice.service.ISysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "通知公告管理")
@RestController
@RequestMapping("/api/content/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final ISysNoticeService sysNoticeService;

    @Operation(summary = "分页查询通知公告")
    @GetMapping("/page")
    public Result<PageResult<SysNotice>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.ok(sysNoticeService.pageQuery(page, size, keyword, category));
    }

    @Operation(summary = "获取待办事项数量")
    @GetMapping("/todo-count")
    public Result<Map<String, Object>> todoCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("todoCount", sysNoticeService.countByCategory("todo"));
        return Result.ok(result);
    }

    @Operation(summary = "获取通知数量概览")
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Map<String, Object> result = new HashMap<>();
        result.put("noticeCount", sysNoticeService.countByCategory("notice"));
        result.put("announcementCount", sysNoticeService.countByCategory("announcement"));
        result.put("todoCount", sysNoticeService.countByCategory("todo"));
        return Result.ok(result);
    }

    @Operation(summary = "新增通知")
    @PostMapping
    @SaCheckPermission(PermissionConstants.Content.NOTICE_ADD)
    @OperateLog(module = "通知公告", operation = "新增通知")
    public Result<Void> add(@RequestBody @Valid NoticeCreateDTO dto) {
        sysNoticeService.addNotice(dto);
        return Result.ok();
    }

    @Operation(summary = "修改通知")
    @PutMapping
    @SaCheckPermission(PermissionConstants.Content.NOTICE_EDIT)
    @OperateLog(module = "通知公告", operation = "修改通知")
    public Result<Void> update(@RequestBody @Valid NoticeUpdateDTO dto) {
        sysNoticeService.updateNotice(dto);
        return Result.ok();
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionConstants.Content.NOTICE_DELETE)
    @OperateLog(module = "通知公告", operation = "删除通知")
    public Result<Void> delete(@PathVariable Long id) {
        sysNoticeService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "根据ID查询通知")
    @GetMapping("/{id}")
    public Result<SysNotice> getById(@PathVariable Long id) {
        return Result.ok(sysNoticeService.getById(id));
    }

    @Operation(summary = "获取已读通知ID列表")
    @GetMapping("/read-ids")
    public Result<List<Long>> getReadIds() {
        return Result.ok(sysNoticeService.getReadIds(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "标记通知为已读")
    @PostMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        sysNoticeService.markRead(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    @Operation(summary = "标记所有通知为已读")
    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        sysNoticeService.markAllRead(StpUtil.getLoginIdAsLong());
        return Result.ok();
    }
}
