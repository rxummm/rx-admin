package com.rx.admin.modules.as400.techblog.controller;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.as400.techblog.entity.TechBlogArticle;
import com.rx.admin.modules.as400.techblog.convert.TechBlogConvert;
import com.rx.admin.modules.as400.techblog.dto.TechBlogCreateDTO;
import com.rx.admin.modules.as400.techblog.dto.BatchDeleteDTO;
import com.rx.admin.modules.as400.techblog.dto.TechBlogUpdateDTO;
import com.rx.admin.modules.as400.techblog.vo.TechBlogVO;
import com.rx.admin.modules.as400.techblog.service.TechBlogArticleService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 技术博客 — 多源文章抓取与展示
 */
@Tag(name = "技术博客")
@Slf4j
@RestController
@RequestMapping("/api/techblog")
@RequiredArgsConstructor
public class TechBlogController {

    private final TechBlogArticleService articleService;
    private final TechBlogConvert techBlogConvert;

    @Operation(summary = "分页查询文章列表")
    @GetMapping("/articles")
    @SaCheckPermission("techblog:query")
    public Result<PageResult<TechBlogVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source) {
        PageResult<TechBlogArticle> pageResult = articleService.pageQuery(page, size, keyword, category, source);
        List<TechBlogVO> voList = techBlogConvert.toVOList(pageResult.getRecords());
        return Result.ok(PageResult.of(pageResult.getTotal(), pageResult.getPage(), pageResult.getSize(), voList));
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/articles/{id}")
    @SaCheckPermission("techblog:query")
    public Result<TechBlogVO> detail(@PathVariable Long id) {
        TechBlogArticle article = articleService.getDetail(id);
        if (article == null) {
            return Result.fail(404, "文章不存在");
        }
        return Result.ok(techBlogConvert.toVO(article));
    }

    @Operation(summary = "获取所有分类标签")
    @GetMapping("/categories")
    public Result<List<String>> categories(
            @RequestParam(required = false) String source) {
        return Result.ok(articleService.getAllCategories(source));
    }

    @Operation(summary = "获取最近文章")
    @GetMapping("/recent")
    public Result<List<TechBlogVO>> recent(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String source) {
        List<TechBlogArticle> articles = articleService.getRecent(limit, source);
        return Result.ok(techBlogConvert.toVOList(articles));
    }

    @Operation(summary = "触发文章抓取")
    @PostMapping("/fetch")
    @SaCheckPermission("techblog:sync")
    public Result<String> fetch(@RequestBody Map<String, String> body) {
        String source = body.getOrDefault("source", "nicklitten");
        articleService.startFetch(source);
        return Result.ok("抓取任务已启动 (source=" + source + ")，请通过 /api/techblog/progress?source=" + source + " 查看进度");
    }

    @Operation(summary = "新增文章")
    @PostMapping("/articles")
    @SaCheckPermission("techblog:add")
    public Result<TechBlogVO> create(@RequestBody @Valid TechBlogCreateDTO dto) {
        TechBlogArticle article = techBlogConvert.toEntity(dto);
        article.setSort(0);
        article.setViewCount(0);
        articleService.save(article);
        return Result.ok(techBlogConvert.toVO(article));
    }

    @Operation(summary = "更新文章")
    @PutMapping("/articles/{id}")
    @SaCheckPermission("techblog:edit")
    public Result<String> update(@PathVariable Long id, @RequestBody @Valid TechBlogUpdateDTO dto) {
        TechBlogArticle article = articleService.getById(id);
        if (article == null) {
            return Result.fail(404, "文章不存在");
        }
        techBlogConvert.updateEntity(dto, article);
        articleService.updateById(article);
        return Result.ok("更新成功");
    }

    @Operation(summary = "删除单篇文章")
    @DeleteMapping("/articles/{id}")
    @SaCheckPermission("techblog:delete")
    public Result<?> delete(@PathVariable Long id) {
        if (articleService.getById(id) == null) {
            return Result.fail(404, "文章不存在");
        }
        articleService.removeById(id);
        return Result.ok("删除成功");
    }

    @Operation(summary = "批量删除文章")
    @DeleteMapping("/articles/batch")
    @SaCheckPermission("techblog:batchDelete")
    public Result<?> batchDelete(@RequestBody BatchDeleteDTO dto) {
        List<Integer> rawIds = dto.getIds();
        if (rawIds == null || rawIds.isEmpty()) {
            return Result.fail(400, "ids不能为空");
        }
        List<Long> ids = rawIds.stream().map(id -> id.longValue()).toList();
        articleService.removeByIds(ids);
        return Result.ok("已删除 " + ids.size() + " 篇文章");
    }

    @Operation(summary = "查看抓取进度")
    @GetMapping("/progress")
    public Result<Map<String, Object>> progress(
            @RequestParam(required = false) String source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source != null && !source.isBlank()) {
            result.put("source", source);
            result.put("progress", articleService.getFetchProgress(source));
            result.put("logs", articleService.getFetchLogs(source));
        } else {
            result.put("allProgress", articleService.getAllProgress());
        }
        return Result.ok(result);
    }
}