package com.rx.admin.modules.content.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.message.entity.SysMessage;

import java.util.List;

public interface ISysMessageService extends IService<SysMessage> {

    PageResult<SysMessage> pageQuery(int page, int size, Long currentUserId, String messageType, Long targetUserId);

    long getUnreadCount(Long userId);

    void markAsRead(Long id);

    void markAllAsRead(Long userId);

    void sendSystemMessage(String title, String content, Long receiverId);

    void sendNotificationMessage(String title, String content, Long receiverId, String linkPath);

    void sendInfoMessage(String title, String content, Long receiverId);

    void sendToAll(String title, String content, String messageType, String linkPath);

    void sendToRoleUsers(String roleCode, String title, String content);

    void sendToRoleUsers(String roleCode, String title, String content, Long excludeUserId);

    void deleteMyMessage(Long msgId, Long currentUserId);

    void updateMessage(Long id, String title, String content, String messageType, String linkPath);

    void deleteMessageBatch(List<Long> ids);
}