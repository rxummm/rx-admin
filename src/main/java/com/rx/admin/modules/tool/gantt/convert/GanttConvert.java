package com.rx.admin.modules.tool.gantt.convert;

import com.rx.admin.modules.tool.gantt.dto.GanttProjectCreateDTO;
import com.rx.admin.modules.tool.gantt.dto.GanttTaskCreateDTO;
import com.rx.admin.modules.tool.gantt.entity.GanttProject;
import com.rx.admin.modules.tool.gantt.entity.GanttTask;
import com.rx.admin.modules.tool.gantt.vo.GanttProjectVO;
import com.rx.admin.modules.tool.gantt.vo.GanttTaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GanttConvert {
    GanttProject toProjectEntity(GanttProjectCreateDTO dto);
    GanttTask toTaskEntity(GanttTaskCreateDTO dto);
    GanttProjectVO toProjectVO(GanttProject entity);
    GanttTaskVO toTaskVO(GanttTask entity);
    List<GanttProjectVO> toProjectVOList(List<GanttProject> list);
    List<GanttTaskVO> toTaskVOList(List<GanttTask> list);
}
