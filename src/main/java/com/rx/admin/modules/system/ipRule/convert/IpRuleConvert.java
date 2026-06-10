package com.rx.admin.modules.system.ipRule.convert;

import com.rx.admin.modules.system.ipRule.dto.IpRuleCreateDTO;
import com.rx.admin.modules.system.ipRule.dto.IpRuleUpdateDTO;
import com.rx.admin.modules.system.ipRule.vo.IpRuleVO;
import com.rx.admin.entity.SysIpRule;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** IP规则对象转换器 */
@Mapper(componentModel = "spring")
public interface IpRuleConvert {
    IpRuleConvert INSTANCE = Mappers.getMapper(IpRuleConvert.class);

    SysIpRule toEntity(IpRuleCreateDTO dto);
    void updateEntity(IpRuleUpdateDTO dto, @MappingTarget SysIpRule entity);
    IpRuleVO toVO(SysIpRule entity);
    List<IpRuleVO> toVOList(List<SysIpRule> list);
}
