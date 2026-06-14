package com.rx.admin.modules.literature.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.framework.datasource.SecondDB;
import com.rx.admin.modules.literature.common.entity.LiteraryWork;

@SecondDB
public interface LiteraryWorkMapper extends BaseMapper<LiteraryWork> {
}
