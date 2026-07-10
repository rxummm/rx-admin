package com.rx.admin.modules.workflow.instance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.exception.BusinessException;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.workflow.instance.dto.WfProcessInstanceCreateDTO;
import com.rx.admin.modules.workflow.instance.dto.WfProcessInstanceQueryDTO;
import com.rx.admin.modules.workflow.instance.entity.WfProcessInstance;
import com.rx.admin.modules.workflow.instance.mapper.WfProcessInstanceMapper;
import com.rx.admin.modules.workflow.instance.service.WfProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WfProcessInstanceServiceImpl extends ServiceImpl<WfProcessInstanceMapper, WfProcessInstance> implements WfProcessInstanceService {

    @Override
    public PageResult<WfProcessInstance> queryPage(WfProcessInstanceQueryDTO query) {
        LambdaQueryWrapper<WfProcessInstance> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            w.like(WfProcessInstance::getTitle, query.getKeyword());
        }
        if (query.getInitiatorId() != null) {
            w.eq(WfProcessInstance::getInitiatorId, query.getInitiatorId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            w.eq(WfProcessInstance::getStatus, query.getStatus());
        }
        w.orderByDesc(WfProcessInstance::getCreateTime);
        Page<WfProcessInstance> page = page(new Page<>(query.getPage(), query.getSize()), w);
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startProcess(WfProcessInstanceCreateDTO dto, Long initiatorId, String initiatorName) {
        WfProcessInstance instance = new WfProcessInstance();
        instance.setDefinitionId(dto.getDefinitionId());
        instance.setTitle(dto.getTitle());
        instance.setBusinessKey(dto.getBusinessKey());
        instance.setBusinessType(dto.getBusinessType());
        instance.setFormData(dto.getFormData());
        instance.setInitiatorId(initiatorId);
        instance.setInitiatorName(initiatorName);
        instance.setStatus("RUNNING");
        instance.setStartTime(LocalDateTime.now());
        instance.setCurrentNode("start");
        save(instance);
        log.info("流程启动: instanceId={}, title={}, initiator={}", instance.getId(), instance.getTitle(), initiatorName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcess(Long id) {
        WfProcessInstance instance = getById(id);
        if (instance == null) {
            throw new BusinessException("流程实例不存在");
        }
        if (!"RUNNING".equals(instance.getStatus()) && !"PENDING".equals(instance.getStatus())) {
            throw new BusinessException("当前状态不允许取消");
        }
        WfProcessInstance update = new WfProcessInstance();
        update.setId(id);
        update.setStatus("CANCELLED");
        update.setEndTime(LocalDateTime.now());
        updateById(update);
        log.info("流程取消: instanceId={}", id);
    }
}
