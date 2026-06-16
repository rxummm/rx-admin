package com.rx.admin.modules.literature.xiyou.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.xiyou.entity.XiyouCharacter;
import com.rx.admin.modules.literature.xiyou.dto.XiyouCharacterCreateDTO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouCharacterUpdateDTO;
import com.rx.admin.modules.literature.xiyou.vo.XiyouCharacterVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface XiyouCharacterConvert {

    XiyouCharacter toEntity(XiyouCharacterCreateDTO dto);

    void updateEntity(@MappingTarget XiyouCharacter entity, XiyouCharacterUpdateDTO dto);

    XiyouCharacterVO toVO(XiyouCharacter entity);

    List<XiyouCharacterVO> toVOList(List<XiyouCharacter> list);

    default PageResult<XiyouCharacterVO> toPageResult(Page<XiyouCharacter> page) {
        List<XiyouCharacterVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}