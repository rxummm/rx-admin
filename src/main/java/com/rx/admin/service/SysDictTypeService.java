package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysDictType;
import com.rx.admin.mapper.SysDictTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysDictTypeService extends ServiceImpl<SysDictTypeMapper, SysDictType> {

    public PageResult<SysDictType> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .like(StringUtils.hasText(keyword), SysDictType::getDictName, keyword)
                .or().like(StringUtils.hasText(keyword), SysDictType::getDictType, keyword)
                .orderByAsc(SysDictType::getId);
        Page<SysDictType> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }
}
