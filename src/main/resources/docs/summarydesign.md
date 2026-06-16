# RX Admin 项目总结文档

> **版本**: 1.0.0 | **更新日期**: 2026-06-15 | **语言**: 中文
>
> 本文档整合 rxadmin.md、SKILL.md、rxadmin-setup.md、AGENT.MD、rxadmin-dev-skills.md、rxadmin-add-static-menu.md 及 .codebuddy/skills/rx-admin-dev/ 下全部文档的核心内容，作为项目整体参考。

---

## 1. 项目概述

**RX Admin** 是一个基于 **Spring Boot 3.5 + Vue 3.4** 的前后端分离通用后台管理系统，采用 **Modular Monolith（领域化单体）** 架构。

| 属性 | 值 |
|------|-----|
| GroupId | com.rx |
| ArtifactId | rx-admin |
| Java 版本 | 17+ |
| Node 版本 | 18+ |
| 数据库 | MySQL 8.0（主库 rx_admin + 次库 rxusysadmin） |
| 认证 | Sa-Token 内存模式（无 Redis） |
| 路由 | 完全动态路由，后端 sys_menu 表驱动 |
| 前端 baseURL | /api（Vite 代理到 localhost:8088） |

### 核心功能

- **认证授权**: 登录/注册/Token 管理，Sa-Token
- **系统管理**: 用户、角色、菜单、部门 CRUD，RBAC 权限模型
- **系统工具**: 字典、行政区划、接口分析、代码生成、批量导入、API 调试、数据备份、数据库 SQL 控制台、开发工具、邮件发送
- **系统监控**: 操作日志、在线用户、登录日志、导出审计、任务日志、缓存管理、慢查询、健康监控、日志分析
- **内容管理**: 通知公告、站内消息、消息模板、发送记录
- **权限管理**: 用户直接授权 + 角色权限双源合并，权限申请与审批
- **常用工具**: Excel 解析、PDF↔Word 互转、文档共享、流程图（3 引擎）、音乐播放器
- **技术博客**: Jsoup 多源抓取、Markdown 渲染、用户投稿
- **四大名著**: 红楼梦/三国/水浒/西游的人物/诗词/关系/章节/事件
- **历代文学**: 作者/朝代/体裁/分类/作品管理
- **AS400**: IBM i 系统对象浏览 + IService 接口平台
- **国际化**: 中英双语，300+ 条目，无刷新切换
- **主题系统**: 亮色/暗色双主题 + 5 套主题色（蓝/绿/紫/橙/青）

---

## 2. 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.5.15 | 应用框架 |
| MyBatis Plus | 3.5.5 | ORM |
| MapStruct | 1.5.5.Final | 编译期对象映射 |
| Sa-Token | 1.37.0 | 认证授权 |
| Knife4j | 4.4.0 | API 文档（OpenAPI 3） |
| MySQL | 8.x | 关系数据库 |
| BCrypt | (Spring Security) | 密码加密 |
| Guava | 33.0.0-jre | 限流 |
| Caffeine | (Boot 内嵌) | 本地缓存 |
| Spring Boot Mail | — | 邮件发送（SMTP） |
| FastExcel | 1.3.0 | Excel 解析 |
| PDFBox | 3.0.1 | PDF 操作 |
| Jsoup | 1.17.2 | HTML 解析 |
| mp3agic | 0.9.1 | MP3 元数据 |
| JTOpen | 20.0.8 | AS400 连接 |
| Lombok | — | 减少样板代码 |
| build-helper-maven-plugin | — | MapStruct generated-sources 识别 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | ^3.4.0 | Composition API + script setup |
| Vite | ^5.0.10 | 构建工具 |
| Vue Router | ^4.2.5 | 动态路由 |
| Pinia | ^2.1.7 | 状态管理 |
| Axios | ^1.6.2 | HTTP 请求 |
| Element Plus | ^2.4.3 | UI 组件库 |
| Vue I18n | ^9.14.4 | 国际化 |
| SCSS (sass-embedded) | ^1.69.5 | CSS 预处理 |
| ECharts | ^6.1.0 | 图表 |
| md-editor-v3 | ^6.5.1 | Markdown 编辑器 |
| marked | ^18.0.4 | Markdown 渲染 |
| highlight.js | ^11.11.1 | 代码高亮 |
| @vue-flow/core | ^1.48.2 | 流程图引擎 |
| @logicflow/core | ^2.2.3 | 流程图引擎 |
| @antv/x6 | ^3.1.7 | 流程图引擎 |
| exceljs / jspdf / html2canvas | — | 前端导出 |
| NProgress | ^0.2.0 | 进度条 |
| @fontsource/dm-sans / ibm-plex-sans / jetbrains-mono | — | 自托管字体 |
| @fortawesome/vue-fontawesome | ^3.0.0-5 | Font Awesome 图标 |
| @sentry/vue | 10.x | 错误监控 |
| DOMPurify | — | HTML 净化（XSS 防护） |
| rollup-plugin-visualizer | — | 构建产物体积分析 |

