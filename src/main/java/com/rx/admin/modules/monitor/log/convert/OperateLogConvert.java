package com.rx.admin.modules.monitor.log.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import com.rx.admin.modules.monitor.log.vo.OperateLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 操作日志对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OperateLogConvert {

    OperateLogVO toVO(SysLog entity);

    List<OperateLogVO> toVOList(List<SysLog> list);

    default PageResult<OperateLogVO> toPageResult(PageResult<SysLog> pageResult) {
        return pageResult.map(this::toVO);
    }
}
