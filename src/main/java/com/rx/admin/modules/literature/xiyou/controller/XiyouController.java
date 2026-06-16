package com.rx.admin.modules.literature.xiyou.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.literature.xiyou.vo.XiyouPoemVO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouPoemCreateDTO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouPoemUpdateDTO;
import com.rx.admin.modules.literature.xiyou.convert.XiyouPoemConvert;
import com.rx.admin.modules.literature.xiyou.vo.XiyouCharacterVO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouCharacterCreateDTO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouCharacterUpdateDTO;
import com.rx.admin.modules.literature.xiyou.convert.XiyouCharacterConvert;
import com.rx.admin.modules.literature.xiyou.vo.XiyouEventVO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouEventCreateDTO;
import com.rx.admin.modules.literature.xiyou.dto.XiyouEventUpdateDTO;
import com.rx.admin.modules.literature.xiyou.convert.XiyouEventConvert;
import com.rx.admin.modules.literature.xiyou.service.XiyouCharacterService;
import com.rx.admin.modules.literature.xiyou.service.XiyouEventService;
import com.rx.admin.modules.literature.xiyou.entity.XiyouCharacter;
import com.rx.admin.modules.literature.xiyou.entity.XiyouEvent;
import com.rx.admin.modules.literature.xiyou.entity.XiyouPoem;
import com.rx.admin.modules.literature.xiyou.service.XiyouPoemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "西游记数据")
@RestController
@RequestMapping("/api/classics/xiyou")
public class XiyouController {


    private final XiyouPoemService poemService;
    private final XiyouPoemConvert poemConvert;
    private final XiyouCharacterService characterService;
    private final XiyouCharacterConvert characterConvert;
    private final XiyouEventService eventService;
    private final XiyouEventConvert eventConvert;

    public XiyouController(XiyouPoemService poemService, XiyouPoemConvert poemConvert, XiyouCharacterService characterService, XiyouCharacterConvert characterConvert, XiyouEventService eventService, XiyouEventConvert eventConvert) {
        this.poemService = poemService;
        this.poemConvert = poemConvert;
        this.characterService = characterService;
        this.characterConvert = characterConvert;
        this.eventService = eventService;
        this.eventConvert = eventConvert;
    }

    // ====== 西游诗词 ======

