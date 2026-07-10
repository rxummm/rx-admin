package com.rx.admin.modules.calendar.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 团队日程实体
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("sys_team_schedule")
public class SysTeamSchedule extends BaseEntity {
    /** 日程标题 */
    private String title;
    
    /** 日程描述 */
    private String description;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 是否全天事件 */
    private Boolean allDay;
    
    /** 重复规则（RRULE格式） */
    private String recurrence;
    
    /** 地点 */
    private String location;
    
    /** 参与者ID列表（逗号分隔） */
    private String participants;
    
    /** 创建者ID */
    private Long creatorId;
    
    /** 创建者姓名 */
    private String creatorName;
    
    /** 日程类型：meeting-会议，event-事件，reminder-提醒 */
    private String scheduleType;
    
    /** 状态：tentative-待定，confirmed-已确认，cancelled-已取消 */
    private String status;
    
    /** 颜色 */
    private String color;
}
