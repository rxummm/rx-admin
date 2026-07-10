package com.rx.admin.modules.monitor.exportlog.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.exportlog.entity.SysExportLog;
import com.rx.admin.modules.monitor.exportlog.vo.SysExportLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysExportLogConvert {
    SysExportLogVO toVO(SysExportLog entity);
    List<SysExportLogVO> toVOList(List<SysExportLog> list);

    default PageResult<SysExportLogVO> toPageResult(PageResult<SysExportLog> pageResult) {
        return pageResult.map(this::toVO);
    }
}
