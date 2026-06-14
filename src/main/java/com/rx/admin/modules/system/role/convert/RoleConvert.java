package com.rx.admin.modules.system.role.convert;

import com.rx.admin.modules.system.role.dto.RoleCreateDTO;
import com.rx.admin.modules.system.role.dto.RoleUpdateDTO;
import com.rx.admin.modules.system.role.vo.RoleVO;
import com.rx.admin.modules.system.role.entity.SysRole;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 角色对象转换器（MapStruct编译期生成实现） */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleConvert {

    SysRole toEntity(RoleCreateDTO dto);
    void updateEntity(RoleUpdateDTO dto, @MappingTarget SysRole entity);
    RoleVO toVO(SysRole entity);
    List<RoleVO> toVOList(List<SysRole> list);
}