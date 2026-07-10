package com.rx.admin.modules.workflow.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.exception.BusinessException;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.workflow.task.dto.WfTaskApproveDTO;
import com.rx.admin.modules.workflow.task.dto.WfTaskQueryDTO;
import com.rx.admin.modules.workflow.task.dto.WfTaskTransferDTO;
import com.rx.admin.modules.workflow.task.entity.WfTask;
import com.rx.admin.modules.workflow.task.mapper.WfTaskMapper;
import com.rx.admin.modules.workflow.task.service.WfTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WfTaskServiceImpl extends ServiceImpl<WfTaskMapper, WfTask> implements WfTaskService {

    @Override
    public PageResult<WfTask> queryPage(WfTaskQueryDTO query) {
        LambdaQueryWrapper<WfTask> w = new LambdaQueryWrapper<>();
        if (query.getAssigneeId() != null) {
            w.eq(WfTask::getAssigneeId, query.getAssigneeId());
        }
        if (query.getInstanceId() != null) {
            w.eq(WfTask::getInstanceId, query.getInstanceId());
        }
        if (query.getStatus() != null) {
            w.eq(WfTask::getStatus, query.getStatus());
        }
        w.orderByDesc(WfTask::getCreateTime);
        Page<WfTask> page = page(new Page<>(query.getPage(), query.getSize()), w);
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(WfTaskApproveDTO dto, Long operatorId, String operatorName) {
        WfTask task = getById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException("任务状态不允许审批");
        }
        if (!operatorId.equals(task.getAssigneeId())) {
            throw new BusinessException("无权审批此任务");
        }
        WfTask update = new WfTask();
        update.setId(dto.getTaskId());
        update.setStatus("reject".equals(dto.getAction()) ? "REJECTED" : "COMPLETED");
        update.setComment(dto.getComment());
        update.setCompleteTime(LocalDateTime.now());
        updateById(update);
        log.info("任务审批: taskId={}, action={}, operator={}", dto.getTaskId(), dto.getAction(), operatorName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(WfTaskTransferDTO dto) {
        WfTask task = getById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException("任务状态不允许转办");
        }
        WfTask update = new WfTask();
        update.setId(dto.getTaskId());
        update.setAssigneeId(dto.getTargetUserId());
        update.setAssigneeName(dto.getTargetUserName());
        update.setComment(dto.getComment());
        updateById(update);
        log.info("任务转办: taskId={}, targetUser={}", dto.getTaskId(), dto.getTargetUserName());
    }
}
