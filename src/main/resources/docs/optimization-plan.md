# RX Admin 系统优化与增强计划

> 基于 2026-07 全系统代码扫描，涵盖后端 Java + 前端 Vue3。

---

## P0 — 安全/稳定性

### 1. Entity 直接暴露（DTO/VO 缺失）

`SysNotice` 和 `SysMessage` 实体从 Controller 直接返回给前端，存在字段过度暴露风险。

| 文件 | 行 | 当前返回类型 |
|------|----|-------------|
| `SysNoticeController.java` | 35 | `Result<PageResult<SysNotice>>` |
| `SysNoticeController.java` | 90 | `Result<SysNotice>` |
| `SysMessageController.java` | 32 | `Result<PageResult<SysMessage>>` |

**方案**：创建 `SysNoticeVO` / `SysMessageVO`，对敏感字段加 `@JsonIgnore`（如 `internal`、`targetType` 等内部字段），Controller 中转换。

### 2. `@Valid` 缺失

`SysUserFavoriteController` 收 Entity 做入参，无校验注解，非法数据可直接入库。

| 文件 | 行 | 端点 |
|------|----|------|
| `SysUserFavoriteController.java` | 45 | `POST /` |
| `SysUserFavoriteController.java` | 56 | `POST /toggle` |

**方案**：入参改为 DTO + `@Valid`，或用 `@NotBlank`/`@NotNull` 标注实体字段。

### 3. Config 端点未加 `@SaCheckPermission`

系统核心配置（`/grouped`、`/values`、`/value/{key}`）无权限控制，可能泄露敏感配置项（如第三方 API Key）。

| 文件 | 行 | 端点 |
|------|----|------|
| `SysConfigController.java` | 38 | `GET /grouped` |
| `SysConfigController.java` | 47 | `POST /values` |
| `SysConfigController.java` | 53 | `GET /value/{key}` |

**方案**：补充 `@SaCheckPermission("system:config:query")`，或区分公共配置与私密配置。

### 4. `NotRoleException` 未处理 → 返回 500

Sa-Token `@SaCheckRole` 校验失败时，`GlobalExceptionHandler` 未显式捕获，落入通用 `Exception` 兜底返回 500。

| 文件 | 行 | 当前行为 |
|------|----|----------|
| `GlobalExceptionHandler.java` | 229-233 | 500 + "系统繁忙" |

**方案**：新增 `@ExceptionHandler(NotRoleException.class)` → 403 + "无角色权限"。

---

## P1 — 性能

### 5. N+1 查询 — 角色-菜单映射

`SysRoleService.getRoleList()` 中 for 循环逐条调用 `roleMenuMapper.selectMenuIdsByRoleId()`。

**方案**：改为批量查询 `selectMenuIdsByRoleIds(List<Long>)` + `Map<Long, List<Long>>` 归组。

### 6. N+1 查询 — `forEach` 逐条 insert/delete

| 文件 | 方法 | 模式 |
|------|------|------|
| `SysUserService.java:92` | `addUser()` | `forEach` → `userRoleMapper.insert()` |
| `SysUserService.java:150` | `updateUser()` | `forEach` → `userRoleMapper.insert()` |
| `SysUserService.java:210` | `deleteUserBatch()` | `forEach` → `userRoleMapper.deleteByUserId()` |
| `SysRoleService.java:63` | `addRole()` | `forEach` → `roleMenuMapper.insert()` |
| `SysRoleService.java:84` | `updateRole()` | `forEach` → `roleMenuMapper.insert()` |
| `SysRoleService.java:98` | `deleteRoleBatch()` | `forEach` → `roleMenuMapper.deleteByRoleId()` |
| `SysPermissionManageService.java:60-96` | `getUserMenuIds()` | for 循环查询 |
| `SysPermissionManageService.java:90-118` | `addUserMenus()`/`setUserMenus()` | for 循环 insert |
| `SysPermissionManageService.java:143-145` | `removeUserMenus()` | for 循环 delete |
| `SysNoticeService.java:163-171` | `markAllRead()` | for 循环 insert |
| `SysMessageService.java:195-198` | `sendToRoleUsers()` | for 循环 send |

**方案**：Mapper 新增批量 insert/delete 方法（`@Insert({...})` / `<script>foreach</script>`），Service 调用一次批量执行。

### 7. 缓存缺失 — 高频读取方法无 `@Cacheable`

