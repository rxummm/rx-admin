package com.rx.admin.controller.classics;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.classics.SanguoCharacter;
import com.rx.admin.entity.classics.SanguoPoem;
import com.rx.admin.service.classics.SanguoCharacterService;
import com.rx.admin.service.classics.SanguoPoemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "三国演义数据")
@RestController
@RequestMapping("/api/classics/sanguo")
public class SanguoController {

    private final SanguoPoemService poemService;
    private final SanguoCharacterService characterService;

    public SanguoController(SanguoPoemService poemService,
                            SanguoCharacterService characterService) {
        this.poemService = poemService;
        this.characterService = characterService;
    }

    // ====== 诗词 ======

    @Operation(summary = "三国诗词分页查询")
    @GetMapping("/poem/page")
    @SaCheckLogin
    public Result<PageResult<SanguoPoem>> poemPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(poemService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "三国诗词详情")
    @GetMapping("/poem/{id}")
    @SaCheckLogin
    public Result<SanguoPoem> poemDetail(@PathVariable Long id) {
        return Result.ok(poemService.getById(id));
    }

    @Operation(summary = "新增三国诗词")
    @PostMapping("/poem")
    @SaCheckPermission("classics:sanguo:poem:add")
    public Result<?> poemAdd(@RequestBody SanguoPoem poem) {
        poemService.save(poem);
        return Result.ok();
    }

    @Operation(summary = "编辑三国诗词")
    @PutMapping("/poem")
    @SaCheckPermission("classics:sanguo:poem:edit")
    public Result<?> poemUpdate(@RequestBody SanguoPoem poem) {
        poemService.updateById(poem);
        return Result.ok();
    }

    @Operation(summary = "删除三国诗词")
    @DeleteMapping("/poem/{id}")
    @SaCheckPermission("classics:sanguo:poem:delete")
    public Result<?> poemDelete(@PathVariable Long id) {
        poemService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除三国诗词")
    @DeleteMapping("/poem/batch")
    @SaCheckPermission("classics:sanguo:poem:delete")
    public Result<?> poemBatchDelete(@RequestBody List<Long> ids) {
        poemService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 人物 ======

    @Operation(summary = "三国人物分页查询")
    @GetMapping("/character/page")
    @SaCheckLogin
    public Result<PageResult<SanguoCharacter>> characterPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(characterService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "三国人物详情")
    @GetMapping("/character/{id}")
    @SaCheckLogin
    public Result<SanguoCharacter> characterDetail(@PathVariable Long id) {
        return Result.ok(characterService.getById(id));
    }

    @Operation(summary = "按国家筛选三国人物")
    @GetMapping("/character/country")
    @SaCheckLogin
    public Result<List<SanguoCharacter>> characterByCountry(@RequestParam(required = false) String country) {
        return Result.ok(characterService.listByCountry(country));
    }

    @Operation(summary = "新增三国人物")
    @PostMapping("/character")
    @SaCheckPermission("classics:sanguo:character:add")
    public Result<?> characterAdd(@RequestBody SanguoCharacter character) {
        characterService.save(character);
        return Result.ok();
    }

    @Operation(summary = "编辑三国人物")
    @PutMapping("/character")
    @SaCheckPermission("classics:sanguo:character:edit")
    public Result<?> characterUpdate(@RequestBody SanguoCharacter character) {
        characterService.updateById(character);
        return Result.ok();
    }

    @Operation(summary = "删除三国人物")
    @DeleteMapping("/character/{id}")
    @SaCheckPermission("classics:sanguo:character:delete")
    public Result<?> characterDelete(@PathVariable Long id) {
        characterService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除三国人物")
    @DeleteMapping("/character/batch")
    @SaCheckPermission("classics:sanguo:character:delete")
    public Result<?> characterBatchDelete(@RequestBody List<Long> ids) {
        characterService.removeByIds(ids);
        return Result.ok();
    }
}
