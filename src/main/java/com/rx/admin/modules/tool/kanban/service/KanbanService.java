package com.rx.admin.modules.tool.kanban.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.tool.kanban.dto.KanbanBoardCreateDTO;
import com.rx.admin.modules.tool.kanban.dto.KanbanCardCreateDTO;
import com.rx.admin.modules.tool.kanban.entity.KanbanBoard;

import java.util.List;

public interface KanbanService extends IService<KanbanBoard> {
    List<KanbanBoard> listBoards(Long userId);
    KanbanBoard getBoardDetail(Long boardId);
    void createBoard(KanbanBoardCreateDTO dto, Long ownerId);
    void createCard(KanbanCardCreateDTO dto);
    void moveCard(Long cardId, Long targetColumnId, Integer sortOrder);
    void deleteCard(Long cardId);
}
