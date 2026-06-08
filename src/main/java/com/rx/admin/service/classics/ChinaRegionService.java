package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.classics.ChinaRegion;
import com.rx.admin.mapper.classics.ChinaRegionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 中国行政区划 Service
 */
@Service
public class ChinaRegionService extends ServiceImpl<ChinaRegionMapper, ChinaRegion> {

    /**
     * 分页查询行政区划
     */
    public PageResult<ChinaRegion> pageQuery(int page, int size, String keyword, Integer level, String parentCode) {
        LambdaQueryWrapper<ChinaRegion> wrapper = new LambdaQueryWrapper<ChinaRegion>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ChinaRegion::getName, keyword);
        }
        if (level != null && level > 0) {
            wrapper.eq(ChinaRegion::getLevel, level);
        }
        if (StringUtils.hasText(parentCode)) {
            wrapper.eq(ChinaRegion::getParentCode, parentCode);
        }
        wrapper.orderByAsc(ChinaRegion::getLevel)
               .orderByAsc(ChinaRegion::getSort)
               .orderByAsc(ChinaRegion::getId);
        Page<ChinaRegion> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result);
    }

    /**
     * 查询下级行政区划列表（用于级联选择器）
     */
    public List<ChinaRegion> listByParentCode(String parentCode) {
        LambdaQueryWrapper<ChinaRegion> wrapper = new LambdaQueryWrapper<ChinaRegion>()
                .eq(ChinaRegion::getStatus, 1);
        if (parentCode == null || parentCode.isEmpty()) {
            wrapper.eq(ChinaRegion::getLevel, 1);
        } else {
            wrapper.eq(ChinaRegion::getParentCode, parentCode);
        }
        wrapper.orderByAsc(ChinaRegion::getSort)
               .orderByAsc(ChinaRegion::getId);
        return list(wrapper);
    }

    /**
     * 搜索行政区划（用于级联选择器搜索）
     */
    public List<ChinaRegion> search(String keyword, Integer level) {
        LambdaQueryWrapper<ChinaRegion> wrapper = new LambdaQueryWrapper<ChinaRegion>()
                .eq(ChinaRegion::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ChinaRegion::getName, keyword);
        }
        if (level != null) {
            wrapper.eq(ChinaRegion::getLevel, level);
        }
        wrapper.orderByAsc(ChinaRegion::getLevel)
               .orderByAsc(ChinaRegion::getSort)
               .orderByAsc(ChinaRegion::getId);
        return list(wrapper);
    }
}
