# RX Admin 通用后台管理系统

基于 **SpringBoot 3 + Vue 3** 的全栈后台管理系统。

## 技术栈

### 后端
- **SpringBoot 3.2.0** - Java 17+
- **MyBatis Plus 3.5.5** - ORM 框架
- **Sa-Token 1.37.0** - 轻量级权限认证框架（内存模式）
- **MySQL 8.0** - 数据库
- **Knife4j** - API 文档

### 前端
- **Vue 3** - 渐进式框架
- **Vite 5** - 构建工具
- **Element Plus** - UI 组件库
- **Pinia** - 状态管理
- **Vue Router 4** - 路由管理
- **Axios** - HTTP 请求

## 功能模块

- 用户认证（登录/登出/Token）
- 用户管理（CRUD、角色分配、数据导出）
- 角色管理（CRUD、菜单权限分配、数据导出）
- 菜单管理（树形菜单配置）
- 操作日志（记录与查询）
- 权限控制（按钮级权限）
- **数据导出（Excel/PDF 双模式：前端导出 + 后端导出）**

## 项目结构

```
RX/
├── pom.xml                          # Maven 配置
├── src/main/java/com/rx/admin/
│   ├── RxAdminApplication.java      # 启动类
│   ├── common/                      # 通用模块（Result、BaseEntity、异常处理）
│   ├── config/                      # 配置类（MyBatis、Sa-Token、CORS）
│   ├── entity/                      # 实体类
│   ├── mapper/                      # 数据访问层
│   ├── service/                     # 业务逻辑层
│   └── controller/                  # 控制器层
├── src/main/resources/
│   ├── application.yml              # 应用配置
│   └── db/init.sql                  # 数据库初始化脚本
└── ui/                              # 前端项目
    ├── src/
    │   ├── api/                     # API 接口
    │   ├── assets/                  # 静态资源
    │   ├── layout/                  # 布局组件
    │   ├── router/                  # 路由配置
    │   ├── stores/                  # Pinia 状态管理
    │   ├── styles/                  # 全局样式
    │   ├── utils/                   # 工具函数
    │   └── views/                   # 页面组件
    ├── package.json
    └── vite.config.js
```

## 快速启动

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+

### 1. 初始化数据库

执行 `src/main/resources/db/init.sql` 脚本：

```sql
source src/main/resources/db/init.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rx_admin?...
    username: root
    password: your_password
```

### 3. 启动后端

```bash
cd RX
mvn clean package -DskipTests
mvn spring-boot:run
```

后端启动后访问: http://localhost:8088

API 文档地址: http://localhost:8088/doc.html

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
