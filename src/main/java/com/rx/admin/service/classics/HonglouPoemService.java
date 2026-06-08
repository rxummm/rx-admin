package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.classics.HonglouPoem;
import com.rx.admin.mapper.classics.HonglouPoemMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HonglouPoemService extends ServiceImpl<HonglouPoemMapper, HonglouPoem> {

    public PageResult<HonglouPoem> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<HonglouPoem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(HonglouPoem::getTitle, keyword)
                    .or().like(HonglouPoem::getAuthor, keyword)
                    .or().like(HonglouPoem::getContent, keyword));
        }
        wrapper.orderByAsc(HonglouPoem::getId);
        IPage<HonglouPoem> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getRecords());
    }
}
