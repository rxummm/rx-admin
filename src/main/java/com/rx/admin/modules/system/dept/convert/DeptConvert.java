package com.rx.admin.modules.system.dept.convert;

import com.rx.admin.modules.system.dept.dto.DeptCreateDTO;
import com.rx.admin.modules.system.dept.dto.DeptUpdateDTO;
import com.rx.admin.modules.system.dept.vo.DeptVO;
import com.rx.admin.entity.SysDept;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 部门对象转换器 */
@Mapper(componentModel = "spring")
public interface DeptConvert {
    DeptConvert INSTANCE = Mappers.getMapper(DeptConvert.class);

    SysDept toEntity(DeptCreateDTO dto);
    void updateEntity(DeptUpdateDTO dto, @MappingTarget SysDept entity);
    DeptVO toVO(SysDept entity);
    List<DeptVO> toVOList(List<SysDept> list);
}
