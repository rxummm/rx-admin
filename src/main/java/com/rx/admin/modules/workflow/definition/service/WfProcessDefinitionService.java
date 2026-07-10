package com.rx.admin.modules.workflow.definition.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionCreateDTO;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionQueryDTO;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionUpdateDTO;
import com.rx.admin.modules.workflow.definition.entity.WfProcessDefinition;

public interface WfProcessDefinitionService extends IService<WfProcessDefinition> {
    PageResult<WfProcessDefinition> queryPage(WfProcessDefinitionQueryDTO query);
    void addEntity(WfProcessDefinitionCreateDTO dto);
    void updateEntity(WfProcessDefinitionUpdateDTO dto);
}
