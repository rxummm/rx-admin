# RX Admin 通用管理系统 — 项目技术架构文档

> **版本**: 3.2.0 | **更新日期**: 2026-06-15 | **文档类型**: 技术架构说明书
>
> **v3.2 更新**: 主题色系统统一（CSS 变量 → design tokens）+ ECharts 主题运行时读取 CSS 变量 + Sentry v10 升级 + 字体自托管（Google Fonts → @fontsource）+ sass 去重 + useMarkdownRenderer 抽取 + 构建优化（8 manualChunks + visualizer）+ api/config.js 新建 + cache-manage bug 修复

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术栈总览](#2-技术栈总览)
3. [后端架构](#3-后端架构)
   - [3.1 项目坐标与启动类](#31-项目坐标与启动类)
   - [3.2 包结构 (v3 Modular Monolith)](#32-包结构-v3-modular-monolith)
   - [3.3 数据源配置](#33-数据源配置)
   - [3.4 实体层 (Entity)](#34-实体层-entity)
   - [3.5 数据访问层 (Mapper)](#35-数据访问层-mapper)
   - [3.6 服务层 (Service)](#36-服务层-service)
   - [3.7 控制层 (Controller)](#37-控制层-controller)
   - [3.8 安全认证 (Sa-Token)](#38-安全认证-sa-token)
   - [3.9 公共模块 (common/)](#39-公共模块-common)
   - [3.10 框架层 (framework/)](#310-框架层-framework)
   - [3.11 业务模块层 (modules/)](#311-业务模块层-modules)
   - [3.12 DTO/VO/Convert 分层规范](#312-dtovoconvert-分层规范)
   - [3.13 API 接口清单](#313-api-接口清单)
4. [前端架构](#4-前端架构)
   - [4.1 技术栈](#41-技术栈)
   - [4.2 目录结构](#42-目录结构)
   - [4.3 路由设计](#43-路由设计)
   - [4.4 状态管理 (Pinia)](#44-状态管理-pinia)
   - [4.5 API 请求层](#45-api-请求层)
   - [4.6 页面视图清单](#46-页面视图清单)
   - [4.7 权限申请与审批](#47-权限申请与审批)
   - [4.8 待办事项提醒](#48-待办事项提醒)
5. [布局与样式](#5-布局与样式)
   - [5.1 整体布局](#51-整体布局)
   - [5.2 UI 组件库](#52-ui-组件库)
   - [5.3 主题系统](#53-主题系统)
   - [5.4 全局样式](#54-全局样式)
   - [5.5 响应式与动画](#55-响应式与动画)
   - [5.6 收藏夹](#56-收藏夹)
   - [5.7 全站命令面板 (Ctrl+K)](#57-全站命令面板-ctrlk)
- [6. 常用工具模块](#6-常用工具模块)
   - [6.1 音乐播放器](#61-音乐播放器)
   - [6.2 技术博客](#62-技术博客)
   - [6.3 Excel/PDF/Word 文档工具](#63-excelpdfword-文档工具)
   - [6.4 流程图编辑器](#64-流程图编辑器)
   - [6.5 API 接口分析](#65-api-接口分析)
   - [6.6 中国行政区划](#66-中国行政区划)
   - [6.7 AS400 IService 接口平台](#67-as400-iservice-接口平台)
   - [6.8 邮件发送](#68-邮件发送)
   - [6.9 数据库工具](#69-数据库工具)
   - [6.10 开发工具](#610-开发工具)
- [7. 四大名著模块](#7-四大名著模块)
- [8. 菜单功能详解](#8-菜单功能详解)
   - [8.1 仪表盘](#81-仪表盘)
   - [8.2 系统管理](#82-系统管理)
   - [8.3 系统工具](#83-系统工具)
   - [8.4 内容管理](#84-内容管理)
   - [8.5 办公工具](#85-办公工具)
   - [8.6 音乐播放器](#86-音乐播放器)
   - [8.7 系统监控](#87-系统监控)
   - [8.8 权限申请](#88-权限申请)
- [9. 构建与部署](#9-构建与部署)
- [10. 项目搭建与新增模块指南（独立文档）](./rxadmin-setup.md)
- [12. 路由动态化](#12-路由动态化)
- [13. 启动命令](#13-启动命令)
- [14. 安全机制](#14-安全机制在线用户踢出xss防重放加密脱敏)
- [15. 密码策略（P1）](#15-密码策略p1)
- [16. i18n 国际化](#16-i18n-国际化)
- [17. v2.0 新增功能模块 (2026-06-05)](#17-v20-新增功能模块-2026-06-05)
- [18. Git 版本控制功能集成方案（独立文档）](./rxadmin-git.md)
- [19. 待实施增强建议](#19-待实施增强建议)
- [20. 待修复项（生产发布前）](#20-待修复项生产发布前)

---

## 1. 项目概述

**RX Admin** 是一个基于 **Spring Boot 3 + Vue 3** 的通用后台管理系统，采用前后端分离架构。系统包含完整的用户/角色/菜单/部门/字典等 RBAC 权限管理功能，支持权限申请与审批流程，四大名著（红楼梦、三国演义、水浒传、西游记）的经典文化数据管理模块，AS400 IBM i 系统对象浏览与 IService 接口管理，音乐播放器，技术博客多源抓取，邮件发送工具，数据库SQL控制台，开发工具箱，以及丰富的常用工具集（文档转换、Excel解析、流程图编辑等）。

### 核心功能模块

| 模块 | 说明 |
|------|------|
| **认证授权** | 登录/注册/Token 管理，基于 Sa-Token |
| **系统管理** | 用户、角色、菜单、部门 CRUD，RBAC 权限模型 |
| **系统工具** | 字典管理、行政区划、接口分析、数据库工具（SQL控制台+表结构+连接池）、开发工具（JSON/Base64/UUID/时间戳/二维码/正则）、代码生成、批量导入、API调试、数据备份、邮件发送 |
| **系统监控** | 操作日志、在线用户、登录日志、导出审计、任务执行日志、缓存管理、慢查询监控、健康监控、日志分析 |
| **内容管理** | 通知公告、消息中心、通知中心（消息模板+发送记录） |
| **权限管理** | 用户直接授权、权限申请与审批（双源合并 RBAC） |
| **系统配置** | 系统参数配置管理 |
| **定时任务** | Quartz 定时任务管理（新增/修改/暂停/执行） |
| **文件管理** | 文件上传/下载/列表管理 |
| **AS400 IService** | IBM i 接口平台管理（类别/条目/列/示例/参数） |
| **仪表盘** | 统计概览 + 增强仪表盘 + 知识图谱（ECharts 可视化图表，支持暗黑模式） |
| **常用工具** | Excel解析、PDF↔Word互转、文档上传共享、流程图编辑器（3引擎）、邮件发送、数据库SQL控制台、开发工具 |
| **音乐播放器** | MP3 流式播放、歌单管理、播放记录统计、热门排行 |
| **技术博客** | 多源文章抓取（Jsoup）、分类浏览、Markdown渲染、用户投稿 |
| **API 分析** | 菜单级前后端交互链路自动分析 |
| **历代文学** | 历代文学作品管理（作者/朝代/体裁/内容分类） |
| **四大名著** | 红楼梦/三国/水浒/西游的人物、诗词、关系、章节等数据管理 |
| **AS400 管理** | IBM i (AS400) 系统库对象浏览与查询 |
| **国际化** | 中/英文双语切换，菜单/表单/提示全量翻译 |
| **暗黑模式** | 全站亮色/暗色双主题，CSS 变量 + ECharts 暗黑适配 + 5 套主题色 |
| **系统健康监控** | CPU/内存/磁盘/JVM/GC 实时监控，ECharts 可视化，10秒自动刷新 |
| **IP黑白名单** | IP 规则 CRUD，黑名单/白名单/关闭三模式切换，sys_config 持久化 |
| **站内消息中心** | 用户间私信、系统通知，未读计数，el-timeline 时间轴展示 |
| **快捷收藏夹** | 侧边栏收藏面板 + 页面星标组件，toggle 切换，localStorage 缓存 |
| **系统公告弹窗** | 登录后自动弹窗公告，一次一条，localStorage 去重 |
| **字体自托管** | Google Fonts CDN 替换为 @fontsource/dm-sans/ibm-plex-sans/jetbrains-mono，无外部 CDN 依赖 |
| **构建优化** | 8 个 manualChunks（vendor/echarts/element-plus/flowchart/editor/export/icons/markdown）+ rollup-plugin-visualizer |
| **Sentry 错误监控** | @sentry/vue v10 + browserTracingIntegration，移除已废弃 @sentry/tracing |
| **代码生成器** | 三步向导（选表→配置→预览），生成 Entity/Mapper/Service/Controller/Vue/API |
| **批量数据导入** | Excel/CSV 上传→预览→执行，三步向导，含校验与错误报告 |
| **日志可视化分析** | 4 统计卡片 + ECharts 柱状图/饼图/折线图，按小时/类型/趋势分析 |
| **API 调试面板** | 左右分栏（端点列表+请求面板），类似 Postman，扫描 RequestMappingHandlerMapping |
| **数据库备份恢复** | mysqldump 备份到 backups/ 目录，列表管理/下载/还原 |
| **Git 版本管理** | Web 查看仓库状态/提交历史/分支/文件差异，支持 Pull 拉取 |
| **全局命令搜索** | Ctrl+K 唤起 Spotlight 风格面板，菜单+最近访问+快捷操作 |
| **多主题色系统** | 5 套预设主题色（蓝/绿/紫/橙/青），CSS 变量 + data-theme 切换 |
| **知识图谱** | Dashboard 知识图谱子页，ECharts 关系图展示系统实体关联 |
| **邮件发送工具** | SMTP 邮件发送，支持 HTML 富文本模板，发送历史记录 |

---

## 2. 技术栈总览

| 层级 | 技术 | 版本 |
|------|------|------|
| **运行环境** | Java / Node.js | Java 17 / Node 18+ |
| **后端框架** | Spring Boot | 3.5.15 |
| **ORM** | MyBatis Plus | 3.5.5 |
| **对象映射** | MapStruct | 1.5.5 |
| **安全认证** | Sa-Token | 1.37.0 |
| **API 文档** | Knife4j (OpenAPI 3) | 4.4.0 |
| **数据库** | MySQL | 8.x |
| **密码加密** | Spring Security Crypto (BCrypt) | — |
| **AOP 日志** | Spring AOP + @Async | — |
| **限流** | Guava RateLimiter | 33.0.0-jre |
| **邮件发送** | Spring Boot Mail (SMTP) | — |
| **本地缓存** | Caffeine | Spring Boot 内嵌 |
| **监控** | Spring Boot Actuator + Micrometer Prometheus | — |
| **AS400 连接** | JTOpen (jt400) | 20.0.8 |
| **Excel 解析** | FastExcel（阿里 EasyExcel 继任） | 1.3.0 |
| **PDF 操作** | Apache PDFBox | 3.0.1 |
| **MP3 元数据** | mp3agic | 0.9.1 |
| **HTML 解析** | Jsoup | 1.17.2 |
| **前端框架** | Vue 3 (Composition API) | ^3.4.0 |
| **构建工具** | Vite | ^5.0.10 |
| **路由** | Vue Router | ^4.2.5 |
| **状态管理** | Pinia | ^2.1.7 |
| **HTTP 客户端** | Axios | ^1.6.2 |
| **UI 组件库** | Element Plus | ^2.4.3 |
| **图表库** | ECharts | ^6.1.0 |
| **图标库** | @element-plus/icons-vue | ^2.3.1 |
| **图标库** | @fortawesome/vue-fontawesome | ^3.0.0-5 |
| **国际化** | Vue I18n | ^9.14.4 |
| **CSS 预处理** | SCSS (sass-embedded) | ^1.69.5 |
| **流程图引擎** | @vue-flow/core / @logicflow/core / @antv/x6 | ^1.48.2 / ^2.2.3 / ^3.1.7 |
| **Markdown 编辑器** | md-editor-v3 | ^6.5.1 |
| **进度条** | NProgress | ^0.2.0 |
| **Markdown 渲染** | marked + highlight.js | ^18.0.4 / ^11.11.1 |
| **Markdown 样式** | github-markdown-css | ^5.9.0 |
| **错误监控** | @sentry/vue | ^10.0.0 |
| **自托管字体** | @fontsource/dm-sans / ibm-plex-sans / jetbrains-mono | ^5.x |
| **体积分析** | rollup-plugin-visualizer | ^5.x |
| **Excel 导出** | exceljs + jspdf + html2canvas | 前端导出引擎 |

---

## 3. 后端架构

### 3.1 项目坐标与启动类

```xml
<groupId>com.rx</groupId>
<artifactId>rx-admin</artifactId>
<version>1.0.0</version>
<name>RX Admin</name>
```

**启动类**: `com.rx.admin.RxAs400Application`（排除 `DataSourceAutoConfiguration`，手动配置双数据源）

**Spring Boot 版本**: 3.5.15（通过 `spring-boot-starter-parent` 继承）

**配置文件**: `src/main/resources/application.yml`

```yaml
server:
  port: 8088

spring:
  application:
    name: rx-admin

# 双数据源：rx_admin（系统管理） + rxusysadmin（业务数据）
# Sa-Token：内存模式，token 有效期 604800 秒（7天）
# AS400：连接 pub400.com（可选）
```

### 3.2 包结构 (v3 Modular Monolith)

v3 采用 **Modular Monolith（领域化单体）** 架构，按业务模块组织代码，替代原来的平铺式分层结构。

```
com.rx.admin
├── RxAs400Application.java              # Spring Boot 启动类
│
├── common/                               # 公共模块（按职责拆分子包）
│   ├── annotation/                       # 自定义注解
│   │   ├── DataScope.java               # 数据权限注解
│   │   └── OperateLog.java              # 操作日志注解
│   ├── result/                           # 统一响应
│   │   ├── Result.java                  # 统一响应封装 {code, msg, data}
│   │   └── PageResult.java              # 分页响应 {records, total, page, pageSize}
│   ├── exception/                        # 异常处理
│   │   └── GlobalExceptionHandler.java  # 全局异常处理 (10种异常)
│   ├── constant/                         # 常量定义
│   │   └── PageConstants.java           # 分页常量
│   ├── utils/                            # 工具类
│   │   ├── CaptchaUtil.java             # 验证码工具
│   │   └── DataMaskUtil.java            # 数据脱敏工具
│   ├── security/                         # 安全组件
│   │   ├── IpFilter.java                # IP 黑白名单过滤器
│   │   ├── NotLoginFilter.java          # 未登录过滤器（区分KICK_OUT/过期）
│   │   ├── ReplayAttackFilter.java      # 防重放攻击过滤器
│   │   └── XssJacksonConfig.java        # XSS 防护 Jackson 配置
│   ├── base/                             # 基类
│   │   ├── BaseEntity.java              # 实体基类
│   │   └── BaseCrudController.java      # CRUD 控制器基类（构造器注入）
│   ├── aspect/                           # AOP 切面
│   │   └── OperateLogAspect.java        # 操作日志切面
│   └── handler/                          # MyBatis 处理器
│       ├── AesTypeHandler.java           # AES 加密类型处理器
│       ├── DataScopeInnerInterceptor.java # 数据权限拦截器
│       └── SlowQueryInterceptor.java     # 慢查询拦截器
│
├── framework/                            # 框架层（基础设施配置）
│   ├── security/                         # 安全配置
│   │   ├── SaTokenConfig.java           # Sa-Token 路由拦截配置
│   │   └── StpInterfaceImpl.java        # 权限/角色加载实现（双源合并）
│   ├── datasource/                       # 数据源配置
│   │   ├── PrimaryDataSourceConfig.java # 主数据源 rx_admin
│   │   ├── SecondDataSourceConfig.java  # 第二数据源 rxusysadmin
│   │   └── SecondDB.java               # @SecondDB 注解
│   ├── mybatis/                          # MyBatis Plus 配置
│   │   ├── MybatisPlusConfig.java       # 分页插件 & 自动填充
│   │   └── MetaObjectHandlerConfig.java # 元数据自动填充
│   ├── async/                            # 异步配置
│   │   └── AsyncConfig.java             # 异步任务线程池
│   ├── cache/                            # 缓存配置
│   │   └── CacheConfig.java             # Caffeine 缓存配置
│   └── web/                              # Web 配置
│       ├── CorsConfig.java              # CORS 跨域配置
│       └── RateLimiterConfig.java       # 限流配置
│
├── modules/                              # 业务模块层（领域化单体）
│   ├── system/                           # 系统管理模块
│   │   ├── user/     dto/ vo/ convert/
│   │   ├── role/     dto/ vo/ convert/
│   │   ├── menu/     dto/ vo/ convert/
│   │   ├── dept/     dto/ vo/ convert/
│   │   ├── config/   dto/ vo/ convert/
│   │   ├── dict/     dto/ vo/ convert/
│   │   ├── ipRule/   dto/ vo/ convert/
│   │   ├── file/     dto/ vo/ convert/
│   │   └── favorite/ dto/ vo/ convert/
│   ├── monitor/                          # 系统监控模块
│   │   ├── log/        vo/ convert/
│   │   ├── loginlog/   vo/ convert/
│   │   ├── job/        dto/ vo/ convert/
│   │   └── slowquery/  vo/ convert/
│   ├── content/                          # 内容管理模块
│   │   ├── notice/    dto/ vo/ convert/
│   │   └── message/   dto/ vo/ convert/
│   └── as400/                            # AS400 模块
│       └── techblog/  dto/ vo/ convert/
│
├── entity/                               # 实体定义（共用）
├── controller/                           # 控制器（共用）
├── service/                              # 服务层（共用）
└── mapper/                               # 数据访问层（共用）
```

> **说明**: modules/ 当前已创建完整的 DTO/VO/Convert 层（16 个 Convert 接口 + 对应 DTO/VO），Controller/Service/Mapper 逐步从顶层迁入对应模块目录。

### 3.3 数据源配置

系统采用**双数据源**架构：

| 数据源 | 数据库 | 用途 | 配置类 |
|--------|--------|------|--------|
| **主数据源** (Primary) | `rx_admin` | 系统管理表（用户/角色/菜单/部门/日志等） | `PrimaryDataSourceConfig` |
| **第二数据源** (Second) | `rxusysadmin` | 四大名著业务数据表 | `SecondDataSourceConfig` |

- 使用 `@SecondDB` 自定义注解标记使用第二数据源的 Mapper
- MyBatis Plus 分页插件配置在主数据源中

### 3.4 实体层 (Entity)

#### 系统管理实体（主数据源 `rx_admin`）

| 实体类 | 表名 | 说明 | 主要字段 |
|--------|------|------|----------|
| `SysUser` | `sys_user` | 系统用户 | username(UNIQUE), password(BCrypt加密), nickname, email(UNIQUE+AES加密), phone(AES加密), avatar, gender, status, remark |
| `SysRole` | `sys_role` | 系统角色 | name, code, description, status |
| `SysMenu` | `sys_menu` | 系统菜单 | name, path, component, icon, parentId, type(目录/菜单/按钮), permission, sort |
| `SysDept` | `sys_dept` | 部门 | name, parentId, sort, leader, phone, status |
| `SysLog` | `sys_log` | 操作日志 | userId, username, operation, method, params, ip, duration |
| `SysNotice` | `sys_notice` | 通知公告/待办事项 | title, content, noticeType, category, linkPath, status |
| `SysDictData` | `sys_dict_data` | 字典数据 | dictType, label, value, sort, status |
| `SysDictType` | `sys_dict_type` | 字典类型 | name, type, status |

**新增系统实体**:

| 实体类 | 表名 | 说明 | 主要字段 |
|--------|------|------|----------|
| `SysConfig` | `sys_config` | 系统配置 | name, configKey, configValue, remark |
| `SysJob` | `sys_job` | 定时任务 | beanName, methodName, cronExpression, status |
| `SysFile` | `sys_file` | 文件管理 | fileName, originalName, filePath, fileSize, fileType |
| `SysSlowQuery` | `sys_slow_query` | 慢查询记录 | querySql, queryTime, executeTime, userName |
| `SysPermissionRequest` | `sys_permission_request` | 权限申请 | userId, menuIds, status(待审批/通过/拒绝), remark |
| `Song` | `sys_songs` | 音乐歌曲 | title, artist, album, duration, filePath, coverUrl |
| `TechBlogArticle` | `tech_blog_article` | 技术博客文章 | title, slug, sourceUrl, author, publishDate, categories, excerptText, contentHtml, contentText, coverImage, sort, viewCount, source |
| `SysLoginLog` | `sys_login_log` | 登录日志 | username, ip, browser, os, status(1=成功/0=失败), failReason, loginTime |
| `SysExportLog` | `sys_export_log` | 导出审计日志 | username, exportType(excel/pdf), exportTitle, recordCount, fileName, ip, exportTime |
| `SysMessageTemplate` | `sys_message_template` | 通知模板 | name, code(UNIQUE), titleTemplate, contentTemplate, channels, status |
| `SysNotifyRecord` | `sys_notify_record` | 通知发送记录 | templateId, channel, receiver, title, content, status, errorMsg, retryCount |
| `SysJobLog` | `sys_job_log` | 任务执行日志 | jobId, jobName, startTime, endTime, durationMs, status, errorMessage |
| `SysIpRule` | `sys_ip_rule` | IP黑白名单规则 | ipAddress, ruleType(black/white), status, remark |
| `SysUserFavorite` | `sys_user_favorite` | 用户收藏 | userId, menuId, menuPath, menuName, createTime |
| `SysNoticeRead` | `sys_notice_read` | 通知已读记录 | noticeId, userId, readTime |
| `SysMessage` | `sys_message` | 站内消息 | senderId, receiverId, title, content, msgType, isRead |
| `SysExportConfig` | `sys_export_config` | 导出列配置 | pagePath, visibleColumns(JSON), createTime/updateTime |
| `LoginRequest` | — | 登录请求 DTO | username, password, captcha |
| `RegisterRequest` | — | 注册请求 DTO | username, password, confirmPassword, nickname, email |
| `SharedFile` | `sys_shared_files` | 共享文档 | fileName, originalName, filePath, fileSize, uploadUser |
| `PlayRecord` | `sys_play_records` | 音乐播放记录 | songId, userId, playTime, playDuration |

**中间表**:
- `sys_user_role` — 用户角色关联
- `sys_role_menu` — 角色菜单关联
- `sys_user_menu` — 用户直接授权关联（个性化权限，不通过角色）

#### 四大名著 + 历代文学实体（第二数据源 `rxusysadmin`，16 个）

| 实体类 | 表名 | 说明 |
|--------|------|------|
| `HonglouCharacter` | `honglou_characters` | 红楼梦人物 |
| `HonglouCharacterRelation` | `honglou_character_relations` | 红楼梦人物关系 |
| `HonglouPoem` | `honglou_poems` | 红楼梦诗词 |
| `SanguoCharacter` | `sanguo_characters` | 三国人物 |
| `SanguoPoem` | `sanguo_poems` | 三国诗词 |
| `ShuihuChapter` | `shuihu_chapters` | 水浒章节 |
| `ShuihuPoem` | `shuihu_poems` | 水浒诗词 |
| `XiyouCharacter` | `xiyou_characters` | 西游人物 |
| `XiyouEvent` | `xiyou_events` | 西游八十一难 |
| `XiyouPoem` | `xiyou_poems` | 西游诗词 |
| `LiteraryWork` | `literature_works` | 文学作品 |
| `Author` | `literature_authors` | 文学作者 |
| `Dynasty` | `literature_dynasties` | 朝代 |
| `Genre` | `literature_genres` | 体裁 |
| `ContentCategory` | `literature_content_categories` | 内容分类 |
| `ChinaRegion` | `china_regions` | 行政区划 |

### 3.5 数据访问层 (Mapper)

所有 Mapper 继承 MyBatis Plus `BaseMapper<T>`，自动获得 CRUD 能力。共 **50+ 个 Mapper**。

### 3.6 服务层 (Service)

服务层基于 MyBatis Plus `IService<T>` / `ServiceImpl<M, T>` 模式。共 **50+ 个 Service**。

**系统管理服务**（30+ 个）:
- `AuthService` — 登录认证、注册、Token 签发
- `SysUserService` — 用户 CRUD + 角色分配 + 密码修改
- `SysRoleService` — 角色 CRUD + 菜单权限分配
- `SysMenuService` — 菜单 CRUD + 树形构建 + 路由生成
- `SysDeptService` — 部门 CRUD + 树形结构
- `SysLogService` — 操作日志记录与查询（含删除/批量删除）
- `SysNoticeService` — 通知公告/待办事项管理
- `SysDictDataService` — 字典数据管理
- `SysDictTypeService` — 字典类型管理
- `LoginAttemptService` — 登录失败次数追踪（5次失败锁定30分钟）
- `ApiAnalysisService` — 接口调用分析（菜单级全链路分析）
- `CommonToolsService` — 通用工具（Excel解析/FastExcel/PDFBox/POI文档转换）
- `EmailService` — 邮件发送服务（SMTP + HTML 富文本）
- `MusicService` — 音乐播放服务（MP3元数据扫描/播放记录/热门排行）
- `TechBlogArticleService` — 技术博客文章服务（多源Jsoup抓取/CRUD/进度追踪）
- `As400Service` — AS400 IBM i 系统对象查询
- `SysConfigService` — 系统配置管理
- `SysJobService` — 定时任务管理（Quartz）
- `SysFileService` — 文件管理（上传/下载/列表）
- `OnlineUserService` — 在线用户管理（自定义在线统计 + 心跳清理）
- `SysSlowQueryService` — 慢查询监控
- `SysPermissionManageService` — 权限管理（用户直接授权分配）
- `SysPermissionRequestService` — 权限申请审批
- `ChinaRegionService` — 中国行政区划
- `LoginLogService` — 登录日志
- `ExportLogService` — 导出审计
- `ExportService` — 导出引擎（Excel/PDF后端生成）
- `MessageTemplateService` — 通知模板管理
- `NotifyRecordService` — 通知发送记录
- `JobLogService` — 任务执行日志
- `HealthService` — 系统健康监控
- `DataScopeService` — 行级数据权限
- `SysIpRuleService` — IP黑白名单管理
- `SysUserFavoriteService` — 用户收藏管理
- `SysMessageService` — 站内消息管理
- `CaptchaService` — 验证码生成与校验
- `IServiceCategoryService` — IService 接口平台类别管理

### 3.7 控制层 (Controller)

#### 系统管理控制器（35+ 个）

| Controller | 路径前缀 | 说明 |
|-----------|---------|------|
| `AuthController` | `/auth` | 登录、注册、获取用户信息、获取路由菜单 |
| `CaptchaController` | `/auth/captcha` | 验证码生成 |
| `DashboardController` | `/dashboard` | 仪表盘统计数据 |
| `DashboardEnhancedController` | `/api/dashboard/enhanced` | 仪表盘增强统计 |
| `SysUserController` | `/sys/user` | 用户 CRUD、角色分配、密码重置 |
| `SysRoleController` | `/sys/role` | 角色 CRUD、菜单权限分配 |
| `SysMenuController` | `/sys/menu` | 菜单树查询、菜单 CRUD |
| `SysDeptController` | `/sys/dept` | 部门树查询、部门 CRUD |
| `SysConfigController` | `/sys/config` | 系统配置管理 |
| `SysLogController` | `/sys/log` | 操作日志查询 + 删除/批量删除 |
| `SysNoticeController` | `/content/notice` | 通知公告/待办事项 CRUD |
| `SysDictDataController` | `/sys/dict/data` | 字典数据管理 |
| `SysDictTypeController` | `/sys/dict/type` | 字典类型管理 |
| `SysOnlineController` | `/sys/online` | 在线用户列表/踢出 |
| `SysPermissionManageController` | `/sys/permission/manage` | 权限管理（用户直接授权分配/移除） |
| `SysPermissionRequestController` | `/sys/permission-request` | 权限申请提交/审批 |
| `SysSlowQueryController` | `/monitor/slow-query` | 慢查询监控列表/删除/清空 |
| `SysJobController` | `/monitor/job` | 定时任务管理 |
| `SysFileController` | `/system/file` | 文件管理 |
| `SysIpRuleController` | `/api/sys/ip-rule` | IP黑白名单管理 |
| `SysMessageController` | `/content/message` | 站内消息管理 |
| `SysUserFavoriteController` | `/api/favorite` | 用户收藏管理 |
| `ApiAnalysisController` | `/api/tool/analysis` | API 接口分析工具 |
| `ChinaRegionController` | `/api/tool/region` | 中国行政区划管理 |
| `As400Controller` | `/as400` | AS400 IBM i 系统对象查询 |
| `IServiceController` | `/api/as400/iservice` | iService 接口平台管理 |
| `CommonToolsController` | `/api/common-tools` | 常用工具（Excel解析/文档上传/转换） |
| `MusicController` | `/api/music` | 音乐播放 |
| `TechBlogController` | `/api/techblog` | 技术博客 |
| `SysLoginLogController` | `/api/monitor/login-log` | 登录日志查询/删除 |
| `SysExportLogController` | `/api/monitor/export-log` | 导出审计日志分页查询 |
| `SysJobLogController` | `/api/monitor/job-log` | 任务执行日志查询/删除 |
| `CacheManageController` | `/api/monitor/cache` | Caffeine 缓存管理 |
| `NotifyCenterController` | `/api/notify-center` | 通知中心（模板CRUD/发送/记录） |
| `DatabaseToolController` | `/api/tool/database` | 数据库工具（只读SQL/表结构/连接池） |
| `DevToolsController` | `/api/tool/dev` | 开发工具（UUID/时间戳/JSON/Base64/QR等） |
| `ExportController` | `/api/export` | 后端导出（Excel/PDF） |
| `GenController` | `/api/tool/gen` | 代码生成器 |
| `ImportController` | `/api/tool/import` | 批量数据导入 |
| `ApiDebugController` | `/api/tool/api-debug` | API 调试面板 |
| `BackupController` | `/api/tool/backup` | 数据库备份恢复 |
| `HealthController` | `/api/health` | 系统健康监控 |
| `LogAnalysisController` | `/api/monitor/log-analysis` | 日志分析 |
| `AnnouncementController` | `/api/announcement` | 系统公告管理 |

### 3.8 安全认证 (Sa-Token)

**框架**: Sa-Token 1.37.0 (Spring Boot 3 Starter)

**核心配置** (`SaTokenConfig.java`):
- Token 名称: `rx-admin-token`
- Token 有效期: 7 天 (604800 秒)
- Token 风格: 随机 UUID
- 是否允许并发登录: 是
- 是否打印日志: 开发环境打印

**路由拦截规则**:
```
/auth/login      → 放行（匿名访问）
/auth/register   → 放行（匿名访问）
/**              → 需要登录认证
```

**权限加载** (`StpInterfaceImpl.java`):
- 实现 `StpInterface` 接口
- `getPermissionList()` — 从数据库加载用户权限码（**双源合并**：角色权限 `sys_role_menu` ∪ 直接授权 `sys_user_menu`）
- `getRoleList()` — 从数据库加载用户角色标识

**密码加密**: 使用 Spring Security `BCryptPasswordEncoder`

### 3.9 公共模块 (common/)

| 子包 | 类 | 说明 |
|------|-----|------|
| `annotation/` | `DataScope` | 数据权限注解 |
| `annotation/` | `OperateLog` | `@OperateLog` 操作日志注解 |
| `result/` | `Result` | 统一响应封装，`Result.ok(data)` / `Result.fail(msg)` |
| `result/` | `PageResult` | 分页响应封装，`records`, `total`, `page`, `pageSize`。推荐 `of(IPage)` 代替废弃的 `of(long, long, List)` |
| `exception/` | `GlobalExceptionHandler` | 全局异常处理 (@RestControllerAdvice, 10种异常) |
| `constant/` | `PageConstants` | 分页常量 (默认页码/每页条数) |
| `utils/` | `CaptchaUtil` | 验证码生成工具 |
| `utils/` | `DataMaskUtil` | 数据脱敏工具 |
| `security/` | `IpFilter` | IP 黑白名单过滤器 |
| `security/` | `NotLoginFilter` | 未登录过滤器（区分 KICK_OUT 和过期） |
| `security/` | `ReplayAttackFilter` | 防重放攻击过滤器 (nonce + timestamp) |
| `security/` | `XssJacksonConfig` | XSS 防护 Jackson 配置 |
| `base/` | `BaseEntity` | 实体基类 (id, deleted, createTime, updateTime) |
| `base/` | `BaseCrudController` | CRUD 控制器基类（构造器注入 `protected final S baseService`） |
| `aspect/` | `OperateLogAspect` | 操作日志切面 (@Async 异步 + 敏感字段脱敏) |
| `handler/` | `AesTypeHandler` | AES 加密 MyBatis TypeHandler |
| `handler/` | `DataScopeInnerInterceptor` | 数据权限 MyBatis 拦截器 |
| `handler/` | `SlowQueryInterceptor` | 慢查询拦截器 (记录执行时间 > 阈值) |

### 3.10 框架层 (framework/)

| 子包 | 类 | 说明 |
|------|-----|------|
| `security/` | `SaTokenConfig` | Sa-Token 路由拦截器配置 |
| `security/` | `StpInterfaceImpl` | 权限/角色加载实现（双源合并） |
| `datasource/` | `PrimaryDataSourceConfig` | 主数据源配置 (rx_admin) |
| `datasource/` | `SecondDataSourceConfig` | 第二数据源配置 (rxusysadmin) |
| `datasource/` | `SecondDB` | `@SecondDB` 自定义注解 |
| `mybatis/` | `MybatisPlusConfig` | MyBatis Plus 分页插件 + 自动填充 |
| `mybatis/` | `MetaObjectHandlerConfig` | 元数据自动填充 (createTime/updateTime) |
| `async/` | `AsyncConfig` | 异步任务线程池配置 |
| `cache/` | `CacheConfig` | Caffeine 本地缓存配置 |
| `web/` | `CorsConfig` | CORS 跨域配置 |
| `web/` | `RateLimiterConfig` | Guava RateLimiter 限流配置 |

### 3.11 业务模块层 (modules/)

modules/ 目录下按业务领域划分子包，每个子包包含 DTO（数据传输对象）、VO（视图对象）、Convert（MapStruct 转换器）三个子层。

| 模块 | DTO 数量 | VO 数量 | Convert |
|------|---------|---------|---------|
| `system/user` | 3 (Create/Update/Query) | 1 | UserConvert |
| `system/role` | 3 | 1 | RoleConvert |
| `system/menu` | 3 | 1 | MenuConvert |
| `system/dept` | 3 | 1 | DeptConvert |
| `system/config` | 3 | 1 | ConfigConvert |
| `system/dict` | 6 (类型3+数据3) | 2 | DictConvert |
| `system/ipRule` | 3 | 1 | IpRuleConvert |
| `system/file` | 1 | 1 | FileConvert |
| `system/favorite` | 1 | 1 | FavoriteConvert |
| `monitor/job` | 3 | 1 | JobConvert |
| `monitor/log` | 0 | 1 | OperateLogConvert |
| `monitor/loginlog` | 0 | 1 | LoginLogConvert |
| `monitor/slowquery` | 0 | 1 | SlowQueryConvert |
| `content/notice` | 3 | 1 | NoticeConvert |
| `content/message` | 2 | 1 | MessageConvert |
| `as400/techblog` | 3 | 1 | TechBlogConvert |

**总计**: 16 个 Convert 接口 + 40 个 DTO + 17 个 VO = **73 个分层文件**

### 3.12 DTO/VO/Convert 分层规范

#### MapStruct 转换器规范

所有 Convert 接口统一使用以下注解配置：

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface XxxConvert {
    Entity toEntity(CreateDTO dto);
    void updateEntity(UpdateDTO dto, @MappingTarget Entity entity);
    VO toVO(Entity entity);
    List<VO> toVOList(List<Entity> list);
}
```

> **说明**: `unmappedTargetPolicy = ReportingPolicy.IGNORE` 告诉 MapStruct 忽略 DTO/VO 中不存在的目标字段（如 `id`、`deleted`、`createTime`、`updateTime` 等由数据库管理的字段），避免产生 "Unmapped target properties" 编译警告。

#### 分层职责

| 层级 | 职责 | 注解 |
|------|------|------|
| **DTO** | 请求参数封装，含 `@Valid` 校验 | `@Data` |
| **VO** | 响应视图对象，排除敏感字段（如 password） | `@Data` |
| **Convert** | MapStruct 编译期 DTO↔Entity↔VO 转换 | `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)` |

### 3.13 API 接口清单

#### 认证与系统管理接口

| 模块 | 路径 | 方法 |
|------|------|------|
| 登录 | `/auth/login` | POST |
| 注册 | `/auth/register` | POST |
| 用户信息 | `/auth/user/info` | GET |
| 路由菜单 | `/auth/menu/routes` | GET |
| 验证码 | `/auth/captcha` | GET |
| 心跳 | `/api/auth/ping` | GET |
| 用户管理 | `/sys/user/**` | GET/POST/PUT/DELETE |
| 角色管理 | `/sys/role/**` | GET/POST/PUT/DELETE |
| 菜单管理 | `/sys/menu/**` | GET/POST/PUT/DELETE |
| 部门管理 | `/sys/dept/**` | GET/POST/PUT/DELETE |

#### 监控与内容管理接口

| 模块 | 路径 | 方法 |
|------|------|------|
| 操作日志 | `/sys/log/page` | GET 分页查询 |
| 操作日志 | `/sys/log/{id}` | DELETE 删除 |
| 操作日志 | `/sys/log/batch` | DELETE 批量删除 |
| 通知公告 | `/content/notice/**` | CRUD + `/summary` + `/todo-count` |
| 消息中心 | `/content/message/**` | 分页/未读数/已读/发送/删除 |
| 字典管理 | `/sys/dict/**` | 标准 CRUD |
| 在线用户 | `/sys/online/list` | GET |
| 踢出用户 | `/sys/online/kick/{token}` | POST |
| 系统配置 | `/sys/config/**` | CRUD |
| 权限管理 | `/sys/permission/manage/**` | 分配/移除 |
| 权限申请 | `/sys/permission-request/**` | 提交/审批 |
| 慢查询 | `/monitor/slow-query/**` | 分页/删除/清空 |
| 定时任务 | `/monitor/job/**` | CRUD/暂停/执行 |
| 文件管理 | `/system/file/**` | 上传/下载/列表/删除 |
| IP规则 | `/api/sys/ip-rule/**` | CRUD |
| 用户收藏 | `/api/favorite/**` | CRUD/toggle |
| 仪表盘 | `/dashboard/stats` / `/api/dashboard/enhanced` | GET |
| 登录日志 | `/api/monitor/login-log/**` | 分页/删除 |
| 导出审计 | `/api/monitor/export-log/page` | GET |
| 任务日志 | `/api/monitor/job-log/**` | 分页/删除 |
| 缓存管理 | `/api/monitor/cache/**` | 列表/清除 |
| 通知中心 | `/api/notify-center/**` | 模板CRUD/发送/记录 |
| 数据库工具 | `/api/tool/database/**` | SQL执行/表结构/连接池 |
| 开发工具 | `/api/tool/dev/**` | UUID/时间戳/JSON/Base64/QR |
| 邮件发送 | `/api/tool/email/**` | 发送/模板/历史 |
| 系统健康 | `/api/health/**` | CPU/内存/磁盘/JVM/GC |
| 日志分析 | `/api/monitor/log-analysis/**` | 统计/图表数据 |
| 代码生成 | `/api/tool/gen/**` | 表列表/配置/生成 |
| 批量导入 | `/api/tool/import/**` | 上传/预览/执行 |
| API调试 | `/api/tool/api-debug/**` | 端点列表/发送请求 |
| 数据备份 | `/api/tool/backup/**` | 备份/列表/下载/还原 |
| 公告管理 | `/api/announcement/**` | CRUD |

---

## 4. 前端架构

### 4.1 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 (Composition API + `<script setup>`) | ^3.4.0 |
| 构建工具 | Vite | ^5.0.10 |
| 路由 | Vue Router 4 | ^4.2.5 |
| 状态管理 | Pinia | ^2.1.7 |
| HTTP 客户端 | Axios | ^1.6.2 |
| UI 组件库 | Element Plus | ^2.4.3 |
| 图标库 | @element-plus/icons-vue | ^2.3.1 |
| 图标库 | @fortawesome/vue-fontawesome | ^3.0.0-5 |
| 国际化 | Vue I18n | ^9.14.4 |
| CSS 预处理 | SCSS (sass-embedded) | ^1.69.5 |
| 进度条 | NProgress | ^0.2.0 |
| 图表 | ECharts | ^6.1.0 |
| Markdown 编辑 | md-editor-v3 | ^6.5.1 |
| Markdown 渲染 | marked + highlight.js | ^18.0.4 / ^11.11.1 |
| 流程图 | @vue-flow/core / @logicflow/core / @antv/x6 | — |
| Excel 导出 | exceljs / jspdf / html2canvas | 前端导出 |
| 语言 | JavaScript (ES Module) | — |

### 4.2 目录结构

```
ui/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.js                         # 应用入口
    ├── App.vue                         # 根组件（el-config-provider + router-view）
    ├── api/                            # API 请求层（50+ 模块）
    │   ├── request.js                  # Axios 实例封装
    │   ├── auth.js / user.js / role.js / menu.js / dept.js
    │   ├── dict.js / notice.js / log.js / online.js
    │   ├── dashboard.js / analysis.js / region.js
    │   ├── literature.js / honglou.js / sanguo.js / shuihu.js / xiyou.js
    │   ├── as400.js / iService.js / techBlog.js
    │   ├── music.js / commonTools.js / permission.js
    │   ├── job.js / file.js / slowQuery.js
    │   ├── message.js / favorite.js / ipRule.js
    │   ├── health.js / exportLog.js / loginLog.js / jobLog.js
    │   ├── cacheManage.js / notifyCenter.js / devTools.js / dbTool.js
    │   ├── gen.js / importData.js / logAnalysis.js
    │   ├── apiDebug.js / backup.js / announcement.js / export.js
    │   └── modules/                    # 模块化 API 聚合入口（v3 新增）
    │       ├── auth/index.js           # 认证模块
    │       ├── system/index.js         # 系统管理模块
    │       ├── monitor/index.js        # 系统监控模块
    │       ├── content/index.js        # 内容管理模块
    │       ├── tool/index.js           # 工具集模块
    │       ├── as400/index.js          # AS400 模块
    │       └── classics/index.js       # 四大名著模块
    ├── composables/                     # 组合式函数
    │   ├── useStorage.js               # localStorage 统一管理
    │   ├── useTablePage.js             # 通用表格分页
    │   ├── useTheme.js                 # 主题切换
	│   ├── useMenuI18n.js              # 菜单国际化
	│   ├── usePasswordStrength.js      # 密码强度检测
	│   ├── useTableHeight.js           # 表格高度自适应
	│   ├── useLayoutSettings.js        # 布局设置（含 ECharts 主题联动）
	│   └── useMarkdownRenderer.js      # Markdown 渲染器（marked + highlight.js 封装）
    ├── i18n/                            # 国际化
    │   ├── index.js
    │   └── lang/
    │       ├── zh-CN.js                # 中文语言包
    │       └── en-US.js                # 英文语言包
    ├── layout/                         # 布局组件
    │   ├── index.vue                   # 主布局
    │   ├── SubMenu.vue                 # 递归子菜单
    │   ├── TagsView.vue                # 标签页导航
    │   ├── SearchBox.vue               # 全局搜索框
    │   └── NoticePopover.vue           # 通知公告弹窗
    ├── components/                      # 公共组件
    │   ├── CommandPalette.vue          # Ctrl+K 命令面板
    │   ├── FavoriteStar.vue            # 收藏星标
    │   ├── FavoritesPanel.vue          # 收藏面板
    │   ├── AnnouncementPopup.vue       # 公告弹窗
    │   └── ExportButton/               # 导出按钮组件
    ├── router/
    │   ├── index.js                    # 路由配置（动态路由）
    │   └── componentMap.js             # 组件映射表（50+ 条目）
    ├── stores/                         # Pinia 状态管理
    │   ├── user.js                     # 用户状态
    │   └── tags.js                     # 标签页状态
    ├── styles/                         # 全局样式
    │   ├── global.scss                 # 全局样式
    │   ├── variables.scss              # CSS 变量（亮/暗双主题）
    │   └── themes.scss                 # 5套主题色
    ├── utils/
    │   ├── request.js                  # Axios 封装
    │   └── index.js                    # 工具函数
    └── views/                          # 页面视图（50+ 个页面）
        ├── login/index.vue
        ├── dashboard/
        │   ├── index.vue
        │   └── knowledgeGraph/index.vue
        ├── profile/index.vue
        ├── system/
        │   ├── user/index.vue / role/index.vue / menu/index.vue
        │   ├── dept/index.vue / config/index.vue / file/index.vue
        │   └── ipRule/index.vue
        ├── tool/
        │   ├── dict/index.vue / region/index.vue
        │   ├── analysis/index.vue / docs/index.vue / standards/index.vue
        │   ├── gen/index.vue / importData/index.vue / apiDebug/index.vue
        │   ├── backup/index.vue / dbConsole/index.vue / devTools/index.vue
        │   ├── excelParser/index.vue / docConverter/index.vue / docUpload/index.vue
        │   ├── flowChart/index.vue / musicPlayer/index.vue
        │   └── emailSender/index.vue
        ├── content/
        │   ├── notice/index.vue / message/index.vue
        │   └── notify-center/index.vue
        ├── monitor/
        │   ├── log/index.vue / online/index.vue / job/index.vue
        │   ├── slow-query/index.vue / health/index.vue
        │   ├── logAnalysis/index.vue / login-log/index.vue
        │   ├── export-log/index.vue / job-log/index.vue
        │   └── cache-manage/index.vue
        ├── permission/request/index.vue
        ├── as400/
        │   ├── objects/index.vue / iservice/index.vue
        │   └── techblog/ (index.vue + detail.vue)
        └── classics/
            ├── honglou/ (poems + characters + relations)
            ├── sanguo/ (poems + characters)
            ├── shuihu/ (poems + chapters)
            ├── xiyou/ (poems + characters + events)
            └── literature/ (index.vue + works/index.vue)
```

### 4.3 路由设计（完全动态路由）

路由配置文件: `ui/src/router/index.js` + `ui/src/router/componentMap.js`

项目已实现**完全动态路由**，`constantRoutes` 只保留 Login 和 Layout 空壳，所有业务路由在登录后由 `generateDynamicRoutes()` 从后端菜单树动态注入。

**componentMap 当前映射项（50+ 个）**：
仪表盘/个人/系统管理（用户/角色/菜单/部门/配置/文件/IP规则）/ 系统工具（字典/行政区划/接口分析/项目文档/开发规范/Excel解析/文档转换/文档上传/流程图×3/音乐播放器/代码生成/批量导入/API调试/数据备份/数据库工具/开发工具/邮件发送）/ AS400管理（对象浏览/IService/技术博客列表+详情）/ 内容管理（通知公告/消息中心/通知中心）/ 系统监控（操作日志/在线用户/定时任务/慢查询/健康/日志分析/登录日志/导出审计/任务日志/缓存管理）/ 权限申请 / 四大名著×4 / 历代文学×2 / 知识图谱

### 4.4 状态管理 (Pinia)

#### `useUserStore` (`stores/user.js`)

| 状态 | 类型 | 说明 |
|------|------|------|
| `token` | String | 登录 Token（`useStorage` 持久化到 localStorage） |
| `userInfo` | Object | 用户信息（id, username, nickname, avatar 等） |
| `roles` | Array | 用户角色列表 |
| `permissions` | Array | 用户权限码列表 |
| `menus` | Array | 用户菜单路由树 |

#### `useTagsStore` (`stores/tags.js`)

| 状态 | 类型 | 说明 |
|------|------|------|
| `visitedViews` | Array | 已访问标签页列表 |
| `cachedViews` | Array | 需要缓存的组件名列表 |

### 4.5 API 请求层

`utils/request.js` 封装 Axios 实例，统一处理：
- `baseURL: '/api'` + Vite proxy
- 请求拦截器：自动附加 Token + NProgress + 可选 `_skipNProgress` 跳过进度条
- 响应拦截器：统一错误处理 + KICK_OUT 强制下线遮罩
- 会话心跳：每 10 秒 `GET /api/auth/ping` 检测是否被踢出
- 超时/轮询间隔等可配参数通过 `import.meta.env.VITE_xxx` 读取

### 4.6 页面视图清单

| 模块 | 文件路径 | 说明 |
|------|----------|------|
| **登录** | `login/index.vue` | 登录/注册 Tab 切换 |
| **仪表盘** | `dashboard/index.vue` | ECharts 统计图表 + 暗黑适配 |
| **知识图谱** | `dashboard/knowledgeGraph/index.vue` | ECharts 关系图展示系统实体关联 |
| **个人信息** | `profile/index.vue` | 用户信息查看与编辑 |
| **用户管理** | `system/user/index.vue` | 搜索+表格+分页+弹窗+角色分配 |
| **角色管理** | `system/role/index.vue` | 搜索+表格+分页+弹窗+菜单权限分配 |
| **菜单管理** | `system/menu/index.vue` | 树形表格 |
| **部门管理** | `system/dept/index.vue` | 树形表格 |
| **系统配置** | `system/config/index.vue` | 系统参数CRUD |
| **IP规则** | `system/ipRule/index.vue` | 黑白名单管理 |
| **文件管理** | `system/file/index.vue` | 上传/下载/列表 |
| **字典管理** | `tool/dict/index.vue` | 类型+数据双Tab |
| **行政区划** | `tool/region/index.vue` | 省市区三级管理 |
| **接口分析** | `tool/analysis/index.vue` | API 调用链路分析 |
| **项目文档** | `tool/docs/index.vue` | Markdown 渲染架构文档 |
| **开发规范** | `tool/standards/index.vue` | Markdown 渲染规范文档 |
| **代码生成** | `tool/gen/index.vue` | 三步向导（选表→配置→预览） |
| **批量导入** | `tool/importData/index.vue` | 上传→预览→执行 |
| **API调试** | `tool/apiDebug/index.vue` | 类 Postman 面板 |
| **数据备份** | `tool/backup/index.vue` | mysqldump 备份管理 |
| **数据库工具** | `tool/dbConsole/index.vue` | SQL控制台+表结构+连接池 |
| **开发工具** | `tool/devTools/index.vue` | JSON/Base64/UUID/时间戳/QR/正则 |
| **邮件发送** | `tool/emailSender/index.vue` | SMTP 邮件+模板 |
| **Excel解析** | `tool/excelParser/index.vue` | 上传解析Excel |
| **文档转换** | `tool/docConverter/index.vue` | PDF↔Word互转 |
| **文档共享** | `tool/docUpload/index.vue` | 文档上传共享 |
| **流程图** | `tool/flowChart/index.vue` | 三引擎（vue-flow/LogicFlow/AntV X6） |
| **音乐播放器** | `tool/musicPlayer/index.vue` | MP3流式播放+歌单 |
| **通知公告** | `content/notice/index.vue` | 通知/公告/待办管理 |
| **消息中心** | `content/message/index.vue` | 站内消息+时间轴 |
| **通知中心** | `content/notify-center/index.vue` | 消息模板+发送记录 |
| **操作日志** | `monitor/log/index.vue` | 表格+详情+批量删除 |
| **在线用户** | `monitor/online/index.vue` | 在线列表+踢出 |
| **定时任务** | `monitor/job/index.vue` | CRUD+暂停+执行 |
| **慢查询** | `monitor/slow-query/index.vue` | 列表+删除+清空 |
| **健康监控** | `monitor/health/index.vue` | CPU/内存/磁盘/JVM/GC |
| **日志分析** | `monitor/logAnalysis/index.vue` | ECharts 可视化 |
| **登录日志** | `monitor/login-log/index.vue` | 分页+删除 |
| **导出审计** | `monitor/export-log/index.vue` | 分页查询 |
| **任务日志** | `monitor/job-log/index.vue` | 分页+删除 |
| **缓存管理** | `monitor/cache-manage/index.vue` | 列表+清除 |
| **权限申请** | `permission/request/index.vue` | 菜单树+申请+审批 |
| **AS400对象** | `as400/objects/index.vue` | IBM i 库对象浏览 |
| **IService** | `as400/iservice/index.vue` | 接口平台管理 |
| **技术博客** | `as400/techblog/index.vue` | 文章列表CRUD |
| **博客详情** | `as400/techblog/detail.vue` | Markdown编辑 |
| **红楼诗词/人物/关系** | `classics/honglou/**` | 3个子页面 |
| **三国诗词/人物** | `classics/sanguo/**` | 2个子页面 |
| **水浒诗词/章节** | `classics/shuihu/**` | 2个子页面 |
| **西游诗词/人物/事件** | `classics/xiyou/**` | 3个子页面 |
| **历代文学** | `classics/literature/**` | 作者/朝代/体裁/分类+作品 |

### 4.7 权限申请与审批

用户权限 = 角色权限（`sys_role_menu`） ∪ 直接授权权限（`sys_user_menu`）

#### 强制下线倒计时遮罩

当用户被管理员踢出时，下一次 API 请求触发 KICK_OUT → 全屏遮罩（5秒倒计时） → 清除认证数据 → 返回登录页。

### 4.8 待办事项提醒

顶栏铃铛图标整合通知公告、待办事项、消息中心未读消息，通过 6 个分类 Tab（未读/全部/通知/公告/待办/消息）区分。每 15 秒轮询刷新未读数。

---

## 5. 布局与样式

### 5.1 整体布局

经典后台三件套：`el-container` → 侧边栏 220px（可折叠至64px）+ 顶栏 50px + 标签栏 36px + 内容区

### 5.2 UI 组件库

Element Plus 2.4.3 全量引入 + 中文语言包，暗黑模式由 `element-plus/theme-chalk/dark/css-vars.css` 提供。

### 5.3 主题系统

双主题 CSS 变量方案（`:root` / `html.dark`），50+ CSS 变量覆盖页面背景/文字/边框/侧边栏/顶栏/标签/搜索/通知等。5 套预设主题色通过 `data-theme` 切换（蓝/绿/紫/橙/青），设计令牌 `--rx-primary` 驱动 ECharts 主题运行时读取。

### 5.4 全局样式

`.page-container` / `.search-bar` / `.table-container` / `.page-pagination` 通用类

### 5.5 响应式与动画

侧边栏折叠 220px↔64px，Canvas 自适应，时间轴响应式。CSS Transition 微交互覆盖 9 个元素，Vue Transition 覆盖搜索下拉和页面切换。

### 5.6 收藏夹

星形图标收藏/取消收藏，localStorage 持久化，按收藏时间排序。

### 5.7 全站命令面板 (`Ctrl+K`)

模糊搜索全站菜单和快捷操作，键盘导航，支持最近访问记录。

---

## 6. 常用工具模块

### 6.1 音乐播放器

mp3agic 提取元数据 + HTTP Range 流式播放，播放统计 + 热门排行。

### 6.2 技术博客

Jsoup 多源抓取 + CRUD + Markdown 渲染 + 抓取进度追踪。

### 6.3 Excel/PDF/Word 文档工具

FastExcel 解析 Excel、PDFBox PDF↔Word 互转、POI 文档操作。

### 6.4 流程图编辑器

三引擎集成：@vue-flow/core / LogicFlow 2.x / AntV X6。

### 6.5 API 接口分析

菜单级前后端交互链路自动分析。

### 6.6 中国行政区划

省市区三级数据管理，级联选择器，下级数据保护。

### 6.7 AS400 IService 接口平台

IBM i 五层结构：类别 → 条目 → 列/示例/参数。

### 6.8 邮件发送

基于 Spring Boot Mail (SMTP)，支持 HTML 富文本模板，发送历史记录查询。

### 6.9 数据库工具

只读 SQL 执行控制台 + 表结构浏览 + HikariCP 连接池状态监控。

### 6.10 开发工具

UUID 生成、时间戳转换、JSON 格式化与压缩、Base64 编解码、二维码生成、正则表达式测试。

---

## 7. 四大名著模块

| 名著 | 人物 | 诗词 | 关系 | 章节/事件 | 数据库 |
|------|------|------|------|-----------|--------|
| 红楼梦 | 54 | 待统计 | 99 | — | rxusysadmin |
| 三国演义 | 待统计 | 待统计 | — | — | rxusysadmin |
| 水浒传 | — | 待统计 | — | 待统计 | rxusysadmin |
| 西游记 | 待统计 | 待统计 | — | 81难 | rxusysadmin |

---

## 8. 菜单功能详解

（略，参见 v3.0.0 版本文档。）


## 9. 构建与部署

```bash
# 后端
mvn clean package -DskipTests
java -jar target/rx-admin-1.0.0.jar

# 前端
cd ui
npm run build     # 生产构建 → ui/dist/
npm run dev       # 开发模式 → localhost:5173
```

## 10. 项目搭建与新增模块指南

独立文档：[rxadmin-setup.md](./rxadmin-setup.md)

## 12. 路由动态化

完全动态路由：`constantRoutes`仅外壳，登录后从后端菜单树生成路由。

## 13. 启动命令

```bash
# 后端
mvn spring-boot:run

# 前端
cd ui && npm run dev
```

访问 `http://localhost:5173`（前端）或 `http://localhost:8088/doc.html`（API文档）。

## 14. 安全机制

- **在线用户追踪**: ConcurrentHashMap + Sa-Token 交叉校验
- **踢出逻辑**: `StpUtil.kickoutByTokenValue()` → NotLoginFilter → 前端遮罩倒计时
- **心跳机制**: 每 10 秒 `GET /api/auth/ping`
- **验证码**: CaptchaUtil 生成 + 登录校验
- **XSS 防护**: XssJacksonConfig + Jackson 字符转义
- **防重放**: nonce + timestamp 校验 (ReplayAttackFilter)
- **敏感数据加密**: AES TypeHandler (email/phone) + BCrypt (password)
- **SSE 实时推送**: Server-Sent Events

## 15. 密码策略

BCrypt 加密，最少 6 位。

## 16. i18n 国际化

Vue I18n + el-config-provider 无刷新切换，300+ 条目。

## 17. v2.0 新增功能模块

（参见 v3.0.0 版本文档。）

## 18. Git 版本控制

独立文档：[rxadmin-git.md](./rxadmin-git.md)

## 19. 待实施增强建议

（参见 v3.0.0 版本文档。）

## 20. 待修复项（生产发布前）

（参见 v3.0.0 版本文档。）

---

> **文档维护**: 本文档为 RX Admin 项目技术架构说明书，随项目迭代持续更新。
> **历史版本**: v3.0.0 (2026-06-10) → v3.1.0 (2026-06-13): Spring Boot 3.5.15 + MapStruct unmappedTargetPolicy + EmailService + 前端新模块补齐 → v3.2.0 (2026-06-15): 主题色统一 + Sentry v10 + 字体自托管 + sass 去重 + 构建优化 + useMarkdownRenderer