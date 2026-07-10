package com.rx.admin.modules.literature.sanguo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.literature.sanguo.vo.SanguoPoemVO;
import com.rx.admin.modules.literature.sanguo.dto.SanguoPoemCreateDTO;
import com.rx.admin.modules.literature.sanguo.dto.SanguoPoemUpdateDTO;
import com.rx.admin.modules.literature.sanguo.convert.SanguoPoemConvert;
import com.rx.admin.modules.literature.sanguo.vo.SanguoCharacterVO;
import com.rx.admin.modules.literature.sanguo.dto.SanguoCharacterCreateDTO;
import com.rx.admin.modules.literature.sanguo.dto.SanguoCharacterUpdateDTO;
import com.rx.admin.modules.literature.sanguo.convert.SanguoCharacterConvert;
import com.rx.admin.modules.literature.sanguo.service.SanguoCharacterService;
import com.rx.admin.modules.literature.sanguo.entity.SanguoCharacter;
import com.rx.admin.modules.literature.sanguo.entity.SanguoPoem;
import com.rx.admin.modules.literature.sanguo.service.SanguoPoemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "三国演义数据")
@RestController
@ApiVersion(1)
@RequestMapping("/classics/sanguo")
@RequiredArgsConstructor
public class SanguoController {

    private final SanguoPoemService poemService;
    private final SanguoPoemConvert poemConvert;
    private final SanguoCharacterService characterService;
    private final SanguoCharacterConvert characterConvert;

    // ====== 三国诗词 ======

    @Operation(summary = "SanguoPoem分页查询")
    @GetMapping("/poem/page")
    @SaCheckLogin
    public Result<PageResult<SanguoPoemVO>> poemServicePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<SanguoPoem> pr = poemService.pageQuery(page, size, keyword);
        return Result.ok(poemConvert.toPageResult(pr));
    }

    @Operation(summary = "SanguoPoem详情")
    @GetMapping("/poem/{id}")
    @SaCheckLogin
    public Result<SanguoPoemVO> poemServiceDetail(@PathVariable Long id) {
        return Result.ok(poemConvert.toVO(poemService.getById(id)));
    }

    @Operation(summary = "获取所有SanguoPoem")
    @GetMapping("/poem/all")
    @SaCheckLogin
    public Result<List<SanguoPoemVO>> poemServiceAll() {
        return Result.ok(poemConvert.toVOList(poemService.list()));
    }

    @Operation(summary = "新增SanguoPoem")
    @PostMapping("/poem")
    @SaCheckPermission(PermissionConstants.Sanguo.POEM_ADD)
    @OperateLog(module = "三国演义数据", operation = "新增SanguoPoem")
    public Result<?> poemServiceAdd(@RequestBody @Valid SanguoPoemCreateDTO dto) {
        poemService.save(poemConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑SanguoPoem")
    @PutMapping("/poem")
    @SaCheckPermission(PermissionConstants.Sanguo.POEM_EDIT)
    @OperateLog(module = "三国演义数据", operation = "编辑SanguoPoem")
    public Result<?> poemServiceUpdate(@RequestBody @Valid SanguoPoemUpdateDTO dto) {
        SanguoPoem entity = poemService.getById(dto.getId());
        poemConvert.updateEntity(entity, dto);
        poemService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除SanguoPoem")
    @DeleteMapping("/poem/{id}")
    @SaCheckPermission(PermissionConstants.Sanguo.POEM_DELETE)
    @OperateLog(module = "三国演义数据", operation = "删除SanguoPoem")
    public Result<?> poemServiceDelete(@PathVariable Long id) {
        poemService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除SanguoPoem")
    @DeleteMapping("/poem/batch")
    @SaCheckPermission(PermissionConstants.Sanguo.POEM_DELETE)
    @OperateLog(module = "三国演义数据", operation = "批量删除SanguoPoem")
    public Result<?> poemServiceBatchDelete(@RequestBody List<Long> ids) {
        poemService.removeByIds(ids);
        return Result.ok();
    }
    // ====== 三国人物 ======

    @Operation(summary = "SanguoCharacter分页查询")
    @GetMapping("/character/page")
    @SaCheckLogin
    public Result<PageResult<SanguoCharacterVO>> characterServicePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<SanguoCharacter> pr = characterService.pageQuery(page, size, keyword);
        return Result.ok(characterConvert.toPageResult(pr));
    }

    @Operation(summary = "SanguoCharacter详情")
    @GetMapping("/character/{id}")
    @SaCheckLogin
    public Result<SanguoCharacterVO> characterServiceDetail(@PathVariable Long id) {
        return Result.ok(characterConvert.toVO(characterService.getById(id)));
    }

    @Operation(summary = "获取所有SanguoCharacter")
    @GetMapping("/character/all")
    @SaCheckLogin
    public Result<List<SanguoCharacterVO>> characterServiceAll() {
        return Result.ok(characterConvert.toVOList(characterService.list()));
    }

    @Operation(summary = "新增SanguoCharacter")
    @PostMapping("/character")
    @SaCheckPermission(PermissionConstants.Sanguo.CHARACTER_ADD)
    @OperateLog(module = "三国演义数据", operation = "新增SanguoCharacter")
    public Result<?> characterServiceAdd(@RequestBody @Valid SanguoCharacterCreateDTO dto) {
        characterService.save(characterConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑SanguoCharacter")
    @PutMapping("/character")
    @SaCheckPermission(PermissionConstants.Sanguo.CHARACTER_EDIT)
    @OperateLog(module = "三国演义数据", operation = "编辑SanguoCharacter")
    public Result<?> characterServiceUpdate(@RequestBody @Valid SanguoCharacterUpdateDTO dto) {
        SanguoCharacter entity = characterService.getById(dto.getId());
        characterConvert.updateEntity(entity, dto);
        characterService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除SanguoCharacter")
    @DeleteMapping("/character/{id}")
    @SaCheckPermission(PermissionConstants.Sanguo.CHARACTER_DELETE)
    @OperateLog(module = "三国演义数据", operation = "删除SanguoCharacter")
    public Result<?> characterServiceDelete(@PathVariable Long id) {
        characterService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除SanguoCharacter")
    @DeleteMapping("/character/batch")
    @SaCheckPermission(PermissionConstants.Sanguo.CHARACTER_DELETE)
    @OperateLog(module = "三国演义数据", operation = "批量删除SanguoCharacter")
    public Result<?> characterServiceBatchDelete(@RequestBody List<Long> ids) {
        characterService.removeByIds(ids);
        return Result.ok();
    }
    @Operation(summary = "按国家筛选三国人物")
    @GetMapping("/character/country")
    @SaCheckLogin
    public Result<List<SanguoCharacterVO>> characterByCountry(@RequestParam(required = false) String country) {
        return Result.ok(characterConvert.toVOList(characterService.listByCountry(country)));
    }
}