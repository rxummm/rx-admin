package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.classics.Genre;
import com.rx.admin.mapper.classics.GenreMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GenreService extends ServiceImpl<GenreMapper, Genre> {

    public PageResult<Genre> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<Genre> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Genre::getName, keyword)
                    .or().like(Genre::getDescription, keyword));
        }
        wrapper.orderByAsc(Genre::getSortOrder, Genre::getId);
        IPage<Genre> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }

    public List<Genre> listAll() {
        LambdaQueryWrapper<Genre> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Genre::getSortOrder, Genre::getId);
        return list(wrapper);
    }
}