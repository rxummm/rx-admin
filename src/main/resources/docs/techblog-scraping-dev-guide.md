# 技术博客抓取模块开发文档

> **版本**: 2.1.0 | **更新日期**: 2026-06-04 | **适用项目**: RX Admin 技术博客模块

---

## 目录

1. [模块概述](#1-模块概述)
2. [系统架构总览](#2-系统架构总览)
3. [数据表设计](#3-数据表设计)
4. [已实现：NickLitten 博客抓取](#4-已实现nicklitten-博客抓取)
5. [规划中：Apimy WordPress 博客接入](#5-规划中apimy-wordpress-博客接入)
6. [规划中：blog.faq400.com 博客接入](#6-规划中blogfaq400com-博客接入)
7. [规划中：rpgpgm.com 博客接入](#7-规划中rpgpgmcom-博客接入)
8. [规划中：as400andsqltricks.com 博客接入](#8-规划中as400andsqltrickscom-博客接入)
9. [五个来源对比总览](#9-五个来源对比总览)
10. [前后端变动对比](#10-前后端变动对比)
11. [多源并发抓取方案](#11-多源并发抓取方案)
12. [开发排期建议](#12-开发排期建议)

---

## 1. 模块概述

### 1.1 背景

技术博客模块用于从外部博客网站抓取技术文章，存储到本地数据库，并通过管理后台提供浏览、搜索、分类筛选功能。

### 1.2 当前状态

| 来源 | source 标识 | 状态 | 爬虫方式 | 文章数 |
|------|------------|------|---------|--------|
| `https://www.nicklitten.com/blog/` | `nicklitten` | ✅ 已实现 | Jsoup 纯 HTTP | ~760 篇 |
| `https://apimymymy.wordpress.com/blog/` | `apimy` | 📋 规划中 | Playwright 浏览器 | ~320 篇 |
| `https://blog.faq400.com/en/` | `faq400` | 📋 规划中 | Jsoup 纯 HTTP | ~168 篇 |
| `https://www.rpgpgm.com/p/list-of-all-posts.html` | `rpgpgm` | 📋 规划中 | Jsoup 纯 HTTP | ~1100 篇 |
| `https://www.as400andsqltricks.com/` | `as400sql` | 📋 规划中 | Jsoup 纯 HTTP | 待探测 |

### 1.3 涉及文件清单

| 层级 | 文件路径 | 说明 |
|------|----------|------|
| SQL/DDL | `src/main/resources/db/techblog_init.sql` | 建表 + 菜单初始化 |
| Entity | `src/main/java/com/rx/admin/entity/TechBlogArticle.java` | 实体类 |
| Mapper | `src/main/java/com/rx/admin/mapper/TechBlogArticleMapper.java` | MyBatis-Plus Mapper |
| Service | `src/main/java/com/rx/admin/service/TechBlogArticleService.java` | 业务逻辑 + 爬虫 |
| Controller | `src/main/java/com/rx/admin/controller/TechBlogController.java` | REST API |
| 前端 API | `ui/src/api/techBlog.js` | 前端 API 封装 |
| 前端列表页 | `ui/src/views/as400/techblog/index.vue` | 文章列表页 |
| 前端详情页 | `ui/src/views/as400/techblog/detail.vue` | 文章详情页 |
| 路由映射 | `ui/src/router/componentMap.js` | 组件映射表 |
| 依赖 | `pom.xml` | 引入 Jsoup 1.17.2 |

---

## 2. 系统架构总览

### 2.1 分层架构

```
┌─────────────────────────────────────────────────────┐
│  前端 (Vue 3 + Element Plus)                         │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ index.vue    │  │ detail.vue   │  │ techBlog.js│ │
│  │ 文章列表     │  │ 文章详情     │  │ API 封装   │ │
│  └─────────────┘  └──────────────┘  └────────────┘ │
├─────────────────────────────────────────────────────┤
│  后端 (Spring Boot 3)                                │
│  ┌──────────────────────┐  ┌─────────────────────┐  │
│  │ TechBlogController   │  │ TechBlogArticleService│ │
│  │ /api/techblog/*      │  │ 查询 + 爬虫逻辑     │  │
│  └──────────────────────┘  └─────────────────────┘  │
│  ┌──────────────────────┐                           │
│  │ TechBlogArticleMapper│ (MyBatis-Plus BaseMapper) │
│  └──────────────────────┘                           │
├─────────────────────────────────────────────────────┤
│  数据库 (MySQL: rx_admin)                            │
│  ┌──────────────────────────┐                      │
│  │ tech_blog_article 表     │                      │
│  └──────────────────────────┘                      │
└─────────────────────────────────────────────────────┘
```

### 2.2 请求数据流

```
用户操作                                后台处理
─────────────────────────────────────────────────────────

【浏览文章列表】
浏览器 → GET /api/techblog/articles?page=1&size=10
       → Controller.list()
       → Service.pageQuery() 排除 content_html/content_text 大字段
       ← 返回 PageResult (total, records)

【查看文章详情】
浏览器 → GET /api/techblog/articles/{id}
       → Controller.detail()
       → Service.getDetail() 同时 viewCount+1
       ← 返回完整文章（含 contentHtml）

【同步文章】
浏览器 → POST /api/techblog/fetch
       → Controller.fetch()
       → Service.startFetch()
         → 异步线程 doFetch()
           → 探测总页数
           → for 每页：
               → Jsoup 抓取列表页 HTML
               → 解析文章条目（标题/链接/日期/分类/摘要/封面图）
               → for 每篇：
                   → 按 sourceUrl 去重检查
                   → Jsoup 抓取详情页 HTML
                   → 提取正文 + 清理噪音
                   → save() 写入数据库
                   → delay 1s
           → 更新进度 AtomicInteger
       ← 返回 "抓取任务已启动"

【轮询进度】
浏览器 → GET /api/techblog/progress (每2秒轮询)
       ← { progress: 0-100, logs: [...] }
```

---

## 3. 数据表设计

### 3.1 表结构

```sql
CREATE TABLE IF NOT EXISTS tech_blog_article (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文章ID',
    title           VARCHAR(500) NOT NULL COMMENT '文章标题',
    slug            VARCHAR(500) COMMENT 'URL slug',
    source_url      VARCHAR(1000) COMMENT '原始链接',
    author          VARCHAR(100) DEFAULT 'Nick Litten' COMMENT '作者',
    publish_date    VARCHAR(20) COMMENT '发布日期(yyyy-MM-dd)',
    categories      VARCHAR(500) COMMENT '分类标签(英文逗号分隔)',
    excerpt_text    TEXT COMMENT '摘要',
    content_html    MEDIUMTEXT COMMENT 'HTML正文内容',
    content_text    TEXT COMMENT '纯文本正文(用于搜索)',
    cover_image     VARCHAR(1000) COMMENT '封面图URL',
    sort            INT DEFAULT 0 COMMENT '排序',
    view_count      INT DEFAULT 0 COMMENT '浏览次数',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_publish_date (publish_date),
    INDEX idx_source_url (source_url(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术博客文章表(NickLitten)';
```

### 3.2 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK | 自增主键 |
| `title` | VARCHAR(500) | 文章标题，从列表页/详情页 `h1`/`h2` 提取 |
| `slug` | VARCHAR(500) | URL 路径片段，从 `sourceUrl` 截取域名之后的部分 |
| `source_url` | VARCHAR(1000) | 文章原文链接，**用于去重的关键字段** |
| `author` | VARCHAR(100) | 作者，NickLitten 默认 `'Nick Litten'` |
| `publish_date` | VARCHAR(20) | 发布日期，统一为 `yyyy-MM-dd` 格式 |
| `categories` | VARCHAR(500) | 分类标签，英文逗号分隔，如 `"AS400, SQL, IBM i"` |
| `excerpt_text` | TEXT | 文章摘要，从列表页提取 |
| `content_html` | MEDIUMTEXT | 完整 HTML 正文（最大 16MB），清理广告/评论/相关文章后的纯正文 |
| `content_text` | TEXT | 纯文本正文，截取前 5000 字符，**用于全文搜索** |
| `cover_image` | VARCHAR(1000) | 封面图 URL |
| `sort` | INT | 手动排序字段 |
| `view_count` | INT | 浏览次数，每次查看详情自动 +1 |
| `deleted` | TINYINT | MyBatis-Plus 逻辑删除标记 |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间（自动更新） |

### 3.3 设计要点

1. **publish_date 用 VARCHAR 而非 DATE**：因为不同源站的日期格式多样（ISO 8601、英文月份、`dd/MM/yyyy` 等），通过 Service 层的 `normalizeDate()` 统一为 `yyyy-MM-dd` 后再存储
2. **content_html 用 MEDIUMTEXT**：完整 HTML 正文可能很大（含内联样式、图片 base64），MEDIUMTEXT 支持 16MB
3. **列表查询排除大字段**：`pageQuery()` 中通过 `.select()` 排除 `content_html` 和 `content_text`，避免列表查询时加载大量数据
4. **去重策略**：按 `source_url` 去重，同一篇文章不会被重复抓取

### 3.4 菜单权限

```sql
-- 菜单结构：AS400管理(id=107) > 技术博客(id=380)
--                          ├── 文章列表(381) → component: as400/techblog/index
--                          │   ├── 博客查询(382) → 权限: techblog:query
--                          │   └── 文章详情(383) → component: as400/techblog/detail
```

| 菜单 ID | 层级 | 名称 | 权限标识 |
|---------|------|------|----------|
| 380 | 1级 | 技术博客 | — |
| 381 | 2级 | 文章列表 | — |
| 382 | 3级 | 博客查询 | `techblog:query` |
| 383 | 3级 | 文章详情 | `techblog:detail` |

---

## 4. 已实现：NickLitten 博客抓取

### 4.1 目标网站分析

| 属性 | 值 |
|------|-----|
| URL | `https://www.nicklitten.com/blog/` |
| 分页格式 | `/blog/page/N/`（N 从 1 开始） |
| 总页数 | ~76 页（动态探测） |
| 每页文章数 | ~10 篇 |
| 反爬机制 | 无（可直接用 Jsoup HTTP 请求） |
| 列表页 HTML 特征 | 文章在 `<article>` 标签内，分页控件为 `.tcb-pagination` |
| 详情页 HTML 特征 | 正文在 `.entry-content` 或 `.tcb-post-content` 中 |

### 4.2 抓取流程（doFetch 方法）

```
┌────────────────────────────────────────────────────────────┐
│  步骤 1: 探测总页数                                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Jsoup.connect("https://www.nicklitten.com/blog/")     │  │
│  │   .userAgent("Mozilla/5.0")                           │  │
│  │   .timeout(15000)                                     │  │
│  │   .get()                                               │  │
│  │ → 解析 .tcb-pagination 获取最大页码                     │  │
│  │ → 如果未解析到分页控件，默认使用 MAX_PAGES=76           │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ↓                                  │
│  步骤 2: 逐页抓取列表 (for pageNum = 1 → totalPages)       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 步骤 2a: 抓取列表页 HTML                               │  │
│  │   Jsoup.connect(BLOG_URL + "page/" + pageNum + "/")   │  │
│  │   → 解析 <article> 标签，提取每篇文章：                  │  │
│  │     • 标题：h2 a, h3 a, .entry-title a 等选择器       │  │
│  │     • 链接：从标题元素的 href 提取                      │  │
│  │     • slug：从 url 截取 domain 之后的路径               │  │
│  │     • 日期：time, .entry-date, .published 元素的      │  │
│  │             datetime 属性或文本内容                     │  │
│  │     • 分类：.cat-links a, .category a 等选择器        │  │
│  │     • 摘要：.entry-summary, .post-excerpt,             │  │
│  │            .entry-content p 的首段                      │  │
│  │     • 封面图：第一个 img 标签的 src/data-src           │  │
│  │     • author/sort/viewCount 设初始值                    │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ↓                                  │
│  步骤 3: 逐篇抓取详情 (for each article in list)            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 步骤 3a: 去重检查                                      │  │
│  │   existsBySourceUrl(article.getSourceUrl())            │  │
│  │   → 已存在则 skip，记录日志                            │  │
│  │   → 不存在则继续                                       │  │
│  │                                                        │  │
│  │ 步骤 3b: 抓取详情页 HTML                               │  │
│  │   Jsoup.connect(article.getSourceUrl())                │  │
│  │   → 提取标题覆盖列表页标题（详情页更准确）              │  │
│  │      选择器: h1.entry-title, h1.post-title, h1        │  │
│  │   → 提取正文内容                                       │  │
│  │      选择器: .entry-content, .post-content,            │  │
│  │              .tcb-post-content, article .content        │  │
│  │   → 正文噪音清理（移除以下元素）：                      │  │
│  │      .author-box, .author-bio (作者信息框)             │  │
│  │      .related-posts, .yarpp-related (相关文章)         │  │
│  │      .share-buttons, .social-share (分享按钮)          │  │
│  │      .comments-area, #comments (评论区)                │  │
│  │      .newsletter-signup, .cta-box (订阅/广告)          │  │
│  │      script, style, noscript, iframe (非正文标签)      │  │
│  │      .ad-container, .advertisement (广告)              │  │
│  │      h2/h3/h4 中包含 "related post" 的区块             │  │
│  │   → 设置 contentHtml = contentEl.html()               │  │
│  │   → 设置 contentText = 纯文本（截取 5000 字符）        │  │
│  │                                                        │  │
│  │ 步骤 3c: 补全列表页缺失的字段                           │  │
│  │   → 如果 publishDate 为空：从详情页 time 标签补        │  │
│  │   → 如果 categories 为空：从详情页分类链接补           │  │
│  │   → 如果 coverImage 为空：从详情页第一张图片补         │  │
│  │                                                        │  │
│  │ 步骤 3d: 保存到数据库                                  │  │
│  │   save(article)                                        │  │
│  │   totalSaved++                                         │  │
│  │   Thread.sleep(1000)  // 请求间隔 1 秒                │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ↓                                  │
│  步骤 4: 更新进度                                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ progress = (pageNum * 100) / totalPages               │  │
│  │ fetchProgress.set(Math.min(progress, 99))             │  │
│  │ pageNum == totalPages 时 → fetchProgress.set(100)     │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

### 4.3 核心方法清单

| 方法 | 访问 | 功能 |
|------|------|------|
| `startFetch()` | public | 触发异步抓取，检查是否正在运行 |
| `doFetch()` | private | 主抓取逻辑，独立线程运行 |
| `detectTotalPages(Document)` | private | 从首页探测总页数 |
| `scrapeListPage(String url)` | private | 抓取单页列表，返回 `List<TechBlogArticle>` |
| `scrapeDetail(TechBlogArticle)` | private | 抓取单篇文章详情，填充 contentHtml/contentText |
| `existsBySourceUrl(String url)` | private | 按 sourceUrl 去重检查 |
| `normalizeDate(String raw)` | private | 标准化日期格式为 `yyyy-MM-dd` |
| `getFetchProgress()` | public | 获取当前进度 `AtomicInteger` |
| `getFetchLogs()` | public | 获取日志列表 |

### 4.4 配置常量

```java
private static final String BASE_URL = "https://www.nicklitten.com";
private static final String BLOG_URL = BASE_URL + "/blog/";
private static final int MAX_PAGES = 76;            // 默认最大页数
private static final int REQUEST_DELAY_MS = 1000;    // 请求间隔 1 秒
```

### 4.5 进度与去重机制

**进度追踪：**
- `AtomicInteger fetchProgress`：`-1`=未开始，`0~99`=进行中，`100`=完成
- `Collections.synchronizedList` 存储操作日志
- 前端每 2 秒通过 `GET /api/techblog/progress` 轮询

**去重机制：**
- 按 `source_url` 字段查询 `count() > 0` 判断是否存在
- 已存在的文章直接跳过，日志记录 `"跳过已存在: xxx"`
- **注意**：已存在的文章不会被更新，即使源站内容有变化

**异常处理：**
- 单篇文章抓取失败不影响后续文章
- 整个流程失败后 `fetchProgress` 重置为 `-1`
- 错误日志记录到 `fetchLogs` 供前端展示

### 4.6 API 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/techblog/articles` | 分页查询，参数：page, size, keyword, category |
| GET | `/api/techblog/articles/{id}` | 文章详情（包含完整 HTML） |
| GET | `/api/techblog/categories` | 所有分类标签（从所有文章去重提取） |
| GET | `/api/techblog/recent` | 最近 N 篇文章，参数：limit（默认 5） |
| POST | `/api/techblog/fetch` | 触发抓取 |
| GET | `/api/techblog/progress` | 抓取进度 + 日志 |

### 4.7 前端页面说明

#### 列表页 (`index.vue`)
| 功能模块 | 说明 |
|---------|------|
| 页面标题 | "技术博客"，副标题 "Nick Litten's IBM i Blog" |
| 同步按钮 | 触发抓取 + 进度轮询（2秒间隔），显示进度条和完成提示 |
| 搜索框 | 支持标题/内容/摘要关键词搜索，回车触发 |
| 分类筛选 | 下拉选择器，从后端获取分类列表 |
| 文章卡片 | 封面图(220px) + 标题(2行截断) + 日期/作者/阅读量 + 摘要(2行截断) + 分类标签(可点击筛选) + "阅读全文"按钮 |
| 分页 | el-pagination，支持 total/prev/pager/next/jumper |
| 空状态 | 引导用户点击"同步文章" |
| 图标 | Search, Calendar, User, View, ArrowRight, Download, QuestionFilled |

#### 详情页 (`detail.vue`)
| 功能模块 | 说明 |
|---------|------|
| 顶部导航 | sticky 定位，"返回列表"按钮 |
| 标题区 | 26px 粗体标题 + 日期/作者/阅读量/原文链接 |
| 分类标签 | 多个 el-tag |
| 封面图 | 最大高度 400px，自适应宽度 |
| 正文渲染 | `v-html` 渲染，完整 CSS 美化：h2/h3/h4 样式、blockquote 蓝色左边框、code 深色主题(#1e1e1e)、pre 代码块、图片自适应、链接主题色、表格样式 |
| 底部操作 | "返回列表" + "查看原文"（新窗口打开） |

---

## 5. 规划中：Apimy WordPress 博客接入

### 5.1 目标网站分析

| 属性 | 值 |
|------|-----|
| URL | `https://apimymymy.wordpress.com/blog/` |
| 平台 | WordPress.com 托管 |
| 分页方式 | 无限滚动（滚动到底部自动加载下一页） |
| URL 分页 | 同样支持 `/blog/page/N/` 直接访问 |
| 总页数 | ~32 页（page 33 返回 404） |
| 每页文章数 | ~10 篇 |
| **反爬机制** | ⚠️ **WordPress.com JavaScript 反爬保护** |
| 反爬表现 | 直接 HTTP 请求返回 "Just a moment..." Cloudflare/JS 验证页 |
| 必需工具 | **Playwright**（真实浏览器）或等效浏览器自动化工具 |

### 5.2 与 NickLitten 的核心差异

```
               NickLitten                         Apimy WordPress
               ─────────                         ───────────────
抓取方式：     Jsoup.connect(URL).get()          playwright page.goto(URL)
               ↓                                  ↓
返回内容：     完整的 HTML 文章列表               JS 验证拦截页 → 等待 JS 执行 →
                                                 完整的 HTML 文章列表
分页方式：     静态 .tcb-pagination 控件          无限滚动（JS 动态加载）
HTML 选择器：  .tcb-post-card, .entry-title       WordPress 主题选择器（完全不同）
总页数探测：   解析首页分页控件                    二分查找页数 + 404 判断
延迟策略：     Thread.sleep(1000)                 浏览器加载等待(页面渲染完成)
```

### 5.3 抓取实现方案设计

#### 方案一：Playwright Java 直接集成（推荐）

在 Java 后端通过 Playwright Java API 控制浏览器：

```java
// 伪代码示例
try (Playwright playwright = Playwright.create()) {
    Browser browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions().setHeadless(true)
    );
    Page page = browser.newPage();

    // 抓取列表页
    page.navigate("https://apimymymy.wordpress.com/blog/page/1/");
    page.waitForLoadState();  // 等待 JS 执行完毕

    // 提取文章列表
    List<ElementHandle> articles = page.querySelectorAll("article");
    for (ElementHandle article : articles) {
        String title = article.querySelector("h2 a").textContent();
        String url = article.querySelector("h2 a").getAttribute("href");
        // ... 提取其他字段
    }
}
```

**优点：**
- 完全在 Java 生态内，不需要额外进程
- 可以精确等待页面渲染完成
- 支持截图调试

**缺点：**
- 需要引入 Playwright Java 依赖（`com.microsoft.playwright`）
- 首次运行需要下载浏览器（`playwright install chromium`）
- 内存开销较大

#### 方案二：Node.js 桥接

用 Node.js Playwright 脚本 + Java 调用子进程，通过 JSON/stdout 传递数据。

**优点：**
- Node.js 生态的 Playwright 更成熟
- 可以利用已有项目的 `playwright-cli` skill 经验

**缺点：**
- 跨进程通信复杂
- 错误处理和超时控制困难
- 不推荐

#### 推荐采用方案一（Playwright Java）

已有 `pom.xml` 中可加入：
```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.48.0</version>
</dependency>
```

### 5.4 分页探测策略

由于 WordPress.com 使用无限滚动而非传统分页控件，探测总页数的方法为：

```
步骤 1: 直接访问 /blog/page/1/
步骤 2: 用 Playwright 解析页面中的文章数量（通常 10 篇/页）
步骤 3: 二分查找边界：
        ┌─────────────────────────────────────┐
        │ page=1  ✅ 有文章                     │
        │ page=2  ✅ 有文章                     │
        │ ...    ...                          │
        │ page=100 ❌ 404 / 无内容             │
        │ → 二分到 page=50 ❌                  │
        │ → 二分到 page=25 ✅ 有文章            │
        │ → 二分到 page=37 ❌                  │
        │ → ...精确到 page=32 ✅, page=33 ❌   │
        │ → 总页数 = 32                        │
        └─────────────────────────────────────┘
```

### 5.5 HTML 选择器对照

由于 WordPress.com 使用自己的主题，HTML 结构与 NickLitten 完全不同：

| 提取目标 | NickLitten 选择器 | Apimy WordPress 选择器（待实测确认） |
|---------|-------------------|-------------------------------------|
| 列表页文章 | `article` | `article`（WordPress 通用标签） |
| 标题+链接 | `h2 a, h3 a, .entry-title a` | `h2.entry-title a` 或 `a[rel=bookmark]` |
| 日期 | `time, .entry-date, .post-date, [datetime]` | `time.published, .entry-date a` |
| 分类 | `.cat-links a, .category a, [rel=category]` | `.cat-links a, .entry-categories a` |
| 摘要 | `.entry-summary, .post-excerpt, .entry-content p` | `.entry-summary p, .entry-content p:first` |
| 封面图 | `img` 的 `src/data-src/data-lazy-src` | `img.wp-post-image, .entry-content img:first` |
| 详情正文 | `.entry-content, .post-content, .tcb-post-content` | `.entry-content, .post-content` |

> **注意**：以上 WordPress 选择器为经验推测，实际开发时需要用 Playwright 访问具体页面确认。曾经通过 `playwright snapshot` 已获取过页面结构（保存为 YAML），可从本地文件 `.playwright-cli/page-*.yml` 中精确认证。

---

## 6. 规划中：blog.faq400.com 博客接入

### 6.1 目标网站分析

| 属性 | 值 |
|------|-----|
| URL | `https://blog.faq400.com/en/` |
| 平台 | 自建 WordPress（非 WordPress.com） |
| 语言 | 英语版（另有意大利语版 `/it/`） |
| 分页格式 | `/en/page/N/`（N 从 1 开始） |
| 总页数 | **21 页**（Page 21 of 21） |
| 每页文章数 | **8 篇**（21 × 8 ≈ 168 篇总文章） |
| **反爬机制** | ❌ **无**（HTTP 200，Jsoup 可直接抓取） |
| 列表页 HTML 特征 | 文章在 `<article>` 标签内 |
| 详情页 HTML 特征 | 标题 `h1.entry-title`，正文 `.entry-content`，在 `<article>` 标签内 |
| 作者 | 多作者（团队博客） |

### 6.2 抓取策略

```
抓取方式：✅ Jsoup 纯 HTTP（与 NickLitten 相同，无 JS 反爬）

步骤 1: 探测总页数
  Jsoup.connect("https://blog.faq400.com/en/")
  → 从页面标题解析 "Page X of Y" 格式 → 总页数 = 21

步骤 2: 逐页抓取列表 (for pageNum = 1 → 21)
  URL: https://blog.faq400.com/en/page/{N}/
  → 选择器: article
  → 提取每篇文章：
    • 标题: article h2 a, .entry-title a
    • 链接: 标题元素的 href
    • slug: 从 url 截取
    • 日期: article time, .entry-date
    • 分类: .cat-links a, .entry-categories a
    • 摘要: article .entry-summary p, article p:first-of-type
    • 封面图: article img 的 src/data-src

步骤 3: 逐篇抓取详情 (for each article)
  Jsoup.connect(article.getSourceUrl())
  → 标题: h1.entry-title（覆盖列表页标题）
  → 正文: .entry-content（在 <article> 内）
  → 清理: script, style, iframe 等噪音元素
  → 补全: publishDate, categories, coverImage
  → save() + delay 1s

步骤 4: 更新进度
  progress = (pageNum * 100) / 21
```

### 6.3 关键选择器

| 提取目标 | 列表页选择器 | 详情页选择器 |
|---------|------------|------------|
| 文章容器 | `article` | `article` |
| 标题 | `article h2 a`, `.entry-title a` | `h1.entry-title` |
| 日期 | `article time`, `.entry-date` | `time.published`, `meta[property="article:published_time"]` |
| 分类 | `.cat-links a`, `a[href*="/category/"]` | `.cat-links a` |
| 摘要 | `.entry-summary p`, `.entry-content p:first` | — |
| 正文 | — | `.entry-content` |
| 封面图 | `article img` 的 `src`/`data-src` | `.entry-content img:first` |

### 6.4 特点

- **Jsoup 直接可用**：无 JavaScript 反爬，与 NickLitten 抓取方式完全一致
- **中文（意大利语）博客**：内容以 IBM i 相关技术为主，英文版和意语版分两个路径
- **分页标准化**：标准 WordPress 分页格式，探测简单
- **文章量适中**：~168 篇，按 8 篇/页 + 1s 延迟 ≈ 约 3.5 分钟完成全量抓取

---

## 7. 规划中：rpgpgm.com 博客接入

### 7.1 目标网站分析

| 属性 | 值 |
|------|-----|
| URL | `https://www.rpgpgm.com/` |
| 平台 | **Blogger.com (Blogspot)** |
| 作者 | **Simon Hutchinson**（个人博客，使用 `.fn, .author` 选择器） |
| 文章总数 | **~1104 篇**（从 `List of all posts` 页面统计） |
| 分页格式 | ❌ **无分页** — 所有文章汇总在一个页面 |
| 文章列表页 | `/p/list-of-all-posts.html` — **所有文章链接在一个页面** |
| 文章详情 URL | `/YYYY/MM/post-slug.html`（Blogger 标准格式） |
| **反爬机制** | ❌ **无**（Blogger 标准模板，Jsoup 可直接抓取） |
| 详情页 HTML | 标题 `.post h3`，日期 `h2.date-header`，正文 `.post-body` |

### 7.2 抓取策略（独特：先全量URL后逐篇取）

```
抓取方式：✅ Jsoup 纯 HTTP

阶段一：获取所有文章 URL（从单一列表页）
  Jsoup.connect("https://www.rpgpgm.com/p/list-of-all-posts.html")
  → 页面结构：按主题分组的文章列表
  → 每篇文章格式：
    <li>
      <a href="/2017/01/steve-will-what-is-coming-in-2017.html">标题</a>
      日期文本
    </li>
  → 选择器提取：li a[href*=".html"]
  → 从链接文本获取标题
  → 从链接后的文本获取日期
  → 从 href 拼接完整 URL
  → 总数：~1104 篇

阶段二：逐篇抓取详情 (for each article URL)
  Jsoup.connect(articleUrl)
  → 标题: .post h3（覆盖列表页标题）
  → 作者: .fn, .author → "Simon Hutchinson"
  → 日期: h2.date-header（"Monday, January 30, 2017"）→ normalizeDate()
  → 分类: .post-labels a（单标签，如 "shared"）
  → 正文: .post-body
  → 噪音清理: script, style, .post-footer, .comment-link 等
  → 封面图: .post-body img:first 的 src
  → save() + delay 1s

阶段三：更新进度
  按已处理文章数 / 总数 计算百分比
```

### 7.3 关键选择器

| 提取目标 | 列表页选择器 | 详情页选择器 |
|---------|------------|------------|
| 文章条目 | `li a[href*=".html"]` | — |
| 标题 | `a` 的 `text()` | `.post h3` |
| URL | `a` 的 `href` 属性 | — |
| 日期（列表） | `a` 元素后的文本节点 | `h2.date-header`（"Monday, January 30, 2017"） |
| 作者 | — | `.fn, .author` → "Simon Hutchinson" |
| 分类/标签 | — | `.post-labels a` |
| 正文 | — | `.post-body` |
| 封面图 | — | `.post-body img:first` |

### 7.4 特点

- **最大文章量**：~1104 篇，是所有来源中最多的
- **一次获取所有 URL**：不需要分页遍历，一个列表页搞定
- **Blogger 标准模板**：结构规范，选择器稳定
- **需全部抓取详情**：列表页只有标题+日期，正文/分类/封面图需要逐篇访问详情页
- **预计时间**：1104 篇 × 1s 延迟 ≈ 约 18 分钟完成全量抓取（可考虑并行优化）
- **日期格式**：标准英文长日期格式 `EEEE, MMMM d, yyyy`，需要在 `normalizeDate()` 中新增支持

---

## 8. 规划中：as400andsqltricks.com 博客接入

### 8.1 目标网站分析

| 属性 | 值 |
|------|-----|
| URL | `https://www.as400andsqltricks.com/` |
| 平台 | **Blogger.com (Blogspot)** 自定义可视化模板 |
| 文章总数 | 待探测（通过年月归档统计） |
| 分页格式 | 首页无限滚动（JS 懒加载）；支持年月归档 `/YYYY/`、`/YYYY/MM/` |
| 文章详情 URL | `/YYYY/MM/PostSlug.html`（Blogger 标准格式） |
| 首页文章数 | ~22 篇（一次加载） |
| **反爬机制** | ❌ **无**（Blogger 标准模板，Jsoup 可直接抓取） |
| 详情页 HTML | 文章在 `<article>` 标签内，标题 `h2 a` |

### 8.2 抓取策略（通过年月归档或 Blogger Feed API）

```
抓取方式：✅ Jsoup 纯 HTTP（Blogger 平台，无 JS 反爬）

策略 A：通过 Blogger Feed API 批量获取（推荐）
  利用 Blogger 的 Atom Feed：
  GET https://www.as400andsqltricks.com/feeds/posts/default?max-results=500
  → 返回 XML 格式的完整文章列表（含标题/链接/日期/分类/摘要/作者）
  → 比解析 HTML 更稳定、更完整
  → 可通过 ?start-index=N 分页获取全部文章

策略 B：通过年月归档逐月抓取
  step 1: 探测有文章的年份（首页侧边栏有年份归档列表）
  step 2: 按年/月访问 /YYYY/ 或 /YYYY/MM/ 归档页
  step 3: 从归档页提取文章链接
  step 4: 逐篇访问详情页
  step 5: 提交日期/正文

策略 C：通过首页 + Next 链接遍历
  Jsoup 访问首页 → 提取 "Older Posts" 链接 → 循环访问

推荐方案：策略 A（Feed API）
```

### 8.3 关键选择器

| 提取目标 | 列表页选择器 | 详情页选择器 |
|---------|------------|------------|
| 文章条目 | `article` | `article` |
| 标题 | `article h2 a` | `h1.entry-title` 或 `h2.post-title` |
| 日期 | `article time`, `.post-date` | `meta[itemprop="datePublished"]` 的 `content` |
| 摘要 | `article .post-snippet` | — |
| 分类 | `.post-labels a` | `.post-labels a` |
| 正文 | — | `.post-body, .entry-content` |
| 封面图 | `article img` 的 `src` | `.post-body img:first` |

### 8.4 特点

- **Blogger Feed API 优势**：Blogger 平台提供标准 Atom Feed，包含完整文章元数据，是最高效的抓取方式
- **自定义模板**：非标准 Blogger 模板，HTML 选择器需访问具体页面确认
- **页面内大量图片**：博客文章包含大量截图/示意图，加载较慢
- **Feed API 格式**：
  ```xml
  <entry>
    <title>文章标题</title>
    <link href="文章URL"/>
    <published>2024-03-15T10:30:00.000+05:30</published>
    <category term="SQL"/>
    <author><name>作者名</name></author>
    <content type="html">正文HTML</content>
  </entry>
  ```

---

## 9. 五个来源对比总览

### 9.1 快速对比

| 维度 | NickLitten | Apimy WP | faq400 | rpgpgm | as400sql |
|------|-----------|----------|--------|--------|----------|
| **平台** | 独立站 | WordPress.com | 自建 WordPress | Blogger | Blogger |
| **source 标识** | `nicklitten` | `apimy` | `faq400` | `rpgpgm` | `as400sql` |
| **反爬** | ❌ 无 | ⚠️ JS 验证 | ❌ 无 | ❌ 无 | ❌ 无 |
| **抓取工具** | Jsoup | **Playwright** | Jsoup | Jsoup | Jsoup |
| **分页方式** | `/blog/page/N/` | `/blog/page/N/` | `/en/page/N/` | 单页全列表 | 年月归档 |
| **总页数** | ~76 | ~32 | 21 | 1（列表页） | N/A |
| **文章总数** | ~760 | ~320 | ~168 | ~1104 | 待探测 |
| **预计耗时** | ~12min | 取决于浏览器 | ~3.5min | ~18min | 取决于方式 |

### 9.2 抓取方式总结

```
Jsoup（纯 HTTP）可直接抓取：
  ├── nicklitten.com   ✅
  ├── blog.faq400.com  ✅
  ├── rpgpgm.com       ✅
  └── as400andsqltricks.com ✅

需要 Playwright（浏览器）：
  └── apimymymy.wordpress.com ⚠️ 有 Cloudflare JS 验证
```

### 9.3 实现优先级建议

| 优先级 | 来源 | 理由 |
|--------|------|------|
| 1 | `faq400` | Jsoup 可直接用，文章量小(168)，分页标准，最容易实现 |
| 2 | `rpgpgm` | Jsoup 可直接用，列表页一次性获取，文章最多但结构简单 |
| 3 | `as400sql` | Jsoup 可直接用，Feed API 最稳定，文章量待确认 |
| 4 | `apimy` | 需要 Playwright 浏览器，复杂度最高 |

---

## 10. 前后端变动对比

### 10.1 数据库变动

| 项 | 当前 | 改动 |
|----|------|------|
| 表名 | `tech_blog_article` | **不变** |
| 新增字段 | — | `source VARCHAR(50) DEFAULT 'nicklitten' COMMENT '博客来源'` |
| 索引变更 | `idx_source_url(source_url(255))` | 替换为 `idx_source_url_source (source_url(255), source)` |
| 表注释 | `'技术博客文章表(NickLitten)'` | `'技术博客文章表'` |

### 10.2 Entity 变动

```java
// TechBlogArticle.java 新增字段
/** 博客来源标识: nicklitten / apimy / faq400 / rpgpgm / as400sql */
private String source;
```

### 10.3 Service 变动

| 方法 | 当前 | 改动 | 说明 |
|------|------|------|------|
| `pageQuery()` | 无 source 参数 | 增加 `String source` 参数 | 按来源过滤 |
| `getAllCategories()` | 无 source 参数 | 增加 `String source` 参数 | 按来源过滤分类（或全量） |
| `getRecent()` | 无 source 参数 | 增加 `String source` 参数 | 按来源过滤 |
| `startFetch()` | 无参数，裸 `new Thread()` | 改为 `startFetch(String source)`，用 `ExecutorService` 提交 | 指定抓取目标，线程池管理，按 source 独立守卫 |
| `doFetch()` | 尼克专用 | 拆分为 5 个独立方法 | `doFetchNicklitten()` + `doFetchApimy()` + `doFetchFaq400()` + `doFetchRpgpgm()` + `doFetchAs400sql()` |
| `scrapeListPage()` | Jsoup 解析尼克页面 | 各来源独立实现 | 选择器完全不同 |
| `scrapeDetail()` | Jsoup 解析尼克详情 | 各来源独立实现 | 选择器完全不同 |
| `detectTotalPages()` | 解析 `.tcb-pagination` | 按来源分策略 | faq400: 解析页面标题 "Page X of Y"；rpgpgm: 无需探测（单页） |
| `existsBySourceUrl()` | 只查 sourceUrl | 改为 `existsBySourceUrlAndSource()` | 不同来源可能有相同文章 slug |
| `normalizeDate()` | 已有 8 种格式 | 增加 Blogger 日期格式 | `EEEE, MMMM d, yyyy`（Monday, January 30, 2017） |
| `getFetchProgress()` | 返回单个 int | 改为 `getFetchProgress(String source)` | 按 source 查询独立进度 |
| `getFetchLogs()` | 返回单个 List | 改为 `getFetchLogs(String source)` | 按 source 查询独立日志 |
| 进度状态 | `AtomicInteger` | `Map<String, AtomicInteger>`（`ConcurrentHashMap`） | 多源并发独立追踪 |
| 日志状态 | `List<String>` | `Map<String, List<String>>`（`ConcurrentHashMap`） | 多源独立日志 |
| 线程管理 | `new Thread()` 裸线程 | `ExecutorService` 固定 4 线程池 | 控制并发度，可优雅关闭 |

### 10.4 Controller 变动

| 接口 | 当前 | 改动 |
|------|------|------|
| `GET /articles` | 参数：page, size, keyword, category | 增加 `source` 参数（可选） |
| `GET /categories` | 无参数 | 增加 `source` 参数（可选，不传返回全量） |
| `GET /recent` | 参数：limit | 增加 `source` 参数（可选） |
| `POST /fetch` | 无参数，只抓 nicklitten | `@RequestBody {source:"faq400"}`，支持多次调用触发不同源 |
| `GET /progress` | 返回单个进度+日志 | 支持 `?source=faq400` 查单源，不传返回所有源进度概览 |

### 10.5 前端变动

#### API 层 (`techBlog.js`)
- 所有 6 个函数增加 `source` 参数传递

#### 列表页 (`index.vue`)

| 区域 | 当前 | 改动 |
|------|------|------|
| **新增** | 无 | **博客来源切换器**（el-tabs 或 el-select）|
| 副标题 | `"Nick Litten's IBM i Blog"` | 动态：NickLitten 时显示原文本，Apimy 时显示对应文本 |
| 同步按钮 tooltip | `"从 nicklitten.com 抓取..."` | 动态根据当前 source |
| 同步进度提示 | `"正在从 nicklitten.com 同步..."` | 动态 |
| 空状态文字 | `"从 Nick Litten 博客抓取"` | 动态 |
| 搜索/分类/分页 | 不变 | 传递 `source` 参数 |

#### 详情页 (`detail.vue`)
- **基本不需要改动**，只展示数据

### 10.6 路由与菜单
- 当前菜单/路由不需要变动
- 所有文章统一通过列表页浏览，通过 source 切换筛选

---

## 11. 多源并发抓取方案

### 11.1 核心问题

当前 `TechBlogArticleService` 的抓取架构是**单线程、单源**设计：

| 组件 | 当前值 | 阻塞并发的原因 |
|------|--------|--------------|
| `fetchProgress` | `AtomicInteger` 单值 | 只能存一个进度，第二个 source 启动会覆盖 |
| `fetchLogs` | `List<String>` 单列表 | 多源日志混在一起无法区分 |
| `startFetch()` 守卫 | `if (progress >= 0 && < 100) return` | 只要任一源在抓取，其他源直接被拒绝 |
| 线程创建 | `new Thread(this::doFetch)` 裸线程 | 无线程池，多源无法并行启动 |
| `doFetch()` | 硬编码 nicklitten URL 常量 | 只能抓一个站 |

### 11.2 数据写入并发安全性论证

逐篇写入流程中，多线程同时写 DB 的场景：

```
线程A (faq400):   existsBySourceUrl(u1) → scrapeDetail() → save(a1)
线程B (rpgpgm):   existsBySourceUrl(u2) → scrapeDetail() → save(a2)
线程C (as400sql): existsBySourceUrl(u3) → scrapeDetail() → save(a3)
线程D (apimy):    existsBySourceUrl(u4) → scrapeDetail() → save(a4)
```

| 风险点 | 分析 | 结论 |
|--------|------|------|
| **URL 去重冲突** | 不同来源的域名完全不同，`sourceUrl` 不可能重复 | ✅ 安全 |
| **INSERT 并发** | MyBatis Plus 每次 `save()` 是独立 INSERT 新行，不冲突 | ✅ 安全 |
| **Jsoup connect()** | 每个线程独立创建 HTTP 连接，不共享 Session/Cookie | ✅ 安全 |
| **HikariCP 连接池** | Spring Boot 默认 10 连接，4 个爬虫线程绰绰有余 | ✅ 安全 |
| **目标站点隔离** | 4 个源部署在不同域名/服务器，互不影响 | ✅ 安全 |
| **内存** | 每篇文章对象逐个处理后即释放，不在内存中累积 | ✅ 安全 |

**结论：数据写入层完全支持并发，无竞态条件。**

### 11.3 两种并发策略

#### 方案 A：真并行 — `ExecutorService` 线程池（推荐）

```
ExecutorService pool = Executors.newFixedThreadPool(4);

startFetch("faq400")  ──→ [线程1] 抓取 ~168篇  ≈ 3.5min
startFetch("rpgpgm")  ──→ [线程2] 抓取 ~1104篇 ≈ 18min
startFetch("as400sql") ──→ [线程3] 抓取 ~?篇   ≈ ?min
startFetch("apimy")   ──→ [线程4] 抓取 ~320篇  ≈ ?min

总耗时 = max(3.5, 18, ?, ?) ≈ 18min（以最慢源为准）
```

| 维度 | 值 |
|------|-----|
| 总耗时 | **~18 分钟**（依最长源而定） |
| 改动量 | 约 60 行（进度 Map 化 + ExecutorService + Controller 适配） |
| 用户体验 | 4 个源可同时发起，独立查看各自进度 |

#### 方案 B：顺序队列 — 单线程依次执行

```
startFetch(["faq400","rpgpgm","as400sql","apimy"])

单线程: faq400(3.5min) → rpgpgm(18min) → as400sql(?min) → apimy(?min)
总耗时 = 3.5 + 18 + ? + ? ≈ 40+ 分钟
```

| 维度 | 值 |
|------|-----|
| 总耗时 | **~40+ 分钟**（累加） |
| 改动量 | 约 20 行（仅循环遍历 source） |
| 用户体验 | 前端只能看到一个接一个的进度 |

**推荐方案 A**：改动量增加不多，但总时间从 40+ 分钟降到约 18 分钟，收益显著。

### 11.4 方案 A 具体设计

#### 11.4.1 进度与日志改造

```java
// 当前（单源）
private final AtomicInteger fetchProgress = new AtomicInteger(-1);
private final List<String> fetchLogs = Collections.synchronizedList(new ArrayList<>());

// 改造后（多源独立追踪）
private final Map<String, AtomicInteger> progressMap = new ConcurrentHashMap<>();
private final Map<String, List<String>> logsMap = new ConcurrentHashMap<>();
private final ExecutorService executor = Executors.newFixedThreadPool(4);
```

#### 11.4.2 startFetch() 按 source 守卫

```java
public void startFetch(String source) {
    // 每个 source 独立检查运行状态
    AtomicInteger progress = progressMap.computeIfAbsent(source, 
        k -> new AtomicInteger(-1));
    if (progress.get() >= 0 && progress.get() < 100) {
        log.warn("来源 {} 正在抓取中，忽略重复请求", source);
        return; // 仅拒绝当前 source，不影响其他 source
    }
    
    progress.set(0);
    logsMap.put(source, Collections.synchronizedList(new ArrayList<>()));
    logsMap.get(source).add("开始抓取: " + source);
    
    executor.submit(() -> {
        try {
            switch (source) {
                case "nicklitten" -> doFetchNicklitten();
                case "faq400"    -> doFetchFaq400();
                case "rpgpgm"    -> doFetchRpgpgm();
                case "as400sql"  -> doFetchAs400sql();
                case "apimy"     -> doFetchApimy();
                default -> throw new IllegalArgumentException("未知来源: " + source);
            }
        } catch (Exception e) {
            progress.set(-1);
            logsMap.get(source).add("❌ 抓取出错: " + e.getMessage());
            log.error("{} 抓取失败", source, e);
        }
    });
}
```

#### 11.4.3 各 doFetch 方法中的进度更新

每个 `doFetchXxx()` 方法内部改为：

```java
private void doFetchFaq400() {
    String source = "faq400";
    AtomicInteger progress = progressMap.get(source);
    List<String> logs = logsMap.get(source);
    
    // ... 抓取逻辑 ...
    
    // 进度更新改为操作本 source 的对象
    progress.set((int)(pageNum * 100.0 / totalPages));
    logs.add("正在抓取第 " + pageNum + "/" + totalPages + " 页...");
    
    // 完成后
    progress.set(100);
    logs.add("✅ 抓取完成! 共保存 " + totalSaved + " 篇新文章");
}
```

#### 11.4.4 Controller 提供按 source 查询

```java
// 触发抓取
@PostMapping("/fetch")
public Result<String> fetch(@RequestBody Map<String, String> body) {
    String source = body.getOrDefault("source", "nicklitten");
    articleService.startFetch(source);
    return Result.ok("抓取任务已启动，source=" + source);
}

// 查询进度（支持按 source 过滤）
@GetMapping("/progress")
public Result<Map<String, Object>> progress(
        @RequestParam(required = false) String source) {
    if (source != null) {
        // 查询单个 source
        return Result.ok(Map.of(
            "source", source,
            "progress", articleService.getFetchProgress(source),
            "logs", articleService.getFetchLogs(source)
        ));
    } else {
        // 返回所有 source 的进度概览
        Map<String, Integer> allProgress = new LinkedHashMap<>();
        for (String src : ALL_SOURCES) {
            allProgress.put(src, articleService.getFetchProgress(src));
        }
        return Result.ok(Map.of("allProgress", allProgress));
    }
}
```

### 11.5 前端多源抓取面板

列表页可扩展为展示各来源的抓取状态面板：

```
┌─────────────────────────────────────────────┐
│ 博客来源              │ 进度  │ 操作        │
├─────────────────────────────────────────────┤
│ 🟢 NickLitten (已同步)│ 100%  │ [重新抓取]  │
│ ⏳ faq400 (抓取中...) │ 45%   │ [查看日志]  │
│ ⚪ rpgpgm (待抓取)    │ -     │ [开始抓取]  │
│ ⚪ as400sql (待抓取)  │ -     │ [开始抓取]  │
│ ⚪ apimy (待抓取)     │ -     │ [开始抓取]  │
└─────────────────────────────────────────────┘
```

每个 source 独立显示进度条，可同时观察多个源的抓取状态。

### 11.6 断点续传（天然支持）

由于去重逻辑 `existsBySourceUrl()` 对已入库文章自动跳过，即便抓取中途中断：
- 再次调用 `startFetch(source)` 时，已入库的文章会被 `continue` 跳过
- 抓取从上次中断处附近继续（页码可能需重跑，但文章不重复入库）

**无需额外实现断点续传机制。**

### 11.7 rpgpgm.com 的特殊注意事项

rpgpgm.com 有 ~1104 篇文章，单线程约 18-22 分钟（1s 延迟 × 1104 篇），但它是 Jsoup 纯 HTTP，不会阻塞其他源的 Playwright 浏览器线程。

| 关注点 | 处理 |
|--------|------|
| 耗时最长 | 占用线程池中的一个线程约 18min，不影响其他 3 个线程 |
| 中途重启 | 已入库文章自动跳过，无需重抓 |
| 日志量 | 1104 行日志，`logsMap.get("rpgpgm")` 列表占用内存约 110KB，可接受 |
| 目标站压力 | 1s 延迟足够友好，不会触发反爬 |

### 11.8 并发安全性总结

| 层面 | 安全性 | 说明 |
|------|--------|------|
| DB 写入 | ✅ 安全 | 不同 source 不同 URL，INSERT 不冲突 |
| 内存状态 | ✅ 安全 | `ConcurrentHashMap` + `AtomicInteger` 天然线程安全 |
| 网络请求 | ✅ 安全 | Jsoup 无共享连接池，各线程独立 |
| Playwright | ✅ 安全 | Playwright Java 支持多 BrowserContext 并发，一个线程一个 context |
| 进度展示 | ✅ 安全 | 每个 source 独立进度，互不覆盖 |

---

## 12. 开发排期建议

### 阶段一：数据表改造 + 并发框架（所有来源的前置依赖）
- [ ] 执行 DDL 添加 `source` 字段，重建索引
- [ ] 为已有 NickLitten 数据回填 `source = 'nicklitten'`
- [ ] 修改 Entity 类添加 `source` 属性
- [ ] 进度状态改造：`AtomicInteger` → `Map<String, AtomicInteger>`（`ConcurrentHashMap`）
- [ ] 日志状态改造：`List<String>` → `Map<String, List<String>>`（`ConcurrentHashMap`）
- [ ] 引入 `ExecutorService` 线程池（`Executors.newFixedThreadPool(4)`）
- [ ] `startFetch()` 改造为 `startFetch(String source)`，按 source 独立守卫
- [ ] 重构 `doFetch()` 为各 source 独立方法的路由分发

### 阶段二：faq400 抓取（最简单，快速出成果）
- [ ] 编写 `doFetchFaq400()` 抓取主方法
- [ ] 编码分页探测（解析 "Page X of 21"）
- [ ] 编码列表页解析（Jsoup + article 选择器）
- [ ] 编码详情页解析 + 噪音清理

### 阶段三：rpgpgm 抓取（文章最多）
- [ ] 编写 `doFetchRpgpgm()` 抓取主方法
- [ ] 编码 `scrapeAllPostUrls()` — 从单页提取全部 ~1104 个 URL
- [ ] 编码详情页解析（Blogger .post h3 + .post-body 选择器）
- [ ] `normalizeDate()` 增加 Blogger 英文长日期格式支持
- [ ] 考虑分批/断点续传以应对长耗时（~18min）

### 阶段四：as400sql 抓取（推荐 Feed API）
- [ ] 验证 Blogger Feed API: `GET /feeds/posts/default?max-results=500`
- [ ] 编写 `doFetchAs400sql()` — 优先使用 Feed XML 解析
- [ ] 如果 Feed 不可用，fallback 到年月归档逐月抓取
- [ ] 编码详情页解析 + 噪音清理

### 阶段五：Apimy 抓取（最复杂）
- [ ] pom.xml 添加 Playwright Java 依赖
- [ ] 编写 `doFetchApimy()` 抓取主方法
- [ ] 编码分页探测逻辑（二分法）
- [ ] 编码列表页解析（Playwright 选择器）
- [ ] 编码详情页解析 + 噪音清理

### 阶段六：Controller 适配
- [ ] 各接口增加 `source` 参数
- [ ] `/fetch` 接口支持指定来源
- [ ] `/progress` 接口支持按 source 查询

### 阶段七：前端适配
- [ ] API 层增加 `source` 参数
- [ ] 列表页增加博客来源切换器（el-tabs 或 el-select，5 个选项）
- [ ] 动态文案适配（标题、副标题、提示文字根据 source 变化）

### 阶段八：测试
- [ ] 各来源全量抓取测试
- [ ] 增量去重测试（再次点击不重复入库）
- [ ] 前端多源切换测试
- [ ] 搜索/分类筛选在各来源下的正确性验证
- [ ] 并发安全性验证（不同来源同时抓取不互相影响）

---

## 附录

### A. 相关文件路径速查

| 文件 | 绝对路径 |
|------|---------|
| 建表 SQL | `src/main/resources/db/techblog_init.sql` |
| Entity | `src/main/java/com/rx/admin/entity/TechBlogArticle.java` |
| Mapper | `src/main/java/com/rx/admin/mapper/TechBlogArticleMapper.java` |
| Service | `src/main/java/com/rx/admin/service/TechBlogArticleService.java` |
| Controller | `src/main/java/com/rx/admin/controller/TechBlogController.java` |
| pom.xml | `pom.xml`（Jsoup 依赖） |
| 前端 API | `ui/src/api/techBlog.js` |
| 前端列表页 | `ui/src/views/as400/techblog/index.vue` |
| 前端详情页 | `ui/src/views/as400/techblog/detail.vue` |
| 路由组件映射 | `ui/src/router/componentMap.js` |

### B. 依赖版本参考

| 依赖 | 推荐版本 | 说明 |
|------|---------|------|
| Jsoup | 1.17.2 | 通用爬虫（已引入，覆盖尼克/faq400/rpgpgm/as400sql） |
| Playwright Java | 1.48.0 | Apimy WordPress 爬虫（待引入，仅此一个源需要） |
| Playwright CLI 浏览器 | 最新 Chromium | `playwright install chromium` |

### C. source 标识对照表

| source 值 | 博客名称 | URL |
|-----------|---------|-----|
| `nicklitten` | Nick Litten's IBM i Blog | `https://www.nicklitten.com/blog/` |
| `apimy` | API My My My | `https://apimymymy.wordpress.com/blog/` |
| `faq400` | BlogFaq400 (English) | `https://blog.faq400.com/en/` |
| `rpgpgm` | RPGPGM.COM | `https://www.rpgpgm.com/` |
| `as400sql` | AS400 and SQL Tricks | `https://www.as400andsqltricks.com/` |

### D. 页面结构快照参考

以下 Playwright CLI 快照 YAML 文件可用于确定具体 CSS 选择器：

| 快照文件 | 对应页面 |
|---------|---------|
| `.playwright-cli/page-2026-06-04T13-36-30-662Z.yml` | Apimy WordPress 博客首页 |
| `.playwright-cli/page-2026-06-04T13-37-20-298Z.yml` | Apimy WordPress 文章详情示例 |
| `.playwright-cli/page-2026-06-04T13-53-30-279Z.yml` | faq400 博客首页（英文版） |
| `.playwright-cli/page-2026-06-04T13-54-20-961Z.yml` | rpgpgm 全文章列表页 |
| `.playwright-cli/page-2026-06-04T13-54-30-625Z.yml` | as400andsqltricks 首页 |
| `.playwright-cli/page-2026-06-04T13-56-28-470Z.yml` | as400andsqltricks 文章详情示例 |

### E. Blogger Feed API 参考

Blogger 平台提供标准 Atom Feed，可通过以下 URL 获取文章（推荐用于 as400andsqltricks.com 和 rpgpgm.com）：

```
# 格式
https://{blog-url}/feeds/posts/default?max-results={n}&start-index={m}

# 示例：获取最多 500 篇文章
https://www.as400andsqltricks.com/feeds/posts/default?max-results=500

# 分页：获取第 501 篇开始的下 500 篇
https://www.as400andsqltricks.com/feeds/posts/default?max-results=500&start-index=501
```

Feed XML 结构包含：
- `<title>` — 文章标题
- `<link rel="alternate" href="...">` — 文章 URL
- `<published>` — ISO 8601 发布日期
- `<updated>` — 最后更新日期
- `<category term="...">` — 分类标签
- `<author><name>...</name></author>` — 作者
- `<content type="html">` — 完整 HTML 正文
- `<media:thumbnail url="...">` — 封面图（如果模板支持）

### F. 扩展性考虑

当前设计可轻松扩展到更多博客来源，只需：
1. 在 `source` 字段中定义新的来源标识（如 `source = 'newblog'`）
2. 新增对应的 `doFetchXxx()` 抓取方法
3. 在 `startFetch(source)` 中注册路由
4. 前端下拉/标签中增加新的来源选项

无需新建数据表，所有博客文章共用同一张表。
