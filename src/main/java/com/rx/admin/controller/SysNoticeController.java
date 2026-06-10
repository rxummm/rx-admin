package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.SysNotice;
import com.rx.admin.entity.SysNoticeRead;
import com.rx.admin.entity.SysUser;
import com.rx.admin.mapper.SysNoticeReadMapper;
import com.rx.admin.service.SysNoticeService;
import com.rx.admin.service.SysUserService;
import com.rx.admin.service.SysMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/content/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService sysNoticeService;
    private final SysUserService sysUserService;
    private final SysMessageService sysMessageService;
    private final SysNoticeReadMapper noticeReadMapper;

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
    public Result<Void> add(@RequestBody @Valid SysNotice notice) {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        notice.setCreateBy(userId);
        notice.setCreateByName(user != null ? user.getNickname() : "");
        sysNoticeService.save(notice);

        // 发布通知/公告时，同步写入消息中心（广播给所有启用用户）
        if ("announcement".equals(notice.getCategory()) || "notice".equals(notice.getCategory())) {
            String type = "announcement".equals(notice.getCategory()) ? "notice" : "notice";
            sysMessageService.sendToAll(notice.getTitle(), notice.getContent(), type, notice.getLinkPath());
        }
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("content:notice:edit")
    @OperateLog(module = "通知公告", operation = "修改通知")
    public Result<Void> update(@RequestBody @Valid SysNotice notice) {
        sysNoticeService.updateById(notice);
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
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> ids = noticeReadMapper.selectList(
                new LambdaQueryWrapper<SysNoticeRead>().eq(SysNoticeRead::getUserId, userId)
        ).stream().map(SysNoticeRead::getNoticeId).collect(Collectors.toList());
        return Result.ok(ids);
    }

    /**
     * 标记单条通知/公告为已读（持久化到 sys_notice_read 表）
     */
    @PostMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 防重复插入：先查是否已有记录
        Long exists = noticeReadMapper.selectCount(
                new LambdaQueryWrapper<SysNoticeRead>()
                        .eq(SysNoticeRead::getNoticeId, id)
                        .eq(SysNoticeRead::getUserId, userId)
        );
        if (exists == 0) {
            SysNoticeRead read = new SysNoticeRead();
            read.setNoticeId(id);
            read.setUserId(userId);
            read.setReadTime(LocalDateTime.now());
            noticeReadMapper.insert(read);
        }
        return Result.ok();
    }

    /**
     * 标记所有通知/公告为已读
     */
    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        // 查当前所有通知/公告ID
        List<Long> allNoticeIds = sysNoticeService.list(
                new LambdaQueryWrapper<SysNotice>()
                        .in(SysNotice::getCategory, "notice", "announcement")
                        .eq(SysNotice::getStatus, 1)
        ).stream().map(SysNotice::getId).collect(Collectors.toList());

        if (!allNoticeIds.isEmpty()) {
            // 查已读ID
            List<Long> alreadyReadIds = noticeReadMapper.selectList(
                    new LambdaQueryWrapper<SysNoticeRead>()
                            .eq(SysNoticeRead::getUserId, userId)
            ).stream().map(SysNoticeRead::getNoticeId).collect(Collectors.toList());

            // 批量插入未读的
            for (Long noticeId : allNoticeIds) {
                if (!alreadyReadIds.contains(noticeId)) {
                    SysNoticeRead read = new SysNoticeRead();
                    read.setNoticeId(noticeId);
                    read.setUserId(userId);
                    read.setReadTime(LocalDateTime.now());
                    noticeReadMapper.insert(read);
                }
            }
        }
        return Result.ok();
    }
}
