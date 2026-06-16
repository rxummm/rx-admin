package com.rx.admin.modules.literature.xiyou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.xiyou.entity.XiyouPoem;
import com.rx.admin.modules.literature.xiyou.mapper.XiyouPoemMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@SuppressWarnings("null")
public class XiyouPoemService extends ServiceImpl<XiyouPoemMapper, XiyouPoem> {

    public PageResult<XiyouPoem> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<XiyouPoem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(XiyouPoem::getTitle, keyword)
                    .or().like(XiyouPoem::getAuthor, keyword)
                    .or().like(XiyouPoem::getContent, keyword));
        }
        wrapper.orderByAsc(XiyouPoem::getId);
        IPage<XiyouPoem> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }
}