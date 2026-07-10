package com.rx.admin.modules.tool.wiki.convert;

import com.rx.admin.modules.tool.wiki.dto.WikiSpaceCreateDTO;
import com.rx.admin.modules.tool.wiki.dto.WikiPageCreateDTO;
import com.rx.admin.modules.tool.wiki.entity.WikiSpace;
import com.rx.admin.modules.tool.wiki.entity.WikiPage;
import com.rx.admin.modules.tool.wiki.vo.WikiSpaceVO;
import com.rx.admin.modules.tool.wiki.vo.WikiPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WikiConvert {
    WikiSpace toSpaceEntity(WikiSpaceCreateDTO dto);
    WikiPage toPageEntity(WikiPageCreateDTO dto);
    WikiSpaceVO toSpaceVO(WikiSpace entity);
    WikiPageVO toPageVO(WikiPage entity);
    List<WikiSpaceVO> toSpaceVOList(List<WikiSpace> list);
    List<WikiPageVO> toPageVOList(List<WikiPage> list);
}
