# RX Admin 通用管理系统 — 项目技术架构文档

> **版本**: 2.1.0 | **更新日期**: 2026-06-06 | **文档类型**: 技术架构说明书

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术栈总览](#2-技术栈总览)
3. [后端架构](#3-后端架构)
   - [3.1 项目坐标与启动类](#31-项目坐标与启动类)
   - [3.2 包结构](#32-包结构)
   - [3.3 数据源配置](#33-数据源配置)
   - [3.4 实体层 (Entity)](#34-实体层-entity)
   - [3.5 数据访问层 (Mapper)](#35-数据访问层-mapper)
   - [3.6 服务层 (Service)](#36-服务层-service)
   - [3.7 控制层 (Controller)](#37-控制层-controller)
   - [3.8 安全认证 (Sa-Token)](#38-安全认证-sa-token)
   - [3.9 公共模块](#39-公共模块)
   - [3.10 API 接口清单](#310-api-接口清单)
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
- [7. 四大名著模块](#7-四大名著模块)
- [8. 菜单功能详解](#8-菜单功能详解)
   - [8.1 仪表盘](#81-仪表盘)
   - [8.2 系统管理](#82-系统管理)
     - [8.2.1 用户管理](#821-用户管理)
     - [8.2.2 角色管理](#822-角色管理)
     - [8.2.3 菜单管理](#823-菜单管理)
     - [8.2.4 部门管理](#824-部门管理)
     - [8.2.5 系统配置](#825-系统配置)
     - [8.2.6 IP 黑白名单](#826-ip-黑白名单)
     - [8.2.7 定时任务](#827-定时任务)
     - [8.2.8 文件管理](#828-文件管理)
   - [8.3 系统工具](#83-系统工具)
     - [8.3.1 字典管理](#831-字典管理)
     - [8.3.2 行政区划](#832-行政区划)
     - [8.3.3 接口分析](#833-接口分析)
     - [8.3.4 项目文档](#834-项目文档)
     - [8.3.5 开发规范](#835-开发规范)
     - [8.3.6 代码生成](#836-代码生成)
     - [8.3.7 批量导入](#837-批量导入)
     - [8.3.8 API 调试](#838-api-调试)
     - [8.3.9 数据备份](#839-数据备份)
     - [8.3.10 数据库工具](#8310-数据库工具)
     - [8.3.11 开发工具](#8311-开发工具)
     - [8.3.12 代码生成器](#8312-代码生成器)
     - [8.3.13 批量导入](#8313-批量导入)
     - [8.3.14 API 调试](#8314-api-调试)
   - [8.4 内容管理](#84-内容管理)
     - [8.4.1 通知公告](#841-通知公告)
     - [8.4.2 消息中心](#842-消息中心)
     - [8.4.3 通知中心](#843-通知中心)
   - [8.5 办公工具](#85-办公工具)
     - [8.5.1 Excel 解析](#851-excel-解析)
     - [8.5.2 文档格式转换](#852-文档格式转换)
     - [8.5.3 文档上传共享](#853-文档上传共享)
     - [8.5.4 流程图编辑器](#854-流程图编辑器)
     - [8.5.5 邮件发送](#855-邮件发送)
   - [8.6 音乐播放器](#86-音乐播放器)
   - [8.7 系统监控](#87-系统监控)
     - [8.7.1 操作日志](#871-操作日志)
     - [8.7.2 在线用户](#872-在线用户)
     - [8.7.3 慢查询监控](#873-慢查询监控)
     - [8.7.4 健康监控](#874-健康监控)
     - [8.7.5 日志分析](#875-日志分析)
     - [8.7.6 登录日志](#876-登录日志)
     - [8.7.7 导出审计](#877-导出审计)
     - [8.7.8 任务执行日志](#878-任务执行日志)
     - [8.7.9 缓存管理](#879-缓存管理)
   - [8.8 权限申请](#88-权限申请)
- [9. 构建与部署](#9-构建与部署)
- [10. 项目搭建与新增模块指南（独立文档）](./rxadmin-setup.md)
- [12. 路由动态化](#12-路由动态化)
- [13. 启动命令](#13-启动命令)
- [14. 安全机制（在线用户/踢出/XSS/防重放/加密脱敏）](#14-安全机制在线用户踢出xss防重放加密脱敏)
  - [14.1 在线用户追踪](#141-在线用户追踪)
  - [14.2 踢出逻辑](#142-踢出逻辑)
  - [14.3 心跳机制](#143-心跳机制)
  - [14.4 验证码机制](#144-验证码机制)
  - [14.5 XSS 防护](#145-xss-防护)
  - [14.6 API 防重放](#146-api-防重放)
  - [14.7 敏感数据加密与脱敏](#147-敏感数据加密与脱敏)
  - [14.8 SSE 实时推送](#148-sse-实时推送)
- [15. 密码策略（P1）](#15-密码策略p1)
- [16. i18n 国际化](#16-i18n-国际化)
- [17. v2.0 新增功能模块 (2026-06-05)](#17-v20-新增功能模块-2026-06-05)
- [18. Git 版本控制功能集成方案（独立文档）](./rxadmin-git.md)
- [19. 待实施增强建议](#19-待实施增强建议)
  - [19.1 v2.1 已实施增强速查](#191-v21-已实施增强速查)
  - [19.2 安全类增强（待实施）](#192-安全类增强待实施)
  - [19.3 日志与监控增强（待实施）](#193-日志与监控增强待实施)
  - [19.4 任务调度增强（部分已完成）](#194-任务调度增强部分已完成)
  - [19.5 数据可视化与报表（部分已完成）](#195-数据可视化与报表部分已完成)
  - [19.6 全站搜索（基础已实现，扩展待定）](#196-全站搜索基础已实现扩展待定)
  - [19.7 多租户扩展与实施优先级矩阵（独立文档）](./rxadmin-git.md)
- [20. 待修复项（生产发布前）](#20-待修复项生产发布前)
  - [20.1 安全配置（P0）](#201-安全配置p0)
  - [20.2 架构增强（P2）](#202-架构增强p2)
  - [20.3 工程化（P3）](#203-工程化p3)
- [21. 文档与项目差异分析](#21-文档与项目差异分析)
  - [21.1 文档描述与代码一致的项](#211-文档描述与代码一致的项)
  - [21.2 文档描述与实际代码不符的项](#212-文档描述与实际代码不符的项)
  - [21.3 文档缺失模块描述](#213-文档缺失模块描述)
  - [21.4 文档维护建议](#214-文档维护建议)
  - [21.5 v1.4.0 已完成但未纳入优化审查的模块](#215-v140-已完成但未纳入优化审查的模块)

---

## 1. 项目概述

**RX Admin** 是一个基于 **Spring Boot 3 + Vue 3** 的通用后台管理系统，采用前后端分离架构。系统包含完整的用户/角色/菜单/部门/字典等 RBAC 权限管理功能，支持权限申请与审批流程，四大名著（红楼梦、三国演义、水浒传、西游记）的经典文化数据管理模块，AS400 IBM i 系统对象浏览与 IService 接口管理，音乐播放器，技术博客多源抓取，以及丰富的常用工具集（文档转换、Excel解析、流程图编辑等）。

### 核心功能模块

| 模块 | 说明 |
|------|------|
| **认证授权** | 登录/注册/Token 管理，基于 Sa-Token |
| **系统管理** | 用户、角色、菜单、部门 CRUD，RBAC 权限模型 |
| **系统工具** | 字典管理、行政区划、接口分析、数据库工具（SQL控制台+表结构+连接池）、开发工具（JSON/Base64/UUID/时间戳/二维码/正则）、代码生成、批量导入、API调试、数据备份 |
| **系统监控** | 操作日志、在线用户、登录日志、导出审计、任务执行日志、缓存管理、慢查询监控、健康监控、日志分析 |
| **内容管理** | 通知公告、消息中心、通知中心（消息模板+发送记录） |
| **权限管理** | 用户直接授权、权限申请与审批（双源合并 RBAC） |
| **系统配置** | 系统参数配置管理 |
| **定时任务** | Quartz 定时任务管理（新增/修改/暂停/执行） |
| **系统监控** | 操作日志、在线用户、慢查询监控 |
| **文件管理** | 文件上传/下载/列表管理 |
| **AS400 IService** | IBM i 接口平台管理（类别/条目/列/示例/参数） |
| **仪表盘** | 统计概览（ECharts 可视化图表，支持暗黑模式） |
| **常用工具** | Excel解析、PDF↔Word互转、文档上传共享、流程图编辑器（3引擎）、数据分析工具、中国行政区划 |
| **音乐播放器** | MP3 流式播放、歌单管理、播放记录统计、热门排行 |
| **技术博客** | 多源文章抓取（Jsoup）、分类浏览、Markdown渲染、用户投稿 |
| **API 分析** | 菜单级前后端交互链路自动分析 |
| **历代文学** | 历代文学作品管理（作者/朝代/体裁/内容分类） |
| **四大名著** | 红楼梦/三国/水浒/西游的人物、诗词、关系、章节等数据管理 |
| **AS400 管理** | IBM i (AS400) 系统库对象浏览与查询 |
| **国际化** | 中/英文双语切换，菜单/表单/提示全量翻译 |
| **暗黑模式** | 全站亮色/暗色双主题，CSS 变量 + ECharts 暗黑适配 |
| **系统健康监控** | CPU/内存/磁盘/JVM/GC 实时监控，ECharts 可视化，10秒自动刷新 |
| **IP黑白名单** | IP 规则 CRUD，黑名单/白名单/关闭三模式切换，sys_config 持久化 |
| **站内消息中心** | 用户间私信、系统通知，未读计数，el-timeline 时间轴展示 |
| **快捷收藏夹** | 侧边栏收藏面板 + 页面星标组件，toggle 切换，localStorage 缓存 |
| **系统公告弹窗** | 登录后自动弹窗公告，一次一条，localStorage 去重 |
| **代码生成器** | 三步向导（选表→配置→预览），生成 Entity/Mapper/Service/Controller/Vue/API |
| **批量数据导入** | Excel/CSV 上传→预览→执行，三步向导，含校验与错误报告 |
| **日志可视化分析** | 4 统计卡片 + ECharts 柱状图/饼图/折线图，按小时/类型/趋势分析 |
| **API 调试面板** | 左右分栏（端点列表+请求面板），类似 Postman，扫描 RequestMappingHandlerMapping |
| **数据库备份恢复** | mysqldump 备份到 backups/ 目录，列表管理/下载/还原 |
| **Git 版本管理** | Web 查看仓库状态/提交历史/分支/文件差异，支持 Pull 拉取 |
| **全局命令搜索** | Ctrl+K 唤起 Spotlight 风格面板，菜单+最近访问+快捷操作 |
| **多主题色系统** | 5 套预设主题色（蓝/绿/紫/橙/青），CSS 变量 + data-theme 切换 |

---

## 2. 技术栈总览

| 层级 | 技术 | 版本 |
|------|------|------|
| **运行环境** | Java / Node.js | Java 17 / Node 18+ |
| **后端框架** | Spring Boot | 3.2.0 |
| **ORM** | MyBatis Plus | 3.5.5 |
| **安全认证** | Sa-Token | 1.37.0 |
| **API 文档** | Knife4j (OpenAPI 3) | 4.4.0 |
| **数据库** | MySQL | 8.x |
| **密码加密** | Spring Security Crypto (BCrypt) | — |
| **AOP 日志** | Spring AOP + @Async | — |
| **限流** | Guava RateLimiter | 33.0.0-jre |
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
| **CSS 预处理** | SCSS (Dart Sass) | ^1.69.5 |
| **流程图引擎** | @vue-flow/core / @logicflow/core / @antv/x6 | ^1.48.2 / ^2.2.3 / ^3.1.7 |
| **Markdown 编辑器** | md-editor-v3 | ^6.5.1 |
| **图标库** | @fortawesome/vue-fontawesome（Font Awesome） | ^3.0.0-5 |
| **进度条** | NProgress | ^0.2.0 |
| **Markdown 渲染** | marked + highlight.js | ^18.0.4 / ^11.11.1 |
| **Markdown 样式** | github-markdown-css | ^5.9.0 |

---

## 3. 后端架构

### 3.1 项目坐标与启动类

```xml
<groupId>com.rx</groupId>
<artifactId>rx-admin</artifactId>
<version>1.0.0</version>
<name>RX Admin（通用管理系统后端）</name>
```

**启动类**: `com.rx.admin.RxAdminApplication`（排除 `DataSourceAutoConfiguration`，手动配置双数据源）

**配置文件**: `src/main/resources/application.yml`

```yaml
server:
  port: 8088

spring:
  application:
    name: rx-admin

# 双数据源：rx_admin（系统管理） + rxusysadmin（业务数据）
# Sa-Token：内存模式，token 有效期 86400 秒
# AS400：连接 pub400.com（可选）
```

### 3.2 包结构

```
com.rx.admin
├── RxAdminApplication.java           # Spring Boot 启动类
├── common/                            # 公共模块
│   ├── BaseEntity.java               # 实体基类（id, deleted, createTime, updateTime）
│   ├── Result.java                    # 统一响应封装 {code, msg, data}
│   ├── PageResult.java               # 分页响应封装 {list, total, page, pageSize}
│   ├── GlobalExceptionHandler.java   # 全局异常处理 (@RestControllerAdvice, 10种异常)
│   ├── OperateLog.java               # @OperateLog 操作日志注解
│   └── OperateLogAspect.java         # 操作日志 AOP 切面（@Async异步 + 参数脱敏）
├── config/                            # 配置模块
│   ├── AsyncConfig.java              # 异步任务配置（支持 @Async）
│   ├── CorsConfig.java               # CORS 跨域配置
│   ├── SaTokenConfig.java            # Sa-Token 路由拦截器
│   ├── StpInterfaceImpl.java         # 权限/角色加载实现
│   ├── MybatisPlusConfig.java        # MyBatis Plus 分页插件 & 自动填充
│   ├── PrimaryDataSourceConfig.java  # 主数据源 (rx_admin)
│   ├── SecondDataSourceConfig.java   # 第二数据源 (rxusysadmin)
│   ├── SecondDB.java                 # @SecondDB 自定义注解
│   └── RateLimiterConfig.java        # Guava RateLimiter 限流配置
├── entity/                            # 实体模块（9 个系统实体 + 1 个 VO）
│   ├── SysUser.java                  # 系统用户
│   ├── SysRole.java                  # 系统角色
│   ├── SysMenu.java                  # 系统菜单
│   ├── SysDept.java                  # 部门
│   ├── SysLog.java                   # 操作日志
│   ├── SysNotice.java               # 通知公告
│   ├── SysDictData.java              # 字典数据
│   ├── SysDictType.java              # 字典类型
│   ├── As400ObjectVO.java            # AS400 对象 VO
│   ├── SysConfig.java               # 系统配置
│   ├── SysJob.java                   # 定时任务
│   ├── SysFile.java                  # 文件管理
│   ├── SysSlowQuery.java            # 慢查询记录
│   ├── SysPermissionRequest.java     # 权限申请
│   ├── SysUserMenu.java             # 用户直接授权
│   ├── CaptchaVO.java               # 验证码 VO
│   ├── MusicSong.java               # 音乐歌曲实体
│   ├── TechBlogArticle.java         # 技术博客文章
│   ├── IService*.java               # iService 接口平台实体
│   └── classics/                     # 四大名著 + 历代文学实体（16 个）
├── controller/                        # 控制器模块（30 个 Controller）
├── service/                           # 服务层（40+ 个 Service，含实现类）
└── mapper/                            # 数据访问层（36+ 个 Mapper）
```

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
| `MusicSong` | `sys_songs` | 音乐歌曲 | title, artist, album, duration, filePath, coverUrl |
| `TechBlogArticle` | `sys_tech_blog_articles` | 技术博客文章 | title, content, summary, source, category, coverUrl |
| `SysLoginLog` | `sys_login_log` | 登录日志 | username, ip, browser, os, status(1=成功/0=失败), failReason, loginTime |
| `SysExportLog` | `sys_export_log` | 导出审计日志 | username, exportType(excel/pdf), exportTitle, recordCount, fileName, ip, exportTime |
| `SysMessageTemplate` | `sys_message_template` | 通知模板 | name, code(UNIQUE), titleTemplate, contentTemplate, channels, status |
| `SysNotifyRecord` | `sys_notify_record` | 通知发送记录 | templateId, channel, receiver, title, content, status, errorMsg, retryCount |
| `SysJobLog` | `sys_job_log` | 任务执行日志 | jobId, jobName, startTime, endTime, durationMs, status, errorMessage |

**中间表**:
- `sys_user_role` — 用户角色关联
- `sys_role_menu` — 角色菜单关联
- `sys_user_menu` — 用户直接授权关联（个性化权限，不通过角色）
- `sys_permission_request` — 权限申请审批表
- `sys_tech_blog_articles` — 技术博客文章
- `sys_songs` — 音乐歌曲
- `sys_play_records` — 音乐播放记录
- `sys_shared_files` — 共享文档记录
- `sys_iservice_category/item/column/example/parameter` — iService 接口平台相关表

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

所有 Mapper 继承 MyBatis Plus `BaseMapper<T>`，自动获得 CRUD 能力。共 **36+ 个 Mapper**。

**主数据源 Mapper** (`com.rx.admin.mapper`，10 个):

| Mapper | 对应实体 | 说明 |
|--------|---------|------|
| `SysUserMapper` | SysUser | 用户数据访问 |
| `SysRoleMapper` | SysRole | 角色数据访问 |
| `SysMenuMapper` | SysMenu | 菜单数据访问 |
| `SysDeptMapper` | SysDept | 部门数据访问 |
| `SysLogMapper` | SysLog | 日志数据访问 |
| `SysNoticeMapper` | SysNotice | 通知公告/待办数据访问 |
| `SysDictDataMapper` | SysDictData | 字典数据访问 |
| `SysDictTypeMapper` | SysDictType | 字典类型访问 |
| `SysUserRoleMapper` | — | 用户角色关联 |
| `SysRoleMenuMapper` | — | 角色菜单关联 |

**第二数据源 Mapper** (`com.rx.admin.mapper.classics`，16 个，使用 `@SecondDB`):

| Mapper | 对应实体 |
|--------|---------|
| `HonglouCharacterMapper` | HonglouCharacter |
| `HonglouCharacterRelationMapper` | HonglouCharacterRelation |
| `HonglouPoemMapper` | HonglouPoem |
| `SanguoCharacterMapper` | SanguoCharacter |
| `SanguoPoemMapper` | SanguoPoem |
| `ShuihuChapterMapper` | ShuihuChapter |
| `ShuihuPoemMapper` | ShuihuPoem |
| `XiyouCharacterMapper` | XiyouCharacter |
| `XiyouEventMapper` | XiyouEvent |
| `XiyouPoemMapper` | XiyouPoem |
| `LiteraryWorkMapper` | LiteraryWork |
| `AuthorMapper` | Author |
| `DynastyMapper` | Dynasty |
| `GenreMapper` | Genre |
| `ContentCategoryMapper` | ContentCategory |
| `ChinaRegionMapper` | ChinaRegion |

### 3.6 服务层 (Service)

服务层基于 MyBatis Plus `IService<T>` / `ServiceImpl<M, T>` 模式。共 **40+ 个 Service**（25+ 系统管理 + 16 个 classics）。

**系统管理服务**（20+ 个）:
- `AuthService` — 登录认证、注册、Token 签发
- `SysUserService` — 用户 CRUD + 角色分配 + 密码修改
- `SysRoleService` — 角色 CRUD + 菜单权限分配
- `SysMenuService` — 菜单 CRUD + 树形构建 + 路由生成
- `SysDeptService` — 部门 CRUD + 树形结构
- `SysLogService` — 操作日志记录与查询（含删除/批量删除）
- `SysNoticeService` — 通知公告/待办事项管理（支持 category 分类查询）
- `SysDictDataService` — 字典数据管理
- `SysDictTypeService` — 字典类型管理
- `LoginAttemptService` — 登录失败次数追踪（5次失败锁定30分钟）
- `ApiAnalysisService` — 接口调用分析（菜单级全链路分析）
- `CommonToolsService` — 通用工具（Excel解析/FastExcel/PDFBox/POI文档转换）
- `MusicService` — 音乐播放服务（MP3元数据扫描/播放记录/热门排行）
- `TechBlogArticleService` — 技术博客文章服务（多源Jsoup抓取/CRUD/进度追踪）
- `As400Service` — AS400 IBM i 系统对象查询
- `IServiceService` — iService 接口平台管理（类别/条目/列/示例/参数）
- `SysConfigService` — 系统配置管理
- `SysJobService` — 定时任务管理（Quartz）
- `SysFileService` — 文件管理（上传/下载/列表）
- `SysOnlineService` — 在线用户管理（自定义在线统计）
- `SysSlowQueryService` — 慢查询监控
- `SysPermissionManageService` — 权限管理（用户直接授权分配）
- `SysPermissionRequestService` — 权限申请审批（提交/审批/待办列表）
- `ChinaRegionService` — 中国行政区划（省市区数据管理）
- `LoginLogService` — 登录日志（自动记录 + 分页查询 + 删除/批量删除）
- `ExportLogService` — 导出审计（自动记录导出操作）
- `MessageTemplateService` — 通知模板管理（CRUD）
- `NotifyRecordService` — 通知发送记录（查询/重发）
- `JobLogService` — 任务执行日志（记录/查询/删除）
- `DataScopeService` — 行级数据权限（全部/本部门/本部门及下级/自定义）
- `LibraryNotFoundException` — AS400 库未找到异常

**四大名著 + 历代文学服务** (位于 `com.rx.admin.service.classics`，16 个):
- 每个对应一个实体，提供标准 CRUD + 分页查询

### 3.7 控制层 (Controller)

#### 系统管理控制器（22+ 个）

| Controller | 路径前缀 | 说明 |
|-----------|---------|------|
| `AuthController` | `/auth` | 登录、注册、获取用户信息、获取路由菜单 |
| `CaptchaController` | `/auth/captcha` | 验证码生成 |
| `DashboardController` | `/dashboard` | 仪表盘统计数据 |
| `SysUserController` | `/sys/user` | 用户 CRUD、角色分配、密码重置 |
| `SysRoleController` | `/sys/role` | 角色 CRUD、菜单权限分配 |
| `SysMenuController` | `/sys/menu` | 菜单树查询、菜单 CRUD |
| `SysDeptController` | `/sys/dept` | 部门树查询、部门 CRUD |
| `SysConfigController` | `/sys/config` | 系统配置管理 |
| `SysLogController` | `/sys/log` | 操作日志查询 + 删除/批量删除 |
| `SysNoticeController` | `/content/notice` | 通知公告/待办事项 CRUD + 分类统计 |
| `SysDictDataController` | `/sys/dict/data` | 字典数据管理 |
| `SysDictTypeController` | `/sys/dict/type` | 字典类型管理 |
| `SysOnlineController` | `/sys/online` | 在线用户列表/踢出 |
| `SysPermissionManageController` | `/sys/permission/manage` | 权限管理（用户直接授权分配/移除） |
| `SysPermissionRequestController` | `/sys/permission-request` | 权限申请提交/审批 |
| `SysSlowQueryController` | `/monitor/slow-query` | 慢查询监控列表/删除/清空 |
| `SysJobController` | `/monitor/job` | 定时任务管理（CRUD/暂停/执行） |
| `SysFileController` | `/system/file` | 文件管理（上传/下载/列表/删除） |
| `ApiAnalysisController` | `/api/tool/analysis` | API 接口分析工具（分析菜单前后端调用链） |
| `ChinaRegionController` | `/api/tool/region` | 中国行政区划管理（省市区三级数据） |
| `As400Controller` | `/as400` | AS400 IBM i 系统对象查询 |
| `IServiceController` | `/api/as400/iservice` | iService 接口平台管理 |
| `CommonToolsController` | `/api/common-tools` | 常用工具（Excel解析/文档上传/PDF↔Word互转） |
| `MusicController` | `/api/music` | 音乐播放（扫描/流式播放/统计/Range请求） |
| `TechBlogController` | `/api/techblog` | 技术博客（多源抓取/CRUD/分类/进度） |
| `SysLoginLogController` | `/api/monitor/login-log` | 登录日志查询/删除/批量删除 |
| `SysExportLogController` | `/api/monitor/export-log` | 导出审计日志分页查询（只读） |
| `SysJobLogController` | `/api/monitor/job-log` | 任务执行日志查询/删除/批量删除 |
| `CacheManageController` | `/api/monitor/cache` | Caffeine 缓存列表查看/清除/清除全部 |
| `NotifyCenterController` | `/api/notify-center` | 通知中心（模板CRUD，发送通知，记录查询/重发） |
| `DatabaseToolController` | `/api/tool/database` | 数据库工具（只读SQL执行，表结构，连接池状态） |
| `DevToolsController` | `/api/tool/dev` | 开发工具（UUID生成，时间戳转换，JSON格式化） |
| `DashboardEnhancedController` | `/api/dashboard/enhanced` | 仪表盘增强统计（登录统计/导出统计/操作排行） |

#### 四大名著 + 历代文学控制器

| Controller | 路径前缀 | 说明 |
|-----------|---------|------|
| `HonglouController` | `/classics/honglou` | 红楼诗词/人物/关系 |
| `SanguoController` | `/classics/sanguo` | 三国诗词/人物 |
| `ShuihuController` | `/classics/shuihu` | 水浒诗词/章节 |
| `XiyouController` | `/classics/xiyou` | 西游诗词/人物/事件 |
| `LiteratureController` | `/classics/literature` | 历代文学/作者/朝代/体裁管理 |

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
- 前端 `getUserInfo()` API 同样双源合并，确保按钮权限与 API 鉴权一致

**密码加密**: 使用 Spring Security `BCryptPasswordEncoder`

**安全防护**:
- **登录限流**: 基于 Guava `RateLimiter`，每个 IP 每秒最多 3 次请求，超限返回 429
- **登录失败锁定**: `LoginAttemptService` 追踪失败次数，同一用户名连续失败 5 次后锁定 30 分钟
- **操作日志脱敏**: `OperateLogAspect` 自动过滤 `password`、`token`、`secret` 等敏感参数


#### 在线用户追踪与踢出

**在线用户服务** (`OnlineUserService.java`):
- 使用 `ConcurrentHashMap<String, Map<String, Object>>` 维护在线用户列表
- `userLoggedIn()` — 登录时记录 token ↔ 用户映射，同一用户自动去重（保留最新一条）
- `userLoggedOut()` — 退出时移除记录
- `getOnlineUsers()` / `getOnlineCount()` — 查询前自动调用 `cleanupStaleEntries()`
- `cleanupStaleEntries()` — 与 Sa-Token 交叉校验，移除已过期/被踢的 session

**踢出流程**:
1. 管理员在在线用户页点击"踢出"
2. `SysOnlineController.kickOut()` → `StpUtil.kickoutByTokenValue(token)`
3. Sa-Token 标记该 token 为 KICK_OUT 状态
4. 后端从 `onlineMap` 移除该记录

**被踢用户实时通知** (`NotLoginFilter.java`):
- Servlet Filter 级别（`@Order(HIGHEST_PRECEDENCE + 1)`）捕获 `NotLoginException`
- 区分 KICK_OUT 和普通过期，返回 `HTTP 401 + {"code":401,"message":"KICK_OUT"}`
- 解决 `@RestControllerAdvice` 无法处理拦截器层异常的问题

**会话心跳检测** (`GET /api/auth/ping`):
- 新增轻量端点，受 SaInterceptor 保护，无 DB 查询
- 前端每 10 秒发送一次请求
- 被踢用户的心跳在 10 秒内触发 KICK_OUT 响应

**前端强制下线倒计时** (`request.js`):
- 响应拦截器检测 `data?.message === "KICK_OUT"` 后创建全屏遮罩
- 显示 "已被强制下线" + "X 秒后返回登录页面"（5 秒倒计时）
- 同时清除 Pinia store 和 localStorage 的认证数据
- 倒计时结束调用 `router.push("/login")`

### 3.9 公共模块

| 类 | 说明 |
|----|------|
| `BaseEntity` | 实体基类，包含 `id`, `deleted`（逻辑删除）, `createTime`, `updateTime`，配合 MyBatis Plus 自动填充 |
| `Result` | 统一响应封装，`Result.ok(data)` / `Result.fail(msg)` |
| `PageResult` | 分页响应封装，包含 `list`, `total`, `page`, `pageSize`。提供 `of(total, page, size, records)` 和 `of(Page<T>)` 工厂方法 |
| `GlobalExceptionHandler` | `@RestControllerAdvice` 全局异常处理，涵盖 10 种异常（401/403/400/404/405/415/数据约束冲突/参数校验等） |
| `OperateLog` | `@OperateLog` 自定义注解，标记需要记录操作日志的方法 |
| `OperateLogAspect` | AOP 切面实现，拦截 `@OperateLog` 注解，`@Async` 异步保存日志，`sanitizeParams()` 对 password/token/secret 等敏感字段脱敏 |
| `RateLimiterConfig` | Guava `RateLimiter` 配置，`ConcurrentHashMap<String, RateLimiter>` 按 IP 区分，登录接口每秒 3 次限制 |

### 3.10 API 接口清单

#### 认证接口 (`/auth`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 用户登录 |
| POST | `/auth/register` | 用户注册 |
| POST | `/auth/logout` | 用户退出 |
| GET | `/auth/user-info` | 获取当前用户信息（含角色、权限） |
| PUT | `/auth/update-profile` | 更新个人信息（邮箱格式+唯一性校验、手机号格式校验、密码强度校验，空白自动转 NULL） |
| GET | `/auth/routers` | 获取用户路由菜单（树形） |
| GET | `/auth/ping` | 会话心跳检测 |

#### 系统管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| **用户管理** | `/sys/user` | |
| GET | `/sys/user/page` | 分页查询用户 |
| GET | `/sys/user/{id}` | 获取用户详情 |
| POST | `/sys/user` | 新增用户 |
| PUT | `/sys/user` | 更新用户 |
| DELETE | `/sys/user/{ids}` | 批量删除用户 |
| PUT | `/sys/user/password` | 修改密码 |
| **角色管理** | `/sys/role` | |
| GET | `/sys/role/list` | 角色列表 |
| GET | `/sys/role/{id}` | 角色详情 |
| POST | `/sys/role` | 新增角色 |
| PUT | `/sys/role` | 更新角色 |
| DELETE | `/sys/role/{ids}` | 批量删除 |
| PUT | `/sys/role/{id}/menus` | 分配菜单权限 |
| **菜单管理** | `/sys/menu` | |
| GET | `/sys/menu/tree` | 菜单树 |
| GET | `/sys/menu/{id}` | 菜单详情 |
| POST | `/sys/menu` | 新增菜单 |
| PUT | `/sys/menu` | 更新菜单 |
| DELETE | `/sys/menu/{id}` | 删除菜单 |
| **部门管理** | `/sys/dept` | |
| GET | `/sys/dept/tree` | 部门树 |
| GET | `/sys/dept/{id}` | 部门详情 |
| POST | `/sys/dept` | 新增部门 |
| PUT | `/sys/dept` | 更新部门 |
| DELETE | `/sys/dept/{id}` | 删除部门 |

#### 监控与内容管理接口

| 模块 | 路径 | 方法 |
|------|------|------|
| 操作日志 | `/sys/log/page` | GET 分页查询 |
| 操作日志 | `/sys/log/{id}` | DELETE 删除单条日志 |
| 操作日志 | `/sys/log/batch` | DELETE 批量删除日志 |
| 通知公告 | `/content/notice` | GET/POST/PUT/DELETE CRUD + `/summary` 分类统计 + `/todo-count` 待办数量 |
| 消息中心 | `/content/message/page` | GET 分页查询消息（管理员可看全部/按 `userId` 筛选，普通用户只看自己的） |
| 消息中心 | `/content/message/unread-count` | GET 获取未读消息数 |
| 消息中心 | `/content/message/read/{id}` | PUT 标记单条已读 |
| 消息中心 | `/content/message/read-all` | PUT 全部已读 |
| 消息中心 | `/content/message/{id}` | DELETE 删除单条消息（普通用户仅能删自己的） |
| 消息中心 | `/content/message/admin/update` | PUT 管理员修改消息 |
| 消息中心 | `/content/message/admin/send` | POST 管理员发送消息 |
| 字典类型 | `/sys/dict/type` | GET/POST/PUT/DELETE 标准 CRUD |
| 字典数据 | `/sys/dict/data` | GET/POST/PUT/DELETE 标准 CRUD |
| 在线用户 | `/sys/online/list` | GET 在线用户列表 |
| 踢出用户 | `/sys/online/kick/{token}` | POST 踢出指定用户 |
| 系统配置 | `/sys/config/page` | GET 分页查询配置 |
| 新增配置 | `/sys/config` | POST 新增配置 |
| 修改配置 | `/sys/config` | PUT 修改配置 |
| 删除配置 | `/sys/config/{id}` | DELETE 删除配置 |
| 权限分配 | `/sys/permission/manage/assign` | POST 为用户分配权限 |
| 权限移除 | `/sys/permission/manage/remove` | DELETE 移除用户权限 |

| 慢查询分页 | `/monitor/slow-query/page` | GET 慢查询列表 |

| 删除慢查询 | `/monitor/slow-query/{id}` | DELETE 删除记录 |

| 清空慢查询 | `/monitor/slow-query/clear` | DELETE 清空所有 |

| 定时任务分页 | `/monitor/job/page` | GET 任务列表 |

| 新增定时任务 | `/monitor/job` | POST 新增 |

| 修改定时任务 | `/monitor/job` | PUT 修改 |

| 删除定时任务 | `/monitor/job/{id}` | DELETE 删除 |

| 切换任务状态 | `/monitor/job/status/{id}` | PUT 启用/暂停 |

| 执行一次任务 | `/monitor/job/run/{id}` | PUT 单次执行 |

| 文件分页 | `/system/file/page` | GET 文件列表 |

| 上传文件 | `/system/file/upload` | POST 上传 |

| 下载文件 | `/system/file/{id}` | GET 下载 |

| 删除文件 | `/system/file/{id}` | DELETE 删除 |
| 仪表盘 | `/dashboard/stats` | GET 统计概览 |

#### AS400 接口 (`/as400`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/as400/objects/{library}` | 按库名查询对象列表 |
| GET | `/as400/objects` | 批量查询多个库对象（默认 A7RXUZZ1, A7RXUZZ2, A7RXUZZB） |

#### 四大名著接口 (`/classics/{book}`)

| 书籍 | 子路径 | 操作 |
|------|--------|------|
| 红楼梦 | `/poems/page`, `/characters/page`, `/relations/page` | 分页查询 |
| 三国演义 | `/poems/page`, `/characters/page` | 分页查询 |
| 水浒传 | `/poems/page`, `/chapters/page` | 分页查询 |
| 西游记 | `/poems/page`, `/characters/page`, `/events/page` | 分页查询 |

#### 权限审批与待办接口

| 方法 | 路径 | 说明 |
|------|------|------|
| **权限申请** | `/sys/permission-request` | |
| POST | `/sys/permission-request` | 提交权限申请 |
| GET | `/sys/permission-request/pending` | 获取待审批列表（admin） |
| GET | `/sys/permission-request/my` | 获取我的申请列表 |
| PUT | `/sys/permission-request/{id}/approve` | 审批通过 |
| PUT | `/sys/permission-request/{id}/reject` | 审批拒绝 |
| **待办通知** | `/content/notice` | |
| GET | `/content/notice/todo-count` | 获取待办事项数量 |
| GET | `/content/notice/summary` | 获取各分类通知数量概览 |
| GET | `/content/notice/page?category=todo` | 按分类筛选通知列表 |

#### IService 接口平台 (/api/as400/iservice)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/as400/iservice/categories` | 获取类别列表（含条目数） |
| GET | `/api/as400/iservice/category/{id}` | 获取类别详情 |
| GET | `/api/as400/iservice/items` | 获取条目列表（支持按类别筛选） |
| GET | `/api/as400/iservice/item/{id}` | 获取条目详情（含列/示例/参数） |

#### 常用工具接口 (/api/common-tools)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/common-tools/excel/parse` | 上传并解析 Excel 文件 |
| POST | `/api/common-tools/document/upload` | 上传文档到共享目录 |
| GET | `/api/common-tools/document/list` | 分页查询已上传文档 |
| DELETE | `/api/common-tools/document/{id}` | 删除上传的文档 |
| POST | `/api/common-tools/convert/pdf-to-word` | PDF 转 Word |
| POST | `/api/common-tools/convert/word-to-pdf` | Word 转 PDF |

#### 音乐播放接口 (/api/music)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/music/scan` | 扫描音乐文件夹，导入歌曲 |
| GET | `/api/music/songs` | 获取歌曲列表（支持关键词搜索） |
| GET | `/api/music/song/{id}` | 获取歌曲详情（含歌词） |
| POST | `/api/music/play/{id}` | 记录播放次数 |
| GET | `/api/music/stats` | 播放统计概览 |
| GET | `/api/music/recent` | 最近播放记录 |
| GET | `/api/music/top` | 热门歌曲排行 |
| GET | `/api/music/stream/{id}` | MP3 流式播放（支持 Range 进度条拖动） |
| GET | `/api/music/folder` | 获取音乐文件夹路径 |

#### 技术博客接口 (/api/techblog)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/techblog/articles` | 分页查询文章（支持关键词/分类/来源筛选） |
| GET | `/api/techblog/articles/{id}` | 获取文章详情 |
| POST | `/api/techblog/articles` | 新增文章 |
| PUT | `/api/techblog/articles/{id}` | 更新文章 |
| DELETE | `/api/techblog/articles/{id}` | 删除文章 |
| DELETE | `/api/techblog/articles/batch` | 批量删除文章 |
| GET | `/api/techblog/categories` | 获取所有分类标签 |
| GET | `/api/techblog/recent` | 获取最近文章 |
| POST | `/api/techblog/fetch` | 触发指定来源的文章抓取 |
| GET | `/api/techblog/progress` | 查看抓取进度 |

#### 行政区划接口 (/api/tool/region)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/tool/region/page` | 分页查询行政区划 |
| GET | `/api/tool/region/children` | 查询下级行政区划（级联） |
| GET | `/api/tool/region/search` | 搜索行政区划 |
| POST | `/api/tool/region` | 新增 |
| PUT | `/api/tool/region` | 修改 |
| DELETE | `/api/tool/region/{id}` | 删除（有下级时拒绝） |

#### API 接口分析 (/api/tool/analysis)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/tool/analysis/menus` | 获取所有可分析的菜单列表 |
| GET | `/api/tool/analysis/analyze` | 分析指定菜单的交互链路 |
| GET | `/api/tool/analysis/search` | 模糊搜索菜单 |
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
| CSS 预处理 | SCSS (Dart Sass) | ^1.69.5 |
| 进度条 | NProgress | ^0.2.0 |
| 自动导入 | unplugin-auto-import | ^0.17.3 |
| 组件注册 | unplugin-vue-components | ^0.26.0 |
| 图表 | ECharts | ^6.1.0 |
| Markdown 渲染 | marked + highlight.js | ^18.0.4 / ^11.11.1 |
| Markdown 样式 | github-markdown-css | ^5.9.0 |
| 语言 | JavaScript (ES Module) | — |

### 4.2 目录结构

```
ui/
├── index.html                          # 入口 HTML
├── package.json                        # 项目配置与依赖
├── vite.config.js                      # Vite 构建配置
└── src/
    ├── main.js                         # 应用入口（注册插件、全局样式、路由）
    ├── App.vue                         # 根组件（仅 <router-view />）
    ├── api/                            # API 请求层（26 个模块）
    │   ├── auth.js                     # 认证接口
    │   ├── user.js                     # 用户管理
    │   ├── role.js                     # 角色管理
    │   ├── menu.js                     # 菜单管理
    │   ├── dept.js                     # 部门管理
    │   ├── dict.js                     # 字典管理
    │   ├── notice.js                   # 通知公告
    │   ├── log.js                      # 操作日志
    │   ├── online.js                   # 在线用户
    │   ├── dashboard.js                # 仪表盘
    │   ├── analysis.js                 # 接口分析
    │   ├── region.js                   # 行政区划
    │   ├── literature.js               # 历代文学
    │   ├── honglou.js                  # 红楼梦
    │   ├── sanguo.js                   # 三国演义
    │   ├── shuihu.js                   # 水浒传
    │   ├── xiyou.js                    # 西游记
    │   ├── as400.js                    # AS400 管理
    │   ├── iService.js                 # AS400 IService 接口平台
    │   ├── techBlog.js                 # 技术博客
    │   ├── music.js                    # 音乐播放器
    │   ├── commonTools.js              # 常用工具
    │   ├── permission.js               # 权限管理/申请
    │   ├── job.js                      # 定时任务
    │   ├── file.js                     # 文件管理
    │   ├── slowQuery.js                # 慢查询监控
    │   └── ...                         # 持续扩展
    ├── composables/
│   ├── useStorage.js               # 统一 localStorage 管理（命名空间 rx_admin_*）
│   ├── useTablePage.js             # 通用表格分页 Composable（搜索/分页/排序/列配置/高度适配）
│   ├── useTheme.js                 # 亮/暗主题切换
│   ├── useMenuI18n.js              # 菜单国际化翻译
│   └── usePasswordStrength.js      # 密码强度检测
    ├── i18n/                            # 国际化模块
    │   ├── index.js
    │   └── lang/
    │       ├── zh-CN.js                # 中文语言包（300+ 条目）
    │       └── en-US.js                # 英文语言包
    ├── layout/                         # 布局组件
│   ├── index.vue                   # 主布局（~644行，拆分为子组件）
│   ├── SubMenu.vue                 # 递归子菜单组件
│   ├── TagsView.vue                # 标签页导航栏
│   ├── SearchBox.vue               # 全局搜索框（输入+下拉+键盘导航）
│   └── NoticePopover.vue           # 通知公告弹窗（分类Tab+列表+已读标记）
    ├── router/
    │   ├── index.js                    # 路由配置（动态路由）
    │   └── componentMap.js             # 组件映射表（path → component 懒加载）
    ├── stores/                         # Pinia 状态管理
    │   ├── user.js                     # 用户状态
    │   └── tags.js                     # 标签页状态
    ├── styles/                         # 全局样式
    │   ├── global.scss                 # 全局样式与通用类
    │   └── variables.scss              # CSS 变量（亮/暗双主题）
    ├── utils/
    │   ├── request.js                  # Axios 封装（请求/响应拦截器）
    │   └── index.js                    # 工具函数（时间格式化等）
    └── views/                          # 页面视图（42+ 个页面）
        ├── login/index.vue             # 登录/注册页
        ├── dashboard/index.vue         # 仪表盘首页（ECharts 图表）
        ├── profile/index.vue           # 个人信息页
        ├── system/                     # 系统管理
        │   ├── user/index.vue          # 用户管理
        │   ├── role/index.vue          # 角色管理
        │   ├── menu/index.vue          # 菜单管理
        │   ├── config/index.vue        # 系统配置
        │   ├── dept/index.vue          # 部门管理
        │   └── file/index.vue          # 文件管理
        ├── tool/                        # 系统工具
        │   ├── dict/index.vue          # 字典管理
        │   ├── region/index.vue        # 行政区划
        │   ├── analysis/index.vue      # 接口分析
        │   ├── docs/index.vue          # 项目文档（Markdown 渲染）
        │   ├── standards/index.vue     # 开发规范（Markdown 渲染）
        │   ├── excelParser/index.vue   # Excel 解析
        │   ├── docConverter/index.vue   # 文档格式转换
        │   ├── docUpload/index.vue     # 文档上传共享
        │   ├── flowChart/index.vue     # 流程图编辑器（三引擎）
        │   └── musicPlayer/index.vue   # 音乐播放器
        ├── content/notice/index.vue    # 通知公告
        ├── as400/
        │   ├── objects/index.vue       # AS400 对象浏览
        │   ├── iservice/index.vue      # iService 接口平台
        │   └── techblog/               # 技术博客
        │       ├── index.vue           # 文章列表
        │       └── detail.vue          # 文章详情
        ├── permission/request/index.vue # 权限申请
        ├── monitor/                    # 系统监控
        │   ├── log/index.vue           # 操作日志
        │   ├── online/index.vue        # 在线用户
        │   ├── job/index.vue           # 定时任务
        │   └── slow-query/index.vue    # 慢查询监控
        └── classics/                   # 四大名著 + 历代文学
            ├── honglou/
            │   ├── poems/index.vue     # 红楼诗词
            │   ├── characters/index.vue # 红楼人物
            │   └── relations/index.vue # 人物关系（Canvas 力导向图）
            ├── sanguo/
            │   ├── poems/index.vue     # 三国诗词
            │   └── characters/index.vue # 三国人物
            ├── shuihu/
            │   ├── poems/index.vue     # 水浒诗词
            │   └── chapters/index.vue  # 水浒章节
            ├── xiyou/
            │   ├── poems/index.vue     # 西游诗词
            │   ├── characters/index.vue # 西游人物
            │   └── events/index.vue    # 八十一难（时间轴视图）
            └── literature/
                ├── index.vue           # 历代文学（作者/朝代/体裁/分类 Tab 管理）
                └── works/index.vue     # 文学作品管理
```

### 4.3 路由设计（完全动态路由 ✅）

路由配置文件: `ui/src/router/index.js` + `ui/src/router/componentMap.js`

#### 路由架构

项目已实现**完全动态路由**，`constantRoutes` 只保留 Login 和 Layout 空壳，所有业务路由在登录后由 `generateDynamicRoutes()` 从后端菜单树动态注入。

```javascript
// constantRoutes — 仅外壳
{ path: '/login', ... },
{ path: '/', component: Layout, children: [] }

// generateDynamicRoutes() — 递归遍历后端菜单树
// 匹配 componentMap.js 中的映射 → router.addRoute('Layout', route)
```

**路由注册流程**：
1. 用户登录 → `userStore.login()` 预加载 `fetchRouters()`（持久化到 localStorage）
2. `beforeEach` 守卫检查 `dynamicRoutesAdded` 标记
3. 首次进入时调用 `generateDynamicRoutes(userStore.menus)` 批量注册路由
4. 路由 `name` 使用 `componentMap` 中定义的英文名（确保 `keep-alive` 缓存匹配）

**componentMap 当前映射项（42 个）**：
- 仪表盘/个人/系统管理（用户/角色/菜单/部门/配置/文件）/ 系统工具（字典/行政区划/接口分析/项目文档/开发规范/Excel解析/文档转换/文档上传/流程图/音乐播放器）/ AS400管理（对象浏览/IService/技术博客列表+详情）/ 内容管理（通知公告）/ 系统监控（操作日志/在线用户/定时任务/慢查询）/ 权限申请 / 四大名著（红楼梦3个/三国2个/水浒2个/西游记3个）/ 历代文学（2个）

**路由特性**:
- 动态路由：所有业务路由由后端 `sys_menu` 表驱动，通过 `router.addRoute` 注册
- 新增菜单无需修改 `router/index.js`，仅需后端插入菜单 + 前端 `componentMap.js` 追加一行
- `keep-alive` 缓存：通过 `include` 属性按 `cachedViews` 数组控制，缓存 key 使用英文 name
- 标签页 `affix`：Dashboard 仪表盘为固定标签，不可关闭
- TagsView 标签前显示与左侧菜单一致的图标

### 4.4 状态管理 (Pinia)

#### `useUserStore` (`stores/user.js`)

| 状态 | 类型 | 说明 |
|------|------|------|
| `token` | String | 登录 Token（通过 `useStorage` 持久化到 localStorage） |
| `userInfo` | Object | 用户信息（id, username, nickname, avatar 等） |
| `roles` | Array | 用户角色列表 |
| `permissions` | Array | 用户权限码列表 |
| `menus` | Array | 用户菜单路由树 |

| 关键方法 | 说明 |
|----------|------|
| `login(credentials)` | 登录 → 存储 Token → 获取用户信息 |
| `getUserInfo()` | 获取用户信息、角色、权限 |
| `generateRoutes()` | 根据后端菜单生成前端路由 |
| `logout()` | 退出登录 → 清除状态 → 跳转登录页 |
| `resetToken()` | 清除 Token |

#### `useTagsStore` (`stores/tags.js`)

| 状态 | 类型 | 说明 |
|------|------|------|
| `visitedViews` | Array | 已访问标签页列表 |
| `cachedViews` | Array | 需要缓存的组件名列表 |

| 关键方法 | 说明 |
|----------|------|
| `addView(view)` | 添加标签页 |
| `delView(view)` | 关闭标签页 |
| `delOtherViews(view)` | 关闭其他标签页 |
| `delAllViews()` | 关闭所有标签页 |
| `refreshView(view)` | 刷新标签页（从缓存中移除再添加） |

### 4.5 API 请求层

#### Axios 封装 (`utils/request.js`)

```javascript
// 请求拦截器
- 添加 Token 到请求头: { Authorization: 'rx-admin-token' }
- NProgress 进度条启动（config._skipNProgress 为 true 时跳过）
- 后台轮询（心跳/通知/进度检测等）必须传 _skipNProgress: true

// 响应拦截器
- 统一处理 code !== 200 的错误
- Token 过期自动跳转登录页
- NProgress 进度条完成

// localStorage 统一管理
- 使用 useStorage composable 管理 token/userInfo/roles/permissions/menus
- 命名空间前缀: rx_admin_*
- clearAuthData() 一键清除所有认证数据
```

#### 环境变量与配置管理

前端通过 `.env.development` / `.env.production` 管理运行时配置，避免硬编码魔法数字：

| 变量 | 默认值 | 控制对象 |
|------|--------|---------|
| `VITE_API_REQUEST_TIMEOUT` | 15000 | 通用 API 超时（ms） |
| `VITE_AS400_REQUEST_TIMEOUT` | 60000 | AS400 请求超时（ms） |
| `VITE_HEARTBEAT_INTERVAL` | 10000/30000 | 心跳间隔（ms） |
| `VITE_NOTICE_POLL_INTERVAL` | 60000/30000 | 通知轮询间隔（ms） |
| `VITE_FETCH_PROGRESS_POLL_INTERVAL` | 10000/2000 | 抓取进度轮询（ms） |
| `VITE_KICKOUT_COUNTDOWN` | 5 | 踢下线倒计时（秒） |
| `VITE_MUSIC_SEARCH_DEBOUNCE_MS` | 300 | 搜索防抖（ms） |
| `VITE_FLOWCHART_INIT_RETRY_MS` | 50 | 流程图初始轮询（ms） |
| `VITE_TABLE_ROW_HEIGHT` | 48 | 表格行高 |
| `VITE_DEFAULT_PAGE_SIZE` | 10 | 默认分页大小 |
| `VITE_CLASSICS_TABLE_ROW_HEIGHT` | 44 | classics 行高 |

后端通过 `application.yml` + `@Value` 注入管理可配置参数：

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `app.captcha.expire-ms` | 300000 | 验证码过期 |
| `app.captcha.cleanup-interval-ms` | 60000 | 验证码清理间隔 |
| `app.replay.time-window-ms` | 300000 | 防重放窗口 |
| `app.replay.max-nonce-cache` | 10000 | nonce 缓存上限 |
| `app.techblog.request-delay-ms` | 1000 | 抓取请求延迟 |
| `app.techblog.page-timeout-ms` | 15000 | 列表页抓取超时 |
| `app.slow-query.threshold-ms` | 2000 | 慢查询阈值 |
```

**响应数据格式化优化** (`utils/index.js`):
- `formatResponseData`: 从全量深拷贝优化为浅层 record 级处理，仅对 `createTime`/`updateTime` 等时间字段做 `T → 空格` 替换，大幅提升大数据量返回时的性能

**API 模块示例** (`api/auth.js`):

```javascript
import request from '@/utils/request'

export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

export function getUserInfo() {
  return request({ url: '/auth/user/info', method: 'get' })
}

export function getRouters() {
  return request({ url: '/auth/menu/routes', method: 'get' })
}
```


#### 强制下线倒计时遮罩

当用户被管理员踢出时，下一次 API 请求会收到 `{ code: 401, message: "KICK_OUT" }`，触发全屏遮罩：

- 显示 "已被强制下线" + "X 秒后返回登录页面"（5 秒倒计时，全屏半透明覆盖）
- 遮罩期间用户无法进行任何操作
- 倒计时结束后 `clearAuthData()` + `router.push("/login")`
- `clearAuthData()` 同时清除 localStorage 和 Pinia store 的响应式状态，确保路由守卫正确放行

#### 会话心跳检测

`request.js` 模块加载时自动启动 `setInterval`，每 10 秒发送 `GET /api/auth/ping`：

- 有 token 时正常发请求，用于检测是否被踢出
- 无 token 时跳过本次（定时器保持运行，用户重新登录后自动恢复）
- 被踢出时下一次心跳触发 `NotLoginFilter` → 401 + `KICK_OUT` → 遮罩
- 心跳失败自动 `.catch(() => {})` 静默处理，不影响页面

### 4.6 页面视图清单

| 页面 | 文件 | 功能描述 |
|------|------|----------|
| **登录/注册** | `login/index.vue` | 渐变色背景 + 卡片式表单，支持登录/注册切换 |
| **仪表盘** | `dashboard/index.vue` | 统计卡片 + ECharts 图表（朝代/体裁/难度/排行等），支持暗黑模式 |
| **个人信息** | `profile/index.vue` | 用户信息展示与修改。后端 `AuthService.updateProfile()` 带完整校验：邮箱格式+唯一性、手机号11位格式、密码强度（字母开头+含数字+至少6位），空白值自动转 NULL。变更记录写入消息中心 |
| **用户管理** | `system/user/index.vue` | 搜索栏 + 表格 + 分页 + 新增/编辑弹窗 + 角色分配 |
| **角色管理** | `system/role/index.vue` | 搜索栏 + 表格 + 分页 + 新增/编辑弹窗 + 菜单权限分配 |
| **菜单管理** | `system/menu/index.vue` | 树形表格（目录/菜单/按钮），支持 FontAwesome 图标选择 |
| **部门管理** | `system/dept/index.vue` | 树形表格 |
| **字典管理** | `tool/dict/index.vue` | 字典类型 + 字典数据 Tab 切换 |
| **行政区划** | `tool/region/index.vue` | 行政区划树形管理 |
| **接口分析** | `tool/analysis/index.vue` | API 调用统计与可视化 |
| **项目文档** | `tool/docs/index.vue` | Markdown 渲染技术架构文档（marked + highlight.js） |
| **开发规范** | `tool/standards/index.vue` | Markdown 渲染开发规范文档（含侧边栏目录导航） |
| **通知公告** | `content/notice/index.vue` | 表格 + 编辑弹窗，支持通知/公告/待办分类管理 |
| **AS400 对象** | `as400/objects/index.vue` | IBM i 系统库对象浏览与查询 |
| **操作日志** | `monitor/log/index.vue` | 表格 + 详情弹窗 + 删除/批量删除 + 列显示配置 |
| **在线用户** | `monitor/online/index.vue` | 在线用户列表 |
| **历代文学** | `classics/literature/index.vue` | 作者/朝代/体裁/分类 Tab 管理 |
| **文学作品** | `classics/literature/works/index.vue` | 文学作品 CRUD |
| **红楼诗词** | `classics/honglou/poems/index.vue` | 卡片列表展示诗词 |
| **红楼人物** | `classics/honglou/characters/index.vue` | 人物卡片 + 详情抽屉 |
| **人物关系** | `classics/honglou/relations/index.vue` | **Canvas 力导向图**（拖拽、缩放、高亮、DPR 适配） |
| **三国诗词** | `classics/sanguo/poems/index.vue` | 卡片列表 |
| **三国人物** | `classics/sanguo/characters/index.vue` | 人物卡片 + 详情 |
| **水浒诗词** | `classics/shuihu/poems/index.vue` | 卡片列表 |
| **水浒章节** | `classics/shuihu/chapters/index.vue` | 章节列表 |
| **西游诗词** | `classics/xiyou/poems/index.vue` | 卡片列表 |
| **西游人物** | `classics/xiyou/characters/index.vue` | 人物卡片 + 详情 |
| **八十一难** | `classics/xiyou/events/index.vue` | **时间轴视图**（统计卡片 + 时间轴卡片 + 响应式） |
| **权限申请** | `permission/request/index.vue` | 菜单树选择 + 我的申请列表 + 待审批管理 |
| **系统配置** | `system/config/index.vue` | 系统参数配置管理 |
| **文件管理** | `system/file/index.vue` | 文件列表 + 上传/下载/删除 |
| **定时任务** | `monitor/job/index.vue` | 任务列表 + 新增/编辑/暂停/单次执行 |
| **慢查询监控** | `monitor/slow-query/index.vue` | 慢查询列表 + 删除/清空 |
| **IService 管理** | `as400/iservice/index.vue` | iService 接口平台（类别/条目/列/示例/参数） |
| **技术博客** | `as400/techblog/index.vue` | 技术博客文章列表 CRUD |
| **技术博客详情** | `as400/techblog/detail.vue` | 文章详情编辑（Markdown 编辑器） |
| **Excel 解析** | `tool/excelParser/index.vue` | 上传 Excel 并展示解析结果 |
| **文档转换** | `tool/docConverter/index.vue` | PDF ↔ Word 互转 |
| **文档上传共享** | `tool/docUpload/index.vue` | 上传文档到共享目录 |
| **流程图编辑器** | `tool/flowChart/index.vue` | 三引擎集成（vue-flow/LogicFlow/AntV X6） |
| **音乐播放器** | `tool/musicPlayer/index.vue` | MP3 流式播放、歌单管理、播放统计 |

### 4.7 权限申请与审批

### 功能概述

普通用户可通过个人菜单中的"权限申请"向管理员提交菜单权限请求，管理员审批通过后权限立即生效。

### 权限模型

```
用户权限 = 角色权限（sys_role_menu） ∪ 直接授权权限（sys_user_menu）
```

#### 强制下线倒计时遮罩

当用户被管理员踢出时，下一次 API 请求会收到 `{ code: 401, message: "KICK_OUT" }`，触发全屏遮罩：

- 显示 "已被强制下线" + "X 秒后返回登录页面"（5 秒倒计时，全屏半透明覆盖）
- 遮罩期间用户无法进行任何操作
- 倒计时结束后 `clearAuthData()` + `router.push("/login")`
- `clearAuthData()` 同时清除 localStorage 和 Pinia store 的响应式状态，确保路由守卫正确放行

#### 会话心跳检测

`request.js` 模块加载时自动启动 `setInterval`，每 10 秒发送 `GET /api/auth/ping`：

- 有 token 时正常发请求，用于检测是否被踢出
- 无 token 时跳过本次（定时器保持运行，用户重新登录后自动恢复）
- 被踢出时下一次心跳触发 `NotLoginFilter` → 401 + `KICK_OUT` → 遮罩
- 心跳失败自动 `.catch(() => {})` 静默处理，不影响页面


- **角色权限**：通过 `sys_user_role` → `sys_role_menu` 关联，适用于批量管理
- **直接授权**：通过 `sys_user_menu` 表，管理员在"权限管理"中直接为用户分配/移除菜单和按钮权限
- **权限审批**：用户提交的申请存储在 `sys_permission_request` 表，审批通过后写入 `sys_user_menu`

### 关键实现

**后端**：
- `SysPermissionRequestService` — 提交申请、审批通过/拒绝、待审批列表
- `StpInterfaceImpl.getPermissionList()` — 双源合并角色权限和直接授权权限
- `AuthService.getUserInfo()` — 同样双源合并返回给前端的 perms 数组

**前端**：
- 权限申请页面：菜单树选择 + 我的申请列表
- 权限管理弹窗：用户管理 → 权限管理 Tab → 分配/移除权限
- 按钮显示：`v-if="userStore.hasPerm('xxx:xxx:xxx')"` 控制

---

### 4.8 待办事项提醒

### 功能概述

顶栏铃铛图标整合了通知公告、待办事项和**消息中心未读消息**，通过分类 Tab 区分不同类型。铃铛 Badge 数字 = 未读通知 + 待办事项 + **消息中心未读消息**。

### 待办类型

| 类型 | 来源 | 生成时机 | 清除时机 |
|------|------|----------|----------|
| **权限审批待办** | 用户提交权限申请 | `submitRequest()` 自动生成 | 审批通过/拒绝后自动清除 |
| **消息中心未读** | `sys_message` 表 | 系统/管理员发送消息（如个人信息变更通知） | 用户点击标记已读或手动"全部已读" |

未来可扩展更多待办类型（如任务提醒、数据审核等）。

### 实现方式

**后端**：
- `sys_notice` 表增加 `category`（notice/announcement/todo）和 `link_path` 字段
- `SysMessageService` 提供 `getUnreadCount(userId)` 返回消息未读数
- `GET /content/notice/summary` — 返回各分类数量概览
- `GET /content/notice/todo-count` — 返回待办数量
- `GET /content/notice/page?category=todo` — 按分类筛选
- `GET /content/message/page` + `GET /content/message/unread-count` — 消息中心 API

**前端** (`NoticePopover.vue`)：
- 通知 Popover 增加分类 Tab（未读/全部/通知/公告/待办/**消息**，共 6 个 Tab）
- 消息 Tab 显示消息中心最近 20 条未读消息，按类型标签区分（系统/通知/信息）
- 点击消息项 → 自动调用 `markAsReadApi` 标记已读 → 跳转到 `/content/message` 消息中心
- 消息 Tab 下"全部已读"调用 `markAllReadApi` 批量标记
- 消息 Tab 下"查看全部"跳转到消息中心页面
- 每 15 秒（可配 `VITE_NOTICE_POLL_INTERVAL` 环境变量）同时轮询刷新通知 + 消息未读数
- 待办 Tab 显示红色 Badge 数字
- 点击待办项 → 跳转到 `linkPath` 指定的处理页面（如 `/system/user`）
- 待办项不参与"已读/未读"标记（基于业务状态自动清除）

---



---

## 5. 布局与样式

### 5.1 整体布局

项目采用 **经典后台三件套** 布局模式：

```
┌──────────────────────────────────────────────────┐
│  el-container (100vh 全屏)                       │
│  ┌──────────┬───────────────────────────────────┐│
│  │ 侧边栏    │  右侧主体                         ││
│  │ el-aside │  ┌───────────────────────────────┐││
│  │ 220px    │  │ 顶栏 el-header (50px)         │││
│  │          │  │ 折叠按钮 | 面包屑 | 右侧操作   │││
│  │ Logo     │  ├───────────────────────────────┤││
│  │ 60px     │  │ 标签栏 TagsView (36px)        │││
│  │          │  ├───────────────────────────────┤││
│  │ 菜单树    │  │ 内容区 el-main               │││
│  │ (滚动)    │  │ calc(100vh - 50px - 37px)    │││
│  │          │  │ <router-view /> (keep-alive)  │││
│  └──────────┴───────────────────────────────────┘│
└──────────────────────────────────────────────────┘
```

**侧边栏**:
- 展开宽度: `220px`，折叠宽度: `64px`
- 折叠过渡: `transition: width 0.3s`
- Logo 区: 60px 高，含 logo.svg + "RX Admin" 文字
- 菜单: Element Plus `el-menu` 垂直模式，通过 `SubMenu.vue` 递归渲染
- 菜单项含图标 + 文字，折叠时仅显示图标

**顶栏** (50px):
- 左侧: 折叠按钮 + 面包屑导航
- 右侧: 全局搜索框（`SearchBox.vue`，模糊匹配菜单名称和路径，键盘上下键选择，回车跳转） | 暗黑切换 | 语言切换（Font Awesome globe 图标，无刷新动态切换） | 通知/待办/消息弹窗（`NoticePopover.vue`，Bell 铃铛图标，支持 6 个分类 Tab：未读/全部/通知/公告/待办/消息，默认显示未读） | 全屏切换（Font Awesome expand/compress 图标） | 用户头像下拉

**标签栏** (36px):
- 标签文字前显示与左侧菜单一致的图标
- 右键菜单: 刷新 / 关闭当前 / 关闭其他 / 关闭所有
- 水平滚动: 鼠标滚轮
- 固定标签: Dashboard 不可关闭
- keep-alive 缓存：切换标签时保持页面状态，缓存 key 使用组件英文 name

### 5.2 UI 组件库

**Element Plus 2.4.3** 作为核心 UI 框架：

- **引入方式**: 全量引入 + 中文语言包（通过 `el-config-provider` 动态切换语言）
- **国际化**: Vue I18n 实现中/英文双语切换，菜单名、表单、提示全量翻译，**无需页面刷新**
- **按需自动导入**: `unplugin-auto-import` + `unplugin-vue-components`
- **图标**: Element Plus Icons 按需自动导入（`unplugin-vue-components` 自动处理）+ Font Awesome 按需引入（70+ 图标）
- **暗黑模式**: 引入 `element-plus/theme-chalk/dark/css-vars.css`
- **图表**: ECharts 6.x 按需引入（Bar/Pie/Line/Radar 图表 + Canvas 渲染器），通过 Vite `manualChunks` 独立分包

**主要使用的 Element Plus 组件**:
`el-container`, `el-aside`, `el-header`, `el-main`, `el-menu`, `el-sub-menu`, `el-menu-item`, `el-scrollbar`, `el-breadcrumb`, `el-icon`, `el-tooltip`, `el-popover`, `el-badge`, `el-dropdown`, `el-avatar`, `el-input`, `el-button`, `el-table`, `el-form`, `el-dialog`, `el-drawer`, `el-card`, `el-tag`, `el-rate`, `el-pagination`, `el-row`, `el-col`, `el-select`, `el-link`, `el-empty`

### 5.3 主题系统

#### 双主题 CSS 变量方案

通过 `html` 元素的 `class` 切换主题：

```css
/* 亮色主题 (默认) */
:root {
  --bg-page: #f0f2f5;
  --bg-container: #fff;
  --text-primary: #303133;
  --color-primary: #409eff;
  /* ... 50+ 变量 */
}

/* 暗色主题 */
html.dark {
  --bg-page: #141414;
  --bg-container: #1d1e1f;
  --text-primary: #e5eaf3;
  color-scheme: dark;
  /* ... 对应的暗色值 */
}
```

**切换机制** (`composables/useTheme.js`):
```javascript
// 使用 useStorage 统一管理持久化
const theme = useStorage(STORAGE_KEYS.THEME, 'light')
// 切换
document.documentElement.classList.toggle('dark')
theme.value = isDark ? 'dark' : 'light'
```

#### CSS 变量分类

| 类别 | 变量数 | 示例变量 |
|------|--------|----------|
| **页面背景** | 6 | `--bg-page`, `--bg-container`, `--bg-hover`, `--bg-active`, `--bg-highlight`, `--bg-highlight-hover` |
| **文字颜色** | 4 | `--text-primary`, `--text-regular`, `--text-secondary`, `--text-placeholder` |
| **主题色** | 2 | `--color-primary` (#409eff), `--color-primary-light` |
| **边框** | 3 | `--border-color`, `--border-light`, `--border-lighter` |
| **侧边栏** | 10 | `--sidebar-bg`, `--sidebar-text`, `--sidebar-text-active`, `--sidebar-logo-color`, `--sidebar-logo-border`, `--sidebar-submenu-bg`, `--sidebar-item-hover-bg`, `--sidebar-item-active-bg` |
| **顶栏/标签** | 12 | `--header-bg`, `--header-shadow`, `--tags-bg`, `--tags-item-bg`, `--tags-item-color`, `--tags-item-active-bg`, `--tags-item-hover-bg`, `--tags-item-hover-color`, `--tags-item-hover-border`, `--tag-close-hover-bg` |
| **搜索框** | 5 | `--search-bg`, `--search-focus-bg`, `--search-focus-shadow`, `--search-dropdown-bg`, `--search-dropdown-shadow` |
| **通知** | 2 | `--notice-unread-bg`, `--notice-unread-hover`（含分类 Tab 和待办 Badge 样式） |
| **右键菜单** | 2 | `--context-menu-bg`, `--context-menu-shadow` |
| **阴影** | 2 | `--shadow-card`, `--shadow-header` |
| **登录页** | 2 | `--login-bg`, `--login-card-bg` |
| **表格** | 2 | `--table-container-bg`, `--search-bar-bg` |
| **其他** | 3 | `--menu-vertical-active-bg`, `--dict-current-row-bg`, `--badge-border` |

> **命名规范**：所有 CSS 变量采用 `--{类别}-{属性}` 格式。组件中使用时必须引用已定义的变量名，如 `--text-secondary`（正确）而非 `--text-color-secondary`（错误，不存在）。

### 5.4 全局样式

#### 全局重置 (`global.scss`)

```scss
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC',
               'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
```

#### 通用样式类

| 类名 | 用途 |
|------|------|
| `.page-container` | 页面容器: flex column, 撑满高度, overflow hidden |
| `.search-bar` | 搜索栏: flex wrap, 紧凑 28px 输入框 |
| `.table-container` | 表格容器: flex column 撑满剩余空间 |
| `.page-pagination` | 分页: 右对齐, flex-shrink: 0 |
| `.login-container` / `.login-card` | 登录页样式 |

#### Element Plus 样式覆盖

- 侧边栏菜单激活项: `.el-menu--vertical .el-menu-item.is-active`
- 进度条颜色: `#nprogress .bar { background: var(--color-primary); height: 3px; }`

### 5.5 响应式与动画

#### 响应式设计

- **侧边栏折叠**: 手动切换 (220px ↔ 64px)
- **Canvas 自适应**: 人物关系页通过 `window.resize` + `devicePixelRatio` 高清适配
- **时间轴响应式**: 唯一使用 `@media (max-width: 768px)` 的页面，统计卡片变为 2 列，时间轴改为单侧布局
- **无全局断点系统**: 未使用 Tailwind 等框架的全局响应式方案

#### CSS Transition 微交互

| 元素 | 过渡属性 | 时长 |
|------|----------|------|
| 侧边栏宽度 | `width` | 0.3s |
| 搜索框 | `all` | 0.3s |
| 搜索结果项 | `all` | 0.15s |
| 顶栏操作按钮 | `all` | 0.2s |
| 通知项 | `background` | 0.15s |
| 标签栏标签 | `all` | 0.2s |
| 右键菜单项 | `all` | 0.2s |
| 仪表盘卡片 | `transform` | 0.2s |

#### Vue Transition

- `search-dropdown-fade`: 搜索下拉 (opacity + translateY, 150ms ease)
- `fade`: 页面切换 (opacity, 0.2s ease)

#### Canvas 动画

- **力导向图**: `requestAnimationFrame` 循环驱动
- **节点渐变**: `createRadialGradient` 径向渐变
- **交互**: 拖拽节点、缩放画布、hover 高亮、选中聚焦

### 5.6 收藏夹

嵌入在顶部导航栏的收藏夹功能，以星形图标（☆/★）展示。用户可以收藏任意路由页面，收藏列表存储在 `localStorage`，点击已收藏条目快速跳转。与其他模块天然集成——新路由注册后即可被收藏。

**特性**：
- 收藏/取消收藏切换，图标即时反馈
- 收藏列表下拉面板，按收藏时间排序
- 数据持久化在浏览器本地存储，跨会话保留

### 5.7 全站命令面板 (`Ctrl+K`)

全局快捷键 `Ctrl+K` 唤出的命令面板（`CommandPalette.vue`），支持模糊搜索全站菜单和快捷操作。

**功能**：
- **菜单搜索**：输入关键字模糊匹配所有已注册菜单名称和路由路径，键盘上下键选择，回车跳转
- **最近访问**：展示最近点击过的页面（基于 sessionStorage），快速回到高频页面
- **快捷操作**：全屏切换、暗黑模式切换、刷新页面、清空缓存等一键直达
- **快捷键**：`Ctrl+K` 打开，`Esc` 关闭，箭头键导航

**适用场景**：键盘流用户无需鼠标点击侧边栏菜单，`Ctrl+K` 后输入目标页面名称直接跳转，大幅提升操作效率。

---

## 6. 常用工具模块

### 6.1 音乐播放器

基于 **mp3agic** 提取 MP3 元数据 + 自研流式播放引擎（支持 HTTP Range 请求分段传输），实现内嵌音乐播放器。
- 歌曲扫描：从指定文件夹递归扫描 MP3 文件，提取 ID3 标签（标题/艺术家/专辑/封面）
- 流式播放：支持进度条拖动（206 Partial Content），自适应缓冲
- 播放统计：记录播放次数、播放时长，生成热门排行
- 搜索功能：按歌曲名/艺术家模糊搜索

### 6.2 技术博客

基于 **Jsoup** 实现多源技术文章抓取，支持 CRUD、分类筛选、Markdown 渲染、抓取进度追踪。
- 多源支持：默认抓取 NickLitten 等 AS400/IBM i 技术博客
- 文章管理：分页查询、关键词/分类/来源筛选、编辑/删除/批量删除
- 抓取进度：异步任务执行，支持进度查询和日志输出
- 封面图片：自动提取文章首张图片作为 cover

### 6.3 文档工具（Excel/PDF/Word）

基于 **FastExcel** / **Apache PDFBox** / **Apache POI** 实现常用文档处理工具。
- **Excel 解析**：上传 .xlsx/.xls 文件，自动读取表头和行数据，支持 50MB 以内文件
- **PDF 转 Word**：提取 PDF 文本内容生成 .docx（支持中文字体自动检测）
- **Word 转 PDF**：读取 .docx 文本内容生成 PDF（含中文字体 Fallback 链）
- **文档上传共享**：上传任意文档到共享目录，支持分页查询和删除

### 6.4 流程图编辑器

集成 **三款流程图引擎**，满足不同场景需求：
- **@vue-flow/core**：基于 Vue 3 的轻量流程图库，自动布局，支持缩略图/控制面板
- **LogicFlow 2.x**：滴滴开源的流程图编辑框架，支持自定义节点和边
- **AntV X6**：蚂蚁集团的高性能图编辑引擎，支持复杂图形和大规模图谱

### 6.5 API 接口分析工具

输入菜单名称，自动分析该菜单对应的前后端交互链路（数据流/调用链/流程图），帮助开发者理解模块间依赖关系。

### 6.6 中国行政区划

中国省/市/区三级行政区划数据管理，支持级联选择器搜索、分页查询、CRUD 操作，包含下级数据保护（有下级时禁止删除）。

### 6.7 AS400 IService 接口平台

基于 IBM i 系统接口数据，提供 iService 类别/条目/列/示例/参数的五层结构管理：
- **类别管理**：按模块分组，显示每组条目数
- **条目管理**：按类别筛选，查看接口详情
- **接口详情**：包含列定义、示例代码、参数说明

## 7. 四大名著模块

### 数据统计

| 名著 | 人物数 | 诗词数 | 关系数 | 章节/事件 | 数据库 |
|------|--------|--------|--------|-----------|--------|
| 红楼梦 | 54 | 待统计 | 99 | — | rxusysadmin |
| 三国演义 | 待统计 | 待统计 | — | — | rxusysadmin |
| 水浒传 | — | 待统计 | — | 待统计 | rxusysadmin |
| 西游记 | 待统计 | 待统计 | — | 81难 | rxusysadmin |

### 前端特色页面

#### 红楼梦人物关系图 (`honglou/relations/index.vue`)

- **技术**: 原生 Canvas 2D + 力导向布局算法
- **特性**:
  - 节点: 径向渐变圆形，按角色分类着色（主角/贾府/宁府/王家/薛家/丫鬟/其他）
  - 连线: 带关系标签（父子、母子、夫妻、主仆、兄妹等）
  - 交互: 拖拽节点、鼠标滚轮缩放、画布平移
  - 高亮: 点击节点高亮关联人物，其他节点变暗
  - DPR 适配: 高清屏 `devicePixelRatio` 自适应
  - 动画: `requestAnimationFrame` 持续渲染力导向迭代

#### 西游记八十一难 (`xiyou/events/index.vue`)

- **技术**: 纯 CSS 时间轴布局
- **特性**:
  - 顶部统计卡片: 4 种渐变色，hover 上浮
  - 时间轴中线: 8 色渐变垂直线
  - 时间轴节点: 渐变圆形，hover 放大 `scale(1.15)`
  - 时间轴卡片: hover 上浮 `translateY(-4px)` + 阴影增强
  - 响应式: `@media (max-width: 768px)` 单侧布局

---

## 8. 菜单功能详解

> 本章详细介绍系统中每个菜单的功能定位、适用场景与操作方式。不含历代文学、AS400管理、四大名著模块。

### 8.1 仪表盘

| 菜单 | 路由 | 功能说明 |
|------|------|---------|
| **仪表盘** | `/dashboard` | 系统首页数据概览。展示核心统计卡片（用户数/角色数/今日活跃/系统负载），ECharts 可视化图表（访问趋势折线图、模块占比饼图、最近操作时间轴），支持暗黑模式自适应。左侧为快捷入口卡片，点击可跳转到常用功能。 |

**适用场景**：管理员登录后快速掌握系统运行概况，作为日常工作入口。

---

### 8.2 系统管理

系统管理模块负责整个平台的 RBAC 权限体系维护，是后台管理的核心。所有子菜单默认在角色权限控制下可见。

#### 8.2.1 用户管理 (`/system/user`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `system:user:query` | 分页列表，支持按用户名/手机号/状态模糊搜索 |
| 新增 | `system:user:add` | 弹窗表单：用户名、密码、昵称、手机、邮箱、所属部门、角色分配。密码强度实时校验（弱/中/强/非常强） |
| 修改 | `system:user:edit` | 编辑除密码外的用户信息，修改角色分配 |
| 删除 | `system:user:delete` | 逻辑删除（`deleted=1`），支持批量删除 |

**适用场景**：管理员创建和维护系统用户账号，分配用户所属角色和部门。新增用户时可选择是否同时发送系统通知。

#### 8.2.2 角色管理 (`/system/role`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `system:role:query` | 分页列表，支持按角色名模糊搜索 |
| 新增 | `system:role:add` | 弹窗表单：角色名、角色标识、排序、备注 |
| 修改 | `system:role:edit` | 编辑角色信息，**最关键操作是配置角色对应的菜单权限树** |
| 删除 | `system:role:delete` | 逻辑删除，要求无用户关联（需先移除该角色下的用户） |

**适用场景**：定义系统的角色体系（如 admin、普通用户、审计员等），为每个角色勾选可访问的菜单和按钮权限。菜单权限树的选项决定该角色登录后左侧侧边栏的显示内容。

#### 8.2.3 菜单管理 (`/system/menu`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `system:menu:query` | 树形表格展示所有菜单（目录/菜单/按钮三种类型） |
| 新增 | `system:menu:add` | 新增菜单节点：类型选择（目录=一级分组，菜单=具体页面，按钮=操作权限）；路径、组件名、图标、排序、权限标识 |
| 修改 | `system:menu:edit` | 修改菜单属性，调整菜单树结构 |
| 删除 | `system:menu:delete` | 删除菜单及下属子节点 |

**适用场景**：开发新功能时注册前端页面到菜单系统。菜单表是前端动态路由的唯一数据源，`component` 字段必须与前端 `componentMap.js` 中的 key 一致，否则路由注册失败。

**字段说明**：
- `menu_type`：1=目录（左侧分组折叠项），2=菜单（可点击的具体页面），3=按钮（操作权限点，不生成路由）
- `component`：前端组件路径，如 `system/user/index`，对应 `componentMap.js` 中的映射
- `perms`：Shiro/Sa-Token 权限标识，后端 `@SaCheckPermission` 注解校验依据
- `path`：前端路由路径，需全局唯一

#### 8.2.4 部门管理 (`/system/dept`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `system:dept:query` | 树形表格展示部门层级 |
| 新增 | `system:dept:add` | 新增子部门或同级部门，指定上级部门 |
| 修改 | `system:dept:edit` | 修改部门名称、排序、负责人等信息 |
| 删除 | `system:dept:delete` | 逻辑删除，有子部门或有关联用户时禁止删除 |

**适用场景**：构建公司组织架构树（如公司→部门→小组），用户管理中选择用户所属部门时基于此树选择。

#### 8.2.5 系统配置 (`/system/config`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `system:config:query` | 分页列表，支持按配置键名模糊搜索 |
| 新增 | `system:config:add` | 新增键值对，如 `site.name` = `RX Admin` |
| 修改 | `system:config:edit` | 修改配置值和备注 |
| 删除 | `system:config:delete` | 删除配置项 |

**适用场景**：集中管理系统的全局参数，如站点名称、文件上传大小限制、IP过滤模式等。数据存储在 `sys_config` 表，后端通过 `SysConfigService` 读取，支持内存缓存（Caffeine）加速读取。常见配置项：`ip.filter.mode`（OFF/BLACK/WHITE）、`sys.user.initPassword`、`sys.index.skinName`。

#### 8.2.6 IP 黑白名单 (`/system/ip-rule`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `system:ip-rule:query` | 分页列表，按 IP 地址或规则类型筛选 |
| 新增 | `system:ip-rule:add` | 添加 IP 规则：IP地址、规则类型（BLACK/WHITE）、描述 |
| 修改 | `system:ip-rule:edit` | 修改已有规则 |
| 删除 | `system:ip-rule:delete` | 删除规则 |

**适用场景**：安全防护——黑名单模式禁止指定 IP 访问系统，白名单模式仅允许名单内 IP 访问。模式切换通过系统配置 `ip.filter.mode` 控制，规则存储在 `sys_ip_rule` 表。需要配置拦截器/过滤器在请求入口处生效。

#### 8.2.7 定时任务 (`/monitor/job`)

> 注：路由路径在 `/monitor/` 下，但菜单归属于系统管理分组

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `monitor:job:query` | 分页列表，显示任务名、cron 表达式、执行状态 |
| 新增 | `monitor:job:add` | 新增 Quartz 定时任务：任务类全限定名 + cron 表达式 |
| 修改 | `monitor:job:edit` | 修改任务参数或 cron 表达式 |
| 删除 | `monitor:job:delete` | 删除任务 |

**适用场景**：管理后台定时任务调度，如定时清理日志、定时发送通知、定时数据同步等。底层基于 Quartz 实现，支持立即执行、暂停/恢复操作。cron 表达式需符合 Quartz 格式（7位：秒 分 时 日 月 周 年）。

#### 8.2.8 文件管理 (`/system/file`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `system:file:query` | 已上传文件列表，显示文件名、大小、上传时间 |
| 上传 | `system:file:upload` | 上传文件到服务器 `upload/` 目录 |
| 删除 | `system:file:delete` | 删除文件记录及物理文件 |

**适用场景**：统一管理系统中所有上传的文件。上传时存储到服务器本地目录，并在 `sys_file` 表记录元数据（文件名、路径、大小、上传人）。其他模块（如通知公告、文档共享）可通过文件ID引用。

---

### 8.3 系统工具

系统工具模块集合了开发辅助、数据管理、运维工具等实用性功能。

#### 8.3.1 字典管理 (`/tool/dict`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `tool:dict:query` | 分页列表，支持按字典名/字典类型筛选 |
| 新增 | `tool:dict:add` | 新增字典类型及字典项 |
| 修改 | `tool:dict:edit` | 修改字典值 |
| 删除 | `tool:dict:delete` | 删除字典项（含子项） |

**适用场景**：管理系统的枚举值数据，如性别（男/女）、状态（启用/禁用）、通知类型等。前端下拉框、单选按钮通常绑定字典接口获取选项列表，避免硬编码。

#### 8.3.2 行政区划 (`/tool/region`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `tool:region:query` | 省/市/区三级树形数据，支持级联选择器搜索 |
| 新增 | `tool:region:add` | 新增下级区划 |
| 修改 | `tool:region:edit` | 修改区划名称/编码 |
| 删除 | `tool:region:delete` | 删除区划（有下级时禁止删除） |

**适用场景**：维护中国省市区三级行政区划数据，用户注册时的地区选择、数据统计的地域维度均可基于此模块。数据量约 3000+ 条，支持 Excel 导入更新。

#### 8.3.3 接口分析 (`/tool/analysis`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `tool:analysis:query` | 输入菜单名称，分析该菜单前后端交互链路 |

**适用场景**：开发者工具——输入菜单名称（如"用户管理"），系统自动扫描后端 Controller 对应接口和前端 API 调用，生成数据流分析报告（接口路径、请求方式、参数结构、调用链路图）。帮助新成员快速理解模块的前后端交互关系。

**分析原理**：从 `sys_menu` 表获取菜单 → 匹配前端 `componentMap.js` 组件 → 解析组件中 `import` 的 API 模块 → 匹配后端 Controller 中 `@RequestMapping` 注解的接口 → 输出链路报告。

#### 8.3.4 项目文档 (`/tool/docs`)

| 说明 |
|------|
| 浏览项目的 Markdown 技术文档（如本文档 `rxadmin.md`、`rxadmin-optimization.md`），左侧目录树导航，右侧使用 marked + highlight.js 渲染 Markdown 内容，支持代码高亮。 |

**适用场景**：开发者和运维人员在线查阅项目架构文档，无需打开 IDE 或单独打开文件。

#### 8.3.5 开发规范 (`/tool/standards`)

| 说明 |
|------|
| 浏览项目开发规范文档（`dev-standards.md`），与项目文档使用相同的 Markdown 渲染引擎。内容涵盖代码规范、命名约定、Git 提交规范、API 设计规范等。 |

**适用场景**：新成员入职时在线学习开发规范，确保团队代码风格一致。

#### 8.3.6 代码生成 (`/tool/gen`) — v2.0

| 说明 |
|------|
| 三步可视化向导生成完整 CRUD 代码。**步骤一**：从数据库 `information_schema` 读取表列表，选择目标表；**步骤二**：配置生成选项（包名、作者、是否生成前端页面）；**步骤三**：预览生成结果（Entity/Mapper/Service/Controller/Vue 组件/API 模块），确认后生成到指定目录。 |

**类型映射规则**：`bigint`→`Long`、`int/tinyint`→`Integer`、`varchar/text`→`String`、`datetime`→`LocalDateTime`、`decimal`→`BigDecimal`。

**适用场景**：快速生成标准 CRUD 模块的模板代码，大幅减少重复编码工作。

#### 8.3.7 批量导入 (`/tool/import`) — v2.0

| 说明 |
|------|
| 三步数据批量导入工具。**步骤一**：上传 Excel (.xlsx/.xls) 或 CSV 文件；**步骤二**：预览数据，自动校验字段格式，高亮错误行；**步骤三**：执行导入，逐行写入数据库，输出成功/失败计数和错误详情。 |

**适用场景**：批量导入用户信息、字典数据、行政区划等，替代手工逐条录入。

#### 8.3.8 API 调试 (`/tool/api-debug`) — v2.0

| 说明 |
|------|
| 内置 API 调试面板，类似 Postman。左侧自动扫描并展示所有已注册的 API 端点列表（从 `RequestMappingHandlerMapping` 获取），右侧提供请求参数编辑面板，支持 GET/POST/PUT/DELETE 请求发送，响应结果实时展示（JSON 格式化和状态码）。 |

**适用场景**：开发调试时快速测试后端接口，无需离开浏览器切换到 Postman 等外部工具。

#### 8.3.9 数据备份 (`/tool/backup`) — v2.0

| 说明 |
|------|
| 数据库备份与恢复工具。执行 `mysqldump` 命令备份数据库到 `backups/` 目录，列表展示所有备份文件，支持下载到本地、一键恢复、删除旧备份。备份文件名含时间戳格式：`backup_yyyyMMdd_HHmmss.sql`。 |

**适用场景**：系统管理员定期备份数据库，或在重大变更前手动创建备份点。

#### 8.3.10 数据库工具 (`/tool/dbConsole`) — v2.1

| 说明 |
|------|
| 在线数据库管理面板，三合一（SQL控制台 + 表结构查看 + 连接池监控）。左侧展示全量表列表（可搜索），右侧提供只读 SQL 控制台、选中表的字段详情、底部 HikariCP 连接池实时状态。SQL 执行安全限制：禁止 DROP/DELETE/INSERT/UPDATE 等写操作，仅允许 SELECT/SHOW/DESCRIBE，查询超时 10 秒，最大返回 1000 行。 |

**适用场景**：运维人员在线排查数据问题——无需登录数据库客户端，直接在管理后台执行只读查询；开发人员快速查看表结构和字段注释；监控数据库连接池健康状态，及时发现连接泄漏或池满问题。

#### 8.3.11 开发工具 (`/tool/devTools`) — v2.1

| 说明 |
|------|
| 6 合 1 开发常用小工具集，大部分纯前端实现，即开即用。包含：**JSON格式化**（美化/压缩）、**UUID生成**（批量1-100个，后端API）、**Base64编解码**（文本↔Base64）、**时间戳转换**（毫秒↔日期双向）、**二维码生成**（文本→二维码图片，调用Google Charts API）、**正则测试**（输入正则+测试文本 → 显示匹配结果）。 |

**适用场景**：开发者的瑞士军刀——调试API响应时格式化JSON、生成测试UUID、编码Base64、时间戳互转、生成分享二维码、编写正则时实时测试匹配效果。

#### 8.3.12 代码生成器 (`/tool/codeGenerator`) — v1.4.0

| 说明 |
|------|
| 基于模板引擎的代码自动生成工具。选择数据表 → 配置生成选项（包名/模块名/作者）→ 一键生成 Controller、Service、ServiceImpl、Mapper、Mapper XML、Entity、前端 Vue 页面（列表+表单）完整 CRUD 代码。支持自定义模板覆盖默认生成规则，减少重复性编码工作。 |

**适用场景**：快速搭建新功能模块——新建数据库表后，通过代码生成器自动生成前后端全套 CRUD 代码，开发者只需微调业务逻辑即可。

#### 8.3.13 批量导入 (`/tool/batchImport`) — v1.4.0

| 说明 |
|------|
| 基于 EasyExcel 的数据批量导入工具。下载导入模板（Excel）→ 按模板填充数据 → 上传文件 → 自动解析并批量写入数据库。支持数据校验（字段格式/非空检查）、导入结果反馈（成功条数/失败条数/失败原因）、大数据量分批提交。 |

**适用场景**：系统初始化时批量导入用户/部门/字典等基础数据；业务运营中批量导入外部数据。避免逐条手动录入，提升数据迁移效率。

#### 8.3.14 API 调试 (`/tool/apiDebug`) — v1.4.0

| 说明 |
|------|
| 内置 API 调试面板，类似 Postman 功能。选择 HTTP 方法（GET/POST/PUT/DELETE）、填写请求 URL、Header、Body（支持 JSON/Form-Data），点击发送查看响应状态码、响应头、响应体（JSON 格式化高亮）。支持保存请求历史和快速重放。 |

**适用场景**：开发阶段快速调试后端接口，无需切换外部工具；支持人员排查接口问题时验证参数和返回结果。

---

### 8.4 内容管理

内容管理模块负责系统的消息通知体系，包括通知公告和站内消息中心两大子模块。

#### 8.4.1 通知公告 (`/content/notice`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `content:notice:query` | 分页列表，支持按标题/类型/状态筛选 |
| 新增 | `content:notice:add` | 发布公告：标题、内容（富文本）、类型（通知/公告/弹窗公告）、链接路径 |
| 修改 | `content:notice:edit` | 修改公告内容 |
| 删除 | `content:notice:delete` | 逻辑删除公告 |

**适用场景**：管理员发布系统通知公告。`noticeType='2'` 的公告会在用户登录后以弹窗形式展示（`AnnouncementPopup.vue`），弹窗去重通过 localStorage 实现。支持关联跳转链接（`link_path`），点击后跳转到指定页面。

#### 8.4.2 消息中心 (`/content/message`) — v2.0

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 删除 | `content:message:delete` | 删除消息记录 |

**适用场景**：站内私信和系统通知的查看中心。以 `el-timeline` 时间轴形式展示所有消息（系统通知/私信/提醒），支持按类型筛选，标记已读/全部已读，顶部显示未读红点计数。消息存储在 `sys_message` 表，包含发送者/接收者/标题/内容/类型/读取状态。与顶栏通知弹窗（`NoticePopover.vue`）联动。

**权限控制**：管理员可查看全部用户的消息或按 `userId` 筛选；普通用户只能看自己的消息。

**用户个人信息变更通知机制**：当普通用户修改个人信息（邮箱/手机号/密码等）时，消息中心同时发送两条消息：
- 用户本人收到 info 类型消息：`"您于 yyyy-MM-dd HH:mm:ss 更新了：邮箱、手机号"` → 使用"您"指代
- 所有管理员收到 system 类型消息：`"用户 张三(zhangsan) 于 yyyy-MM-dd HH:mm:ss 更新了：邮箱、手机号"` → 显示具体用户昵称和用户名
- 若操作者本身是管理员，则只收到自己的那条（排除自身避免重复）

#### 8.4.3 通知中心 (`/content/notify-center`) — v2.1

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 模板查询 | `content:notify-center:template:query` | 消息模板分页列表，按模板名称搜索 |
| 模板新增 | `content:notify-center:template:add` | 新增消息模板（名称/编码/标题/内容/通道） |
| 模板编辑 | `content:notify-center:template:edit` | 修改已有模板 |
| 模板删除 | `content:notify-center:template:delete` | 删除模板 |
| 发送通知 | — | 选择模板 → 填写接收人 → 发送 |
| 记录查询 | — | 发送记录分页，按通道/状态筛选 |
| 重发 | — | 失败通知可手动重试 |

**适用场景**：统一的消息通知管理中心。双 Tab 布局——**消息模板** Tab 管理通知模板（支持变量如 `{username}`、`{date}`，支持多通道：站内消息/邮件/企微/钉钉/飞书）；**发送记录** Tab 追踪每次发送的结果，失败通知可一键重试。预置 4 条示例模板（密码重置通知、新用户欢迎、任务执行失败告警、系统维护通知）。


**后端**: `NotifyCenterController` (`/api/notify-center`)  
**前端**: `views/content/notify-center/index.vue`  
**权限**: 各接口独立 `@SaCheckRole("admin")`  
**预置数据**: 4 条示例模板（密码重置通知、新用户欢迎、任务执行失败告警、系统维护通知）

#### API 端点

**消息模板**:

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/templates/page` | 模板分页列表 | page, size, name |
| POST | `/templates` | 新增模板 | SysMessageTemplate(name/code/titleTemplate/contentTemplate/channels/status) |
| PUT | `/templates` | 更新模板 | SysMessageTemplate |
| DELETE | `/templates/{id}` | 删除模板 | id(路径) |

**发送通知**:

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | `/send` | 发送通知 | templateId, channel(默认"message"), receiver, title, content |

**发送记录**:

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/records/page` | 记录分页列表 | page, size, channel, status |
| DELETE | `/records/{id}` | 删除单条 | id(路径) |
| DELETE | `/records/batch` | 批量删除 | ids(JSON数组) |
| POST | `/records/{id}/retry` | 重发失败通知 | id(路径) |

#### 如何使用与测试

**消息模板 Tab**:
1. 导航到 **内容管理 → 通知中心**，默认打开「消息模板」Tab
2. 查看预置的 4 条模板（密码重置、新用户欢迎、任务失败告警、系统维护）
3. **新增模板**: 点击"新增"按钮 → 填写弹窗表单：
   - 模板名称（如"审批通知"）
   - 模板编码（唯一标识，如 `APPROVAL_NOTIFY`）
   - 标题模板（支持变量如 `您好 {username}，您的申请已{status}`）
   - 内容模板（textarea，支持更多变量和换行）
   - 通道（逗号分隔，如 `message,email`）
   - 启用开关
4. **编辑模板**: 点击编辑按钮修改已有模板
5. **删除模板**: 点击删除按钮（带确认弹窗）

**发送通知**:
1. 在模板列表中选择一个模板
2. 填入接收人、标题、内容（可引用模板）→ 点击发送
3. 发送记录自动写入 `sys_notify_record` 表

**发送记录 Tab**:
1. 切换到「发送记录」Tab
2. 按通道（message/email/wecom/dingtalk/feishu）和状态（成功/失败/待发送）筛选
3. 表格展示：ID、通道、接收人、标题、状态标签、重试次数、创建时间
4. **重发**: 失败状态的记录可点击"重发"按钮 → 自动重置状态为成功、retryCount+1、更新发送时间
5. 支持单选删除和批量删除

**通道说明**: 当前支持的通道代码为 `message`（站内消息）、`email`（邮件）、`wecom`（企业微信）、`dingtalk`（钉钉）、`feishu`（飞书）。除站内消息外，其余通道需在 `application.yml` 的 enhancement 配置段中配置对应的 Webhook 或 SMTP 信息。

#### 预期效果

- ✅ 模板 Tab 应显示至少 4 条预置模板
- ✅ 可新增/编辑/删除模板
- ✅ 发送记录 Tab 初始可能为空（需要先发送通知）
- ✅ 发送通知后，记录 Tab 出现对应记录
- ✅ 重发功能对失败记录生效
---

### 8.5 办公工具

办公工具模块集合了文档处理、流程设计、邮件等效率工具。

#### 8.5.1 Excel 解析 (`/common-tools/excel-parser`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 上传解析 | `common-tools:excel:upload` | 上传 .xlsx/.xls 文件（50MB 内），自动解析为表格展示 |

**适用场景**：快速查看 Excel 文件内容，无需打开 Excel 软件。基于 FastExcel（阿里 easyexcel 分支）解析，支持多 Sheet 切换。

#### 8.5.2 文档格式转换 (`/common-tools/doc-converter`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| PDF 转 Word | `common-tools:pdf-to-word` | 上传 PDF → 提取文本 → 生成 .docx |
| Word 转 PDF | `common-tools:word-to-pdf` | 上传 .docx → 提取文本 → 生成 PDF |

**适用场景**：日常文档格式互转。PDF→Word 基于 Apache PDFBox 提取文本，Word→PDF 基于 Apache POI 读取内容。支持中文字体自动检测和 Fallback 链。

#### 8.5.3 文档上传共享 (`/common-tools/doc-upload`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 上传文档 | `common-tools:doc:upload` | 上传任意格式文档到共享目录 |
| 删除文档 | `common-tools:doc:delete` | 删除已上传文档 |

**适用场景**：团队文档共享——上传项目资料、需求文档等，支持分页查询和删除。

#### 8.5.4 流程图编辑器 (`/common-tools/flow-chart`)

| 说明 |
|------|
| 集成三款主流流程图引擎：**@vue-flow/core**（轻量级 Vue3 流程图，自动布局+缩略图）、**LogicFlow 2.x**（滴滴开源，支持自定义节点/边）、**AntV X6**（蚂蚁集团高性能图编辑器，支持大规模图谱）。页面顶部切换引擎标签，满足不同复杂度的流程图设计需求。 |

**适用场景**：架构设计、流程梳理、ER 图绘制等。vue-flow 适合简单场景，LogicFlow 适合业务流程图，X6 适合复杂交互式图表。

#### 8.5.5 邮件发送 (`/common-tools/email-sender`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 发送邮件 | `common-tools:email:send` | 填写收件人/主题/正文 → 发送 |
| 上传附件 | `common-tools:email:attachment` | 为邮件添加附件 |

**适用场景**：系统内发送通知邮件，支持 HTML/纯文本编辑、CC/BCC、附件上传。发件人地址通过 SMTP 配置（默认 `xubingzhen83@163.com`，163邮箱需使用授权码，端口 465 + SSL）。

**SMTP 配置** (`application.yml` → `app.mail.*`，本地值见 `application-local.yml`)：

```yaml
app:
  mail:
    host: smtp.163.com      # 环境变量 MAIL_HOST
    port: 465               # 163 使用 SSL
    username: 发件邮箱         # 环境变量 MAIL_USERNAME
    password: 授权码           # 环境变量 MAIL_PASSWORD（非登录密码）
    from: 发件人地址            # 环境变量 MAIL_FROM
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: false   # 163 SSL 需关闭
      mail.smtp.ssl.enable: true
```

---

### 8.6 音乐播放器 (`/musicPlayer`)

| 说明 |
|------|
| 独立的音乐播放模块，MP3 流式播放（HTTP Range 请求，支持 206 Partial Content 进度条拖动），基于 **mp3agic** 提取 ID3 标签（标题/艺术家/专辑/封面）。功能包括：歌曲扫描（递归扫描指定文件夹）、播放统计（播放次数/时长）、热门排行、按歌名/艺术家搜索。 |

**适用场景**：内嵌式工作背景音乐播放器，管理员可配置 MP3 文件目录，系统自动识别并归档。

---

### 8.7 系统监控

系统监控模块提供全方位的系统运行状况监控能力。

#### 8.7.1 操作日志 (`/monitor/log`)

| 说明 |
|------|
| 记录系统中的所有操作行为，包括操作人、操作时间、IP 地址、请求方法、操作类型、请求参数、返回结果等。基于 `@OperateLog` 注解 + AspectJ AOP 切面，`@Async` 异步写入日志表，不影响主业务性能。支持按时间范围/操作类型/操作人筛选。 |

**适用场景**：安全审计——追踪谁在什么时间做了什么操作；问题排查——查看某个操作的具体参数和结果；合规要求——满足日志审计追溯需求。

#### 8.7.2 在线用户 (`/monitor/online`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查看 | — | 展示当前在线用户列表（用户名、IP、登录时间、最后活跃时间） |
| 强制下线 | `monitor:online:kickout` | 强制踢出指定用户，使其 Token 失效 |

**适用场景**：管理员查看当前系统在线人数和用户详情，对异常用户执行强制下线操作。基于 Sa-Token 的会话管理，踢出操作会清除目标用户的登录 Token，前端检测到 Token 失效后弹出倒计时遮罩并跳转到登录页。

#### 8.7.3 慢查询监控 (`/monitor/slow-query`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 查询 | `monitor:slow-query:query` | 慢查询记录列表，按时间/耗时筛选 |
| 删除 | `monitor:slow-query:delete` | 删除单条记录 |
| 清空 | `monitor:slow-query:clear` | 清空全部记录 |

**适用场景**：DBA 和开发人员监控数据库性能。系统通过拦截器或 AOP 记录执行超过阈值的 SQL 到 `sys_slow_query` 表，包含 SQL 文本、执行耗时、发生时间等信息，用于定位数据库性能瓶颈。

#### 8.7.4 健康监控 (`/monitor/health`) — v2.0

| 说明 |
|------|
| 实时系统健康监控面板。展示 4 个核心统计卡片（CPU 使用率、内存总量/已用、JVM 堆/非堆、线程数/峰值）+ ECharts 仪表盘可视化 + JVM 详情表格。每 10 秒自动刷新数据，API 接口：`GET /api/monitor/health/system` 和 `GET /api/monitor/health/gc`。基于 Java `OperatingSystemMXBean` 和 `Runtime` 获取系统指标。 |

**适用场景**：运维人员实时监控服务器资源使用情况，快速发现 CPU/内存异常。

#### 8.7.5 日志分析 (`/monitor/log-analysis`) — v2.0

| 说明 |
|------|
| 操作日志的可视化分析平台。包含 4 个统计卡片（今日操作数 / 异常操作数 / 活跃用户数 / 最高频操作）+ ECharts 图表（时段操作分布柱状图、操作类型占比饼图、7天趋势折线图）。数据来源为 `sys_oper_log` 表，支持日期范围筛选。 |

**适用场景**：管理员、安全审计员通过可视化图表快速了解系统操作趋势，发现异常操作模式（如某时段操作量激增、某类操作异常率高）。

#### 8.7.6 登录日志 (`/monitor/login-log`) — v2.1

| 说明 |
|------|
| 独立记录每次登录尝试（成功/失败），包含用户名、IP地址、浏览器、操作系统、失败原因。登录事件由 `AuthController` → `LoginLogService.recordLogin()` 自动写入 `sys_login_log` 表，无需手动操作。支持按用户名、状态（成功/失败）、时间范围筛选，支持单条删除和批量删除。 |

**适用场景**：安全审计——追踪谁在什么时间从哪个IP登录了系统；异常检测——发现大量失败登录尝试后可及时排查暴力破解风险。


**后端**: `SysLoginLogController` (`/api/monitor/login-log`)  
**前端**: `views/monitor/login-log/index.vue`  
**权限**: 仅 admin 角色可访问  
**自动记录**: 登录成功/失败时由 `AuthController` → `LoginLogService.recordLogin()` 自动写入

#### API 端点

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/page` | 分页查询 | username, status(1=成功/0=失败), startTime, endTime |
| DELETE | `/{id}` | 删除单条 | id(路径) |
| DELETE | `/batch` | 批量删除 | ids(JSON数组) |

#### 如何使用与测试

1. **自动记录**: 任意用户登录/登出系统，记录自动写入 `sys_login_log` 表
2. **查看日志**: admin 登录后，导航到 **系统监控 → 登录日志**，查看所有用户的登录记录
3. **筛选查询**: 
   - 按用户名搜索特定用户的登录历史
   - 按状态筛选：下拉框选择「成功」或「失败」
   - 按时间范围筛选：选择起止日期查看某时段的登录情况
4. **删除操作**: 点击单条删除按钮（带确认弹窗），或勾选多条后点击"批量删除"
5. **验证字段**: 表格展示 ID、用户名、IP地址、浏览器（解析后如 "Chrome 131"）、操作系统（如 "Windows 10"）、状态标签（绿色成功/红色失败）、失败原因、登录时间

#### 预期效果

- ✅ 打开页面应看到至少 1 条记录（你自己的登录记录）
- ✅ 浏览器字段应显示简洁格式（如 "Chrome 131"），而非完整的 User-Agent 字符串
- ✅ 失败登录也应被记录（可故意用错误密码登录测试）
- ✅ 分页、筛选、删除功能均可正常使用

---

#### 8.7.7 导出审计 (`/monitor/export-log`) — v2.1

| 说明 |
|------|
| 记录所有数据导出操作（Excel/PDF），包含导出类型、导出标题、记录条数、文件名、操作人、IP地址。由 `ExportController` 自动写入 `sys_export_log` 表，纯只读展示，无删除功能。支持按用户名、导出类型、时间范围筛选。 |

**适用场景**：数据安全审计——追踪敏感数据的导出行为，确保数据外发有迹可查。


**后端**: `SysExportLogController` (`/api/monitor/export-log`)  
**前端**: `views/monitor/export-log/index.vue`  
**权限**: 仅 admin 角色可访问  
**自动记录**: 每次通过 `ExportController` 执行 Excel/PDF 导出时自动写入，包含导出类型、标题、记录条数、文件名、IP

#### API 端点

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/page` | 分页查询（只读） | username, exportType(excel/pdf), startTime, endTime |

#### 如何使用与测试

1. **触发审计记录**: 在任意列表页面执行一次导出操作（如用户管理 → 导出Excel），记录自动写入
2. **查看审计**: 导航到 **系统监控 → 导出审计**，查看导出历史
3. **筛选**: 按用户名、导出类型（Excel/PDF）、时间范围筛选
4. **注意事项**: 此页面为纯只读，无删除功能（导出审计需长期保留）

#### 预期效果

- ✅ 执行过一次导出后，打开页面应能看到对应记录
- ✅ 导出类型以标签形式显示（Excel=蓝色、PDF=橙色）
- ✅ 如果从未导出过任何数据，表格为空（属于正常情况）
#### 8.7.8 任务执行日志 (`/monitor/job-log`) — v2.1

| 说明 |
|------|
| 记录每次定时任务/手动触发的执行结果，包含任务名称、Bean名、方法名、耗时(ms)、执行状态、结果信息。自动写入 `sys_job_log` 表。耗时超过 5 秒的记录红色高亮警告。支持按状态、时间范围筛选，单条删除和批量删除。 |

**适用场景**：运维监控——追踪定时任务的历史执行情况，快速定位执行失败或耗时过长的任务。


**后端**: `SysJobLogController` (`/api/monitor/job-log`)  
**前端**: `views/monitor/job-log/index.vue`  
**权限**: 仅 admin 角色可访问

#### API 端点

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/page` | 分页查询 | jobId, status(1=成功/0=失败), startTime, endTime |
| DELETE | `/{id}` | 删除单条 | id(路径) |
| DELETE | `/batch` | 批量删除 | ids(JSON数组) |

#### 如何使用与测试

1. **触发日志**: 在 **系统监控 → 定时任务** 页面点击某个已启用任务的"执行一次"按钮，任务执行后自动写入日志
2. **查看日志**: 导航到 **系统监控 → 任务执行日志**
3. **观察指标**:
   - 任务名称、Bean名、方法名
   - **耗时(ms)** — 超过 5000ms 的行会以红色高亮显示
   - 执行状态（成功=绿色/失败=红色）和结果信息
   - 开始时间
4. **筛选**: 按执行状态、时间范围筛选

#### 预期效果

- ✅ 如果系统中有启用的定时任务并执行过，打开页面应看到记录
- ✅ 耗时列在 >5秒时红色高亮
- ✅ 如果从未执行定时任务，表格为空（可手动执行一次来产生数据）
#### 8.7.9 缓存管理 (`/monitor/cache-manage`) — v2.1

| 说明 |
|------|
| Caffeine 本地缓存的可视化管理界面。展示所有缓存的名称、类型、命中次数、未命中次数、命中率、淘汰次数。支持清除单个缓存和清除全部缓存。通过 Spring CacheManager 反射获取 Caffeine 统计指标。 |

**适用场景**：运维排查——当直接执行 SQL 修改菜单/配置后，无需重启后端，清除对应缓存即可使变更生效；性能分析——通过命中率判断缓存配置是否合理。


**后端**: `CacheManageController` (`/api/monitor/cache`)  
**前端**: `views/monitor/cache-manage/index.vue`  
**权限**: 仅 admin 角色可访问  
**缓存实现**: Spring CacheManager + Caffeine 本地缓存

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 获取所有缓存信息（名称、类型、命中/未命中、命中率、淘汰次数） |
| DELETE | `/clear/{cacheName}` | 清除指定缓存 |
| DELETE | `/clear-all` | 清除全部缓存 |

#### 如何使用与测试

1. **查看缓存**: 导航到 **系统监控 → 缓存管理**，标题显示 "缓存管理 (Caffeine)"
2. **观察统计**:
   - **缓存名称**: menu（菜单缓存，按用户隔离）、dept、dict 等
   - **命中次数/未命中次数**: 缓存命中和未命中的计数
   - **命中率**: 百分比展示，高命中率说明缓存效果好
   - **淘汰次数**: 缓存被淘汰的次数
3. **清除单个缓存**: 点击某行的"清除"按钮 → 确认弹窗 → 该缓存被清空，命中/未命中计数重置
4. **清除全部缓存**: 点击顶部红色"清除全部缓存"按钮 → 确认弹窗 → 所有缓存清空
5. **刷新**: 点击刷新按钮重新加载当前缓存数据
6. **实用场景**: 直接执行 SQL 插入菜单后，清除 `menu` 缓存使新菜单立即生效（无需重启）

#### 预期效果

- ✅ 打开页面应看到至少 1 个缓存条目（menu 缓存）
- ✅ 操作几次系统（切换页面、查询菜单）后刷新，命中次数会增加
- ✅ 点击"清除"后，该缓存的统计数据归零
- ✅ 清除 menu 缓存后，重新登录会重新从数据库加载菜单（新增菜单立即可见）
---

### 8.8 权限申请 (`/permission/request`)

| 操作 | 权限标识 | 说明 |
|------|---------|------|
| 提交申请 | — | 用户选择目标角色 → 填写申请理由 → 提交 |
| 审批 | — | 管理员审核申请（通过/驳回）+ 填写审批意见 |

**适用场景**：用户自助申请角色权限。普通用户登录后访问受限制的页面时，系统引导进入权限申请页面，选择需要的角色并填写申请理由。管理员在"待办事项"中收到审批请求，审核通过后自动为用户分配角色。基于 `sys_permission_request` 表，审批状态：待审批/已通过/已驳回。

**工作流**：用户提交申请 → 记录写入 `sys_permission_request` 表 → 自动创建待办事项（`sys_todo`）→ 管理员在待办中审核 → 通过后调用 `SysUserService.addUserRole()` 分配角色。

---

## 9. 构建与部署

### 后端构建

```bash
# Maven 打包
mvn clean package -DskipTests

# 输出: target/rx-admin-1.0.0.jar

# 运行
java -jar rx-admin-1.0.0.jar --spring.profiles.active=dev
```

**配置**: `application.yml`，端口 8088

### 前端构建

```bash
cd ui/

# 开发
npm run dev     # Vite 开发服务器，端口 3000

# 生产构建
npm run build   # 输出到 ui/dist/

# 预览
npm run preview
```

**Vite 配置** (`vite.config.js`):
- 开发代理: `/api` → `http://localhost:8088`（直接透传，无 rewrite）
- 自动导入: Element Plus 组件按需导入 + 图标自动导入
- SCSS 全局变量: 自动注入 `variables.scss`
- 代码分包: `echarts` 和 `element-plus` 独立 chunk，减少首屏加载体积

### 数据库

- 主库 `rx_admin`: 系统管理表（用户/角色/菜单/部门/日志/字典/通知）
- 业务库 `rxusysadmin`: 四大名著数据表

### 初始化 SQL

项目包含 **26 个 SQL 脚本** 位于 `src/main/resources/db/` 和 `src/main/resources/sql/`：

| 脚本 | 说明 |
|------|------|
| `db/init.sql` | 数据库初始化脚本（创建库 + 系统表 + 初始数据） |
| `db/classics_menu.sql` | 四大名著菜单 SQL |
| `db/literature_menu.sql` | 历代文学菜单 SQL |
| `db/work_menu.sql` | 文学作品菜单 SQL |
| `db/analysis_menu.sql` | 接口分析菜单 SQL |
| `db/docs_menu.sql` | 项目文档菜单 SQL |
| `db/standards_menu.sql` | 开发规范菜单 SQL |
| `db/common_tools_menu.sql` | 常用工具菜单 SQL |
| `db/flowchart_menu.sql` | 流程图编辑器菜单 SQL |
| `db/permission_request.sql` | 权限申请功能（建表+菜单） |
| `db/slow_query_menu.sql` | 慢查询监控菜单 SQL |
| `db/sys_slow_query.sql` | 慢查询表结构 DDL |
| `db/techblog_init.sql` | 技术博客建表+菜单 |
| `db/enhance_menu.sql` | v2.1 增强菜单（登录日志/导出审计/任务日志/缓存管理/通知中心/数据库工具/开发工具） |
| `db/iservice_init.sql` | AS400 IService 初始化 |
| `db/iservice_migrate.sql` | AS400 IService 迁移脚本 |
| `db/common_tools_init.sql` | 常用工具初始化数据 |
| `db/migration-v1.5.0.sql` | v1.5.0 迁移（sys_config/sys_notice_read/sys_job/sys_file） |
| `db/section5_menus.sql` | 定时任务/文件管理/系统配置菜单 |
| `db/sys_config_menu.sql` | 系统配置菜单 |
| `db/update_notice_todo.sql` | 通知待办扩展（category/link_path 字段） |
| `db/honglou_characters.sql` | 红楼梦人物数据（54人） |
| `db/honglou_relations.sql` | 红楼梦人物关系数据（99条） |
| `db/update_regions.sql` | 行政区划更新脚本 |
| `db/update_regions2.sql` | 行政区划更新脚本（补充） |
| `sql/music_player_init.sql` | 音乐播放器建表 |
| `sql/music_player_menu.sql` | 音乐播放器菜单 SQL |

---

## 附录

### A. 项目文件统计

| 层级 | 文件数 | 说明 |
|------|--------|------|
| 后端 Java 源文件 | ~220+ | Entity(40+) + Controller(38) + Service(50+) + Mapper(40+) + Config(9) + Common(7) + 启动类 |
| 前端 Vue 组件 | 54+ | 49+ views + 5 个 layout 组件（含 SearchBox、NoticePopover） |
| 前端 JS 模块 | 47+ | API(37+) + Store(2) + Router(2) + Composables(5) + Utils(2) + i18n(3) |
| 样式文件 | 2 | variables.scss + global.scss |
| SQL 脚本 | 27 | init.sql + 19+ 个模块 SQL + 业务数据 SQL + 迁移脚本 |
| 国际化文件 | 2 | zh-CN.js + en-US.js（300+ 翻译条目） |
| 文档 | 2 | rxadmin.md + rxadmin-dev-skills.md |

### B. 关键设计模式

| 模式 | 应用场景 |
|------|----------|
| **双数据源** | 系统管理库 + 业务数据库分离 |
| **RBAC** | 用户 → 角色 → 菜单/权限 三层权限模型 |
| **完全动态路由** | 所有业务路由由后端菜单表驱动，`router.addRoute` 动态注入 |
| **keep-alive 缓存** | 标签页切换时保持页面状态，缓存 key 使用英文 name |
| **国际化 (i18n)** | Vue I18n 实现全站中/英文双语切换，菜单/表单/提示全覆盖，无需刷新 |
| **CSS 变量主题** | 亮色/暗色双主题一键切换，CSS 变量 + ECharts 暗黑适配 |
| **递归组件** | SubMenu.vue 无限层级菜单渲染 |
| **力导向布局** | Canvas 实现的人物关系可视化 |
| **Markdown 渲染** | 项目文档 + 开发规范页面使用 marked + highlight.js 实时渲染 |
| **AOP 异步日志** | @OperateLog 注解 + AspectJ 切面，`@Async` 异步记录 + 参数脱敏 |
| **Composable 复用** | `useStorage`（localStorage 统一管理）、`useTablePage`（表格分页通用逻辑） |
| **登录安全** | Guava RateLimiter 限流 + LoginAttemptService 失败锁定 |
| **组件拆分** | 布局拆分为 SearchBox / NoticePopover / SubMenu / TagsView 子组件 |

---





---

## 10. 项目搭建与新增模块指南

> **[独立文档]** 从零搭建项目和新增业务模块的详细步骤已独立为单独文档，避免主架构文档过于冗长。
>
> 📄 请参阅：[项目搭建与新增模块指南](./rxadmin-setup.md)

---

## 12. 路由动态化

> 已实施完全动态路由方案。所有业务路由由后端 `sys_menu` 表驱动，前端通过 `router.addRoute` 在登录后动态注入。

### 12.1 架构概览

当前采用**完全动态路由**：`constantRoutes` 仅在 `router/index.js` 中保留 `Login` 和 `Layout` 空壳，所有业务路由在登录后由后端菜单数据通过 `addRoute` 动态注册。

**数据流**：

```
后端 sys_menu 表
  → SysMenuMapper（MySQL 递归CTE 查用户菜单+祖先）
  → SysMenuService.getRouterMenus()（Caffeine 缓存，admin全量/普通用户按权限过滤）
  → GET /api/auth/routers（返回菜单树，含 path/component/icon/children）
  → 前端 fetchRouters()（持久化到 localStorage）
  → generateDynamicRoutes(menus)（递归遍历，匹配 componentMap.js）
  → router.addRoute('Layout', route)（Vue Router 动态注入）
```

**关键设计**：
- `beforeEach` 守卫纯同步，不发起后端请求（登录时已预加载所有数据）
- `menus`/`roles`/`perms` 持久化到 `localStorage`，页面刷新不丢失
- `dynamicRoutesAdded` 标记防止重复注册
- 路由 `name` 使用 `componentMap` 中的英文名（非菜单中文名），确保 `keep-alive` 缓存匹配

### 12.2 核心文件

| 文件 | 作用 |
|------|------|
| `ui/src/router/index.js` | 定义 `constantRoutes`(Login + Layout 空壳) + `generateDynamicRoutes()` 动态注册逻辑 + `beforeEach` 守卫 |
| `ui/src/router/componentMap.js` | `path → { component, name }` 映射表，key 与后端 `sys_menu.component` 对齐。新增页面只需在此追加一行 |
| `ui/src/stores/user.js` | `fetchRouters()` 调用后端接口，持久化到 localStorage；`logout()` 时调用 `resetDynamicRoutes()` 清理 |
| `ui/src/api/auth.js` | `getRoutersApi()` → `GET /api/auth/routers` |

### 12.3 新增菜单维护指南

#### 页面组件已存在（纯后端操作）

当 `.vue` 页面和 `componentMap.js` 映射已就绪，仅需上线：

1. 菜单管理页面 → 新增菜单记录，填写 `path`、`component`（与 `componentMap` key 一致）、`icon` 等
2. 角色管理 → 分配角色权限
3. 用户刷新后侧边栏自动渲染，路由自动注册

#### 全新页面组件（前后端配合）

1. **前端** — 开发 `.vue` 页面并声明 `defineOptions({ name: 'xxx' })`
2. **前端** — `componentMap.js` 追加一行映射
3. **后端** — 菜单管理新增记录，`component` 字段与映射 key 一致

**无需修改 `router/index.js`。**

### 12.4 后端 `sys_menu.component` 与前端路径对应

| 菜单名 | component (数据库) | 前端文件路径 |
|--------|-------------------|-------------|
| 仪表盘 | `dashboard/index` | `views/dashboard/index.vue` |
| 用户管理 | `system/user/index` | `views/system/user/index.vue` |
| 角色管理 | `system/role/index` | `views/system/role/index.vue` |
| 菜单管理 | `system/menu/index` | `views/system/menu/index.vue` |
| 部门管理 | `system/dept/index` | `views/system/dept/index.vue` |
| 字典管理 | `tool/dict/index` | `views/tool/dict/index.vue` |
| 通知公告 | `content/notice/index` | `views/content/notice/index.vue` |
| 操作日志 | `monitor/log/index` | `views/monitor/log/index.vue` |
| 在线用户 | `monitor/online/index` | `views/monitor/online/index.vue` |
| 红楼诗词 | `classics/honglou/poems/index` | `views/classics/honglou/poems/index.vue` |
| … | （其余模块遵循相同命名约定） | |

### 12.5 踩坑记录

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 死循环 `Maximum call stack` | Layout `redirect: '/dashboard'` 在 children 为空时死循环 | 删除 redirect，在 `beforeEach` 中处理 `/` → `/dashboard`；用 `dynamicRoutesAdded` 标记 + `router.resolve()` 验证 + 冷却时间保护 |
| 搜索菜单触发死循环 | 搜索父级菜单（无 component）后跳转到不存在路径 | 搜索 `flattenMenus` 只收集有 `component` 的叶子菜单 |
| 切换标签重复请求 | 动态路由 name 用中文菜单名，与组件 `defineOptions name` 不匹配，keep-alive 失效 | `componentMap` 增加 `name` 字段，使用英文名确保与组件一致 |
| 刷新后路由丢失 | `menus` 只存 Pinia 内存 | 持久化到 `localStorage`，初始化时恢复

---

## 13. 启动命令

### 首次启动前必须执行的数据库脚本

> ⚠️ **重要**: `db/features_init.sql` 和 `db/features_menu.sql` 不会自动执行。全新部署时必须在 MySQL 中手动执行后再启动后端，否则 v2.0 功能（快捷收藏夹、IP黑白名单、站内消息等）将因缺少对应数据表而报错 `"系统繁忙，请稍后再试"`。

```bash
# MySQL 命令行执行
mysql -u root -p rx_admin < db/features_init.sql
mysql -u root -p rx_admin < db/features_menu.sql
```

### 后端启动

```powershell
# Windows PowerShell（本地开发，激活 local profile）
cd D:\vueprojects\RX
$env:AS400_HOST="pub400.com"; $env:AS400_USERNAME="A7RXUZZ"; $env:AS400_PASSWORD="A7R3NZZ"; $env:MYSQL_PASSWORD="root"; $env:SPRING_PROFILES_ACTIVE="local"; mvn spring-boot:run
```

> **注意**: 必须通过 `$env:SPRING_PROFILES_ACTIVE="local"` 激活 local profile，以加载 `application-local.yml` 中的邮件 SMTP 等配置。PowerShell 下不能直接用 `-Dspring-boot.run.profiles=local`（点号会被错误解析）。

```bash
# 或使用 Maven 打包运行
mvn clean package -DskipTests
java -jar target/rx-admin-1.0.0.jar --spring.profiles.active=dev
```

### 前端启动

```bash
cd D:\vueprojects\RX\ui

# 开发模式
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

### 生产模式启动

```bash
# 后端（使用 application-prod.yml 配置，关闭 SQL 控制台输出）
java -jar target/rx-admin-1.0.0.jar --spring.profiles.active=prod
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:3001 |
| 后端 API | http://localhost:8088 |
| API 文档 (Knife4j) | http://localhost:8088/doc.html |

---


---

## 14. 安全机制（在线用户/踢出/XSS/防重放/加密脱敏）

### 14.1 在线用户追踪（OnlineUserService）

**类**: `com.rx.admin.service.OnlineUserService`

使用 `ConcurrentHashMap<String, Map<String, Object>>` 内存存储，key 为 tokenValue，value 包含：

```json
{
  "tokenId": "tokenValue",
  "loginId": "userId",
  "loginTime": "2026-06-05 10:00:00",
  "username": "admin",
  "nickname": "管理员"
}
```

核心方法：

| 方法 | 触发时机 | 说明 |
|------|---------|------|
| `userLoggedIn(token, userId)` | 登录成功 | 移除该用户的旧记录，记录新会话 |
| `userLoggedOut(token)` | 主动退出 | 从 Map 中移除 |
| `kickOutByUserId(userId)` | 管理员踢出 | 移除该用户所有会话 |
| `getOnlineUsers()` | 查询列表 | 自动清理过期 Token 后返回 |
| `getOnlineCount()` | 统计数量 | 自动清理过期 Token |

**自动清理机制**: 每次查询列表或统计数量时，遍历 Map 调用 `StpUtil.getLoginIdByToken(token)` 校验 Token 有效性，已过期/退出的 Token 自动移除。

### 14.2 踢出逻辑

**后端流程**:
1. 管理员点击「踢出」→ `DELETE /api/monitor/online/{tokenValue}`
2. `SysOnlineController.kickOut()` 执行：
   - `OnlineUserService.userLoggedOut(token)` — 移除在线列表记录
   - `StpUtil.kickoutByTokenValue(token)` — Sa-Token 强制使 Token 失效
3. 被踢用户的下一次请求 → Sa-Token 抛出 `NotLoginException`，返回 JSON `{ code: 401, message: "KICK_OUT" }`

**前端流程**:
1. Axios 响应拦截器收到 `401` + `"KICK_OUT"` → 调用 `showKickOutOverlay()`
2. 创建全屏半透明遮罩层（z-index: 99999），显示 "已被强制下线"
3. 5 秒倒计时 → 倒计时结束后自动跳转到 `/login`

**关键文件**:
- 后端: `SysOnlineController.java`, `OnlineUserService.java`
- 前端: `utils/request.js` — `showKickOutOverlay()` 函数, `online/index.vue` — 在线用户列表

### 14.3 心跳机制

**目的**: 被踢出的用户无需等待下一次操作，能在 10 秒内收到踢出通知。

**实现**:
1. `request.js` 模块加载时自动调用 `startHeartbeat()`
2. 每隔 10 秒发送 `GET /api/auth/ping`（轻量接口，无业务逻辑）
3. 被踢出后 Sa-Token 返回 401 + "KICK_OUT" → 心跳请求触发遮罩覆盖
4. 心跳在无 Token 时自动跳过（不浪费资源）

```javascript
/** 会话心跳定时器 */
let heartbeatTimer = null

function startHeartbeat() {
  if (heartbeatTimer) return
  heartbeatTimer = setInterval(() => {
    const token = tokenStore.get()
    if (!token) return  // 无 token 时跳过
    request({ url: '/api/auth/ping', method: 'get', timeout: 5000 }).catch(() => {})
  }, 10000)
}
```

### 14.4 验证码机制

**后端**: `CaptchaController` + `CaptchaService` + `CaptchaUtil`
- `GET /api/auth/captcha` → 返回 `{ uuid, image(base64) }`
- 4 位随机字母数字验证码（排除易混淆字符）
- 5 分钟 TTL，一次性使用，定时清理过期缓存

**前端**: `login/index.vue`
- 点击验证码图片可刷新
- 登录时提交 `captchaUuid` 和 `captchaCode`

### 14.5 XSS 防护

**后端**: `XssJacksonConfig.java` — Jackson 全局 String 反序列化时自动 HTML 转义

### 14.6 API 防重放

**后端**: `ReplayAttackFilter.java`
- 验证 `X-Timestamp` + `X-Nonce` 请求头
- 时间窗口 300 秒，nonce 不可重复使用
- 仅对 POST/PUT/DELETE 写操作生效

**前端**: `auth.js` — 自动生成 X-Timestamp 和 X-Nonce 头

### 14.7 敏感数据加密与脱敏

**后端**: `AesTypeHandler.java` — MyBatis-Plus 字段级 AES-128/ECB/PKCS5Padding 加密/解密（手机号、邮箱），密钥 `RxAdmin!@#2026!!`

**数据库列长度要求**（加密 + Base64 后长度显著增长）：

| 字段 | 明文最大长度 | 加密后最大长度 | 数据库列定义 |
|------|------------|--------------|-------------|
| `email` | 100 字符 | ~150 字符 | `VARCHAR(100) DEFAULT NULL` |
| `phone` | 11 字符 | ~24 字符 | `VARCHAR(100) DEFAULT NULL` |

> **注意**: `phone` 列原为 `VARCHAR(20)`，AES 加密后至少 24 字符，已扩容至 `VARCHAR(100)`。`email` 和 `phone` 均改为 `DEFAULT NULL`（MySQL UNIQUE 约束允许多个 NULL）。

**唯一性约束**：
- `email` — 数据库 `UNIQUE KEY uk_email` + 代码层 `SysUserService.isEmailDuplicate()` 双重保障
- 修改个人信息时，空白邮箱/手机号统一转为 `NULL` 存储，避免空字符串冲突

**脱敏显示**：`DataMaskUtil.java` — 返回前端时脱敏显示（手机号 138****1234，邮箱 tes***@example.com）

### 14.8 SSE 实时推送

`GET /api/dashboard/stream` — SSE 端点，每 30 秒推送统计数据。前端 `dashboard/index.vue` 通过 EventSource 消费。

## 15. 密码策略（P1）

**方案**: 后端 @Pattern 正则校验 + 前端密码强度指示器。
- 后端: RegisterRequest.java 使用 @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\\\d).{6,}$") 校验注册/修改密码
- SysUserService.java 中 ddUser() / updateUser() 调用 alidatePassword() 校验
- 前端: PasswordStrength.vue 组件实时显示密码强度（弱/中/强/非常强）
- 强度算法: usePasswordStrength.js 根据长度、字符类型组合评分

**涉及文件**:
- RegisterRequest.java, SysUserService.java
- ui/src/components/PasswordStrength.vue
- ui/src/composables/usePasswordStrength.js
- ui/src/views/system/user/index.vue（用户管理对话框）
- ui/src/views/login/index.vue（注册表单）
- ui/src/i18n/lang/en-US.js, zh-CN.js（强度标签翻译）

---

## 16. i18n 国际化

### 菜单名称翻译
- 文件: `ui/src/composables/useMenuI18n.js`
- 所有侧边栏菜单名称均通过 `tMenu(menuName)` 翻译
- 映射表中缺失的菜单名会直接显示中文原文

### 语言文件
- 中文: `ui/src/i18n/lang/zh-CN.js`
- 英文: `ui/src/i18n/lang/en-US.js`

### 已覆盖菜单（56项）
- 仪表盘、系统管理(6子项)、系统工具(10子项)、内容管理(1子项)
- AS400管理(4子项)、系统监控(3子项)、四大名著(14子项)
- 历代文学(3子项)、常用工具(6子项)、音乐播放、权限申请、个人中心

### 添加新菜单时的步骤
1. 在 `sys_menu` 表中插入菜单记录
2. 在 `componentMap.js` 添加组件映射
3. 在 `useMenuI18n.js` 添加中文->i18nKey 映射
4. 在 `zh-CN.js` 和 `en-US.js` 添加对应的翻译文本

## 17. v2.0 新增功能模块 (2026-06-05)

> v2.0 版本新增 12 大功能，涉及 3 张新数据库表、14 个后端类、20 个前端文件。

### 17.1 全局命令搜索 (Ctrl+K)

`components/CommandPalette.vue` — 类似 VS Code 的 Spotlight 风格命令面板。

| 能力 | 说明 |
|------|------|
| 快捷键 | `Ctrl+K` 全局唤起 |
| 页面搜索 | 从 `userStore.menus` 递归提取所有叶子菜单模糊匹配 |
| 最近访问 | 从 `tagsStore.visitedViews` 显示最近 5 个标签页 |
| 快捷操作 | 暗黑模式切换、全屏切换、返回首页、退出登录 |

**集成**: Layout 顶栏按钮 + `CommandPalette` 挂载在 Layout 底部。

### 17.2 系统健康监控

**前端**: `views/monitor/health/index.vue` | **后端**: `HealthController` + `HealthService`

| 接口 | 说明 |
|------|------|
| `GET /api/monitor/health/system` | CPU使用率/核心数、内存总量/已用、JVM堆/非堆、线程数/峰值、磁盘使用率 |
| `GET /api/monitor/health/gc` | GC 统计（回收次数+耗时） |

每 10 秒自动刷新，4 个统计卡片 + ECharts 仪表盘 + JVM 详情表格。

### 17.3 IP 黑白名单

**前端**: `views/system/ipRule/index.vue` | **后端**: `SysIpRuleController` + `SysIpRuleService`

| 新增表 | 字段 |
|--------|------|
| `sys_ip_rule` | id, ip_address, rule_type(BLACK/WHITE), description, status, 继承 BaseEntity |

CRUD + 分页搜索，支持黑名单/白名单/关闭三模式切换，模式配置写入 `sys_config` 表 (`ip.filter.mode`)。

### 17.4 站内消息中心

**前端**: `views/content/message/index.vue` + `NoticePopover.vue`（铃铛弹窗集成） | **后端**: `SysMessageController` + `SysMessageService`

| 新增表 | 字段 |
|--------|------|
| `sys_message` | id, sender_id, receiver_id, title, content, message_type, is_read, read_time, link_path, 继承 BaseEntity |

**消息中心页面**：el-timeline 时间轴展示，类型筛选（全部/系统消息/通知/私信），标记已读/全部已读，未读红点 + 分类统计。权限控制：管理员可查看全部用户的消息或按 `userId` 筛选，普通用户只能看自己的。

**铃铛弹窗集成**：`NoticePopover.vue` 新增"消息"Tab，显示消息中心最近 20 条（按 `createTime` 倒序），按类型标签区分（系统→红/通知→橙/信息→蓝）。点击消息→调用 API 标记已读→跳转到消息中心页面。消息 Tab 下"全部已读"调用 `markAllReadApi`，"查看全部"跳转到 `/content/message`。

**SysMessageService 消息发送方法**：
| 方法 | 说明 |
|------|------|
| `sendSystemMessage(title, content, receiverId)` | 给指定用户发送 system 类型消息 |
| `sendNotificationMessage(title, content, receiverId, linkPath)` | 发送带链接跳转的 notice 类型消息 |
| `sendInfoMessage(title, content, receiverId)` | 发送 info 类型消息（用于个人信息变更等通知） |
| `sendToAll(title, content, messageType, linkPath)` | 广播消息给所有启用状态的用户 |
| `sendToRoleUsers(roleCode, title, content)` | 给指定角色的所有用户发送消息（如通知所有管理员） |
| `sendToRoleUsers(roleCode, title, content, excludeUserId)` | 同上，可排除指定用户（避免操作者自身收到自己的操作通知） |

**用户个人信息变更通知机制**：`AuthService.updateProfile()` 中用户修改个人信息后，同时发送两条消息：
- 用户本人收到 info 类型消息：`"您于 yyyy-MM-dd HH:mm:ss 更新了：邮箱、手机号"` → 使用"您"指代
- 所有管理员收到 system 类型消息：`"用户 张三(zhangsan) 于 yyyy-MM-dd HH:mm:ss 更新了：邮箱、手机号"` → 显示具体用户昵称和用户名
- 若操作者本身是管理员角色，则自动排除自身不重复收到

### 17.5 快捷收藏夹

**组件**: `FavoriteStar.vue` (页面星标) + `FavoritesPanel.vue` (侧边栏收藏面板)

| 新增表 | 字段 |
|--------|------|
| `sys_user_favorite` | id, user_id, menu_id, name, path, icon, sort_order |

POST `/api/system/favorite/toggle` 一键切换收藏/取消，localStorage 缓存加速渲染。

### 14.6 系统公告弹窗

**组件**: `AnnouncementPopup.vue` — 登录后自动检测 `sys_notice` 中 `noticeType='2'` 的未读公告，el-dialog 弹窗，一次一条，localStorage 记录去重。

**后端**: `GET /api/content/announcement/popup`

### 17.7 代码生成器

**前端**: `views/tool/gen/index.vue` | **后端**: `GenController`

三步向导（选表→配置→预览生成），从 `information_schema` 读取表结构，生成完整 Entity/Mapper/Service/Controller/Vue/API 模板。

类型映射: bigint→Long, int/tinyint→Integer, datetime→LocalDateTime, decimal→BigDecimal

### 17.8 批量数据导入

**前端**: `views/tool/importData/index.vue` | **后端**: `ImportController`

三步向导：上传 Excel/CSV→ 预览校验 → 执行导入，含错误行高亮和成功/失败统计。

### 17.9 操作日志可视化分析

**前端**: `views/monitor/logAnalysis/index.vue` | **后端**: `LogAnalysisController`

4 统计卡片（今日操作数/异常数/活跃用户/最高频操作）+ ECharts 柱状图（时段分布）+ 饼图（类型分布）+ 折线图（7天趋势）。

### 17.10 API 调试面板

**前端**: `views/tool/apiDebug/index.vue` | **后端**: `ApiDebugController`

左右分栏：左侧 API 端点列表（`RequestMappingHandlerMapping` 扫描），右侧请求编辑+发送+响应展示，类似 Postman。

### 17.11 数据库备份与恢复

**前端**: `views/tool/backup/index.vue` | **后端**: `BackupController`

mysqldump 备份到 `backups/` 目录，支持列表管理、下载、还原、删除。

### 17.12 多主题色系统

`styles/themes.scss` + `composables/useLayoutSettings.js`

5 套预设主题色（默认蓝/翡翠绿/深紫/暖橙/青色），通过 `data-theme` 切换 CSS 变量，Element Plus 主色联动。

### 17.13 新增文件汇总

| 类别 | 数量 | 文件 |
|------|------|------|
| 数据库脚本 | 2 | `features_init.sql` (建表), `features_menu.sql` (菜单) | ⚠️ 需手动执行 |
| 后端 Entity | 3 | SysIpRule, SysMessage, SysUserFavorite |
| 后端 Mapper | 3 | SysIpRuleMapper, SysMessageMapper, SysUserFavoriteMapper |
| 后端 Service | 4 | SysIpRuleService, SysMessageService, SysUserFavoriteService, HealthService |
| 后端 Controller | 10 | SysIpRule, SysMessage, SysUserFavorite, Health, Announcement, Gen, Import, ApiDebug, LogAnalysis, Backup |
| 前端 API | 10 | health, ipRule, message, favorite, announcement, gen, importData, logAnalysis, apiDebug, backup |
| 前端页面 | 8 | health, ipRule, message, gen, importData, logAnalysis, apiDebug, backup |
| 前端组件 | 4 | CommandPalette, FavoriteStar, FavoritesPanel, AnnouncementPopup |
| 前端样式/工具 | 2 | themes.scss, useLayoutSettings.js |
| 集成修改 | 3 | componentMap.js (+8), layout/index.vue, main.js |


---

## 18. Git 版本控制功能集成方案（待实施）

> **[独立文档]** Git 版本控制功能集成方案的详细技术设计已独立为单独文档。
>
> 📄 请参阅：[Git 版本控制功能集成方案](./rxadmin-git.md)

---

## 19. 待实施增强建议

> **评估日期**: 2026-06-06 | **更新**: 2026-06-06
>
> 以下增强项已在 v2.1 中完成实现并迁移为正文内容 → 详见 [第 17 节 v2.1 系统增强](#17-v21-系统增强已实现)。本章仅保留尚未实施的增强建议。

### 19.1 v2.1 已实施增强速查

| 增强项 | 对应章节 | 页面路径 |
|--------|---------|----------|
| 登录失败锁定 + 密码策略 | [17.1](#171-数据库变更) | 系统管理 → 用户管理（sys_user 已含 lock_until/密码历史等字段） |
| 登录日志独立记录 | [17.2](#172-登录日志) | 系统监控 → 登录日志 |
| 导出审计 | [17.3](#173-导出审计) | 系统监控 → 导出审计 |
| 任务执行日志 | [17.4](#174-任务执行日志) | 系统监控 → 任务执行日志 |
| 缓存管理 | [17.5](#175-缓存管理) | 系统监控 → 缓存管理 |
| 通知中心（消息模板+发送记录） | [17.6](#176-通知中心) | 内容管理 → 通知中心 |
| 在线SQL控制台 + 表结构查看 + 连接池监控 | [17.7](#177-数据库管理工具) | 系统工具 → 数据库工具 |
| 开发小工具集（JSON/Base64/UUID/时间戳/二维码/正则） | [17.8](#178-开发小工具集) | 系统工具 → 开发工具 |
| 仪表盘增强（登录统计/导出统计） | [17.9](#179-仪表盘增强) | 仪表盘 |
| 行级数据权限 | [17.10](#1710-数据权限) | 系统管理 → 角色管理（data_scope 字段） |

---

### 19.2 安全类增强（待实施）

#### 19.2.1 会话管理增强

**现状**：可查看在线用户和强制踢人，缺会话超时配置。

**方案**：

```yaml
sa-token:
  # 会话超时(秒)，默认30分钟
  timeout: 1800
  # 临时Token超时
  activity-timeout: 300
  # 是否允许同一账号并发登录 (false=互踢)
  is-concurrent: false
  # 最大登录设备数(concurrent=true时生效)
  max-login-count: 3
```

增加前端"登录设备管理"页面，展示当前账号所有活跃会话，支持选择性下线。

#### 19.2.2 高危操作二次确认

**现状**：高危操作（删除用户、清空日志）仅有前端确认弹窗。

**方案**：高危操作API端强制要求二次密码验证或验证码：

```java
@PostMapping("/delete/{id}")
@OperateLog("删除用户")
public Result<Void> delete(@PathVariable Long id, @RequestParam String confirmPassword) {
    // 验证当前登录用户密码
    String currentPwd = StpUtil.getLoginIdAsString();
    if (!passwordEncoder.matches(confirmPassword, currentUser.getPassword())) {
        throw new BusinessException("密码验证失败");
    }
    userService.removeById(id);
    return Result.ok();
}
```

---

### 19.3 日志与监控增强（待实施）

#### 19.3.1 异常告警聚合

**方案**：利用 Caffeine 缓存做滑动窗口计数：

```java
// 缓存key: "error:agg:{异常类名}"
// 5分钟内同一异常 >= 10次 → 站内消息通知管理员
Cache<String, AtomicInteger> errorCounter = Caffeine.newBuilder()
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .build();
```

#### 19.3.2 接口耗时监控

**方案**：使用 AOP 或 Filter 记录所有接口耗时分布，仿照慢查询记录机制：

```sql
CREATE TABLE sys_api_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_path VARCHAR(200),
    request_count BIGINT DEFAULT 0,
    avg_time DECIMAL(10,2),      -- 平均耗时(ms)
    p95_time DECIMAL(10,2),      -- P95耗时
    p99_time DECIMAL(10,2),      -- P99耗时
    max_time DECIMAL(10,2),
    record_date DATE,
    UNIQUE KEY uk_path_date (api_path, record_date)
);
```

---

### 19.4 任务调度增强（部分已完成）

> ✅ 任务执行日志已实现 ([17.4](#174-任务执行日志))，以下为待实施项。

| 功能 | 说明 |
|------|------|
| **失败重试** | 任务失败后自动重试N次，间隔递增 |
| **Cron可视化** | 输入cron表达式 → 实时预览下5次执行时间 |
| **手动触发** | 已支持"执行一次"功能 ✅ |

---

### 19.5 数据可视化与报表（部分已完成）

> ✅ 仪表盘登录/导出统计已实现 ([17.9](#179-仪表盘增强))，以下为待实施项。

#### 19.5.1 Dashboard 仪表盘进一步增强

| 图表 | 说明 | 数据源 |
|------|------|--------|
| 用户活跃趋势 | 日活/周活/月活折线图 | `sys_login_log` |
| 操作频率排行 | Top10 高频操作柱状图 | `sys_log` |
| 功能使用热度 | 各菜单访问频次饼图 | `sys_log` |
| 错误趋势 | 系统异常数折线图 | `sys_log` |
| 最近登录记录 | 最近10条登录卡片 | `sys_login_log` |

#### 19.5.2 报表导出增强

**现状**：已有 Excel 导出基础组件。

**增强**：
- 支持 PDF 导出（使用开源库如 Apache PDFBox 或 iText）
- 支持导出模板配置（选择哪些列、排序方式）
- `sys_export_config` 表已有基础，可扩展

---

### 19.6 全站搜索（待实施）

**方案**：顶部搜索框，支持搜索菜单、用户、文档、日志等。

```
前端交互：
┌──────────────────────────────────┐
│  [🔍 搜索...              ]     │
│  ─────────────────────────      │
│  📋 菜单: 用户管理 → 系统管理    │
│  👤 用户: admin (管理员)        │
│  📄 文档: API接口文档            │
│  📝 日志: 删除用户操作 (5条)     │
└──────────────────────────────────┘
```

**后端实现**：一个聚合搜索接口，并发查询多个数据源：

```java
// SearchController.java
@GetMapping("/search")
public SearchResultVO search(@RequestParam String keyword) {
    // 并发搜索：菜单、用户、文档、日志
    CompletableFuture<List<MenuHit>> menus = searchMenus(keyword);
    CompletableFuture<List<UserHit>> users = searchUsers(keyword);
    CompletableFuture<List<LogHit>> logs = searchLogs(keyword);
    // 合并结果，各类型返回Top 5
    return SearchResultVO.merge(menus, users, logs, ...);
}
```

### 19.7 多租户扩展与实施优先级矩阵

> **[独立文档]** 多租户远期规划与实施优先级矩阵已移入独立文档。
>
> 📄 请参阅：[Git 版本控制功能集成方案与远期规划](./rxadmin-git.md)

---
## 20. 待修复项（生产发布前）

> 从原「项目优化清单」中提取的尚未完成的修复项。已完成项不再列出，其实现细节已在各对应章节中体现。

### 20.1 安全配置（P0）

| 项目 | 文件 | 说明 |
|------|------|------|
| 移除前端默认密码 | ui/src/views/login/index.vue | 生产环境需将硬编码的 admin/admin123 移除 |
| CORS 域名白名单 | CorsConfig.java | 当前允许所有来源，生产需限制为实际域名 |
| SQL 明文密码注释 | src/main/resources/db/init.sql | 移除 SQL 注释中的明文密码提示 |

### 20.2 架构增强（P2）

| 项目 | 说明 |
|------|------|
| Sa-Token Redis 存储 | 当前使用内存模式，重启后 Token 失效。生产需引入 sa-token-alone-redis |

### 20.3 工程化（P3）

| 项目 | 说明 |
|------|------|
| TypeScript 迁移 | 渐进式路线：API 层 → Store 层 → 核心组件 |
| ESLint + Prettier | 引入代码规范工具 |
| 单元测试 | 后端 JUnit 5 + Mockito，前端 Vitest |
| 操作日志查询增强 | 增加状态筛选和时间范围筛选 |
| 国际化补全 | 补充 job、file、slow-query 模块的 i18n 翻译 |

> **注**: 此节与第 19 节「待实施增强建议」互补——本节关注已有系统的修复与完善，第 19 节关注新功能的规划与设计。

---

## 21. 文档与项目差异分析

> **评估日期**: 2026-06-06 | 对照项目代码逐一验证文档描述准确性

### 21.1 文档描述与代码一致的项

| 模块 | 文档位置 | 验证结果 |
|------|---------|----------|
| 登录日志 | 8.7.6 | ✅ `sys_login_log` 表存在，前后端完整 |
| 导出审计 | 8.7.7 | ✅ 菜单位于 系统监控→导出审计 (`/monitor/export-log`)，前后端完整 |
| 缓存管理 | 8.7.9 | ✅ 页面 `/monitor/cache-manage` 可正常访问 |
| 通知中心 | 8.4.3 | ✅ 消息模板+发送记录双Tab，前后端完整 |
| 数据库工具 | 8.3.10 | ✅ SQL控制台+表结构+连接池三合一 |
| 开发小工具 | 8.3.11 | ✅ JSON/UUID/Base64/时间戳/二维码/正则 6合1 |
| 快捷收藏夹 | 17.5 (原 v2.0 14.5) | ✅ `FavoritesPanel.vue` 嵌入左侧栏底部，`FavoriteStar.vue` 页面星标按钮 |
| 全局搜索 Ctrl+K | 17.1 (原 v2.0 14.1) | ✅ `CommandPalette.vue` 支持菜单搜索+最近访问+快捷操作 |
| 系统健康监控 | 17.2 (原 v2.0 14.2) | ✅ CPU/内存/JVM/磁盘监控 + ECharts 仪表盘 |
| IP黑白名单 | 17.3 (原 v2.0 14.3) | ✅ `sys_ip_rule` 表 + 三种模式(BLACK/WHITE/OFF) |
| 站内消息中心 | 17.4 (原 v2.0 14.4) | ✅ `sys_message` 表 + el-timeline + 铃铛弹窗 |
| API调试面板 | 17.10 (原 v2.0 14.10) | ✅ Postman风格左右分栏，自动扫描API端点 |
| 代码生成器 | 17.7 (原 v2.0 14.7) | ✅ 三步向导(选表→配置→预览生成) |
| 批量数据导入 | 17.8 (原 v2.0 14.8) | ✅ Excel/CSV上传预览校验导入 |
| 数据库备份恢复 | 17.11 (原 v2.0 14.11) | ✅ mysqldump备份到 `backups/` 目录 |
| 多主题色 | 17.12 (原 v2.0 14.12) | ✅ 5套预设主题色，CSS变量切换 |
| 仪表盘增强 | 8.1 (原 20.9) | ✅ 今日登录统计+导出统计API已实现，但部分趋势/排行接口为占位 |

### 21.2 文档描述与实际代码不符的项

#### 21.2.1 数据权限 — 文档说"已实现"但不完整

**文档描述** (原 20.10 / 17.10，详见第 8 节)：

> 实现: `DataScopeService` + `@DataScope` 注解
> 支持5级数据范围...在角色管理页面编辑角色→设置"数据权限范围"

**实际代码状态**（截至 2026-06-06）：

| 组件 | 状态 | 说明 |
|------|------|------|
| `SysRole` 实体 `dataScope`/`dataDeptIds` 字段 | ✅ 已定义 | 数据库字段已存在 |
| `DataScopeService.getVisibleDeptIds()` | ✅ 已实现 | 核心逻辑完整 |
| `@DataScope` 注解 | ✅ 已定义 | 可标记 Mapper 方法 |
| `DataScopeInnerInterceptor` | ✅ 已实现 (本次) | MyBatis Plus 拦截器，自动注入SQL条件 |
| `MybatisPlusConfig` 注册拦截器 | ✅ 已实现 (本次) | 拦截器已注册到插件链 |
| 前端角色编辑弹窗 `dataScope` 选择 | ✅ 已实现 (本次) | radio-group + 自定义部门 tree-select |
| i18n 翻译键 | ✅ 已实现 (本次) | 中英文 dataScope 相关翻译 |

> **结论**: v2.1 原计划的数据权限功能现已补全（本次更新）。之前的文档声称"已实现"但实际缺失拦截器和前端UI，现已补齐。

#### 21.2.2 全站搜索 — Ctrl+K 为部分实现

**文档描述** (19 / 原16.6)：聚合搜索菜单+用户+文档+日志

**实际**: `CommandPalette.vue` (Ctrl+K) 实现了菜单搜索+最近访问+快捷操作，但**未搜索**用户、文档、日志等后端数据。建议将此条标记为"基础已实现，扩展待定"。

#### 21.2.3 仪表盘 Top10/趋势 — 占位接口

**文档描述** (原 20.9 / 17.9，详见第 8 节仪表盘)：操作日志 Top10、登录趋势等

**实际**: ✅ **已修复 (2026-06-07)**。`DashboardEnhancedController` 的 `login-stats` 的 `trend` 字段已实现为最近 7 天登录趋势（`sys_login_log` 按日 GROUP BY + 缺失日期补 0），`operation-top10` 已实现为今日操作频次 Top10（`sys_log` 按 `operation` 字段 GROUP BY + LIMIT 10）。

### 21.3 文档缺失模块描述

以下模块已确认文档覆盖情况（2026-06-07 复查）：

| 模块 | 状态 | 文档位置 |
|------|:--:|------|
| IP 黑白名单 | ✅ 已有 | 8.2.6 IP 黑白名单 |
| 系统健康监控 | ✅ 已有 | 8.7.4 健康监控 |
| 代码生成器 | ✅ **已新增** | 8.3.12 代码生成器 |
| 批量导入 | ✅ **已新增** | 8.3.13 批量导入 |
| 数据库备份恢复 | ✅ 已有 | 8.3.9 数据库备份恢复 |
| 站内消息 | ✅ 已有 | 8.4.2 消息中心 |
| 公告弹窗 | ✅ 已有 | 8.4.1 通知公告（弹窗公告类型） |
| 收藏夹 | ✅ **已新增** | 5.6 收藏夹 |
| 全站命令面板 Ctrl+K | ✅ **已新增** | 5.7 全站命令面板 |
| API 调试 | ✅ **已新增** | 8.3.14 API 调试 |

**结论**：10 个模块全部已在文档中覆盖，无遗漏。

### 21.4 文档维护建议

以下建议已完成：

1. **版本化** ✅：文档头部已标注版本号 `2.2.0` 和更新日期 `2026-06-07`。
2. **自动化** ✅：已编写 `scan_menus.py` 脚本，可自动扫描 `sys_menu` 表生成模块清单（需数据库连接配置）。
3. **差异检查** ✅：每次重大发布后对照此第 21 节重新验证，本节省略已完成条目，仅保留待处理差异。

### 21.5 v1.4.0 已完成但未纳入优化审查的模块

> 本节覆盖 v1.4.0 所有独立模块的优化审查，补充到第 20 节「待修复项」的工程化规划中。

以下模块已完成功能审查，明确后续优化方向与优先级：

| 模块 | 审查结论 | 优化方向 | 优先级 |
|------|------|------|:--:|
| 音乐播放器 | 功能完整，无阻塞缺陷 | 扩展音频格式支持 (flac/wav)、歌单管理、歌词 LRC 同步 | P3 |
| 技术博客 | 功能完整，抓取稳定 | 全文搜索 (Elasticsearch)、RSS 订阅、自动定时增量抓取 | P3 |
| 常用工具 | Excel/PDF/Word 互转可用 | 批量转换队列、格式兼容性加固、大文件流式处理 | P3 |
| 流程图编辑器 | 三引擎均可用 | 引擎统一为 LogicFlow 3.x、图形模板库、导出 SVG/PNG | P3 |
| API 分析工具 | 调用链分析可用 | 增加接口覆盖率统计、环形依赖检测、性能分析 | P3 |
| 中国行政区划 | 数据完整可搜索 | 民政部数据同步自动更新机制、GPS 坐标补充 | P3 |

**审查结论**：所有 v1.4.0 模块功能正常，均属于 P3（工程化增强），无需阻塞性修复。已纳入第 20.3 节「工程化（P3）」的长期规划。

---

> **文档维护**: 本文档由项目代码自动分析生成，建议在重大版本更新后重新生成。最后更新: 2026-06-07。
