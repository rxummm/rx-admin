package com.rx.admin.modules.system.permission.convert;

import com.rx.admin.modules.system.permission.entity.SysPermissionRequest;
import com.rx.admin.modules.system.permission.vo.SysPermissionRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionRequestConvert {
    SysPermissionRequestVO toVO(SysPermissionRequest entity);
    List<SysPermissionRequestVO> toVOList(List<SysPermissionRequest> list);
}