---

## 3. 后端架构

### 3.1 包结构

```
com.rx.admin
├── RxAs400Application.java           # 启动类（排除 DataSourceAutoConfiguration）
├── common/                            # 公共模块
│   ├── annotation/                    # @OperateLog, @DataScope
│   ├── result/                        # Result<T>, PageResult<T>
│   ├── exception/                     # GlobalExceptionHandler（10种异常）
│   ├── constant/                      # PageConstants
│   ├── utils/                         # CaptchaUtil, DataMaskUtil
│   ├── security/                      # IpFilter, NotLoginFilter, ReplayAttackFilter, XssJacksonConfig
│   ├── base/                          # BaseEntity, BaseCrudController
│   ├── aspect/                        # OperateLogAspect（@Async 异步 + 脱敏）
│   └── handler/                       # AesTypeHandler, DataScopeInnerInterceptor, SlowQueryInterceptor
├── framework/                         # 框架层配置
│   ├── datasource/                    # PrimaryDataSourceConfig / SecondDataSourceConfig / @SecondDB
│   ├── mybatis/                       # MybatisPlusConfig / MetaObjectHandlerConfig
│   ├── security/                      # SaTokenConfig / StpInterfaceImpl（双源合并权限）
│   ├── async/                         # AsyncConfig
│   ├── cache/                         # CacheConfig（Caffeine）
│   └── web/                           # CorsConfig / RateLimiterConfig
├── modules/                           # 业务模块层（领域化 DTO/VO/Convert）
│   ├── system/user/ role/ menu/ dept/ config/ dict/ ipRule/ file/ favorite/
│   ├── monitor/log/ loginlog/ job/ slowquery/
│   ├── content/notice/ message/
│   └── as400/techblog/
├── entity/                            # 实体（50+ 个）
├── controller/                        # 控制器（40+ 个）
├── service/                           # 服务层（50+ 个）
└── mapper/                            # Mapper（50+ 个，禁止 XML）
```

### 3.2 双数据源

| 数据源 | 数据库 | 用途 | 注解 |
|--------|--------|------|------|
| 主数据源 | rx_admin | 系统管理表（sys_*） | 无（默认） |
| 第二数据源 | rxusysadmin | 四大名著 + 行政区划 | @SecondDB |

### 3.3 DTO/VO/Convert 分层

所有 37 个子模块统一分层，共 16 个 Convert + 40 个 DTO + 17 个 VO：

| 层级 | 作用 | 示例路径 |
|------|------|----------|
| DTO | 请求参数封装，含 @Valid 校验 | modules/{domain}/{entity}/dto/ |
| VO | 响应视图对象，排除敏感字段 | modules/{domain}/{entity}/vo/ |
| Convert | MapStruct 编译期转换器 | modules/{domain}/{entity}/convert/ |

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

