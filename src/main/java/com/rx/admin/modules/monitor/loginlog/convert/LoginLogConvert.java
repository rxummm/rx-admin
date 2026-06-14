package com.rx.admin.modules.monitor.loginlog.convert;

import com.rx.admin.modules.monitor.loginlog.vo.LoginLogVO;
import com.rx.admin.entity.SysLoginLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 登录日志对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoginLogConvert {

    LoginLogVO toVO(SysLoginLog entity);
    List<LoginLogVO> toVOList(List<SysLoginLog> list);
}