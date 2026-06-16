package com.rx.admin.modules.monitor.dashboard.controller;

import com.rx.admin.common.result.Result;
import com.rx.admin.modules.literature.common.entity.LiteraryWork;
import com.rx.admin.modules.literature.common.entity.Author;
import com.rx.admin.modules.literature.common.entity.Dynasty;
import com.rx.admin.modules.literature.common.entity.Genre;
import com.rx.admin.modules.system.user.service.ISysUserService;
import com.rx.admin.modules.system.role.service.ISysRoleService;
import com.rx.admin.modules.system.menu.service.SysMenuService;
import com.rx.admin.modules.system.dept.service.ISysDeptService;
import com.rx.admin.modules.system.dict.service.ISysDictTypeService;
import com.rx.admin.modules.content.notice.service.ISysNoticeService;
import com.rx.admin.modules.monitor.log.service.SysLogService;
import com.rx.admin.modules.monitor.online.service.OnlineUserService;
import com.rx.admin.modules.literature.common.service.LiteraryWorkService;
import com.rx.admin.modules.literature.common.service.AuthorService;
import com.rx.admin.modules.literature.common.service.DynastyService;
import com.rx.admin.modules.literature.common.service.GenreService;
import com.rx.admin.modules.literature.common.service.ContentCategoryService;
import com.rx.admin.modules.literature.honglou.service.HonglouPoemService;
import com.rx.admin.modules.literature.honglou.service.HonglouCharacterService;
import com.rx.admin.modules.literature.honglou.service.HonglouCharacterRelationService;
import com.rx.admin.modules.literature.xiyou.service.XiyouPoemService;
import com.rx.admin.modules.literature.xiyou.service.XiyouCharacterService;
import com.rx.admin.modules.literature.xiyou.service.XiyouEventService;
import com.rx.admin.modules.literature.sanguo.service.SanguoPoemService;
import com.rx.admin.modules.literature.sanguo.service.SanguoCharacterService;
import com.rx.admin.modules.literature.shuihu.service.ShuihuPoemService;
import com.rx.admin.modules.literature.shuihu.service.ShuihuChapterService;
import com.rx.admin.modules.tool.music.service.MusicService;
import com.rx.admin.modules.as400.techblog.entity.TechBlogArticle;
import com.rx.admin.modules.as400.techblog.service.ITechBlogArticleService;
import com.rx.admin.modules.monitor.loginlog.mapper.SysLoginLogMapper;
import com.rx.admin.modules.monitor.exportlog.mapper.SysExportLogMapper;
import com.rx.admin.modules.monitor.exportlog.entity.SysExportLog;
import com.rx.admin.modules.monitor.log.mapper.SysLogMapper;
import com.rx.admin.modules.monitor.log.entity.SysLog;
import com.rx.admin.modules.monitor.loginlog.entity.SysLoginLog;
import com.rx.admin.modules.monitor.health.service.HealthService;
import com.rx.admin.modules.monitor.notification.service.SseSessionManager;
import com.rx.admin.modules.monitor.notification.service.DashboardCache;
import com.rx.admin.modules.monitor.notification.event.DashboardChangeEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final SysMenuService menuService;
    private final ISysDeptService deptService;
    private final ISysDictTypeService dictTypeService;
    private final ISysNoticeService noticeService;
    private final SysLogService logService;
    private final OnlineUserService onlineUserService;

    // SSE 统一推送 + 仪表盘缓存管理
    private final SseSessionManager sseSessionManager;
    private final DashboardCache dashboardCache;

    // 增强统计（原 EnhancedController 合并）
    private final SysLoginLogMapper loginLogMapper;
    private final SysExportLogMapper exportLogMapper;
    private final SysLogMapper sysLogMapper;
    private final HealthService healthService;

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
    private final ITechBlogArticleService techBlogArticleService;

    // 音乐播放器
    private final MusicService musicService;

    /**
     * 仪表盘统计缓存（volatile 保证多线程可见性）
     * 由 @Scheduled 定时任务自动刷新，避免每次 API 调用都实时查库计算
     */
    private volatile Map<String, Object> cachedStats;
    private volatile Map<String, Object> cachedEnhanced;
    private volatile Map<String, Object> cachedHealth;
    private volatile Map<String, Object> cachedGc;

    /**
     * 应用启动时立即初始化缓存（避免首次请求等待 30 秒）
     */
    @PostConstruct
    public void init() {
        refreshAllCache();
    }

    /**
     * 全量刷新所有仪表盘缓存（默认每 30 秒）
     * 使用 dirty 标记跳过未变更的文学/名著/TechBlog 数据
     * 仅当数据实际变化时才广播 SSE，避免无效推送
     */
    @Scheduled(fixedRateString = "${app.cache.dashboard-refresh-ms:30000}")
    public void refreshAllCache() {
        doRefreshStats();
        doRefreshEnhanced();
        doRefreshHealth();
        doRefreshGc();
    }

    /**
     * 监听 DashboardChangeEvent，实现数据变化时立即推送 SSE
     */
    @EventListener
    public void onDashboardChange(DashboardChangeEvent event) {
        switch (event.getSection()) {
            case DashboardChangeEvent.SECTION_SYSTEM:
                dashboardCache.markSystemDirty();
                doRefreshStats();
                break;
            case DashboardChangeEvent.SECTION_ENHANCED:
                dashboardCache.markEnhancedDirty();
                doRefreshEnhanced();
                break;
            case DashboardChangeEvent.SECTION_LITERATURE:
                dashboardCache.markLiteratureDirty();
                doRefreshStats();
                break;
            case DashboardChangeEvent.SECTION_CLASSICS:
                dashboardCache.markClassicsDirty();
                doRefreshStats();
                break;
            case DashboardChangeEvent.SECTION_TECHBLOG:
                dashboardCache.markTechblogDirty();
                doRefreshStats();
                doRefreshEnhanced();
                break;
            case DashboardChangeEvent.SECTION_ALL:
                dashboardCache.markAllDirty();
                computeAndPushAll();
                break;
        }
    }

    private void computeAndPushAll() {
        this.cachedStats = computeStats();
        this.cachedEnhanced = computeEnhanced();
        this.cachedHealth = healthService.getSystemHealth();
        this.cachedGc = healthService.getGcStats();
        sseSessionManager.broadcast("stats", Result.ok(cachedStats));
        sseSessionManager.broadcast("enhanced", Result.ok(cachedEnhanced));
        sseSessionManager.broadcast("health", Result.ok(cachedHealth));
        sseSessionManager.broadcast("gc", Result.ok(cachedGc));
    }

    private void doRefreshStats() {
        Map<String, Object> newStats = computeStats();
        if (!java.util.Objects.equals(this.cachedStats, newStats)) {
            this.cachedStats = newStats;
            sseSessionManager.broadcast("stats", Result.ok(cachedStats));
        }
    }

    private void doRefreshEnhanced() {
        Map<String, Object> newEnhanced = computeEnhanced();
        if (!java.util.Objects.equals(this.cachedEnhanced, newEnhanced)) {
            this.cachedEnhanced = newEnhanced;
            sseSessionManager.broadcast("enhanced", Result.ok(cachedEnhanced));
        }
    }

    private void doRefreshHealth() {
        Map<String, Object> newHealth = healthService.getSystemHealth();
        if (!java.util.Objects.equals(this.cachedHealth, newHealth)) {
            this.cachedHealth = newHealth;
            sseSessionManager.broadcast("health", Result.ok(cachedHealth));
        }
    }

    private void doRefreshGc() {
        Map<String, Object> newGc = healthService.getGcStats();
        if (!java.util.Objects.equals(this.cachedGc, newGc)) {
            this.cachedGc = newGc;
            sseSessionManager.broadcast("gc", Result.ok(cachedGc));
        }
    }

    /**
     * 实际计算仪表盘统计数据（使用 dirty 标记跳过未变更部分）
     */
    private Map<String, Object> computeStats() {
        Map<String, Object> map = new LinkedHashMap<>();

        // ===== 系统管理统计（始终刷新：在线人数变化频繁） =====
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

        // ===== 经典文学统计（仅 dirty 时重算） =====
        if (dashboardCache.isLiteratureDirty() || cachedStats == null) {
            Map<String, Object> literature = new LinkedHashMap<>();
            literature.put("workCount", literaryWorkService.count());
            literature.put("authorCount", authorService.count());
            literature.put("dynastyCount", dynastyService.count());
            literature.put("genreCount", genreService.count());
            literature.put("categoryCount", contentCategoryService.count());

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

            Map<Long, Long> worksByDynasty = literaryWorkService.countByDynasty();
            Map<Long, Long> authorCountByDynasty = literaryWorkService.list().stream()
                .filter(w -> w.getDynastyId() != null && w.getAuthorId() != null)
                .collect(Collectors.groupingBy(
                    LiteraryWork::getDynastyId,
                    Collectors.mapping(w -> w.getAuthorId(), Collectors.toSet())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size()));
            List<Map<String, Object>> dynastyStats = new ArrayList<>();
            List<Dynasty> dynasties = dynastyService.listAll();
            for (Dynasty d : dynasties) {
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

            Map<Long, Long> worksByGenre = literaryWorkService.countByGenre();
            List<Map<String, Object>> genreStats = new ArrayList<>();
            List<Genre> genres = genreService.listAll();
            for (Genre g : genres) {
                if (worksByGenre.containsKey(g.getId())) {
                    Map<String, Object> gs = new LinkedHashMap<>();
                    gs.put("genreName", g.getName());
                    gs.put("workCount", worksByGenre.get(g.getId()));
                    genreStats.add(gs);
                }
            }
            literature.put("genreStats", genreStats);

            Map<Long, Long> worksByAuthor = literaryWorkService.countByAuthor();
            List<Map<String, Object>> authorRankStats = new ArrayList<>();
            Set<Long> topAuthorIds = worksByAuthor.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            Map<Long, String> authorNameMap = topAuthorIds.isEmpty() ? Collections.emptyMap() :
                    authorService.listByIds(new ArrayList<>(topAuthorIds)).stream()
                            .collect(Collectors.toMap(Author::getId, Author::getName));
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
            dashboardCache.clearLiteratureDirty();
        } else {
            map.put("literature", cachedStats.get("literature"));
        }

        // ===== 四大名著统计（仅 dirty 时重算） =====
        if (dashboardCache.isClassicsDirty() || cachedStats == null) {
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
            dashboardCache.clearClassicsDirty();
        } else {
            map.put("classics", cachedStats.get("classics"));
        }

        // ===== 技术博客统计（仅 dirty 时重算） =====
        if (dashboardCache.isTechblogDirty() || cachedStats == null) {
            Map<String, Object> techblog = new LinkedHashMap<>();
            long totalArticles = techBlogArticleService.count();
            techblog.put("totalArticles", totalArticles);
            Map<String, Long> sourceCounts = techBlogArticleService.list().stream()
                    .collect(Collectors.groupingBy(TechBlogArticle::getSource, Collectors.counting()));
            techblog.put("sourceCounts", sourceCounts);
            Long totalViews = techBlogArticleService.list().stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0L).sum();
            techblog.put("totalViews", totalViews);
            map.put("techblog", techblog);
            dashboardCache.clearTechblogDirty();
        } else {
            map.put("techblog", cachedStats.get("techblog"));
        }

        // ===== 音乐播放器统计（轻量，始终刷新） =====
        Map<String, Object> music = musicService.getPlayStats();
        map.put("music", music);

        return map;
    }

    /**
     * 计算增强统计（原 EnhancedController 合并）
     */
    private Map<String, Object> computeEnhanced() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 登录统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        var loginQw = new LambdaQueryWrapper<SysLoginLog>();
        data.put("todayLogins", loginLogMapper.selectCount(
            loginQw.ge(SysLoginLog::getLoginTime, todayStart).eq(SysLoginLog::getStatus, 1)));
        data.put("todayFailLogins", loginLogMapper.selectCount(
            new LambdaQueryWrapper<SysLoginLog>()
                .ge(SysLoginLog::getLoginTime, todayStart)
                .eq(SysLoginLog::getStatus, 0)));
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        var trendQuery = new QueryWrapper<SysLoginLog>()
            .select("DATE(login_time) as date", "COUNT(*) as count")
            .ge("login_time", sevenDaysAgo)
            .eq("status", 1)
            .groupBy("DATE(login_time)")
            .orderByAsc("DATE(login_time)");
        List<Map<String, Object>> trendData = loginLogMapper.selectMaps(trendQuery);
        Map<String, Long> trendMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            trendMap.put(date, 0L);
        }
        for (Map<String, Object> row : trendData) {
            Object dateObj = row.get("date");
            Object countObj = row.get("count");
            if (dateObj != null && countObj != null) {
                trendMap.put(dateObj.toString(), ((Number) countObj).longValue());
            }
        }
        data.put("trend", trendMap);

        // 导出统计
        Map<String, Object> exportData = new LinkedHashMap<>();
        exportData.put("totalExports", exportLogMapper.selectCount(null));
        exportData.put("todayExcelExports", exportLogMapper.selectCount(
            new LambdaQueryWrapper<SysExportLog>()
                .ge(SysExportLog::getCreateTime, todayStart)
                .eq(SysExportLog::getExportType, "excel")));
        exportData.put("todayPdfExports", exportLogMapper.selectCount(
            new LambdaQueryWrapper<SysExportLog>()
                .ge(SysExportLog::getCreateTime, todayStart)
                .eq(SysExportLog::getExportType, "pdf")));
        data.put("exportStats", exportData);

        // 操作日志 Top10
        var topQuery = new QueryWrapper<SysLog>()
            .select("operation", "COUNT(*) as count")
            .ge("create_time", todayStart)
            .groupBy("operation")
            .orderByDesc("count")
            .last("LIMIT 10");
        List<Map<String, Object>> rows = sysLogMapper.selectMaps(topQuery);
        List<Map<String, Object>> opTop10 = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("operation", row.get("operation"));
            item.put("count", ((Number) row.get("count")).longValue());
            opTop10.add(item);
        }
        data.put("operationTop10", opTop10);

        return data;
    }

    @Operation(summary = "获取统计数据（读取 @Scheduled 缓存，30秒刷新间隔；在线人数实时计算）")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = cachedStats;
        if (data == null) {
            data = computeStats();
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> system = (Map<String, Object>) data.get("system");
            if (system != null) {
                system.put("onlineCount", onlineUserService.getOnlineCount());
            }
        }
        return Result.ok(data);
    }

}