**强制规范**：
- `unmappedTargetPolicy = IGNORE` — 消除未映射字段编译警告
- updateEntity 方法必须加 `@BeanMapping` — null 值不覆盖已有字段
- 禁止 Entity 直接暴露到 Controller 层
- 禁止手动 `BeanUtils.copyProperties` 替代 MapStruct
- 禁止使用 `Mappers.getMapper()` 静态方法

### 3.4 核心配置文件

- `application.yml` — 双数据源、Sa-Token、文件上传、慢查询阈值等
- 邮件配置通过环境变量注入（`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME` 等）
- 前端 `.env.development` / `.env.production` — 心跳间隔、请求超时、表格行高等

---

## 4. 前端架构

### 4.1 目录结构

```
ui/src/
├── main.js                     # 入口（ElementPlus + Router + Pinia + i18n + 字体样式）
├── App.vue                     # 根组件（el-config-provider + router-view）
├── api/                        # 50+ API 模块
│   └── modules/                # 模块化聚合入口（auth/system/monitor/content/tool/as400/classics）
├── composables/                # 8 个组合式函数
│   ├── useStorage.js           # localStorage 统一管理
│   ├── useTablePage.js         # 通用表格分页
│   ├── useTheme.js             # 主题切换
│   ├── useMenuI18n.js          # 菜单国际化
│   ├── usePasswordStrength.js  # 密码强度检测
│   ├── useTableHeight.js       # 表格高度自适应
│   ├── useLayoutSettings.js    # 布局设置（ECharts 主题联动）
│   └── useMarkdownRenderer.js  # Markdown 渲染（marked + highlight.js）
├── i18n/                       # 中英文双语
├── layout/                     # 主布局 / SubMenu / TagsView / SearchBox / NoticePopover
├── components/                 # CommandPalette / FavoriteStar / FavoritesPanel / AnnouncementPopup / ExportButton
├── router/
│   ├── index.js                # constantRoutes + 动态路由注册
│   └── componentMap.js         # 60+ 组件映射
├── stores/                     # user.js + tags.js
├── styles/                     # variables.scss / global.scss / themes.scss
├── utils/                      # request.js（Axios 封装）/ sanitize.js（DOMPurify）/ echartsTheme.js
└── views/                      # 50+ 页面视图
```

### 4.2 动态路由

`constantRoutes` 只保留 Login + Layout 空壳，所有业务路由登录后通过后端 `/auth/routers` 返回的菜单树动态注入。

componentMap 映射规则：key 必须与 `sys_menu.component` 字段完全一致，name 必须与页面 `defineOptions({ name })` 一致。

### 4.3 Composables

| 函数 | 用途 |
|------|------|
| useStorage | localStorage 统一管理，`rx_admin_` 前缀命名空间 |
| useTablePage | 通用表格分页（loading/数据/分页/搜索/排序/列配置/高度适配/多选） |
| useTheme | 亮色/暗色双主题切换 |
| useMenuI18n | 菜单中文名 → i18n key 映射 |
| usePasswordStrength | 密码强度检测（弱/中/强） |
| useTableHeight | 表格高度自适应 |
| useLayoutSettings | 布局设置（侧边栏折叠/主题色切换/标签页） |
| useMarkdownRenderer | Markdown 渲染器（marked + highlight.js 封装） |

### 4.4 主题系统

CSS 变量双主题方案（`:root` / `html.dark`） + 5 套主题色（`data-theme` 属性切换）。50+ CSS 变量覆盖页面背景/文字/边框/侧边栏/顶栏/标签/搜索/通知。

ECharts 主题通过 `getComputedStyle()` 在运行时读取 `--rx-primary` CSS 变量，`invalidateCyberTheme()` 在主题切换时清除缓存。

---

## 5. 开发规范

### 5.1 后端强制规范

