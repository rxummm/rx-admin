package com.rx.admin.modules.calendar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.calendar.entity.CalendarEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CalendarEventMapper extends BaseMapper<CalendarEvent> {
}
