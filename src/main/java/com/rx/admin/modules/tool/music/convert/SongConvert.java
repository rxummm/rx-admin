package com.rx.admin.modules.tool.music.convert;

import com.rx.admin.modules.tool.music.entity.Song;
import com.rx.admin.modules.tool.music.vo.SongVO;
import com.rx.admin.modules.tool.music.dto.SongCreateDTO;
import com.rx.admin.modules.tool.music.dto.SongUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SongConvert {
    SongVO toVO(Song entity);
    List<SongVO> toVOList(List<Song> list);
    Song toEntity(SongCreateDTO dto);
    void updateEntity(@MappingTarget Song entity, SongUpdateDTO dto);
}