package com.rx.admin.controller.classics;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.classics.XiyouCharacter;
import com.rx.admin.entity.classics.XiyouEvent;
import com.rx.admin.entity.classics.XiyouPoem;
import com.rx.admin.service.classics.XiyouCharacterService;
import com.rx.admin.service.classics.XiyouEventService;
import com.rx.admin.service.classics.XiyouPoemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "西游记数据")
@RestController
@RequestMapping("/api/classics/xiyou")
public class XiyouController {

    private final XiyouPoemService poemService;
    private final XiyouCharacterService characterService;
    private final XiyouEventService eventService;

    public XiyouController(XiyouPoemService poemService,
                           XiyouCharacterService characterService,
                           XiyouEventService eventService) {
        this.poemService = poemService;
        this.characterService = characterService;
        this.eventService = eventService;
    }

    // ====== 诗词 ======

    @Operation(summary = "西游诗词分页查询")
    @GetMapping("/poem/page")
    @SaCheckLogin
    public Result<PageResult<XiyouPoem>> poemPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(poemService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "西游诗词详情")
    @GetMapping("/poem/{id}")
    @SaCheckLogin
    public Result<XiyouPoem> poemDetail(@PathVariable Long id) {
        return Result.ok(poemService.getById(id));
    }

    @Operation(summary = "新增西游诗词")
    @PostMapping("/poem")
    @SaCheckPermission("classics:xiyou:poem:add")
    public Result<?> poemAdd(@RequestBody XiyouPoem poem) {
        poemService.save(poem);
        return Result.ok();
    }

    @Operation(summary = "编辑西游诗词")
    @PutMapping("/poem")
    @SaCheckPermission("classics:xiyou:poem:edit")
    public Result<?> poemUpdate(@RequestBody XiyouPoem poem) {
        poemService.updateById(poem);
        return Result.ok();
    }

    @Operation(summary = "删除西游诗词")
    @DeleteMapping("/poem/{id}")
    @SaCheckPermission("classics:xiyou:poem:delete")
    public Result<?> poemDelete(@PathVariable Long id) {
        poemService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除西游诗词")
    @DeleteMapping("/poem/batch")
    @SaCheckPermission("classics:xiyou:poem:delete")
    public Result<?> poemBatchDelete(@RequestBody List<Long> ids) {
        poemService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 人物 ======

    @Operation(summary = "西游人物分页查询")
    @GetMapping("/character/page")
    @SaCheckLogin
    public Result<PageResult<XiyouCharacter>> characterPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(characterService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "西游人物详情")
    @GetMapping("/character/{id}")
    @SaCheckLogin
    public Result<XiyouCharacter> characterDetail(@PathVariable Long id) {
        return Result.ok(characterService.getById(id));
    }

    @Operation(summary = "按种族筛选西游人物")
    @GetMapping("/character/race")
    @SaCheckLogin
    public Result<List<XiyouCharacter>> characterByRace(@RequestParam(required = false) String race) {
        return Result.ok(characterService.listByRace(race));
    }

    @Operation(summary = "新增西游人物")
    @PostMapping("/character")
    @SaCheckPermission("classics:xiyou:character:add")
    public Result<?> characterAdd(@RequestBody XiyouCharacter character) {
        characterService.save(character);
        return Result.ok();
    }

    @Operation(summary = "编辑西游人物")
    @PutMapping("/character")
    @SaCheckPermission("classics:xiyou:character:edit")
    public Result<?> characterUpdate(@RequestBody XiyouCharacter character) {
        characterService.updateById(character);
        return Result.ok();
    }

    @Operation(summary = "删除西游人物")
    @DeleteMapping("/character/{id}")
    @SaCheckPermission("classics:xiyou:character:delete")
    public Result<?> characterDelete(@PathVariable Long id) {
        characterService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除西游人物")
    @DeleteMapping("/character/batch")
    @SaCheckPermission("classics:xiyou:character:delete")
    public Result<?> characterBatchDelete(@RequestBody List<Long> ids) {
        characterService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 八十一难 ======

    @Operation(summary = "西游八十一难全部列表（时间轴用）")
    @GetMapping("/event/list/all")
    @SaCheckLogin
    public Result<List<XiyouEvent>> eventAll() {
        return Result.ok(eventService.listAll());
    }

    @Operation(summary = "西游八十一难分页查询")
    @GetMapping("/event/page")
    @SaCheckLogin
    public Result<PageResult<XiyouEvent>> eventPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(eventService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "西游八十一难详情")
    @GetMapping("/event/{id}")
    @SaCheckLogin
    public Result<XiyouEvent> eventDetail(@PathVariable Long id) {
        return Result.ok(eventService.getById(id));
    }

    @Operation(summary = "新增西游八十一难")
    @PostMapping("/event")
    @SaCheckPermission("classics:xiyou:event:add")
    public Result<?> eventAdd(@RequestBody XiyouEvent event) {
        eventService.save(event);
        return Result.ok();
    }

    @Operation(summary = "编辑西游八十一难")
    @PutMapping("/event")
    @SaCheckPermission("classics:xiyou:event:edit")
    public Result<?> eventUpdate(@RequestBody XiyouEvent event) {
        eventService.updateById(event);
        return Result.ok();
    }

    @Operation(summary = "删除西游八十一难")
    @DeleteMapping("/event/{id}")
    @SaCheckPermission("classics:xiyou:event:delete")
    public Result<?> eventDelete(@PathVariable Long id) {
        eventService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除西游八十一难")
    @DeleteMapping("/event/batch")
    @SaCheckPermission("classics:xiyou:event:delete")
    public Result<?> eventBatchDelete(@RequestBody List<Long> ids) {
        eventService.removeByIds(ids);
        return Result.ok();
    }
}