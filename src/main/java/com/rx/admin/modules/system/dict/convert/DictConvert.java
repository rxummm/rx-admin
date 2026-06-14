package com.rx.admin.modules.system.dict.convert;

import com.rx.admin.modules.system.dict.dto.DictDataCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictDataUpdateDTO;
import com.rx.admin.modules.system.dict.dto.DictTypeCreateDTO;
import com.rx.admin.modules.system.dict.dto.DictTypeUpdateDTO;
import com.rx.admin.modules.system.dict.vo.DictDataVO;
import com.rx.admin.modules.system.dict.vo.DictTypeVO;
import com.rx.admin.entity.SysDictData;
import com.rx.admin.entity.SysDictType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 字典对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictConvert {

    // 字典类型
    SysDictType toEntity(DictTypeCreateDTO dto);
    void updateTypeEntity(DictTypeUpdateDTO dto, @MappingTarget SysDictType entity);
    DictTypeVO toTypeVO(SysDictType entity);
    List<DictTypeVO> toTypeVOList(List<SysDictType> list);

    // 字典数据
    SysDictData toDataEntity(DictDataCreateDTO dto);
    void updateDataEntity(DictDataUpdateDTO dto, @MappingTarget SysDictData entity);
    DictDataVO toDataVO(SysDictData entity);
    List<DictDataVO> toDataVOList(List<SysDictData> list);
}