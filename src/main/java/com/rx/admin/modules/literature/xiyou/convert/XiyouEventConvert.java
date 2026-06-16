package com.rx.admin.modules.literature.xiyou.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.xiyou.entity.XiyouEvent;
import com.rx.admin.modules.literature.xiyou.dto.XiyouEventCreateDTO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouEventUpdateDTO;
import com.rx.admin.modules.literature.xiyou.vo.XiyouEventVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface XiyouEventConvert {

    XiyouEvent toEntity(XiyouEventCreateDTO dto);

    void updateEntity(@MappingTarget XiyouEvent entity, XiyouEventUpdateDTO dto);

    XiyouEventVO toVO(XiyouEvent entity);

    List<XiyouEventVO> toVOList(List<XiyouEvent> list);

    default PageResult<XiyouEventVO> toPageResult(Page<XiyouEvent> page) {
        List<XiyouEventVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}