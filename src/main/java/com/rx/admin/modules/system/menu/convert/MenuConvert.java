package com.rx.admin.modules.system.menu.convert;

import com.rx.admin.modules.system.menu.dto.MenuCreateDTO;
import com.rx.admin.modules.system.menu.dto.MenuUpdateDTO;
import com.rx.admin.modules.system.menu.vo.MenuVO;
import com.rx.admin.entity.SysMenu;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 菜单对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuConvert {

    SysMenu toEntity(MenuCreateDTO dto);
    void updateEntity(MenuUpdateDTO dto, @MappingTarget SysMenu entity);
    MenuVO toVO(SysMenu entity);
    List<MenuVO> toVOList(List<SysMenu> list);
}