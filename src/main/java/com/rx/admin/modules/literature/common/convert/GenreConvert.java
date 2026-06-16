package com.rx.admin.modules.literature.common.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.common.entity.Genre;
import com.rx.admin.modules.literature.common.dto.GenreCreateDTO;
import com.rx.admin.modules.literature.common.dto.GenreUpdateDTO;
import com.rx.admin.modules.literature.common.vo.GenreVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface GenreConvert {

    Genre toEntity(GenreCreateDTO dto);

    void updateEntity(@MappingTarget Genre entity, GenreUpdateDTO dto);

    GenreVO toVO(Genre entity);

    List<GenreVO> toVOList(List<Genre> list);

    default PageResult<GenreVO> toPageResult(Page<Genre> page) {
        List<GenreVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}