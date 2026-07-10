package com.rx.admin.modules.calendar.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.calendar.entity.CalendarEvent;
import com.rx.admin.modules.calendar.dto.CalendarEventCreateDTO;
import com.rx.admin.modules.calendar.dto.CalendarEventUpdateDTO;
import com.rx.admin.modules.calendar.vo.CalendarEventVO;

import java.time.LocalDate;
import java.util.List;

public interface ICalendarEventService extends IService<CalendarEvent> {

    PageResult<CalendarEventVO> pageQuery(int page, int size, String keyword);

    List<CalendarEventVO> getEventsByMonth(int year, int month);

    List<CalendarEventVO> getEventsByRange(LocalDate startDate, LocalDate endDate);

    List<CalendarEventVO> getTodayEvents();

    CalendarEventVO getEventById(Long id);

    void addEvent(CalendarEventCreateDTO dto);

    void updateEvent(CalendarEventUpdateDTO dto);

    void deleteEvent(Long id);

    void deleteEventBatch(List<Long> ids);
}
