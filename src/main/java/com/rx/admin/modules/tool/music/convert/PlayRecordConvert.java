package com.rx.admin.modules.tool.music.convert;

import com.rx.admin.modules.tool.music.entity.PlayRecord;
import com.rx.admin.modules.tool.music.vo.PlayRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayRecordConvert {
    PlayRecordVO toVO(PlayRecord entity);
    List<PlayRecordVO> toVOList(List<PlayRecord> list);
}