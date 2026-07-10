package com.rx.admin.modules.workflow.definition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionCreateDTO;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionQueryDTO;
import com.rx.admin.modules.workflow.definition.dto.WfProcessDefinitionUpdateDTO;
import com.rx.admin.modules.workflow.definition.entity.WfProcessDefinition;
import com.rx.admin.modules.workflow.definition.mapper.WfProcessDefinitionMapper;
import com.rx.admin.modules.workflow.definition.service.WfProcessDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class WfProcessDefinitionServiceImpl extends ServiceImpl<WfProcessDefinitionMapper, WfProcessDefinition> implements WfProcessDefinitionService {

    @Override
    public PageResult<WfProcessDefinition> queryPage(WfProcessDefinitionQueryDTO query) {
        LambdaQueryWrapper<WfProcessDefinition> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            w.and(wrapper -> wrapper.like(WfProcessDefinition::getName, query.getKeyword())
                    .or().like(WfProcessDefinition::getCode, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getCategory())) {
            w.eq(WfProcessDefinition::getCategory, query.getCategory());
        }
        if (query.getStatus() != null) {
            w.eq(WfProcessDefinition::getStatus, query.getStatus());
        }
        w.orderByDesc(WfProcessDefinition::getCreateTime);
        Page<WfProcessDefinition> page = page(new Page<>(query.getPage(), query.getSize()), w);
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEntity(WfProcessDefinitionCreateDTO dto) {
        WfProcessDefinition entity = new WfProcessDefinition();
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setFormConfig(dto.getFormConfig());
        entity.setProcessConfig(dto.getProcessConfig());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setVersion(1);
        save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEntity(WfProcessDefinitionUpdateDTO dto) {
        WfProcessDefinition entity = new WfProcessDefinition();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setFormConfig(dto.getFormConfig());
        entity.setProcessConfig(dto.getProcessConfig());
        entity.setStatus(dto.getStatus());
        updateById(entity);
    }
}
