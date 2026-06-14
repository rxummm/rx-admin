package com.rx.admin.modules.literature.sanguo.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.sanguo.entity.SanguoCharacter;
import com.rx.admin.modules.literature.sanguo.dto.SanguoCharacterCreateDTO;
import com.rx.admin.modules.literature.sanguo.dto.SanguoCharacterUpdateDTO;
import com.rx.admin.modules.literature.sanguo.vo.SanguoCharacterVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SanguoCharacterConvert {

    SanguoCharacter toEntity(SanguoCharacterCreateDTO dto);

    void updateEntity(@MappingTarget SanguoCharacter entity, SanguoCharacterUpdateDTO dto);

    SanguoCharacterVO toVO(SanguoCharacter entity);

    List<SanguoCharacterVO> toVOList(List<SanguoCharacter> list);

    default PageResult<SanguoCharacterVO> toPageResult(Page<SanguoCharacter> page) {
        List<SanguoCharacterVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}