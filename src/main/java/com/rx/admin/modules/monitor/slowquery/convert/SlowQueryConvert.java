package com.rx.admin.modules.monitor.slowquery.convert;

import com.rx.admin.modules.monitor.slowquery.vo.SlowQueryVO;
import com.rx.admin.entity.SysSlowQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 慢查询对象转换器 */
@Mapper(componentModel = "spring")
public interface SlowQueryConvert {
    SlowQueryConvert INSTANCE = Mappers.getMapper(SlowQueryConvert.class);

    SlowQueryVO toVO(SysSlowQuery entity);
    List<SlowQueryVO> toVOList(List<SysSlowQuery> list);
}
