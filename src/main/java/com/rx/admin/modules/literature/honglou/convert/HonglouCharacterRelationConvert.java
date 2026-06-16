package com.rx.admin.modules.literature.honglou.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.honglou.entity.HonglouCharacterRelation;
import com.rx.admin.modules.literature.honglou.dto.HonglouCharacterRelationCreateDTO;
import com.rx.admin.modules.literature.honglou.dto.HonglouCharacterRelationUpdateDTO;
import com.rx.admin.modules.literature.honglou.vo.HonglouCharacterRelationVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface HonglouCharacterRelationConvert {

    HonglouCharacterRelation toEntity(HonglouCharacterRelationCreateDTO dto);

    void updateEntity(@MappingTarget HonglouCharacterRelation entity, HonglouCharacterRelationUpdateDTO dto);

    HonglouCharacterRelationVO toVO(HonglouCharacterRelation entity);

    List<HonglouCharacterRelationVO> toVOList(List<HonglouCharacterRelation> list);

    default PageResult<HonglouCharacterRelationVO> toPageResult(Page<HonglouCharacterRelation> page) {
        List<HonglouCharacterRelationVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}