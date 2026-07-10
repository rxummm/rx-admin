package com.rx.admin.modules.tool.gantt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.exception.BusinessException;
import com.rx.admin.modules.tool.gantt.dto.GanttProjectCreateDTO;
import com.rx.admin.modules.tool.gantt.dto.GanttTaskCreateDTO;
import com.rx.admin.modules.tool.gantt.entity.GanttProject;
import com.rx.admin.modules.tool.gantt.entity.GanttTask;
import com.rx.admin.modules.tool.gantt.mapper.GanttProjectMapper;
import com.rx.admin.modules.tool.gantt.mapper.GanttTaskMapper;
import com.rx.admin.modules.tool.gantt.service.GanttService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GanttServiceImpl extends ServiceImpl<GanttProjectMapper, GanttProject> implements GanttService {

    private final GanttTaskMapper taskMapper;

    @Override
    public List<GanttProject> listProjects() {
        return list();
    }

    @Override
    public GanttProject getProjectDetail(Long projectId) {
        return getById(projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProject(GanttProjectCreateDTO dto, Long ownerId) {
        GanttProject project = new GanttProject();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setStatus(dto.getStatus() != null ? dto.getStatus() : "PLANNING");
        project.setOwnerId(ownerId);
        save(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTask(GanttTaskCreateDTO dto) {
        GanttTask task = new GanttTask();
        task.setProjectId(dto.getProjectId());
        task.setParentId(dto.getParentId());
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : "TODO");
        task.setAssigneeId(dto.getAssigneeId());
        task.setAssigneeName(dto.getAssigneeName());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM");
        task.setSortOrder(0);
        taskMapper.insert(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskProgress(Long taskId, Integer progress) {
        GanttTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        task.setProgress(progress);
        if (progress >= 100) {
            task.setStatus("DONE");
        }
        taskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        taskMapper.deleteById(taskId);
    }
}
