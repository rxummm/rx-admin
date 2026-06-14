# 项目优化建议

## 优化分级体系

| 优先级 | 含义 | 处理时限 |
|--------|------|---------|
| **P0** | 紧急安全/功能问题 | 1 天内 |
| **P1** | 高优先级，影响开发效率 | 1-2 周内 |
| **P2** | 中等优先级，性能/架构改进 | 1 个月内 |
| **P3** | 低优先级，工程化建设 | 持续进行 |

## 已完成的优化项 (v1.3.0+)

| 优先级 | 优化项 | 实现 |
|--------|--------|------|
| P0 | POST 请求参数修复 | `auth.js` 改用 `data` 传参 |
| P1 | 抽取通用表格 Composable | `useTablePage.js` — 分页/搜索/排序/列配置/高度适配/多选 |
| P1 | 拆分布局组件 | `SearchBox.vue` + `NoticePopover.vue` |
| P1 | 清理未使用 API | 16 个函数添加 `@reserved` 注释 |
| P1 | 异常处理优化 | 10 种异常处理器 (401/403/400/404/405/415/数据约束等) |
| P1 | 操作日志异步化 + 脱敏 | `@Async` + `sanitizeParams()` |
| P1 | PageResult 分页补全 | `of(total, page, size, records)` + `of(IPage)` |
| P1 | **架构重构: DTO/VO/Convert 分层** | 40+ DTO / 17+ VO / 16+ Convert, Entity 不暴露到 Controller |
| P1 | **MapStruct 对象转换引入** | 编译时类型安全转换, 替代 BeanUtils.copyProperties |
| P1 | **模块化架构 (framework/)** | datasource/mybatis/security/async/cache/web 六大配置模块 |
| P1 | **构造器注入统一** | @Autowired → @RequiredArgsConstructor + private final |
| P2 | **Spring Boot 3.5.15 升级** | Jakarta EE 适配, MapStruct 兼容性 |
| P2 | ECharts 分包 | `manualChunks` 独立 echarts chunk |
| P2 | Element Plus 图标按需引入 | `unplugin-vue-components` 自动导入 |
| P2 | Vite 构建分包 | `manualChunks: { echarts, element-plus }` |
| P2 | 生产环境 SQL 日志关闭 | `application-prod.yml` Slf4jImpl |
| P2 | 响应拦截器浅层优化 | `formatResponseData` 仅处理时间字段 |
| P2 | 语言切换无刷新 | `el-config-provider` + `i18n.global.locale.value` |
| P2 | localStorage 统一管理 | `useStorage.js` — 命名空间 `rx_admin_*` |
| P2 | 请求频率限制 | `RateLimiterConfig.java` — Guava RateLimiter 3次/秒 |
| P2 | 登录失败锁定 | `LoginAttemptService.java` — 5次失败锁定30分钟 |
| P2 | **build-helper-maven-plugin** | 解决 IDE 无法识别 MapStruct 生成代码 |
| P3 | DashboardController DI 优化 | `@RequiredArgsConstructor` |
| P3 | 清理根目录杂项文件 | 11 个无用文件已删除 |
| P3 | **文档体系重构** | SKILL.md / rxadmin-setup.md / AGENT.MD 三份文档更新至最新架构 |

## 待实施优化 (测试阶段暂保留)

| 优先级 | 优化项 | 说明 |
|--------|--------|------|
| P0 | 移除硬编码默认密码 | 发布前改为 `import.meta.env.DEV ? 'admin' : ''` |
| P0 | CORS 安全配置 | 发布前限制域名白名单 |
| P0 | 清理 init.sql 密码注释 | 发布前移除明文密码提示 |

## 后续可扩展优化

| 优化项 | 说明 |
|--------|------|
| TypeScript 迁移 | 渐进式: API 层 → Store 层 → 组件层 |
| ESLint + Prettier | 代码规范统一 |
| 单元测试 | 后端 JUnit 5 + 前端 Vitest |
| Sa-Token Redis 集成 | 生产环境多实例部署支持 |

## 常见问题与解决方案

### 路由无限循环

**问题**: `[Vue Router warn]: No match found for location with path "/xxx"` 无限循环

**原因**: 点击了没有 `component` 的父级菜单路径，路由守卫中 `next({ ...to, replace: true })` 死循环

**解决**:
1. 搜索结果只收集有 `component` 的叶子菜单
2. 路由守卫中使用 `router.resolve()` 验证路径
3. 添加冷却时间保护机制

### keep-alive 缓存失效

**问题**: 切换标签页时重复请求业务数据

**原因**: 动态路由 `name` 与组件 `defineOptions name` 不匹配

**解决**: `componentMap` 中 `name` 字段与组件 `defineOptions name` 完全一致

### 刷新后路由丢失

**问题**: 页面刷新后无法访问之前打开的页面

**原因**: `menus` 只存在 Pinia 内存中

**解决**: menus/roles/perms 持久化到 localStorage，Store 初始化时恢复

### 搜索面板样式错乱

**问题**: 菜单名称竖排显示

**原因**: `flex: 1` 在窄面板中导致文字换行

**解决**: `white-space: nowrap` + `max-width` + `text-overflow: ellipsis`

### 登录后"系统繁忙，请稍后再试"

**问题**: 全新部署后登录成功但所有功能报错

**原因**: `FavoritesPanel.vue` 查询 `sys_user_favorite` 表，表不存在触发全局异常

**解决**:
1. 执行 `db/features_init.sql` 创建表
2. 执行 `db/features_menu.sql` 插入菜单
3. 重启后端 + 清除浏览器缓存

### 左侧菜单看不到 v2.0 新增功能

**问题**: 代码部署完成但菜单栏缺少新功能入口

**原因**: `features_menu.sql` 未执行，`sys_menu` 表缺少记录

**解决**: `mysql -u root -p rx_admin < db/features_menu.sql` 后重启

### MapStruct 实现类未生成 (No implementation was created)

**问题**: 编译/启动时报 `No implementation was created for XxxConvert`

**原因**: DTO/VO 字段名与 Entity 不一致，或缺少 `@Mapper` 配置

**解决**: 对齐字段名 + 添加 `unmappedTargetPolicy = ReportingPolicy.IGNORE` + `@BeanMapping` 注解

### IDE 报红色波浪线但 mvn compile 正常

**问题**: IDE 中显示 `The import Xxx cannot be resolved`

**原因**: MapStruct 生成代码在 `target/generated-sources/`，IDE 未识别为源码目录

**解决**:
1. 确认 `pom.xml` 中的 `build-helper-maven-plugin` 配置
2. VS Code: 执行 `Java: Clean Java Language Server Workspace`
3. IntelliJ: `File → Invalidate Caches → Restart`

### Unmapped target properties 警告

**问题**: 编译时出现大量 `Unmapped target properties` 警告

**原因**: Convert 接口未设置 `unmappedTargetPolicy`

**解决**: 所有 Convert 接口添加 `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`