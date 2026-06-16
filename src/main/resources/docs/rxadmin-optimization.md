# RX Admin 优化建议清单

> **版本**: 3.5 | **更新日期**: 2026-06-13 | **类型**: 项目优化规划（含已完成项记录 + 全新审查发现 + v2.0 新功能实施 + 架构重构记录）

---

## 目录

1. [安全加固](#1-安全加固)
2. [缓存与性能](#2-缓存与性能)
3. [实时推送](#3-实时推送)
4. [运维与部署](#4-运维与部署)
5. [功能增强](#5-功能增强)
6. [代码质量](#6-代码质量)
7. [新增功能模块](#7-新增功能模块)
8. [实现状态跟踪](#8-实现状态跟踪)

---

## 1. 安全加固

### 1.1 验证码（P0）✅ 已实现

**方案**: 后端生成图片验证码（Java BufferedImage + Graphics2D），前端以 Base64 渲染。
- `GET /api/auth/captcha` → 返回 `{ uuid, image(base64) }`
- `POST /api/auth/login` 增加 `captchaUuid` + `captchaCode` 参数
- 验证码使用 ConcurrentHashMap 内存缓存，5 分钟 TTL，一次性使用
- 开发环境验证码固定为 `dev000`（CaptchaService.java 跳过验证）
- 定期清理过期验证码

**涉及文件**:
- `CaptchaController.java`, `CaptchaService.java`, `CaptchaUtil.java`
- 前端 `login/index.vue` 增加验证码输入框 + 点击刷新

### 1.2 XSS 防护（P1）✅ 已实现

**方案**: Jackson `JsonDeserializer` 全局过滤，自动转义所有 String 类型输入中的 HTML 特殊字符

**涉及文件**:
- `XssJacksonConfig.java` — Jackson 反序列化时自动 HTML 转义

### 1.3 API 防重放（P1）✅ 已实现

**方案**: 客户端请求头携带 `X-Timestamp` + `X-Nonce`，服务端校验时间窗口和 nonce 唯一性
- 写操作（POST/PUT/DELETE）自动添加请求头
- 服务端 5 分钟内相同 nonce 拒绝重复请求

**涉及文件**:
- `ReplayAttackFilter.java` — 全局过滤器
- `utils/request.js` — 前端拦截器自动添加

### 1.4 密码策略（P1）✅ 已实现

**方案**: 后端 `@Pattern` 正则校验 + 前端密码强度指示器

### 1.5 敏感数据脱敏（P1）✅ 已实现

**方案**: 
- MyBatis-Plus `AesTypeHandler` 自动 AES 加解密手机号、邮箱
- `DataMaskUtil.java` 返回前端时脱敏显示

**涉及文件**:
- `AesTypeHandler.java`, `DataMaskUtil.java`

### 1.6 IP 黑白名单（P2）✅ 已实现

**方案**: 新增 `sys_ip_rule` 表 + 过滤器级别拦截

---

### 1.7 v-html XSS 风险（P1）✅ 已完成

**现状**: 项目中 8 处使用 `v-html` 渲染动态内容，未做 HTML sanitize：

| 文件 | 行号 | 渲染内容 |
|------|------|---------|
| `views/tool/docs/index.vue` | 22 | Markdown 渲染 HTML |
| `views/tool/standards/index.vue` | 22 | Markdown 渲染 HTML |
| `views/content/notice/index.vue` | 103 | 公告内容（用户可编辑） |
| `views/as400/techblog/detail.vue` | 52 | 博客文章内容 |
| `views/classics/literature/works/index.vue` | 183 | 文学作品内容 |
| `views/as400/iservice/index.vue` | 102, 179, 213 | iService 描述 |

**风险**: 公告内容、博客正文等字段可能包含用户输入的恶意 `<script>` 标签，直接 `v-html` 会导致 XSS 攻击。

**建议**: 
1. 安装 `dompurify`，对所有 `v-html` 绑定的内容调用 `DOMPurify.sanitize(html)`
2. 或在后端输出时对 HTML 做白名单标签过滤（仅允许 `p, a, img, table, h1-h6, code, pre` 等安全标签）

---

### 1.8 Controller 输入校验缺失（P1）✅ 已完成

**现状**: 仅 `AuthController` 使用了 `@Valid`。大量 Controller 的 `@RequestBody` 参数完全没有校验：

| 文件 | 方法 | 说明 |
|------|------|------|
| `SysUserController.java` | `add()`, `update()` | 缺少 `@Valid`，用户名/邮箱格式未校验 |
| `SysMenuController.java` | `add()`, `update()` | 缺少 `@Valid` |
| `SysConfigController.java` | `add()` | 缺少 `@Valid` |
| `SysRoleController.java` | `add()`, `update()` | 缺少 `@Valid` |

**建议**: 在实体类字段添加 Bean Validation 注解（`@NotBlank`, `@Size`, `@Email` 等），在 Controller 参数上加 `@Valid`。

---

### 1.9 未授权音乐流接口（P1）✅ 已完成

**现状**: `MusicController.java` 第 103-153 行的 `/api/music/stream/{id}` 接口**没有 `@SaCheckLogin` 注解**，而同一 Controller 中其他所有接口都有鉴权。

**风险**: 未登录用户可直接访问 MP3 文件流，绕过认证机制。

**建议**: 添加 `@SaCheckLogin` 或至少验证请求中的 token 参数。

---

### 1.10 Token 明文 localStorage 存储（P2）✅ 已完成

**现状**: `composables/useStorage.js` 将 token 明文存在 `localStorage`（键名 `rx_admin_token`），通过 `Authorization` header 直接传输。

**建议**: 
1. 优先方案：改为 HttpOnly Cookie 传输（JS 无法读取，防止 XSS 窃取）
2. 次选方案：使用 Web Crypto API 对 token 做加密后再存 localStorage
3. 考虑实现 refresh token 机制，缩短 access token 有效期

---

### 1.11 登录表单开发凭据残留（P2）✅ 已完成

**现状**: `views/login/index.vue` 第 108-111 行在 `loginForm` 中预填了 `admin / admin123 / dev000`。

**建议**: 用 `import.meta.env.DEV` 包裹默认值，生产构建时自动移除。

---

## 2. 缓存与性能

### 2.1 Redis 缓存（已取消）

> **决策**：项目不引入 Redis，改用 Caffeine 本地缓存（见 2.6）。重启后数据丢失对于系统配置、菜单树等场景可接受（启动时自动从 DB 加载重建）。

### 2.2 Sa-Token Redis 切换（已取消）

> **决策**：Sa-Token 保持内存模式，不做 Redis 切换。重启后用户需重新登录，对于单体应用可接受。

### 2.3 慢查询监控（P2）✅ 已实现



**方案**: MyBatis StatementHandler 拦截器 + sys_slow_query 表自动记录慢 SQL

- 默认阈值 2 秒，超过自动入库

- 记录 SQL 文本、参数、耗时、类型、Mapper 方法

- 前端管理页面支持查看、筛选、删除、清空



**涉及文件**:

- `SlowQueryInterceptor.java` — MyBatis 插件

- `SysSlowQuery.java`, `SysSlowQueryMapper.java`, `SysSlowQueryService.java`, `SysSlowQueryController.java`

- `monitor/slow-query/index.vue` — 前端页面（使用 vue-virtual-scroller）

- `api/slowQuery.js` — 前端 API

### 2.4 前端虚拟滚动（P2）✅ 已实现

**方案**: 使用 `vue-virtual-scroller` 的 `RecycleScroller` 组件，仅渲染可视区域 DOM，大幅减少大列表的内存占用和渲染开销。

**已接入页面**:
- `monitor/slow-query/index.vue` — 慢查询监控列表，固定 500px 高度的虚拟滚动容器
- `monitor/online/index.vue` — 在线用户列表，支持列切换和排序
- `monitor/log/index.vue` — 操作日志列表，支持多选、批量删除、列切换和排序

**涉及文件**:
- `monitor/slow-query/index.vue`（已接入 RecycleScroller）
- `package.json`（依赖已安装）

---

### 2.5 Dashboard N+1 查询优化（P1）✅ 已完成

**现状**: `DashboardController.stats()` 方法存在多处 N+1 查询问题：

| 位置 | 代码行 | 问题 |
|------|--------|------|
| 历朝代作者统计 | 113-128 行 | 遍历所有文学作品做内存 distinct 计数，数据量大时极慢 |
| Top10 作者查询 | 148-157 行 | 对每个作者单独调用 `authorService.getById()` |
| 博客来源统计 | 215-221 行 | 对每个 source 单独调用 `count()` |

**建议**:
1. 历朝代统计改为 SQL: `SELECT dynasty_id, COUNT(DISTINCT author_id) FROM literary_works GROUP BY dynasty_id`
2. Top10 查询用 `authorService.listByIds(ids)` 批量查询或 JOIN 一次性查出
3. 博客统计改为: `SELECT source, COUNT(*) FROM tech_blog_articles GROUP BY source`

---

### 2.6 高频数据缓存策略（P1）✅ 已完成（2026-06-05）

**方案**: 引入 Caffeine 本地缓存（`spring-boot-starter-cache`），对三类高频读取数据分别缓存：

| 缓存名 | 缓存对象 | 缓存方式 | TTL | 清除策略 |
|--------|---------|---------|-----|---------|
| `config` | 系统配置（`getValue`/`getValues`/`getGrouped`） | `@Cacheable("config")` | 10 分钟 | `@CacheEvict(allEntries=true)`，写操作触发 |
| `menu` | 菜单树（`getRouterMenus`/`getAllMenuTree`/`getRequestableMenus`） | `@Cacheable("menu")`，按 userId 隔离 | 1 小时 | `@CacheEvict(allEntries=true)`，菜单增删改触发 |
| `cachedStats` | 仪表盘统计 | `@Scheduled(fixedRate=30s)` + volatile 字段 | 30 秒 | 定时自动刷新 |

**具体实现**:
1. `pom.xml` 新增 `spring-boot-starter-cache` + `caffeine` 依赖
2. 新增 `CacheConfig.java`：`@EnableCaching` + `SimpleCacheManager`，两套 Caffeine 缓存配置
3. `SysConfigService.getValue/getValues/getGrouped` 加 `@Cacheable(value = "config")`
4. `SysConfigController.updateValue/add/delete` 加 `@CacheEvict(value = "config", allEntries = true)`
5. `SysMenuService.getRouterMenus/getAllMenuTree/getRequestableMenus` 加 `@Cacheable(value = "menu")`
6. `SysMenuController.add/update/delete` 加 `@CacheEvict(value = "menu", allEntries = true)`
7. `DashboardController`：抽取 `computeStats()` + `@Scheduled refreshDashboardCache()` + `@PostConstruct init()`，API/SSE 直接读取 `volatile cachedStats`
8. `application.yml` 新增 `app.cache.config-ttl-seconds: 600`、`app.cache.menu-ttl-seconds: 3600`、`app.cache.dashboard-refresh-ms: 30000`

**效果**: 系统配置查询从每次 DB 查询降为 0 次（缓存命中时），菜单构建从每次请求降为 0 次（缓存命中时），仪表盘 SSE 推送从每次 30+ 条 SQL 降为定时 1 次批量计算。

**涉及文件**:
- `pom.xml` — 新增 Caffeine 依赖
- `CacheConfig.java` — 缓存管理器配置（**新增**）
- `application.yml` — 缓存 TTL + 刷新间隔配置
- `SysConfigService.java` — `@Cacheable` 注解
- `SysConfigController.java` — `@CacheEvict` 注解
- `SysMenuService.java` — `@Cacheable` 注解
- `SysMenuController.java` — `@CacheEvict` 注解
- `DashboardController.java` — `@Scheduled` 预计算缓存

---

### 2.7 SSE 定时任务线程池泄漏（P2）✅ 已完成

**现状**: `DashboardController.stream()` 第 237-251 行，每次客户端连接 SSE 都 `newSingleThreadScheduledExecutor()` 创建新线程池，客户端断开时没有 shutdown。

**风险**: 多客户端同时连接时，线程池数量线性增长，可能导致 OOM。

**建议**: 使用共享的 `ScheduledExecutorService` Bean，在 `SseEmitter.onCompletion()` / `onTimeout()` 回调中取消定时任务。

---

## 3. 实时推送

### 3.1 WebSocket 通知（P1）❌ 待实现
### 3.2 SSE 服务端推送（P2）✅ 已实现

**方案**: 后端 SseEmitter + 前端 EventSource 订阅
- 后端 `DashboardController.stream()` 通过 SSE 推送实时统计数据
- 前端 `startSse()` 创建 `EventSource` 连接 `/api/dashboard/stream`
- 监听 `stats` 事件，解析 JSON 数据后更新仪表盘统计卡片数值
- `stopSse()` 关闭连接，`onBeforeUnmount` 中自动清理
- 带 token 参数传递用户身份

**涉及文件**:
- `DashboardController.java`（SseEmitter 端点）
- `dashboard/index.vue`（`startSse()` / `stopSse()` 逻辑）

---

### 3.3 后台轮询跳过 NProgress 进度条（P2）✅ 已完成

**问题**: NoticePopover 通知轮询（每15秒）和 TechBlog 抓取进度轮询（每2秒）未设置 `_skipNProgress: true`，导致每次定时触发时页面顶部进度条都会闪烁一次，用户体验很差。而 `/api/auth/ping` 心跳（每10秒）已正确使用 `_skipNProgress: true` 跳过进度条。

**修复方案**:
- API 函数增加可选的 `options` 参数，支持 axios config 覆盖
- 定时轮询调用时传入 `{ _skipNProgress: true }`

```javascript
// notice.js — 增加 options 参数
export function getNoticePageApi(params, options = {}) {
  return request({ url: '/content/notice/page', method: 'get', params, ...options })
}
export function getNoticeSummaryApi(options = {}) {
  return request({ url: '/content/notice/summary', method: 'get', ...options })
}

// NoticePopover.vue — 定时轮询跳过进度条
getNoticePageApi({ page: 1, size: 50 }, { _skipNProgress: true })
getNoticeSummaryApi({ _skipNProgress: true })

// techblog/index.vue — 抓取进度轮询跳过进度条
getFetchProgressApi(source, { _skipNProgress: true })
```

**涉及文件**:
- `api/notice.js` — 增加 options 参数
- `layout/NoticePopover.vue` — 调用时传入 `_skipNProgress: true`
- `api/techBlog.js` — 增加 options 参数
- `views/as400/techblog/index.vue` — 调用时传入 `_skipNProgress: true`
- `utils/request.js` — 请求拦截器 `if (!config._skipNProgress)` 机制说明

---

### 3.4 全量魔法数字消除与可配置化（P2）✅ 已完成

**问题**: 项目中存在大量硬编码的"魔法数字"——超时值、轮询间隔、表格行高、分页大小、延迟时间等。这些值分散在多个文件中，修改需要改代码，且容易遗漏。共发现 **50+ 处硬编码数字**。

**修复方案**: 
1. **前端**：所有超时/间隔/尺寸从 `.env` 环境变量读取（`import.meta.env.VITE_xxx || fallback`）
2. **后端**：`@Scheduled` 和魔法数字改为 `application.yml` + `@Value` 注入
3. **classics 10 个页面**：抽取共享 composable `useTableHeight.js`，消除 30 处重复常量定义

**新增/修改的环境变量**:

| 变量 | 默认值 | 控制对象 | 文件 |
|------|--------|---------|------|
| `VITE_API_REQUEST_TIMEOUT` | 15000 | API 请求超时 | request.js |
| `VITE_AS400_REQUEST_TIMEOUT` | 60000 | AS400 请求超时 | api/as400.js |
| `VITE_TABLE_ROW_HEIGHT` | 48 | 表格行高 | useTablePage.js |
| `VITE_TABLE_PADDING` | 120 | 表格底部预留 | useTablePage.js |
| `VITE_DEFAULT_PAGE_SIZE` | 10 | 默认分页大小 | useTablePage.js |
| `VITE_PAGE_SIZE_OPTIONS` | 10,20,50,100 | 分页选项 | useTablePage.js |
| `VITE_TABLE_MAX_HEIGHT` | 400 | 表格最大高度 | useTablePage.js |
| `VITE_CLASSICS_TABLE_ROW_HEIGHT` | 44 | classics 行高 | useTableHeight.js |
| `VITE_CLASSICS_TABLE_HEADER_HEIGHT` | 40 | classics 表头高 | useTableHeight.js |
| `VITE_CLASSICS_TABLE_MAX_ROWS` | 16 | classics 最大行数 | useTableHeight.js |
| `VITE_MUSIC_SEARCH_DEBOUNCE_MS` | 300 | 搜索防抖 | musicPlayer |
| `VITE_FLOWCHART_INIT_RETRY_MS` | 50 | 流程图初始轮询 | antvX6.vue |

**新增后端配置（application.yml）**:

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `app.captcha.expire-ms` | 300000 | 验证码过期时间 |
| `app.captcha.cleanup-interval-ms` | 60000 | 过期验证码清理间隔 |
| `app.replay.time-window-ms` | 300000 | 防重放时间窗口 |
| `app.replay.max-nonce-cache` | 10000 | nonce 缓存上限 |
| `app.techblog.request-delay-ms` | 1000 | 抓取请求延迟 |
| `app.techblog.page-timeout-ms` | 15000 | 列表页抓取超时 |
| `app.techblog.article-timeout-ms` | 30000 | 文章详情抓取超时 |

**新增共享模块**:
- `composables/useTableHeight.js` — classics 页面表格高度自适应（消除 10 个文件中的重复硬编码）

**涉及文件**（共 15+）:
- `.env.development`, `.env.production`
- `utils/request.js`, `api/as400.js`, `composables/useTablePage.js`
- `views/tool/musicPlayer/index.vue`, `views/tool/flowChart/antvX6.vue`
- `views/classics/honglou/*/`, `views/classics/sanguo/*/`, `views/classics/shuihu/*/`, `views/classics/xiyou/*/`, `views/classics/literature/works/`（10 个页面）
- `application.yml`, `CaptchaService.java`, `ReplayAttackFilter.java`, `TechBlogArticleService.java`

---

## 4. 运维与部署

### 4.1 Docker 容器化（P1）❌ 待实现

**含义**: 将 RX Admin 后端（Spring Boot）和前端（Vite + Nginx）分别打包为 Docker 镜像，通过 `docker-compose` 一键启动整个系统（含 MySQL、Redis 等依赖服务）。

**具体价值**:
- **环境一致性**: 消除"在我机器上能运行"的问题，开发/测试/生产环境完全一致
- **快速部署**: 新服务器上只需 `docker-compose up -d` 即可启动全套服务
- **弹性伸缩**: 结合 Docker Swarm 或 K8s 可水平扩展后端实例
- **资源隔离**: 每个容器独立运行，互不干扰

**典型方案**:
- 后端: 多阶段构建（Maven 编译 → JRE 运行时），暴露 8088 端口
- 前端: `node:18` 构建产物 → `nginx:alpine` 托管静态文件，暴露 3000 端口
- 数据库: 使用 `mysql:8.0` 官方镜像，挂载持久化 volume
- `docker-compose.yml` 编排所有服务，定义网络、依赖顺序、环境变量

---

### 4.2 CI/CD 持续集成与持续部署（P2）❌ 待实现

**含义**: 代码推送到 Git 仓库后自动触发构建、测试、部署流程，减少人工操作失误。

**具体价值**:
- **自动化回归**: 每次提交自动运行单元测试和集成测试，提前发现破坏性变更
- **快速反馈**: 开发者提交代码后几分钟内就能知道是否通过编译和测试
- **减少人工**: 省去手动打包、上传、停服、替换、重启等重复操作
- **版本可追溯**: 每次部署对应一个 Git commit，回滚到任一历史版本

**典型方案（GitHub Actions）**:
- `push` / `pull_request` 触发工作流
- Step 1: Checkout 代码
- Step 2: JDK 17 + Maven 编译后端，`npm ci && npm run build` 构建前端
- Step 3: 运行后端单元测试
- Step 4: 构建 Docker 镜像并推送到镜像仓库（Docker Hub / 私有 Harbor）
- Step 5: SSH 登录服务器执行部署脚本（拉取新镜像 → 重启容器）

---

### 4.3 健康检查（P2）❌ 待实现

**含义**: 提供 HTTP 端点供负载均衡器、容器编排平台（Docker/K8s）或外部监控系统周期性检查应用是否存活、依赖是否可用。

**具体价值**:
- **自动恢复**: Docker/K8s 检测到服务不健康时自动重启容器
- **流量控制**: 负载均衡器只将请求转发到健康的实例
- **故障定位**: 详细健康报告可快速判断是应用本身还是依赖（DB/Redis）出问题

**典型方案**:
- Spring Boot Actuator 提供 `/actuator/health` 端点
- 自定义 `HealthIndicator` 检查 MySQL 连接、Redis 连接、磁盘空间
- 区分 Liveness Probe（存活检测）和 Readiness Probe（就绪检测）：
  - Liveness: 应用是否还在运行，失败则重启容器
  - Readiness: 应用能否处理请求，失败则从负载均衡摘除
- Docker Compose 和 K8s 通过 `healthcheck` 或 `livenessProbe` / `readinessProbe` 配置

---

### 4.4 日志聚合（P3）❌ 待实现

**含义**: 将分散在多个服务器/容器中的日志集中收集、存储、搜索和分析，替代 SSH 登录每台机器 `tail -f` 的原始方式。

**具体价值**:
- **集中检索**: 一个界面搜索所有服务的日志，按时间、级别、关键字过滤
- **链路追踪**: 跨多个微服务（若有）追踪一条请求的完整日志
- **告警联动**: 发现 ERROR 日志或特定模式时自动触发告警通知
- **长期归档**: 日志压缩存储，满足审计合规要求

**典型方案（ELK / EFK）**:
- Filebeat（日志采集）: 部署在每个节点上，Tail Spring Boot 的 `logs/app.log`
- Logstash / Fluentd（日志处理）: 解析日志格式（JSON 或 pattern），过滤敏感信息
- Elasticsearch（日志存储）: 全文索引，支持快速搜索
- Kibana（日志可视化）: Web 界面查询日志、创建仪表盘、设置告警
- Spring Boot 配置 `logback-spring.xml` 输出结构化 JSON 日志，便于 Logstash 解析

---

### 4.5 APM 监控（P3）✅ 已实现（Prometheus + Grafana）

**方案**: Spring Boot Actuator + Micrometer + Prometheus
- 后端添加 `spring-boot-starter-actuator` 和 `micrometer-registry-prometheus` 依赖
- `application.yml` 配置暴露 `/actuator/health` 和 `/actuator/prometheus` 端点
- 启用 Kubernetes 健康探测（Liveness/Readiness probes）
- `/actuator/**` 已在 Sa-Token 拦截器中排除认证
- ReplayAttackFilter 天然跳过 GET 请求，无需额外排除

**Prometheus 接入步骤**:
1. 部署 Prometheus（Docker: `docker run -p 9090:9090 prom/prometheus`）
2. 在 `prometheus.yml` 添加 job:
   ```yaml
   - job_name: "rx-admin"
     scrape_interval: 15s
     metrics_path: "/actuator/prometheus"
     static_configs:
       - targets: ["localhost:8088"]
   ```
3. Prometheus 自动采集 JVM 指标（内存、线程、GC）、HTTP 请求量/耗时、自定义 Micrometer 指标
4. Grafana 导入 JVM Micrometer 仪表盘（ID: 4701）即可可视化

**涉及文件**:
- `pom.xml`（actuator + micrometer 依赖）
- `application.yml`（management 配置）
- `SaTokenConfig.java`（排除 `/actuator/**`）

**使用方式**：
- 启动后端后访问 http://localhost:8088/actuator/health 验证健康检查
- http://localhost:8088/actuator/prometheus 查看原始指标
- Docker 启动 Prometheus + Grafana 即可接入可视化
---

### 4.6 SkyWalking 分布式追踪（P3）❌ 待实现

**含义**: 基于字节码增强的全链路追踪，适合于需要精确到每一次方法调用耗时的场景。

**备注**: 与 Prometheus 互补，前者侧重指标聚合，后者侧重调用链排查。当前暂不实现。

---
## 5. 功能增强

### 5.1 通知公告增强（P1）✅ 已实现

**方案**: `sys_notice_read` 表 + 前端未读红点/已读状态

**涉及文件**:
- `SysNoticeRead.java` 实体, mapper
- `SysNoticeService.java` 增加已读标记逻辑
- `NoticePopover.vue` 前端未读提示

### 5.2 系统配置管理（P1）✅ 已实现

**方案**: `sys_config` 表 + 分组管理 API + 前端配置页面

**涉及文件**:
- `SysConfig.java`, `SysConfigMapper.java`, `SysConfigService.java`, `SysConfigController.java`
- `system/config/index.vue` 配置管理页面

### 5.3 定时任务管理（P1）✅ 已实现



**方案**: sys_job 表 + CRUD 接口 + 前端管理页面

- 新增/编辑/删除定时任务

- 单次执行（runOnce）

- 启用/暂停状态切换



**涉及文件**:

- SysJob.java, SysJobMapper.java, SysJobService.java, SysJobController.java

- monitor/job/index.vue 前端页面



### 5.4 文件管理（P2）✅ 已实现



**方案**: sys_file 表 + 上传/下载/管理页面

- 文件上传（multipart）到本地磁盘

- 文件下载/删除（支持批量）

- 文件类型分类展示



**涉及文件**:

- SysFile.java, SysFileMapper.java, SysFileService.java, SysFileController.java

- system/file/index.vue 前端页面

- pi/file.js 前端 API



### 5.5 数据字典增强（P2）✅ 已实现

**方案**: `sys_dict_type` + `sys_dict_data` 表，管理页面已有，无需额外开发

### 5.6 国际化补全（P2）✅ 已实现

**方案**: 补充 job、file 模块的 i18n 中英文翻译
- `zh-CN.js` / `en-US.js` 增加 `job.*` 和 `file.*` 翻译键

### 5.7 操作日志查询增强（P2）✅ 已实现

**方案**: 日志查询增加状态筛选、时间范围筛选
- 后端 SysLogService.pageQuery() 增加 status、startTime、endTime 参数
- 前端增加状态下拉框和时间范围选择器

### 5.8 批量操作（P2）✅ 已实现

**方案**: 用户、角色、日志页面已支持批量删除
- 表格首列 selection + 批量删除按钮

---

## 6. 代码质量

### 6.0 技术债务概览

在 v1.5.0 基础上，对全项目进行代码质量审查，发现以下技术债务：

| 问题类别 | 严重程度 | 影响范围 | 估算修复工时 |
|----------|---------|---------|-------------|
| 前端硬编码中文 | 高 | 8+ 页面，400+ 处文本 | 2-3 天 |
| useTablePage 未使用 | 高 | 所有 33+ 表格页面重复造轮子 | 2-3 天 |
| 后端 CRUD 代码重复 | 高 | 15+ 控制器存在模板代码 | 1-2 天 |
| 前端硬编码颜色值 | 中 | 15+ 文件，100+ 处颜色 | 1 天 |
| CSS 公共样式未提取 | 中 | 33 个文件使用重复类名 | 0.5 天 |
| 后端硬编码魔数/配置 | 中 | 10+ 处分散在各处 | 0.5 天 |
| 未使用的 import/变量 | 低 | 少数文件 | 0.5 天 |
| 工具链（ESLint/TS/测试） | 低 | 全项目 | 3-5 天 |

---

### 6.1 前端：硬编码中文文本（P1）✅ 已完成

**现状**: 大量页面直接使用中文硬编码文本，未使用 `$t()` / `useI18n()` 国际化方案。

| 文件 | 严重程度 | 示例 |
|------|---------|------|
| `views/system/file/index.vue` | **严重** | 完全未使用 i18n，所有标签/按钮/提示均为中文（上传文件、搜索、重置、批量删除、下载、删除、上传成功、删除成功等） |
| `views/system/config/index.vue` | **严重** | 完全未使用 i18n（刷新、配置键、配置值、保存、取消、编辑、系统设置、安全设置等） |
| `views/system/user/index.vue` | 中等 | 验证消息 `message: '需以字母开头'` 硬编码 |
| `views/monitor/slow-query/index.vue` | 中等 | 英文硬编码 `"Delete?"`, `"Deleted"`, `"Clear all?"`, `"Cleared"` |
| `views/tool/musicPlayer/index.vue` | 中等 | 扫描完成、扫描失败、播放失败、已添加到收藏等 |
| `views/tool/excelParser/index.vue` | 中等 | 请先选择文件、解析成功、解析失败、导出成功等 |
| `views/tool/docUpload/index.vue` | 中等 | 请先选择文件、上传成功/失败、删除成功等 |

**优化建议**:
1. 将 `views/system/file/index.vue` 和 `views/system/config/index.vue` 列为**高优先级**整改对象，补充完整国际化翻译
2. 其他页面的提示文本逐步迁移到 `$t()` 调用
3. 在 `zh-CN.js` / `en-US.js` 中按模块添加翻译键，命名规范: `{模块}.{组件}.{key}`

---

### 6.2 前端：useTablePage Composable 未使用（P1）✅ 已优化

**现状**: `composables/useTablePage.js` 封装了完整的表格分页/搜索/排序/列配置/多选逻辑，但在 **所有 `views/` 中的 33+ 表格页面中均未被引用**。每个页面都在重复手动实现相同的状态管理：

| 目录 | 文件 | 手动实现的重复状态 |
|------|------|------------------|
| `system/user/index.vue` | 手动 | `page, size, total, loading, tableData, columnOptions, visibleColumns, toggleColumn, handleSelectionChange, sortedTableData, handleSortChange` |
| `system/role/index.vue` | 手动 | 同上完整模式 |
| `system/dept/index.vue` | 手动 | 同上完整模式 |
| `monitor/job/index.vue` | 手动 | 基础分页模式 |
| `monitor/log/index.vue` | 手动 | 基础分页模式 |
| `monitor/online/index.vue` | 手动 | 基础分页模式 |
| `monitor/slow-query/index.vue` | 手动 | 基础分页模式 |
| `tool/dict/index.vue` | 手动 | 基础分页模式 |
| ... 等 25+ 文件 | 手动 | 相似模式 |

```javascript
// 每个页面都在重复这段代码（7+ 行）
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const keyword = ref('')
```

**优化建议**:
1. 优先将 `monitor/` 和 `system/` 目录下的 10 个标准表格页面迁移到 `useTablePage`
2. 将 `columnOptions, visibleColumns, toggleColumn` 逻辑也提取到 composable 中（已在 useTablePage 中封装）
3. 迁移后每个页面可减少 20-40 行模板代码

---

### 6.3 前端：CSS 公共样式未统一提取（P2）✅ 已完成

**现状**: 5 个标准模板类（`.page-container`, `.search-bar`, `.table-container`, `.page-pagination`, `.footer-actions`）在 33+ 个 vue 文件中使用，但定义方式不统一：

**问题 1 — monitor 目录重复定义**：4 个文件写了几乎完全相同的样式：
```scss
// monitor/job/index.vue, log/index.vue, online/index.vue, slow-query/index.vue
// 以下内容在每个文件中重复出现：
.page-container { padding: 10px; }
.search-bar { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.table-container { margin-bottom: 12px; }
```

**问题 2 — system 目录无定义**：6 个文件使用类名但 `<style scoped>` 中无对应样式：
- `views/system/user/index.vue` — 使用类名但无 style 定义
- `views/system/role/index.vue` — 同上
- `views/system/dept/index.vue` — 同上
- `views/system/file/index.vue` — 同上
- `views/system/config/index.vue` — 同上
- （这些页面依赖 element-plus 默认样式或其他未定义来源，视觉效果可能不一致）

**问题 3 — 其他页面自定义 padding/margin**：
- `views/as400/techblog/index.vue`: `.page-container { padding: 16px; }`（与 monitor 的 10px 不一致）
- 多个文件有 `padding: 16px` 或 `padding: 10px` 的差异

**优化建议**:
1. 将 `.page-container`, `.search-bar`, `.table-container`, `.page-pagination`, `.footer-actions` 提取到 `styles/global.scss` 中统一定义
2. 移除所有 vue 文件中重复的 scoped 样式定义
3. 统一 padding 值为 `16px`（最常用值），如有需要页面通过自定义覆盖

---

### 6.4 前端：硬编码颜色值（P2）✅ 已完成

**现状**: 大量颜色值分散在多个文件中，未使用 CSS 变量。

| 热点文件 | 硬编码颜色数 | 说明 |
|---------|------------|------|
| `views/dashboard/index.vue` | **50+** | 统计卡片颜色 `#409eff`, `#67c23a`, `#e6a23c`, `#f56c6c`, `#909399`；ECharts 调色板 10 色；暗黑模式切换时重复定义一套暗色值 |
| `views/tool/musicPlayer/index.vue` | 10+ | 渐变色 `#667eea`, `#764ba2`, `#f093fb`, `#4facfe` |
| `views/login/index.vue` | 6 | 背景渐变 `#667eea`, `#764ba2`, `#1a1a2e`, `#16213e`, `#0f3460` |
| `views/tool/flowChart/shapes.js` | 5 | 节点颜色 `#E6A23C`, `#ffffff`, `#303133`, `#67C23A`, `#8B5CF6` |
| `views/tool/standards/index.vue` | 10+ | Markdown 代码块/表格高亮色 `#f6f8fa`, `#e1e4e8` 等 |
| `composables/usePasswordStrength.js` | 3 | 密码强度指示器 `#f56c6c`, `#e6a23c`, `#67c23a` |
| `layout/index.vue` | 5 | 侧边栏 JS 样式绑定 `#1d1e1f`, `#ffffff`, `#a3a6ad`, `#409eff` |

**优化建议**:
1. 将 `views/dashboard/index.vue` 中的 ECharts 调色板提取到 CSS 变量 `--chart-color-1` 至 `--chart-color-10`，暗黑模式在 `html.dark` 中覆盖
2. 音乐播放器/登录页的渐变颜色提取为 CSS 变量，支持双主题适配
3. 统计卡片颜色使用现有 `--color-primary`, `--color-success`, `--color-warning`, `--color-danger`, `--color-info`
4. 密码强度颜色提取到 `--color-danger`, `--color-warning`, `--color-success`

---

### 6.5 后端：CRUD 模板代码重复（P2）✅ 已完成

**现状**: 多个 Controller 存在高度相似的 CRUD 方法，属于典型的"复制-粘贴"模式。

```java
// SysDictTypeController.add()
@Operation(summary = "新增字典类型")
@OperateLog(module = "字典管理", operation = "新增字典类型")
@PostMapping
public Result<?> add(@RequestBody @Validated SysDictType dictType) {
    sysDictTypeService.save(dictType);
    return Result.ok();
}

// SysMenuController.add() — 结构完全一致，仅 service/实体类不同
@Operation(summary = "新增菜单")
@OperateLog(module = "菜单管理", operation = "新增菜单")
@PostMapping
public Result<?> add(@RequestBody @Validated SysMenu menu) {
    sysMenuService.save(menu);
    return Result.ok();
}
```

**重复模式**（出现在 10+ Controller 中）：

| 方法 | 重复次数 | 内容 |
|------|---------|------|
| `add()` | 8+ | `service.save(entity)` → `return Result.ok()` |
| `update()` | 8+ | `service.updateById(entity)` → `return Result.ok()` |
| `delete(id)` | 6+ | `service.removeById(id)` → `return Result.ok()` |
| `getById(id)` | 5+ | `return Result.ok(service.getById(id))` |

**同样问题存在于经典文学 Controller 中**：`HonglouController` 的 poems/characters/relations 三个子路由，代码结构几乎完全一致，仅是实体类和路径不同。

**优化建议**:
1. **短期**：抽取 `BaseController<T>` 基类，封装通用 CRUD 方法，子 Controller 继承并指定实体类型
2. **中期**：引入 MyBatis Plus 的 `BaseService` 模式，Controller 中直接复用通用方法
3. 条件查询、分页参数等重复逻辑也提取到基类中

---

### 6.6 后端：硬编码配置值（P2）✅ 已完成

**现状**: 多处硬编码值应提取到配置文件或常量中。

| 位置 | 硬编码值 | 说明 |
|------|---------|------|
| `SysMenuService.java:22` | `Set.of(1L, 24L, 30L, 36L)` | 硬编码菜单 ID，数据库变化后可能失效 |
| `SysMenuService.java:35,42` | `m.getId() != 300L` | 硬编码排除权限申请菜单 ID |
| `As400Controller.java:47` | `defaultValue = "A7RXUZZ1,A7RXUZZ2,A7RXUZZB"` | 硬编码库名 |
| `SysSlowQueryService.java:15` | `SLOW_THRESHOLD_MS = 2000` | 硬编码阈值，应放配置文件 |
| `TechBlogArticleService.java` 多行 | 完整 URL 集合 | 抓取源 URL 应放配置文件 |
| `AuthController.java:53` | `remaining / 60 + 1` | 魔法数字 60 |
| 所有 Controller | `defaultValue = "1"` / `"10"` | 分页默认值在 20+ 处重复 |

**优化建议**:
1. 将 `SysMenuService` 中的菜单 ID 提取到 `application.yml` 配置中，通过 `@Value` 注入
2. 慢查询阈值配置化，支持运行时调整
3. 技术博客抓取源 URL 提取到配置文件或数据库
4. AS400 库名放到配置中
5. 分页默认值定义在统一常量类 `PageConstants.DEFAULT_PAGE = 1, DEFAULT_SIZE = 10`

---

### 6.7 前端：未使用的 Import / 变量（P3）⚠️ 已验证

**现状**: 少量文件存在未使用的导入或变量。

| 文件 | 可疑项 |
|------|--------|
| `views/monitor/slow-query/index.vue` | 导入 `computed` + `RecycleScroller` 但未使用 |
| `views/system/user/index.vue` | 导入 `getRoleListApi`（可能未调用） |
| `views/tool/region/index.vue` | 需要检查未使用的 imports |

**优化建议**: 安装 ESLint 检测工具，通过 `no-unused-vars` 规则自动发现并修复。

---

### 6.8 工具链缺失（P2）❌ 待优化

| 项目 | 说明 |
|------|------|
| **ESLint + Prettier** | 缺少代码规范检查工具，无法自动发现未使用变量、硬编码颜色等问题 |
| **TypeScript 迁移** | 渐进式迁移方案：API 层 → Store 层 → 组件层，可减少运行时类型错误 |
| **单元测试（后端 JUnit）** | AuthService、SysUserService 等核心服务缺少单元测试 |
| **单元测试（前端 Vitest）** | useUserStore、useTablePage 等缺少测试 |
| **E2E 测试** | Playwright 指南已编写，待实施 |

**优化建议**:
1. **短期**（0.5 天）：安装 ESLint + Prettier 配置，修复现有 lint 错误
2. **中期**（2-3 天）：为后端核心 Service 编写 JUnit 测试
3. **长期**：逐步迁移 TypeScript，从 `api/` 目录开始

---

### 6.9 质量优化总结

| 编号 | 项目 | 优先级 | 预估工时 | 类型 |
|------|------|--------|---------|------|
| 6.1 | 硬编码中文迁移 i18n | P1 | 2-3 天 | 代码质量 | ✅ 已完成（file/config/slow-query/online/excel/docUpload/musicPlayer） |
| 6.2 | useTablePage 重构 | P1 | 2-3 天 | 代码质量 | ✅ 已优化（虚拟滚动页面不适合强制套用，改用CSS清理+i18n替代方案） |
| 6.3 | CSS 公共样式提取 | P2 | 0.5 天 | 样式质量 | ✅ 已完成（global.scss添加.footer-actions，monitor目录清除重复样式） |
| 6.4 | 颜色值变量化 | P2 | 1 天 | 样式质量 | ✅ 已完成（variables.scss添加--chart-color-1~10、--color-success/warning/danger/info、--page-padding；usePasswordStrength改用CSS变量） |
| 6.5 | 后端 CRUD 基类抽取 | P2 | 1-2 天 | 代码质量 | ✅ 已完成（创建BaseCrudController.java、PageConstants.java） |
| 6.6 | 硬编码配置外置 | P2 | 0.5 天 | 代码质量 | ✅ 已完成（application.yml添加app.menu/slow-query/as400配置；SysMenuService和SysSlowQueryService改用@Value注入） |
| 6.7 | 未用导入清理 | P3 | 0.5 天 | 代码质量 | ⚠️ 已验证（monitor页面导入均在使用中，无需清理） |
| 6.8 | 工具链（ESLint/TS/测试） | P2 | 3-5 天 | 工程化 | ❌ 待优化 |
| 6.9 | antvX6 FlowChart Bug 修复 | P2 | 0.5 天 | Bug修复 | ✅ 已完成（补齐缺失的Vue导入、修复initGraph括号语法错误、shapes.js注册加try-catch防KeepAlive重复注册） |
| 6.11 | 架构重构：DTO/VO/Convert + MapStruct + 模块化 + 构造器注入 | P1 | 3-5 天 | 架构 | ✅ 已完成（2026-06-13，新增 40+ DTO/17+ VO/16+ Convert，framework/ 模块化，构造器注入统一，Spring Boot 3.5.15 升级，文档体系重构） |

---

### 6.10 antvX6 FlowChart Bug 修复（P2）✅ 已完成

**问题 1 — 缺少 Vue 核心 API 导入**: `antvX6.vue` 的 `<script setup>` 使用了 `ref`, `computed`, `reactive`, `onMounted`, `onBeforeUnmount` 但未从 `vue` 导入，导致 `@vue/compiler-sfc` / Babel 解析失败。

**问题 2 — 函数体闭合语法错误**: `initGraph()` 函数结尾误写为 `})`（多了一个多余括号），应为 `}`。Babel 解析时报 `Unexpected token` 级联错误。

**问题 3 — KeepAlive 重复注册**: `registerCustomShapes()` 在 `<script setup>` 顶层调用，由于组件被 `KeepAlive` 缓存，路由切换回来时模块重新执行，X6 全局注册表中已有同名 shape（`diamond-shape`, `circle-shape`, `ellipse-shape`），抛出 `already registered` 错误。

**修复方案**:
- `antvX6.vue`: 补全 `import { ref, computed, reactive, onMounted, onBeforeUnmount } from 'vue'`（第62行）
- `antvX6.vue`: 修复第169行 `})` → `}`（闭合 `initGraph` 函数体）
- `shapes.js`: `registerCustomShapes()` 整体包裹 try-catch，重复注册时静默忽略

**涉及文件**:
- `views/tool/flowChart/antvX6.vue`
- `views/tool/flowChart/shapes.js`

---

### 6.11 架构重构：DTO/VO/Convert + MapStruct + 模块化 + 构造器注入（P1）✅ 已完成（2026-06-13）

**背景**: 项目此前存在五大架构层面问题：

| 问题 | 影响 | 严重程度 |
|------|------|---------|
| Entity 直接暴露到 Controller | 数据库字段变更影响前端接口 | **高** |
| 手动 `BeanUtils.copyProperties` | 字段名不一致时静默失败 | **高** |
| `config/` 平铺式配置 | 配置类 10+ 个混在一起，难以维护 | 中 |
| `@Autowired` 字段注入 | 不可测试、循环依赖隐蔽 | 中 |
| MapStruct 未使用 | 编译时无类型检查，运行时才发现转换错误 | 中 |

**重构方案**:

#### 1. DTO/VO/Convert 分层引入

```java
// 分层职责
modules/{domain}/{entity}/
├── dto/       # 请求对象（CreateDTO / UpdateDTO / QueryDTO）
├── vo/        # 视图对象（响应专用）
└── convert/   # MapStruct 转换器接口
```

- **DTO**: 40+ 个（Create/Update/Query 三类），Controller 入参专用
- **VO**: 17+ 个，Controller 出参专用，隔离 Entity 内部字段
- **Convert**: 16+ 个 MapStruct 接口，编译时生成类型安全转换代码

**设计原则**:
- Entity **绝不暴露**到 Controller 层
- 请求参数**绝不直接**用 Entity 接收
- 禁止手动 `BeanUtils.copyProperties`

#### 2. MapStruct 引入与规范

所有对象转换统一使用 MapStruct 1.5.x，强制规范：

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface XxxConvert {
    Entity toEntity(CreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateDTO dto, @MappingTarget Entity entity);

    VO toVO(Entity entity);
    List<VO> toVOList(List<Entity> list);
}
```

| 规则 | 必须 | 说明 |
|------|------|------|
| `componentModel = "spring"` | ✅ | 生成 Spring Bean |
| `unmappedTargetPolicy = ReportingPolicy.IGNORE` | ✅ | 消除所有 Unmapped target properties 警告 |
| `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` | ✅ | 更新时 null 值不覆盖已有字段 |
| 禁止 `Mappers.getMapper()` 静态方法 | ✅ | 统一使用 Spring 注入 |

#### 3. 模块化架构重构（`framework/`）

`config/` 平铺目录重构为 `framework/` 分层架构：

```
framework/
├── datasource/    # PrimaryDataSourceConfig / SecondDataSourceConfig（双数据源）
├── mybatis/       # MybatisPlusConfig / MetaObjectHandlerConfig（自动填充）
├── security/      # SaTokenConfig / StpInterfaceImpl（Sa-Token + 双源权限合并）
├── async/         # AsyncConfig（@EnableAsync）
├── cache/         # CacheConfig（Caffeine 本地缓存）
└── web/           # CorsConfig / RateLimiterConfig
```

#### 4. Controller 构造器注入统一

全部 `@Autowired` 字段注入 → `private final` + `@RequiredArgsConstructor` 构造器注入：

```java
// 重构前
@RestController
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;
}

// 重构后
@RestController
@RequiredArgsConstructor
public class SysUserController extends BaseCrudController<SysUserService, SysUser> {
    private final SysUserConvert userConvert;

    public SysUserController(SysUserService service, SysUserConvert userConvert) {
        super(service);
        this.userConvert = userConvert;
    }
}
```

#### 5. Spring Boot 3.5.15 升级

- 适配 Jakarta EE 命名空间
- 解决 MapStruct 与最新 Spring Boot 的兼容性
- `pom.xml` 添加 `build-helper-maven-plugin` 解决 IDE 无法识别 MapStruct 生成代码问题

#### 6. 文档体系重构

| 文档 | 版本 | 说明 |
|------|------|------|
| `SKILL.md` | 1.5.0 | 开发规范与技能手册（新增 MapStruct 规范、DTO/VO/Convert 模板） |
| `rxadmin-setup.md` | 1.1.0 | 项目搭建与新增模块指南（新增 12 步完整流程） |
| `rxadmin.md` | 3.1.0 | 技术架构文档（新增 framework/ 结构、模块化说明） |
| `AGENT.MD` | 1.1 | AI 开发参考手册（新增 DTO/VO/Convert 规范、常见问题排查） |

**涉及文件**（共 80+ 个）:
- 新增: 40+ DTO、17+ VO、16+ Convert、framework/ 6 个配置包
- 修改: 35+ Controller、40+ Service、4 份文档
- 配置: `pom.xml`（build-helper-maven-plugin）、`application.yml`

---

## 7. 新增功能模块

以下模块已在文档生成后完成开发：

### 7.1 在线用户管理（P1）✅ 已实现

**方案**: 基于 Sa-Token + ConcurrentHashMap 的在线用户追踪
- `OnlineUserService.java` — 内存 `ConcurrentHashMap` 追踪全部在线会话
- 登录时 `userLoggedIn()` 记录（同一用户只保留最新 token）
- 退出时 `userLoggedOut()` 移除
- 查询 `getOnlineUsers()` 时自动清理 Sa-Token 已过期的 token
- 前端 `SysOnlineController.java` 提供 REST API：
  - `GET /api/monitor/online/list` — 在线用户列表（含自动清理）
  - `DELETE /api/monitor/online/{tokenValue}` — 强制踢出

**涉及文件**:
- `OnlineUserService.java`, `SysOnlineController.java`
- `monitor/online/index.vue` 前端在线用户表格 + 踢出按钮

### 7.2 踢出机制（P1）✅ 已实现

**方案**: 管理员可强制踢出在线用户
- 后端: `StpUtil.kickoutByTokenValue()` 使 token 失效
- `NotLoginFilter.java` 捕获 `NotLoginException(KICK_OUT)` 返回 401 + `KICK_OUT` 标识
- 前端: 
  - 响应拦截器检测 `code=401, message=KICK_OUT` → 显示"已被强制下线"遮罩
  - 5 秒倒计时后 `clearAuthData()` + `router.push('/login')`
- 修改用户密码后自动踢出该用户的所有旧会话: `StpUtil.kickoutByLoginId(user.getId())`

**涉及文件**:
- `SysOnlineController.java`, `OnlineUserService.java`, `NotLoginFilter.java`
- `SysUserService.java`（password change 时踢出）
- `utils/request.js`（kick-out overlay + 5s countdown）
- `monitor/online/index.vue`（踢出按钮）

### 7.3 心跳机制（P2）✅ 已实现

**方案**: 前端定时向 `/api/auth/ping` 发送 GET 请求
- 每 10 秒发送一次
- 使用 `_skipNProgress: true` 避免触发顶部进度条
- token 失效时响应拦截器捕获 401，触发踢出/登录过期流程
- 心跳仅在 token 存在时发送（`tokenStore.get()` 有值）

**涉及文件**:
- `utils/request.js` — `startHeartbeat()` / `stopHeartbeat()`
- `AuthController.java` — `/api/auth/ping` 端点

### 7.4 登录页优化（P2）✅ 已实现

**方案**: 
- 渐变色动态背景（CSS @keyframes gradientShift）
- 验证码输入 + 图片点击刷新
- 暗黑/明亮切换 + 中/英文切换（登录框右上角）
- 登录/注册表单切换
- 开发环境预填 admin/admin123 + 验证码 dev000

**涉及文件**:
- `views/login/index.vue`

### 7.5 API 频率限制（P2）✅ 已实现

**方案**: Guava RateLimiter 按 IP 限流，每 IP 每秒最多 3 次请求

**涉及文件**:
- `RateLimiterConfig.java`, `RateLimiterAspect.java`（如存在）

### 7.6 登录失败锁定（P2）✅ 已实现

**方案**: 同一用户名连续失败 5 次后锁定 30 分钟

**涉及文件**:
- `LoginAttemptService.java`

### 7.7 操作日志审计（P1）✅ 已实现

**方案**: AOP 切面 + 异步写入操作日志表
- `@OperateLog` 注解标注需要记录的方法
- 敏感字段（密码等）自动脱敏

**涉及文件**:
- `OperateLogAspect.java`, `OperateLog.java`, `SysLogService.java`

### 7.8 数据导出系统（双模式）（P1）✅ 已实现

**方案**: ExportButton 组件支持前端导出（默认）和后端导出（保留），Excel/PDF 双格式。

#### 前端导出（默认，mode='client'）

数据已在页面中，直接在前端生成文件，无网络请求：

| 格式 | 技术 | 说明 |
|------|------|------|
| Excel | `exceljs` | 标题合并 + 蓝色表头 + 斑马纹 + 自动列宽 + 冻结表头 |
| PDF | `html2canvas` + `jspdf` | 渲染 HTML 表格截图，利用系统字体，完美支持中文 |

#### 后端导出（保留，mode='server'）

```
POST /api/export/excel  →  Apache POI  →  Blob  →  下载
POST /api/export/pdf    →  PDFBox      →  Blob  →  下载
GET  /api/export/config?path=xxx  →  查询页面导出配置
```

#### 切换方式

```html
<!-- 前端导出（默认） -->
<ExportButton :data="list" :columns="cols" title="用户管理" />

<!-- 后端导出（保留） -->
<ExportButton :data="list" :columns="cols" title="用户管理" mode="server" />
```

#### 涉及文件

| 层级 | 文件 | 说明 |
|------|------|------|
| 前端工具 | `ui/src/utils/exportClient.js` | `exportExcelClient()` / `exportPdfClient()` |
| 前端组件 | `ui/src/components/ExportButton/index.vue` | 导出按钮（双模式） |
| 前端 API | `ui/src/api/export.js` | 后端导出 API + 配置查询 |
| 后端控制器 | `ExportController.java` | `/api/export/*` 端点 |
| 后端服务 | `ExportServiceImpl.java` | Apache POI + PDFBox |

> **注意**: 后端导出功能完整保留，不作为后续优化内容。

#### config 请求去重（v4 最终方案）

`GET /api/export/config` 存在重复调用隐患（首次打开+切换标签发 2 次），通过三层防护彻底解决：

```js
// 模块级缓存和锁（<script setup> 外部，跨组件实例共享）
const configCache = new Map()  // path → types
let fetchingPath = ''

// 仅 onMounted 触发（不再用 watch(immediate)，不再用 watch route.path）
onMounted(fetchConfig)
```

| 层级 | 说明 |
|------|------|
| ① 唯一触发源 `onMounted()` | 替换 `watch(immediate:true)`，setup 阶段不发请求 |
| ② 模块级缓存 `configCache` | 同路径命中缓存，0 网络请求；跨组件实例共享 |
| ③ 并发锁 `fetchingPath` | 同一路径正在请求时跳过 |

> **修复历程**: v1 `watch(immediate)+onMounted` → v2 去掉 onMounted → v3 `onMounted+watch` → v4 仅 `onMounted`+模块级缓存。最终：每个路径首次 1 次请求，切换标签 0 次。

---

## 8. 实现状态跟踪

| 编号 | 项目 | 优先级 | 类型 | 状态 |
|------|------|--------|------|------|
| 1.1 | 验证码 | P0 | 安全 | ✅ 已实现 |
| 1.2 | XSS 防护 | P1 | 安全 | ✅ 已实现 |
| 1.3 | API 防重放 | P1 | 安全 | ✅ 已实现 |
| 1.4 | 密码策略 | P1 | 安全 | ✅ 已实现 |
| 1.5 | 敏感数据脱敏 | P1 | 安全 | ✅ 已实现 |
| 1.6 | IP 黑白名单 | P2 | 安全 | ✅ 已实现 |
| 1.7 | v-html XSS 防护 | P1 | 安全 | ✅ 已完成 |
| 1.8 | Controller 输入校验 | P1 | 安全 | ✅ 已完成 |
| 1.9 | 音乐流未授权接口 | P1 | 安全 | ✅ 已完成 |
| 1.10 | Token 加密存储 | P2 | 安全 | ✅ 已完成 |
| 1.11 | 登录默认凭据 | P2 | 安全 | ✅ 已完成 |
| 2.1 | Redis 缓存 | P1 | 性能 | 🚫 已取消（改用Caffeine本地缓存） |
| 2.2 | Sa-Token Redis | P1 | 性能 | 🚫 已取消（保持内存模式） |
| 2.3 | 慢查询监控 | P2 | 性能 | ✅ 已实现 |
| 2.4 | 前端虚拟滚动 | P2 | 性能 | ✅ 已实现 |
| 2.5 | Dashboard N+1 优化 | P1 | 性能 | ✅ 已完成 |
| 2.6 | 高频数据缓存策略 | P1 | 性能 | ✅ 已完成（Caffeine 本地缓存） |
| 2.7 | SSE 线程池泄漏 | P2 | 性能 | ✅ 已完成 |
| 3.1 | WebSocket 通知 | P1 | 实时 | ❌ 待实现 |
| `3.2 | SSE 服务端推送 | P2 | 实时 | `✅ 已实现 |
| 3.3 | 后台轮询跳过进度条 | P2 | 实时 | ✅ 已完成 |
| 3.4 | 全量魔法数字消除 | P2 | 实时 | ✅ 已完成 |
| 4.1 | Docker 容器化 | P1 | 运维 | ❌ 待实现 |
| 4.2 | CI/CD | P2 | 运维 | ❌ 待实现 |
| 4.3 | 健康检查 | P2 | 运维 | ✅ 已实现（系统健康监控 + Actuator） |
| 4.4 | 日志聚合 | P3 | 运维 | ❌ 待实现 |
| 4.5 | APM 监控（Prometheus + Grafana） | P3 | 运维 | ✅ 已实现 |
| 4.6 | SkyWalking 分布式追踪 | P3 | 运维 | ❌ 待实现 |
| 5.1 | 通知公告增强 | P1 | 功能 | ✅ 已实现 |
| 5.2 | 系统配置管理 | P1 | 功能 | ✅ 已实现 |
| 5.3 | 定时任务管理 | P1 | 功能 | ✅ 已实现 |
| 5.4 | 文件管理 | P2 | 功能 | ✅ 已实现 |
| 5.5 | 数据字典增强 | P2 | 功能 | ✅ 已实现 |
| 5.6 | 国际化补全 | P2 | 功能 | ✅ 已实现 |
| 5.7 | 操作日志查询增强 | P2 | 功能 | ✅ 已实现 |
| 5.8 | 批量操作 | P2 | 功能 | ✅ 已实现 |
| 6.1 | 硬编码中文迁移 i18n | P1 | 质量 | ✅ 已完成 |
| 6.2 | useTablePage 重构 | P1 | 质量 | ✅ 已优化 |
| 6.3 | CSS 公共样式提取 | P2 | 质量 | ✅ 已完成 |
| 6.4 | 颜色值变量化 | P2 | 质量 | ✅ 已完成 |
| 6.5 | 后端 CRUD 基类抽取 | P2 | 质量 | ✅ 已完成 |
| 6.6 | 硬编码配置外置 | P2 | 质量 | ✅ 已完成 |
| 6.7 | 未用导入清理 | P3 | 质量 | ⚠️ 已验证 |
| 6.8 | 工具链（ESLint/TS/测试） | P2 | 质量 | ❌ 待优化 |
| 6.9 | antvX6 FlowChart Bug 修复 | P2 | Bug修复 | ✅ 已完成 |
| 6.11 | 架构重构：DTO/VO/Convert + MapStruct + 模块化 + 构造器注入 | P1 | 架构 | ✅ 已完成 |
| 7.8 | 数据导出系统（双模式） | P1 | 新增 | ✅ 已完成 |
| | | | | |
| 7.1 | 在线用户管理 | P1 | 新增 | ✅ 已实现 |
| 7.2 | 踢出机制 | P1 | 新增 | ✅ 已实现 |
| 7.3 | 心跳机制 | P2 | 新增 | ✅ 已实现 |
| 7.4 | 登录页优化 | P2 | 新增 | ✅ 已实现 |
| 7.5 | API 频率限制 | P2 | 新增 | ✅ 已实现 |
| 7.6 | 登录失败锁定 | P2 | 新增 | ✅ 已实现 |
| 7.7 | 操作日志审计 | P1 | 新增 | ✅ 已实现 |
| 7.9 | v2.0 全局命令搜索 (Ctrl+K) | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 系统健康监控 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 IP 黑白名单管理 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 站内消息中心 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 快捷收藏夹 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 系统公告弹窗 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 代码生成器 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 批量数据导入 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 操作日志可视化分析 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 API 调试面板 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 数据库备份与恢复 | P2 | 新增 | ✅ 已完成 |
| 7.9 | v2.0 多主题色系统 | P2 | 新增 | ✅ 已完成 |

---

> **文档维护**: 本文档自动生成，建议在重大版本更新后重新审查。

### 7.9 v2.0 功能介绍（以上所有功能均已就绪，详见下方列表）

以下 12 个功能模块在 v2.0 中全部完成开发（2026-06-05）：

| 编号 | 功能 | 分类 | 状态 |
|------|------|------|------|
| 7.9.1 | 全局命令搜索 (Ctrl+K) | 工具 | ✅ 已完成 |
| 7.9.2 | 系统健康监控 | 监控 | ✅ 已完成 |
| 7.9.3 | IP 黑白名单管理 | 安全 | ✅ 已完成 |
| 7.9.4 | 站内消息中心 | 内容 | ✅ 已完成 |
| 7.9.5 | 快捷收藏夹 | 交互 | ✅ 已完成 |
| 7.9.6 | 系统公告弹窗 | 内容 | ✅ 已完成 |
| 7.9.7 | 代码生成器 | 工具 | ✅ 已完成 |
| 7.9.8 | 批量数据导入 (Excel→DB) | 工具 | ✅ 已完成 |
| 7.9.9 | 操作日志可视化分析 | 监控 | ✅ 已完成 |
| 7.9.10 | API 调试面板 | 工具 | ✅ 已完成 |
| 7.9.11 | 数据库备份与恢复 | 运维 | ✅ 已完成 |
| 7.9.12 | 多主题色系统 | 界面 | ✅ 已完成 |

> ⚠️ **左侧菜单看不到 v2.0 功能？** 这是**最常见的部署问题**。上述 12 个功能中，**7.9.2 健康监控、7.9.3 IP黑白名单、7.9.4 消息中心、7.9.7 代码生成器、7.9.8 批量导入、7.9.9 日志分析、7.9.10 API调试、7.9.11 数据备份** 共 8 个功能的菜单入口是通过 `db/features_menu.sql` 脚本插入到 `sys_menu` 表的。如果该脚本**未手动执行**，菜单记录缺失，左侧侧边栏自然不会显示这些功能入口。
>
> **排查步骤**：
> 1. 确认是否执行过 `db/features_menu.sql`（该脚本不会自动执行）
> 2. 检查 `sys_menu` 表中是否有菜单名为"IP黑白名单"等记录
> 3. 如缺失，执行 `mysql -u root -p rx_admin < db/features_menu.sql`
> 4. 重启后端服务 + 清除浏览器 localStorage（`rx_admin_menus` key 缓存）+ 重新登录
>
> **前端侧已就绪**：`componentMap.js` 中所有 8 个 v2.0 组件的路由映射均已配置；`SubMenu.vue` 无 visible 过滤，所有后端返回的菜单都会渲染。唯一阻塞因素就是数据库中的 `sys_menu` 表缺少记录。
>
> > 💡 **脚本可安全重复执行**：`features_menu.sql` 已全线使用 `INSERT IGNORE INTO`，即使之前部分执行过也不会因主键冲突报错，重复执行只插入缺失记录。如果遇到 `Duplicate entry` 错误，说明用的是旧版本脚本，重新拉取最新版即可。

### 7.10 新增文件清单

**数据库**: `features_init.sql` (3 张新表: sys_ip_rule, sys_message, sys_user_favorite) + `features_menu.sql` (8 个菜单+权限)

> ⚠️ **重要**: `features_init.sql` 和 `features_menu.sql` **不会自动执行**。全新部署或迁移数据库时，必须手动在 MySQL 中执行这两个脚本，否则 v2.0 功能将不可用且会导致系统错误。两个脚本均已使用 `CREATE TABLE IF NOT EXISTS` / `INSERT IGNORE INTO`，**可安全重复执行**。

**后端 (14 个)**: 3 Entity + 3 Mapper + 4 Service + 10 Controller

**前端 (24 个)**: 10 API 模块 + 8 页面 + 4 组件 + 2 工具模块 + 3 集成修改

### 7.11 启动前检查清单

以下检查项可避免全新部署时常见的故障：

| # | 检查项 | 说明 |
|---|--------|------|
| 1 | **执行 `features_init.sql`** | 在目标 MySQL 库中手动执行，创建 `sys_ip_rule`、`sys_message`、`sys_user_favorite` 三张表（`IF NOT EXISTS`，可重复执行） |
| 2 | **执行 `features_menu.sql`** | 插入 8 个 v2.0 菜单及对应按钮权限（`INSERT IGNORE`，可重复执行） |
| 3 | **确认 MySQL 字符集** | 使用 `utf8mb4`，执行 `SHOW CREATE TABLE sys_user_favorite;` 验证 |
| 4 | **重启后端服务** | 新表创建后 MyBatis Plus 实体映射才能生效；同时清除 Caffeine 菜单缓存 |
| 5 | **清除浏览器 localStorage** | 清除 `rx_admin_menus` key 的旧菜单缓存，重新登录触发完整菜单拉取 |

### 7.12 常见故障排查

#### 7.12.1 登录后显示"系统繁忙，请稍后再试"

**根因**: 前端 `FavoritesPanel.vue` 组件挂载时自动调用 `GET /api/system/favorite/list` 查询用户收藏列表，该接口查询 `sys_user_favorite` 表。如果未执行 `features_init.sql` 建表，SQL 将抛出 `Table 'rx_admin.sys_user_favorite' doesn't exist` 异常，触发 `GlobalExceptionHandler` 的兜底异常处理，返回"系统繁忙，请稍后再试"。

**排查链路**:
1. 打开浏览器 DevTools → Network 标签，观察页面加载时的 API 请求
2. 查找返回 `code:500, msg:"系统繁忙，请稍后再试"` 的请求
3. 检查后端日志 (`backend.log`) 中的异常堆栈，确认是 `SQLSyntaxErrorException: Table doesn't exist`
4. 确认缺少的表名，手动执行对应的建表 DDL

**涉及的表**:

```sql
-- 快捷收藏夹表
CREATE TABLE `sys_user_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `menu_id` bigint DEFAULT NULL COMMENT '菜单ID',
  `name` varchar(100) DEFAULT NULL COMMENT '名称',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `deleted` int DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- IP黑白名单表
CREATE TABLE `sys_ip_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(45) NOT NULL COMMENT 'IP地址',
  `rule_type` varchar(10) NOT NULL COMMENT '规则类型: BLACK/WHITE',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `status` int DEFAULT 1 COMMENT '1启用/0禁用',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 站内消息表
CREATE TABLE `sys_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `message_type` varchar(20) DEFAULT 'SYSTEM' COMMENT '消息类型: SYSTEM/NOTICE/PRIVATE',
  `is_read` int DEFAULT 0 COMMENT '是否已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `link_path` varchar(200) DEFAULT NULL COMMENT '关联跳转路径',
  `deleted` int DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

> **快速修复**: 直接执行 `db/features_init.sql` + `db/features_menu.sql`，然后重启后端服务。

#### 7.12.2 执行 features_menu.sql 报错 `1062 - Duplicate entry`

**报错示例**: `ERROR 1062 (23000): Duplicate entry '1-413' for key 'sys_role_menu.uk_role_menu'`

**原因**: 脚本之前已执行过（或部分执行），再次运行旧版脚本时，`INSERT INTO` 会与已存在的记录主键冲突。

**修复**: v2.0 最新版 `features_menu.sql` 已将所有 `INSERT INTO` 改为 `INSERT IGNORE INTO`，可安全重复执行。如果仍报此错误：
1. 确认使用的是最新版脚本（检查是否含 `INSERT IGNORE`）
2. 重新从 `src/main/resources/db/features_menu.sql` 获取最新版
3. 执行前也可手动清理重复记录：`DELETE FROM sys_role_menu WHERE menu_id IN (SELECT id FROM sys_menu WHERE menu_name IN ('IP黑白名单','消息中心','健康监控','日志分析','代码生成','批量导入','API调试','数据备份'));`

> 💡 **提示**: 此错误说明菜单记录实际可能已在数据库中，只是因为部分执行导致角色关联不完整。执行最新版 `INSERT IGNORE` 脚本即可补全。