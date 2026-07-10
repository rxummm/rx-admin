package com.rx.admin.modules.calendar.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.calendar.entity.CalendarEvent;
import com.rx.admin.modules.calendar.dto.CalendarEventCreateDTO;
import com.rx.admin.modules.calendar.dto.CalendarEventUpdateDTO;
import com.rx.admin.modules.calendar.vo.CalendarEventVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CalendarEventConvert {

    CalendarEvent toEntity(CalendarEventCreateDTO dto);

    void updateEntity(@MappingTarget CalendarEvent entity, CalendarEventUpdateDTO dto);

    CalendarEventVO toVO(CalendarEvent entity);

    default PageResult<CalendarEventVO> toPageResult(Page<CalendarEvent> page) {
        List<CalendarEventVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page).map(this::toVO);
    }

    default PageResult<CalendarEventVO> toPageResult(PageResult<CalendarEvent> pageResult) {
        return pageResult.map(this::toVO);
    }
}
