package com.rx.admin.modules.calendar.controller;

import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.calendar.entity.SysTeamSchedule;
import com.rx.admin.modules.calendar.service.TeamScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 团队日程控制器
 */
@Tag(name = "团队日程")
@RestController
@ApiVersion(1)
@RequestMapping("/calendar/team-schedule")
@RequiredArgsConstructor
public class TeamScheduleController {

    private final TeamScheduleService scheduleService;

    @Operation(summary = "获取用户日程")
    @GetMapping
    public Result<List<SysTeamSchedule>> getUserSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        // TODO: 从 Sa-Token 获取当前用户ID
        Long userId = 1L;
        return Result.ok(scheduleService.getUserSchedules(userId, startTime, endTime));
    }

    @Operation(summary = "创建日程")
    @PostMapping
    @OperateLog(module = "团队日程", operation = "创建日程")
    public Result<SysTeamSchedule> createSchedule(@RequestBody SysTeamSchedule schedule) {
        // TODO: 从 Sa-Token 获取当前用户信息
        schedule.setCreatorId(1L);
        schedule.setCreatorName("admin");
        return Result.ok(scheduleService.createSchedule(schedule));
    }

    @Operation(summary = "更新日程")
    @PutMapping("/{id}")
    @OperateLog(module = "团队日程", operation = "更新日程")
    public Result<Void> updateSchedule(@PathVariable Long id, @RequestBody SysTeamSchedule schedule) {
        schedule.setId(id);
        scheduleService.updateSchedule(schedule);
        return Result.ok();
    }

    @Operation(summary = "删除日程")
    @DeleteMapping("/{id}")
    @OperateLog(module = "团队日程", operation = "删除日程")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return Result.ok();
    }

    @Operation(summary = "检查时间冲突")
    @PostMapping("/conflict")
    public Result<List<SysTeamSchedule>> checkConflict(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeId) {
        return Result.ok(scheduleService.checkConflict(startTime, endTime, excludeId));
    }

    @Operation(summary = "添加参与者")
    @PostMapping("/{id}/participant/{userId}")
    @OperateLog(module = "团队日程", operation = "添加参与者")
    public Result<Void> addParticipant(@PathVariable Long id, @PathVariable Long userId) {
        scheduleService.addParticipant(id, userId);
        return Result.ok();
    }

    @Operation(summary = "移除参与者")
    @DeleteMapping("/{id}/participant/{userId}")
    @OperateLog(module = "团队日程", operation = "移除参与者")
    public Result<Void> removeParticipant(@PathVariable Long id, @PathVariable Long userId) {
        scheduleService.removeParticipant(id, userId);
        return Result.ok();
    }
}
