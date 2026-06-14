package com.rx.admin.modules.monitor.slowquery.convert;

import com.rx.admin.modules.monitor.slowquery.vo.SlowQueryVO;
import com.rx.admin.entity.SysSlowQuery;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 慢查询对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SlowQueryConvert {

    SlowQueryVO toVO(SysSlowQuery entity);
    List<SlowQueryVO> toVOList(List<SysSlowQuery> list);
}