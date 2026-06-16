package com.rx.admin.modules.literature.common.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.common.entity.ContentCategory;
import com.rx.admin.modules.literature.common.dto.ContentCategoryCreateDTO;
import com.rx.admin.modules.literature.common.dto.ContentCategoryUpdateDTO;
import com.rx.admin.modules.literature.common.vo.ContentCategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface ContentCategoryConvert {

    ContentCategory toEntity(ContentCategoryCreateDTO dto);

    void updateEntity(@MappingTarget ContentCategory entity, ContentCategoryUpdateDTO dto);

    ContentCategoryVO toVO(ContentCategory entity);

    List<ContentCategoryVO> toVOList(List<ContentCategory> list);

    default PageResult<ContentCategoryVO> toPageResult(Page<ContentCategory> page) {
        List<ContentCategoryVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}