package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.classics.ShuihuPoem;
import com.rx.admin.mapper.classics.ShuihuPoemMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
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