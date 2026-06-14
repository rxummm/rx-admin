package com.rx.admin.modules.monitor.joblog.convert;

import com.rx.admin.modules.monitor.joblog.entity.SysJobLog;
import com.rx.admin.modules.monitor.joblog.vo.SysJobLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysJobLogConvert {
    SysJobLogVO toVO(SysJobLog entity);
    List<SysJobLogVO> toVOList(List<SysJobLog> list);
}