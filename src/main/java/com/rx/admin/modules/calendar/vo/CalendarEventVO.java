package com.rx.admin.modules.calendar.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class CalendarEventVO {

    private Long id;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
