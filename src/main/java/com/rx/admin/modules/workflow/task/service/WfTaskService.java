package com.rx.admin.modules.workflow.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.workflow.task.dto.WfTaskApproveDTO;
import com.rx.admin.modules.workflow.task.dto.WfTaskQueryDTO;
import com.rx.admin.modules.workflow.task.dto.WfTaskTransferDTO;
import com.rx.admin.modules.workflow.task.entity.WfTask;

public interface WfTaskService extends IService<WfTask> {
    PageResult<WfTask> queryPage(WfTaskQueryDTO query);
    void approveTask(WfTaskApproveDTO dto, Long operatorId, String operatorName);
    void transferTask(WfTaskTransferDTO dto);
    
    /**
     * 委托任务
     */
    void delegateTask(Long taskId, Long delegateId, String delegateName, String reason, Long operatorId);
    
    /**
     * 检查超时任务
     */
    void checkTimeoutTasks();
}
