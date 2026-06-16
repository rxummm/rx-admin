# AGENTS.md — RX Admin

Spring Boot 3.5.15 (Java 17) + Vue 3 (Node 18) 后台管理系统。双 MySQL 数据源，Sa-Token 内存模式，无 Redis。

## Quick start

| Command | What |
|---------|------|
| `mvn spring-boot:run -Dspring-boot.run.profiles=local` | Backend (port 8088), SQL logging + local mail |
| `cd ui; npm run dev` | Frontend (port 3000), proxies `/api` → `http://localhost:8088` |
| `mvn compile -q` | Fast compile check (avoids full boot) |
| `cd ui; node tests/e2e.spec.js` | Playwright E2E (requires backend + frontend running) |

Default login: `admin` / `admin123`. API docs: `http://localhost:8088/doc.html` (Knife4j).

## Architecture must-knows

- **`RxAdminApplication.java`** excludes `DataSourceAutoConfiguration` — dual datasource manually configured.
- **Primary datasource** (`rx_admin`): system tables (user/role/menu/dict/config/log). **Secondary** (`rxusysadmin`): literature/classics/region tables. Mappers for secondary must be annotated with `@SecondDB`.
- **Sa-Token memory mode** — no Redis, token state lost on restart. `is-concurrent: false`, `is-share: false`.
- **Caffeine** replaces Redis for all caching. Two named caches: `config` (100 max, 10min TTL) and `menu` (500 max, 1hr TTL). Dashboard uses in-memory volatile fields + SSE push.
- **No MyBatis XML** — all SQL in annotations (`@Select`, `@Update`, etc.).
- **System routes are DB-driven** — `sys_menu` table, not Vue Router static routes. Register new pages: add entry to `ui/src/router/componentMap.js` + insert `sys_menu` row. Router's `resetDynamicRoutes()` tears down and rebuilds on login/logout/permission change.
- **API versioning**: Controllers annotated with `@ApiVersion(1)` get auto-prepended `/api/v1` prefix via `ApiVersionConfiguration.java`. Frontend request.js has `baseURL: '/api/v1'`.
- **Sentry** in frontend (`@sentry/vue`) — no backend Sentry.

## Backend conventions

| Rule | Detail |
|------|--------|
| DI | `@RequiredArgsConstructor` — no `@Autowired` |
| API path | `@ApiVersion(1)` + `@RequestMapping("/sys/user")` → `/api/v1/sys/user` |
| Response | `Result.ok(data)` / `Result.fail(msg)` |
| Pagination | `PageResult.of(IPage)` — never return MP `Page` directly |
| DTO/VO/Convert | Entity never exposed. MapStruct: `unmappedTargetPolicy=IGNORE`, update methods: `@BeanMapping(nullValuePropertyMappingStrategy=IGNORE)` |
| Permission | `@SaCheckPermission("module:entity:op")` on every endpoint |
| Write ops | `@Transactional` + `@OperateLog(module, operation)` |
| Validation | `@Valid` on `@RequestBody` params |
| Secondary DS | Literature/classics/region Mappers use `@SecondDB` annotation |
| Admin role | `StpInterfaceImpl` — `"admin"` role gets all permissions via `selectAllValidPerms()` |
| `app.*` config | Maps to `AppConfig.java` (`@ConfigurationProperties(prefix = "app")`) — menu, cache, security, audio, OCR, etc. |
| Sa-Token excludes | `/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/auth/captcha`, swagger, actuator |

## Frontend conventions

