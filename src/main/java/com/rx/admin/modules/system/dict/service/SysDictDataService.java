package com.rx.admin.modules.system.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.system.dict.entity.SysDictData;
import com.rx.admin.modules.system.dict.mapper.SysDictDataMapper;
import com.rx.admin.modules.system.dict.dto.DictDataCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictDataUpdateDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@SuppressWarnings("null")
public class SysDictDataService extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

    public List<SysDictData> listByTypeId(Long typeId) {
        return list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getTypeId, typeId)
                .orderByAsc(SysDictData::getSort));
    }

    public List<SysDictData> getByDictType(String dictType) {
        return baseMapper.selectByDictType(dictType);
    }

    /**
     * 新增字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDictData(DictDataCreateDTO dto) {
        long count = count(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getTypeId, dto.getTypeId())
                .eq(SysDictData::getDictValue, dto.getDictValue()));
        if (count > 0) {
            throw new IllegalArgumentException("该字典类型下已存在相同字典值");
        }
        SysDictData entity = new SysDictData();
        entity.setTypeId(dto.getTypeId());
        entity.setDictLabel(dto.getDictLabel());
        entity.setDictValue(dto.getDictValue());
        entity.setCssClass(dto.getCssClass());
        entity.setListClass(dto.getListClass());
        entity.setSort(dto.getSort());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        save(entity);
    }

    /**
     * 更新字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDictData(DictDataUpdateDTO dto) {
        SysDictData entity = getById(dto.getId());
        if (entity == null) {
            throw new IllegalArgumentException("字典数据不存在");
        }
        if (dto.getTypeId() != null) entity.setTypeId(dto.getTypeId());
        if (StringUtils.hasText(dto.getDictLabel())) entity.setDictLabel(dto.getDictLabel());
        if (StringUtils.hasText(dto.getDictValue())) entity.setDictValue(dto.getDictValue());
        if (StringUtils.hasText(dto.getCssClass())) entity.setCssClass(dto.getCssClass());
        if (StringUtils.hasText(dto.getListClass())) entity.setListClass(dto.getListClass());
        if (dto.getSort() != null) entity.setSort(dto.getSort());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getRemark())) entity.setRemark(dto.getRemark());
        updateById(entity);
    }

    @Override
    public void deleteDictData(Long id) {
        SysDictData entity = getById(id);
        if (entity == null) {
            throw new IllegalArgumentException("字典数据不存在");
        }
        removeById(id);
    }

    @Override
    public void deleteDictDataBatch(List<Long> ids) {
        for (Long id : ids) {
            deleteDictData(id);
        }
    }
}
