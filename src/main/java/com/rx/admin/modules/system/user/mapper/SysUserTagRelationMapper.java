package com.rx.admin.modules.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.system.user.entity.SysUserTagRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户标签关联 Mapper
 */
@Mapper
public interface SysUserTagRelationMapper extends BaseMapper<SysUserTagRelation> {
}
