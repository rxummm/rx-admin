package com.rx.admin.modules.calendar.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.calendar.entity.SysTeamSchedule;
import com.rx.admin.modules.calendar.mapper.SysTeamScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 团队日程服务
 * 支持日程共享、会议安排、冲突检测
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamScheduleService {

    private final SysTeamScheduleMapper scheduleMapper;

    /**
     * 获取用户的日程（包括参与的日程）
     */
    public List<SysTeamSchedule> getUserSchedules(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        String userIdStr = userId.toString();
        
        LambdaQueryWrapper<SysTeamSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(SysTeamSchedule::getCreatorId, userId)
                .or()
                .like(SysTeamSchedule::getParticipants, userIdStr)
        );
        
        if (startTime != null) {
            wrapper.ge(SysTeamSchedule::getEndTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysTeamSchedule::getStartTime, endTime);
        }
        
        wrapper.orderByAsc(SysTeamSchedule::getStartTime);
        return scheduleMapper.selectList(wrapper);
    }

    /**
     * 创建日程
     */
    public SysTeamSchedule createSchedule(SysTeamSchedule schedule) {
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setStatus("confirmed");
        scheduleMapper.insert(schedule);
        log.info("创建团队日程: title={}, creator={}", schedule.getTitle(), schedule.getCreatorName());
        return schedule;
    }

    /**
     * 更新日程
     */
    public void updateSchedule(SysTeamSchedule schedule) {
        schedule.setUpdateTime(LocalDateTime.now());
        scheduleMapper.updateById(schedule);
    }

    /**
     * 删除日程
     */
    public void deleteSchedule(Long id) {
        scheduleMapper.deleteById(id);
    }

    /**
     * 检查时间冲突
     */
    public List<SysTeamSchedule> checkConflict(LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        LambdaQueryWrapper<SysTeamSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(SysTeamSchedule::getStartTime, endTime)
               .ge(SysTeamSchedule::getEndTime, startTime)
               .ne(SysTeamSchedule::getStatus, "cancelled");
        
        if (excludeId != null) {
            wrapper.ne(SysTeamSchedule::getId, excludeId);
        }
        
        return scheduleMapper.selectList(wrapper);
    }

    /**
     * 添加参与者
     */
    public void addParticipant(Long scheduleId, Long userId) {
        SysTeamSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("日程不存在");
        }
        
        String participants = schedule.getParticipants();
        String userIdStr = userId.toString();
        
        if (participants == null || participants.isEmpty()) {
            schedule.setParticipants(userIdStr);
        } else if (!Arrays.asList(participants.split(",")).contains(userIdStr)) {
            schedule.setParticipants(participants + "," + userIdStr);
        }
        
        scheduleMapper.updateById(schedule);
    }

    /**
     * 移除参与者
     */
    public void removeParticipant(Long scheduleId, Long userId) {
        SysTeamSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getParticipants() == null) {
            return;
        }
        
        String userIdStr = userId.toString();
        List<String> participants = Arrays.asList(schedule.getParticipants().split(","));
        String newParticipants = participants.stream()
                .filter(p -> !p.equals(userIdStr))
                .collect(Collectors.joining(","));
        
        schedule.setParticipants(newParticipants);
        scheduleMapper.updateById(schedule);
    }
}
