package com.rx.admin.modules.as400.convert;

import com.rx.admin.modules.as400.entity.IServiceCategory;
import com.rx.admin.modules.as400.vo.IServiceCategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IServiceCategoryConvert {
    IServiceCategoryVO toVO(IServiceCategory entity);
    List<IServiceCategoryVO> toVOList(List<IServiceCategory> list);
}