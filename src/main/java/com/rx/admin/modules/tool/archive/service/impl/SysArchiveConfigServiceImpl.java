package com.rx.admin.modules.tool.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.tool.archive.dto.SysArchiveConfigCreateDTO;
import com.rx.admin.modules.tool.archive.entity.SysArchiveConfig;
import com.rx.admin.modules.tool.archive.mapper.SysArchiveConfigMapper;
import com.rx.admin.modules.tool.archive.service.SysArchiveConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysArchiveConfigServiceImpl extends ServiceImpl<SysArchiveConfigMapper, SysArchiveConfig> implements SysArchiveConfigService {

    @Override
    public List<SysArchiveConfig> listAll() {
        return list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEntity(SysArchiveConfigCreateDTO dto) {
        SysArchiveConfig entity = new SysArchiveConfig();
        entity.setTableName(dto.getTableName());
        entity.setArchiveTable(dto.getArchiveTable());
        entity.setConditionField(dto.getConditionField());
        entity.setRetainDays(dto.getRetainDays() != null ? dto.getRetainDays() : 365);
        entity.setBatchSize(dto.getBatchSize() != null ? dto.getBatchSize() : 1000);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        save(entity);
    }
}
