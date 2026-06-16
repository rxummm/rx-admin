package com.rx.admin.modules.content.notice.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.notice.entity.SysNotice;
import com.rx.admin.modules.content.notice.entity.SysNoticeRead;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.user.service.SysUserService;
import com.rx.admin.modules.content.message.service.SysMessageService;
import com.rx.admin.modules.content.notice.mapper.SysNoticeMapper;
import com.rx.admin.modules.content.notice.mapper.SysNoticeReadMapper;
import com.rx.admin.modules.content.notice.dto.NoticeCreateDTO;
import com.rx.admin.modules.content.notice.dto.NoticeUpdateDTO;
import com.rx.admin.modules.monitor.notification.service.SseSessionManager;
import com.rx.admin.modules.monitor.notification.event.DashboardChangeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SysNoticeService extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    private final SysUserService sysUserService;
    private final SysMessageService sysMessageService;
    private final SysNoticeReadMapper noticeReadMapper;
    private final SseSessionManager sseSessionManager;
    private final ApplicationEventPublisher eventPublisher;

    public PageResult<SysNotice> pageQuery(int page, int size, String keyword) {
        return pageQuery(page, size, keyword, null);
    }

    public PageResult<SysNotice> pageQuery(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>()
                .eq(category != null, SysNotice::getCategory, category)
                .like(StringUtils.hasText(keyword), SysNotice::getTitle, keyword)
                .orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    /**
     * 新增通知/公告
     */
    public void addNotice(NoticeCreateDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);

        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setNoticeType(dto.getNoticeType());
        notice.setCategory(dto.getCategory());
        notice.setLinkPath(dto.getLinkPath());
        notice.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        notice.setCreateBy(userId);
        notice.setCreateByName(user != null ? user.getNickname() : "");

        save(notice);

        // 发布通知/公告时，同步写入消息中心（广播给所有启用用户）
        if ("announcement".equals(notice.getCategory()) || "notice".equals(notice.getCategory())) {
            String type = "announcement".equals(notice.getCategory()) ? "notice" : "notice";
            sysMessageService.sendToAll(notice.getTitle(), notice.getContent(), type, notice.getLinkPath());
        }

        // SSE 推送通知更新给所有在线用户（必须用 Map 确保 JSON 序列化）
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", notice.getTitle());
        payload.put("id", notice.getId());
        payload.put("noticeType", notice.getNoticeType());
        payload.put("category", notice.getCategory());
        sseSessionManager.broadcast("new_notice", payload);
        // 触发仪表盘统计立即刷新
        eventPublisher.publishEvent(new DashboardChangeEvent(DashboardChangeEvent.SECTION_ALL));
    }

    /**
     * 修改通知/公告
     */
    public void updateNotice(NoticeUpdateDTO dto) {
        SysNotice notice = getById(dto.getId());
        if (notice == null) {
            throw new IllegalArgumentException("通知不存在");
        }
        if (StringUtils.hasText(dto.getTitle())) notice.setTitle(dto.getTitle());
        if (StringUtils.hasText(dto.getContent())) notice.setContent(dto.getContent());
        if (StringUtils.hasText(dto.getNoticeType())) notice.setNoticeType(dto.getNoticeType());
        if (StringUtils.hasText(dto.getCategory())) notice.setCategory(dto.getCategory());
        if (StringUtils.hasText(dto.getLinkPath())) notice.setLinkPath(dto.getLinkPath());
        if (dto.getStatus() != null) notice.setStatus(dto.getStatus());

        updateById(notice);
    }

    /**
     * 获取各分类的未读数量
     */
    public long countByCategory(String category) {
        return count(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getCategory, category)
                .eq(SysNotice::getStatus, 1));
    }

    /**
     * 获取当前用户已读的通知/公告ID列表
     */
    public List<Long> getReadIds(Long userId) {
        return noticeReadMapper.selectList(
                new LambdaQueryWrapper<SysNoticeRead>().eq(SysNoticeRead::getUserId, userId)
        ).stream().map(SysNoticeRead::getNoticeId).collect(Collectors.toList());
    }

    /**
     * 标记单条通知/公告为已读
     */
    public void markRead(Long userId, Long noticeId) {
        Long exists = noticeReadMapper.selectCount(
                new LambdaQueryWrapper<SysNoticeRead>()
                        .eq(SysNoticeRead::getNoticeId, noticeId)
                        .eq(SysNoticeRead::getUserId, userId)
        );
        if (exists == 0) {
            SysNoticeRead read = new SysNoticeRead();
            read.setNoticeId(noticeId);
            read.setUserId(userId);
            read.setReadTime(LocalDateTime.now());
            noticeReadMapper.insert(read);
        }
    }

    /**
     * 标记所有通知/公告为已读
     */
    public void markAllRead(Long userId) {
        // 查当前所有通知/公告ID
        List<Long> allNoticeIds = list(
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
    }
}