package com.rx.admin.modules.tool.kanban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.tool.kanban.entity.KanbanDependency;
import org.apache.ibatis.annotations.Mapper;

/**
 * 看板任务依赖 Mapper
 */
@Mapper
public interface KanbanDependencyMapper extends BaseMapper<KanbanDependency> {
}
