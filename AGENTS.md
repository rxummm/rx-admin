# AGENTS.md — RX Admin

Spring Boot 3.5.15 + Vue 3 后台管理系统。双 MySQL 数据源，Sa-Token 内存模式，无 Redis。

## Quick start

**Backend** (port 8088):
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
`local` enables MyBatis SQL logging + local mail config. Without it, mail env vars must be set. Login at `http://localhost:8088/doc.html` (Knife4j).

**Frontend** (port 3000):
```powershell
cd ui; npm run dev
```
Proxies `/api` → `http://localhost:8088` (Vite config, env var `VITE_API_PROXY_TARGET`). Default login: `admin` / `admin123`.

**Fast compile check** (avoids full boot):
```powershell
mvn compile -q
```

**E2E tests** (Playwright, requires backend + frontend running):
```powershell
cd ui; node tests/e2e.spec.js
```

## Architecture must-knows

- **`RxAdminApplication.java`** excludes `DataSourceAutoConfiguration` — dual datasource (`rx_admin` + `rxusysadmin`) configured manually in `application.yml` `spring.datasource.primary/secondary`.
- **Sa-Token memory mode** — no Redis, token state lost on restart. `is-concurrent: false`, `is-share: false`.
- **Caffeine** replaces Redis for all caching (Spring `@Cacheable` + `@CacheEvict`).
- **No MyBatis XML** — all SQL in annotations (`@Select`, `@Update`, etc.).
- **System routes are DB-driven** — `sys_menu` table, not Vue Router static routes. Register new pages via `componentMap.js` + `sys_menu` insert. Router's `resetDynamicRoutes()` tears down and rebuilds dynamic routes (called on login/logout/permission change).
- **Sentry** in frontend (`@sentry/vue`) — no backend Sentry.

## Backend conventions

| Rule | Detail |
|------|--------|
| Inject with `@RequiredArgsConstructor` | No `@Autowired` |
| API prefix | `/api/{module}/{entity}` |
| Response wrapper | `Result.ok(data)` / `Result.fail(msg)` |
| Pagination | `PageResult.of(IPage)` — never return MP `Page` directly |
| DTO/VO/Convert | Entity never in Controller params/returns. MapStruct: `unmappedTargetPolicy=IGNORE`, update methods: `@BeanMapping(nullValuePropertyMappingStrategy=IGNORE)` |
| Permission | Every endpoint needs `@SaCheckPermission("module:entity:op")` |
| Write ops | `@Transactional` + `@OperateLog(module, operation)` |
| Validation | `@Valid` on `@RequestBody` params |

## Frontend conventions

- `<script setup>` + `defineOptions({ name: 'PascalName' })` — name must match `componentMap.js` entry.
- Table pages: use `useTablePage(apiModule)` composable.
- `useStorage(STORAGE_KEYS.X)` returns `{ get, set, remove }` — never raw `localStorage`.
- Polling/background requests **must** pass `{ _skipNProgress: true }` (otherwise every tick flashes NProgress).
- `v-html` must use `sanitizeHtml()` from `@/utils/sanitize` (DOMPurify).
- CSS: use CSS variables (`--bg-page`, `--color-primary`, etc.) — no hardcoded colors.
- i18n: `$t('key')` — no hardcoded Chinese strings.

## Dev quirks & gotchas

- **Dev captcha**: always `dev000` for admin user. Check `CaptchaService.java:51`.
- **New deployment** must manually run `db/features_init.sql` (3 tables) and `db/features_menu.sql` (v2.0 menus). These do NOT auto-execute; missing tables cause "系统繁忙" (sys_user_favorite on login).
- **Env values** in `ui/.env.development` / `.env.production` (prefix `VITE_`). Backend equivalents in `application.yml` with `${key:default}` placeholders.
- **Mail SMTP**: env vars (`MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`) or `application-local.yml` (gitignored). Uses 163 SMTP with SSL port 465.
- **SSE unified notification** at `/api/notification/stream` (via `SseSessionManager`). Dashboard listens for events: `stats`, `enhanced`, `health`, `gc`. System stats (`stats`) include `noticeCount` = full table count (including all categories).
- **SSE data must be JSON-capable** — `SseEmitter.send()` serializes String as plain text, not JSON. Send `Map` or POJO to guarantee valid `JSON.parse` on frontend.
- **Dashboard immediate push**: Services publish `DashboardChangeEvent` (Spring `ApplicationEventPublisher`); `DashboardController.@EventListener` runs change detection (`Map.equals`) and broadcasts SSE only when values differ.
- **DashboardCache dirty flags** (`literatureDirty`, `classicsDirty`, `techblogDirty`) — sub-sections only recompute on change, avoid heavy queries every 30s.
- **Build chunks** (vite manualChunks): echarts, element-plus, flowchart, editor, export, icons, fonts, markdown, vendor — in `vite.config.js`.
- **GlobalExceptionHandler**: 16 `@ExceptionHandler` methods (Business, NotLogin, NotPermission, Bind, MissingParam, HttpMessageNotReadable, MethodArgumentNotValid, IllegalArgumentException, TypeMismatch, NoResourceFound, HttpMethodNotSupported, HttpMediaTypeNotSupported, DataIntegrityViolation, TransactionSystem, UnexpectedRollback, ConstraintViolation, MissingPathVariable, HttpMessageNotWritable, DuplicateKey, BadSqlGrammar, AsyncRequestTimeout, AsyncRequestNotUsable, IOException, Exception).
- **Dashboard SSE 通知与消息 section** (`noticeStats`) is NOT pushed via SSE — it's fetched once via REST `/api/content/notice/summary`. Refresh is triggered by the `stats` SSE event handler calling `fetchNoticeAndHealthStats()`.

## Key restrictions

- ❌ No Redis, no Fastjson, no Spring Security/Shiro, no JSP/Thymeleaf, no Webpack
- ❌ No `console.log` in committed code
- ❌ No hardcoded colors, Chinese strings, magic numbers, or `localStorage` direct calls
- ❌ No Entity exposure in Controller params/returns (use DTO/VO)
- ❌ No `BeanUtils.copyProperties` (use MapStruct)
- ❌ No `sass` package (use `sass-embedded` with Dart Sass `modern-compiler` API — configured in vite.config.js)
- ❌ No Google Fonts CDN (use `@fontsource/*` npm packages: dm-sans, ibm-plex-sans, jetbrains-mono)
