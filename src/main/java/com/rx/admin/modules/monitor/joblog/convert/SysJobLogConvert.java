package com.rx.admin.modules.monitor.joblog.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.joblog.entity.SysJobLog;
import com.rx.admin.modules.monitor.joblog.vo.SysJobLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 定时任务日志对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysJobLogConvert {

    SysJobLogVO toVO(SysJobLog entity);

    List<SysJobLogVO> toVOList(List<SysJobLog> list);

    default PageResult<SysJobLogVO> toPageResult(PageResult<SysJobLog> pageResult) {
        return pageResult.map(this::toVO);
    }
}
