package com.rx.admin.modules.tool.gantt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.tool.gantt.dto.GanttProjectCreateDTO;
import com.rx.admin.modules.tool.gantt.dto.GanttTaskCreateDTO;
import com.rx.admin.modules.tool.gantt.entity.GanttProject;

import java.util.List;

public interface GanttService extends IService<GanttProject> {
    List<GanttProject> listProjects();
    GanttProject getProjectDetail(Long projectId);
    void createProject(GanttProjectCreateDTO dto, Long ownerId);
    void createTask(GanttTaskCreateDTO dto);
    void updateTaskProgress(Long taskId, Integer progress);
    void deleteTask(Long taskId);
}
