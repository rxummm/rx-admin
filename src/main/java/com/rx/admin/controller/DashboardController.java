package com.rx.admin.controller;

import com.rx.admin.common.result.Result;
import com.rx.admin.entity.classics.LiteraryWork;
import com.rx.admin.service.*;
import com.rx.admin.service.classics.*;
import com.rx.admin.entity.TechBlogArticle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

@Slf4j
@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final SysDeptService deptService;
    private final SysDictTypeService dictTypeService;
    private final SysNoticeService noticeService;
    private final SysLogService logService;
    private final OnlineUserService onlineUserService;

    /** SSE 推送复用线程池，避免每次连接泄漏 */
    private final ScheduledExecutorService sseExecutor = Executors.newSingleThreadScheduledExecutor();

    @PreDestroy
    public void shutdown() {
        sseExecutor.shutdown();
    }

    // 经典文学模块
    private final LiteraryWorkService literaryWorkService;
    private final AuthorService authorService;
    private final DynastyService dynastyService;
    private final GenreService genreService;
    private final ContentCategoryService contentCategoryService;

    // 四大名著 - 红楼梦
    private final HonglouPoemService honglouPoemService;
    private final HonglouCharacterService honglouCharacterService;
    private final HonglouCharacterRelationService honglouCharacterRelationService;

    // 四大名著 - 西游记
    private final XiyouPoemService xiyouPoemService;
    private final XiyouCharacterService xiyouCharacterService;
    private final XiyouEventService xiyouEventService;

    // 四大名著 - 三国演义
    private final SanguoPoemService sanguoPoemService;
    private final SanguoCharacterService sanguoCharacterService;

    // 四大名著 - 水浒传
    private final ShuihuPoemService shuihuPoemService;
    private final ShuihuChapterService shuihuChapterService;

    // 技术博客
    private final TechBlogArticleService techBlogArticleService;

    // 音乐播放器
    private final MusicService musicService;

    /**
     * 仪表盘统计缓存（volatile 保证多线程可见性）
     * 由 @Scheduled 定时任务自动刷新，避免每次 API 调用都实时查库计算
     */
    private volatile Map<String, Object> cachedStats;

    /**
     * 应用启动时立即初始化缓存（避免首次请求等待 30 秒）
     */
    @PostConstruct
    public void init() {
        refreshDashboardCache();
    }

    /**
     * 定时刷新仪表盘缓存（默认每 30 秒）
     * 由 application.yml 中 app.cache.dashboard-refresh-ms 控制
     */
    @Scheduled(fixedRateString = "${app.cache.dashboard-refresh-ms:30000}")
    public void refreshDashboardCache() {
        this.cachedStats = computeStats();
    }

    /**
     * 实际计算仪表盘统计数据（从各 Service 实时查询）
     */
    private Map<String, Object> computeStats() {
        Map<String, Object> map = new LinkedHashMap<>();

        // ===== 系统管理统计 =====
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("userCount", userService.count());
        system.put("roleCount", roleService.count());
        system.put("menuCount", menuService.count());
        system.put("deptCount", deptService.count());
        system.put("dictTypeCount", dictTypeService.count());
        system.put("noticeCount", noticeService.count());
        system.put("logCount", logService.count());
        system.put("onlineCount", onlineUserService.getOnlineCount());
        map.put("system", system);

        // ===== 经典文学统计 =====
        Map<String, Object> literature = new LinkedHashMap<>();
        literature.put("workCount", literaryWorkService.count());
        literature.put("authorCount", authorService.count());
        literature.put("dynastyCount", dynastyService.count());
        literature.put("genreCount", genreService.count());
        literature.put("categoryCount", contentCategoryService.count());

        // ... 文学作品分类统计 ...
        Map<Integer, Long> worksByDifficulty = literaryWorkService.countByDifficultyLevel();
        List<Map<String, Object>> difficultyStats = new ArrayList<>();
        String[] diffLabels = {"", "入门", "初级", "中级", "高级", "专家"};
        for (Map.Entry<Integer, Long> entry : worksByDifficulty.entrySet()) {
            Map<String, Object> ds = new LinkedHashMap<>();
            int level = entry.getKey();
            ds.put("level", level);
            ds.put("label", level < diffLabels.length ? diffLabels[level] : "Lv" + level);
            ds.put("workCount", entry.getValue());
            difficultyStats.add(ds);
        }
        literature.put("difficultyStats", difficultyStats);

        // 各朝代作品与作者分布（一次查询避免 N+1）
        Map<Long, Long> worksByDynasty = literaryWorkService.countByDynasty();
        // 预计算每个朝代的去重作者数，避免 per-dynasty 循环中反复 list() + stream filter
        Map<Long, Long> authorCountByDynasty = literaryWorkService.list().stream()
            .filter(w -> w.getDynastyId() != null && w.getAuthorId() != null)
            .collect(Collectors.groupingBy(
                com.rx.admin.entity.classics.LiteraryWork::getDynastyId,
                Collectors.mapping(w -> w.getAuthorId(), Collectors.toSet())
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size()));
        List<Map<String, Object>> dynastyStats = new ArrayList<>();
        List<com.rx.admin.entity.classics.Dynasty> dynasties = dynastyService.listAll();
        for (com.rx.admin.entity.classics.Dynasty d : dynasties) {
            if (worksByDynasty.containsKey(d.getId())) {
                Map<String, Object> ds = new LinkedHashMap<>();
                ds.put("dynastyName", d.getName());
                ds.put("workCount", worksByDynasty.get(d.getId()));
                ds.put("authorCount", authorCountByDynasty.getOrDefault(d.getId(), 0L));
                dynastyStats.add(ds);
            }
        }
        dynastyStats.sort((a, b) -> Long.compare(
            ((Number)b.get("workCount")).longValue() + ((Number)b.get("authorCount")).longValue(),
            ((Number)a.get("workCount")).longValue() + ((Number)a.get("authorCount")).longValue()
        ));
        literature.put("dynastyStats", dynastyStats);

        // 体裁分布
        Map<Long, Long> worksByGenre = literaryWorkService.countByGenre();
        List<Map<String, Object>> genreStats = new ArrayList<>();
        List<com.rx.admin.entity.classics.Genre> genres = genreService.listAll();
        for (com.rx.admin.entity.classics.Genre g : genres) {
            if (worksByGenre.containsKey(g.getId())) {
                Map<String, Object> gs = new LinkedHashMap<>();
                gs.put("genreName", g.getName());
                gs.put("workCount", worksByGenre.get(g.getId()));
                genreStats.add(gs);
            }
        }
        literature.put("genreStats", genreStats);

        // 作者作品数Top排行（批量查询避免 N+1）
        Map<Long, Long> worksByAuthor = literaryWorkService.countByAuthor();
        List<Map<String, Object>> authorRankStats = new ArrayList<>();
        Set<Long> topAuthorIds = worksByAuthor.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Map<Long, String> authorNameMap = topAuthorIds.isEmpty() ? Collections.emptyMap() :
                authorService.listByIds(new ArrayList<>(topAuthorIds)).stream()
                        .collect(Collectors.toMap(
                                com.rx.admin.entity.classics.Author::getId,
                                com.rx.admin.entity.classics.Author::getName));
        worksByAuthor.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Map<String, Object> ar = new LinkedHashMap<>();
                    ar.put("authorId", entry.getKey());
                    ar.put("authorName", authorNameMap.getOrDefault(entry.getKey(), "未知"));
                    ar.put("workCount", entry.getValue());
                    authorRankStats.add(ar);
                });
        literature.put("authorRankStats", authorRankStats);

        // 浏览量Top排行
        List<LiteraryWork> topViews = literaryWorkService.topByViewCount(10);
        List<Map<String, Object>> viewRankStats = new ArrayList<>();
        for (LiteraryWork w : topViews) {
            Map<String, Object> vr = new LinkedHashMap<>();
            vr.put("id", w.getId());
            vr.put("title", w.getTitle());
            vr.put("viewCount", w.getViewCount() != null ? w.getViewCount() : 0L);
            viewRankStats.add(vr);
        }
        literature.put("viewRankStats", viewRankStats);

        literature.put("totalWordCount", literaryWorkService.sumWordCount());
        literature.put("totalViewCount", literaryWorkService.sumViewCount());
        map.put("literature", literature);

        // ===== 四大名著统计 =====
        Map<String, Object> classics = new LinkedHashMap<>();

        Map<String, Long> honglou = new LinkedHashMap<>();
        honglou.put("poemCount", honglouPoemService.count());
        honglou.put("characterCount", honglouCharacterService.count());
        honglou.put("relationCount", honglouCharacterRelationService.count());
        classics.put("honglou", honglou);

        Map<String, Long> xiyou = new LinkedHashMap<>();
        xiyou.put("poemCount", xiyouPoemService.count());
        xiyou.put("characterCount", xiyouCharacterService.count());
        xiyou.put("eventCount", xiyouEventService.count());
        classics.put("xiyou", xiyou);

        Map<String, Long> sanguo = new LinkedHashMap<>();
        sanguo.put("poemCount", sanguoPoemService.count());
        sanguo.put("characterCount", sanguoCharacterService.count());
        classics.put("sanguo", sanguo);

        Map<String, Long> shuihu = new LinkedHashMap<>();
        shuihu.put("poemCount", shuihuPoemService.count());
        shuihu.put("chapterCount", shuihuChapterService.count());
        classics.put("shuihu", shuihu);

        long totalPoems = honglou.get("poemCount") + xiyou.get("poemCount")
                + sanguo.get("poemCount") + shuihu.get("poemCount");
        long totalCharacters = honglou.get("characterCount") + xiyou.get("characterCount")
                + sanguo.get("characterCount");
        classics.put("totalPoems", totalPoems);
        classics.put("totalCharacters", totalCharacters);
        map.put("classics", classics);

        // ===== 技术博客统计 =====
        Map<String, Object> techblog = new LinkedHashMap<>();
        long totalArticles = techBlogArticleService.count();
        techblog.put("totalArticles", totalArticles);

        // 按来源分组统计（一次查询避免每源一次 COUNT）
        Map<String, Long> sourceCounts = techBlogArticleService.list().stream()
                .collect(Collectors.groupingBy(TechBlogArticle::getSource, Collectors.counting()));
        techblog.put("sourceCounts", sourceCounts);

        Long totalViews = techBlogArticleService.list().stream()
            .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0L).sum();
        techblog.put("totalViews", totalViews);
        map.put("techblog", techblog);

        // ===== 音乐播放器统计 =====
        Map<String, Object> music = musicService.getPlayStats();
        map.put("music", music);

        return map;
    }

    @Operation(summary = "获取统计数据（读取 @Scheduled 缓存，30秒刷新间隔；在线人数实时计算）")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        // 优先返回缓存，若缓存尚未就绪则实时计算
        Map<String, Object> data = cachedStats;
        if (data == null) {
            data = computeStats();
        } else {
            // 在线人数不能依赖缓存（登录后立即查询时缓存可能过期），必须实时获取
            @SuppressWarnings("unchecked")
            Map<String, Object> system = (Map<String, Object>) data.get("system");
            if (system != null) {
                system.put("onlineCount", onlineUserService.getOnlineCount());
            }
        }
        return Result.ok(data);
    }

    @Operation(summary = "SSE 实时推送统计数据（读取 @Scheduled 缓存）")
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        ScheduledFuture<?> task = sseExecutor.scheduleAtFixedRate(() -> {
            try {
                Map<String, Object> data = cachedStats;
                if (data == null) {
                    data = computeStats();
                }
                emitter.send(SseEmitter.event()
                    .name("stats")
                    .data(Result.ok(data), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                // 异步线程上的客户端断开异常会被 GlobalExceptionHandler 兜底并降级为 DEBUG
                log.debug("SSE 推送结束: {}", e.getMessage());
                emitter.completeWithError(e);
            }
        }, 0, 30, TimeUnit.SECONDS);
        // 客户端断开或超时时取消定时任务，防止泄漏
        emitter.onCompletion(() -> task.cancel(false));
        emitter.onTimeout(() -> task.cancel(false));
        return emitter;
    }
}