| 规范 | 说明 |
|------|------|
| 构造器注入 | 禁止 @Autowired，使用 private final + 构造函数 |
| DTO/VO 隔离 | 禁止 Entity 直接暴露到 Controller |
| MapStruct 转换 | 全部通过 Convert 接口，禁止 BeanUtils.copyProperties |
| unmappedTargetPolicy | Convert 接口必须设置为 IGNORE |
| 分页返回 | 使用 PageResult<T>，非 MyBatis Plus 原生 Page |
| 条件查询 | 使用 LambdaQueryWrapper（类型安全） |
| 写操作事务 | @Transactional(rollbackFor = Exception.class) |
| 操作日志 | @OperateLog 注解 + AOP 异步保存 + 敏感字段脱敏 |
| 权限注解 | 每个接口 @SaCheckPermission("module:entity:action") |
| API 文档 | @Tag(name) + @Operation(summary) |
| 禁止 MyBatis XML | 复杂 SQL 使用 @Select/@Update/@Delete 注解 |
| 空 catch 块 | 必须用 log.warn() 记录日志 |

### 5.2 前端强制规范

| 规范 | 说明 |
|------|------|
| defineOptions name | 必须声明，与 componentMap 完全一致 |
| 完全动态路由 | 不修改 router/index.js，只需更新 componentMap |
| useStorage | 管理所有 localStorage 操作，禁止原生 API |
| useTablePage | 表格页面推荐使用 |
| CSS 变量 | 禁止硬编码颜色值 |
| 定时轮询 | 必须传入 `_skipNProgress: true` |
| 删除操作 | 必须 ElMessageBox.confirm 二次确认 |
| 弹窗关闭 | 必须重置表单 |
| v-html | 必须经过 DOMPurify 净化 |
| 语言切换 | 使用 `i18n.global.locale.value`，禁止 `window.location.reload()` |
| ElMessageBox.confirm | rejection 必须用 try/catch 捕获 |

### 5.3 权限码规范

`{模块}:{实体}:{操作}` — 如 `sys:user:query`, `sys:user:add`, `sys:user:edit`, `sys:user:delete`

### 5.4 新增模块完整流程（12 步）

1. DDL SQL — 创建数据库表（sys\_ 前缀，含 deleted/create\_time/update\_time）
2. Entity — 继承 BaseEntity + @EqualsAndHashCode(callSuper = true) + @TableName
3. Mapper — 继承 BaseMapper<Entity> + @Mapper，禁止 XML
4. DTO — 创建 CreateDTO / UpdateDTO / QueryDTO
5. VO — 视图对象（响应用，排除敏感字段）
6. Convert — MapStruct 接口（unmappedTargetPolicy = IGNORE）
7. Service — IService<Entity> + ServiceImpl（构造器注入）
8. Controller — DTO 入参 → VO 出参 + @SaCheckPermission + @OperateLog
9. sys\_menu — 插入菜单记录（component 与 componentMap key 一致）
10. API 模块 — api/xxx.js（5 个标准函数）
11. Vue 页面 — views/xxx/index.vue（defineOptions name + useTablePage）
12. componentMap — 追加 1 行映射

---

## 6. 安全机制

| 机制 | 实现方式 |
|------|----------|
| 认证 | Sa-Token 内存模式，Token 7 天有效期 |
| 密码加密 | BCryptPasswordEncoder |
| 验证码 | BufferedImage + Graphics2D，开发环境固定 dev000 |
| 登录限流 | Guava RateLimiter 3次/秒/IP |
| 失败锁定 | 5 次失败锁定 30 分钟（LoginAttemptService） |
| 防重放 | X-Timestamp + X-Nonce 校验（ReplayAttackFilter） |
| XSS 防护 | Jackson 反序列化 HTML 转义（XssJacksonConfig） |
| IP 黑白名单 | IpFilter + sys\_config 持久化（黑/白/关闭三模式） |
| 敏感数据加密 | AES TypeHandler（email/phone）+ BCrypt（password） |
| 操作日志脱敏 | AOP 异步记录，过滤 password/token/secret 等 |
| 强制下线 | StpUtil.kickout → NotLoginFilter → 前端遮罩 5 秒倒计时 |
| Token 混淆 | Base64 + XOR 混淆存储在 localStorage |
| v-html 安全 | 全部经过 DOMPurify 净化 |
| 会话心跳 | 每 10 秒 GET /api/auth/ping（_skipNProgress: true） |

