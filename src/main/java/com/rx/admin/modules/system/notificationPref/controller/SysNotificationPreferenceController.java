package com.rx.admin.modules.system.notificationPref.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.system.notificationPref.dto.NotificationPrefUpdateDTO;
import com.rx.admin.modules.system.notificationPref.entity.SysNotificationPreference;
import com.rx.admin.modules.system.notificationPref.service.SysNotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知偏好")
@RestController
@ApiVersion(1)
@RequestMapping("/system/notification-pref")
@RequiredArgsConstructor
public class SysNotificationPreferenceController {

    private final SysNotificationPreferenceService service;

    @SaCheckLogin
    @GetMapping("/list")
    @Operation(summary = "获取我的通知偏好")
    public Result<List<SysNotificationPreference>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(service.getUserPreferences(userId));
    }

    @SaCheckLogin
    @PutMapping
    @Operation(summary = "更新通知偏好")
    @OperateLog(module = "通知偏好", operation = "更新")
    public Result<Void> update(@RequestBody @Valid NotificationPrefUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        service.saveOrUpdatePreference(userId, dto);
        return Result.ok();
    }
}
