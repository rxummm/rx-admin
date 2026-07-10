package com.rx.admin.modules.monitor.loginlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.loginlog.mapper.SysLoginLogMapper;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@SuppressWarnings("null")
public class LoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements LoginLogService {

    @Override
    public PageResult<SysLoginLog> pageQuery(int page, int size, String username, Integer status,
                                              String startTime, String endTime) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysLoginLog::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(SysLoginLog::getLoginTime, LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(SysLoginLog::getLoginTime, LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        Page<SysLoginLog> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    @Override
    public void recordLogin(String username, String ip, String userAgent, String os, boolean success, String failReason) {
        String[] parsed = parseUserAgent(userAgent);
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setIp(ip);
        log.setBrowser(parsed[0]);
        log.setOs(parsed[1]);
        log.setStatus(success ? 1 : 0);
        log.setFailReason(failReason);
        log.setLoginTime(LocalDateTime.now());
        save(log);
    }

    /**
     * 从 User-Agent 字符串中提取浏览器名称和操作系统
     * @return [browser, os]，已截断到数据库列长度限制
     */
    private String[] parseUserAgent(String ua) {
        if (ua == null || ua.isEmpty()) {
            return new String[]{"Unknown", "Unknown"};
        }
        String browser = "Unknown";
        String os = "Unknown";

        // 解析浏览器
        if (ua.contains("Edg/")) {
            browser = ua.replaceAll(".*Edg/([\\d.]+).*", "Edge $1");
        } else if (ua.contains("Chrome/")) {
            browser = ua.replaceAll(".*Chrome/([\\d.]+).*", "Chrome $1");
        } else if (ua.contains("Firefox/")) {
            browser = ua.replaceAll(".*Firefox/([\\d.]+).*", "Firefox $1");
        } else if (ua.contains("Safari/") && !ua.contains("Chrome")) {
            browser = ua.replaceAll(".*Version/([\\d.]+).*Safari.*", "Safari $1");
        } else if (ua.contains("MSIE ") || ua.contains("Trident/")) {
            browser = "IE";
        }

        // 解析操作系统
        if (ua.contains("Windows NT 10.0")) {
            os = "Windows 10";
        } else if (ua.contains("Windows NT 6.3")) {
            os = "Windows 8.1";
        } else if (ua.contains("Windows NT 6.1")) {
            os = "Windows 7";
        } else if (ua.contains("Mac OS X")) {
            os = "macOS";
        } else if (ua.contains("Android")) {
            os = "Android";
        } else if (ua.contains("iPhone") || ua.contains("iPad")) {
            os = "iOS";
        } else if (ua.contains("Linux")) {
            os = "Linux";
        }

        // 截断到数据库列长度限制
        if (browser.length() > 100) browser = browser.substring(0, 100);
        if (os.length() > 50) os = os.substring(0, 50);

        return new String[]{browser, os};
    }
}
