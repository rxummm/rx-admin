package com.rx.admin.modules.calendar.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.calendar.dto.CalendarEventCreateDTO;
import com.rx.admin.modules.calendar.dto.CalendarEventUpdateDTO;
import com.rx.admin.modules.calendar.service.ICalendarEventService;
import com.rx.admin.modules.calendar.vo.CalendarEventVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "日历管理")
@RestController
@ApiVersion(1)
@RequestMapping("/calendar/event")
@RequiredArgsConstructor
public class CalendarEventController {

    private final ICalendarEventService calendarEventService;

    @Operation(summary = "事件列表(分页)")
    @GetMapping("/page")
    @SaCheckPermission("tool:calendar:list")
    public Result<PageResult<CalendarEventVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(calendarEventService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "按月查询事件")
    @GetMapping("/month")
    @SaCheckPermission("tool:calendar:list")
    public Result<List<CalendarEventVO>> getByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return Result.ok(calendarEventService.getEventsByMonth(year, month));
    }

    @Operation(summary = "按日期范围查询事件")
    @GetMapping("/range")
    @SaCheckPermission("tool:calendar:list")
    public Result<List<CalendarEventVO>> getByRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.ok(calendarEventService.getEventsByRange(
                LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @Operation(summary = "今日事件")
    @GetMapping("/today")
    @SaCheckPermission("tool:calendar:list")
    public Result<List<CalendarEventVO>> getToday() {
        return Result.ok(calendarEventService.getTodayEvents());
    }

    @Operation(summary = "事件详情")
    @GetMapping("/{id}")
    @SaCheckPermission("tool:calendar:list")
    public Result<CalendarEventVO> getById(@PathVariable Long id) {
        CalendarEventVO vo = calendarEventService.getEventById(id);
        if (vo == null) return Result.fail("事件不存在");
        return Result.ok(vo);
    }

    @Operation(summary = "新增事件")
    @PostMapping
    @SaCheckPermission("tool:calendar:add")
    @OperateLog(module = "日历管理", operation = "新增事件")
    public Result<?> add(@RequestBody @Valid CalendarEventCreateDTO dto) {
        calendarEventService.addEvent(dto);
        return Result.ok();
    }

    @Operation(summary = "修改事件")
    @PutMapping
    @SaCheckPermission("tool:calendar:edit")
    @OperateLog(module = "日历管理", operation = "修改事件")
    public Result<?> update(@RequestBody @Valid CalendarEventUpdateDTO dto) {
        calendarEventService.updateEvent(dto);
        return Result.ok();
    }

    @Operation(summary = "删除事件")
    @DeleteMapping("/{id}")
    @SaCheckPermission("tool:calendar:delete")
    @OperateLog(module = "日历管理", operation = "删除事件")
    public Result<?> delete(@PathVariable Long id) {
        calendarEventService.deleteEvent(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除事件")
    @DeleteMapping("/batch")
    @SaCheckPermission("tool:calendar:delete")
    @OperateLog(module = "日历管理", operation = "批量删除")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        calendarEventService.deleteEventBatch(ids);
        return Result.ok();
    }
}
