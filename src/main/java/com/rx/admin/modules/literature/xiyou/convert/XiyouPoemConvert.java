package com.rx.admin.modules.literature.xiyou.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.xiyou.entity.XiyouPoem;
import com.rx.admin.modules.literature.xiyou.dto.XiyouPoemCreateDTO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouPoemUpdateDTO;
import com.rx.admin.modules.literature.xiyou.vo.XiyouPoemVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface XiyouPoemConvert {

    XiyouPoem toEntity(XiyouPoemCreateDTO dto);

    void updateEntity(@MappingTarget XiyouPoem entity, XiyouPoemUpdateDTO dto);

    XiyouPoemVO toVO(XiyouPoem entity);

    List<XiyouPoemVO> toVOList(List<XiyouPoem> list);

    default PageResult<XiyouPoemVO> toPageResult(Page<XiyouPoem> page) {
        List<XiyouPoemVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page).map(this::toVO);
    }

    default PageResult<XiyouPoemVO> toPageResult(PageResult<XiyouPoem> pageResult) {
        return pageResult.map(this::toVO);
    }
}