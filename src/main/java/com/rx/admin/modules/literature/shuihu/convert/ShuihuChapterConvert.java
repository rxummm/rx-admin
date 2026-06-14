package com.rx.admin.modules.literature.shuihu.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.shuihu.entity.ShuihuChapter;
import com.rx.admin.modules.literature.shuihu.dto.ShuihuChapterCreateDTO;
import com.rx.admin.modules.literature.shuihu.dto.ShuihuChapterUpdateDTO;
import com.rx.admin.modules.literature.shuihu.vo.ShuihuChapterVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShuihuChapterConvert {

    ShuihuChapter toEntity(ShuihuChapterCreateDTO dto);

    void updateEntity(@MappingTarget ShuihuChapter entity, ShuihuChapterUpdateDTO dto);

    ShuihuChapterVO toVO(ShuihuChapter entity);

    List<ShuihuChapterVO> toVOList(List<ShuihuChapter> list);

    default PageResult<ShuihuChapterVO> toPageResult(Page<ShuihuChapter> page) {
        List<ShuihuChapterVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}