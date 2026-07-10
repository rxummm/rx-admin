package com.rx.admin.modules.monitor.loginlog.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.loginlog.vo.LoginLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 登录日志对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoginLogConvert {

    LoginLogVO toVO(SysLoginLog entity);

    List<LoginLogVO> toVOList(List<SysLoginLog> list);

    default PageResult<LoginLogVO> toPageResult(PageResult<SysLoginLog> pageResult) {
        return pageResult.map(this::toVO);
    }
}
