package com.rx.admin.modules.literature.shuihu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.literature.shuihu.vo.ShuihuChapterVO;
import com.rx.admin.modules.literature.shuihu.dto.ShuihuChapterCreateDTO;
import com.rx.admin.modules.literature.shuihu.dto.ShuihuChapterUpdateDTO;
import com.rx.admin.modules.literature.shuihu.convert.ShuihuChapterConvert;
import com.rx.admin.modules.literature.shuihu.vo.ShuihuPoemVO;
import com.rx.admin.modules.literature.shuihu.dto.ShuihuPoemCreateDTO;
import com.rx.admin.modules.literature.shuihu.dto.ShuihuPoemUpdateDTO;
import com.rx.admin.modules.literature.shuihu.convert.ShuihuPoemConvert;
import com.rx.admin.modules.literature.shuihu.service.ShuihuChapterService;
import com.rx.admin.modules.literature.shuihu.entity.ShuihuChapter;
import com.rx.admin.modules.literature.shuihu.entity.ShuihuPoem;
import com.rx.admin.modules.literature.shuihu.service.ShuihuPoemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "水浒传数据")
@RestController
@RequestMapping("/api/classics/shuihu")
public class ShuihuController {


    private final ShuihuChapterService chapterService;
    private final ShuihuChapterConvert chapterConvert;
    private final ShuihuPoemService poemService;
    private final ShuihuPoemConvert poemConvert;

    public ShuihuController(ShuihuChapterService chapterService, ShuihuChapterConvert chapterConvert, ShuihuPoemService poemService, ShuihuPoemConvert poemConvert) {
        this.chapterService = chapterService;
        this.chapterConvert = chapterConvert;
        this.poemService = poemService;
        this.poemConvert = poemConvert;
    }

    // ====== 水浒章节 ======

    @Operation(summary = "ShuihuChapter分页查询")
    @GetMapping("/chapter/page")
    @SaCheckLogin
    public Result<PageResult<ShuihuChapterVO>> chapterServicePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<ShuihuChapter> pr = chapterService.pageQuery(page, size, keyword);
        List<ShuihuChapterVO> voList = chapterConvert.toVOList(pr.getRecords());
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), voList));
    }

    @Operation(summary = "ShuihuChapter详情")
    @GetMapping("/chapter/{id}")
    @SaCheckLogin
    public Result<ShuihuChapterVO> chapterServiceDetail(@PathVariable Long id) {
        return Result.ok(chapterConvert.toVO(chapterService.getById(id)));
    }

    @Operation(summary = "获取所有ShuihuChapter")
    @GetMapping("/chapter/all")
    @SaCheckLogin
    public Result<List<ShuihuChapterVO>> chapterServiceAll() {
        return Result.ok(chapterConvert.toVOList(chapterService.list()));
    }

    @Operation(summary = "新增ShuihuChapter")
    @PostMapping("/chapter")
    @SaCheckPermission(PermissionConstants.Shuihu.CHAPTER_ADD)
    @OperateLog(module = "水浒传数据", operation = "新增ShuihuChapter")
    public Result<?> chapterServiceAdd(@RequestBody @Valid ShuihuChapterCreateDTO dto) {
        chapterService.save(chapterConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑ShuihuChapter")
    @PutMapping("/chapter")
    @SaCheckPermission(PermissionConstants.Shuihu.CHAPTER_EDIT)
    @OperateLog(module = "水浒传数据", operation = "编辑ShuihuChapter")
    public Result<?> chapterServiceUpdate(@RequestBody @Valid ShuihuChapterUpdateDTO dto) {
        ShuihuChapter entity = chapterService.getById(dto.getId());
        chapterConvert.updateEntity(entity, dto);
        chapterService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除ShuihuChapter")
    @DeleteMapping("/chapter/{id}")
    @SaCheckPermission(PermissionConstants.Shuihu.CHAPTER_DELETE)
    @OperateLog(module = "水浒传数据", operation = "删除ShuihuChapter")
    public Result<?> chapterServiceDelete(@PathVariable Long id) {
        chapterService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除ShuihuChapter")
    @DeleteMapping("/chapter/batch")
    @SaCheckPermission(PermissionConstants.Shuihu.CHAPTER_DELETE)
    public Result<?> chapterServiceBatchDelete(@RequestBody List<Long> ids) {
        chapterService.removeByIds(ids);
        return Result.ok();
    }
    // ====== 水浒诗词 ======

    @Operation(summary = "ShuihuPoem分页查询")
    @GetMapping("/poem/page")
    @SaCheckLogin
    public Result<PageResult<ShuihuPoemVO>> poemServicePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<ShuihuPoem> pr = poemService.pageQuery(page, size, keyword);
        List<ShuihuPoemVO> voList = poemConvert.toVOList(pr.getRecords());
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), voList));
    }

    @Operation(summary = "ShuihuPoem详情")
    @GetMapping("/poem/{id}")
    @SaCheckLogin
    public Result<ShuihuPoemVO> poemServiceDetail(@PathVariable Long id) {
        return Result.ok(poemConvert.toVO(poemService.getById(id)));
    }

    @Operation(summary = "获取所有ShuihuPoem")
    @GetMapping("/poem/all")
    @SaCheckLogin
    public Result<List<ShuihuPoemVO>> poemServiceAll() {
        return Result.ok(poemConvert.toVOList(poemService.list()));
    }

    @Operation(summary = "新增ShuihuPoem")
    @PostMapping("/poem")
    @SaCheckPermission(PermissionConstants.Shuihu.POEM_ADD)
    @OperateLog(module = "水浒传数据", operation = "新增ShuihuPoem")
    public Result<?> poemServiceAdd(@RequestBody @Valid ShuihuPoemCreateDTO dto) {
        poemService.save(poemConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑ShuihuPoem")
    @PutMapping("/poem")
    @SaCheckPermission(PermissionConstants.Shuihu.POEM_EDIT)
    @OperateLog(module = "水浒传数据", operation = "编辑ShuihuPoem")
    public Result<?> poemServiceUpdate(@RequestBody @Valid ShuihuPoemUpdateDTO dto) {
        ShuihuPoem entity = poemService.getById(dto.getId());
        poemConvert.updateEntity(entity, dto);
        poemService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除ShuihuPoem")
    @DeleteMapping("/poem/{id}")
    @SaCheckPermission(PermissionConstants.Shuihu.POEM_DELETE)
    @OperateLog(module = "水浒传数据", operation = "删除ShuihuPoem")
    public Result<?> poemServiceDelete(@PathVariable Long id) {
        poemService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除ShuihuPoem")
    @DeleteMapping("/poem/batch")
    @SaCheckPermission(PermissionConstants.Shuihu.POEM_DELETE)
    public Result<?> poemServiceBatchDelete(@RequestBody List<Long> ids) {
        poemService.removeByIds(ids);
        return Result.ok();
    }
}