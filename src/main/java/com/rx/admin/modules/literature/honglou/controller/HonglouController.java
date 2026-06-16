package com.rx.admin.modules.literature.honglou.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.literature.honglou.vo.HonglouPoemVO;
import com.rx.admin.modules.literature.honglou.dto.HonglouPoemCreateDTO;
import com.rx.admin.modules.literature.honglou.dto.HonglouPoemUpdateDTO;
import com.rx.admin.modules.literature.honglou.convert.HonglouPoemConvert;
import com.rx.admin.modules.literature.honglou.service.HonglouPoemService;
import com.rx.admin.modules.literature.honglou.vo.HonglouCharacterVO;
import com.rx.admin.modules.literature.honglou.dto.HonglouCharacterCreateDTO;
import com.rx.admin.modules.literature.honglou.dto.HonglouCharacterUpdateDTO;
import com.rx.admin.modules.literature.honglou.convert.HonglouCharacterConvert;
import com.rx.admin.modules.literature.honglou.service.HonglouCharacterService;
import com.rx.admin.modules.literature.honglou.vo.HonglouCharacterRelationVO;
import com.rx.admin.modules.literature.honglou.dto.HonglouCharacterRelationCreateDTO;
import com.rx.admin.modules.literature.honglou.dto.HonglouCharacterRelationUpdateDTO;
import com.rx.admin.modules.literature.honglou.convert.HonglouCharacterRelationConvert;
import com.rx.admin.modules.literature.honglou.service.HonglouCharacterRelationService;
import com.rx.admin.modules.literature.honglou.entity.HonglouCharacter;
import com.rx.admin.modules.literature.honglou.entity.HonglouCharacterRelation;
import com.rx.admin.modules.literature.honglou.entity.HonglouPoem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "红楼梦数据")
@RestController
@RequestMapping("/api/classics/honglou")
public class HonglouController {


    private final HonglouPoemService poemService;
    private final HonglouPoemConvert poemConvert;
    private final HonglouCharacterService characterService;
    private final HonglouCharacterConvert characterConvert;
    private final HonglouCharacterRelationService relationService;
    private final HonglouCharacterRelationConvert relationConvert;

    public HonglouController(HonglouPoemService poemService, HonglouPoemConvert poemConvert, HonglouCharacterService characterService, HonglouCharacterConvert characterConvert, HonglouCharacterRelationService relationService, HonglouCharacterRelationConvert relationConvert) {
        this.poemService = poemService;
        this.poemConvert = poemConvert;
        this.characterService = characterService;
        this.characterConvert = characterConvert;
        this.relationService = relationService;
        this.relationConvert = relationConvert;
    }

    // ====== 诗词 ======

