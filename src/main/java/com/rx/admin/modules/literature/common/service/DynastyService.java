package com.rx.admin.modules.literature.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.common.entity.Dynasty;
import com.rx.admin.modules.literature.common.mapper.DynastyMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@SuppressWarnings("null")
public class DynastyService extends ServiceImpl<DynastyMapper, Dynasty> {

    public PageResult<Dynasty> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<Dynasty> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Dynasty::getName, keyword)
                    .or().like(Dynasty::getDescription, keyword));
        }
        wrapper.orderByAsc(Dynasty::getSortOrder, Dynasty::getId);
        IPage<Dynasty> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }

    public List<Dynasty> listAll() {
        LambdaQueryWrapper<Dynasty> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Dynasty::getSortOrder, Dynasty::getId);
        return list(wrapper);
    }
}