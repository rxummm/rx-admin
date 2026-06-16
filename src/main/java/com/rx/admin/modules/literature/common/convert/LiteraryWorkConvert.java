package com.rx.admin.modules.literature.common.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.common.entity.LiteraryWork;
import com.rx.admin.modules.literature.common.dto.LiteraryWorkCreateDTO;
import com.rx.admin.modules.literature.common.dto.LiteraryWorkUpdateDTO;
import com.rx.admin.modules.literature.common.vo.LiteraryWorkVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface LiteraryWorkConvert {

    LiteraryWork toEntity(LiteraryWorkCreateDTO dto);

    void updateEntity(@MappingTarget LiteraryWork entity, LiteraryWorkUpdateDTO dto);

    LiteraryWorkVO toVO(LiteraryWork entity);

    List<LiteraryWorkVO> toVOList(List<LiteraryWork> list);

    default PageResult<LiteraryWorkVO> toPageResult(Page<LiteraryWork> page) {
        List<LiteraryWorkVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}