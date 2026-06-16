package com.rx.admin.modules.literature.common.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.common.entity.Dynasty;
import com.rx.admin.modules.literature.common.dto.DynastyCreateDTO;
import com.rx.admin.modules.literature.common.dto.DynastyUpdateDTO;
import com.rx.admin.modules.literature.common.vo.DynastyVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface DynastyConvert {

    Dynasty toEntity(DynastyCreateDTO dto);

    void updateEntity(@MappingTarget Dynasty entity, DynastyUpdateDTO dto);

    DynastyVO toVO(Dynasty entity);

    List<DynastyVO> toVOList(List<Dynasty> list);

    default PageResult<DynastyVO> toPageResult(Page<Dynasty> page) {
        List<DynastyVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}