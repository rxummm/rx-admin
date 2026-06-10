package com.rx.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.TechBlogArticle;
import com.rx.admin.mapper.TechBlogArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;

/**
 * 技术博客文章服务（含多源并发爬虫抓取逻辑）
 *
 * 数据来源:
 *   nicklitten - https://www.nicklitten.com/blog/
 *   faq400     - https://blog.faq400.com/en/
 *   rpgpgm     - https://www.rpgpgm.com/
 *   as400sql   - https://www.as400andsqltricks.com/
 *   apimy      - https://apimymymy.wordpress.com/blog/
 */
@Slf4j
@Service
public class TechBlogArticleService extends ServiceImpl<TechBlogArticleMapper, TechBlogArticle> {

    // ==================== 并发状态管理 ====================

    /** 每个 source 独立爬取进度（-1=未开始, 0-99=进行中, 100=完成） */
    private final Map<String, AtomicInteger> progressMap = new ConcurrentHashMap<>();

    /** 每个 source 独立爬取日志 */
    private final Map<String, List<String>> logsMap = new ConcurrentHashMap<>();

    /** 固定 4 线程池，支持最多 4 个源并发抓取 */
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    // ==================== 各来源 URL 常量 ====================

    private static final Map<String, String> BASE_URLS = Map.of(
        "nicklitten", "https://www.nicklitten.com",
        "faq400",     "https://blog.faq400.com",
        "rpgpgm",     "https://www.rpgpgm.com",
        "as400sql",   "https://www.as400andsqltricks.com",
        "apimy",      "https://apimymymy.wordpress.com"
    );

    private static final Map<String, String> BLOG_URLS = Map.of(
        "nicklitten", "https://www.nicklitten.com/blog/",
        "faq400",     "https://blog.faq400.com/en/",
        "rpgpgm",     "https://www.rpgpgm.com/p/list-of-all-posts.html",
        "as400sql",   "https://www.as400andsqltricks.com/",
        "apimy",      "https://apimymymy.wordpress.com/blog/"
    );

    /** 所有已注册的来源标识 */
    public static final List<String> ALL_SOURCES = List.of("nicklitten", "faq400", "rpgpgm", "as400sql", "apimy");

    /** 列表页抓取超时（毫秒），通过 app.techblog.page-timeout-ms 配置 */
    @Value("${app.techblog.page-timeout-ms:15000}")
    private int pageTimeoutMs;

    /** 单篇文章详情抓取超时（毫秒），通过 app.techblog.article-timeout-ms 配置 */
    @Value("${app.techblog.article-timeout-ms:30000}")
    private int articleTimeoutMs;

    /** 批量抓取页间请求延迟（毫秒），通过 app.techblog.request-delay-ms 配置 */
    @Value("${app.techblog.request-delay-ms:1000}")
    private int requestDelayMs;

    // ==================== 查询 ====================

