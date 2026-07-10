package com.rx.admin.modules.calendar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CalendarEventUpdateDTO {

    @NotNull(message = "事件ID不能为空")
    private Long id;

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
