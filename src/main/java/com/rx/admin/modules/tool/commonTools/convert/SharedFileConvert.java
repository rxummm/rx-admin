package com.rx.admin.modules.tool.commonTools.convert;

import com.rx.admin.modules.tool.commonTools.entity.SharedFile;
import com.rx.admin.modules.tool.commonTools.vo.SharedFileVO;
import com.rx.admin.modules.tool.commonTools.dto.SharedFileCreateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SharedFileConvert {
    SharedFileVO toVO(SharedFile entity);
    List<SharedFileVO> toVOList(List<SharedFile> list);
    SharedFile toEntity(SharedFileCreateDTO dto);
}