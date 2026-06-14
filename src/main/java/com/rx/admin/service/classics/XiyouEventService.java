package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.classics.XiyouEvent;
import com.rx.admin.mapper.classics.XiyouEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class XiyouEventService extends ServiceImpl<XiyouEventMapper, XiyouEvent> {

    public PageResult<XiyouEvent> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<XiyouEvent> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(XiyouEvent::getTitle, keyword)
                    .or().like(XiyouEvent::getMonster, keyword)
                    .or().like(XiyouEvent::getLocation, keyword));
        }
        wrapper.orderByAsc(XiyouEvent::getDifficultyNum);
        IPage<XiyouEvent> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }

    public List<XiyouEvent> listAll() {
        LambdaQueryWrapper<XiyouEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(XiyouEvent::getDifficultyNum);
        return list(wrapper);
    }
}