    @Operation(summary = "红楼诗词分页查询")
    @GetMapping("/poem/page")
    @SaCheckLogin
    public Result<PageResult<HonglouPoemVO>> poemPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<HonglouPoem> pr = poemService.pageQuery(page, size, keyword);
        List<HonglouPoemVO> voList = poemConvert.toVOList(pr.getRecords());
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), voList));
    }

    @Operation(summary = "红楼诗词详情")
    @GetMapping("/poem/{id}")
    @SaCheckLogin
    public Result<HonglouPoemVO> poemDetail(@PathVariable Long id) {
        return Result.ok(poemConvert.toVO(poemService.getById(id)));
    }

    @Operation(summary = "新增红楼诗词")
    @PostMapping("/poem")
    @SaCheckPermission(PermissionConstants.Honglou.POEM_ADD)
    @OperateLog(module = "红楼梦管理", operation = "新增诗词")
    public Result<?> poemAdd(@RequestBody @Valid HonglouPoemCreateDTO dto) {
        poemService.save(poemConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑红楼诗词")
    @PutMapping("/poem")
    @SaCheckPermission(PermissionConstants.Honglou.POEM_EDIT)
    @OperateLog(module = "红楼梦管理", operation = "编辑诗词")
    public Result<?> poemUpdate(@RequestBody @Valid HonglouPoemUpdateDTO dto) {
        HonglouPoem entity = poemService.getById(dto.getId());
        poemConvert.updateEntity(entity, dto);
        poemService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除红楼诗词")
    @DeleteMapping("/poem/{id}")
    @SaCheckPermission(PermissionConstants.Honglou.POEM_DELETE)
    @OperateLog(module = "红楼梦管理", operation = "删除诗词")
    public Result<?> poemDelete(@PathVariable Long id) {
        poemService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除红楼诗词")
    @DeleteMapping("/poem/batch")
    @SaCheckPermission(PermissionConstants.Honglou.POEM_DELETE)
    public Result<?> poemBatchDelete(@RequestBody List<Long> ids) {
        poemService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 人物 ======

    @Operation(summary = "红楼人物分页查询")
    @GetMapping("/character/page")
    @SaCheckLogin
    public Result<PageResult<HonglouCharacterVO>> characterPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<HonglouCharacter> pr = characterService.pageQuery(page, size, keyword);
        List<HonglouCharacterVO> voList = characterConvert.toVOList(pr.getRecords());
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), voList));
    }

    @Operation(summary = "红楼人物详情")
    @GetMapping("/character/{id}")
    @SaCheckLogin
    public Result<HonglouCharacterVO> characterDetail(@PathVariable Long id) {
        return Result.ok(characterConvert.toVO(characterService.getById(id)));
    }

    @Operation(summary = "按角色筛选红楼人物")
    @GetMapping("/character/role")
    @SaCheckLogin
    public Result<List<HonglouCharacterVO>> characterByRole(@RequestParam(required = false) String role) {
        return Result.ok(characterConvert.toVOList(characterService.listByRole(role)));
    }

    @Operation(summary = "获取所有红楼人物")
    @GetMapping("/character/all")
    @SaCheckLogin
    public Result<List<HonglouCharacterVO>> characterAll() {
        return Result.ok(characterConvert.toVOList(characterService.listAll()));
    }

    @Operation(summary = "新增红楼人物")
    @PostMapping("/character")
    @SaCheckPermission(PermissionConstants.Honglou.CHARACTER_ADD)
    @OperateLog(module = "红楼梦管理", operation = "新增人物")
    public Result<?> characterAdd(@RequestBody @Valid HonglouCharacterCreateDTO dto) {
        characterService.save(characterConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑红楼人物")
    @PutMapping("/character")
    @SaCheckPermission(PermissionConstants.Honglou.CHARACTER_EDIT)
    @OperateLog(module = "红楼梦管理", operation = "编辑人物")
    public Result<?> characterUpdate(@RequestBody @Valid HonglouCharacterUpdateDTO dto) {
        HonglouCharacter entity = characterService.getById(dto.getId());
        characterConvert.updateEntity(entity, dto);
        characterService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除红楼人物")
    @DeleteMapping("/character/{id}")
    @SaCheckPermission(PermissionConstants.Honglou.CHARACTER_DELETE)
    @OperateLog(module = "红楼梦管理", operation = "删除人物")
    public Result<?> characterDelete(@PathVariable Long id) {
        characterService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除红楼人物")
    @DeleteMapping("/character/batch")
    @SaCheckPermission(PermissionConstants.Honglou.CHARACTER_DELETE)
    public Result<?> characterBatchDelete(@RequestBody List<Long> ids) {
        characterService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 人物关系 ======

    @Operation(summary = "红楼人物关系列表（按人物ID查询）")
    @GetMapping("/relation/{characterId}")
    @SaCheckLogin
    public Result<List<HonglouCharacterRelationVO>> relations(@PathVariable Long characterId) {
        return Result.ok(relationConvert.toVOList(relationService.listByCharacterId(characterId)));
    }

    @Operation(summary = "新增人物关系")
    @PostMapping("/relation")
    @SaCheckPermission(PermissionConstants.Honglou.RELATION_ADD)
    @OperateLog(module = "红楼梦管理", operation = "新增人物关系")
    public Result<?> relationAdd(@RequestBody @Valid HonglouCharacterRelationCreateDTO dto) {
        relationService.save(relationConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑人物关系")
    @PutMapping("/relation")
    @SaCheckPermission(PermissionConstants.Honglou.RELATION_EDIT)
    @OperateLog(module = "红楼梦管理", operation = "编辑人物关系")
    public Result<?> relationUpdate(@RequestBody @Valid HonglouCharacterRelationUpdateDTO dto) {
        HonglouCharacterRelation entity = relationService.getById(dto.getId());
        relationConvert.updateEntity(entity, dto);
        relationService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除人物关系")
    @DeleteMapping("/relation/{id}")
    @SaCheckPermission(PermissionConstants.Honglou.RELATION_DELETE)
    @OperateLog(module = "红楼梦管理", operation = "删除人物关系")
    public Result<?> relationDelete(@PathVariable Long id) {
        relationService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取所有人物关系")
    @GetMapping("/relation/all")
    @SaCheckLogin
    public Result<List<HonglouCharacterRelationVO>> relationAll() {
        return Result.ok(relationConvert.toVOList(relationService.listAll()));
    }
}