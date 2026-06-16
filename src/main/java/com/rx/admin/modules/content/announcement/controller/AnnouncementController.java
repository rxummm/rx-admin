package com.rx.admin.modules.content.announcement.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.content.notice.entity.SysNotice;
import com.rx.admin.modules.content.notice.entity.SysNoticeRead;
import com.rx.admin.modules.content.notice.mapper.SysNoticeMapper;
import com.rx.admin.modules.content.notice.mapper.SysNoticeReadMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "系统公告弹窗")
@RestController
@ApiVersion(1)
@RequestMapping("/content/announcement")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AnnouncementController {

    private final SysNoticeMapper noticeMapper;
    private final SysNoticeReadMapper noticeReadMapper;

    @GetMapping("/popup")
    @Operation(summary = "获取需要弹窗的公告")
    public Result<List<Map<String, Object>>> getPopupAnnouncements() {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询未读的弹窗公告
        LambdaQueryWrapper<SysNotice> noticeW = new LambdaQueryWrapper<>();
        noticeW.eq(SysNotice::getNoticeType, "2") // 2=公告
                .eq(SysNotice::getStatus, 1)
                .orderByDesc(SysNotice::getCreateTime);
        List<SysNotice> notices = noticeMapper.selectList(noticeW);

        // 过滤已读的
        List<Long> readIds = noticeReadMapper.selectList(
                new LambdaQueryWrapper<SysNoticeRead>().eq(SysNoticeRead::getUserId, userId)
        ).stream().map(SysNoticeRead::getNoticeId).collect(Collectors.toList());

        return Result.ok(notices.stream()
                .filter(n -> !readIds.contains(n.getId()))
                .map(n -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", n.getId());
                    m.put("title", n.getTitle());
                    m.put("content", n.getContent());
                    m.put("createTime", n.getCreateTime());
                    return m;
                })
                .collect(Collectors.toList()));
    }

    @PostMapping("/read/{noticeId}")
    @Operation(summary = "标记公告已读")
    @SaCheckPermission("content:announcement:read")
    public Result<Void> markRead(@PathVariable Long noticeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysNoticeRead read = new SysNoticeRead();
        read.setNoticeId(noticeId);
        read.setUserId(userId);
        read.setReadTime(LocalDateTime.now());
        noticeReadMapper.insert(read);
        return Result.ok();
    }
}
