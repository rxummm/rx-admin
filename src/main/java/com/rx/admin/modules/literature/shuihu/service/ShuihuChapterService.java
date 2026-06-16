package com.rx.admin.modules.literature.shuihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.shuihu.entity.ShuihuChapter;
import com.rx.admin.modules.literature.shuihu.mapper.ShuihuChapterMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ShuihuChapterService extends ServiceImpl<ShuihuChapterMapper, ShuihuChapter> {

    public PageResult<ShuihuChapter> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<ShuihuChapter> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ShuihuChapter::getChapterTitle, keyword)
                    .or().like(ShuihuChapter::getChapterContent, keyword)
                    .or().like(ShuihuChapter::getCharacters, keyword));
        }
        wrapper.orderByAsc(ShuihuChapter::getChapterNumber);
        IPage<ShuihuChapter> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }
}