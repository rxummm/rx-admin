package com.rx.admin.modules.system.i18n.convert;

import com.rx.admin.modules.system.i18n.entity.SysI18nKey;
import com.rx.admin.modules.system.i18n.vo.SysI18nKeyVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysI18nConvert {
    SysI18nKeyVO toKeyVO(SysI18nKey entity);
    List<SysI18nKeyVO> toKeyVOList(List<SysI18nKey> list);
}
