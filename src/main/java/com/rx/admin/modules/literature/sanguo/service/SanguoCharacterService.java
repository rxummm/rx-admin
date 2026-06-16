package com.rx.admin.modules.literature.sanguo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.sanguo.entity.SanguoCharacter;
import com.rx.admin.modules.literature.sanguo.mapper.SanguoCharacterMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@SuppressWarnings("null")
public class SanguoCharacterService extends ServiceImpl<SanguoCharacterMapper, SanguoCharacter> {

    public PageResult<SanguoCharacter> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<SanguoCharacter> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SanguoCharacter::getName, keyword)
                    .or().like(SanguoCharacter::getCourtesyName, keyword)
                    .or().like(SanguoCharacter::getNickname, keyword)
                    .or().like(SanguoCharacter::getNotableEvents, keyword));
        }
        wrapper.orderByAsc(SanguoCharacter::getId);
        IPage<SanguoCharacter> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage);
    }

    /** 按国家筛选人物 */
    public List<SanguoCharacter> listByCountry(String country) {
        LambdaQueryWrapper<SanguoCharacter> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(country)) {
            wrapper.eq(SanguoCharacter::getCountry, country);
        }
        wrapper.orderByAsc(SanguoCharacter::getId);
        return list(wrapper);
    }
}