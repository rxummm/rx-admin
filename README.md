# RX Admin

> 基于 Spring Boot 3.5.15 + Vue 3 的现代化后台管理系统

## ✨ 特性

- **技术栈**：Spring Boot 3.5.15 + Vue 3 + Element Plus + Vite
- **双数据源**：MySQL 主从分离，支持 `rx_admin` 和 `rxusysadmin` 两个数据库
- **权限认证**：Sa-Token 内存模式，无 Redis 依赖
- **缓存策略**：Caffeine 本地缓存，替代 Redis
- **API 文档**：Knife4j OpenAPI 3.0 自动生成
- **代码规范**：MapStruct 数据转换，Lombok 简化代码
- **系统监控**：健康检查、缓存管理、日志分析、慢查询监控
- **文学数据**：四大名著（西游记、水浒传、三国演义、红楼梦）数据管理
- **AS400 集成**：IBM i 系统连接与数据查询
- **国际化**：支持中英文切换

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.15 | 应用框架 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| Sa-Token | 1.37.0 | 权限认证 |
| Knife4j | 4.4.0 | API 文档 |
| MapStruct | 1.5.5 | 数据转换 |
| Caffeine | 3.x | 本地缓存 |
| MySQL Connector | 8.x | 数据库驱动 |
| JTOpen (jt400) | 20.0.8 | AS400 连接 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Element Plus | 2.4.x | UI 组件库 |
| Vue Router | 4.2.x | 路由管理 |
| Pinia | 2.1.x | 状态管理 |
| Vite | 5.0.x | 构建工具 |
| ECharts | 6.1.x | 图表库 |
| wangEditor | 5.1.x | 富文本编辑器 |
| Vue Flow | 1.48.x | 流程图 |

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### 后端启动

```powershell
# 开发环境（开启 SQL 日志 + 本地邮件配置）
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 生产环境（需配置环境变量）
mvn spring-boot:run
```

- 访问地址：`http://localhost:8088`
- API 文档：`http://localhost:8088/doc.html`

### 前端启动

```powershell
cd ui
npm install
npm run dev
```

- 访问地址：`http://localhost:3000`
- 默认账号：`admin` / `admin123`

### 数据库初始化

```powershell
# 1. 创建数据库
CREATE DATABASE rx_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE rxusysadmin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 执行初始化脚本
# 位置：src/main/resources/db/
# 必须执行：features_init.sql, features_menu.sql
```

### 编译检查

```powershell
# 快速编译检查（不启动服务）
mvn compile -q
```

## 📁 项目结构

```
RX/
├── src/main/java/com/rx/admin/
│   ├── common/          # 公共模块（工具类、常量、异常处理）
│   ├── framework/       # 框架配置（数据源、安全、缓存）
│   ├── modules/         # 业务模块
│   │   ├── auth/        # 认证模块（登录、验证码）
│   │   ├── system/      # 系统管理（用户、角色、部门、配置）
│   │   ├── content/     # 内容管理（通知、消息）
│   │   ├── monitor/     # 系统监控（日志、健康、缓存）
│   │   ├── as400/       # AS400 模块（IService、技术博客）
│   │   └── classics/    # 文学数据（四大名著）
│   └── RxAdminApplication.java
├── src/main/resources/
│   ├── db/              # 数据库脚本
│   ├── docs/            # 项目文档
│   ├── application.yml  # 应用配置
│   └── logback-spring.xml
├── ui/                  # 前端项目
│   ├── src/
│   │   ├── views/       # 页面组件
│   │   ├── api/         # API 接口
│   │   ├── components/  # 公共组件
│   │   ├── composables/ # 组合式函数
│   │   ├── router/      # 路由配置
│   │   └── stores/      # 状态管理
│   ├── .env.development
│   └── package.json
├── pom.xml
└── AGENTS.md
```

## 🔧 核心功能

### 系统管理
- 用户管理：用户列表、新增、编辑、删除、批量操作
- 角色管理：角色列表、权限分配、数据权限配置
- 部门管理：部门树形结构、部门信息维护
- 菜单管理：动态路由配置、菜单权限控制
- 配置管理：系统配置项管理

