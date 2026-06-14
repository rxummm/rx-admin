package com.rx.admin.modules.content.notice.convert;

import com.rx.admin.modules.content.notice.dto.NoticeCreateDTO;
import com.rx.admin.modules.content.notice.dto.NoticeUpdateDTO;
import com.rx.admin.modules.content.notice.vo.NoticeVO;
import com.rx.admin.entity.SysNotice;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 通知公告对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoticeConvert {

    SysNotice toEntity(NoticeCreateDTO dto);
    void updateEntity(NoticeUpdateDTO dto, @MappingTarget SysNotice entity);
    NoticeVO toVO(SysNotice entity);
    List<NoticeVO> toVOList(List<SysNotice> list);
}