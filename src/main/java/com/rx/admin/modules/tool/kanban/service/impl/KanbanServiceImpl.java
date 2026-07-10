package com.rx.admin.modules.tool.kanban.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.exception.BusinessException;
import com.rx.admin.modules.tool.kanban.dto.KanbanBoardCreateDTO;
import com.rx.admin.modules.tool.kanban.dto.KanbanCardCreateDTO;
import com.rx.admin.modules.tool.kanban.entity.KanbanBoard;
import com.rx.admin.modules.tool.kanban.entity.KanbanCard;
import com.rx.admin.modules.tool.kanban.entity.KanbanColumn;
import com.rx.admin.modules.tool.kanban.mapper.KanbanBoardMapper;
import com.rx.admin.modules.tool.kanban.mapper.KanbanCardMapper;
import com.rx.admin.modules.tool.kanban.mapper.KanbanColumnMapper;
import com.rx.admin.modules.tool.kanban.service.KanbanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KanbanServiceImpl extends ServiceImpl<KanbanBoardMapper, KanbanBoard> implements KanbanService {

    private final KanbanColumnMapper columnMapper;
    private final KanbanCardMapper cardMapper;

    @Override
    public List<KanbanBoard> listBoards(Long userId) {
        LambdaQueryWrapper<KanbanBoard> w = new LambdaQueryWrapper<>();
        w.eq(KanbanBoard::getOwnerId, userId).or().eq(KanbanBoard::getStatus, 1);
        return list(w);
    }

    @Override
    public KanbanBoard getBoardDetail(Long boardId) {
        return getById(boardId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBoard(KanbanBoardCreateDTO dto, Long ownerId) {
        KanbanBoard board = new KanbanBoard();
        board.setName(dto.getName());
        board.setDescription(dto.getDescription());
        board.setOwnerId(ownerId);
        board.setStatus(1);
        save(board);

        String[] defaultColumns = {"待办", "进行中", "已完成"};
        String[] colors = {"#909399", "#409eff", "#67c23a"};
        for (int i = 0; i < defaultColumns.length; i++) {
            KanbanColumn col = new KanbanColumn();
            col.setBoardId(board.getId());
            col.setName(defaultColumns[i]);
            col.setColor(colors[i]);
            col.setSortOrder(i);
            col.setWipLimit(0);
            columnMapper.insert(col);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCard(KanbanCardCreateDTO dto) {
        KanbanCard card = new KanbanCard();
        card.setBoardId(dto.getBoardId());
        card.setColumnId(dto.getColumnId());
        card.setTitle(dto.getTitle());
        card.setDescription(dto.getDescription());
        card.setPriority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM");
        card.setAssigneeId(dto.getAssigneeId());
        card.setAssigneeName(dto.getAssigneeName());
        card.setDueDate(dto.getDueDate());
        card.setTags(dto.getTags());
        card.setSortOrder(0);
        cardMapper.insert(card);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveCard(Long cardId, Long targetColumnId, Integer sortOrder) {
        KanbanCard card = cardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException("卡片不存在");
        }
        card.setColumnId(targetColumnId);
        card.setSortOrder(sortOrder);
        cardMapper.updateById(card);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCard(Long cardId) {
        cardMapper.deleteById(cardId);
    }
}
