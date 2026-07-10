package com.rx.admin.modules.tool.region.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.tool.region.entity.ChinaRegion;
import com.rx.admin.modules.tool.region.vo.ChinaRegionVO;
import com.rx.admin.modules.tool.region.dto.ChinaRegionCreateDTO;
import com.rx.admin.modules.tool.region.dto.ChinaRegionUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChinaRegionConvert {
    ChinaRegionVO toVO(ChinaRegion entity);
    List<ChinaRegionVO> toVOList(List<ChinaRegion> list);
    ChinaRegion toEntity(ChinaRegionCreateDTO dto);
    void updateEntity(@MappingTarget ChinaRegion entity, ChinaRegionUpdateDTO dto);

    default PageResult<ChinaRegionVO> toPageResult(PageResult<ChinaRegion> pageResult) {
        return pageResult.map(this::toVO);
    }
}