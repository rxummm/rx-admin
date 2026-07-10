package com.rx.admin.modules.literature.honglou.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.honglou.entity.HonglouPoem;
import com.rx.admin.modules.literature.honglou.dto.HonglouPoemCreateDTO;
import com.rx.admin.modules.literature.honglou.dto.HonglouPoemUpdateDTO;
import com.rx.admin.modules.literature.honglou.vo.HonglouPoemVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HonglouPoemConvert {

    HonglouPoem toEntity(HonglouPoemCreateDTO dto);

    void updateEntity(@MappingTarget HonglouPoem entity, HonglouPoemUpdateDTO dto);

    HonglouPoemVO toVO(HonglouPoem entity);

    List<HonglouPoemVO> toVOList(List<HonglouPoem> list);

    default PageResult<HonglouPoemVO> toPageResult(Page<HonglouPoem> page) {
        List<HonglouPoemVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page).map(this::toVO);
    }

    default PageResult<HonglouPoemVO> toPageResult(PageResult<HonglouPoem> pageResult) {
        return pageResult.map(this::toVO);
    }
}