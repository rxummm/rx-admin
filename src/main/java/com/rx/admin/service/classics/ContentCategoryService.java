package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.classics.ContentCategory;
import com.rx.admin.mapper.classics.ContentCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ContentCategoryService extends ServiceImpl<ContentCategoryMapper, ContentCategory> {

    public PageResult<ContentCategory> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<ContentCategory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ContentCategory::getName, keyword)
                    .or().like(ContentCategory::getDescription, keyword));
        }
        wrapper.orderByAsc(ContentCategory::getSortOrder, ContentCategory::getId);
        IPage<ContentCategory> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getRecords());
    }

    public List<ContentCategory> listAll() {
        LambdaQueryWrapper<ContentCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ContentCategory::getSortOrder, ContentCategory::getId);
        return list(wrapper);
    }
}
