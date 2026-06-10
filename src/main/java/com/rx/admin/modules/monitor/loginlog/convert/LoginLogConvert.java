package com.rx.admin.modules.monitor.loginlog.convert;

import com.rx.admin.modules.monitor.loginlog.vo.LoginLogVO;
import com.rx.admin.entity.SysLoginLog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 登录日志对象转换器 */
@Mapper(componentModel = "spring")
public interface LoginLogConvert {
    LoginLogConvert INSTANCE = Mappers.getMapper(LoginLogConvert.class);

    LoginLogVO toVO(SysLoginLog entity);
    List<LoginLogVO> toVOList(List<SysLoginLog> list);
}
