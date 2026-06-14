package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.SysDictType;
import com.rx.admin.mapper.SysDictTypeMapper;
import com.rx.admin.modules.system.dict.dto.DictTypeCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictTypeUpdateDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysDictTypeService extends ServiceImpl<SysDictTypeMapper, SysDictType> {

    public PageResult<SysDictType> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysDictType::getDictName, keyword)
                  .or()
                  .like(SysDictType::getDictType, keyword);
        }
        wrapper.orderByAsc(SysDictType::getId);
        Page<SysDictType> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    /**
     * 新增字典类型
     */
    public void addDictType(DictTypeCreateDTO dto) {
        long count = count(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dto.getDictType()));
        if (count > 0) {
            throw new IllegalArgumentException("字典类型已存在");
        }
        SysDictType entity = new SysDictType();
        entity.setDictName(dto.getDictName());
        entity.setDictType(dto.getDictType());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        save(entity);
    }

    /**
     * 更新字典类型
     */
    public void updateDictType(DictTypeUpdateDTO dto) {
        SysDictType entity = getById(dto.getId());
        if (entity == null) {
            throw new IllegalArgumentException("字典类型不存在");
        }
        if (StringUtils.hasText(dto.getDictName())) entity.setDictName(dto.getDictName());
        if (StringUtils.hasText(dto.getDictType())) entity.setDictType(dto.getDictType());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getRemark())) entity.setRemark(dto.getRemark());
        updateById(entity);
    }
}
