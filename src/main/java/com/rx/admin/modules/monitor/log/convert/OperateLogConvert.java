package com.rx.admin.modules.monitor.log.convert;

import com.rx.admin.modules.monitor.log.vo.OperateLogVO;
import com.rx.admin.entity.SysLog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 操作日志对象转换器 */
@Mapper(componentModel = "spring")
public interface OperateLogConvert {
    OperateLogConvert INSTANCE = Mappers.getMapper(OperateLogConvert.class);

    OperateLogVO toVO(SysLog entity);
    List<OperateLogVO> toVOList(List<SysLog> list);
}
