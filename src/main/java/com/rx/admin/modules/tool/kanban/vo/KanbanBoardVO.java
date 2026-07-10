package com.rx.admin.modules.tool.kanban.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KanbanBoardVO {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private Integer status;
    private LocalDateTime createTime;
    private List<KanbanColumnVO> columns;
}
