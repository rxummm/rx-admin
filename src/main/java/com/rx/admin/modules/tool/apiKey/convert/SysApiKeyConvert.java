package com.rx.admin.modules.tool.apiKey.convert;

import com.rx.admin.modules.tool.apiKey.entity.SysApiKey;
import com.rx.admin.modules.tool.apiKey.vo.SysApiKeyVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysApiKeyConvert {
    SysApiKeyVO toVO(SysApiKey entity);
    List<SysApiKeyVO> toVOList(List<SysApiKey> list);
}
