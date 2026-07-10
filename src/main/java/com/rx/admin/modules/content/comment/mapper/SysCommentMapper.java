package com.rx.admin.modules.content.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.content.comment.entity.SysComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论 Mapper
 */
@Mapper
public interface SysCommentMapper extends BaseMapper<SysComment> {
}
