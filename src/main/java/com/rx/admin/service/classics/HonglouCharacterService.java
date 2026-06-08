package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.classics.HonglouCharacter;
import com.rx.admin.mapper.classics.HonglouCharacterMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class HonglouCharacterService extends ServiceImpl<HonglouCharacterMapper, HonglouCharacter> {

    public PageResult<HonglouCharacter> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<HonglouCharacter> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(HonglouCharacter::getName, keyword)
                    .or().like(HonglouCharacter::getNickname, keyword)
                    .or().like(HonglouCharacter::getAppearanceDescription, keyword));
        }
        wrapper.orderByAsc(HonglouCharacter::getId);
        IPage<HonglouCharacter> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage.getTotal(), iPage.getRecords());
    }

    /** 按角色筛选人物（数据库 role 字段：主角/重要配角/一般角色） */
    public List<HonglouCharacter> listByRole(String role) {
        LambdaQueryWrapper<HonglouCharacter> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) {
            wrapper.eq(HonglouCharacter::getRole, role);
        }
        wrapper.orderByAsc(HonglouCharacter::getId);
        return list(wrapper);
    }

    /** 获取所有人物 */
    public List<HonglouCharacter> listAll() {
        LambdaQueryWrapper<HonglouCharacter> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(HonglouCharacter::getId);
        return list(wrapper);
    }
}