    /** 分页查询文章列表（可选按来源过滤） */
    public PageResult<TechBlogArticle> pageQuery(int pageNum, int pageSize, String keyword, String category, String source) {
        LambdaQueryWrapper<TechBlogArticle> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(TechBlogArticle::getTitle, keyword)
                .or()
                .like(TechBlogArticle::getContentText, keyword)
                .or()
                .like(TechBlogArticle::getExcerptText, keyword));
        }
        if (category != null && !category.isBlank()) {
            wrapper.like(TechBlogArticle::getCategories, category);
        }
        if (source != null && !source.isBlank()) {
            wrapper.eq(TechBlogArticle::getSource, source);
        }
        wrapper.orderByDesc(TechBlogArticle::getPublishDate)
               .orderByAsc(TechBlogArticle::getSort)
               .select(TechBlogArticle.class, info -> !"content_html".equals(info.getColumn())
                                                      && !"content_text".equals(info.getColumn()));
        Page<TechBlogArticle> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords());
    }

    /** 获取文章详情（含正文） */
    public TechBlogArticle getDetail(Long id) {
        TechBlogArticle article = getById(id);
        if (article != null) {
            article.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
            updateById(article);
        }
        return article;
    }

    /** 获取所有分类标签（可选按来源过滤） */
    public List<String> getAllCategories(String source) {
        LambdaQueryWrapper<TechBlogArticle> queryWrapper = new LambdaQueryWrapper<TechBlogArticle>()
            .select(TechBlogArticle::getCategories)
            .isNotNull(TechBlogArticle::getCategories)
            .ne(TechBlogArticle::getCategories, "");
        if (source != null && !source.isBlank()) {
            queryWrapper.eq(TechBlogArticle::getSource, source);
        }
        List<TechBlogArticle> articles = list(queryWrapper);
        Set<String> catSet = new LinkedHashSet<>();
        for (TechBlogArticle a : articles) {
            if (a.getCategories() != null) {
                for (String c : a.getCategories().split(",")) {
                    String trimmed = c.trim();
                    if (!trimmed.isEmpty()) {
                        catSet.add(trimmed);
                    }
                }
            }
        }
        return new ArrayList<>(catSet);
    }

    /** 获取最近N篇文章（可选按来源过滤） */
    public List<TechBlogArticle> getRecent(int limit, String source) {
        LambdaQueryWrapper<TechBlogArticle> wrapper = new LambdaQueryWrapper<TechBlogArticle>()
            .orderByDesc(TechBlogArticle::getPublishDate)
            .last("LIMIT " + limit)
            .select(TechBlogArticle.class, info -> !"content_html".equals(info.getColumn())
                                                  && !"content_text".equals(info.getColumn()));
        if (source != null && !source.isBlank()) {
            wrapper.eq(TechBlogArticle::getSource, source);
        }
        return list(wrapper);
    }

    // ==================== 爬取进度 ====================

    /** 获取指定来源的爬取进度 */
    public int getFetchProgress(String source) {
        AtomicInteger progress = progressMap.get(source);
        return progress != null ? progress.get() : -1;
    }

    /** 获取指定来源的爬取日志 */
    public List<String> getFetchLogs(String source) {
        List<String> logs = logsMap.get(source);
        return logs != null ? new ArrayList<>(logs) : Collections.emptyList();
    }

    /** 获取所有来源的进度概览 */
    public Map<String, Integer> getAllProgress() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String src : ALL_SOURCES) {
            result.put(src, getFetchProgress(src));
        }
        return result;
    }

    // ==================== 爬虫入口 ====================

    /**
     * 触发指定来源的文章抓取（异步，线程池执行）
     * 每个 source 独立守卫：同一 source 重复调用会被忽略，不影响其他 source
     */
    public void startFetch(String source) {
        if (!ALL_SOURCES.contains(source)) {
            log.warn("未知的博客来源: {}", source);
            throw new IllegalArgumentException("未知的博客来源: " + source + "，有效值: " + ALL_SOURCES);
        }

        AtomicInteger progress = progressMap.computeIfAbsent(source, k -> new AtomicInteger(-1));
        if (progress.get() >= 0 && progress.get() < 100) {
            log.info("来源 {} 正在抓取中 (进度={}%), 忽略重复请求", source, progress.get());
            return;
        }

        progress.set(0);
        logsMap.computeIfAbsent(source, k -> Collections.synchronizedList(new ArrayList<>())).clear();
        List<String> logs = logsMap.get(source);
        logs.add("开始抓取: " + source + " (" + BASE_URLS.getOrDefault(source, "") + ")");

        executor.submit(() -> {
            try {
                switch (source) {
                    case "nicklitten" -> doFetchNicklitten(source);
                    case "faq400"     -> doFetchFaq400(source);
                    case "rpgpgm"     -> doFetchRpgpgm(source);
                    case "as400sql"   -> doFetchAs400sql(source);
                    case "apimy"      -> doFetchApimy(source);
                }
            } catch (Exception e) {
                progress.set(-1);
                logFetch(source, "❌ 抓取出错: " + e.getMessage());
                log.error("{} 抓取失败", source, e);
            }
        });
    }

    // ==================== nicklitten 抓取 (Jsoup) ====================

    private void doFetchNicklitten(String source) {
        String baseUrl = BASE_URLS.get(source);
        String blogUrl = BLOG_URLS.get(source);
        int maxPages = 76;
        int totalSaved = 0;

        try {
            Document firstPage = Jsoup.connect(blogUrl)
                .userAgent("Mozilla/5.0")
                .timeout(pageTimeoutMs)
                .get();
            int totalPages = detectNicklittenPages(firstPage, maxPages);
            logFetch(source, "检测到博客总页数: " + totalPages);

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                String pageUrl = pageNum == 1 ? blogUrl : blogUrl + "page/" + pageNum + "/";
                logFetch(source, "正在抓取第 " + pageNum + "/" + totalPages + " 页...");

                List<TechBlogArticle> articles = scrapeNicklittenList(pageUrl, baseUrl, source);
                for (TechBlogArticle article : articles) {
                    try {
                        if (existsBySourceUrl(article.getSourceUrl())) {
                            continue;
                        }
                        scrapeNicklittenDetail(article);
                        article.setSource(source);
                        save(article);
                        totalSaved++;
                        logFetch(source, "  ✓ [" + totalSaved + "] " + article.getTitle());
                        Thread.sleep(requestDelayMs);
                    } catch (Exception e) {
                        logFetch(source, "  ✗ 失败: " + article.getTitle() + " - " + e.getMessage());
                    }
                }

                setProgress(source, (int) ((pageNum * 100.0) / totalPages));
                if (pageNum < totalPages) Thread.sleep(requestDelayMs);
            }

            setProgress(source, 100);
            logFetch(source, "✅ 抓取完成! 共保存 " + totalSaved + " 篇新文章");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int detectNicklittenPages(Document doc, int fallback) {
        try {
            Elements pageLinks = doc.select(".tcb-pagination a");
            int maxPage = 1;
            for (Element link : pageLinks) {
                String href = link.attr("href");
                if (href.contains("/page/")) {
                    try {
                        String num = href.replaceAll(".*/page/(\\d+)/.*", "$1");
                        int p = Integer.parseInt(num);
                        if (p > maxPage) maxPage = p;
                    } catch (NumberFormatException ignored) {}
                }
            }
            return maxPage > 1 ? maxPage : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private List<TechBlogArticle> scrapeNicklittenList(String url, String baseUrl, String source) throws Exception {
        Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(pageTimeoutMs)
            .get();

        List<TechBlogArticle> articles = new ArrayList<>();
        Elements posts = doc.select("article");

        for (Element post : posts) {
            TechBlogArticle article = new TechBlogArticle();
            article.setAuthor("Nick Litten");
            article.setSort(0);
            article.setViewCount(0);

            Element titleEl = post.selectFirst("h2 a, h3 a, .entry-title a, h2.entry-title, h1.entry-title a");
            if (titleEl == null) titleEl = post.selectFirst("a[rel=bookmark]");
            if (titleEl == null) titleEl = post.selectFirst(".post-title a, .blog-title a");
            if (titleEl != null) {
                article.setTitle(titleEl.text().trim());
                String href = titleEl.attr("href");
                if (href.isEmpty()) href = titleEl.parent() != null ? titleEl.parent().attr("href") : "";
                article.setSourceUrl(href);
                if (!href.isEmpty()) {
                    article.setSlug(href.replace(baseUrl + "/", "").replaceAll("/$", ""));
                }
            }
            if (article.getSourceUrl() == null || article.getSourceUrl().isEmpty()) continue;

            Element dateEl = post.selectFirst("time, .entry-date, .post-date, .published");
            if (dateEl == null) dateEl = post.selectFirst("[datetime]");
            if (dateEl != null) {
                String dateStr = dateEl.attr("datetime");
                if (dateStr.isEmpty()) dateStr = dateEl.text().trim();
                article.setPublishDate(normalizeDate(dateStr));
            }

            Elements catEls = post.select(".cat-links a, .category a, [rel=category], .entry-categories a");
            if (catEls.isEmpty()) catEls = post.select("a[href*='/category/']");
            String cats = catEls.stream().map(e -> e.text().trim()).filter(s -> !s.isEmpty()).collect(Collectors.joining(", "));
            article.setCategories(cats);

            Element excerptEl = post.selectFirst(".entry-summary, .post-excerpt, .entry-content p");
            if (excerptEl == null) excerptEl = post.selectFirst(".tcb-post-content p");
            if (excerptEl != null) article.setExcerptText(excerptEl.text().trim());

            Element imgEl = post.selectFirst("img");
            if (imgEl != null) {
                String src = imgEl.attr("src");
                if (src.isEmpty()) src = imgEl.attr("data-src");
                if (src.isEmpty()) src = imgEl.attr("data-lazy-src");
                if (!src.isEmpty()) article.setCoverImage(src);
            }

            articles.add(article);
        }
        return articles;
    }

    private void scrapeNicklittenDetail(TechBlogArticle article) throws Exception {
        Document doc = Jsoup.connect(article.getSourceUrl())
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(pageTimeoutMs)
            .get();

        Element titleEl = doc.selectFirst("h1.entry-title, h1.post-title, h1");
        if (titleEl != null) {
            String detailTitle = titleEl.text().trim();
            if (!detailTitle.isEmpty()) article.setTitle(detailTitle);
        }

        Element contentEl = doc.selectFirst(".entry-content, .post-content, .tcb-post-content, article .content");
        if (contentEl == null) contentEl = doc.selectFirst("article");

        if (contentEl != null) {
            cleanupNoise(contentEl);
            article.setContentHtml(contentEl.html());
            String text = contentEl.text();
            article.setContentText(text.length() > 5000 ? text.substring(0, 5000) : text);
        }

        if (article.getPublishDate() == null || article.getPublishDate().isEmpty()) {
            Element dateEl = doc.selectFirst("time.published, time.entry-date, .post-date time, meta[property='article:published_time']");
            if (dateEl != null) {
                String ds = dateEl.attr("datetime");
                if (ds.isEmpty()) ds = dateEl.attr("content");
                if (ds.isEmpty()) ds = dateEl.text().trim();
                article.setPublishDate(normalizeDate(ds));
            }
        }

        if (article.getCategories() == null || article.getCategories().isEmpty()) {
            Elements catEls = doc.select(".cat-links a, .category a, a[rel=category]");
            String cats = catEls.stream().map(e -> e.text().trim()).filter(s -> !s.isEmpty()).collect(Collectors.joining(", "));
            article.setCategories(cats);
        }

        if (article.getCoverImage() == null || article.getCoverImage().isEmpty()) {
            Element imgEl = contentEl != null ? contentEl.selectFirst("img") : null;
            if (imgEl == null) imgEl = doc.selectFirst(".entry-content img, .post-content img");
            if (imgEl != null) {
                String src = imgEl.attr("src");
                if (src.isEmpty()) src = imgEl.attr("data-src");
                if (!src.isEmpty()) article.setCoverImage(src);
            }
        }
    }

    // ==================== faq400 抓取 (WordPress RSS Feed) ====================

    private void doFetchFaq400(String source) {
        int totalSaved = 0;
        try {
            // 先探第一页获取总页数（通过标题 "Page X – BlogFaq400" 解析）
            logFetch(source, "通过 WordPress RSS Feed 获取文章...");
            String firstPageUrl = "https://blog.faq400.com/en/feed/?paged=1";
            Document firstDoc = Jsoup.connect(firstPageUrl)
                .userAgent("Mozilla/5.0")
                .timeout(pageTimeoutMs)
                .ignoreContentType(true)
                .get();
            int totalPages = detectFaq400FeedPages(firstDoc, source);

            for (int page = 1; page <= totalPages; page++) {
                if (page > 1) Thread.sleep(requestDelayMs);
                String feedUrl = "https://blog.faq400.com/en/feed/?paged=" + page;
                logFetch(source, "正在抓取第 " + page + "/" + totalPages + " 页 RSS...");
                setProgress(source, (int) ((page * 100.0) / totalPages));

                Document feedDoc;
                if (page == 1) {
                    feedDoc = firstDoc;
                } else {
                    feedDoc = Jsoup.connect(feedUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(pageTimeoutMs)
                        .ignoreContentType(true)
                        .get();
                }

                Elements items = feedDoc.select("item");
                if (items.isEmpty()) break;

                for (Element item : items) {
                    try {
                        // 提取链接
                        Element linkEl = item.selectFirst("link");
                        if (linkEl == null) continue;
                        String url = linkEl.text().trim();
                        if (url.isEmpty()) continue;
                        if (existsBySourceUrl(url)) continue;

                        TechBlogArticle article = new TechBlogArticle();
                        article.setSourceUrl(url);
                        article.setSource(source);
                        article.setSort(0);
                        article.setViewCount(0);

                        // 标题
                        Element titleEl = item.selectFirst("title");
                        if (titleEl != null) article.setTitle(titleEl.text().trim());

                        // 链接中提取 slug
                        if (!url.isEmpty()) {
                            String slug = url.replaceAll(".*?/([^/]+/?[^/]*)/?$", "$1").replaceAll("/$", "");
                            article.setSlug(slug);
                        }

                        // 日期
                        Element pubDateEl = item.selectFirst("pubDate");
                        if (pubDateEl != null) {
                            article.setPublishDate(normalizeDate(pubDateEl.text().trim()));
                        }

                        // 作者
                        Element creatorEl = item.selectFirst("dc|creator");
                        if (creatorEl != null) article.setAuthor(creatorEl.text().trim());

                        // 分类
                        Elements catEls = item.select("category");
                        article.setCategories(catEls.stream()
                            .map(e -> e.text().trim())
                            .filter(s -> !s.isEmpty() && !s.startsWith(" "))
                            .collect(Collectors.joining(", ")));

                        // 正文 (从 content:encoded 或 description 提取)
                        Element contentEl = item.selectFirst("content|encoded");
                        if (contentEl == null) contentEl = item.selectFirst("description");
                        if (contentEl != null) {
                            String html = contentEl.html();
                            article.setContentHtml(html);
                            Document tempDoc = Jsoup.parse(html);
                            // 提取 pure text
                            Element body = tempDoc.selectFirst(".entry-content, .post-content, article");
                            if (body == null) body = tempDoc.body();
                            String text = body != null ? body.text() : tempDoc.text();
                            article.setContentText(text.length() > 5000 ? text.substring(0, 5000) : text);

                            // 提取摘要 (取第一段)
                            String plain = tempDoc.text();
                            article.setExcerptText(plain.length() > 300 ? plain.substring(0, 300) : plain);

                            // 封面图
                            Element imgEl = tempDoc.selectFirst("img");
                            if (imgEl != null) {
                                String src = imgEl.attr("src");
                                if (src.isEmpty()) src = imgEl.attr("data-src");
                                if (!src.isEmpty()) article.setCoverImage(src);
                            }
                        }

                        save(article);
                        totalSaved++;
                        if (totalSaved % 10 == 0) {
                            logFetch(source, "  ✓ [" + totalSaved + "] " + article.getTitle());
                        }
                        Thread.sleep(requestDelayMs);
                    } catch (Exception e) {
                        String title = item.selectFirst("title") != null ? item.selectFirst("title").text() : "unknown";
                        logFetch(source, "  ✗ 失败: " + title + " - " + e.getMessage());
                    }
                }

                // 如果返回空或少于3条，结束
                if (items.size() < 3) break;
            }

            setProgress(source, 100);
            logFetch(source, "✅ 抓取完成! 共保存 " + totalSaved + " 篇新文章");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** faq400: 探测 RSS 分页总数（逐页探测直到空页，上限50页） */
    private int detectFaq400FeedPages(Document feedDoc, String source) {
        int page = 2;
        int maxProbe = 50;
        try {
            while (page <= maxProbe) {
                Thread.sleep(500);
                String url = "https://blog.faq400.com/en/feed/?paged=" + page;
                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(pageTimeoutMs)
                    .ignoreContentType(true)
                    .get();
                if (doc.select("item").isEmpty()) {
                    break;
                }
                page++;
            }
        } catch (Exception e) {
            logFetch(source, "Feed 分页探测中断于第" + page + "页: " + e.getMessage());
        }
        int pages = Math.max(page - 1, 1);
        logFetch(source, "Feed 检测到总页数: " + pages);
        return pages;
    }

    // ==================== rpgpgm 抓取 (Jsoup, 单页全列表) ====================

    private void doFetchRpgpgm(String source) {
        String blogUrl = BLOG_URLS.get(source);
        int totalSaved = 0;

        try {
            logFetch(source, "正在从全文章列表页提取所有文章URL...");
            Document listPage = fetchWithRetry(blogUrl, source);
            if (listPage == null) {
                // 列表页不可访问，fallback 到 Blogger RSS Feed
                logFetch(source, "⚠️ 列表页不可访问，尝试 Blogger RSS Feed...");
                totalSaved = doFetchRpgpgmFeedFallback(source);
                setProgress(source, 100);
                logFetch(source, "✅ RSS Feed 抓取完成! 共保存 " + totalSaved + " 篇新文章");
                return;
            }

            List<Map<String, String>> allPostUrls = scrapeRpgpgmAllUrls(listPage);
            int total = allPostUrls.size();
            logFetch(source, "共发现 " + total + " 篇文章链接");

            for (int i = 0; i < total; i++) {
                Map<String, String> item = allPostUrls.get(i);
                String url = item.get("url");
                String listTitle = item.get("title");

                try {
                    if (existsBySourceUrl(url)) {
                        if (i % 50 == 0 || totalSaved < 10) {
                            logFetch(source, "  跳过已存在: " + listTitle);
                        }
                        continue;
                    }

                    TechBlogArticle article = new TechBlogArticle();
                    article.setSourceUrl(url);
                    article.setTitle(listTitle);
                    article.setSort(0);
                    article.setViewCount(0);
                    article.setAuthor("Simon Hutchinson");
                    article.setSource(source);

                    scrapeRpgpgmDetail(article);
                    save(article);
                    totalSaved++;
                    if (totalSaved % 5 == 0 || totalSaved < 10) {
                        logFetch(source, "  ✓ [" + totalSaved + "/" + total + "] " + article.getTitle());
                    }
                    Thread.sleep(requestDelayMs);

                    if (i % 50 == 0) {
                        setProgress(source, (int) ((i * 100.0) / total));
                    }
                } catch (Exception e) {
                    logFetch(source, "  ✗ 失败: " + listTitle + " - " + e.getMessage());
                    // 单篇失败不中断，继续下一篇
                }
            }

            setProgress(source, 100);
            logFetch(source, "✅ 抓取完成! 共保存 " + totalSaved + " 篇新文章");
        } catch (Exception e) {
            logFetch(source, "❌ 抓取出错: " + e.getMessage());
            setProgress(source, -1);
            throw new RuntimeException(e);
        }
    }

    /** 带重试的页面抓取（3次，递增延迟） */
    private Document fetchWithRetry(String url, String source) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logFetch(source, "  尝试连接 (" + attempt + "/" + maxRetries + ")...");
                return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(articleTimeoutMs)
                    .get();
            } catch (Exception e) {
                logFetch(source, "  ✗ 连接失败 (" + attempt + "/" + maxRetries + "): " + e.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(2000L * attempt); } catch (InterruptedException ignored) {}
                }
            }
        }
        return null;
    }

    /** rpgpgm: Blogger RSS Feed fallback（当列表页不可访问时） */
    private int doFetchRpgpgmFeedFallback(String source) {
        int totalSaved = 0;
        int page = 1;
        int maxProbe = 5;
        while (page <= maxProbe) {
            try {
                String feedUrl = "https://www.rpgpgm.com/feeds/posts/default?max-results=100";
                if (page > 1) {
                    feedUrl += "&page=" + page;
                }
                logFetch(source, "  RSS Feed 第" + page + "页: " + feedUrl);
                Document feedDoc = Jsoup.connect(feedUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(pageTimeoutMs)
                    .ignoreContentType(true)
                    .get();
                Elements entries = feedDoc.select("entry");
                if (entries.isEmpty()) break;

                for (Element entry : entries) {
                    try {
                        Element linkEl = entry.selectFirst("link[rel=alternate]");
                        String url = linkEl != null ? linkEl.attr("href") : "";
                        if (url.isEmpty()) continue;
                        if (existsBySourceUrl(url)) continue;

                        TechBlogArticle article = new TechBlogArticle();
                        article.setSourceUrl(url);
                        article.setSource(source);
                        article.setSort(0);
                        article.setViewCount(0);
                        article.setAuthor("Simon Hutchinson");

                        Element titleEl = entry.selectFirst("title");
                        if (titleEl != null) article.setTitle(titleEl.text().trim());

                        if (!url.isEmpty()) {
                            String slug = url.replaceAll(".*?/(\\d+/\\d+/[^/]+)\\.html$", "$1");
                            article.setSlug(slug);
                        }

                        Element pubEl = entry.selectFirst("published");
                        if (pubEl != null) article.setPublishDate(normalizeDate(pubEl.text().trim()));

                        Elements catEls = entry.select("category");
                        article.setCategories(catEls.stream()
                            .map(e -> e.attr("term"))
                            .filter(s -> !s.isEmpty()).collect(Collectors.joining(", ")));

                        Element contentEl = entry.selectFirst("content");
                        if (contentEl != null) {
                            String html = contentEl.html();
                            article.setContentHtml(html);
                            Document tempDoc = Jsoup.parse(html);
                            String text = tempDoc.text();
                            article.setContentText(text.length() > 5000 ? text.substring(0, 5000) : text);
                            article.setExcerptText(text.length() > 300 ? text.substring(0, 300) : text);
                            Element imgEl = tempDoc.selectFirst("img");
                            if (imgEl != null) {
                                String src = imgEl.attr("src");
                                if (src.isEmpty()) src = imgEl.attr("data-src");
                                if (!src.isEmpty()) article.setCoverImage(src);
                            }
                        }

                        save(article);
                        totalSaved++;
                        if (totalSaved % 10 == 0) logFetch(source, "  ✓ [" + totalSaved + "] " + article.getTitle());
                        Thread.sleep(requestDelayMs);
                    } catch (Exception e) {
                        String title = entry.selectFirst("title") != null ? entry.selectFirst("title").text() : "unknown";
                        logFetch(source, "  ✗ 失败: " + title + " - " + e.getMessage());
                    }
                }
                if (entries.size() < 100) break;
                page++;
                Thread.sleep(requestDelayMs);
            } catch (Exception e) {
                if (page == 1) {
                    throw new RuntimeException("RSS Feed 也不可用: " + e.getMessage());
                }
                break;
            }
        }
        return totalSaved;
    }

    /** rpgpgm: 从全列表页提取所有文章URL和标题 */
    private List<Map<String, String>> scrapeRpgpgmAllUrls(Document doc) {
        List<Map<String, String>> result = new ArrayList<>();
        // 先尝试 Blogger 静态页面常见结构：.post-body 内的链接
        Elements links = doc.select(".post-body a[href*=\".html\"]");
        if (links.isEmpty()) links = doc.select("li a[href*=\".html\"]");
        if (links.isEmpty()) links = doc.select("a[href*=\"rpgpgm.com\"][href*=\".html\"]");
        log.info("rpgpgm: selector matched {} links on list page", links.size());
        for (Element link : links) {
            String href = link.attr("href");
            if (href.isEmpty()) continue;
            String title = link.text().trim();
            if (title.isEmpty()) continue;
            Map<String, String> item = new LinkedHashMap<>();
            item.put("url", href.startsWith("http") ? href : "https://www.rpgpgm.com" + href);
            item.put("title", title);
            result.add(item);
        }
        return result;
    }

    private void scrapeRpgpgmDetail(TechBlogArticle article) throws Exception {
        Document doc = Jsoup.connect(article.getSourceUrl())
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(pageTimeoutMs)
            .get();

        // 标题 (Blogger 格式)
        Element titleEl = doc.selectFirst(".post h3, h3.post-title");
        if (titleEl != null) {
            String detailTitle = titleEl.text().trim();
            if (!detailTitle.isEmpty()) article.setTitle(detailTitle);
        }

        // 日期 (优先 Blogger 标准格式: "Monday, January 30, 2017")
        Element dateEl = doc.selectFirst("h2.date-header");
        if (dateEl == null) dateEl = doc.selectFirst("abbr.published, time[datetime], .timestamp-link a");
        if (dateEl != null) {
            String ds = dateEl.attr("datetime");
            if (ds.isEmpty()) ds = dateEl.attr("title");
            if (ds.isEmpty()) ds = dateEl.text().trim();
            article.setPublishDate(normalizeDate(ds));
        }

        // 分类/标签
        Elements labelEls = doc.select(".post-labels a");
        article.setCategories(labelEls.stream().map(e -> e.text().trim()).filter(s -> !s.isEmpty()).collect(Collectors.joining(", ")));

        // 正文
        Element contentEl = doc.selectFirst(".post-body");
        if (contentEl != null) {
            cleanupNoise(contentEl);
            article.setContentHtml(contentEl.html());
            String text = contentEl.text();
            article.setContentText(text.length() > 5000 ? text.substring(0, 5000) : text);
        }

        // 封面图
        Element imgEl = doc.selectFirst(".post-body img:first-child");
        if (imgEl != null) {
            String src = imgEl.attr("src");
            if (!src.isEmpty()) article.setCoverImage(src);
        }
    }

    // ==================== as400sql 抓取 (Blogger Feed API 优先) ====================

    private void doFetchAs400sql(String source) {
        int totalSaved = 0;
        try {
            // 优先尝试 Blogger Feed API
            logFetch(source, "尝试通过 Blogger Feed API 获取文章...");
            String feedUrl = "https://www.as400andsqltricks.com/feeds/posts/default?max-results=500";
            Document feedDoc = Jsoup.connect(feedUrl)
                .userAgent("Mozilla/5.0")
                .timeout(pageTimeoutMs)
                .ignoreContentType(true)
                .get();

            Elements entries = feedDoc.select("entry");
            if (!entries.isEmpty()) {
                logFetch(source, "Feed API 可用，共获取 " + entries.size() + " 篇文章");
                totalSaved = processFeedEntries(source, entries);
            } else {
                logFetch(source, "Feed API 返回空，fallback 到年月归档爬取");
                totalSaved = doFetchAs400sqlFallback(source);
            }

            setProgress(source, 100);
            logFetch(source, "✅ 抓取完成! 共保存 " + totalSaved + " 篇新文章");
        } catch (Exception e) {
            logFetch(source, "Feed API 不可用: " + e.getMessage() + "，尝试 fallback...");
            try {
                totalSaved = doFetchAs400sqlFallback(source);
                setProgress(source, 100);
                logFetch(source, "✅ 抓取完成! 共保存 " + totalSaved + " 篇新文章");
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    private int processFeedEntries(String source, Elements entries) {
        int totalSaved = 0;
        int total = entries.size();

        for (int i = 0; i < total; i++) {
            Element entry = entries.get(i);
            try {
                // 提取 Feed XML 中的文章信息
                Element titleEl = entry.selectFirst("title");
                String title = titleEl != null ? titleEl.text().trim() : "";
                Element linkEl = entry.selectFirst("link[rel=alternate]");
                String url = linkEl != null ? linkEl.attr("href") : "";

                if (url.isEmpty()) continue;
                if (existsBySourceUrl(url)) {
                    if (i % 50 == 0) logFetch(source, "  跳过已存在: " + title);
                    continue;
                }

                TechBlogArticle article = new TechBlogArticle();
                article.setTitle(title);
                article.setSourceUrl(url);
                article.setSource(source);
                article.setSort(0);
                article.setViewCount(0);

                // 日期
                Element pubEl = entry.selectFirst("published");
                if (pubEl != null) article.setPublishDate(normalizeDate(pubEl.text().trim()));

                // 作者
                Element authorEl = entry.selectFirst("author name");
                if (authorEl != null) article.setAuthor(authorEl.text().trim());

                // 分类
                Elements catEls = entry.select("category");
                article.setCategories(catEls.stream().map(e -> e.attr("term")).filter(s -> !s.isEmpty()).collect(Collectors.joining(", ")));

                // 正文
                Element contentEl = entry.selectFirst("content");
                if (contentEl != null) {
                    String html = contentEl.html();
                    article.setContentHtml(html);
                    // 用 Jsoup 解析提取纯文本
                    Document tempDoc = Jsoup.parse(html);
                    String text = tempDoc.text();
                    article.setContentText(text.length() > 5000 ? text.substring(0, 5000) : text);
                }

                // 封面图
                Element thumbEl = entry.selectFirst("thumbnail");
                if (thumbEl == null) thumbEl = entry.selectFirst("media|thumbnail");
                if (thumbEl != null) {
                    String thumbUrl = thumbEl.attr("url");
                    if (!thumbUrl.isEmpty()) article.setCoverImage(thumbUrl);
                }

                save(article);
                totalSaved++;
                if (totalSaved % 10 == 0) {
                    logFetch(source, "  ✓ [" + totalSaved + "/" + total + "] " + title);
                    setProgress(source, (int) ((totalSaved * 100.0) / total));
                }
                Thread.sleep(requestDelayMs);
            } catch (Exception e) {
                logFetch(source, "  ✗ 失败: " + (entry.selectFirst("title") != null ? entry.selectFirst("title").text() : "unknown") + " - " + e.getMessage());
            }
        }
        return totalSaved;
    }

    private int doFetchAs400sqlFallback(String source) {
        int totalSaved = 0;
        // Fallback: 逐月归档页面爬取（从2021年到现在）
        logFetch(source, "Fallback: 通过年月归档逐月爬取...");
        int currentYear = java.time.Year.now().getValue();
        for (int year = currentYear; year >= 2021; year--) {
            for (int month = 12; month >= 1; month--) {
                String archiveUrl = String.format("https://www.as400andsqltricks.com/%d/%02d/", year, month);
                try {
                    Document doc = Jsoup.connect(archiveUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(pageTimeoutMs)
                        .get();

                    Elements postLinks = doc.select("article h2 a");
                    for (Element link : postLinks) {
                        String href = link.attr("href");
                        if (href.isEmpty()) continue;
                        if (existsBySourceUrl(href)) continue;

                        TechBlogArticle article = new TechBlogArticle();
                        article.setSourceUrl(href);
                        article.setTitle(link.text().trim());
                        article.setSource(source);
                        article.setSort(0);
                        article.setViewCount(0);

                        try {
                            scrapeAs400sqlDetail(article);
                            save(article);
                            totalSaved++;
                            logFetch(source, "  ✓ [" + totalSaved + "] " + article.getTitle());
                            Thread.sleep(requestDelayMs);
                        } catch (Exception e) {
                            logFetch(source, "  ✗ 失败: " + article.getTitle() + " - " + e.getMessage());
                        }
                    }
                } catch (Exception ignored) {
                    // 该月可能没有文章，正常跳过
                }
            }
        }
        return totalSaved;
    }

    private void scrapeAs400sqlDetail(TechBlogArticle article) throws Exception {
        Document doc = Jsoup.connect(article.getSourceUrl())
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(pageTimeoutMs)
            .get();

        Element titleEl = doc.selectFirst("h1.entry-title, h2.post-title");
        if (titleEl != null) {
            String detailTitle = titleEl.text().trim();
            if (!detailTitle.isEmpty()) article.setTitle(detailTitle);
        }

        Element dateMeta = doc.selectFirst("meta[itemprop='datePublished']");
        if (dateMeta != null) article.setPublishDate(normalizeDate(dateMeta.attr("content")));

        Element authorMeta = doc.selectFirst("meta[itemprop='author']");
        if (authorMeta != null) article.setAuthor(authorMeta.attr("content"));

        Elements labelEls = doc.select(".post-labels a");
        article.setCategories(labelEls.stream().map(e -> e.text().trim()).filter(s -> !s.isEmpty()).collect(Collectors.joining(", ")));

        Element contentEl = doc.selectFirst(".post-body, .entry-content");
        if (contentEl != null) {
            cleanupNoise(contentEl);
            article.setContentHtml(contentEl.html());
            String text = contentEl.text();
            article.setContentText(text.length() > 5000 ? text.substring(0, 5000) : text);
        }

        if (article.getCoverImage() == null || article.getCoverImage().isEmpty()) {
            Element imgEl = doc.selectFirst(".post-body img:first-child, .entry-content img:first-child");
            if (imgEl != null) {
                String src = imgEl.attr("src");
                if (!src.isEmpty()) article.setCoverImage(src);
            }
        }
    }

    // ==================== apimy 抓取 (WordPress.com REST API) ====================

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private void doFetchApimy(String source) {
        int totalSaved = 0;
        int perPage = 50;
        String siteId = "apimymymy.wordpress.com";

        try {
            logFetch(source, "通过 WordPress.com REST API 获取文章...");

            // 先获取第一页，得到总数
            String firstPageUrl = "https://public-api.wordpress.com/rest/v1.1/sites/" + siteId + "/posts/?number=" + perPage + "&page=1";
            JsonNode firstPage = fetchJson(firstPageUrl);
            int totalFound = firstPage.get("found").asInt();
            int totalPages = (int) Math.ceil((double) totalFound / perPage);
            logFetch(source, "REST API 共 " + totalFound + " 篇文章，分 " + totalPages + " 页");

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                String apiUrl = "https://public-api.wordpress.com/rest/v1.1/sites/" + siteId + "/posts/?number=" + perPage + "&page=" + pageNum;
                JsonNode root = (pageNum == 1) ? firstPage : fetchJson(apiUrl);

                logFetch(source, "正在抓取第 " + pageNum + "/" + totalPages + " 页...");
                setProgress(source, (int) ((pageNum * 100.0) / totalPages));

                JsonNode posts = root.get("posts");
                if (posts == null || !posts.isArray()) break;

                for (JsonNode post : posts) {
                    try {
                        String url = post.get("URL").asText();
                        if (url.isEmpty()) continue;
                        if (existsBySourceUrl(url)) continue;

                        TechBlogArticle article = new TechBlogArticle();
                        article.setSourceUrl(url);
                        article.setSource(source);
                        article.setSort(0);
                        article.setViewCount(0);

                        // 标题
                        String title = post.has("title") ? post.get("title").asText().trim() : "";
                        if (!title.isEmpty()) article.setTitle(title);

                        // Slug
                        if (post.has("slug")) {
                            article.setSlug(post.get("slug").asText());
                        }

                        // 日期
                        if (post.has("date")) {
                            article.setPublishDate(normalizeDate(post.get("date").asText()));
                        }

                        // 作者
                        JsonNode authorNode = post.get("author");
                        if (authorNode != null && authorNode.has("name")) {
                            article.setAuthor(authorNode.get("name").asText().trim());
                        }

                        // 分类
                        JsonNode categories = post.get("categories");
                        if (categories != null && categories.isObject()) {
                            List<String> catNames = new ArrayList<>();
                            Iterator<String> fieldNames = categories.fieldNames();
                            while (fieldNames.hasNext()) {
                                JsonNode cat = categories.get(fieldNames.next());
                                if (cat.has("name")) {
                                    catNames.add(cat.get("name").asText());
                                }
                            }
                            if (!catNames.isEmpty()) {
                                article.setCategories(String.join(", ", catNames));
                            }
                        }

                        // 正文 (content HTML)
                        String content = post.has("content") ? post.get("content").asText() : "";
                        if (!content.isEmpty()) {
                            article.setContentHtml(content);
                            Document tempDoc = Jsoup.parse(content);
                            String text = tempDoc.text();
                            article.setContentText(text.length() > 5000 ? text.substring(0, 5000) : text);
                            article.setExcerptText(text.length() > 300 ? text.substring(0, 300) : text);

                            // 封面图 (正文首图)
                            if (article.getCoverImage() == null || article.getCoverImage().isEmpty()) {
                                Element imgEl = tempDoc.selectFirst("img");
                                if (imgEl != null) {
                                    String src = imgEl.attr("src");
                                    if (src.isEmpty()) src = imgEl.attr("data-src");
                                    if (!src.isEmpty()) article.setCoverImage(src);
                                }
                            }
                        }

                        // 特色图片 (REST API 直接提供，优先级高于正文首图)
                        if (post.has("featured_image")) {
                            String featuredImage = post.get("featured_image").asText();
                            if (!featuredImage.isEmpty()) {
                                article.setCoverImage(featuredImage);
                            }
                        }

                        save(article);
                        totalSaved++;
                        if (totalSaved % 10 == 0) {
                            logFetch(source, "  ✓ [" + totalSaved + "] " + article.getTitle());
                        }
                        Thread.sleep(requestDelayMs);
                    } catch (Exception e) {
                        String postTitle = post.has("title") ? post.get("title").asText() : "unknown";
                        logFetch(source, "  ✗ 失败: " + postTitle + " - " + e.getMessage());
                    }
                }

                if (pageNum < totalPages) Thread.sleep(requestDelayMs);
            }

            setProgress(source, 100);
            logFetch(source, "✅ 抓取完成! 共保存 " + totalSaved + " 篇新文章");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** WordPress.com REST API JSON 请求 */
    private JsonNode fetchJson(String url) throws Exception {
        String body = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(pageTimeoutMs)
            .ignoreContentType(true)
            .execute()
            .body();
        return OBJECT_MAPPER.readTree(body);
    }

    // ==================== 通用工具方法 ====================

    /** 清理页面噪音元素 */
    private void cleanupNoise(Element contentEl) {
        contentEl.select(".author-box, .author-bio, .about-author, .author-info").remove();
        contentEl.select(".related-posts, .yarpp-related, .crp_related, .related-articles").remove();
        contentEl.select(".share-buttons, .social-share, .post-meta-bottom, .entry-footer").remove();
        contentEl.select(".comments-area, #comments, .comment-respond, .post-footer, .comment-link").remove();
        contentEl.select(".newsletter-signup, .subscribe-box, .cta-box").remove();
        contentEl.select("script, style, noscript, iframe").remove();
        contentEl.select(".ad-container, .advertisement, .google-ad").remove();
        contentEl.select("h2, h3, h4").stream()
            .filter(h -> {
                String t = h.text().toLowerCase();
                return t.contains("related post") || t.contains("related article");
            })
            .forEach(Element::remove);
    }

    private boolean existsBySourceUrl(String sourceUrl) {
        return count(new LambdaQueryWrapper<TechBlogArticle>()
            .eq(TechBlogArticle::getSourceUrl, sourceUrl)) > 0;
    }

    /** 规范化日期格式 */
    private String normalizeDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        raw = raw.trim();
        if (raw.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return raw.substring(0, 10);
        }
        try {
            DateTimeFormatter[] formatters = {
                // 英文长格式
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.ENGLISH),
                // 英文中格式
                DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
                // RSS/Atom 格式
                DateTimeFormatter.RFC_1123_DATE_TIME,  // "Sun, 02 Nov 2025 20:10:50 +0000"
                // 数字格式
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                // ISO 变体
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            };
            for (DateTimeFormatter fmt : formatters) {
                try {
                    return LocalDateTime.parse(raw, fmt).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("日期解析失败: {}", raw);
        }
        log.warn("无法解析日期格式: {}", raw.length() > 50 ? raw.substring(0, 50) + "..." : raw);
        return null; // 解析失败返回null，避免长文本写入DB字段
    }

    private void logFetch(String source, String msg) {
        List<String> logs = logsMap.get(source);
        if (logs != null) logs.add(msg);
    }

    private void setProgress(String source, int value) {
        AtomicInteger progress = progressMap.get(source);
        if (progress != null) progress.set(Math.max(0, Math.min(value, 100)));
    }
}
