package com.rx.admin.controller.classics;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.classics.HonglouCharacter;
import com.rx.admin.entity.classics.HonglouCharacterRelation;
import com.rx.admin.entity.classics.HonglouPoem;
import com.rx.admin.service.classics.HonglouCharacterRelationService;
import com.rx.admin.service.classics.HonglouCharacterService;
import com.rx.admin.service.classics.HonglouPoemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "红楼梦数据")
@RestController
@RequestMapping("/api/classics/honglou")
public class HonglouController {

    private final HonglouPoemService poemService;
    private final HonglouCharacterService characterService;
    private final HonglouCharacterRelationService relationService;

    public HonglouController(HonglouPoemService poemService,
                             HonglouCharacterService characterService,
                             HonglouCharacterRelationService relationService) {
        this.poemService = poemService;
        this.characterService = characterService;
        this.relationService = relationService;
    }

    // ====== 诗词 ======

    @Operation(summary = "红楼诗词分页查询")
    @GetMapping("/poem/page")
    @SaCheckLogin
    public Result<PageResult<HonglouPoem>> poemPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(poemService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "红楼诗词详情")
    @GetMapping("/poem/{id}")
    @SaCheckLogin
    public Result<HonglouPoem> poemDetail(@PathVariable Long id) {
        return Result.ok(poemService.getById(id));
    }

    @Operation(summary = "新增红楼诗词")
    @PostMapping("/poem")
    @SaCheckPermission("classics:honglou:poem:add")
    @OperateLog(module = "红楼梦管理", operation = "新增诗词")
    public Result<?> poemAdd(@RequestBody HonglouPoem poem) {
        poemService.save(poem);
        return Result.ok();
    }

    @Operation(summary = "编辑红楼诗词")
    @PutMapping("/poem")
    @SaCheckPermission("classics:honglou:poem:edit")
    @OperateLog(module = "红楼梦管理", operation = "编辑诗词")
    public Result<?> poemUpdate(@RequestBody HonglouPoem poem) {
        poemService.updateById(poem);
        return Result.ok();
    }

    @Operation(summary = "删除红楼诗词")
    @DeleteMapping("/poem/{id}")
    @SaCheckPermission("classics:honglou:poem:delete")
    @OperateLog(module = "红楼梦管理", operation = "删除诗词")
    public Result<?> poemDelete(@PathVariable Long id) {
        poemService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除红楼诗词")
    @DeleteMapping("/poem/batch")
    @SaCheckPermission("classics:honglou:poem:delete")
    public Result<?> poemBatchDelete(@RequestBody List<Long> ids) {
        poemService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 人物 ======

    @Operation(summary = "红楼人物分页查询")
    @GetMapping("/character/page")
    @SaCheckLogin
    public Result<PageResult<HonglouCharacter>> characterPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(characterService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "红楼人物详情")
    @GetMapping("/character/{id}")
    @SaCheckLogin
    public Result<HonglouCharacter> characterDetail(@PathVariable Long id) {
        return Result.ok(characterService.getById(id));
    }

    @Operation(summary = "按角色筛选红楼人物（主角/重要配角/一般角色）")
    @GetMapping("/character/role")
    @SaCheckLogin
    public Result<List<HonglouCharacter>> characterByRole(@RequestParam(required = false) String role) {
        return Result.ok(characterService.listByRole(role));
    }

    @Operation(summary = "获取所有红楼人物")
    @GetMapping("/character/all")
    @SaCheckLogin
    public Result<List<HonglouCharacter>> characterAll() {
        return Result.ok(characterService.listAll());
    }

    @Operation(summary = "新增红楼人物")
    @PostMapping("/character")
    @SaCheckPermission("classics:honglou:character:add")
    @OperateLog(module = "红楼梦管理", operation = "新增人物")
    public Result<?> characterAdd(@RequestBody HonglouCharacter character) {
        characterService.save(character);
        return Result.ok();
    }

    @Operation(summary = "编辑红楼人物")
    @PutMapping("/character")
    @SaCheckPermission("classics:honglou:character:edit")
    @OperateLog(module = "红楼梦管理", operation = "编辑人物")
    public Result<?> characterUpdate(@RequestBody HonglouCharacter character) {
        characterService.updateById(character);
        return Result.ok();
    }

    @Operation(summary = "删除红楼人物")
    @DeleteMapping("/character/{id}")
    @SaCheckPermission("classics:honglou:character:delete")
    @OperateLog(module = "红楼梦管理", operation = "删除人物")
    public Result<?> characterDelete(@PathVariable Long id) {
        characterService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除红楼人物")
    @DeleteMapping("/character/batch")
    @SaCheckPermission("classics:honglou:character:delete")
    public Result<?> characterBatchDelete(@RequestBody List<Long> ids) {
        characterService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 人物关系 ======

    @Operation(summary = "红楼人物关系列表（按人物ID查询）")
    @GetMapping("/relation/{characterId}")
    @SaCheckLogin
    public Result<List<HonglouCharacterRelation>> relations(@PathVariable Long characterId) {
        return Result.ok(relationService.listByCharacterId(characterId));
    }

    @Operation(summary = "新增人物关系")
    @PostMapping("/relation")
    @SaCheckPermission("classics:honglou:relation:add")
    @OperateLog(module = "红楼梦管理", operation = "新增人物关系")
    public Result<?> relationAdd(@RequestBody HonglouCharacterRelation relation) {
        relationService.save(relation);
        return Result.ok();
    }

    @Operation(summary = "编辑人物关系")
    @PutMapping("/relation")
    @SaCheckPermission("classics:honglou:relation:edit")
    @OperateLog(module = "红楼梦管理", operation = "编辑人物关系")
    public Result<?> relationUpdate(@RequestBody HonglouCharacterRelation relation) {
        relationService.updateById(relation);
        return Result.ok();
    }

    @Operation(summary = "删除人物关系")
    @DeleteMapping("/relation/{id}")
    @SaCheckPermission("classics:honglou:relation:delete")
    @OperateLog(module = "红楼梦管理", operation = "删除人物关系")
    public Result<?> relationDelete(@PathVariable Long id) {
        relationService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取所有人物关系")
    @GetMapping("/relation/all")
    @SaCheckLogin
    public Result<List<HonglouCharacterRelation>> relationAll() {
        return Result.ok(relationService.listAll());
    }
}