    @Operation(summary = "XiyouPoem分页查询")
    @GetMapping("/poem/page")
    @SaCheckLogin
    public Result<PageResult<XiyouPoemVO>> poemServicePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<XiyouPoem> pr = poemService.pageQuery(page, size, keyword);
        List<XiyouPoemVO> voList = poemConvert.toVOList(pr.getRecords());
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), voList));
    }

    @Operation(summary = "XiyouPoem详情")
    @GetMapping("/poem/{id}")
    @SaCheckLogin
    public Result<XiyouPoemVO> poemServiceDetail(@PathVariable Long id) {
        return Result.ok(poemConvert.toVO(poemService.getById(id)));
    }

    @Operation(summary = "获取所有XiyouPoem")
    @GetMapping("/poem/all")
    @SaCheckLogin
    public Result<List<XiyouPoemVO>> poemServiceAll() {
        return Result.ok(poemConvert.toVOList(poemService.list()));
    }

    @Operation(summary = "新增XiyouPoem")
    @PostMapping("/poem")
    @SaCheckPermission("classics:xiyou:poem:add")
    @OperateLog(module = "西游记数据", operation = "新增XiyouPoem")
    public Result<?> poemServiceAdd(@RequestBody @Valid XiyouPoemCreateDTO dto) {
        poemService.save(poemConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑XiyouPoem")
    @PutMapping("/poem")
    @SaCheckPermission("classics:xiyou:poem:edit")
    @OperateLog(module = "西游记数据", operation = "编辑XiyouPoem")
    public Result<?> poemServiceUpdate(@RequestBody @Valid XiyouPoemUpdateDTO dto) {
        XiyouPoem entity = poemService.getById(dto.getId());
        poemConvert.updateEntity(entity, dto);
        poemService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除XiyouPoem")
    @DeleteMapping("/poem/{id}")
    @SaCheckPermission("classics:xiyou:poem:delete")
    @OperateLog(module = "西游记数据", operation = "删除XiyouPoem")
    public Result<?> poemServiceDelete(@PathVariable Long id) {
        poemService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除XiyouPoem")
    @DeleteMapping("/poem/batch")
    @SaCheckPermission("classics:xiyou:poem:delete")
    public Result<?> poemServiceBatchDelete(@RequestBody List<Long> ids) {
        poemService.removeByIds(ids);
        return Result.ok();
    }
    // ====== 西游人物 ======

    @Operation(summary = "XiyouCharacter分页查询")
    @GetMapping("/character/page")
    @SaCheckLogin
    public Result<PageResult<XiyouCharacterVO>> characterServicePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<XiyouCharacter> pr = characterService.pageQuery(page, size, keyword);
        List<XiyouCharacterVO> voList = characterConvert.toVOList(pr.getRecords());
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), voList));
    }

    @Operation(summary = "XiyouCharacter详情")
    @GetMapping("/character/{id}")
    @SaCheckLogin
    public Result<XiyouCharacterVO> characterServiceDetail(@PathVariable Long id) {
        return Result.ok(characterConvert.toVO(characterService.getById(id)));
    }

    @Operation(summary = "获取所有XiyouCharacter")
    @GetMapping("/character/all")
    @SaCheckLogin
    public Result<List<XiyouCharacterVO>> characterServiceAll() {
        return Result.ok(characterConvert.toVOList(characterService.list()));
    }

    @Operation(summary = "新增XiyouCharacter")
    @PostMapping("/character")
    @SaCheckPermission("classics:xiyou:character:add")
    @OperateLog(module = "西游记数据", operation = "新增XiyouCharacter")
    public Result<?> characterServiceAdd(@RequestBody @Valid XiyouCharacterCreateDTO dto) {
        characterService.save(characterConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑XiyouCharacter")
    @PutMapping("/character")
    @SaCheckPermission("classics:xiyou:character:edit")
    @OperateLog(module = "西游记数据", operation = "编辑XiyouCharacter")
    public Result<?> characterServiceUpdate(@RequestBody @Valid XiyouCharacterUpdateDTO dto) {
        XiyouCharacter entity = characterService.getById(dto.getId());
        characterConvert.updateEntity(entity, dto);
        characterService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除XiyouCharacter")
    @DeleteMapping("/character/{id}")
    @SaCheckPermission("classics:xiyou:character:delete")
    @OperateLog(module = "西游记数据", operation = "删除XiyouCharacter")
    public Result<?> characterServiceDelete(@PathVariable Long id) {
        characterService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除XiyouCharacter")
    @DeleteMapping("/character/batch")
    @SaCheckPermission("classics:xiyou:character:delete")
    public Result<?> characterServiceBatchDelete(@RequestBody List<Long> ids) {
        characterService.removeByIds(ids);
        return Result.ok();
    }
    @Operation(summary = "按种族筛选西游人物")
    @GetMapping("/character/race")
    @SaCheckLogin
    public Result<List<XiyouCharacterVO>> characterByRace(@RequestParam(required = false) String race) {
        return Result.ok(characterConvert.toVOList(characterService.listByRace(race)));
    }
    // ====== 西游事件 ======

    @Operation(summary = "XiyouEvent分页查询")
    @GetMapping("/event/page")
    @SaCheckLogin
    public Result<PageResult<XiyouEventVO>> eventServicePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<XiyouEvent> pr = eventService.pageQuery(page, size, keyword);
        List<XiyouEventVO> voList = eventConvert.toVOList(pr.getRecords());
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), voList));
    }

    @Operation(summary = "XiyouEvent详情")
    @GetMapping("/event/{id}")
    @SaCheckLogin
    public Result<XiyouEventVO> eventServiceDetail(@PathVariable Long id) {
        return Result.ok(eventConvert.toVO(eventService.getById(id)));
    }

    @Operation(summary = "获取所有XiyouEvent")
    @GetMapping("/event/all")
    @SaCheckLogin
    public Result<List<XiyouEventVO>> eventServiceAll() {
        return Result.ok(eventConvert.toVOList(eventService.listAll()));
    }

    @Operation(summary = "新增XiyouEvent")
    @PostMapping("/event")
    @SaCheckPermission("classics:xiyou:event:add")
    @OperateLog(module = "西游记数据", operation = "新增XiyouEvent")
    public Result<?> eventServiceAdd(@RequestBody @Valid XiyouEventCreateDTO dto) {
        eventService.save(eventConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑XiyouEvent")
    @PutMapping("/event")
    @SaCheckPermission("classics:xiyou:event:edit")
    @OperateLog(module = "西游记数据", operation = "编辑XiyouEvent")
    public Result<?> eventServiceUpdate(@RequestBody @Valid XiyouEventUpdateDTO dto) {
        XiyouEvent entity = eventService.getById(dto.getId());
        eventConvert.updateEntity(entity, dto);
        eventService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除XiyouEvent")
    @DeleteMapping("/event/{id}")
    @SaCheckPermission("classics:xiyou:event:delete")
    @OperateLog(module = "西游记数据", operation = "删除XiyouEvent")
    public Result<?> eventServiceDelete(@PathVariable Long id) {
        eventService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除XiyouEvent")
    @DeleteMapping("/event/batch")
    @SaCheckPermission("classics:xiyou:event:delete")
    public Result<?> eventServiceBatchDelete(@RequestBody List<Long> ids) {
        eventService.removeByIds(ids);
        return Result.ok();
    }
}