package com.rx.admin.modules.literature.common.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.literature.common.vo.AuthorVO;
import com.rx.admin.modules.literature.common.vo.ContentCategoryVO;
import com.rx.admin.modules.literature.common.vo.DynastyVO;
import com.rx.admin.modules.literature.common.vo.GenreVO;
import com.rx.admin.modules.literature.common.vo.LiteraryWorkVO;
import com.rx.admin.modules.literature.common.dto.AuthorCreateDTO;
import com.rx.admin.modules.literature.common.dto.AuthorUpdateDTO;
import com.rx.admin.modules.literature.common.dto.ContentCategoryCreateDTO;
import com.rx.admin.modules.literature.common.dto.ContentCategoryUpdateDTO;
import com.rx.admin.modules.literature.common.dto.DynastyCreateDTO;
import com.rx.admin.modules.literature.common.dto.DynastyUpdateDTO;
import com.rx.admin.modules.literature.common.dto.GenreCreateDTO;
import com.rx.admin.modules.literature.common.dto.GenreUpdateDTO;
import com.rx.admin.modules.literature.common.dto.LiteraryWorkCreateDTO;
import com.rx.admin.modules.literature.common.dto.LiteraryWorkUpdateDTO;
import com.rx.admin.modules.literature.common.convert.AuthorConvert;
import com.rx.admin.modules.literature.common.convert.ContentCategoryConvert;
import com.rx.admin.modules.literature.common.convert.DynastyConvert;
import com.rx.admin.modules.literature.common.convert.GenreConvert;
import com.rx.admin.modules.literature.common.convert.LiteraryWorkConvert;
import com.rx.admin.modules.literature.common.service.AuthorService;
import com.rx.admin.modules.literature.common.service.ContentCategoryService;
import com.rx.admin.modules.literature.common.service.DynastyService;
import com.rx.admin.modules.literature.common.service.GenreService;
import com.rx.admin.modules.literature.common.service.LiteraryWorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "历代文学数据")
@RestController
@RequestMapping("/api/classics/literature")
public class LiteratureController {
    private final AuthorService authorService;
    private final AuthorConvert authorConvert;
    private final DynastyService dynastyService;
    private final DynastyConvert dynastyConvert;
    private final GenreService genreService;
    private final GenreConvert genreConvert;
    private final ContentCategoryService contentCategoryService;
    private final ContentCategoryConvert contentCategoryConvert;
    private final LiteraryWorkService literaryWorkService;
    private final LiteraryWorkConvert literaryWorkConvert;
    public LiteratureController(AuthorService authorService,
                                AuthorConvert authorConvert,
                                DynastyService dynastyService,
                                DynastyConvert dynastyConvert,
                                GenreService genreService,
                                GenreConvert genreConvert,
                                ContentCategoryService contentCategoryService,
                                ContentCategoryConvert contentCategoryConvert,
                                LiteraryWorkService literaryWorkService,
                                LiteraryWorkConvert literaryWorkConvert) {
        this.authorService = authorService;
        this.authorConvert = authorConvert;
        this.dynastyService = dynastyService;
        this.dynastyConvert = dynastyConvert;
        this.genreService = genreService;
        this.genreConvert = genreConvert;
        this.contentCategoryService = contentCategoryService;
        this.contentCategoryConvert = contentCategoryConvert;
        this.literaryWorkService = literaryWorkService;
        this.literaryWorkConvert = literaryWorkConvert;
    }

    // ====== 作者 ======

