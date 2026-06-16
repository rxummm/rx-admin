package com.rx.admin.modules.literature.honglou.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.literature.honglou.entity.HonglouCharacterRelation;
import com.rx.admin.modules.literature.honglou.mapper.HonglouCharacterRelationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HonglouCharacterRelationService extends ServiceImpl<HonglouCharacterRelationMapper, HonglouCharacterRelation> {

    /** 查询某人相关的所有关系 */
    public List<HonglouCharacterRelation> listByCharacterId(Long characterId) {
        LambdaQueryWrapper<HonglouCharacterRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HonglouCharacterRelation::getFromCharacterId, characterId)
               .or()
               .eq(HonglouCharacterRelation::getToCharacterId, characterId);
        wrapper.orderByAsc(HonglouCharacterRelation::getId);
        return list(wrapper);
    }

    /** 获取所有人物关系 */
    public List<HonglouCharacterRelation> listAll() {
        LambdaQueryWrapper<HonglouCharacterRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(HonglouCharacterRelation::getId);
        return list(wrapper);
    }
}
