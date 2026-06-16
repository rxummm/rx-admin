package com.rx.admin.modules.content.message.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.message.entity.SysMessage;
import com.rx.admin.modules.system.role.entity.SysRole;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.content.message.mapper.SysMessageMapper;
import com.rx.admin.modules.system.role.mapper.SysRoleMapper;
import com.rx.admin.modules.system.user.mapper.SysUserMapper;
import com.rx.admin.modules.system.user.mapper.SysUserRoleMapper;
import com.rx.admin.modules.monitor.notification.service.SseSessionManager;
import com.rx.admin.modules.monitor.notification.event.DashboardChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("null")
public class SysMessageService extends ServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SseSessionManager sseSessionManager;

    private final ApplicationEventPublisher eventPublisher;

    public SysMessageService(SysUserMapper sysUserMapper,
                             SysUserRoleMapper userRoleMapper,
                             SysRoleMapper sysRoleMapper,
                             SseSessionManager sseSessionManager,
                             ApplicationEventPublisher eventPublisher) {
        this.sysUserMapper = sysUserMapper;
        this.userRoleMapper = userRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sseSessionManager = sseSessionManager;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 分页查询消息。管理员可看全部或按指定用户筛选，普通用户只能看自己的消息。
     * @param targetUserId 管理员可按接收人筛选（可选），普通用户忽略此参数
     */
    public PageResult<SysMessage> pageQuery(int page, int size, Long currentUserId, String messageType, Long targetUserId) {
        LambdaQueryWrapper<SysMessage> w = new LambdaQueryWrapper<>();
        if (StpUtil.hasRole("admin")) {
            if (targetUserId != null) {
                w.eq(SysMessage::getReceiverId, targetUserId);
            }
        } else {
            w.eq(SysMessage::getReceiverId, currentUserId);
        }
        if (messageType != null && !messageType.isBlank())
            w.eq(SysMessage::getMessageType, messageType);
        w.orderByDesc(SysMessage::getCreateTime);
        IPage<SysMessage> p = page(new Page<>(page, size), w);
        List<SysMessage> records = p.getRecords();
        // 填充接收人用户名（供前端管理员视角替换"您"为具体用户名）
        fillReceiverUsernames(records);
        return PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), records);
    }

    /**
     * 批量填充消息记录的 receiverUsername
     */
    private void fillReceiverUsernames(List<SysMessage> records) {
        if (records == null || records.isEmpty()) return;
        Set<Long> userIds = records.stream()
                .map(SysMessage::getReceiverId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, userIds).select(SysUser::getId, SysUser::getUsername));
        Map<Long, String> usernameMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername, (a, b) -> a));
        for (SysMessage msg : records) {
            if (msg.getReceiverId() != null) {
                msg.setReceiverUsername(usernameMap.getOrDefault(msg.getReceiverId(), ""));
            }
        }
    }

    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SysMessage> w = new LambdaQueryWrapper<>();
        w.eq(SysMessage::getReceiverId, userId).eq(SysMessage::getIsRead, 0);
        return count(w);
    }

    public void markAsRead(Long id) {
        SysMessage msg = new SysMessage();
        msg.setId(id);
        msg.setIsRead(1);
        msg.setReadTime(LocalDateTime.now());
        updateById(msg);
    }

    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<SysMessage> w = new LambdaQueryWrapper<>();
        w.eq(SysMessage::getReceiverId, userId).eq(SysMessage::getIsRead, 0);
        SysMessage update = new SysMessage();
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());
        update(update, w);
    }

    public void sendSystemMessage(String title, String content, Long receiverId) {
        sendMessage(title, content, receiverId, "system", null);
    }

    public void sendNotificationMessage(String title, String content, Long receiverId, String linkPath) {
        sendMessage(title, content, receiverId, "notice", linkPath);
    }

    public void sendInfoMessage(String title, String content, Long receiverId) {
        sendMessage(title, content, receiverId, "info", null);
    }

    private void sendMessage(String title, String content, Long receiverId, String messageType, String linkPath) {
        SysMessage msg = new SysMessage();
        msg.setSenderId(0L);
        msg.setReceiverId(receiverId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMessageType(messageType);
        msg.setLinkPath(linkPath);
        msg.setIsRead(0);
        boolean ok = save(msg);
        log.info("消息已发送: id={}, title={}, receiverId={}, messageType={}, saved={}",
                msg.getId(), title, receiverId, messageType, ok);
        // SSE 推送消息更新给接收用户
        if (receiverId != null) {
            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("title", title);
                payload.put("messageType", messageType);
                payload.put("unreadCount", getUnreadCount(receiverId));
                sseSessionManager.sendToUser(receiverId, "new_message", payload);
            } catch (Exception e) {
                log.warn("SSE 消息推送失败: userId={}, {}", receiverId, e.getMessage());
            }
        }
        // 触发仪表盘统计立即刷新
        eventPublisher.publishEvent(new DashboardChangeEvent(DashboardChangeEvent.SECTION_ALL));
    }

    /**
     * 广播消息给所有用户（用于通知公告发布等场景）
     */
    public void sendToAll(String title, String content, String messageType, String linkPath) {
        List<SysUser> allUsers = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1));
        if (allUsers.isEmpty()) return;
        List<SysMessage> messages = new ArrayList<>(allUsers.size());
        for (SysUser user : allUsers) {
            SysMessage msg = new SysMessage();
            msg.setSenderId(0L);
            msg.setReceiverId(user.getId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setMessageType(messageType);
            msg.setLinkPath(linkPath);
            msg.setIsRead(0);
            messages.add(msg);
        }
        saveBatch(messages, 500);
    }

    /**
     * 发送消息给具有指定角色编码的所有用户（用于通知管理员等场景，如定时任务失败）
     */
    public void sendToRoleUsers(String roleCode, String title, String content) {
        sendToRoleUsers(roleCode, title, content, null);
    }

    /**
     * 发送消息给具有指定角色编码的所有用户，可排除指定用户（避免自己收到自己的操作通知）
     */
    public void sendToRoleUsers(String roleCode, String title, String content, Long excludeUserId) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode));
        if (role == null) return;
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(role.getId());
        if (userIds == null || userIds.isEmpty()) return;
        for (Long userId : userIds) {
            if (excludeUserId != null && excludeUserId.equals(userId)) continue;
            sendSystemMessage(title, content, userId);
        }
    }

    /**
     * 删除消息。普通用户只能删自己的，管理员可删任意。
     */
    public void deleteMyMessage(Long msgId, Long currentUserId) {
        SysMessage msg = getById(msgId);
        if (msg == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        if (!StpUtil.hasRole("admin") && !msg.getReceiverId().equals(currentUserId)) {
            throw new IllegalArgumentException("无权删除该消息");
        }
        removeById(msgId);
    }

    /**
     * 管理员修改消息
     */
    public void updateMessage(Long id, String title, String content, String messageType, String linkPath) {
        SysMessage msg = getById(id);
        if (msg == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        SysMessage update = new SysMessage();
        update.setId(id);
        if (title != null) update.setTitle(title);
        if (content != null) update.setContent(content);
        if (messageType != null) update.setMessageType(messageType);
        if (linkPath != null) update.setLinkPath(linkPath);
        updateById(update);
    }
}