    @Operation(summary = "作者分页查询")
    @GetMapping("/author/page")
    @SaCheckLogin
    public Result<PageResult<AuthorVO>> authorPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<com.rx.admin.modules.literature.common.entity.Author> pr = authorService.pageQuery(page, size, keyword);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), authorConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "作者详情")
    @GetMapping("/author/{id}")
    @SaCheckLogin
    public Result<AuthorVO> authorDetail(@PathVariable Long id) {
        return Result.ok(authorConvert.toVO(authorService.getById(id)));
    }

    @Operation(summary = "获取所有作者")
    @GetMapping("/author/all")
    @SaCheckLogin
    public Result<List<AuthorVO>> authorAll() {
        return Result.ok(authorConvert.toVOList(authorService.listAll()));
    }

    @Operation(summary = "新增作者")
    @PostMapping("/author")
    @SaCheckPermission("classics:literature:author:add")
    public Result<?> authorAdd(@RequestBody @Valid AuthorCreateDTO dto) {
        authorService.save(authorConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑作者")
    @PutMapping("/author")
    @SaCheckPermission("classics:literature:author:edit")
    public Result<?> authorUpdate(@RequestBody @Valid AuthorUpdateDTO dto) {
        com.rx.admin.modules.literature.common.entity.Author entity = authorService.getById(dto.getId());
        authorConvert.updateEntity(entity, dto);
        authorService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除作者")
    @DeleteMapping("/author/{id}")
    @SaCheckPermission("classics:literature:author:delete")
    public Result<?> authorDelete(@PathVariable Long id) {
        authorService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除作者")
    @DeleteMapping("/author/batch")
    @SaCheckPermission("classics:literature:author:delete")
    public Result<?> authorBatchDelete(@RequestBody List<Long> ids) {
        authorService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 朝代 ======

    @Operation(summary = "朝代分页查询")
    @GetMapping("/dynasty/page")
    @SaCheckLogin
    public Result<PageResult<DynastyVO>> dynastyPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<com.rx.admin.modules.literature.common.entity.Dynasty> pr = dynastyService.pageQuery(page, size, keyword);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), dynastyConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "朝代详情")
    @GetMapping("/dynasty/{id}")
    @SaCheckLogin
    public Result<DynastyVO> dynastyDetail(@PathVariable Long id) {
        return Result.ok(dynastyConvert.toVO(dynastyService.getById(id)));
    }

    @Operation(summary = "获取所有朝代")
    @GetMapping("/dynasty/all")
    @SaCheckLogin
    public Result<List<DynastyVO>> dynastyAll() {
        return Result.ok(dynastyConvert.toVOList(dynastyService.listAll()));
    }

    @Operation(summary = "新增朝代")
    @PostMapping("/dynasty")
    @SaCheckPermission("classics:literature:dynasty:add")
    public Result<?> dynastyAdd(@RequestBody @Valid DynastyCreateDTO dto) {
        dynastyService.save(dynastyConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑朝代")
    @PutMapping("/dynasty")
    @SaCheckPermission("classics:literature:dynasty:edit")
    public Result<?> dynastyUpdate(@RequestBody @Valid DynastyUpdateDTO dto) {
        com.rx.admin.modules.literature.common.entity.Dynasty entity = dynastyService.getById(dto.getId());
        dynastyConvert.updateEntity(entity, dto);
        dynastyService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除朝代")
    @DeleteMapping("/dynasty/{id}")
    @SaCheckPermission("classics:literature:dynasty:delete")
    public Result<?> dynastyDelete(@PathVariable Long id) {
        dynastyService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除朝代")
    @DeleteMapping("/dynasty/batch")
    @SaCheckPermission("classics:literature:dynasty:delete")
    public Result<?> dynastyBatchDelete(@RequestBody List<Long> ids) {
        dynastyService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 体裁 ======

    @Operation(summary = "体裁分页查询")
    @GetMapping("/genre/page")
    @SaCheckLogin
    public Result<PageResult<GenreVO>> genrePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<com.rx.admin.modules.literature.common.entity.Genre> pr = genreService.pageQuery(page, size, keyword);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), genreConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "体裁详情")
    @GetMapping("/genre/{id}")
    @SaCheckLogin
    public Result<GenreVO> genreDetail(@PathVariable Long id) {
        return Result.ok(genreConvert.toVO(genreService.getById(id)));
    }

    @Operation(summary = "获取所有体裁")
    @GetMapping("/genre/all")
    @SaCheckLogin
    public Result<List<GenreVO>> genreAll() {
        return Result.ok(genreConvert.toVOList(genreService.listAll()));
    }

    @Operation(summary = "新增体裁")
    @PostMapping("/genre")
    @SaCheckPermission("classics:literature:genre:add")
    public Result<?> genreAdd(@RequestBody @Valid GenreCreateDTO dto) {
        genreService.save(genreConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑体裁")
    @PutMapping("/genre")
    @SaCheckPermission("classics:literature:genre:edit")
    public Result<?> genreUpdate(@RequestBody @Valid GenreUpdateDTO dto) {
        com.rx.admin.modules.literature.common.entity.Genre entity = genreService.getById(dto.getId());
        genreConvert.updateEntity(entity, dto);
        genreService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除体裁")
    @DeleteMapping("/genre/{id}")
    @SaCheckPermission("classics:literature:genre:delete")
    public Result<?> genreDelete(@PathVariable Long id) {
        genreService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除体裁")
    @DeleteMapping("/genre/batch")
    @SaCheckPermission("classics:literature:genre:delete")
    public Result<?> genreBatchDelete(@RequestBody List<Long> ids) {
        genreService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 内容分类 ======

    @Operation(summary = "内容分类分页查询")
    @GetMapping("/category/page")
    @SaCheckLogin
    public Result<PageResult<ContentCategoryVO>> categoryPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<com.rx.admin.modules.literature.common.entity.ContentCategory> pr = contentCategoryService.pageQuery(page, size, keyword);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), contentCategoryConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "内容分类详情")
    @GetMapping("/category/{id}")
    @SaCheckLogin
    public Result<ContentCategoryVO> categoryDetail(@PathVariable Long id) {
        return Result.ok(contentCategoryConvert.toVO(contentCategoryService.getById(id)));
    }

    @Operation(summary = "获取所有内容分类")
    @GetMapping("/category/all")
    @SaCheckLogin
    public Result<List<ContentCategoryVO>> categoryAll() {
        return Result.ok(contentCategoryConvert.toVOList(contentCategoryService.listAll()));
    }

    @Operation(summary = "新增内容分类")
    @PostMapping("/category")
    @SaCheckPermission("classics:literature:category:add")
    public Result<?> categoryAdd(@RequestBody @Valid ContentCategoryCreateDTO dto) {
        contentCategoryService.save(contentCategoryConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑内容分类")
    @PutMapping("/category")
    @SaCheckPermission("classics:literature:category:edit")
    public Result<?> categoryUpdate(@RequestBody @Valid ContentCategoryUpdateDTO dto) {
        com.rx.admin.modules.literature.common.entity.ContentCategory entity = contentCategoryService.getById(dto.getId());
        contentCategoryConvert.updateEntity(entity, dto);
        contentCategoryService.updateById(entity);
        return Result.ok();
    }

    @Operation(summary = "删除内容分类")
    @DeleteMapping("/category/{id}")
    @SaCheckPermission("classics:literature:category:delete")
    public Result<?> categoryDelete(@PathVariable Long id) {
        contentCategoryService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除内容分类")
    @DeleteMapping("/category/batch")
    @SaCheckPermission("classics:literature:category:delete")
    public Result<?> categoryBatchDelete(@RequestBody List<Long> ids) {
        contentCategoryService.removeByIds(ids);
        return Result.ok();
    }

    // ====== 文学作品 ======

    @Operation(summary = "文学作品分页查询")
    @GetMapping("/work/page")
    @SaCheckLogin
    public Result<PageResult<LiteraryWorkVO>> workPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long dynastyId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Long authorId) {
        PageResult<com.rx.admin.modules.literature.common.entity.LiteraryWork> pr = literaryWorkService.pageQuery(page, size, keyword, dynastyId, genreId, authorId);
        return Result.ok(PageResult.of(pr.getTotal(), pr.getPage(), pr.getSize(), literaryWorkConvert.toVOList(pr.getRecords())));
    }

    @Operation(summary = "文学作品详情")
    @GetMapping("/work/{id}")
    @SaCheckLogin
    public Result<LiteraryWorkVO> workDetail(@PathVariable Long id) {
        return Result.ok(literaryWorkConvert.toVO(literaryWorkService.getDetail(id)));
    }

    @Operation(summary = "获取所有文学作品")
    @GetMapping("/work/all")
    @SaCheckLogin
    public Result<List<LiteraryWorkVO>> workAll() {
        return Result.ok(literaryWorkConvert.toVOList(literaryWorkService.listAll()));
    }

    @Operation(summary = "新增文学作品")
    @PostMapping("/work")
    @SaCheckPermission("classics:literature:work:add")
    public Result<?> workAdd(@RequestBody @Valid LiteraryWorkCreateDTO dto) {
        literaryWorkService.saveWithWordCount(literaryWorkConvert.toEntity(dto));
        return Result.ok();
    }

    @Operation(summary = "编辑文学作品")
    @PutMapping("/work")
    @SaCheckPermission("classics:literature:work:edit")
    public Result<?> workUpdate(@RequestBody @Valid LiteraryWorkUpdateDTO dto) {
        com.rx.admin.modules.literature.common.entity.LiteraryWork entity = literaryWorkService.getById(dto.getId());
        literaryWorkConvert.updateEntity(entity, dto);
        literaryWorkService.updateWithWordCount(entity);
        return Result.ok();
    }

    @Operation(summary = "删除文学作品")
    @DeleteMapping("/work/{id}")
    @SaCheckPermission("classics:literature:work:delete")
    public Result<?> workDelete(@PathVariable Long id) {
        literaryWorkService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "批量删除文学作品")
    @DeleteMapping("/work/batch")
    @SaCheckPermission("classics:literature:work:delete")
    public Result<?> workBatchDelete(@RequestBody List<Long> ids) {
        literaryWorkService.removeByIds(ids);
        return Result.ok();
    }

    @Operation(summary = "批量重新统计所有作品真实字数")
    @PostMapping("/work/recount-word-count")
    @SaCheckPermission("classics:literature:work:edit")
    public Result<?> recountWordCount() {
        int updated = literaryWorkService.recountAllWordCount();
        return Result.ok(updated);
    }
}