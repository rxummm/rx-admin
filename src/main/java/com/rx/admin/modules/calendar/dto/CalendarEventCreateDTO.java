package com.rx.admin.modules.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CalendarEventCreateDTO {

    @NotBlank(message = "事件标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "事件日期不能为空")
    private LocalDate eventDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String eventType;

    private Integer priority;

    private String color;

    private Boolean isAllDay;

    private Integer status;
}