### 内容管理
- 通知管理：系统通知发布、通知类型分类
- 消息管理：站内消息、消息状态管理

### 系统监控
- 日志管理：操作日志、登录日志查询
- 健康检查：系统健康状态监控
- 缓存管理：缓存统计、缓存清除
- 任务管理：定时任务配置、任务日志
- 慢查询：SQL 慢查询监控

### 文学数据
- 西游记：人物、诗词、事件管理
- 水浒传：章节、人物、诗词管理
- 三国演义：人物、诗词管理
- 红楼梦：人物、诗词、关系图谱

### AS400 集成
- IService：IBM i 系统服务管理
- 技术博客：文章抓取、分类管理

### 工具模块
- API 调试：接口测试工具
- 数据库工具：SQL 执行
- 代码生成：CRUD 代码生成
- 导出工具：数据导出
- 流程图：可视化流程设计

## 📋 开发规范

### 后端规范

| 规则 | 说明 |
|------|------|
| 依赖注入 | 使用 `@RequiredArgsConstructor`，禁止 `@Autowired` |
| API 前缀 | `/api/{module}/{entity}` |
| 响应格式 | `Result.ok(data)` / `Result.fail(msg)` |
| 分页响应 | `PageResult.of(IPage)` |
| 数据转换 | 使用 MapStruct，禁止 `BeanUtils.copyProperties` |
| 权限注解 | `@SaCheckPermission("module:entity:op")` |
| 事务管理 | `@Transactional` + `@OperateLog` |
| 参数校验 | `@Valid` + DTO |

### 前端规范

| 规则 | 说明 |
|------|------|
| 组件定义 | `<script setup>` + `defineOptions({ name: 'PascalName' })` |
| 表格页面 | 使用 `useTablePage(apiModule)` |
| 存储操作 | 使用 `useStorage(STORAGE_KEYS.X)`，禁止直接操作 `localStorage` |
| HTML 渲染 | `v-html` 必须使用 `sanitizeHtml()` |
| 样式规范 | 使用 CSS 变量，禁止硬编码颜色 |
| 国际化 | 使用 `$t('key')`，禁止硬编码中文 |

## ⚙️ 配置说明

### 后端配置

主要配置文件：`src/main/resources/application.yml`

```yaml
server:
  port: 8088

spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/rx_admin
      username: ${DB_USERNAME:root}
      password: ${DB_PASSWORD:}
    secondary:
      url: jdbc:mysql://localhost:3306/rxusysadmin
      username: ${DB_USERNAME:root}
      password: ${DB_PASSWORD:}

sa-token:
  is-concurrent: false
  is-share: false
```

### 前端配置

主要配置文件：`ui/.env.development`

```
VITE_API_PROXY_TARGET=http://localhost:8088
```

## 🔒 安全特性

- XSS 防护：前端 DOMPurify 过滤
- CSRF 防护：Sa-Token 内置
- 限流控制：基于令牌桶的接口限流
- IP 过滤：黑白名单 IP 访问控制
- 输入验证：Bean Validation 参数校验

## 📝 提交规范

遵循 Conventional Commits 规范：

```
feat: 新增用户收藏功能
fix: 修复仪表盘 SSE 连接断开问题
refactor: 重构 SysMenuService 树形查询
docs: 更新 README 部署说明
style: 统一代码格式
test: 增加 AuthService 单元测试
chore: 升级 element-plus 到 2.7.0
```

## 🐛 常见问题

### Q: 开发环境验证码是多少？
A: `dev000`（仅 admin 用户）

### Q: 部署后提示"系统繁忙"？
A: 请确保已执行 `db/features_init.sql` 和 `db/features_menu.sql`

### Q: 邮件发送失败？
A: 配置环境变量 `MAIL_HOST`、`MAIL_USERNAME`、`MAIL_PASSWORD` 或创建 `application-local.yml`

### Q: 前端请求跨域？
A: 后端已配置 CORS，开发环境通过 Vite 代理 `/api` → `http://localhost:8088`

## 📄 许可证

MIT License

## 📧 联系方式

如有问题或建议，欢迎提交 Issue 或 PR。