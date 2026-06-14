package com.rx.admin.modules.system.iprule.convert;

import com.rx.admin.modules.system.iprule.dto.IpRuleCreateDTO;
import com.rx.admin.modules.system.iprule.dto.IpRuleUpdateDTO;
import com.rx.admin.modules.system.iprule.vo.IpRuleVO;
import com.rx.admin.entity.SysIpRule;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IpRuleConvert {

    SysIpRule toEntity(IpRuleCreateDTO dto);
    void updateEntity(IpRuleUpdateDTO dto, @MappingTarget SysIpRule entity);
    IpRuleVO toVO(SysIpRule entity);
    List<IpRuleVO> toVOList(List<SysIpRule> list);
}