- `<script setup>` + `defineOptions({ name: 'PascalName' })` — name must match `componentMap.js` entry.
- All API endpoints defined centrally in `api/routes.js` — import `API` constant, never hardcode paths.
- Table pages: use `useTablePage(apiModule)` composable.
- `useStorage(STORAGE_KEYS.X)` returns `{ get, set, remove }` — never raw `localStorage`. Token is Base64+XOR obfuscated.
- Axios `baseURL: '/api/v1'` with interceptors: auto-attach auth token, cancel duplicate requests (`_skipCancel` flag), add anti-replay headers (X-Timestamp, X-Nonce). Background/polling requests **must** pass `{ _skipNProgress: true }`.
- `v-html` must use `sanitizeHtml()` from `@/utils/sanitize` (DOMPurify).
- CSS: use CSS variables (`--bg-page`, `--color-primary`, etc.) — no hardcoded colors.
- i18n: `$t('key')` — no hardcoded Chinese strings.
- Formatter: Prettier (`singleQuote`, no `semi`, `printWidth: 120`, `trailingComma: none`).
- ESLint: Vue3 essential + Prettier, `no-console` is `warn` in dev, `error` in production. `no-unused-vars` ignores `_` prefix.
- Build chunks (vite manualChunks): echarts, element-plus, flowchart, editor, export, icons, fonts, markdown, player, vendor.

## Dev quirks & gotchas

