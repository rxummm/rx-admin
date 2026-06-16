package com.rx.admin.modules.literature.xiyou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.xiyou.entity.XiyouCharacter;
import com.rx.admin.modules.literature.xiyou.mapper.XiyouCharacterMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@SuppressWarnings("null")
public class XiyouCharacterService extends ServiceImpl<XiyouCharacterMapper, XiyouCharacter> {

    public PageResult<XiyouCharacter> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<XiyouCharacter> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(XiyouCharacter::getName, keyword)
                    .or().like(XiyouCharacter::getAlias, keyword)
                    .or().like(XiyouCharacter::getMainDeeds, keyword));
        }
        wrapper.orderByAsc(XiyouCharacter::getId);
        IPage<XiyouCharacter> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }

    /** 按种族筛选人物 */
    public List<XiyouCharacter> listByRace(String race) {
        LambdaQueryWrapper<XiyouCharacter> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(race)) {
            wrapper.eq(XiyouCharacter::getRace, race);
        }
        wrapper.orderByAsc(XiyouCharacter::getId);
        return list(wrapper);
    }
}