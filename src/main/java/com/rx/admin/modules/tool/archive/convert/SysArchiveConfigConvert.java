package com.rx.admin.modules.tool.archive.convert;

import com.rx.admin.modules.tool.archive.dto.SysArchiveConfigCreateDTO;
import com.rx.admin.modules.tool.archive.entity.SysArchiveConfig;
import com.rx.admin.modules.tool.archive.vo.SysArchiveConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysArchiveConfigConvert {
    SysArchiveConfig toEntity(SysArchiveConfigCreateDTO dto);
    SysArchiveConfigVO toVO(SysArchiveConfig entity);
    List<SysArchiveConfigVO> toVOList(List<SysArchiveConfig> list);
}
