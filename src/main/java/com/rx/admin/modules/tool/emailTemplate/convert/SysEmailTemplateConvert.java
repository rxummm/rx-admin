package com.rx.admin.modules.tool.emailTemplate.convert;

import com.rx.admin.modules.tool.emailTemplate.entity.SysEmailTemplate;
import com.rx.admin.modules.tool.emailTemplate.vo.SysEmailTemplateVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysEmailTemplateConvert {
    SysEmailTemplateVO toVO(SysEmailTemplate entity);
    List<SysEmailTemplateVO> toVOList(List<SysEmailTemplate> list);
}
