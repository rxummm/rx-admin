package com.rx.admin.controller;

import com.rx.admin.common.result.PageResult;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.TechBlogArticle;
import com.rx.admin.service.TechBlogArticleService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 技术博客 — 多源文章抓取与展示
 */
@Slf4j
@RestController
@RequestMapping("/api/techblog")
@RequiredArgsConstructor
public class TechBlogController {

    private final TechBlogArticleService articleService;

    /** 分页查询文章列表 */
    @GetMapping("/articles")
    @SaCheckPermission("techblog:query")
    public Result<PageResult<TechBlogArticle>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source) {
        return Result.ok(articleService.pageQuery(page, size, keyword, category, source));
    }

    /** 获取文章详情 */
    @GetMapping("/articles/{id}")
    @SaCheckPermission("techblog:query")
    public Result<TechBlogArticle> detail(@PathVariable Long id) {
        TechBlogArticle article = articleService.getDetail(id);
        if (article == null) {
            return Result.fail(404, "文章不存在");
        }
        return Result.ok(article);
    }

    /** 获取所有分类标签（可选按来源过滤） */
    @GetMapping("/categories")
    public Result<List<String>> categories(
            @RequestParam(required = false) String source) {
        return Result.ok(articleService.getAllCategories(source));
    }

    /** 获取最近文章（可选按来源过滤） */
    @GetMapping("/recent")
    public Result<List<TechBlogArticle>> recent(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String source) {
        return Result.ok(articleService.getRecent(limit, source));
    }

    /** 触发指定来源的文章抓取 */
    @PostMapping("/fetch")
    @SaCheckPermission("techblog:sync")
    public Result<String> fetch(@RequestBody Map<String, String> body) {
        String source = body.getOrDefault("source", "nicklitten");
        articleService.startFetch(source);
        return Result.ok("抓取任务已启动 (source=" + source + ")，请通过 /api/techblog/progress?source=" + source + " 查看进度");
    }

    /** 新增文章 */
    @PostMapping("/articles")
    @SaCheckPermission("techblog:add")
    public Result<?> create(@RequestBody Map<String, Object> body) {
        TechBlogArticle article = new TechBlogArticle();
        article.setTitle((String) body.getOrDefault("title", ""));
        article.setAuthor((String) body.getOrDefault("author", ""));
        article.setSource((String) body.getOrDefault("source", ""));
        article.setPublishDate((String) body.getOrDefault("publishDate", ""));
        article.setCategories((String) body.getOrDefault("categories", ""));
        article.setExcerptText((String) body.getOrDefault("excerptText", ""));
        article.setContentHtml((String) body.getOrDefault("contentHtml", ""));
        article.setContentText((String) body.getOrDefault("contentText", ""));
        article.setCoverImage((String) body.getOrDefault("coverImage", ""));
        article.setSort(0);
        article.setViewCount(0);
        articleService.save(article);
        return Result.ok(article);
    }

    /** 更新文章 */
    @PutMapping("/articles/{id}")
    @SaCheckPermission("techblog:edit")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (articleService.getById(id) == null) {
            return Result.fail(404, "文章不存在");
        }
        TechBlogArticle article = new TechBlogArticle();
        article.setId(id);
        if (body.containsKey("title")) article.setTitle((String) body.get("title"));
        if (body.containsKey("author")) article.setAuthor((String) body.get("author"));
        if (body.containsKey("categories")) article.setCategories((String) body.get("categories"));
        if (body.containsKey("publishDate")) article.setPublishDate((String) body.get("publishDate"));
        if (body.containsKey("excerptText")) article.setExcerptText((String) body.get("excerptText"));
        if (body.containsKey("source")) article.setSource((String) body.get("source"));
        if (body.containsKey("contentHtml")) article.setContentHtml((String) body.get("contentHtml"));
        if (body.containsKey("contentText")) article.setContentText((String) body.get("contentText"));
        if (body.containsKey("coverImage")) article.setCoverImage((String) body.get("coverImage"));
        articleService.updateById(article);
        return Result.ok("更新成功");
    }

    /** 删除单篇文章 */
    @DeleteMapping("/articles/{id}")
    @SaCheckPermission("techblog:delete")
    public Result<?> delete(@PathVariable Long id) {
        if (articleService.getById(id) == null) {
            return Result.fail(404, "文章不存在");
        }
        articleService.removeById(id);
        return Result.ok("删除成功");
    }

    /** 批量删除文章 */
    @DeleteMapping("/articles/batch")
    @SaCheckPermission("techblog:batchDelete")
    public Result<?> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        if (rawIds == null || rawIds.isEmpty()) {
            return Result.fail(400, "ids不能为空");
        }
        List<Long> ids = rawIds.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
        articleService.removeByIds(ids);
        return Result.ok("已删除 " + ids.size() + " 篇文章");
    }

    /** 查看抓取进度（可选按 source 过滤） */
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
