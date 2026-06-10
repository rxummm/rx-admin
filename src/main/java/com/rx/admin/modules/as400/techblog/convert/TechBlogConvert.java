package com.rx.admin.modules.as400.techblog.convert;

import com.rx.admin.modules.as400.techblog.dto.TechBlogCreateDTO;
import com.rx.admin.modules.as400.techblog.dto.TechBlogUpdateDTO;
import com.rx.admin.modules.as400.techblog.vo.TechBlogVO;
import com.rx.admin.entity.TechBlogArticle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 技术博客对象转换器 */
@Mapper(componentModel = "spring")
public interface TechBlogConvert {
    TechBlogConvert INSTANCE = Mappers.getMapper(TechBlogConvert.class);

    TechBlogArticle toEntity(TechBlogCreateDTO dto);
    void updateEntity(TechBlogUpdateDTO dto, @MappingTarget TechBlogArticle entity);
    TechBlogVO toVO(TechBlogArticle entity);
    List<TechBlogVO> toVOList(List<TechBlogArticle> list);
}
