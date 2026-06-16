package com.rx.admin.modules.literature.shuihu.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.shuihu.entity.ShuihuPoem;
import com.rx.admin.modules.literature.shuihu.dto.ShuihuPoemCreateDTO;
import com.rx.admin.modules.literature.shuihu.dto.ShuihuPoemUpdateDTO;
import com.rx.admin.modules.literature.shuihu.vo.ShuihuPoemVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface ShuihuPoemConvert {

    ShuihuPoem toEntity(ShuihuPoemCreateDTO dto);

    void updateEntity(@MappingTarget ShuihuPoem entity, ShuihuPoemUpdateDTO dto);

    ShuihuPoemVO toVO(ShuihuPoem entity);

    List<ShuihuPoemVO> toVOList(List<ShuihuPoem> list);

    default PageResult<ShuihuPoemVO> toPageResult(Page<ShuihuPoem> page) {
        List<ShuihuPoemVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}