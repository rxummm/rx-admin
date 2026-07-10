package com.rx.admin.modules.content.notification.service;

import com.rx.admin.modules.content.message.service.SysMessageService;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @提及通知服务
 * 解析文本中的@提及并发送通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MentionService {

    private final SysMessageService sysMessageService;
    private final SysUserMapper sysUserMapper;

    // @提及的正则表达式：@用户名
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    /**
     * 解析文本中的@提及
     */
    public List<String> parseMentions(String text) {
        List<String> mentions = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return mentions;
        }

        Matcher matcher = MENTION_PATTERN.matcher(text);
        while (matcher.find()) {
            String username = matcher.group(1);
            if (!mentions.contains(username)) {
                mentions.add(username);
            }
        }
        return mentions;
    }

    /**
     * 发送@提及通知
     */
    public void sendMentionNotifications(String text, Long senderId, String senderName, String contentType, Long contentId) {
        List<String> mentions = parseMentions(text);
        
        for (String username : mentions) {
            try {
                // 查找用户
                SysUser user = sysUserMapper.selectByUsername(username);
                if (user == null) {
                    log.warn("提及的用户不存在: {}", username);
                    continue;
                }
                
                // 跳过提及自己
                if (user.getId().equals(senderId)) {
                    continue;
                }
                
                // 发送通知
                String title = "你被 @" + username + " 提及";
                String content = String.format("%s 在%s中提到了你", senderName, getContentTypeName(contentType));
                
                sysMessageService.sendSystemMessage(title, content, user.getId());
                log.info("发送@提及通知: sender={}, mentioned={}", senderName, username);
                
            } catch (Exception e) {
                log.warn("发送@提及通知失败: username={}, error={}", username, e.getMessage());
            }
        }
    }

    /**
     * 获取内容类型名称
     */
    private String getContentTypeName(String contentType) {
        switch (contentType) {
            case "comment": return "评论";
            case "notice": return "通知";
            case "message": return "消息";
            default: return "内容";
        }
    }
}
