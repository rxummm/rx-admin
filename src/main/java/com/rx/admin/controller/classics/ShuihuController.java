package com.rx.admin.controller.classics;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.classics.ShuihuChapter;
import com.rx.admin.entity.classics.ShuihuPoem;
import com.rx.admin.service.classics.ShuihuChapterService;
import com.rx.admin.service.classics.ShuihuPoemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "水浒传数据")
@RestController
@RequestMapping("/api/classics/shuihu")
public class ShuihuController {

    private final ShuihuPoemService poemService;
    private final ShuihuChapterService chapterService;

    public ShuihuController(ShuihuPoemService poemService,
                            ShuihuChapterService chapterService) {
        this.poemService = poemService;
        this.chapterService = chapterService;
    }

    // ====== 诗词 ======

    @Operation(summary = "水浒诗词分页查询")
    @GetMapping("/poem/page")
    @SaCheckLogin
    public Result<PageResult<ShuihuPoem>> poemPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(poemService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "水浒诗词详情")
    @GetMapping("/poem/{id}")
    @SaCheckLogin
    public Result<ShuihuPoem> poemDetail(@PathVariable Long id) {
        return Result.ok(poemService.getById(id));
    }

    @Operation(summary = "新增水浒诗词")
    @PostMapping("/poem")
    @SaCheckPermission("classics:shuihu:poem:add")
    public Result<?> poemAdd(@RequestBody ShuihuPoem poem) {
        poemService.save(poem);
        return Result.ok();
    }

    @Operation(summary = "编辑水浒诗词")
    @PutMapping("/poem")
    @SaCheckPermission("classics:shuihu:poem:edit")
    public Result<?> poemUpdate(@RequestBody ShuihuPoem poem) {
        poemService.updateById(poem);
        return Result.ok();
    }

    @Operation(summary = "删除水浒诗词")
    @DeleteMapping("/poem/{id}")
    @SaCheckPermission("classics:shuihu:poem:delete")
    public Result<?> poemDelete(@PathVariable Long id) {
        poemService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除水浒诗词")
    @DeleteMapping("/poem/batch")
    @SaCheckPermission("classics:shuihu:poem:delete")
    public Result<?> poemBatchDelete(@RequestBody List<Long> ids) {
        poemService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 章节 ======

    @Operation(summary = "水浒章节分页查询")
    @GetMapping("/chapter/page")
    @SaCheckLogin
    public Result<PageResult<ShuihuChapter>> chapterPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(chapterService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "水浒章节详情")
    @GetMapping("/chapter/{id}")
    @SaCheckLogin
    public Result<ShuihuChapter> chapterDetail(@PathVariable Long id) {
        return Result.ok(chapterService.getById(id));
    }

    @Operation(summary = "新增水浒章节")
    @PostMapping("/chapter")
    @SaCheckPermission("classics:shuihu:chapter:add")
    public Result<?> chapterAdd(@RequestBody ShuihuChapter chapter) {
        chapterService.save(chapter);
        return Result.ok();
    }

    @Operation(summary = "编辑水浒章节")
    @PutMapping("/chapter")
    @SaCheckPermission("classics:shuihu:chapter:edit")
    public Result<?> chapterUpdate(@RequestBody ShuihuChapter chapter) {
        chapterService.updateById(chapter);
        return Result.ok();
    }

    @Operation(summary = "删除水浒章节")
    @DeleteMapping("/chapter/{id}")
    @SaCheckPermission("classics:shuihu:chapter:delete")
    public Result<?> chapterDelete(@PathVariable Long id) {
        chapterService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除水浒章节")
    @DeleteMapping("/chapter/batch")
    @SaCheckPermission("classics:shuihu:chapter:delete")
    public Result<?> chapterBatchDelete(@RequestBody List<Long> ids) {
        chapterService.removeByIds(ids);
        return Result.ok();
    }
}