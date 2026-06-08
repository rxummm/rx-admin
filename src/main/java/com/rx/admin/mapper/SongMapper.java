package com.rx.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.entity.Song;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SongMapper extends BaseMapper<Song> {
}