- **Dev captcha**: always `dev000` — `CaptchaService.java:51` skips validation for dev mode.
- **New deployment** must manually run SQL scripts in `src/main/resources/db/` (features_init, features_menu, audio_transcription, etc.). These do NOT auto-execute; missing tables cause "系统繁忙" on login.
- **Env values**: Frontend in `ui/.env.development` / `.env.production` (prefix `VITE_`). Backend in `application.yml` with `${key:default}` placeholders.
- **Mail SMTP**: Uses 163 SMTP (SSL port 465). Credentials from `application-local.yml` (gitignored) or env vars `MAIL_HOST/USERNAME/PASSWORD`.
- **SSE** at `/api/notification/stream` via `SseSessionManager`. Dashboard listens for events: `stats`, `enhanced`, `health`, `gc`. Send `Map` or POJO (not raw String) to guarantee valid JSON on frontend.
- **Dashboard SSE**: `DashboardChangeEvent` (Spring `ApplicationEventPublisher`) triggers change detection with `Map.equals` — only broadcasts when values differ. `noticeStats` is NOT SSE-pushed; fetched once via REST `/api/content/notice/summary`, refreshed by `stats` SSE event callback.
- **GlobalExceptionHandler**: Many `@ExceptionHandler` methods covering Business, Sa-Token (NotLogin/NotPermission), validation, DB, and generic exceptions.
- **`sass-embedded`** replaces `sass` (Dart Sass `modern-compiler` API in vite.config.js). No `sass` package.
- **AS400 代码分析**: 集成 [AS400_Parser](https://github.com/nxn1710/AS400_Parser)（MIT 协议）解析 RPGLE/RPG III/CL/DDS/DSPF/PRTF 源码。JAR 手动安装到本地 Maven 仓库。新增 API `POST /api/v1/as400/analysis` 接收源码文本，返回结构化 IR JSON。前端页面 `as400/codeAnalysis/index` 带代码编辑器和分析结果面板。新增 `sys_menu` 行需手动插入。

## Permission control logic

Implemented in `StpInterfaceImpl.java` via `Sa-Token StpInterface`.

### Roles (`getRoleList`)
- Reads from `sys_user_role` → `sys_role` where `status=1 AND deleted=0`.
- Returns `role_code` list for the current user.

### Permissions (`getPermissionList`)

**Admin user** (`role_code = 'admin'`):
- Directly returns `List.of("*")` — Sa-Token wildcard that matches **any** `@SaCheckPermission` check.
- Bypasses `sys_role_menu` and `sys_user_menu` tables entirely.
- This means: even if a `@SaCheckPermission("xxx")` value doesn't exist in `sys_menu`, admin can still access it.

**Non-admin users**:
- Merges two permission sources (deduplicated via `LinkedHashSet`):
  1. **Role-based** (`sys_role_menu`): `SELECT DISTINCT m.perms FROM sys_user_role ur JOIN sys_role_menu rm ON ur.role_id = rm.role_id JOIN sys_menu m ON rm.menu_id = m.id WHERE ur.user_id = ? AND m.perms IS NOT NULL AND m.perms != '' AND m.status = 1 AND m.deleted = 0`
  2. **Direct assignment** (`sys_user_menu`): `SELECT DISTINCT m.perms FROM sys_user_menu um JOIN sys_menu m ON um.menu_id = m.id WHERE um.user_id = ? AND m.perms IS NOT NULL AND m.perms != '' AND m.status = 1 AND m.deleted = 0`

### Key behaviors
- `@SaCheckPermission("module:entity:op")` guards every API endpoint — no method-level exemption (except the excludes list in Sa-Token config).
- `@OperateLog` is **always** paired with write operations but is purely audit-logging; it does **not** affect access control.
- `selectAllValidPerms()` exists on `SysUserMapper` but is **no longer used** — it was replaced by the admin `"*"` wildcard approach.
- `is-concurrent: false` + `is-share: false` in Sa-Token config: one device per login, later login kicks earlier one.

## Key restrictions

- ❌ No Redis, Fastjson, Spring Security/Shiro, JSP/Thymeleaf, Webpack
- ❌ No TypeScript — 项目使用 JavaScript (Vue 3 `<script setup>` + JSDoc 类型注解)
- ❌ No Docker — 手动 `mvn spring-boot:run` + `npm run dev` 部署
- ❌ No `console.log` in committed code
- ❌ No hardcoded colors, Chinese strings, magic numbers, or `localStorage` direct calls
- ❌ No Entity exposure in Controller params/returns (use DTO/VO)
- ❌ No `BeanUtils.copyProperties` (use MapStruct)

## 凭据管理

- **本地开发**：凭据写入 `application-local.yml`（已 gitignore），从 `application-local.template.yml` 复制后修改
- **生产部署**：通过环境变量注入（`MYSQL_PASSWORD`、`MAIL_PASSWORD`、`WHISPERX_API_KEY` 等）
- **启动脚本**：`start-backend.ps1` / `start-frontend.ps1`
- 所有 `application.yml` 中的 `${VAR:default}` 仅是占位默认值，生产环境务必通过环境变量覆盖

## Recent enhancements

### Flyway 数据库版本管理
- **双数据源**: 主数据源用 `spring.flyway.*` 自动配置 (`src/main/resources/db/migration/primary/`)，副数据源用 `SecondaryFlywayConfig.java` 手动配置 (`db/migration/second/`)
- **基线**: 现有库用 `baseline-on-migrate: true`，`V1__baseline.sql` 为标记基线
- **新迁移**: 在对应目录下创建 `V{版本}__{描述}.sql`，Flyway 启动自动执行
- **SQL 脚本**: 原有 `src/main/resources/db/*.sql` 保留为参考，不纳入 Flyway

### Nonce 持久化 (防重放)
- `ReplayAttackFilter.java` 不再使用 `ConcurrentHashMap`，改为 **Caffeine 缓存**
- 自动 TTL 过期（10 分钟）+ 容量上限（`app.replay.max-nonce-cache`），无需手动清理

### 密码策略增强
- `PASSWORD_PATTERN`：`^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;\",.<>/?]).{8,32}$`
  要求：大写 + 小写 + 数字 + 特殊字符，长度 8～32
- 密码历史表 `sys_user_password_history`（`V2__add_password_history.sql`），记录最近密码

### 结构化日志
- `logback-spring.xml` 新增 `JSON_FILE` appender（LogstashEncoder 格式）
- 写入 `logs/backend.json`，按天滚动，保留 30 天
- 兼容 ELK / Grafana Loki 等日志采集系统

### 自定义 Micrometer 指标
- `CustomMetricsService.java` 注册指标：登录成功/失败次数、写操作总数、API 耗时（p50/p95/p99）
- 通过 `management.endpoints.web.exposure.include: health,prometheus` 暴露给 Prometheus

### CI 流水线
- `.github/workflows/ci.yml`：push/PR 触发
  - **Backend**：JDK 17 + MySQL 8.0 Service → `mvn compile` + `mvn test`
  - **Frontend**：Node 18 → `npm ci` → `npm run lint` → `npm run build`

### 单元测试
- 后端测试框架：JUnit 5 + Mockito
- 测试用例：`AuthServiceTest`（登录/注册异常场景）、`SysUserServiceTest`（密码复杂度校验）
- 测试配置：`src/test/resources/application.yml`（H2 内存库，禁用 Flyway）
- 执行：`mvn test`
