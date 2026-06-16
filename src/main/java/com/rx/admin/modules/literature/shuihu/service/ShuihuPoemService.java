package com.rx.admin.modules.literature.shuihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.shuihu.entity.ShuihuPoem;
import com.rx.admin.modules.literature.shuihu.mapper.ShuihuPoemMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@SuppressWarnings("null")
public class ShuihuPoemService extends ServiceImpl<ShuihuPoemMapper, ShuihuPoem> {

    public PageResult<ShuihuPoem> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<ShuihuPoem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ShuihuPoem::getTitle, keyword)
                    .or().like(ShuihuPoem::getAuthor, keyword)
                    .or().like(ShuihuPoem::getContent, keyword));
        }
        wrapper.orderByAsc(ShuihuPoem::getId);
        IPage<ShuihuPoem> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }
}