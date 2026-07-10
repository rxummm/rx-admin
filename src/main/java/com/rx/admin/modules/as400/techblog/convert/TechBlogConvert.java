package com.rx.admin.modules.as400.techblog.convert;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.as400.techblog.dto.TechBlogCreateDTO;
import com.rx.admin.modules.as400.techblog.dto.TechBlogUpdateDTO;
import com.rx.admin.modules.as400.techblog.vo.TechBlogVO;
import com.rx.admin.modules.as400.techblog.entity.TechBlogArticle;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** 技术博客对象转换器 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechBlogConvert {

    TechBlogArticle toEntity(TechBlogCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TechBlogUpdateDTO dto, @MappingTarget TechBlogArticle entity);

    TechBlogVO toVO(TechBlogArticle entity);
    List<TechBlogVO> toVOList(List<TechBlogArticle> list);

    default PageResult<TechBlogVO> toPageResult(PageResult<TechBlogArticle> pageResult) {
        return pageResult.map(this::toVO);
    }
}