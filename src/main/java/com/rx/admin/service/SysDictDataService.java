package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.entity.SysDictData;
import com.rx.admin.mapper.SysDictDataMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysDictDataService extends ServiceImpl<SysDictDataMapper, SysDictData> {

    public List<SysDictData> listByTypeId(Long typeId) {
        return list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getTypeId, typeId)
                .orderByAsc(SysDictData::getSort));
    }

    public List<SysDictData> getByDictType(String dictType) {
        return baseMapper.selectByDictType(dictType);
    }
}
