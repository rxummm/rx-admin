package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.classics.SanguoPoem;
import com.rx.admin.mapper.classics.SanguoPoemMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SanguoPoemService extends ServiceImpl<SanguoPoemMapper, SanguoPoem> {

    public PageResult<SanguoPoem> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<SanguoPoem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SanguoPoem::getTitle, keyword)
                    .or().like(SanguoPoem::getAuthor, keyword)
                    .or().like(SanguoPoem::getContent, keyword));
        }
        wrapper.orderByAsc(SanguoPoem::getId);
        IPage<SanguoPoem> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getRecords());
    }
}
