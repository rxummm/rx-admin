# RX Admin 通用后台管理系统

基于 **SpringBoot 3.5 + Vue 3** 的全栈后台管理系统。双 MySQL 数据源，Sa-Token 内存模式，Caffeine 本地缓存，无 Redis。

## 技术栈

### 后端
- **SpringBoot 3.5.15** - Java 17+
- **MyBatis Plus 3.5.5** - ORM 框架
- **Sa-Token 1.37.0** - 轻量级权限认证框架（内存模式）
- **MySQL 8.0** - 数据库（主库 rx_admin + 次库 rxusysadmin）
- **Knife4j 4.4.0** - API 文档
- **MapStruct 1.5.5** - 对象映射
- **Caffeine** - 本地缓存（无 Redis）
- **Guava** - 限流（RateLimiter）
- **Spring Mail** - SMTP 邮件发送

### 前端
- **Vue 3.4** - 渐进式框架
- **Vite 5** - 构建工具
- **Element Plus 2.4** - UI 组件库
- **Pinia** - 状态管理
- **Vue Router 4** - 路由管理
- **Axios** - HTTP 请求
- **ECharts 6** - 图表
- **SCSS (Dart Sass)** - 样式预处理
- **Vue I18n** - 国际化

## 功能模块

- 用户认证（登录/登出/Token/心跳）
- 用户管理（CRUD、角色分配、数据导出）
- 角色管理（CRUD、菜单权限分配、数据导出）
- 菜单管理（树形菜单配置，DB 驱动路由）
- 操作日志（记录与查询）
- 权限控制（按钮级权限）
- **数据导出（Excel/PDF 双模式：前端导出 + 后端导出）**
- **历代文学管理**（作者/朝代/体裁/作品管理）
- **四大名著**（红楼梦/三国/水浒/西游的数据管理）
- **技术博客**（多源聚合抓取）
- **音乐播放器**（本地 MP3 管理）
- **系统监控**（健康检查、在线用户、慢查询）
- **v2.0 新功能**（见 AGENTS.md）

## 项目结构

```
RX/
├── pom.xml                              # Maven 配置 (Spring Boot 3.5.15)
├── src/main/java/com/rx/admin/
│   ├── RxAs400Application.java          # 启动类（exclude DataSourceAutoConfiguration）
│   ├── common/                          # Result, PageResult, 异常处理, 注解, AOP
│   ├── framework/                       # 框架配置：双数据源, MyBatis, Sa-Token, CORS, 限流
│   └── modules/{domain}/{entity}/       # 业务模块分层: dto/, vo/, convert/, controller/, service/, mapper/
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   ├── application-local.yml            # 本地开发配置（含邮件 SMTP，gitignored）
│   ├── application-prod.yml             # 生产环境配置
│   ├── db/                              # SQL 脚本（27 个：建表、菜单、迁移）
│   └── docs/                            # 技术文档
└── ui/                                  # 前端项目
    ├── .env.development                 # 开发环境变量
    ├── .env.production                  # 生产环境变量
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js                      # 入口
        ├── api/                         # API 模块
        ├── composables/                 # 可复用组合式函数
        ├── layout/                      # 布局组件
        ├── router/                      # constantRoutes + componentMap
        ├── stores/                      # Pinia 状态管理
        ├── styles/                      # 全局样式 + CSS 变量主题
        ├── utils/                       # 工具函数
        └── views/                       # 页面组件
```

## 快速启动

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+（需已建好 rx_admin + rxusysadmin 两个数据库）
- Node.js 18+

### 1. 初始化数据库

按顺序执行以下 SQL 脚本：

```sql
source src/main/resources/db/init.sql
source src/main/resources/db/features_init.sql
source src/main/resources/db/features_menu.sql
```

`features_init.sql` 和 `features_menu.sql` 不会自动执行，缺失会分别导致"系统繁忙"错误和菜单不可见。

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/rx_admin?...
      username: root
      password: your_password
    second:
      jdbc-url: jdbc:mysql://localhost:3306/rxusysadmin?...
      username: root
      password: your_password
```

双数据源需分别配置 primary 和 second。

### 3. 启动后端

```bash
cd RX
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

`local` profile 启用 MyBatis SQL 日志和本地邮件配置。不带此参数需自行设置邮箱环境变量。

后端启动后访问: http://localhost:8088

API 文档地址: http://localhost:8088/doc.html (Knife4j)

### 4. 启动前端

```bash
cd RX/ui
npm install
npm run dev
```

前端启动后访问: http://localhost:3000

### 5. 登录系统

- 默认账号: `admin`
- 默认密码: `admin123`
- 开发验证码: `dev000`（任意账号均可用）

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/logout | POST | 用户登出 |
| /api/auth/user-info | GET | 获取用户信息 |
| /api/auth/routers | GET | 获取路由菜单 |
| /api/sys/user/page | GET | 用户分页列表 |
| /api/sys/user | POST | 新增用户 |
| /api/sys/user | PUT | 修改用户 |
| /api/sys/user/{id} | DELETE | 删除用户 |
| /api/sys/role/list | GET | 角色列表 |
| /api/sys/role | POST | 新增角色 |
| /api/sys/role | PUT | 修改角色 |
| /api/sys/role/{id} | DELETE | 删除角色 |
| /api/sys/menu/tree | GET | 菜单树 |
| /api/sys/log/page | GET | 日志分页列表 |

## 参考

详细开发规范和架构说明见 `AGENTS.md`（项目根目录）和 `src/main/resources/docs/`。
