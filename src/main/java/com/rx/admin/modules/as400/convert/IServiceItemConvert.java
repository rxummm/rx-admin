package com.rx.admin.modules.as400.convert;

import com.rx.admin.modules.as400.entity.IServiceItem;
import com.rx.admin.modules.as400.vo.IServiceItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IServiceItemConvert {
    IServiceItemVO toVO(IServiceItem entity);
    List<IServiceItemVO> toVOList(List<IServiceItem> list);
}