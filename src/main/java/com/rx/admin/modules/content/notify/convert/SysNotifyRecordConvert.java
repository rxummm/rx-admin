package com.rx.admin.modules.content.notify.convert;

import com.rx.admin.modules.content.notify.entity.SysNotifyRecord;
import com.rx.admin.modules.content.notify.vo.SysNotifyRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysNotifyRecordConvert {
    SysNotifyRecordVO toVO(SysNotifyRecord entity);
    List<SysNotifyRecordVO> toVOList(List<SysNotifyRecord> list);
}