package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.classics.XiyouPoem;
import com.rx.admin.mapper.classics.XiyouPoemMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
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
        return PageResult.of(iPage.getTotal(), iPage.getRecords());
    }
}
