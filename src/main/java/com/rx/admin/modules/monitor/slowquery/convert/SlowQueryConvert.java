package com.rx.admin.modules.monitor.slowquery.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.slowquery.entity.SysSlowQuery;
import com.rx.admin.modules.monitor.slowquery.vo.SlowQueryVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 慢查询对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SlowQueryConvert {

    SlowQueryVO toVO(SysSlowQuery entity);

    List<SlowQueryVO> toVOList(List<SysSlowQuery> list);

    default PageResult<SlowQueryVO> toPageResult(PageResult<SysSlowQuery> pageResult) {
        return pageResult.map(this::toVO);
    }
}
