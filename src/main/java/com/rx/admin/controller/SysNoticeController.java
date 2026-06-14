package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysNotice;
import com.rx.admin.modules.content.notice.dto.NoticeCreateDTO;
import com.rx.admin.modules.content.notice.dto.NoticeUpdateDTO;
import com.rx.admin.service.SysNoticeService;
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

    private final SysNoticeService sysNoticeService;

    /**
     * 分页查询通知（支持 category 筛选）
     */
    @GetMapping("/page")
    public Result<PageResult<SysNotice>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.ok(sysNoticeService.pageQuery(page, size, keyword, category));
    }

    /**
     * 获取待办事项数量（无需权限，登录即可查看自己的待办）
     */
    @GetMapping("/todo-count")
    public Result<Map<String, Object>> todoCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("todoCount", sysNoticeService.countByCategory("todo"));
        return Result.ok(result);
    }

    /**
     * 获取所有分类的通知数量概览（铃铛 badge 用）
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Map<String, Object> result = new HashMap<>();
        result.put("noticeCount", sysNoticeService.countByCategory("notice"));
        result.put("announcementCount", sysNoticeService.countByCategory("announcement"));
        result.put("todoCount", sysNoticeService.countByCategory("todo"));
        return Result.ok(result);
    }

    @PostMapping
    @SaCheckPermission("content:notice:add")
    @OperateLog(module = "通知公告", operation = "新增通知")
    public Result<Void> add(@RequestBody @Valid NoticeCreateDTO dto) {
        sysNoticeService.addNotice(dto);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("content:notice:edit")
    @OperateLog(module = "通知公告", operation = "修改通知")
    public Result<Void> update(@RequestBody @Valid NoticeUpdateDTO dto) {
        sysNoticeService.updateNotice(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("content:notice:delete")
    @OperateLog(module = "通知公告", operation = "删除通知")
    public Result<Void> delete(@PathVariable Long id) {
        sysNoticeService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<SysNotice> getById(@PathVariable Long id) {
        return Result.ok(sysNoticeService.getById(id));
    }

    /**
     * 获取当前用户已读的通知/公告ID列表（铃铛弹窗用，替代localStorage）
     */
    @GetMapping("/read-ids")
    public Result<List<Long>> getReadIds() {
        return Result.ok(sysNoticeService.getReadIds(StpUtil.getLoginIdAsLong()));
    }

    /**
     * 标记单条通知/公告为已读（持久化到 sys_notice_read 表）
     */
    @PostMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        sysNoticeService.markRead(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    /**
     * 标记所有通知/公告为已读
     */
    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        sysNoticeService.markAllRead(StpUtil.getLoginIdAsLong());
        return Result.ok();
    }
}
