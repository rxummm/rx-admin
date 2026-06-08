# RX Admin 项目搭建与新增模块指南

> **版本**: 1.0.0 | **更新日期**: 2026-06-06 | **独立参考文档**
>
> 本文档从 `rxadmin.md` 中独立抽取，包含从零搭建项目和新增业务模块的完整步骤。
> 主架构文档见：[rxadmin.md](./rxadmin.md)

---

## 目录

- [1. 从零搭建项目指南](#1-从零搭建项目指南)
  - [1.1 环境准备](#11-环境准备)
  - [1.2 后端项目创建](#12-后端项目创建)
  - [1.3 前端项目创建](#13-前端项目创建)
- [2. 新增业务模块指南](#2-新增业务模块指南)
  - [2.1 需求分析](#21-需求分析)
  - [2.2 后端实现步骤](#22-后端实现步骤)
  - [2.3 前端实现步骤](#23-前端实现步骤)
  - [2.4 完整新增步骤速查表](#24-完整新增步骤速查表)
  - [2.5 设计要点与最佳实践](#25-设计要点与最佳实践)

---

## 1. 从零搭建项目指南

> 本章详细记录如何从空白环境一步步创建 RX Admin 前后端项目，适合新成员快速上手或从零重建项目。

### 1.1 环境准备

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

### 1.2 后端项目创建（Spring Boot 3）

#### 步骤 1：生成 Spring Boot 项目骨架

**方式一：Spring Initializr（推荐）**

访问 https://start.spring.io/ 配置：

| 配置项 | 值 |
|--------|-----|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.0 |
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
    <version>3.2.0</version>
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
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

然后执行 `mvn clean compile` 下载依赖。

#### 步骤 2：创建启动类

`src/main/java/com/rx/admin/RxAdminApplication.java`:

```java
package com.rx.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RxAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(RxAdminApplication.class, args);
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

### 1.3 前端项目创建（Vue 3 + Vite）

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

## 2. 新增业务模块指南

> 本章以新增 **"历代文学"** 模块为例，详细说明从后端到前端新增一个完整业务模块的全流程步骤。该模块包含：父菜单「历代文学」→ 子菜单「国内文学」「国外文学」→ 各子菜单下按朝代/历史时期展示文学内容。

### 2.1 需求分析

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

### 2.2 后端实现步骤

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

```sql
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

### 2.3 前端实现步骤

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

### 2.4 完整新增步骤速查表

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

### 2.5 设计要点与最佳实践

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

> **文档维护**: 本文档由 [rxadmin.md](./rxadmin.md) 第 10、11 章抽取独立成文。主文档结构变更后请同步更新本文档引用路径。
