package com.rx.admin.modules.tool.kanban.vo;

import lombok.Data;
import java.util.List;

@Data
public class KanbanColumnVO {
    private Long id;
    private Long boardId;
    private String name;
    private String color;
    private Integer sortOrder;
    private Integer wipLimit;
    private List<KanbanCardVO> cards;
}
