package com.rx.admin.mapper.classics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.framework.datasource.SecondDB;
import com.rx.admin.entity.classics.Genre;

@SecondDB
public interface GenreMapper extends BaseMapper<Genre> {
}
