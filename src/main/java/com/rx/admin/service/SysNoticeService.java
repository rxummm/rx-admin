package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.SysNotice;
import com.rx.admin.mapper.SysNoticeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysNoticeService extends ServiceImpl<SysNoticeMapper, SysNotice> {

    public PageResult<SysNotice> pageQuery(int page, int size, String keyword) {
        return pageQuery(page, size, keyword, null);
    }

    public PageResult<SysNotice> pageQuery(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>()
                .eq(category != null, SysNotice::getCategory, category)
                .like(StringUtils.hasText(keyword), SysNotice::getTitle, keyword)
                .orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    /**
     * 获取各分类的未读数量（通知未读 + 公告未读 + 待办数量）
     * 待办以 status=1 记录存在为准（未被清除的）
     */
    public long countByCategory(String category) {
        return count(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getCategory, category)
                .eq(SysNotice::getStatus, 1));
    }
}