---

## 7. 页面与模块清单

### 系统管理（8 个页面）
用户管理 / 角色管理 / 菜单管理 / 部门管理 / 系统配置 / IP 黑白名单 / 文件管理 / 通知公告

### 系统监控（10 个页面）
操作日志 / 在线用户 / 登录日志 / 导出审计 / 定时任务 / 任务日志 / 慢查询 / 健康监控 / 日志分析 / 缓存管理

### 系统工具（16 个页面）
字典管理 / 行政区划 / 接口分析 / 项目文档 / 开发规范 / 代码生成 / 批量导入 / API 调试 / 数据备份 / 数据库工具 / 开发工具 / 邮件发送 / Excel 解析 / 文档转换 / 文档共享 / 流程图（3 引擎）

### 内容管理（3 个页面）
通知公告 / 消息中心 / 通知中心

### 四大名著 + 历代文学（14 个页面）
红楼梦（诗词/人物/关系）/ 三国（诗词/人物）/ 水浒（诗词/章节）/ 西游（诗词/人物/事件）/ 历代文学（总览/作品）

### AS400（3 个页面）
AS400 对象浏览 / IService 接口平台 / 技术博客（列表 + 详情）

### 其他
仪表盘 / 知识图谱 / 个人信息 / 权限申请 / 登录 / 音乐播放器

---

## 8. 优化与完成状态

### 已完成的核心优化

| 优化项 | 说明 |
|--------|------|
| Modular Monolith | 284+ 文件移动，37 个子模块 layered |
| DTO/VO/Convert 全覆盖 | 16 个 Convert + 40 个 DTO + 17 个 VO |
| MapStruct 引入 | 编译期类型安全转换，替代 BeanUtils.copyProperties |
| 构造器注入 | 全部 @Autowired → private final + 构造函数 |
| Swagger 文档 | 8 个 @Tag + 40 个 @Operation |
| serialVersionUID | 所有 Serializable 类补全 |
| 空 catch 修复 | 23 处 → log.warn() |
| 主题色系统统一 | CSS 变量 → --rx-primary 设计令牌，ECharts 运行时读取 |
| Sentry 升级 | 移除 @sentry/tracing，适配 v10 API |
| 字体自托管 | Google Fonts CDN → @fontsource/* npm 包 |
| sass 去重 | 移除 sass，保留 sass-embedded |
| Markdown composable | 抽取 useMarkdownRenderer.js |
| 构建优化 | 8 个 manualChunks + visualizer + analyze 脚本 |
| api/config.js | 8 个导出对接 SysConfigController |
| 缓存管理 bug 修复 | ElMessageBox.confirm() 拒绝处理 |

### 架构级决策

| 决策 | 说明 |
|------|------|
| 不使用 Redis | Caffeine 本地缓存替代，重启可接受 |
| Sa-Token 内存模式 | 重启后需重新登录 |
| 禁止 MyBatis XML | SQL 只使用注解 |
| 禁止 @Autowired | 构造器注入统一 |
| 主题色前移 | ECharts 主题通过 CSS 变量运行时读取 |
| Google Fonts 替换 | @fontsource 自托管，无外部 CDN 依赖 |

---

> **文档维护**: 本文档由 rxadmin.md、SKILL.md、rxadmin-setup.md、AGENT.MD、rxadmin-dev-skills.md、rxadmin-add-static-menu.md 及 .codebuddy/skills/rx-admin-dev/ 下全部文档的核心内容整合而成，随项目迭代持续更新。
