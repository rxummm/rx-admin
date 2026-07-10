package com.rx.admin.modules.tool.kanban.convert;

import com.rx.admin.modules.tool.kanban.dto.KanbanBoardCreateDTO;
import com.rx.admin.modules.tool.kanban.dto.KanbanCardCreateDTO;
import com.rx.admin.modules.tool.kanban.entity.KanbanBoard;
import com.rx.admin.modules.tool.kanban.entity.KanbanCard;
import com.rx.admin.modules.tool.kanban.vo.KanbanBoardVO;
import com.rx.admin.modules.tool.kanban.vo.KanbanCardVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KanbanConvert {
    KanbanBoard toBoardEntity(KanbanBoardCreateDTO dto);
    KanbanCard toCardEntity(KanbanCardCreateDTO dto);
    KanbanBoardVO toBoardVO(KanbanBoard entity);
    KanbanCardVO toCardVO(KanbanCard entity);
    List<KanbanBoardVO> toBoardVOList(List<KanbanBoard> list);
}
