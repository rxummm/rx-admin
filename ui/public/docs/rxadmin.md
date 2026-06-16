# RX Admin 通用管理系统 — 项目技术架构文档

> **版本**: 1.0.0 | **更新日期**: 2026-05-31 | **文档类型**: 技术架构说明书

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
5. [布局与样式](#5-布局与样式)
   - [5.1 整体布局](#51-整体布局)
   - [5.2 UI 组件库](#52-ui-组件库)
   - [5.3 主题系统](#53-主题系统)
   - [5.4 全局样式](#54-全局样式)
   - [5.5 响应式与动画](#55-响应式与动画)
6. [四大名著模块](#6-四大名著模块)
7. [构建与部署](#7-构建与部署)

---

## 1. 项目概述

**RX Admin** 是一个基于 **Spring Boot 3 + Vue 3** 的通用后台管理系统，采用前后端分离架构。系统包含完整的用户/角色/菜单/部门/字典等 RBAC 权限管理功能，以及四大名著（红楼梦、三国演义、水浒传、西游记）的经典文化数据管理模块。

### 核心功能模块

| 模块 | 说明 |
|------|------|
| **认证授权** | 登录/注册/Token 管理，基于 Sa-Token |
| **系统管理** | 用户、角色、菜单、部门 CRUD，RBAC 权限模型 |
| **系统工具** | 字典管理、行政区划、接口分析、项目文档 |
| **系统监控** | 操作日志、在线用户 |
| **内容管理** | 通知公告 |
| **仪表盘** | 统计概览 |
| **历代文学** | 历代文学作品管理（作者/朝代/体裁/内容分类） |
| **四大名著** | 红楼梦/三国/水浒/西游的人物、诗词、关系、章节等数据管理 |
| **国际化** | 中/英文双语切换，菜单/表单/提示全量翻译 |

---

## 2. 技术栈总览

| 层级 | 技术 | 版本 |
|------|------|------|
| **运行环境** | Java / Node.js | Java 17 / Node 18+ |
| **后端框架** | Spring Boot | 3.5.15 |
| **ORM** | MyBatis Plus | 3.5.5 |
| **安全认证** | Sa-Token | 1.37.0 |
| **API 文档** | Knife4j (OpenAPI 3) | 4.4.0 |
| **数据库** | MySQL | 8.x |
| **JSON** | Jackson | — |
| **密码加密** | Spring Security Crypto (BCrypt) | — |
| **前端框架** | Vue 3 (Composition API) | ^3.4.0 |
| **构建工具** | Vite | ^5.0.10 |
| **路由** | Vue Router | ^4.2.5 |
| **状态管理** | Pinia | ^2.1.7 |
| **HTTP 客户端** | Axios | ^1.6.2 |
| **UI 组件库** | Element Plus | ^2.4.3 |
| **图标库** | @element-plus/icons-vue | ^2.3.1 |
| **图标库** | @fortawesome/vue-fontawesome | ^3.0.0-5 |
| **国际化** | Vue I18n | ^9.14.4 |
| **CSS 预处理** | SCSS (Dart Sass) | ^1.69.5 |
| **进度条** | NProgress | ^0.2.0 |

---

## 3. 后端架构

### 3.1 项目坐标与启动类

```xml
<groupId>com.rx</groupId>
<artifactId>rx-admin</artifactId>
<version>1.0.0</version>
<name>RX Admin（通用管理系统后端）</name>
```

**启动类**: `com.rx.admin.RxAs400Application`

**配置文件**: `src/main/resources/application.yml`

```yaml
server:
  port: 8088

spring:
  application:
    name: rx-admin

# 双数据源：rx_admin（系统管理） + rxusysadmin（业务数据）
```

### 3.2 包结构

```
com.rx.admin
├── RxAs400Application.java           # Spring Boot 启动类
├── common/                            # 公共模块
│   ├── BaseEntity.java               # 实体基类（id, createTime, updateTime）
│   ├── Result.java                    # 统一响应封装 {code, msg, data}
│   ├── PageResult.java               # 分页响应封装 {list, total, page, pageSize}
│   └── GlobalExceptionHandler.java   # 全局异常处理 (@RestControllerAdvice)
├── config/                            # 配置模块
│   ├── CorsConfig.java               # CORS 跨域配置
│   ├── SaTokenConfig.java            # Sa-Token 路由拦截器
│   ├── StpInterfaceImpl.java         # 权限/角色加载实现
│   ├── MybatisPlusConfig.java        # MyBatis Plus 分页插件 & 自动填充
│   ├── PrimaryDataSourceConfig.java  # 主数据源 (rx_admin)
│   ├── SecondDataSourceConfig.java   # 第二数据源 (rxusysadmin)
│   ├── SecondDB.java                 # @SecondDB 自定义注解
│   └── AsyncConfig.java              # 异步任务配置
├── entity/                            # 实体模块
│   ├── SysUser.java                  # 系统用户
│   ├── SysRole.java                  # 系统角色
│   ├── SysMenu.java                  # 系统菜单
│   ├── SysDept.java                  # 部门
│   ├── SysLog.java                   # 操作日志
│   ├── SysNotice.java               # 通知公告
│   ├── SysDictData.java              # 字典数据
│   ├── SysDictType.java              # 字典类型
│   └── classics/                     # 四大名著实体（10个）
├── controller/                        # 控制器模块（17个 Controller）
├── service/                           # 服务层（22个 Service）
└── mapper/                            # 数据访问层（22个 Mapper）
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
| `SysUser` | `sys_user` | 系统用户 | username, password, nickname, email, phone, avatar, status, deptId |
| `SysRole` | `sys_role` | 系统角色 | name, code, description, status |
| `SysMenu` | `sys_menu` | 系统菜单 | name, path, component, icon, parentId, type(目录/菜单/按钮), permission, sort |
| `SysDept` | `sys_dept` | 部门 | name, parentId, sort, leader, phone, status |
| `SysLog` | `sys_log` | 操作日志 | userId, username, operation, method, params, ip, duration |
| `SysNotice` | `sys_notice` | 通知公告 | title, content, type, status |
| `SysDictData` | `sys_dict_data` | 字典数据 | dictType, label, value, sort, status |
| `SysDictType` | `sys_dict_type` | 字典类型 | name, type, status |

**中间表**:
- `sys_user_role` — 用户角色关联
- `sys_role_menu` — 角色菜单关联

#### 四大名著 + 历代文学实体（第二数据源 `rxusysadmin`）

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

所有 Mapper 继承 MyBatis Plus `BaseMapper<T>`，自动获得 CRUD 能力。

**主数据源 Mapper** (`com.rx.admin.mapper`):

| Mapper | 对应实体 | 说明 |
|--------|---------|------|
| `SysUserMapper` | SysUser | 用户数据访问 |
| `SysRoleMapper` | SysRole | 角色数据访问 |
| `SysMenuMapper` | SysMenu | 菜单数据访问 |
| `SysDeptMapper` | SysDept | 部门数据访问 |
| `SysLogMapper` | SysLog | 日志数据访问 |
| `SysNoticeMapper` | SysNotice | 通知公告数据访问 |
| `SysDictDataMapper` | SysDictData | 字典数据访问 |
| `SysDictTypeMapper` | SysDictType | 字典类型访问 |
| `SysUserRoleMapper` | — | 用户角色关联 |
| `SysRoleMenuMapper` | — | 角色菜单关联 |

**第二数据源 Mapper** (`com.rx.admin.mapper.classics`，使用 `@SecondDB`):

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

服务层基于 MyBatis Plus `IService<T>` / `ServiceImpl<M, T>` 模式。

**系统管理服务**:
- `AuthService` — 登录认证、注册、Token 签发
- `SysUserService` — 用户 CRUD + 角色分配 + 密码修改
- `SysRoleService` — 角色 CRUD + 菜单权限分配
- `SysMenuService` — 菜单 CRUD + 树形构建 + 路由生成
- `SysDeptService` — 部门 CRUD + 树形结构
- `SysLogService` — 操作日志记录与查询
- `SysNoticeService` — 通知公告管理
- `SysDictDataService` — 字典数据管理
- `SysDictTypeService` — 字典类型管理

**四大名著服务** (位于 `com.rx.admin.service.classics`):
- 共 10 个 Service，每个对应一个实体，提供标准 CRUD + 分页查询

### 3.7 控制层 (Controller)

#### 系统管理控制器

| Controller | 路径前缀 | 说明 |
|-----------|---------|------|
| `AuthController` | `/auth` | 登录、注册、获取用户信息、获取路由菜单 |
| `DashboardController` | `/dashboard` | 仪表盘统计数据 |
| `SysUserController` | `/sys/user` | 用户 CRUD、角色分配、密码重置 |
| `SysRoleController` | `/sys/role` | 角色 CRUD、菜单权限分配 |
| `SysMenuController` | `/sys/menu` | 菜单树查询、菜单 CRUD |
| `SysDeptController` | `/sys/dept` | 部门树查询、部门 CRUD |
| `SysLogController` | `/sys/log` | 操作日志查询 |
| `SysNoticeController` | `/sys/notice` | 通知公告 CRUD |
| `SysDictDataController` | `/sys/dict/data` | 字典数据管理 |
| `SysDictTypeController` | `/sys/dict/type` | 字典类型管理 |
| `SysOnlineController` | `/sys/online` | 在线用户列表 |
| `ApiAnalysisController` | `/sys/analysis` | 接口调用分析 |
| `ChinaRegionController` | `/sys/region` | 行政区划管理 |

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
- `getPermissionList()` — 从数据库加载用户权限码
- `getRoleList()` — 从数据库加载用户角色标识

**密码加密**: 使用 Spring Security `BCryptPasswordEncoder`

### 3.9 公共模块

| 类 | 说明 |
|----|------|
| `BaseEntity` | 实体基类，包含 `id`, `createTime`, `updateTime`，配合 MyBatis Plus 自动填充 |
| `Result` | 统一响应封装，`Result.success(data)` / `Result.error(msg)` |
| `PageResult` | 分页响应封装，包含 `list`, `total`, `page`, `pageSize` |
| `GlobalExceptionHandler` | `@RestControllerAdvice` 全局异常处理，统一返回 `Result.error()` |

### 3.10 API 接口清单

#### 认证接口 (`/auth`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 用户登录 |
| POST | `/auth/register` | 用户注册 |
| GET | `/auth/user/info` | 获取当前用户信息 |
| GET | `/auth/menu/routes` | 获取用户路由菜单（树形） |

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
| 通知公告 | `/sys/notice` | GET/POST/PUT/DELETE 标准 CRUD |
| 字典类型 | `/sys/dict/type` | GET/POST/PUT/DELETE 标准 CRUD |
| 字典数据 | `/sys/dict/data` | GET/POST/PUT/DELETE 标准 CRUD |
| 在线用户 | `/sys/online/list` | GET 在线用户列表 |
| 仪表盘 | `/dashboard/stats` | GET 统计概览 |

#### 四大名著接口 (`/classics/{book}`)

| 书籍 | 子路径 | 操作 |
|------|--------|------|
| 红楼梦 | `/poems/page`, `/characters/page`, `/relations/page` | 分页查询 |
| 三国演义 | `/poems/page`, `/characters/page` | 分页查询 |
| 水浒传 | `/poems/page`, `/chapters/page` | 分页查询 |
| 西游记 | `/poems/page`, `/characters/page`, `/events/page` | 分页查询 |

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
| Markdown 渲染 | marked + highlight.js | — |
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
    ├── api/                            # API 请求层（17 个模块）
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
    │   └── xiyou.js                    # 西游记
    ├── composables/
    │   ├── useTheme.js                 # 亮/暗主题切换
    │   └── useMenuI18n.js              # 菜单国际化翻译
    ├── i18n/                            # 国际化模块
    │   ├── index.js
    │   └── lang/
    │       ├── zh-CN.js                # 中文语言包（300+ 条目）
    │       └── en-US.js                # 英文语言包
    ├── layout/                         # 布局组件
    │   ├── index.vue                   # 主布局（侧边栏 + 顶栏 + 标签栏 + 内容区）
    │   ├── SubMenu.vue                 # 递归子菜单组件
    │   └── TagsView.vue                # 标签页导航栏
    ├── router/
    │   └── index.js                    # 路由配置（单文件）
    ├── stores/                         # Pinia 状态管理
    │   ├── user.js                     # 用户状态
    │   └── tags.js                     # 标签页状态
    ├── styles/                         # 全局样式
    │   ├── global.scss                 # 全局样式与通用类
    │   └── variables.scss              # CSS 变量（亮/暗双主题）
    ├── utils/
    │   └── request.js                  # Axios 封装（请求/响应拦截器）
    └── views/                          # 页面视图（25+ 页面）
        ├── login/index.vue             # 登录/注册页
        ├── dashboard/index.vue         # 仪表盘首页
        ├── profile/index.vue           # 个人信息页
        ├── system/                     # 系统管理
        │   ├── user/index.vue          # 用户管理
        │   ├── role/index.vue          # 角色管理
        │   ├── menu/index.vue          # 菜单管理
        │   └── dept/index.vue          # 部门管理
        ├── tool/                        # 系统工具
        │   ├── dict/index.vue          # 字典管理
        │   ├── region/index.vue        # 行政区划
        │   ├── analysis/index.vue      # 接口分析
        │   └── docs/index.vue          # 项目文档（Markdown 渲染）
        ├── content/notice/index.vue    # 通知公告
        ├── monitor/                    # 系统监控
        │   ├── log/index.vue           # 操作日志
        │   └── online/index.vue        # 在线用户
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

项目已实现**完全动态路由**（方案二），`constantRoutes` 只保留 Login 和 Layout 空壳，所有业务路由在登录后由 `generateDynamicRoutes()` 从后端菜单树动态注入。

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

**componentMap 当前映射项（25个）**：
- 仪表盘/个人/系统管理/系统工具/内容管理/系统监控/四大名著/历代文学

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
| `token` | String | 登录 Token（持久化到 localStorage） |
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
- NProgress 进度条启动

// 响应拦截器
- 统一处理 code !== 200 的错误
- Token 过期自动跳转登录页
- NProgress 进度条完成
```

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

### 4.6 页面视图清单

| 页面 | 文件 | 功能描述 |
|------|------|----------|
| **登录/注册** | `login/index.vue` | 渐变色背景 + 卡片式表单，支持登录/注册切换 |
| **仪表盘** | `dashboard/index.vue` | 统计卡片（hover 上浮效果） |
| **个人信息** | `profile/index.vue` | 用户信息展示与修改 |
| **用户管理** | `system/user/index.vue` | 搜索栏 + 表格 + 分页 + 新增/编辑弹窗 + 角色分配 |
| **角色管理** | `system/role/index.vue` | 搜索栏 + 表格 + 分页 + 新增/编辑弹窗 + 菜单权限分配 |
| **菜单管理** | `system/menu/index.vue` | 树形表格（目录/菜单/按钮） |
| **部门管理** | `system/dept/index.vue` | 树形表格 |
| **字典管理** | `tool/dict/index.vue` | 字典类型 + 字典数据 Tab 切换 |
| **行政区划** | `tool/region/index.vue` | 行政区划树形管理 |
| **接口分析** | `tool/analysis/index.vue` | API 调用统计与可视化 |
| **项目文档** | `tool/docs/index.vue` | Markdown 渲染技术架构文档 |
| **通知公告** | `content/notice/index.vue` | 表格 + 编辑弹窗 |
| **操作日志** | `monitor/log/index.vue` | 只读表格 + 详情弹窗 |
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
- 右侧: 全局搜索框 | 暗黑切换 | 语言切换（Font Awesome globe 图标） | 通知弹窗 | 全屏切换（Font Awesome expand/compress 图标） | 用户头像下拉

**标签栏** (36px):
- 标签文字前显示与左侧菜单一致的图标
- 右键菜单: 刷新 / 关闭当前 / 关闭其他 / 关闭所有
- 水平滚动: 鼠标滚轮
- 固定标签: Dashboard 不可关闭

### 5.2 UI 组件库

**Element Plus 2.4.3** 作为核心 UI 框架：

- **引入方式**: 全量引入 + 中文语言包
- **国际化**: Vue I18n 实现中/英文双语切换，菜单名、表单、提示全量翻译
- **按需自动导入**: `unplugin-auto-import` + `unplugin-vue-components`
- **图标**: Element Plus Icons 全局注册 + Font Awesome 按需引入
- **暗黑模式**: 引入 `element-plus/theme-chalk/dark/css-vars.css`

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
// 切换
document.documentElement.classList.toggle('dark')
// 持久化到 localStorage
localStorage.setItem('theme', isDark ? 'dark' : 'light')
```

#### CSS 变量分类

| 类别 | 变量数 | 示例变量 |
|------|--------|----------|
| **页面背景** | 4 | `--bg-page`, `--bg-container`, `--bg-hover`, `--bg-active` |
| **文字颜色** | 4 | `--text-primary`, `--text-regular`, `--text-secondary`, `--text-placeholder` |
| **主题色** | 1 | `--color-primary` (#409eff) |
| **边框** | 1 | `--border-color` |
| **侧边栏** | 4 | `--sidebar-bg`, `--sidebar-submenu-bg`, `--sidebar-item-hover-bg` |
| **顶栏/标签** | 4 | `--header-bg`, `--tags-bg`, `--tags-item-active-bg` |
| **搜索框** | 2 | `--search-bg`, `--search-dropdown-bg` |
| **通知** | 1 | `--notice-unread-bg` |
| **阴影** | 2 | `--shadow-card` |
| **登录页** | 1 | `--login-bg` |

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

---

## 6. 四大名著模块

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

## 7. 构建与部署

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
- 自动导入: Element Plus 组件按需导入
- SCSS 全局变量: 自动注入 `variables.scss`

### 数据库

- 主库 `rx_admin`: 系统管理表（用户/角色/菜单/部门/日志/字典/通知）
- 业务库 `rxusysadmin`: 四大名著数据表

### 初始化 SQL

- `src/main/resources/db/honglou_characters.sql` — 红楼梦人物数据（54人）
- `src/main/resources/db/honglou_relations.sql` — 红楼梦人物关系数据（99条）

---

## 附录

### A. 项目文件统计

| 层级 | 文件数 | 说明 |
|------|--------|------|
| 后端 Java 源文件 | ~85 | Entity/Controller/Service/Mapper/Config |
| 前端 Vue 组件 | ~30 | 布局/页面/组件 |
| 前端 JS 模块 | ~22 | API/Store/Router/Composables/i18n |
| 样式文件 | 2 | variables.scss + global.scss |
| SQL 脚本 | 7 | init.sql + classics_menu.sql + literature_menu.sql + work_menu.sql + analysis_menu.sql + honglou_characters.sql + honglou_relations.sql |
| 国际化文件 | 2 | zh-CN.js + en-US.js（300+ 翻译条目） |

### B. 关键设计模式

| 模式 | 应用场景 |
|------|----------|
| **双数据源** | 系统管理库 + 业务数据库分离 |
| **RBAC** | 用户 → 角色 → 菜单/权限 三层权限模型 |
| **完全动态路由** | 所有业务路由由后端菜单表驱动，`router.addRoute` 动态注入 |
| **keep-alive 缓存** | 标签页切换时保持页面状态，缓存 key 使用英文 name |
| **国际化 (i18n)** | Vue I18n 实现全站中/英文双语切换，菜单/表单/提示全覆盖 |
| **CSS 变量主题** | 亮色/暗色双主题一键切换 |
| **递归组件** | SubMenu.vue 无限层级菜单渲染 |
| **力导向布局** | Canvas 实现的人物关系可视化 |
| **Markdown 渲染** | 项目文档页面使用 marked + highlight.js 实时渲染 |

---

> **文档维护**: 本文档由项目代码自动分析生成，建议在重大版本更新后重新生成。

---

## 8. 从零搭建项目指南

> 本章详细记录如何从空白环境一步步创建 RX Admin 前后端项目，适合新成员快速上手或从零重建项目。

### 8.1 环境准备

| 工具 | 版本要求 | 用途 |
|------|---------|------|
| **JDK** | 17+ | Java 运行与编译 |
| **Maven** | 3.6+ | 后端构建与依赖管理 |
| **Node.js** | 18+ (LTS) | 前端运行环境 |
| **npm** | 9+ (随 Node.js) | 前端包管理 |
| **MySQL** | 8.0+ | 数据库 |
| **Git** | 任意 | 版本控制 |
| **IDE** | IntelliJ IDEA / VS Code | 开发工具 |

**环境验证命令**:
```bash
java -version          # 确认 Java 17+
mvn -v                 # 确认 Maven 3.6+
node -v                # 确认 Node 18+
npm -v                 # 确认 npm 9+
mysql --version        # 确认 MySQL 8.0+
```

---

### 8.2 后端项目创建（Spring Boot 3）

#### 步骤 1：生成 Spring Boot 项目骨架

**方式一：Spring Initializr（推荐）**

访问 https://start.spring.io/ 配置：

| 配置项 | 值 |
|--------|-----|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.5.15 |
| Group | `com.rx` |
| Artifact | `rx-admin` |
| Java | 17 |
| Dependencies | Spring Web, MySQL Driver, MyBatis Plus, Validation, Spring Security Crypto |

点击 Generate 下载 zip 包，解压到工作目录。

**方式二：IDE 内置创建**

IntelliJ IDEA: `File → New → Project → Spring Initializr`，配置同上。

**方式三：手动 Maven 项目**

创建 `pom.xml`（参考项目现有 pom.xml），手动配置以下核心依赖：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.15</version>
</parent>

<dependencies>
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- MyBatis Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.5</version>
    </dependency>
    <!-- Sa-Token -->
    <dependency>
        <groupId>cn.dev33</groupId>
        <artifactId>sa-token-spring-boot3-starter</artifactId>
        <version>1.37.0</version>
    </dependency>
    <!-- Knife4j API文档 -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>4.4.0</version>
    </dependency>
    <!-- Lombok (可选) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <!-- AOP -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

然后执行 `mvn clean compile` 下载依赖。

#### 步骤 2：创建启动类

`src/main/java/com/rx/admin/RxAs400Application.java`:

```java
package com.rx.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RxAs400Application {
    public static void main(String[] args) {
        SpringApplication.run(RxAs400Application.class, args);
    }
}
```

#### 步骤 3：配置 application.yml

`src/main/resources/application.yml`:

```yaml
server:
  port: 8088

spring:
  application:
    name: rx-admin

# 主数据源 (rx_admin)
  datasource:
    primary:
      driver-class-name: com.mysql.cj.jdbc.Driver
      jdbc-url: jdbc:mysql://localhost:3306/rx_admin?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      username: root
      password: root
    # 第二数据源 (rxusysadmin)
    second:
      driver-class-name: com.mysql.cj.jdbc.Driver
      jdbc-url: jdbc:mysql://localhost:3306/rxusysadmin?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      username: root
      password: root

# Sa-Token 配置
sa-token:
  token-name: rx-admin-token
  timeout: 604800
  is-concurrent: true
  is-log: true

# Knife4j 配置
springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

#### 步骤 4：创建数据库

```sql
-- 创建系统管理库
CREATE DATABASE IF NOT EXISTS rx_admin
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

-- 创建业务数据库
CREATE DATABASE IF NOT EXISTS rxusysadmin
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
```

#### 步骤 5：创建核心系统表

在 `rx_admin` 库中依次创建以下表：

1. **sys_user** — 系统用户表
2. **sys_role** — 系统角色表
3. **sys_menu** — 系统菜单表
4. **sys_dept** — 部门表
5. **sys_user_role** — 用户角色关联表
6. **sys_role_menu** — 角色菜单关联表
7. **sys_log** — 操作日志表
8. **sys_notice** — 通知公告表
9. **sys_dict_type** — 字典类型表
10. **sys_dict_data** — 字典数据表

每张表应包含：自增主键 `id`、业务字段、`create_time`、`update_time`（带默认值 `CURRENT_TIMESTAMP`）。

#### 步骤 6：建立后端包结构

按以下顺序在 `src/main/java/com/rx/admin/` 下创建包：

```
common/      → BaseEntity.java, Result.java, PageResult.java, GlobalExceptionHandler.java
config/      → 各配置类（CORS、Sa-Token、数据源、MyBatis Plus）
entity/      → 实体类（对应数据库表）
mapper/      → MyBatis Mapper 接口
service/     → 服务接口 + 实现类
controller/  → REST 控制器
```

#### 步骤 7：配置双数据源

1. 创建 `PrimaryDataSourceConfig.java` — 主数据源配置
   - 扫描 `com.rx.admin.mapper` 下的 Mapper（排除 `classics` 子包）
   - 配置 `SqlSessionFactory` 和 `DataSourceTransactionManager`
2. 创建 `SecondDataSourceConfig.java` — 第二数据源配置
   - 扫描 `com.rx.admin.mapper.classics` 下的 Mapper
3. 创建 `@SecondDB` 注解 — 标记使用第二数据源的 Mapper

#### 步骤 8：配置 Sa-Token 认证

1. 创建 `SaTokenConfig.java` — 注册路由拦截器，配置放行路径（`/auth/login`, `/auth/register`）
2. 创建 `StpInterfaceImpl.java` — 实现 `StpInterface`，从数据库加载用户角色和权限码
3. 创建 `AuthService.java` — 实现登录逻辑（BCrypt 密码校验 + Token 签发）
4. 创建 `AuthController.java` — 暴露 `/auth/login`、`/auth/register`、`/auth/user/info` 等接口

#### 步骤 9：配置 CORS 跨域

创建 `CorsConfig.java`，注册 `CorsFilter`，允许前端开发服务器 `http://localhost:5173` 跨域访问。

#### 步骤 10：启动验证

```bash
mvn spring-boot:run
# 或
java -jar target/rx-admin-1.0.0.jar
```

访问 `http://localhost:8088/doc.html` 验证 Knife4j API 文档页面可用。

---

### 8.3 前端项目创建（Vue 3 + Vite）

#### 步骤 1：创建 Vite + Vue 3 项目

```bash
# 在工作目录下执行
npm create vite@latest ui -- --template vue

# 进入项目目录
cd ui

# 安装依赖
npm install
```

#### 步骤 2：安装核心依赖

```bash
# Vue 生态
npm install vue-router@4 pinia@2 axios@1

# UI 框架
npm install element-plus @element-plus/icons-vue

# CSS 预处理器
npm install -D sass

# 进度条
npm install nprogress

# 自动导入（可选，提升开发体验）
npm install -D unplugin-auto-import unplugin-vue-components
```

#### 步骤 3：配置 Vite

编辑 `ui/vite.config.js`：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/styles/variables.scss" as *;`
      }
    }
  }
})
```

#### 步骤 4：建立前端目录结构

在 `ui/src/` 下创建以下目录：

```
src/
├── api/           # API 请求模块
├── assets/        # 静态资源
├── composables/   # 组合式函数
├── layout/        # 布局组件
├── router/        # 路由配置
├── stores/        # Pinia 状态管理
├── styles/        # 全局样式
├── utils/         # 工具函数
└── views/         # 页面视图
```

#### 步骤 5：编写 main.js 入口

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import pinia from './stores'
import './styles/global.scss'

const app = createApp(App)

// 注册 Element Plus
app.use(ElementPlus, { locale: zhCn })

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(pinia)
app.mount('#app')
```

#### 步骤 6：创建全局样式文件

1. `src/styles/variables.scss` — CSS 变量定义（亮色/暗色双主题，50+ 变量）
2. `src/styles/global.scss` — 全局重置 + 通用类（`.page-container`, `.search-bar`, `.table-container` 等）

#### 步骤 7：创建路由配置

`src/router/index.js`：
- 使用 `createRouter` + `createWebHistory`
- 定义登录页路由（`/login`，无需布局）
- 定义主布局路由（`/`，Layout 组件为父路由，其余页面为子路由）
- 添加 `beforeEach` 守卫：Token 验证、NProgress 进度条
- 动态路由加载：登录成功后调用 `GET /auth/menu/routes` 动态添加

#### 步骤 8：创建 Axios 封装

`src/utils/request.js`：
- 创建 Axios 实例，`baseURL: '/api'`
- 请求拦截器：自动附加 Token 到 `Authorization` 请求头
- 响应拦截器：统一错误处理，Token 过期跳转登录页

#### 步骤 9：创建 Pinia Store

1. `src/stores/index.js` — 创建 Pinia 实例
2. `src/stores/user.js` — 用户状态（token, userInfo, roles, permissions, menus）
3. `src/stores/tags.js` — 标签页状态（visitedViews, cachedViews）

#### 步骤 10：创建布局组件

1. `src/layout/index.vue` — 主布局（侧边栏 + 顶栏 + 标签栏 + 内容区）
2. `src/layout/SubMenu.vue` — 递归子菜单组件
3. `src/layout/TagsView.vue` — 标签页导航栏

#### 步骤 11：创建登录页

`src/views/login/index.vue`：
- 渐变色背景 + 居中卡片
- 登录/注册 Tab 切换
- 表单验证 + 调用 `/auth/login` 接口

#### 步骤 12：创建首页仪表盘

`src/views/dashboard/index.vue`：
- 统计卡片网格布局
- 调用 `/dashboard/stats` 接口

#### 步骤 13：启动验证

```bash
npm run dev
```

访问 `http://localhost:5173`，确认登录页正常显示，登录后进入仪表盘。

---

## 9. 新增业务模块指南

> 本章以新增 **"历代文学"** 模块为例，详细说明从后端到前端新增一个完整业务模块的全流程步骤。该模块包含：父菜单「历代文学」→ 子菜单「国内文学」「国外文学」→ 各子菜单下按朝代/历史时期展示文学内容。

### 9.1 需求分析

#### 菜单层级结构

```
历代文学 (一级菜单)
├── 国内文学 (二级菜单)
│   ├── 先秦文学
│   ├── 两汉文学
│   ├── 魏晋南北朝文学
│   ├── 唐代文学
│   ├── 宋代文学
│   ├── 元代文学
│   ├── 明代文学
│   └── 清代文学
└── 国外文学 (二级菜单)
    ├── 古希腊罗马
    ├── 中世纪
    ├── 文艺复兴
    ├── 启蒙运动
    ├── 浪漫主义
    ├── 现实主义
    ├── 现代主义
    └── 当代文学
```

#### 数据需求

每个文学作品需要记录：作品名称、作者、朝代/时期、文学类型（诗歌/散文/小说/戏剧/文论）、内容摘要、原文片段、影响力评价、代表句等。

---

### 9.2 后端实现步骤

#### 步骤 1：数据库设计（`rxusysadmin` 库）

**创建 `literature_works` 表**（文学作品主表）:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `name` | VARCHAR(200) | 作品名称 |
| `author` | VARCHAR(100) | 作者 |
| `dynasty` | VARCHAR(50) | 朝代/历史时期（如：唐代、文艺复兴） |
| `region` | VARCHAR(20) | 区域：domestic(国内) / foreign(国外) |
| `category` | VARCHAR(50) | 文学类型：诗歌/散文/小说/戏剧/文论 |
| `summary` | TEXT | 内容摘要 |
| `excerpt` | TEXT | 原文精彩片段 |
| `famous_line` | VARCHAR(500) | 代表名句 |
| `influence` | TEXT | 影响力与评价 |
| `cover_image` | VARCHAR(500) | 配图URL（可选） |
| `sort_order` | INT DEFAULT 0 | 排序号 |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

**创建 `literature_authors` 表**（作者表，可选扩展）:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `name` | VARCHAR(100) | 作者名 |
| `alias` | VARCHAR(200) | 别称/号 |
| `dynasty` | VARCHAR(50) | 所属朝代/时期 |
| `region` | VARCHAR(20) | 区域：domestic / foreign |
| `birth_death` | VARCHAR(100) | 生卒年份 |
| `biography` | TEXT | 生平简介 |
| `style` | VARCHAR(200) | 文学风格 |
| `avatar` | VARCHAR(500) | 头像URL（可选） |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

```sql
-- DDL 参考
CREATE TABLE `literature_works` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL COMMENT '作品名称',
  `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
  `dynasty` VARCHAR(50) NOT NULL COMMENT '朝代/时期',
  `region` VARCHAR(20) NOT NULL COMMENT '区域: domestic/foreign',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '文学类型',
  `summary` TEXT COMMENT '内容摘要',
  `excerpt` TEXT COMMENT '原文片段',
  `famous_line` VARCHAR(500) DEFAULT NULL COMMENT '代表名句',
  `influence` TEXT COMMENT '影响力评价',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '配图URL',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_region_dynasty` (`region`, `dynasty`),
  INDEX `idx_author` (`author`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文学作品表';

CREATE TABLE `literature_authors` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '作者名',
  `alias` VARCHAR(200) DEFAULT NULL COMMENT '别称',
  `dynasty` VARCHAR(50) NOT NULL COMMENT '朝代/时期',
  `region` VARCHAR(20) NOT NULL COMMENT '区域',
  `birth_death` VARCHAR(100) DEFAULT NULL COMMENT '生卒年份',
  `biography` TEXT COMMENT '生平简介',
  `style` VARCHAR(200) DEFAULT NULL COMMENT '文学风格',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_region` (`region`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文学作者表';
```

#### 步骤 2：创建实体类

在 `src/main/java/com/rx/admin/entity/classics/` 下新建：

1. **`LiteratureWork.java`**
   - 添加 `@TableName("literature_works")` 注解
   - 字段与数据库表一一对应
   - 继承 `BaseEntity`（或自行包含 id、createTime、updateTime）
   - 添加 `@SecondDB` 注解（因为表在 `rxusysadmin` 库）

2. **`LiteratureAuthor.java`**（可选）
   - 同上方式创建

#### 步骤 3：创建 Mapper 接口

在 `src/main/java/com/rx/admin/mapper/classics/` 下新建：

1. **`LiteratureWorkMapper.java`**
   - 继承 `BaseMapper<LiteratureWork>`
   - 添加 `@SecondDB` 注解
   - 如需自定义查询（如按朝代分组统计），在此定义方法

2. **`LiteratureAuthorMapper.java`**（可选）
   - 同上

#### 步骤 4：创建 Service 层

在 `src/main/java/com/rx/admin/service/classics/` 下新建：

1. **`LiteratureWorkService.java`**（接口）
   - 继承 `IService<LiteratureWork>`
   - 自定义方法：
     - `pageByRegion(region, dynasty, keyword, page, pageSize)` — 按区域+朝代分页查询
     - `getDynastyGroups(region)` — 获取某区域下的所有朝代分组
     - `getWorkDetail(id)` — 获取作品详情（含作者信息）

2. **`LiteratureWorkServiceImpl.java`**（实现类）
   - 继承 `ServiceImpl<LiteratureWorkMapper, LiteratureWork>`
   - 实现上述接口方法
   - 使用 MyBatis Plus 的 `LambdaQueryWrapper` 构建条件查询

3. **`LiteratureAuthorService.java` / `LiteratureAuthorServiceImpl.java`**（可选）
   - 同上模式创建

#### 步骤 5：创建 Controller

在 `src/main/java/com/rx/admin/controller/classics/` 下新建：

**`LiteratureController.java`**:

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/classics/literature/works/page` | 分页查询作品（参数: region, dynasty, keyword, page, pageSize） |
| GET | `/classics/literature/works/{id}` | 获取作品详情 |
| GET | `/classics/literature/works/dynasties` | 获取区域下的朝代列表（参数: region） |
| POST | `/classics/literature/works` | 新增作品（需管理员权限） |
| PUT | `/classics/literature/works` | 更新作品（需管理员权限） |
| DELETE | `/classics/literature/works/{id}` | 删除作品（需管理员权限） |
| GET | `/classics/literature/authors/page` | 分页查询作者（可选） |
| GET | `/classics/literature/authors/{id}` | 获取作者详情（可选） |

**关键实现要点**:
- 使用 `@RestController` + `@RequestMapping("/classics/literature")`
- 查询接口返回 `Result<PageResult<LiteratureWork>>`
- 使用 `@SaCheckPermission` 注解保护写操作接口
- 参数校验使用 `@Validated`

#### 步骤 6：在系统菜单表中添加菜单记录

在 `rx_admin.sys_menu` 表中插入菜单记录：

```
INSERT INTO sys_menu (name, path, component, icon, parent_id, type, permission, sort, status)
VALUES
-- 一级菜单：历代文学
('历代文学', '/classics/literature', NULL, 'Reading', 0, 1, NULL, 10, 1),
-- 二级菜单：国内文学
('国内文学', '/classics/literature/domestic', 'classics/literature/works/index', 'Collection', @parent_id_1, 2, 'classics:literature:domestic', 1, 1),
-- 二级菜单：国外文学
('国外文学', '/classics/literature/foreign', 'classics/literature/works/index', 'Connection', @parent_id_1, 2, 'classics:literature:foreign', 2, 1);
```

其中 `@parent_id_1` 为插入一级菜单后返回的自增 ID。

#### 步骤 7：初始化数据

准备文学作品初始数据 SQL 脚本，存放在 `src/main/resources/db/literature_data.sql`，按国内（先秦→清代）和国外（古希腊→当代）两大区域组织数据。

---

### 9.3 前端实现步骤

#### 步骤 1：创建 API 模块

在 `ui/src/api/` 下新建 **`literature.js`**:

```javascript
import request from '@/utils/request'

// 分页查询作品
export function getWorkPage(params) {
  return request({ url: '/classics/literature/works/page', method: 'get', params })
}

// 获取作品详情
export function getWorkDetail(id) {
  return request({ url: `/classics/literature/works/${id}`, method: 'get' })
}

// 获取朝代/时期分组
export function getDynasties(region) {
  return request({ url: '/classics/literature/works/dynasties', params: { region }, method: 'get' })
}

// 新增作品
export function addWork(data) {
  return request({ url: '/classics/literature/works', method: 'post', data })
}

// 更新作品
export function updateWork(data) {
  return request({ url: '/classics/literature/works', method: 'put', data })
}

// 删除作品
export function deleteWork(id) {
  return request({ url: `/classics/literature/works/${id}`, method: 'delete' })
}

// 作者相关接口（可选）
export function getAuthorPage(params) {
  return request({ url: '/classics/literature/authors/page', method: 'get', params })
}

export function getAuthorDetail(id) {
  return request({ url: `/classics/literature/authors/${id}`, method: 'get' })
}
```

#### 步骤 2：创建页面目录结构

```
ui/src/views/classics/literature/
├── works/
│   └── index.vue          # 作品列表主页面（国内/国外共用，通过 region 参数区分）
├── authors/
│   └── index.vue          # 作者列表页（可选）
└── components/
    ├── WorkCard.vue        # 作品卡片组件
    ├── WorkDetail.vue      # 作品详情抽屉/弹窗
    ├── DynastyFilter.vue   # 朝代筛选组件
    └── AuthorCard.vue      # 作者卡片组件（可选）
```

#### 步骤 3：实现作品列表页面

**`works/index.vue`** — 国内文学和国外文学共用一个页面组件：

**路由传参区分**:
- 国内文学路由: `/classics/literature/domestic` → `region = 'domestic'`
- 国外文学路由: `/classics/literature/foreign` → `region = 'foreign'`

**页面结构**:
```
.page-container
├── .search-bar (搜索栏)
│   ├── el-input (关键词搜索)
│   ├── el-select (朝代/时期筛选 — 由 DynastyFilter 组件实现)
│   └── el-button (搜索按钮)
├── .table-container
│   ├── .table-header
│   │   ├── .title (根据 region 动态显示"国内文学"或"国外文学")
│   │   └── el-button (新增作品，需管理员权限)
│   └── 作品卡片网格 (el-row + el-col)
│       └── WorkCard.vue × N
└── .page-pagination (el-pagination)
```

**关键实现逻辑**:
1. 通过 `useRoute()` 获取当前路由 `path`，判断 `region`
2. 页面标题动态计算：`computed(() => region === 'domestic' ? '国内文学' : '国外文学')`
3. 朝代/时期列表通过 `getDynasties(region)` 接口动态加载
4. 搜索条件变化时重新请求分页数据
5. 朝代筛选支持：国内为「先秦/两汉/魏晋南北朝/唐/宋/元/明/清」，国外为「古希腊罗马/中世纪/文艺复兴/启蒙运动/浪漫主义/现实主义/现代主义/当代」

#### 步骤 4：实现作品卡片组件

**`WorkCard.vue`**:

**视觉设计**:
- 卡片样式：el-card 带 hover 上浮阴影效果（`transition: transform 0.3s, box-shadow 0.3s`）
- 卡片顶部：朝代/时期标签（el-tag，国内用暖色调，国外用冷色调区分）
- 卡片主体：作品名称（粗体）+ 作者 + 代表名句（斜体引用样式）
- 卡片底部：文学类型标签 + 查看详情按钮
- hover 效果：`transform: translateY(-4px)` + 阴影增强

**Props**: 接收 `work` 对象（包含 id, name, author, dynasty, category, famousLine, summary 等）

**Events**: `@click` 触发详情查看（emit 或 router push）

#### 步骤 5：实现朝代筛选组件

**`DynastyFilter.vue`**:

- 使用 `el-select` 或 `el-radio-group`（推荐按钮式 radio，视觉更佳）
- 根据 `region` prop 动态渲染对应朝代表选项
- 支持"全部"选项
- 选中变化时 emit `@change` 事件

**朝代选项配置**:

| region | 选项列表 |
|--------|---------|
| domestic | 全部 / 先秦 / 两汉 / 魏晋南北朝 / 唐代 / 宋代 / 元代 / 明代 / 清代 |
| foreign | 全部 / 古希腊罗马 / 中世纪 / 文艺复兴 / 启蒙运动 / 浪漫主义 / 现实主义 / 现代主义 / 当代 |

#### 步骤 6：实现作品详情组件

**`WorkDetail.vue`**:

**展示方式**: el-drawer（从右侧滑出）或 el-dialog（居中弹窗），推荐 drawer 方式，宽度约 600px。

**内容布局**:
```
┌────────────────────────────────────┐
│  作品名称（标题，大号粗体）          │
│  ───────────────────────────────── │
│  作者：XXX    朝代：XXX   类型：XXX  │
│  ───────────────────────────────── │
│  📝 内容摘要                        │
│  详细摘要文字...                     │
│  ───────────────────────────────── │
│  📖 精彩片段                        │
│  引用样式展示原文片段...              │
│  ───────────────────────────────── │
│  💬 代表名句                        │
│  "名句内容" — 出处                   │
│  ───────────────────────────────── │
│  ⭐ 影响力与评价                     │
│  评价文字...                        │
└────────────────────────────────────┘
```

**样式要点**:
- 分隔线使用 `el-divider`
- 原文片段使用 blockquote 样式（左边框 + 缩进 + 浅色背景）
- 名句使用引号装饰 + 斜体
- 每个信息区块用图标 + 标签区分

#### 步骤 7：添加路由配置

在 `ui/src/router/index.js` 中添加路由（作为 Layout 的子路由）:

```javascript
{
  path: '/classics/literature/domestic',
  name: 'LiteratureDomestic',
  component: () => import('@/views/classics/literature/works/index.vue'),
  meta: { title: '国内文学', icon: 'Collection', keepAlive: true }
},
{
  path: '/classics/literature/foreign',
  name: 'LiteratureForeign',
  component: () => import('@/views/classics/literature/works/index.vue'),
  meta: { title: '国外文学', icon: 'Connection', keepAlive: true }
},
// 可选：作者详情页
{
  path: '/classics/literature/author/:id',
  name: 'LiteratureAuthorDetail',
  component: () => import('@/views/classics/literature/authors/index.vue'),
  meta: { title: '作者详情', hidden: true }  // hidden: true 不在菜单中显示
}
```

**注意**: 路由主要由后端菜单表动态生成，此处配置作为兜底。实际开发中，前端静态路由只配置登录页和 Layout 外壳，其余路由在登录成功后通过 `GET /auth/menu/routes` 动态注册。

#### 步骤 8：前端文件清单总结

新增模块涉及的前端文件：

```
ui/src/
├── api/
│   └── literature.js                          # API 请求（新增）
├── views/classics/literature/
│   ├── works/
│   │   └── index.vue                           # 作品列表页（新增，国内/国外共用）
│   ├── authors/
│   │   └── index.vue                           # 作者详情页（新增，可选）
│   └── components/
│       ├── WorkCard.vue                        # 作品卡片（新增）
│       ├── WorkDetail.vue                      # 作品详情抽屉（新增）
│       └── DynastyFilter.vue                   # 朝代筛选（新增）
└── router/
    └── index.js                                # 路由配置（追加 2 条路由）
```

---

### 9.4 完整新增步骤速查表

| 步骤 | 层级 | 操作 | 涉及文件 |
|------|------|------|----------|
| 1 | 数据库 | 设计并创建 `literature_works` 表（+可选 `literature_authors` 表） | DDL SQL |
| 2 | 后端 | 创建实体类 | `LiteratureWork.java` |
| 3 | 后端 | 创建 Mapper 接口 | `LiteratureWorkMapper.java` |
| 4 | 后端 | 创建 Service 接口 + 实现 | `LiteratureWorkService.java` + `Impl` |
| 5 | 后端 | 创建 Controller | `LiteratureController.java` |
| 6 | 数据库 | 插入菜单记录到 `sys_menu` 表 | INSERT SQL |
| 7 | 数据库 | 初始化业务数据 | `literature_data.sql` |
| 8 | 前端 | 创建 API 请求模块 | `api/literature.js` |
| 9 | 前端 | 创建作品列表页面 | `views/classics/literature/works/index.vue` |
| 10 | 前端 | 创建作品卡片组件 | `components/WorkCard.vue` |
| 11 | 前端 | 创建作品详情组件 | `components/WorkDetail.vue` |
| 12 | 前端 | 创建朝代筛选组件 | `components/DynastyFilter.vue` |
| 13 | 前端 | 添加路由配置 | `router/index.js` 追加路由 |
| 14 | 验证 | 启动后端 → 启动前端 → 登录 → 菜单可见 → 功能可用 | — |

---

### 9.5 设计要点与最佳实践

1. **页面复用**: 国内文学和国外文学共用一个 `works/index.vue` 页面组件，通过路由参数（`meta.region` 或路径判断）区分数据源，避免重复代码。

2. **组件拆分**: 将卡片、详情、筛选器等拆分为独立子组件，便于维护和测试。遵循「页面 = 布局 + 数据逻辑，组件 = 纯展示 + 事件」的原则。

3. **朝代数据配置化**: 朝代/时期选项列表定义为常量配置（可放在 `utils/constants.js` 或组件的 `setup` 中），而非硬编码在模板里。这样后续新增朝代只需修改配置数组。

4. **样式一致性**: 卡片样式参考现有的四大名著人物卡片设计，保持全站视觉统一。使用项目现有的 CSS 变量（`var(--bg-container)`, `var(--text-primary)` 等），自动适配亮色/暗色主题。

5. **权限控制**: 查询接口无需权限，新增/编辑/删除接口使用 `@SaCheckPermission` 注解保护。前端按钮通过 `v-if` + 权限判断控制显示。

6. **分页与搜索**: 遵循现有页面模式：`.search-bar` → `.table-container` → `.page-pagination` 三段式布局，使用 `el-pagination` 组件。

7. **数据初始化**: 建议准备 50+ 条文学作品初始数据，覆盖国内 8 个朝代和国外 8 个时期，每个时期至少 3-5 部代表作，确保页面展示效果丰富。

8. **后续可扩展方向**:
   - 作品详情页关联作者信息
   - 添加文学流派标签筛选（如：豪放派、婉约派、意识流等）
   - 作品对比功能
   - 文学发展时间轴可视化
   - 用户收藏/笔记功能

---

## 10. 路由动态化：从 Hardcoding 到后端驱动

> 分析当前路由配置的问题，以及如何将 `ui/src/router/index.js` 中硬编码的路由逐步迁移为由后端菜单表驱动，实现真正的动态路由。

### 10.1 现状分析

#### 当前路由架构存在的问题

当前 `ui/src/router/index.js` 采用**前端静态定义所有路由**的方式：

```
constantRoutes = [
  { path: '/login', ... },
  { path: '/', component: Layout, children: [
      { path: 'dashboard', ... },
      { path: '/system/user', ... },
      { path: '/system/role', ... },
      { path: '/system/menu', ... },
      // ... 共 18 条子路由，全部硬编码
  ]}
]
```

**具体问题**：

| 问题 | 影响 |
|------|------|
| **前后端耦合** | 每新增一个菜单页面，前端路由、后端菜单表、页面组件三处都要同步修改 |
| **菜单与路由割裂** | 侧边栏菜单渲染用 `userStore.menus`（后端数据），路由匹配用 `constantRoutes`（前端硬编码），两套数据源容易不一致 |
| **权限校验靠前端硬逻辑** | `router.beforeEach` 中 `if (!userStore.hasRole('admin') && !to.meta.hidden)` 手动收集菜单路径做权限判断，而非由路由本身驱动 |
| **新增页面需重新部署** | 新增菜单页面需要修改前端代码 + 重新打包部署 |
| **重复信息** | `component` 路径和 `meta.title`/`meta.icon` 在前端路由和后端 `sys_menu` 表中各写一份 |

#### 现有动态路由的部分尝试

项目**已经具备了部分动态路由能力**：

1. **后端** `GET /api/auth/routers` → 返回用户可见的菜单树（含 `path`, `component`, `icon`, `children` 等）
2. **前端** `userStore.fetchRouters()` → 将菜单数据存入 `userStore.menus`
3. **侧边栏** `<SubMenu v-for="menu in userStore.menus" ...>` → 菜单渲染完全由后端数据驱动
4. **路由守卫** `fetchRouters()` → 登录后调用，但仅用于权限校验，未用于动态注册路由

**关键矛盾**：侧边栏已经「数据驱动」了，但 `router.addRoute` 没有被使用，路由表仍然是静态的 `constantRoutes`。Vue Router 匹配路由时走的是 `constantRoutes` 中的硬编码 `path`，而不是后端返回的动态菜单数据。

#### 当前数据流

```
登录成功
  → userStore.fetchUserInfo()     // 获取角色、权限
  → userStore.fetchRouters()      // GET /api/auth/routers → menus.value = 后端菜单树
  → router.beforeEach 拦截
      → 从 userStore.menus 收集所有 path
      → 手动比对 to.path 是否在菜单路径集合中
      → 不在则重定向 /dashboard

侧边栏渲染
  → <el-menu> 遍历 userStore.menus（后端数据）
  → 点击菜单 → el-menu router 属性 → 根据 path 跳转
  → Vue Router 匹配 constantRoutes 中的静态路由
  → 渲染对应的 component
```

**可以看到**：菜单渲染走「后端数据」，路由匹配走「前端硬编码」。两套数据各自独立，通过 `path` 字段碰巧对上。

---

### 10.2 改造方案

#### 方案一：最小改动 — 补充 `component` 映射（推荐渐进迁移）

**思路**：保持 `constantRoutes` 不变，但在 `router.beforeEach` 中增加 `component` 的动态映射，让新增菜单无需手动添加路由即可生效。

**改造内容**：

1. **建立 `path → component` 映射表**

   在前端维护一个集中映射（独立文件 `router/componentMap.js`），替代分散在各路由配置中的 `component` 引用：

   ```javascript
   // router/componentMap.js
   // key = sys_menu.component 字段值（与后端数据库一致）
   // value = () => import(...) 懒加载组件

   const componentMap = {
     'dashboard/index': () => import('@/views/dashboard/index.vue'),
     'system/user/index': () => import('@/views/system/user/index.vue'),
     'system/role/index': () => import('@/views/system/role/index.vue'),
     'system/menu/index': () => import('@/views/system/menu/index.vue'),
     'system/dept/index': () => import('@/views/system/dept/index.vue'),
     'tool/dict/index': () => import('@/views/tool/dict/index.vue'),
     'content/notice/index': () => import('@/views/content/notice/index.vue'),
     'monitor/log/index': () => import('@/views/monitor/log/index.vue'),
     'monitor/online/index': () => import('@/views/monitor/online/index.vue'),
     'classics/honglou/poems/index': () => import('@/views/classics/honglou/poems/index.vue'),
     'classics/honglou/characters/index': () => import('@/views/classics/honglou/characters/index.vue'),
     'classics/honglou/relations/index': () => import('@/views/classics/honglou/relations/index.vue'),
     // ... 其他映射
     // 新增菜单只需在此追加一行
   }
   ```

2. **将映射表的 key 与后端 `sys_menu.component` 字段对齐**

   后端的 `component` 字段值（如 `system/user/index`）直接作为映射表的 key。新增菜单时：
   - 后端 `sys_menu` 表插入记录，`component` 字段填入路径
   - 前端 `componentMap.js` 追加一行映射
   - 无需修改 `router/index.js` 中的 `constantRoutes`

3. **在路由守卫中动态注册路由**

   ```javascript
   // router/index.js beforeEach 中
   // 拿到后端菜单树后，动态 addRoute
   function addDynamicRoutes(menuTree, parentPath = '') {
     menuTree.forEach(menu => {
       const fullPath = parentPath + '/' + menu.path
       if (menu.component && componentMap[menu.component]) {
         router.addRoute('Layout', {
           path: fullPath,
           name: menu.menuName,
           component: componentMap[menu.component],
           meta: { title: menu.menuName, icon: menu.icon }
         })
       }
       if (menu.children) {
         addDynamicRoutes(menu.children, menu.path)
       }
     })
   }
   ```

**优点**：改动最小，不破坏现有架构，渐进式迁移。
**缺点**：新增菜单仍需前端追加一行映射（但比修改整个路由对象简单很多）。

---

#### 方案二：完全动态路由 — `router.addRoute` 驱动（推荐目标方案）

**思路**：`constantRoutes` 只保留 `Login` 和 `Layout` 外壳，所有业务路由在登录后由后端数据通过 `addRoute` 动态注入。

**改造内容**：

**1. 精简 `constantRoutes`**

```javascript
// 只保留外壳
const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    redirect: '/dashboard',
    children: []  // 初始为空，后续动态填充
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]
```

**2. 建立 `component` 映射表**（同方案一）

**3. 登录后动态注入路由**

```javascript
// router/index.js
export function generateDynamicRoutes(menuTree) {
  const flatRoutes = []

  function walk(menus, parentPath) {
    menus.forEach(menu => {
      if (menu.component && componentMap[menu.component]) {
        const route = {
          path: menu.path,
          name: menu.menuName,
          component: componentMap[menu.component],
          meta: {
            title: menu.menuName,
            icon: menu.icon,
            keepAlive: true
          }
        }
        flatRoutes.push(route)
      }
      if (menu.children && menu.children.length) {
        walk(menu.children, menu.path)
      }
    })
  }

  walk(menuTree, '')

  // 统一注册到 Layout 下
  flatRoutes.forEach(route => {
    router.addRoute('Layout', route)
  })

  // 添加兜底路由（确保所有动态路由之后匹配）
  router.addRoute({
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  })

  return flatRoutes
}
```

**4. 在 `beforeEach` 守卫中调用**

```javascript
router.beforeEach(async (to, from, next) => {
  // ... token 检查 ...

  if (!userStore.menus.length) {
    await userStore.fetchRouters()
    generateDynamicRoutes(userStore.menus)
    // 动态路由注册后，重新导航到目标页面
    next({ ...to, replace: true })
    return
  }

  next()
})
```

**5. 调整侧边栏渲染**

侧边栏已经用 `userStore.menus` 渲染，无需修改。菜单的 `path` 和动态注入的路由 `path` 保持一致，`el-menu` 的 `router` 属性自动匹配。

**优点**：
- 真正的数据驱动，前端路由完全由后端菜单表控制
- 新增菜单只需：后端插入 `sys_menu` 记录 + 前端 `componentMap.js` 追加一行
- 权限控制自然融入（后端只返回用户有权访问的菜单 → 路由只注册有权限的页面）
- 无需前端路由中的权限硬编码判断

**缺点**：
- 需要处理动态路由注册的时序问题（`next({ ...to, replace: true })` 重定向）
- 页面刷新时需要重新注册路由（`menus` 不持久化时会丢失，需要处理）
- 嵌套路由（如 Tab 页内的子路由）需要额外处理

---

#### 方案三：混合方案 — 静态核心路由 + 动态业务路由

**思路**：系统管理类路由（用户/角色/菜单/部门等）保留在 `constantRoutes` 中（因为它们是系统的基础骨架，几乎不会变），四大名著等业务类路由改为动态注入。

```javascript
const constantRoutes = [
  { path: '/login', ... },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      // 只保留系统核心路由（几乎不变的部分）
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/index.vue'), meta: { hidden: true } },
      { path: '/system/user', ... },
      { path: '/system/role', ... },
      { path: '/system/menu', ... },
      { path: '/system/dept', ... },
      { path: '/tool/dict', ... },
      { path: '/monitor/log', ... },
      { path: '/monitor/online', ... },
    ]
  }
]

// 业务路由动态注入（四大名著、历代文学等）
// 登录后 fetchRouters → 过滤出业务模块 → addRoute
```

**优点**：改动范围可控，核心系统保持稳定，业务模块灵活扩展。
**缺点**：本质上仍是两套逻辑，只是分得更细。

---

### 10.3 方案对比

| 维度 | 当前状态 | 方案一（渐进） | 方案二（完全动态） | 方案三（混合） |
|------|---------|---------------|-------------------|---------------|
| 新增菜单前端改动量 | 修改 router + 可能修改 component | 仅追加 componentMap 一行 | 仅追加 componentMap 一行 | 核心路由不改 / 业务路由追加一行 |
| 前后端耦合度 | 高 | 中 | 低 | 中 |
| 权限控制 | 手动比对路径 | 半自动 | 自动（菜单即权限） | 核心手动 + 业务自动 |
| 实现复杂度 | — | 低 | 中 | 低 |
| 页面刷新恢复 | 天然支持 | 天然支持 | 需处理（menus 丢失需重注册） | 天然支持（核心路由） |
| 适合场景 | 小型项目 | 中型项目渐进改造 | 大型项目/多租户/菜单频繁变动 | 核心稳定 + 业务多变 |

---

### 10.4 关键实施细节

#### 1. `component` 映射表设计

```javascript
// router/componentMap.js
// 每条映射需要同时提供 component（懒加载函数）和 name（组件 defineOptions name）
// name 用于 keep-alive 缓存匹配，必须与页面组件中 defineOptions({ name: 'xxx' }) 一致

export const componentMap = {
  // 格式: 'sys_menu.component字段值': { component: 懒加载函数, name: 组件 defineOptions name }
  'dashboard/index':   { component: () => import('@/views/dashboard/index.vue'),   name: 'Dashboard' },
  'system/user/index': { component: () => import('@/views/system/user/index.vue'), name: 'SystemUser' },
  // ...
}

// 备选：使用 glob 自动扫描（需要路径与 component 字段精确对齐）
// 但组件 name 仍需要手动指定或通过约定自动生成
// const modules = import.meta.glob('@/views/**/index.vue')
// const componentMap = {}
// Object.keys(modules).forEach(key => {
//   const match = key.match(/views\/(.+)\.vue$/)
//   if (match) componentMap[match[1]] = { component: modules[key], name: autoGenName(match[1]) }
// })
```

**注意**：
- `name` 字段必须与页面组件中 `defineOptions({ name: 'xxx' })` 完全一致，否则 `keep-alive` 缓存失效，切换标签时会重复请求业务数据
- 前端 `views/` 目录路径 = 后端 `sys_menu.component` 字段值
- 例如 `sys_menu.component = 'system/user/index'` → 文件必须在 `views/system/user/index.vue`

#### 2. 页面刷新时路由数据恢复

`menus`/`roles`/`perms` 已持久化到 `localStorage`，页面刷新后自动从 `localStorage` 恢复，无需在 `beforeEach` 中重新请求后端。`login()` 时也会预加载这些数据，确保登录后路由立即可用。

```javascript
// stores/user.js — 初始化时从 localStorage 恢复
const menus = ref(JSON.parse(localStorage.getItem('menus') || '[]'))
const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
const perms = ref(JSON.parse(localStorage.getItem('perms') || '[]'))

// login() 中预加载数据，避免 beforeEach 中再发请求
async function login(username, password) {
  const res = await loginApi(username, password)
  // ... 存 token ...
  await fetchUserInfo()   // 获取 roles / perms 并持久化
  await fetchRouters()    // 获取 menus 并持久化
  return res
}
```

#### 3. 后端菜单表字段规范

为使动态路由生效，`sys_menu` 表的字段需要满足以下约定：

| 字段 | 规范 | 示例 |
|------|------|------|
| `path` | 相对路径（前端路由 path） | `system/user` |
| `component` | 与 `views/` 下的文件路径一致（不含 `.vue`） | `system/user/index` |
| `menu_type` | 1=目录，2=菜单，3=按钮 | 路由只处理 type=1,2 |
| `visible` | 0=隐藏，1=显示 | 隐藏的菜单不生成侧边栏但路由仍可用 |
| `icon` | Element Plus 图标名 | `UserFilled` |

#### 4. 嵌套路由处理

对于有子菜单的目录（如「系统管理」下有「用户管理」「角色管理」）：

- **侧边栏**：`SubMenu.vue` 递归渲染 `<el-sub-menu>` → `<el-menu-item>`，已正确处理
- **路由**：`addRoute('Layout', { path: '/system/user', ... })` 平级注册即可，无需嵌套路由
- **面包屑**：从 `userStore.menus` 树中查找当前路径的祖先链

---

### 10.5 推荐的改造路线图

| 阶段 | 内容 | 风险 |
|------|------|------|
| **阶段 1** | 创建 `componentMap.js`，将现有所有路由的 `component` 引用集中管理 | 低 — 纯重构，不改变行为 |
| **阶段 2** | 在 `beforeEach` 中增加 `addRoute` 逻辑，但保留 `constantRoutes` 作为兜底 | 低 — 双轨运行，动态路由优先匹配 |
| **阶段 3** | 逐个将 `constantRoutes` 中的路由迁移到动态注册，验证通过后删除硬编码 | 中 — 需逐条验证 |
| **阶段 4** | 清理 `constantRoutes`，只保留 Login 和 Layout 外壳 | 中 — 需充分测试页面刷新、直接URL访问等场景 |
| **阶段 5** | 考虑 `import.meta.glob` 自动扫描替代手动 `componentMap` | 低 — 可选优化 |

---

### 10.6 总结

**核心结论**：当前项目已经具备动态路由的大部分基础设施（后端返回菜单树、前端存储 menus、侧边栏动态渲染），只需要补上 **`router.addRoute` + `component` 映射表** 这一环节，即可从「菜单数据驱动侧边栏」升级为「菜单数据驱动整个路由系统」。

**最小可行改造**（1 小时内可完成）：
1. 创建 `router/componentMap.js`，集中管理 path → component 映射
2. 在 `fetchRouters()` 后调用 `generateDynamicRoutes(menus)`，用 `addRoute` 注册
3. `constantRoutes` 暂时保留不动（双轨运行）

改造后新增业务菜单的流程简化为：
> 后端 `sys_menu` 插入记录 → 前端 `componentMap.js` 追加一行映射 → 完成。无需修改 `router/index.js`。

---

### 10.7 完全动态方案：新增菜单维护指南

采用方案二后，新增菜单的维护流程根据页面组件是否已存在分为两种情况。

#### 情况一：页面组件已存在（纯后端操作）

**场景**：前端页面已经开发好（如之前已写好「历代文学」的 `.vue` 页面文件，且 `componentMap.js` 中已有映射），仅需通过菜单管理上线。

**步骤**（仅后端，无需改前端代码）：

1. 打开菜单管理页面（`/system/menu`）
2. 新增菜单记录，关键字段：

| 字段 | 说明 | 示例（目录） | 示例（菜单页） |
|------|------|-------------|---------------|
| `menuName` | 菜单显示名称 | `历代文学` | `唐诗宋词` |
| `menuType` | 1=目录, 2=菜单 | `1` | `2` |
| `path` | 前端路由路径 | `/literature` | `/literature/domestic/tangshi` |
| `component` | 目录留空；菜单页填与 `componentMap` key 一致的路径 | 留空 | `literature/domestic/tangshi/index` |
| `icon` | Element Plus 图标名 | `Reading` | `EditPen` |
| `parentId` | 父菜单ID | `0` | 父菜单ID |

3. 分配角色权限（在角色管理中勾选新菜单）

用户刷新或重新登录后：
- 后端返回菜单树包含新菜单 → `generateDynamicRoutes()` 自动 `addRoute` 注册路由
- 侧边栏自动渲染新菜单项（`SubMenu.vue` 递归遍历 `userStore.menus`）
- 点击菜单即可访问，面包屑自动生成

#### 情况二：全新页面组件（前后端配合）

**场景**：一个全新业务模块，前端 `.vue` 页面文件还不存在。

**步骤**：

1. **前端 — 开发页面组件**：
   ```
   ui/src/views/literature/domestic/tangshi/index.vue   # 列表页
   ui/src/views/literature/domestic/tangshi/detail.vue  # 详情页（可选）
   ```
   
   组件中需要声明 `defineOptions({ name: 'xxx' })`，命名规范为路径驼峰，如：
   ```javascript
   // views/literature/domestic/tangshi/index.vue
   defineOptions({ name: 'LiteratureDomesticTangshi' })
   ```

2. **前端 — `componentMap.js` 追加一行**：
   ```javascript
   'literature/domestic/tangshi/index': {
     component: () => import('@/views/literature/domestic/tangshi/index.vue'),
     name: 'LiteratureDomesticTangshi'  // 必须与组件 defineOptions name 一致
   },
   ```

3. **后端 — 菜单管理新增记录**（同情况一），`component` 字段填 `literature/domestic/tangshi/index`

#### 改造前后对比

| 操作 | 改造前（hardcode） | 改造后（完全动态） |
|------|-------------------|-------------------|
| 后端新增菜单记录 | ✅ 需要 | ✅ 需要 |
| 修改 `router/index.js` 加路由 | ✅ **必须改** | ❌ 不需要 |
| 修改 `componentMap.js` | 不存在此文件 | ✅ 仅当新页面时追加一行 |
| 开发 `.vue` 页面文件 | ✅ 需要 | ✅ 需要 |
| 声明 `defineOptions name` | 可选 | ✅ **必须**（keep-alive 缓存需要） |
| 分配角色权限 | ✅ 需要 | ✅ 需要 |

**核心变化**：再也不需要动 `router/index.js`。新增菜单不再需要手动配置路由 path、name、meta 信息，全部由后端菜单数据驱动自动生成。`componentMap.js` 只在开发全新页面时需要追加一行映射。

#### keep-alive 缓存说明

动态路由的 `name` 使用组件 `defineOptions name`（英文），而非后端菜单的 `menuName`（中文）。这样确保：
- `tagsStore.cachedViews` 存储的是英文名 → `keep-alive :include` 能匹配到组件的 `defineOptions name`
- 切换标签时组件缓存生效，不会重复发送业务数据请求
- 右键菜单「刷新页面」通过改变 key 强制重建组件实例

---

### 10.8 方案二具体实现（已实施）

以下为方案二「完全动态路由」的实际代码改动。

#### 10.8.1 新建 `ui/src/router/componentMap.js`

将所有页面的懒加载函数和组件名集中管理。每个映射项包含：
- `component`：Vite 懒加载函数 `() => import(...)`
- `name`：组件 `defineOptions({ name: 'xxx' })` 中声明的名称，用于 `keep-alive` 缓存匹配

**约定**：
- `componentMap` 的 key = `views/` 下相对路径去掉 `.vue` 后缀，与 `sys_menu.component` 字段对齐
- `name` 必须与页面组件的 `defineOptions name` 完全一致

#### 10.8.2 改造 `ui/src/router/index.js`

**改动点**：

| 改动项 | 改造前 | 改造后 |
|--------|--------|--------|
| `constantRoutes` | 包含 Layout + 18 个 children + Login + 404 | 只保留 Login + Layout 空壳（children 为 `[]`） |
| 新增 `generateDynamicRoutes()` | 无 | 递归遍历后端菜单树，匹配 `componentMap`，调用 `router.addRoute('Layout', route)` 动态注册。路由 `name` 使用 `componentMap` 中定义的英文名（而非菜单中文名），确保 `keep-alive` 缓存匹配 |
| `beforeEach` 守卫 | `fetchRouters()` 后只存 store，手动 `collectPaths` 做权限校验 | **纯同步**逻辑：检查 `dynamicRoutesAdded` 标记，首次进入时调用 `generateDynamicRoutes()` 注册路由。不再在守卫中发异步请求 |
| Layout redirect | `redirect: '/dashboard'` | 删除（children 为空时 redirect 会死循环），改在 `beforeEach` 中处理 `/` → `/dashboard` |

**关键设计决策**：
- `beforeEach` 保持纯同步，不发起任何后端请求（`login()` 中已预加载 `fetchUserInfo()` + `fetchRouters()`）
- `menus`/`roles`/`perms` 持久化到 `localStorage`，刷新后自动恢复，避免刷新后路由丢失
- `dynamicRoutesAdded` 布尔标记防止重复注册路由
- 未注册路由的路径自然无法匹配，无需手动 `collectPaths` 做白名单校验

#### 10.8.3 改造 `ui/src/stores/user.js`

| 改动点 | 改造前 | 改造后 |
|--------|--------|--------|
| `roles` 初始化 | `ref([])` | `ref(JSON.parse(localStorage.getItem('roles') \|\| '[]'))` |
| `perms` 初始化 | `ref([])` | `ref(JSON.parse(localStorage.getItem('perms') \|\| '[]'))` |
| `menus` 初始化 | `ref([])` | `ref(JSON.parse(localStorage.getItem('menus') \|\| '[]'))` |
| `fetchUserInfo()` | 只存内存 | 增加 `localStorage.setItem('roles', ...)` 和 `localStorage.setItem('perms', ...)` 持久化 |
| `fetchRouters()` | 只存 `menus.value` | 增加 `localStorage.setItem('menus', ...)` 持久化 |
| `login()` | 只调 `loginApi` | 增加 `await fetchUserInfo()` + `await fetchRouters()`，登录时预加载所有数据 |
| `logout()` | 清理 token + userInfo | 增加清理 `roles`/`perms`/`menus` 的 localStorage 项 |

#### 10.8.4 后端数据层约定

确保 `sys_menu` 表中每条 `menuType=2` 的菜单记录 `component` 字段值与前端 `componentMap.js` 的 key 完全一致：

| 菜单名 | path | component |
|--------|------|-----------|
| 仪表盘 | `/dashboard` | `dashboard/index` |
| 用户管理 | `/system/user` | `system/user/index` |
| 角色管理 | `/system/role` | `system/role/index` |
| 菜单管理 | `/system/menu` | `system/menu/index` |
| 部门管理 | `/system/dept` | `system/dept/index` |
| 字典管理 | `/tool/dict` | `tool/dict/index` |
| 通知公告 | `/content/notice` | `content/notice/index` |
| 操作日志 | `/monitor/log` | `monitor/log/index` |
| 在线用户 | `/monitor/online` | `monitor/online/index` |
| 红楼诗词 | `/classics/honglou/poems` | `classics/honglou/poems/index` |
| 红楼人物 | `/classics/honglou/characters` | `classics/honglou/characters/index` |
| 人物关系 | `/classics/honglou/relations` | `classics/honglou/relations/index` |
| 西游诗词 | `/classics/xiyou/poems` | `classics/xiyou/poems/index` |
| 西游人物 | `/classics/xiyou/characters` | `classics/xiyou/characters/index` |
| 八十一难 | `/classics/xiyou/events` | `classics/xiyou/events/index` |
| 三国诗词 | `/classics/sanguo/poems` | `classics/sanguo/poems/index` |
| 三国人物 | `/classics/sanguo/characters` | `classics/sanguo/characters/index` |
| 水浒诗词 | `/classics/shuihu/poems` | `classics/shuihu/poems/index` |
| 水浒章节 | `/classics/shuihu/chapters` | `classics/shuihu/chapters/index` |

#### 10.8.5 踩坑记录

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| `Maximum call stack size exceeded` | `to.matched.length <= 1` 判断 + `next({...to, replace: true})` 造成无限循环；以及 Layout 的 `redirect: '/dashboard'` 在 children 为空时死循环 | 用 `dynamicRoutesAdded` 布尔标记替代；删除 Layout redirect，在 `beforeEach` 中处理 `/` → `/dashboard` |
| 切换标签时重复请求业务数据 | 动态路由 `name` 使用中文菜单名（如"用户管理"），与组件 `defineOptions name`（如 `SystemUser`）不匹配，导致 `keep-alive` 缓存失效 | `componentMap` 中增加 `name` 字段，动态路由使用英文名，确保与组件 `defineOptions name` 一致 |
| 刷新后路由丢失 | `menus` 只存 Pinia 内存 | `menus`/`roles`/`perms` 持久化到 `localStorage`，初始化时恢复 |

#### 10.8.6 无需修改的文件

| 文件 | 原因 |
|------|------|
| `ui/src/layout/index.vue` | 侧边栏已通过 `userStore.menus` 动态渲染，`el-menu router` 属性自动匹配 |
| `ui/src/layout/SubMenu.vue` | 递归渲染逻辑不变，从 `menus` 树遍历 |
| `ui/src/api/auth.js` | API 接口不变，`getRoutersApi` 照常调用 |
| 后端 `AuthController` / `AuthService` / `SysMenuService` | 返回菜单树的接口逻辑不变 |

---

> **文档维护**: 本文档由项目代码自动分析生成，建议在重大版本更新后重新生成。
