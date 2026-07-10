package com.rx.admin.modules.tool.gantt.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GanttProjectVO {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long ownerId;
    private LocalDateTime createTime;
    private List<GanttTaskVO> tasks;
}
