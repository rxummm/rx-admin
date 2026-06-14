package com.rx.admin.modules.literature.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.framework.datasource.SecondDB;
import com.rx.admin.modules.literature.common.entity.Author;

@SecondDB
public interface AuthorMapper extends BaseMapper<Author> {
}