| 文件 | 方法 | 缓存建议 |
|------|------|---------|
| `CalendarEventService.java:46` | `getEventsByMonth()` | `@Cacheable(value = "config", key = "'calendar:month:'+#year+':'+#month")` |
| `CalendarEventService.java:59` | `getEventsByRange()` | `@Cacheable(... key = "'calendar:range:'+#startDate+':'+#endDate")` |
| `CalendarEventService.java:70` | `getTodayEvents()` | `@Cacheable(... key = "'calendar:today:'+#userId")` |
| `CalendarEventService.java:82` | `getEventById()` | `@Cacheable(... key = "'calendar:event:'+#id")` |
| `SysNoticeService.java:41` | `pageQuery()` | 读多写少，可加短暂缓存 |
| `SysNoticeService.java:111` | `countByCategory()` | 可加短暂缓存 |
| `SysDictTypeService.java:18` | `pageQuery()` | 可加 10min TTL 缓存 |
| `SysIpRuleService.java:19` | `pageQuery()` | 可加缓存 |
| `SysUserService.java:50` | `pageQuery()` | 读多写少，加 5min 缓存 |

### 8. 批量删除缺失（12 个模块）

已有批量删除的模块：`SysUser`、`SysRole`、`SysLog`、`SysSlowQuery`、`SysLoginLog`、`SysJobLog`、`SysNotifyRecord`、`VideoTranscription`、`AudioTranscription`、`TechBlog`、四大名著。

**缺失批量删除的模块**：

| 编号 | 模块 | Controller | 已有单个删除 |
|------|------|------------|-------------|
| B1 | `SysMenu` | `SysMenuController.java:60` | ✅ |
| B2 | `SysConfig` | `SysConfigController.java:88` | ✅ |
| B3 | `SysDictType` | `SysDictTypeController.java:70` | ✅ |
| B4 | `SysDictData` | `SysDictDataController.java:63` | ✅ |
| B5 | `SysDept` | `SysDeptController.java:57` | ✅ |
| B6 | `SysIpRule` | `SysIpRuleController.java:73` | ✅ |
| B7 | `SysNotice` | `SysNoticeController.java:80` | ✅ |
| B8 | `SysMessage` | `SysMessageController.java:68` | ✅ |
| B9 | `SysUserFavorite` | `SysUserFavoriteController.java:70` | ✅ |
| B10 | `CalendarEvent` | `CalendarEventController.java:95` | ✅ |
| B11 | `SysJob` | `SysJobController.java:63` | ✅ |
| B12 | `Announcement` | `AnnouncementController.java` | ❌ 无删除 |

---

## P2 — 工程化/代码质量

### 9. `@Transactional` 缺失

17 处多步写操作缺少事务保障，异常时可能导致数据不一致。

| 文件 | 行 | 方法 |
|------|----|------|
| `SysNoticeService.java` | 57 | `addNotice()` — save + sendToAll + SSE + 事件发布 |
| `SysNoticeService.java` | 93 | `updateNotice()` — 查询 + 更新 |
| `SysDictTypeService.java` | 33 | `addDictType()` — count 检查 + save |
| `SysDictTypeService.java` | 50 | `updateDictType()` — 查询 + 更新 |
| `SysDictDataService.java` | 33 | `addDictData()` — count 检查 + save |
| `SysDictDataService.java` | 53 | `updateDictData()` — 查询 + 更新 |
| `SysDictDataService.java` | 69 | `deleteDictData()` |
| `SysConfigService.java` | 72 | `addConfig()` — count 检查 + save |
| `SysConfigService.java` | 90 | `updateConfig()` — 查询 + 更新 |
| `SysDeptService.java` | 33 | `addDept()` |
| `SysDeptService.java` | 46 | `updateDept()` — 查询 + 更新 |
| `SysDeptService.java` | 72 | `deleteDept()` — count 检查 + 删除 |
| `SysIpRuleService.java` | 33 | `addIpRule()` |
| `SysIpRuleService.java` | 51 | `updateIpRule()` |
| `SysMessageService.java` | 129 | `sendMessage()` — save + SSE |
| `SysMessageService.java` | 160 | `sendToAll()` — 批量 save |
| `SysMessageService.java` | 189 | `sendToRoleUsers()` — 循环发送 |

### 10. `@OperateLog` 缺失

11 处写端点缺少操作日志。

| 文件 | 行 | 端点 |
|------|----|------|
| `SysNoticeController.java` | 101 | `POST /read/{id}` |
| `SysNoticeController.java` | 108 | `POST /read-all` |
| `TechBlogController.java` | 92 | `POST /articles` |
| `TechBlogController.java` | 103 | `PUT /articles/{id}` |
| `TechBlogController.java` | 116 | `DELETE /articles/{id}` |
| `TechBlogController.java` | 127 | `DELETE /articles/batch` |
| `VideoPlayerController.java` | 69 | `POST /record` |
| `NotifyCenterController.java` | 111 | `DELETE /records/{id}` |
| `SysPermissionManageController.java` | 46 | `POST /user/{userId}/add` |
| `SysPermissionManageController.java` | 58 | `POST /user/{userId}/remove` |
| `SysPermissionManageController.java` | 70 | `POST /user/{userId}/set` |

### 11. `v-for` 缺少 `:key`（28 处）

