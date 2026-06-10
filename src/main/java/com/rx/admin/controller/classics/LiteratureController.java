package com.rx.admin.controller.classics;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.OperateLog;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.classics.Author;
import com.rx.admin.entity.classics.ContentCategory;
import com.rx.admin.entity.classics.Dynasty;
import com.rx.admin.entity.classics.Genre;
import com.rx.admin.entity.classics.LiteraryWork;
import com.rx.admin.service.classics.AuthorService;
import com.rx.admin.service.classics.ContentCategoryService;
import com.rx.admin.service.classics.DynastyService;
import com.rx.admin.service.classics.GenreService;
import com.rx.admin.service.classics.LiteraryWorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "历代文学数据")
@RestController
@RequestMapping("/api/classics/literature")
public class LiteratureController {

    private final AuthorService authorService;
    private final DynastyService dynastyService;
    private final GenreService genreService;
    private final ContentCategoryService contentCategoryService;
    private final LiteraryWorkService literaryWorkService;

    public LiteratureController(AuthorService authorService,
                                DynastyService dynastyService,
                                GenreService genreService,
                                ContentCategoryService contentCategoryService,
                                LiteraryWorkService literaryWorkService) {
        this.authorService = authorService;
        this.dynastyService = dynastyService;
        this.genreService = genreService;
        this.contentCategoryService = contentCategoryService;
        this.literaryWorkService = literaryWorkService;
    }

    // ====== 作者 ======

    @Operation(summary = "作者分页查询")
    @GetMapping("/author/page")
    @SaCheckLogin
    public Result<PageResult<Author>> authorPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(authorService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "作者详情")
    @GetMapping("/author/{id}")
    @SaCheckLogin
    public Result<Author> authorDetail(@PathVariable Long id) {
        return Result.ok(authorService.getById(id));
    }

    @Operation(summary = "获取所有作者")
    @GetMapping("/author/all")
    @SaCheckLogin
    public Result<List<Author>> authorAll() {
        return Result.ok(authorService.listAll());
    }

    @Operation(summary = "新增作者")
    @PostMapping("/author")
    @SaCheckPermission("classics:literature:author:add")
    public Result<?> authorAdd(@RequestBody Author author) {
        authorService.save(author);
        return Result.ok();
    }

    @Operation(summary = "编辑作者")
    @PutMapping("/author")
    @SaCheckPermission("classics:literature:author:edit")
    public Result<?> authorUpdate(@RequestBody Author author) {
        authorService.updateById(author);
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
    public Result<PageResult<Dynasty>> dynastyPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(dynastyService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "朝代详情")
    @GetMapping("/dynasty/{id}")
    @SaCheckLogin
    public Result<Dynasty> dynastyDetail(@PathVariable Long id) {
        return Result.ok(dynastyService.getById(id));
    }

    @Operation(summary = "获取所有朝代")
    @GetMapping("/dynasty/all")
    @SaCheckLogin
    public Result<List<Dynasty>> dynastyAll() {
        return Result.ok(dynastyService.listAll());
    }

    @Operation(summary = "新增朝代")
    @PostMapping("/dynasty")
    @SaCheckPermission("classics:literature:dynasty:add")
    public Result<?> dynastyAdd(@RequestBody Dynasty dynasty) {
        dynastyService.save(dynasty);
        return Result.ok();
    }

    @Operation(summary = "编辑朝代")
    @PutMapping("/dynasty")
    @SaCheckPermission("classics:literature:dynasty:edit")
    public Result<?> dynastyUpdate(@RequestBody Dynasty dynasty) {
        dynastyService.updateById(dynasty);
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
    public Result<PageResult<Genre>> genrePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(genreService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "体裁详情")
    @GetMapping("/genre/{id}")
    @SaCheckLogin
    public Result<Genre> genreDetail(@PathVariable Long id) {
        return Result.ok(genreService.getById(id));
    }

    @Operation(summary = "获取所有体裁")
    @GetMapping("/genre/all")
    @SaCheckLogin
    public Result<List<Genre>> genreAll() {
        return Result.ok(genreService.listAll());
    }

    @Operation(summary = "新增体裁")
    @PostMapping("/genre")
    @SaCheckPermission("classics:literature:genre:add")
    public Result<?> genreAdd(@RequestBody Genre genre) {
        genreService.save(genre);
        return Result.ok();
    }

    @Operation(summary = "编辑体裁")
    @PutMapping("/genre")
    @SaCheckPermission("classics:literature:genre:edit")
    public Result<?> genreUpdate(@RequestBody Genre genre) {
        genreService.updateById(genre);
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
    public Result<PageResult<ContentCategory>> categoryPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(contentCategoryService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "内容分类详情")
    @GetMapping("/category/{id}")
    @SaCheckLogin
    public Result<ContentCategory> categoryDetail(@PathVariable Long id) {
        return Result.ok(contentCategoryService.getById(id));
    }

    @Operation(summary = "获取所有内容分类")
    @GetMapping("/category/all")
    @SaCheckLogin
    public Result<List<ContentCategory>> categoryAll() {
        return Result.ok(contentCategoryService.listAll());
    }

    @Operation(summary = "新增内容分类")
    @PostMapping("/category")
    @SaCheckPermission("classics:literature:category:add")
    public Result<?> categoryAdd(@RequestBody ContentCategory category) {
        contentCategoryService.save(category);
        return Result.ok();
    }

    @Operation(summary = "编辑内容分类")
    @PutMapping("/category")
    @SaCheckPermission("classics:literature:category:edit")
    public Result<?> categoryUpdate(@RequestBody ContentCategory category) {
        contentCategoryService.updateById(category);
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
    public Result<PageResult<LiteraryWork>> workPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long dynastyId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Long authorId) {
        return Result.ok(literaryWorkService.pageQuery(page, size, keyword, dynastyId, genreId, authorId));
    }

    @Operation(summary = "文学作品详情")
    @GetMapping("/work/{id}")
    @SaCheckLogin
    public Result<LiteraryWork> workDetail(@PathVariable Long id) {
        return Result.ok(literaryWorkService.getDetail(id));
    }

    @Operation(summary = "获取所有文学作品")
    @GetMapping("/work/all")
    @SaCheckLogin
    public Result<List<LiteraryWork>> workAll() {
        return Result.ok(literaryWorkService.listAll());
    }

    @Operation(summary = "新增文学作品")
    @PostMapping("/work")
    @SaCheckPermission("classics:literature:work:add")
    public Result<?> workAdd(@RequestBody LiteraryWork work) {
        literaryWorkService.saveWithWordCount(work);
        return Result.ok();
    }

    @Operation(summary = "编辑文学作品")
    @PutMapping("/work")
    @SaCheckPermission("classics:literature:work:edit")
    public Result<?> workUpdate(@RequestBody LiteraryWork work) {
        literaryWorkService.updateWithWordCount(work);
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
