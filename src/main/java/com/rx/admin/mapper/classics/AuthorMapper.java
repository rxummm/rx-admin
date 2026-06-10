package com.rx.admin.mapper.classics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.framework.datasource.SecondDB;
import com.rx.admin.entity.classics.Author;

@SecondDB
public interface AuthorMapper extends BaseMapper<Author> {
}
