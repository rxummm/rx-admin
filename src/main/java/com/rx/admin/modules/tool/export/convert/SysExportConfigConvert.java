package com.rx.admin.modules.tool.export.convert;

import com.rx.admin.modules.tool.export.entity.SysExportConfig;
import com.rx.admin.modules.tool.export.vo.SysExportConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysExportConfigConvert {
    SysExportConfigVO toVO(SysExportConfig entity);
    List<SysExportConfigVO> toVOList(List<SysExportConfig> list);
}