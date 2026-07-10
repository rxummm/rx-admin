package com.rx.admin.modules.workflow.instance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.workflow.instance.dto.WfProcessInstanceCreateDTO;
import com.rx.admin.modules.workflow.instance.dto.WfProcessInstanceQueryDTO;
import com.rx.admin.modules.workflow.instance.entity.WfProcessInstance;

public interface WfProcessInstanceService extends IService<WfProcessInstance> {
    PageResult<WfProcessInstance> queryPage(WfProcessInstanceQueryDTO query);
    void startProcess(WfProcessInstanceCreateDTO dto, Long initiatorId, String initiatorName);
    void cancelProcess(Long id);
}
