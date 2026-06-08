package com.rx.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户追踪服务（自动清理过期Token）
 * - 主动登录/退出时通过 userLoggedIn/userLoggedOut 同步
 * - 查询列表时自动校验 Sa-Token 有效性，清理因浏览器关闭等未退出导致的过期记录
 */
@Slf4j
@Service
public class OnlineUserService {

    /** tokenValue -> 用户会话信息 */
    private final ConcurrentHashMap<String, Map<String, Object>> onlineMap = new ConcurrentHashMap<>();
    private final SysUserService sysUserService;

    public OnlineUserService(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /** 用户登录时调用 */
    public void userLoggedIn(String tokenValue, Long userId) {
        // 先移除该用户的旧记录，避免重复（同一用户只保留最新一条记录）
        onlineMap.values().removeIf(v -> String.valueOf(userId).equals(v.get("loginId")));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("tokenId", tokenValue);
        info.put("loginId", String.valueOf(userId));
        info.put("loginTime", sdf.format(new Date()));

        try {
            SysUser user = sysUserService.getById(userId);
            if (user != null) {
                info.put("username", user.getUsername());
                info.put("nickname", user.getNickname() != null ? user.getNickname() : "");
            } else {
                info.put("username", "未知用户(" + userId + ")");
                info.put("nickname", "");
            }
        } catch (Exception e) {
            info.put("username", "未知用户(" + userId + ")");
            info.put("nickname", "");
        }

        onlineMap.put(tokenValue, info);
        log.debug("用户上线: userId={}, token={}", userId, tokenValue.substring(0, Math.min(8, tokenValue.length())) + "...");
    }

    /** 用户退出时调用（支持通过 loginId 批量踢出） */
    public void userLoggedOut(String tokenValue) {
        Map<String, Object> removed = onlineMap.remove(tokenValue);
        if (removed != null) {
            log.debug("用户下线: loginId={}", removed.get("loginId"));
        }
    }

    /** 根据 loginId 踢出所有会话 */
    public void kickOutByUserId(Long userId) {
        String uid = String.valueOf(userId);
        onlineMap.values().removeIf(v -> uid.equals(v.get("loginId")));
    }

    /** 获取所有在线用户（自动清理过期Token） */
    public List<Map<String, Object>> getOnlineUsers() {
        cleanupStaleEntries();
        return new ArrayList<>(onlineMap.values());
    }

    /** 获取在线用户数（自动清理过期Token） */
    public int getOnlineCount() {
        cleanupStaleEntries();
        return onlineMap.size();
    }

    /**
     * 遍历在线用户列表，移除 Sa-Token 中已失效（过期/退出）的 Token 记录。
     * 注意：StpUtil.getLoginIdByToken() 有时在后台定时任务线程中可能返回 null，
     * 因此只移除明确过期的，不因异常而误删。
     */
    private void cleanupStaleEntries() {
        onlineMap.keySet().removeIf(tokenValue -> {
            try {
                Object loginId = StpUtil.getLoginIdByToken(tokenValue);
                return loginId == null;
            } catch (Exception e) {
                // 不因异常误删在线记录；仅日志记录
                log.warn("清理过期Token时查询异常，保留记录: token={}, error={}",
                    tokenValue.substring(0, Math.min(8, tokenValue.length())),
                    e.getMessage());
                return false;
            }
        });
    }
}