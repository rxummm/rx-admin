package com.rx.admin.modules.calendar.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.calendar.entity.CalendarEvent;
import com.rx.admin.modules.calendar.mapper.CalendarEventMapper;
import com.rx.admin.modules.calendar.dto.CalendarEventCreateDTO;
import com.rx.admin.modules.calendar.dto.CalendarEventUpdateDTO;
import com.rx.admin.modules.calendar.convert.CalendarEventConvert;
import com.rx.admin.modules.calendar.vo.CalendarEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarEventService extends ServiceImpl<CalendarEventMapper, CalendarEvent> implements ICalendarEventService {

    private final CalendarEventConvert calendarEventConvert;

    @Override
    public PageResult<CalendarEventVO> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<CalendarEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvent::getUserId, StpUtil.getLoginIdAsLong());
        if (StringUtils.hasText(keyword)) {
            wrapper.like(CalendarEvent::getTitle, keyword);
        }
        wrapper.orderByDesc(CalendarEvent::getEventDate);
        IPage<CalendarEvent> iPage = page(new Page<>(page, size), wrapper);
        return PageResult.of(iPage).map(calendarEventConvert::toVO);
    }

    @Override
    @Cacheable(value = "calendar", key = "'month:'+#year+':'+#month+':'+T(cn.dev33.satoken.stp.StpUtil).getLoginIdAsLong()")
    public List<CalendarEventVO> getEventsByMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        LambdaQueryWrapper<CalendarEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvent::getUserId, StpUtil.getLoginIdAsLong());
        wrapper.between(CalendarEvent::getEventDate, start, end);
        wrapper.orderByAsc(CalendarEvent::getEventDate);
        return baseMapper.selectList(wrapper).stream()
                .map(calendarEventConvert::toVO)
                .toList();
    }

    @Override
    @Cacheable(value = "calendar", key = "'range:'+#startDate+':'+#endDate+':'+T(cn.dev33.satoken.stp.StpUtil).getLoginIdAsLong()")
    public List<CalendarEventVO> getEventsByRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CalendarEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvent::getUserId, StpUtil.getLoginIdAsLong());
        wrapper.between(CalendarEvent::getEventDate, startDate, endDate);
        wrapper.orderByAsc(CalendarEvent::getEventDate);
        return baseMapper.selectList(wrapper).stream()
                .map(calendarEventConvert::toVO)
                .toList();
    }

    @Override
    @Cacheable(value = "calendar", key = "'today:'+T(cn.dev33.satoken.stp.StpUtil).getLoginIdAsLong()")
    public List<CalendarEventVO> getTodayEvents() {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<CalendarEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvent::getUserId, StpUtil.getLoginIdAsLong());
        wrapper.eq(CalendarEvent::getEventDate, today);
        wrapper.orderByAsc(CalendarEvent::getStartTime);
        return baseMapper.selectList(wrapper).stream()
                .map(calendarEventConvert::toVO)
                .toList();
    }

    @Override
    @Cacheable(value = "calendar", key = "'event:'+#id")
    public CalendarEventVO getEventById(Long id) {
        CalendarEvent event = getById(id);
        if (event == null) return null;
        return calendarEventConvert.toVO(event);
    }

    @Override
    @CacheEvict(value = "calendar", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void addEvent(CalendarEventCreateDTO dto) {
        CalendarEvent event = calendarEventConvert.toEntity(dto);
        event.setUserId(StpUtil.getLoginIdAsLong());
        if (event.getEventType() == null) {
            event.setEventType("EVENT");
        }
        if (event.getPriority() == null) {
            event.setPriority(0);
        }
        if (event.getIsAllDay() == null) {
            event.setIsAllDay(true);
        }
        if (event.getStatus() == null) {
            event.setStatus(0);
        }
        save(event);
    }

    @Override
    @CacheEvict(value = "calendar", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateEvent(CalendarEventUpdateDTO dto) {
        CalendarEvent existing = getById(dto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        if (!existing.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            throw new IllegalArgumentException("无权修改该事件");
        }
        calendarEventConvert.updateEntity(existing, dto);
        updateById(existing);
    }

    @Override
    @CacheEvict(value = "calendar", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteEvent(Long id) {
        CalendarEvent existing = getById(id);
        if (existing == null) return;
        if (!existing.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            throw new IllegalArgumentException("无权删除该事件");
        }
        removeById(id);
    }

    @Override
    @CacheEvict(value = "calendar", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteEventBatch(List<Long> ids) {
        removeBatchByIds(ids);
    }
}
