package com.rx.admin.service.classics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.entity.classics.HonglouCharacterRelation;
import com.rx.admin.mapper.classics.HonglouCharacterRelationMapper;
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
