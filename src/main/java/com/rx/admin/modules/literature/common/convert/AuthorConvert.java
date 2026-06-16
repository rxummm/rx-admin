package com.rx.admin.modules.literature.common.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.literature.common.entity.Author;
import com.rx.admin.modules.literature.common.dto.AuthorCreateDTO;
import com.rx.admin.modules.literature.common.dto.AuthorUpdateDTO;
import com.rx.admin.modules.literature.common.vo.AuthorVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings("null")
public interface AuthorConvert {

    Author toEntity(AuthorCreateDTO dto);

    void updateEntity(@MappingTarget Author entity, AuthorUpdateDTO dto);

    AuthorVO toVO(Author entity);

    List<AuthorVO> toVOList(List<Author> list);

    default PageResult<AuthorVO> toPageResult(Page<Author> page) {
        List<AuthorVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}