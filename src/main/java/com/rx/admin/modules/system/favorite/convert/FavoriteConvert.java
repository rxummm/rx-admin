package com.rx.admin.modules.system.favorite.convert;

import com.rx.admin.modules.system.favorite.dto.FavoriteCreateDTO;
import com.rx.admin.modules.system.favorite.vo.FavoriteVO;
import com.rx.admin.entity.SysUserFavorite;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 收藏对象转换器 */
@Mapper(componentModel = "spring")
public interface FavoriteConvert {
    FavoriteConvert INSTANCE = Mappers.getMapper(FavoriteConvert.class);

    SysUserFavorite toEntity(FavoriteCreateDTO dto);
    FavoriteVO toVO(SysUserFavorite entity);
    List<FavoriteVO> toVOList(List<SysUserFavorite> list);
}
