# RX Admin 项目总结文档

**文档版本**: v1.0  
**最后更新**: 2026年6月13日  
**维护人员**: AI Assistant  

---

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目架构](#项目架构)
- [核心功能模块](#核心功能模块)
- [数据库设计](#数据库设计)
- [前端架构](#前端架构)
- [后端架构](#后端架构)
- [API接口规范](#api接口规范)
- [部署与运维](#部署与运维)
- [开发规范](#开发规范)
- [常见问题](#常见问题)
- [更新日志](#更新日志)

---

## 项目概述

### 项目名称
**RX Admin** - 通用后台管理系统

### 项目简介
RX Admin 是一个基于 **SpringBoot 3 + Vue 3** 的全栈后台管理系统，采用现代化的前后端分离架构。系统提供了完整的用户管理、权限控制、数据导出等功能，同时集成了多个特色业务模块（四大名著内容管理、音乐播放器、AS400系统集成等）。

### 项目特点
- 🚀 **现代化技术栈**：SpringBoot 3.5.15 + Vue 3.4 + Vite 5
- 🔐 **完善的权限体系**：基于 Sa-Token 的 RBAC 权限模型
- 📊 **丰富的监控能力**：Actuator + Prometheus 监控指标
- 🎨 **优雅的UI设计**：Element Plus + 自定义主题
- 📦 **强大的代码生成器**：一键生成 CRUD 代码
- 🌍 **多数据源支持**：主从数据库配置
- 📱 **响应式布局**：适配多种屏幕尺寸

### 应用场景
- 企业内部管理系统
- 数据管理平台
- 内容管理系统（CMS）
- 业务支撑平台

---

## 技术栈

### 后端技术栈

#### 核心框架
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.15 | Java Web 应用框架 |
| Java | 17+ | 编程语言 |
| Maven | 3.8+ | 项目构建工具 |

#### 持久层
| 技术 | 版本 | 说明 |
|------|------|------|
| MyBatis Plus | 3.5.5 | ORM 框架，增强版 MyBatis |
| MySQL Connector | 8.x | MySQL 数据库驱动 |

#### 安全认证
| 技术 | 版本 | 说明 |
|------|------|------|
| Sa-Token | 1.37.0 | 轻量级权限认证框架 |
| Spring Security Crypto | - | BCrypt 密码加密 |

#### API 文档
| 技术 | 版本 | 说明 |
|------|------|------|
| Knife4j | 4.4.0 | Swagger UI 增强版 |
| SpringDoc | - | OpenAPI 3 规范实现 |

#### 工具库
| 技术 | 版本 | 说明 |
|------|------|------|
| Lombok | - | 简化 Java 代码 |
| MapStruct | 1.5.5 | 对象映射转换 |
| Guava | 33.0.0 | Google 工具库（限流） |
| Jsoup | 1.17.2 | HTML 解析器 |

#### 文件处理
| 技术 | 版本 | 说明 |
|------|------|------|
| FastExcel | 1.3.0 | 高性能 Excel 读写 |
| Apache PDFBox | 3.0.1 | PDF 操作库 |
| mp3agic | 0.9.1 | MP3 元数据解析 |

#### 缓存与监控
| 技术 | 版本 | 说明 |
|------|------|------|
| Caffeine | - | 高性能本地缓存 |
| Spring Cache | - | 缓存抽象层 |
| Actuator | - | 应用监控 |
| Micrometer | - | 指标收集（Prometheus） |

#### 其他
| 技术 | 版本 | 说明 |
|------|------|------|
| JTOpen (jt400) | 20.0.8 | AS400/IBM i 连接 |
| Spring Mail | - | 邮件发送（SMTP） |

---

### 前端技术栈

#### 核心框架
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 渐进式 JavaScript 框架 |
| Vite | 5.0.10 | 下一代前端构建工具 |
| Vue Router | 4.2.5 | 官方路由管理器 |
| Pinia | 2.1.7 | Vue 状态管理库 |

#### UI 组件库
| 技术 | 版本 | 说明 |
|------|------|------|
| Element Plus | 2.4.3 | Vue 3 UI 组件库 |
| @element-plus/icons-vue | 2.3.1 | Element Plus 图标库 |

#### 可视化与图表
| 技术 | 版本 | 说明 |
|------|------|------|
| ECharts | 6.1.0 | 数据可视化图表库 |
| @antv/x6 | 3.1.7 | 图编辑引擎 |
| @vue-flow/core | 1.48.2 | 流程图组件 |
| LogicFlow | 2.2.3 | 流程图库 |

#### 编辑器
| 技术 | 版本 | 说明 |
|------|------|------|
| WangEditor | 5.1.23 | 富文本编辑器 |
| md-editor-v3 | 6.5.1 | Markdown 编辑器 |

#### 工具库
| 技术 | 版本 | 说明 |
|------|------|------|
| Axios | 1.6.2 | HTTP 请求库 |
| NProgress | 0.2.0 | 页面加载进度条 |
| DOMPurify | 3.4.8 | HTML  sanitization |
| marked | 18.0.4 | Markdown 解析器 |
| highlight.js | 11.11.1 | 代码高亮 |
| html2canvas | 1.4.1 | HTML 转图片 |
| vue-virtual-scroller | 2.0.0-beta.8 | 虚拟滚动 |

#### 国际化
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue I18n | 9.14.4 | Vue 国际化插件 |

#### 监控与追踪
| 技术 | 版本 | 说明 |
|------|------|------|
| @sentry/vue | 10.57.0 | 错误监控 |
| @sentry/tracing | 7.120.4 | 性能追踪 |

#### 样式预处理器
| 技术 | 版本 | 说明 |
|------|------|------|
| Sass | 1.69.5 | CSS 预处理器 |

#### 自动导入
| 技术 | 版本 | 说明 |
|------|------|------|
| unplugin-auto-import | 0.17.3 | 自动导入 API |
| unplugin-vue-components | 0.26.0 | 自动导入组件 |

---

## 项目架构

### 整体架构图

```
┌─────────────────────────────────────────────┐
│                  客户端浏览器                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Vue 3   │  │ Pinia    │  │  Router  │  │
│  └──────────┘  └──────────┘  └──────────┘  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │Element+  │  │  Axios   │  │  Sentry  │  │
│  └──────────┘  └──────────┘  └──────────┘  │
└──────────────────┬──────────────────────────┘
                   │ HTTP/REST API
                   ▼
┌─────────────────────────────────────────────┐
│              Spring Boot 后端                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │Controller│  │ Service  │  │  Mapper  │  │
│  └──────────┘  └──────────┘  └──────────┘  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │Sa-Token  │  │MyBatis+  │  │ Caffeine │  │
│  └──────────┘  └──────────┘  └──────────┘  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │Knife4j   │  │Actuator  │  │  AOP     │  │
│  └──────────┘  └──────────┘  └──────────┘  │
└──────────────────┬──────────────────────────┘
                   │ JDBC
                   ▼
┌─────────────────────────────────────────────┐
│              MySQL 数据库                     │
│  ┌──────────────┐  ┌──────────────┐         │
│  │  rx_admin    │  │rxusysadmin   │         │
│  │  (主数据源)   │  │(第二数据源)   │         │
│  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────┘
```

### 后端分层架构

```
com.rx.admin
├── common/                      # 通用模块
│   ├── annotation/              # 自定义注解
│   ├── aspect/                  # AOP 切面
│   ├── base/                    # 基类（BaseEntity, BaseController）
│   ├── constant/                # 常量定义
│   ├── exception/               # 异常定义
│   ├── handler/                 # 全局处理器
│   ├── result/                  # 统一返回结果
│   ├── security/                # 安全相关
│   └── utils/                   # 工具类
├── config/                      # 配置类
├── controller/                  # 控制器层
│   ├── classics/                # 四大名著模块
│   └── ...                      # 其他业务控制器
├── entity/                      # 实体类
│   ├── classics/                # 四大名著实体
│   └── ...                      # 其他业务实体
├── framework/                   # 框架层
├── mapper/                      # 数据访问层
├── modules/                     # 业务模块
├── service/                     # 业务逻辑层
└── task/                        # 定时任务
```

### 前端目录结构

```
ui/src
├── api/                         # API 接口定义
├── assets/                      # 静态资源
├── components/                  # 公共组件
├── composables/                 # 组合式函数
├── i18n/                        # 国际化配置
├── layout/                      # 布局组件
├── router/                      # 路由配置
├── stores/                      # Pinia 状态管理
├── styles/                      # 全局样式
├── types/                       # TypeScript 类型定义
├── utils/                       # 工具函数
└── views/                       # 页面组件
    ├── as400/                   # AS400 集成模块
    ├── classics/                # 四大名著模块
    ├── content/                 # 内容管理模块
    ├── dashboard/               # 仪表盘模块
    ├── login/                   # 登录页面
    ├── monitor/                 # 系统监控模块
    ├── permission/              # 权限管理模块
    ├── profile/                 # 个人中心
    ├── system/                  # 系统管理模块
    └── tool/                    # 系统工具模块
```

---

## 核心功能模块

### 1. 系统管理模块

#### 1.1 用户管理 (SysUserController)
- ✅ 用户 CRUD 操作
- ✅ 角色分配
- ✅ 数据导出（Excel/PDF）
- ✅ 用户收藏管理
- ✅ 在线用户查看

**主要接口**：
- `GET /api/sys/user/page` - 分页查询用户
- `POST /api/sys/user` - 新增用户
- `PUT /api/sys/user` - 修改用户
- `DELETE /api/sys/user/{id}` - 删除用户
- `GET /api/sys/user/favorites` - 获取用户收藏

#### 1.2 角色管理 (SysRoleController)
- ✅ 角色 CRUD 操作
- ✅ 菜单权限分配
- ✅ 数据导出

**主要接口**：
- `GET /api/sys/role/list` - 角色列表
- `POST /api/sys/role` - 新增角色
- `PUT /api/sys/role` - 修改角色
- `DELETE /api/sys/role/{id}` - 删除角色

#### 1.3 菜单管理 (SysMenuController)
- ✅ 树形菜单配置
- ✅ 菜单权限分配
- ✅ 动态路由生成

**主要接口**：
- `GET /api/sys/menu/tree` - 获取菜单树
- `POST /api/sys/menu` - 新增菜单
- `PUT /api/sys/menu` - 修改菜单
- `DELETE /api/sys/menu/{id}` - 删除菜单

#### 1.4 部门管理 (SysDeptController)
- ✅ 部门树形结构
- ✅ 部门 CRUD 操作

#### 1.5 字典管理 (SysDictDataController, SysDictTypeController)
- ✅ 字典类型管理
- ✅ 字典数据管理
- ✅ 字典缓存刷新

#### 1.6 系统配置 (SysConfigController)
- ✅ 系统参数配置
- ✅ 配置缓存管理

#### 1.7 文件管理 (SysFileController)
- ✅ 文件上传下载
- ✅ 文件列表查询
- ✅ 文件删除

#### 1.8 IP 规则管理 (SysIpRuleController)
- ✅ IP 黑白名单配置
- ✅ IP 过滤开关

---

### 2. 认证授权模块

#### 2.1 登录认证 (AuthController)
- ✅ 用户名密码登录
- ✅ Token 生成与验证
- ✅ 登出功能
- ✅ 获取用户信息
- ✅ 获取动态路由

**主要接口**：
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/user-info` - 获取用户信息
- `GET /api/auth/routers` - 获取路由菜单

#### 2.2 验证码 (CaptchaController)
- ✅ 图形验证码生成
- ✅ 验证码校验

#### 2.3 权限管理 (SysPermissionManageController)
- ✅ 权限申请审批
- ✅ 权限分配管理

#### 2.4 权限申请 (SysPermissionRequestController)
- ✅ 提交权限申请
- ✅ 申请记录查询
- ✅ 申请审批

---

### 3. 系统监控模块

#### 3.1 操作日志 (SysLogController)
- ✅ 操作日志记录
- ✅ 日志查询与筛选
- ✅ 日志导出

#### 3.2 登录日志 (SysLoginLogController)
- ✅ 登录日志记录
- ✅ 登录统计

#### 3.3 在线用户 (SysOnlineController)
- ✅ 在线用户列表
- ✅ 强制下线

#### 3.4 定时任务 (SysJobController, SysJobLogController)
- ✅ 任务管理
- ✅ 任务执行日志
- ✅ 任务启停控制

#### 3.5 慢查询监控 (SysSlowQueryController)
- ✅ 慢 SQL 检测
- ✅ 慢查询记录
- ✅ 阈值配置（默认 2000ms）

#### 3.6 缓存管理 (CacheManageController)
- ✅ 缓存查看
- ✅ 缓存清除
- ✅ 缓存统计

#### 3.7 健康检查 (HealthController)
- ✅ 应用健康状态
- ✅ 数据库连接检查

#### 3.8 仪表盘 (DashboardController, DashboardEnhancedController)
- ✅ 系统概览统计
- ✅ 数据可视化图表
- ✅ 实时监控数据

---

### 4. 系统工具模块

#### 4.1 代码生成器 (GenController)
- ✅ 表结构扫描
- ✅ 代码模板配置
- ✅ 一键生成 CRUD 代码
- ✅ 生成的代码包括：
  - Entity 实体类
  - Mapper 数据访问层
  - Service 业务逻辑层
  - Controller 控制器层
  - Vue 前端页面

#### 4.2 数据导入导出 (ImportController, ExportController)
- ✅ Excel 导入
- ✅ Excel 导出（前端导出 + 后端导出）
- ✅ PDF 导出
- ✅ 导出配置管理
- ✅ 导出日志记录

#### 4.3 数据库工具 (DatabaseToolController)
- ✅ SQL 执行控制台
- ✅ 表结构查看
- ✅ 数据备份恢复

#### 4.4 API 调试 (ApiDebugController)
- ✅ API 在线测试
- ✅ 请求参数配置
- ✅ 响应结果展示

#### 4.5 API 分析 (ApiAnalysisController)
- ✅ API 调用统计
- ✅ 接口性能分析

#### 4.6 日志分析 (LogAnalysisController)
- ✅ 日志文件查看
- ✅ 日志搜索过滤
- ✅ 日志统计分析

#### 4.7 数据备份 (BackupController)
- ✅ 数据库备份
- ✅ 备份文件管理
- ✅ 备份恢复

#### 4.8 开发工具 (DevToolsController)
- ✅ 常用开发辅助工具

#### 4.9 常用工具 (CommonToolsController)
- ✅ 文件上传下载
- ✅ 文档转换
- ✅ 数据格式化工具

#### 4.10 中国地区管理 (ChinaRegionController)
- ✅ 省市区三级联动
- ✅ 地区数据查询

#### 4.11 字典管理 (dict/)
- ✅ 字典数据维护

#### 4.12 文档上传 (docUpload/)
- ✅ 文档批量上传
- ✅ 文档分类管理

#### 4.13 文档转换器 (docConverter/)
- ✅ 文档格式转换

#### 4.14 Excel 解析器 (excelParser/)
- ✅ Excel 数据解析
- ✅ 数据提取

#### 4.15 流程图设计 (flowChart/)
- ✅ 流程图绘制
- ✅ 流程节点配置
- ✅ 流程保存加载

#### 4.16 邮件发送器 (emailSender/)
- ✅ SMTP 邮件发送
- ✅ 邮件模板配置

#### 4.17 数据导入 (importData/)
- ✅ 批量数据导入
- ✅ 导入数据校验

#### 4.18 标准规范 (standards/)
- ✅ 标准文档管理

---

### 5. 四大名著模块 (classics/)

#### 5.1 红楼梦 (honglou/)
- ✅ 诗词管理
- ✅ 人物管理
- ✅ 章节管理

**主要功能**：
- 诗词列表展示（支持表格内滚动）
- 诗词详情查看
- 诗词搜索过滤
- 诗词数据导出

#### 5.2 三国演义 (sanguo/)
- ✅ 人物管理
- ✅ 战役管理

#### 5.3 水浒传 (shuihu/)
- ✅ 好汉管理
- ✅ 章节管理

#### 5.4 西游记 (xiyou/)
- ✅ 妖怪管理
- ✅ 章节管理
- ✅ 法宝管理

#### 5.5 文学经典 (literature/)
- ✅ 文学作品管理
- ✅ 作者管理

---

### 6. 内容管理模块 (content/)

#### 6.1 公告管理 (AnnouncementController)
- ✅ 公告发布
- ✅ 公告列表
- ✅ 公告阅读记录

#### 6.2 通知中心 (NotifyCenterController)
- ✅ 消息推送
- ✅ 多渠道通知（邮件、企业微信、钉钉、飞书）
- ✅ 通知记录

#### 6.3 消息管理 (SysMessageController)
- ✅ 站内消息
- ✅ 消息模板管理

#### 6.4 通知公告 (SysNoticeController)
- ✅ 通知公告发布
- ✅ 阅读状态跟踪

#### 6.5 技术博客 (TechBlogController)
- ✅ 博客文章抓取（Jsoup）
- ✅ 博客列表展示
- ✅ 文章内容查看

---

### 7. 特色业务模块

#### 7.1 音乐播放器 (MusicController)
- ✅ 音乐文件扫描
- ✅ MP3 元数据解析（mp3agic）
- ✅ 音乐播放列表
- ✅ 播放记录管理

**配置项**：
```yaml
music:
  folder: C:/Users/admin/Downloads/music
```

#### 7.2 AS400 集成 (As400Controller)
- ✅ AS400 系统连接
- ✅ 对象浏览
- ✅ 数据查询

**配置项**：
```yaml
as400:
  host: pub400.com
  username: ${AS400_USERNAME}
  password: ${AS400_PASSWORD}
  default-libraries: A7RXUZZ1,A7RXUZZ2,A7RXUZZB
```

#### 7.3 iService 管理 (IServiceController)
- ✅ 服务接口管理
- ✅ 服务参数配置
- ✅ 服务示例管理

---

### 8. 个人中心模块

#### 8.1 个人信息 (profile/)
- ✅ 个人资料修改
- ✅ 头像上传
- ✅ 密码修改

---

## 数据库设计

### 数据库概览

系统使用 **双数据源** 配置：

1. **主数据源**：`rx_admin` - 系统管理相关数据
2. **第二数据源**：`rxusysadmin` - 四大名著等业务数据

### 主要数据表

#### 系统管理表（rx_admin）

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| sys_user | 用户表 | id, username, password, nickname, email, phone, status, dept_id |
| sys_role | 角色表 | id, role_name, role_key, sort, status |
| sys_menu | 菜单表 | id, menu_name, parent_id, path, component, perms, icon, type |
| sys_user_role | 用户角色关联表 | user_id, role_id |
| sys_role_menu | 角色菜单关联表 | role_id, menu_id |
| sys_dept | 部门表 | id, dept_name, parent_id, ancestors, sort |
| sys_dict_type | 字典类型表 | id, dict_name, dict_type, status |
| sys_dict_data | 字典数据表 | id, dict_type, dict_label, dict_value, sort |
| sys_config | 系统配置表 | id, config_key, config_value, config_type |
| sys_file | 文件表 | id, file_name, file_path, file_size, file_type |
| sys_log | 操作日志表 | id, title, business_type, method, params, time, ip |
| sys_login_log | 登录日志表 | id, username, ip, login_time, status, msg |
| sys_job | 定时任务表 | id, job_name, job_group, invoke_target, cron_expression |
| sys_job_log | 任务日志表 | id, job_name, job_group, invoke_target, job_message, status |
| sys_notice | 通知公告表 | id, notice_title, notice_type, content, status |
| sys_message | 消息表 | id, title, content, message_type, receiver_id |
| sys_export_config | 导出配置表 | id, export_name, table_name, columns |
| sys_export_log | 导出日志表 | id, export_name, file_path, export_time |
| sys_ip_rule | IP 规则表 | id, ip_address, rule_type, status |
| sys_permission_request | 权限申请表 | id, user_id, menu_id, reason, status |
| sys_user_favorite | 用户收藏表 | id, user_id, favorite_type, favorite_id |
| sys_slow_query | 慢查询记录表 | id, sql_text, execute_time, query_time |

#### 四大名著表（rxusysadmin）

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| honglou_poem | 红楼梦诗词 | id, title, author, content, chapter |
| honglou_character | 红楼梦人物 | id, name, description, gender |
| sanguo_character | 三国人物 | id, name, description, force |
| shuihu_character | 水浒好汉 | id, name, nickname, rank |
| xiyou_monster | 西游妖怪 | id, name, description, chapter |

### 数据库特性

- ✅ **逻辑删除**：使用 `deleted` 字段（0=未删除，1=已删除）
- ✅ **自动填充**：创建时间、更新时间自动填充
- ✅ **索引优化**：关键字段建立索引
- ✅ **字符集**：UTF-8（支持中文）
- ✅ **时区**：Asia/Shanghai

---

## 前端架构

### 路由设计

#### 路由模式
- 使用 **History 模式**（需要服务器配置 fallback）
- 路由懒加载（按需加载组件）

#### 路由结构
```javascript
const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/index.vue')
      },
      // 动态路由由后端返回
    ]
  }
]
```

#### 路由守卫
- ✅ 登录验证（未登录跳转登录页）
- ✅ 权限验证（无权限提示 403）
- ✅ 页面标题设置
- ✅ 进度条显示（NProgress）

---

### 状态管理（Pinia）

#### 主要 Store

1. **userStore** - 用户信息
   - token
   - userInfo
   - roles
   - permissions

2. **appStore** - 应用配置
   - sidebar 折叠状态
   - theme 主题
   - language 语言

3. **permissionStore** - 权限管理
   - routes 动态路由
   - menus 菜单树

---

### API 封装（Axios）

#### 请求拦截器
```javascript
// 添加 Token
config.headers['Authorization'] = getToken()

// 防重放攻击
config.headers['X-Nonce'] = generateNonce()
config.headers['X-Timestamp'] = Date.now()
```

#### 响应拦截器
```javascript
// 统一错误处理
if (response.code === 401) {
  // Token 过期，跳转登录
  logout()
} else if (response.code !== 200) {
  // 业务错误提示
  ElMessage.error(response.msg)
}
```

#### API 模块化
```
src/api/
├── auth.js          # 认证相关
├── user.js          # 用户管理
├── role.js          # 角色管理
├── menu.js          # 菜单管理
├── classics/        # 四大名著
├── system/          # 系统管理
└── tool/            # 系统工具
```

---

### 组件设计

#### 布局组件（layout/）
- **index.vue** - 主布局容器
  - Header 顶部导航
  - Sidebar 侧边栏菜单
  - TagsView 标签页
  - Main 主内容区

#### 公共组件（components/）
- **ErrorBoundary.vue** - 错误边界组件
- **Pagination.vue** - 分页组件
- **SearchBar.vue** - 搜索栏组件
- **TableWrapper.vue** - 表格包装组件

#### 组合式函数（composables/）
- **useTableHeight.js** - 表格高度自适应
- **useExport.js** - 数据导出
- **useImport.js** - 数据导入
- **useAuth.js** - 权限判断

---

### 样式系统

#### 全局样式（styles/）
- **global.scss** - 全局样式
  - CSS 变量定义
  - 重置样式
  - 布局溢出控制
  - Element Plus 组件优化

#### CSS 变量
```scss
:root {
  --primary-color: #409eff;
  --sidebar-bg: #304156;
  --header-height: 50px;
  --tags-height: 34px;
  --border-light: rgba(0, 0, 0, 0.06);
}
```

#### 响应式设计
- ✅ 桌面端（≥1200px）
- ✅ 平板端（768px - 1199px）
- ✅ 移动端（<768px）

---

### 性能优化

#### 构建优化（vite.config.js）
```javascript
build: {
  rollupOptions: {
    output: {
      manualChunks: {
        'echarts': ['echarts'],
        'element-plus': ['element-plus'],
      }
    }
  }
}
```

#### 代码分割
- ✅ 路由懒加载
- ✅ 组件异步加载
- ✅ 第三方库分包

#### 缓存策略
- ✅ 浏览器缓存（静态资源）
- ✅ 接口缓存（Caffeine）
- ✅ 菜单缓存（1 小时）
- ✅ 配置缓存（10 分钟）

---

## 后端架构

### 统一返回结果

#### Result 类
```java
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;
    
    public static <T> Result<T> success(T data) { ... }
    public static <T> Result<T> error(String msg) { ... }
}
```

#### 返回码规范
| 返回码 | 说明 |
|--------|------|
| 200 | 成功 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

### 异常处理

#### 全局异常处理器
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常，请联系管理员");
    }
}
```

#### 自定义异常
- **BusinessException** - 业务异常
- **AuthException** - 认证异常
- **PermissionException** - 权限异常

---

### AOP 切面

#### 操作日志切面
```java
@Aspect
@Component
public class LogAspect {
    
    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint point, Log log) {
        // 记录操作日志
    }
}
```

#### 限流切面
```java
@Aspect
@Component
public class RateLimitAspect {
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) {
        // Guava RateLimiter 限流
    }
}
```

---

### 安全机制

#### 1. 密码加密
- 使用 **BCrypt** 算法
- 盐值随机生成
- 不可逆加密

#### 2. Token 认证
- 使用 **Sa-Token** 框架
- Token 有效期：24 小时
- 活跃超时：30 分钟
- Token 样式：UUID

#### 3. 防重放攻击
- Nonce 唯一标识
- Timestamp 时间戳
- 时间窗口：5 分钟
- Nonce 缓存：10000 条

#### 4. IP 黑白名单
- 黑名单模式：禁止指定 IP
- 白名单模式：只允许指定 IP
- 可动态开关

#### 5. 验证码
- 图形验证码
- 有效期：5 分钟
- 自动清理过期验证码

#### 6. 登录保护
- 最大失败次数：5 次
- 锁定时长：30 分钟

---

### 缓存策略

#### Caffeine 本地缓存
```yaml
spring:
  cache:
    type: caffeine
```

#### 缓存配置
| 缓存项 | TTL | 说明 |
|--------|-----|------|
| 系统配置 | 10 分钟 | sys_config 表 |
| 菜单树 | 1 小时 | sys_menu 表 |
| 字典数据 | 1 小时 | sys_dict_data 表 |
| 验证码 | 5 分钟 | 临时缓存 |
| Nonce | 5 分钟 | 防重放 |

#### 缓存注解
```java
@Cacheable(value = "config", key = "#key")
@CacheEvict(value = "config", key = "#key")
@CachePut(value = "config", key = "#key")
```

---

### 定时任务

#### Quartz 集成
- 支持 Cron 表达式
- 任务持久化到数据库
- 任务执行日志

#### 内置任务
- 日志清理
- 缓存刷新
- 数据备份
- 慢查询检测

---

### 文件上传

#### 配置
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

#### 存储路径
- 上传目录：`D:/vueprojects/RX/ui/public/shareddocs`
- 支持文件类型：图片、文档、压缩包等

---

## API接口规范

### RESTful 风格

#### URL 命名规范
- 使用小写字母和连字符
- 使用复数名词表示资源集合
- 使用名词而非动词

**示例**：
```
GET    /api/sys/user          # 获取用户列表
GET    /api/sys/user/{id}     # 获取单个用户
POST   /api/sys/user          # 创建用户
PUT    /api/sys/user          # 更新用户
DELETE /api/sys/user/{id}     # 删除用户
```

#### HTTP 方法
| 方法 | 说明 | 幂等性 |
|------|------|--------|
| GET | 查询资源 | ✅ |
| POST | 创建资源 | ❌ |
| PUT | 更新资源（全量） | ✅ |
| PATCH | 更新资源（部分） | ❌ |
| DELETE | 删除资源 | ✅ |

---

### 请求参数

#### Query Parameters（GET 请求）
```
GET /api/sys/user/page?pageNum=1&pageSize=10&username=admin
```

#### Request Body（POST/PUT 请求）
```json
{
  "username": "admin",
  "password": "admin123",
  "nickname": "管理员",
  "email": "admin@example.com"
}
```

#### Path Variables
```
DELETE /api/sys/user/1
```

---

### 响应格式

#### 成功响应
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 1,
    "username": "admin"
  }
}
```

#### 分页响应
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "list": [...]
  }
}
```

#### 错误响应
```json
{
  "code": 401,
  "msg": "Token 过期，请重新登录",
  "data": null
}
```

---

### 认证方式

#### Header 参数
```
Authorization: Bearer {token}
X-Nonce: {uuid}
X-Timestamp: {timestamp}
```

---

### API 文档

#### Knife4j 地址
- 开发环境：http://localhost:8088/doc.html
- 生产环境：根据实际域名配置

#### 文档特性
- ✅ 在线调试
- ✅ 参数说明
- ✅ 响应示例
- ✅ 分组管理

---

## 部署与运维

### 环境要求

#### 后端
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- 内存：≥2GB
- 磁盘：≥10GB

#### 前端
- Node.js 18+
- npm 9+
- 内存：≥1GB
- 磁盘：≥5GB

---

### 开发环境部署

#### 1. 初始化数据库
```bash
mysql -u root -p < src/main/resources/db/init.sql
```

#### 2. 配置环境变量
```bash
# .env 文件或系统环境变量
MYSQL_USERNAME=root
MYSQL_PASSWORD=root
AS400_USERNAME=your_username
AS400_PASSWORD=your_password
MAIL_USERNAME=xubingzhen83@163.com
MAIL_PASSWORD=your_auth_code
```

#### 3. 启动后端
```bash
cd d:\vueprojects\RX
mvn clean package -DskipTests
mvn spring-boot:run
```

或使用 PowerShell 脚本：
```powershell
.\start-backend.ps1
```

#### 4. 启动前端
```bash
cd d:\vueprojects\RX\ui
npm install
npm run dev
```

或使用 PowerShell 脚本：
```powershell
.\start-frontend.ps1
```

#### 5. 访问系统
- 前端：http://localhost:3000
- 后端：http://localhost:8088
- API 文档：http://localhost:8088/doc.html

---

### 生产环境部署

#### 1. 后端打包
```bash
mvn clean package -DskipTests
```

生成的 JAR 包位置：`target/rx-admin-1.0.0.jar`

#### 2. 前端打包
```bash
cd ui
npm run build
```

生成的静态文件位置：`ui/dist/`

#### 3. 部署后端
```bash
# 使用 systemd 管理（Linux）
sudo systemctl start rx-admin
sudo systemctl enable rx-admin

# 或直接运行
nohup java -jar rx-admin-1.0.0.jar > backend.log 2>&1 &
```

#### 4. 部署前端
```bash
# 使用 Nginx
server {
    listen 80;
    server_name your-domain.com;
    
    root /path/to/ui/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8088;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

### 监控与告警

#### Actuator 监控端点
- 健康检查：http://localhost:8088/actuator/health
- Prometheus 指标：http://localhost:8088/actuator/prometheus

#### Prometheus 配置
```yaml
scrape_configs:
  - job_name: 'rx-admin'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8088']
```

#### Grafana 看板
- JVM 指标
- HTTP 请求指标
- 数据库连接池指标
- 缓存命中率

---

### 日志管理

#### 日志配置
```yaml
logging:
  file:
    path: D:/vueprojects/RX
    name: D:/vueprojects/RX/backend.log
  level:
    com.rx.admin: debug
```

#### 日志级别
- **ERROR** - 错误日志
- **WARN** - 警告日志
- **INFO** - 信息日志
- **DEBUG** - 调试日志（开发环境）

#### 日志轮转
- 使用 Logback 配置
- 按大小轮转（100MB）
- 按时间轮转（每天）
- 保留 30 天

---

### 备份策略

#### 数据库备份
- 每日凌晨 2:00 自动备份
- 备份文件保留 7 天
- 备份路径：`D:/vueprojects/RX/backups/`

#### 文件备份
- 上传文件定期备份
- 备份到云存储或 NAS

---

## 开发规范

### 代码规范

#### Java 编码规范
1. **命名规范**
   - 类名：大驼峰（PascalCase），如 `SysUserController`
   - 方法名：小驼峰（camelCase），如 `getUserById`
   - 常量：全大写+下划线，如 `MAX_RETRY_COUNT`
   - 变量名：小驼峰，语义清晰

2. **注释规范**
   - 类注释：包含作者、创建时间、功能描述
   - 方法注释：包含参数说明、返回值说明、异常说明
   - 复杂逻辑：添加行内注释

3. **异常处理**
   - 不要捕获后不处理
   - 使用自定义异常
   - 记录异常日志

4. **事务管理**
   - 使用 `@Transactional` 注解
   - 事务范围尽量小
   - 避免长事务

#### Vue 编码规范
1. **组件命名**
   - 文件名：kebab-case，如 `user-list.vue`
   - 组件名：PascalCase，如 `UserList`

2. **Props 定义**
   - 明确类型
   - 设置默认值
   - 添加验证

3. **Composition API**
   - 优先使用 `<script setup>`
   - 使用 composables 复用逻辑

4. **样式规范**
   - 使用 SCSS
   - 避免全局样式污染
   - 使用 CSS 变量

---

### Git 规范

#### 分支策略
- **main** - 主分支（生产环境）
- **develop** - 开发分支
- **feature/xxx** - 功能分支
- **hotfix/xxx** - 热修复分支

#### Commit 规范
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型**：
- `feat` - 新功能
- `fix` - 修复 Bug
- `docs` - 文档更新
- `style` - 代码格式
- `refactor` - 重构
- `test` - 测试
- `chore` - 构建/工具

**示例**：
```
feat(user): 添加用户导出功能

- 支持 Excel 导出
- 支持 PDF 导出
- 添加导出日志

Closes #123
```

---

### 测试规范

#### 单元测试
- 覆盖率 ≥ 80%
- 使用 JUnit 5
- Mock 外部依赖

#### 集成测试
- 测试 API 接口
- 测试数据库操作
- 测试事务

---

### 安全规范

1. **敏感信息**
   - 不要硬编码密码
   - 使用环境变量
   - 密钥存储在 Vault

2. **SQL 注入防护**
   - 使用参数化查询
   - 不要拼接 SQL

3. **XSS 防护**
   - 前端使用 DOMPurify
   - 后端过滤特殊字符

4. **CSRF 防护**
   - 使用 Token 验证
   - SameSite Cookie

5. **权限校验**
   - 前后端双重校验
   - 最小权限原则

---

## 常见问题

### 1. 启动问题

#### Q: 后端启动失败，提示数据库连接错误
**A**: 
1. 检查 MySQL 是否启动
2. 检查 `application.yml` 中的数据库配置
3. 检查数据库是否存在
4. 检查用户名密码是否正确

#### Q: 前端启动失败，提示端口被占用
**A**: 
1. 修改 `vite.config.js` 中的端口
2. 或关闭占用端口的进程

---

### 2. 登录问题

#### Q: 登录提示"用户名或密码错误"
**A**: 
1. 检查数据库中是否有该用户
2. 检查密码是否加密（BCrypt）
3. 默认账号：`admin`，密码：`admin123`

#### Q: 登录后频繁掉线
**A**: 
1. 检查 Token 有效期配置
2. 检查浏览器是否禁用 Cookie
3. 检查跨域配置

---

### 3. 权限问题

#### Q: 菜单不显示
**A**: 
1. 检查用户是否分配了角色
2. 检查角色是否分配了菜单
3. 检查菜单状态是否为启用
4. 清除浏览器缓存

#### Q: 按钮权限不生效
**A**: 
1. 检查权限标识是否正确
2. 检查前端权限指令 `v-permission`
3. 检查后端权限注解 `@PreAuthorize`

---

### 4. 导出问题

#### Q: Excel 导出乱码
**A**: 
1. 检查文件编码是否为 UTF-8
2. 检查浏览器是否正确识别编码
3. 添加 BOM 头

#### Q: 大数据量导出超时
**A**: 
1. 使用流式导出
2. 增加超时时间
3. 分批导出

---

### 5. 性能问题

#### Q: 页面加载慢
**A**: 
1. 检查网络请求数量
2. 检查接口响应时间
3. 启用 Gzip 压缩
4. 使用 CDN 加速

#### Q: 数据库查询慢
**A**: 
1. 检查是否有索引
2. 检查 SQL 语句
3. 使用慢查询日志分析
4. 优化查询条件

---

### 6. 布局问题

#### Q: 页面出现水平滚动条
**A**: 
1. 检查是否有元素宽度超出视口
2. 检查伪元素尺寸是否过大
3. 添加 `overflow-x: hidden`
4. 参考 `FIX_LAYOUT_OVERFLOW.md` 文档

#### Q: 页面出现垂直滚动条占位
**A**: 
1. 检查容器是否设置了 `overflow-y: auto`
2. 让子组件自己管理滚动
3. 移除负边距
4. 参考 `FIX_LAYOUT_OVERFLOW.md` 文档

---

## 更新日志

### v1.0.0 (2026-06-13)

#### ✨ 新功能
- 完成基础系统管理模块
- 完成四大名著内容管理模块
- 完成音乐播放器模块
- 完成 AS400 系统集成
- 完成代码生成器
- 完成数据导入导出功能
- 完成监控告警功能

#### 🐛 Bug 修复
- 修复 ResizeObserver 警告
- 修复水平滚动条问题
- 修复垂直滚动条占位问题
- 修复 Sentry 集成不一致问题

#### 🎨 优化
- 优化全局布局溢出控制
- 优化 Element Plus 表格滚动
- 优化页面容器负边距
- 优化伪元素动画

#### 📝 文档
- 创建项目总结文档（qorderupdate.md）
- 创建布局修复文档（FIX_LAYOUT_OVERFLOW.md）
- 完善 API 文档

---

## 附录

### A. 环境变量清单

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| MYSQL_USERNAME | MySQL 用户名 | root |
| MYSQL_PASSWORD | MySQL 密码 | root |
| AS400_HOST | AS400 主机地址 | pub400.com |
| AS400_USERNAME | AS400 用户名 | - |
| AS400_PASSWORD | AS400 密码 | - |
| MAIL_FROM | 邮件发件人 | xubingzhen83@163.com |
| MAIL_HOST | SMTP 服务器 | smtp.163.com |
| MAIL_PORT | SMTP 端口 | 465 |
| MAIL_USERNAME | 邮件用户名 | - |
| MAIL_PASSWORD | 邮件密码（授权码） | - |

---

### B. 配置文件清单

| 文件路径 | 说明 |
|----------|------|
| `src/main/resources/application.yml` | 主配置文件 |
| `src/main/resources/application-local.yml` | 本地环境配置 |
| `src/main/resources/application-prod.yml` | 生产环境配置 |
| `ui/.env.development` | 前端开发环境变量 |
| `ui/.env.production` | 前端生产环境变量 |
| `ui/vite.config.js` | Vite 构建配置 |

---

### C. 数据库脚本清单

| 文件路径 | 说明 |
|----------|------|
| `src/main/resources/db/init.sql` | 数据库初始化脚本 |
| `src/main/resources/sql/*.sql` | 其他 SQL 脚本 |

---

### D. 常用命令

#### 后端
```bash
# 编译
mvn clean compile

# 打包
mvn clean package -DskipTests

# 运行
mvn spring-boot:run

# 测试
mvn test
```

#### 前端
```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建
npm run build

# 预览构建结果
npm run preview
```

---

### E. 联系方式

- **项目负责人**: AI Assistant
- **技术支持**: 查看项目 Issues
- **文档维护**: 持续更新中

---

**文档结束**