Vue 3 中 `v-for` 缺少 `:key` 可能导致列表渲染异常。涉及组件：`CommandPalette`、`FavoritesPanel`、`ExportButton`、`SearchBox`、`TagsView`，以及多个视图页面（as400、calendar、video、tool 等）。

**方案**：统一补充 `:key`，优先使用唯一 ID（`item.id`），静态列表使用 `index`。

### 12. 硬编码权限字符串（25+ 处）

Controller 中 `@SaCheckPermission("monitor:log:delete")` 等权限字符串直接硬编码，未引用 `PermissionConstants`。

**方案**：统一替换为 `PermissionConstants.Monitor.LOG_DELETE`（如缺失则先在常量类中新增）。

### 13. 硬编码中文（100+ 处）

模板、`ElMessage`、`ElMessageBox` 中直接写中文，未使用 `$t()`。

**方案**：提取中文到 i18n locale JSON 文件，模板中用 `$t('key')`，JS 中用 `$t('key')` 或 i18n 全局函数。

### 14. `console.log` 残留

| 文件 | 行 | 内容 |
|------|----|------|
| `main.js` | 80 | `console.log('📊 性能监控已启动')` |
| `globalErrorHandler.js` | 38 | `console.log('✅ 全局错误处理器已启动')` |
| `globalErrorHandler.js` | 397 | `console.log('🛑 全局错误处理器已停止')` |
| `sentry.js` | 53 | `console.log('✅ Sentry 错误监控已启动')` |

**方案**：替换为环境判断 + 可配置调试模式，或使用 `console.debug`。

### 15. `useTablePage` 未复用

已有 `useTablePage` composable 提供 loading/initialLoading/pagination/refresh 等能力，但多数 table 页面未使用，自行管理 loading 状态。

**方案**：逐步将各 table 页面迁移到 `useTablePage` 统一管理。

---

## P3 — 优化增强

### 16. Dashboard 重复拉取

`onMounted` 中调用 `fetchStats()` + `fetchTodayCalendar()` 等，同时 SSE 连接后又触发 `renderAllCharts()` 重新拉取相同数据，导致首页加载时数据被反复请求。

**方案**：SSE 回调中只更新增量数据，初始数据通过 mount 一次性拉取后共享给 SSE 回调；或去重请求。

### 17. 前端 API 无 error handling

`content/message/index.vue:116-117` 中 `markAsReadApi` / `markAllReadApi` 无 try-catch 包裹。

**方案**：补充 try-catch，操作失败时 `ElMessage.error` 提示。

### 18. 四名著 CRUD 重复

`XiyouController` / `SanguoController` / `ShuihuController` / `HonglouController` 结构几乎一致。修改逻辑需改 4 份。

**方案**：抽取公共 `BaseLiteratureController`，各名著继承复用；或抽 Service 层逻辑到共用类。

### 19. 顶部菜单 ID 硬编码

`SysPermissionManageService.java:176` 中 `Set.of(1L, 24L, 30L, 36L)` 硬编码排除的顶部菜单 ID。

**方案**：改为从 `sys_menu` 查询 `parent_id IS NULL` 动态获取，或配置在 `application.yml` 中。

### 20. Dashboard 计算属性爆炸

`dashboard/index.vue` 中 11 个 `computed` 均依赖 `statsData` 对象，每次 SSE 推送全量更新时全部重新计算。

**方案**：拆分 `statsData` 为独立 `ref`（`userCount`、`roleCount` 等），或使用 `computed` 精确依赖子字段。

---

## 实施批次建议

### 第一批：安全与工程化加固（6 项）
1. **P0-1**: `SysNotice`/`SysMessage` Entity 暴露修复
2. **P0-2**: `@Valid` 补充
3. **P0-4**: `NotRoleException` 全局异常处理
4. **P1-6**: 批量删除补充（12 个模块）
5. **P2-9**: `@Transactional` 补充（17 处）
6. **P2-10**: `@OperateLog` 补充（11 处）

### 第二批：性能与缓存（4 项）
7. **P1-5**: N+1 角色-菜单批量查询
8. **P1-6**: forEach 批量 insert/delete 改造
9. **P1-7**: Calendar/Notice/Dict 缓存补充
10. **P3-16**: Dashboard 重复拉取优化

### 第三批：代码质量与前端（5 项）
11. **P2-11**: `v-for` `:key` 补充（28 处）
12. **P2-12**: 权限字符串硬编码替换（25+ 处）
13. **P2-13**: 中文硬编码 i18n 提取
14. **P2-14**: `console.log` 清理
15. **P2-15**: `useTablePage` 推广

### 第四批：架构优化（3 项）
16. **P0-3**: Config 端点权限控制
17. **P3-18**: 四名著 CRUD 抽取公共基类
18. **P3-19**: 顶部菜单 ID 动态获取
