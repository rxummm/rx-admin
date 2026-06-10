package com.rx.admin.modules.system.config.convert;

import com.rx.admin.modules.system.config.dto.ConfigCreateDTO;
import com.rx.admin.modules.system.config.dto.ConfigUpdateDTO;
import com.rx.admin.modules.system.config.vo.ConfigVO;
import com.rx.admin.entity.SysConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 系统配置对象转换器 */
@Mapper(componentModel = "spring")
public interface ConfigConvert {
    ConfigConvert INSTANCE = Mappers.getMapper(ConfigConvert.class);

    SysConfig toEntity(ConfigCreateDTO dto);
    void updateEntity(ConfigUpdateDTO dto, @MappingTarget SysConfig entity);
    ConfigVO toVO(SysConfig entity);
    List<ConfigVO> toVOList(List<SysConfig> list);
}
