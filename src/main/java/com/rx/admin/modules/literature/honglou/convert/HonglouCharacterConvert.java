package com.rx.admin.modules.literature.honglou.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.honglou.entity.HonglouCharacter;
import com.rx.admin.modules.literature.honglou.dto.HonglouCharacterCreateDTO;
import com.rx.admin.modules.literature.honglou.dto.HonglouCharacterUpdateDTO;
import com.rx.admin.modules.literature.honglou.vo.HonglouCharacterVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HonglouCharacterConvert {

    HonglouCharacter toEntity(HonglouCharacterCreateDTO dto);

    void updateEntity(@MappingTarget HonglouCharacter entity, HonglouCharacterUpdateDTO dto);

    HonglouCharacterVO toVO(HonglouCharacter entity);

    List<HonglouCharacterVO> toVOList(List<HonglouCharacter> list);

    default PageResult<HonglouCharacterVO> toPageResult(Page<HonglouCharacter> page) {
        List<HonglouCharacterVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}