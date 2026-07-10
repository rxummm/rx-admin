package com.rx.admin.modules.calendar.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.rx.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_calendar_event")
public class CalendarEvent extends BaseEntity {

    private Long userId;
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String eventType;
    private Integer priority;
    private String color;
    private Boolean isAllDay;
    private Integer status;
}
