package com.rx.admin.modules.calendar.dto;

import lombok.Data;

@Data
public class CalendarEventQueryDTO {

    private String keyword;
    private Integer year;
    private Integer month;
    private String startDate;
    private String endDate;
    private String eventType;
    private Integer status;
}
