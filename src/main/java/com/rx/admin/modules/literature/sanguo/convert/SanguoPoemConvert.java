package com.rx.admin.modules.literature.sanguo.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.sanguo.entity.SanguoPoem;
import com.rx.admin.modules.literature.sanguo.dto.SanguoPoemCreateDTO;
import com.rx.admin.modules.literature.sanguo.dto.SanguoPoemUpdateDTO;
import com.rx.admin.modules.literature.sanguo.vo.SanguoPoemVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface SanguoPoemConvert {

    SanguoPoem toEntity(SanguoPoemCreateDTO dto);

    void updateEntity(@MappingTarget SanguoPoem entity, SanguoPoemUpdateDTO dto);

    SanguoPoemVO toVO(SanguoPoem entity);

    List<SanguoPoemVO> toVOList(List<SanguoPoem> list);

    default PageResult<SanguoPoemVO> toPageResult(Page<SanguoPoem> page) {
        List<SanguoPoemVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}