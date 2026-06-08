# AGENT.MD — RX Admin AI 开发参考手册

> **项目**: RX Admin 通用管理系统 | **版本**: 1.0 | **生成日期**: 2026-06-05
>
> 本文档整合了 [rxadmin.md](./src/main/resources/docs/rxadmin.md)、[rxadmin-dev-skills.md](./src/main/resources/docs/rxadmin-dev-skills.md)、[rxadmin-optimization.md](./src/main/resources/docs/rxadmin-optimization.md) 三份文档的核心规则，供 AI 辅助开发时作为第一参考。

---

## 1. 项目身份

```
项目坐标: com.rx:rx-admin:1.0.0
名称:      RX Admin 通用管理系统
类型:      Spring Boot 3 + Vue 3 前后端分离后台管理系统
数据库:    MySQL 8.0 (主库 rx_admin) + (次库 as400, classics)
认证:      Sa-Token（内存模式，无 Redis）
```

**核心设计原则**:
- 前后端完全分离，后端只返回 JSON，无 JSP/Thymeleaf
- 路由完全由后端 `sys_menu` 表驱动，前端通过 `componentMap` 动态注入
- **Redis 已明确排除**，所有缓存使用 Caffeine 本地缓存
- **禁止创建 MyBatis XML 映射文件**，复杂 SQL 用注解

---

## 2. 技术栈（强制）

### 2.1 必须使用

| 层级 | 技术 | 版本 |
|------|------|------|
| Java | JDK | 17+ |
| 后端框架 | Spring Boot | 3.2.x (Jakarta EE) |
| ORM | MyBatis Plus | 3.5.x |
| 安全认证 | Sa-Token | 1.37+ |
| API 文档 | Knife4j (OpenAPI 3) | 4.4+ |
| 前端框架 | Vue 3 Composition API `<script setup>` | ^3.4.0 |
| 构建工具 | Vite | ^5.0 |
| 路由 | Vue Router | ^4.2 |
| 状态管理 | Pinia (Composition API 风格) | ^2.1 |
| UI 组件库 | Element Plus | ^2.4 |
| CSS | SCSS (Dart Sass) | ^1.69 |
| 国际化 | Vue I18n (legacy: false) | ^9.14 |
| 限流 | Guava RateLimiter | 33.0+ |
| 本地缓存 | Caffeine + Spring Cache | 3.x (Boot 内嵌) |

### 2.2 严禁使用

- ❌ Fastjson / Fastjson2（用 Jackson）
- ❌ Spring Security / Shiro（用 Sa-Token）
- ❌ JSP / Thymeleaf
- ❌ Vue 2 / Options API / Vuex（用 Vue 3 + Pinia）
- ❌ Webpack（用 Vite）
- ❌ MyBatis XML 映射文件（用注解）
- ❌ Redis（项目决策不使用）

---

## 3. 项目结构

```
src/main/java/com/rx/admin/
├── RxAdminApplication.java           # 启动类（根包）
├── common/                            # BaseEntity, Result, PageResult, GlobalExceptionHandler, OperateLog
├── config/                            # CorsConfig, SaTokenConfig, StpInterfaceImpl, MybatisPlusConfig, RateLimiterConfig, AsyncConfig, DataSourceConfig
├── entity/                            # 实体类（继承 BaseEntity）
│   └── {module}/                      # 子模块实体（classics/, as400/）
├── controller/                        # @RestController
│   └── {module}/
├── service/                           # IService → ServiceImpl
│   ├── impl/
│   └── {module}/
└── mapper/                            # BaseMapper
    └── {module}/

ui/
├── .env.development                   # 开发环境变量（VITE_* 前缀）
├── .env.production                    # 生产环境变量（VITE_* 前缀）
├── index.html
├── vite.config.js
├── src/
│   ├── main.js                        # 入口：ElementPlus + Router + Pinia + i18n
│   ├── api/                           # 每个模块一个文件（auth.js, user.js, ...）
│   ├── composables/                   # useStorage, useTablePage, useTheme, useMenuI18n, usePasswordStrength, useTableHeight
│   ├── i18n/lang/                     # zh-CN.js, en-US.js
│   ├── layout/                        # index.vue, SearchBox.vue, NoticePopover.vue, SubMenu.vue, TagsView.vue
│   ├── router/
│   │   ├── index.js                   # constantRoutes + 动态路由注册
│   │   └── componentMap.js           # key=(views/下路径) → { component, name }
│   ├── stores/                        # user.js, tags.js
│   ├── styles/                        # variables.scss, global.scss
│   ├── utils/
│   │   ├── request.js                 # Axios 封装
│   │   └── sanitize.js                # DOMPurify HTML 净化
│   └── views/                         # 页面视图
│       ├── login/
│       ├── dashboard/
│       ├── system/                    # user, role, menu, dept, config, file
│       ├── monitor/                   # online, log, job, slow-query
│       ├── tool/                      # dict, region, analysis, docs, standards, excelParser, docConverter, docUpload, emailSender, flowChart, musicPlayer
│       ├── content/                   # notice
│       ├── as400/                     # objects, iservice, techblog
│       ├── classics/                  # honglou, sanguo, shuihu, xiyou, literature
│       └── permission/                # request
```

### 3.1 环境变量配置（`.env.*` 文件）

前端通过 `.env.development` / `.env.production` 管理运行时配置，仅 `VITE_` 前缀变量可被客户端代码访问。

#### 轮询/心跳

| 变量 | 开发环境 | 生产环境 | 说明 |
|------|----------|----------|------|
| `VITE_HEARTBEAT_INTERVAL` | 10000 | 30000 | 会话心跳间隔（ms） |
| `VITE_HEARTBEAT_TIMEOUT` | 15000 | 8000 | 心跳请求超时（ms） |
| `VITE_NOTICE_POLL_INTERVAL` | 60000 | 30000 | 通知轮询间隔（ms） |
| `VITE_FETCH_PROGRESS_POLL_INTERVAL` | 10000 | 2000 | 抓取进度轮询间隔（ms） |
| `VITE_KICKOUT_COUNTDOWN` | 5 | 5 | 被踢下线倒计时（秒） |

#### 请求超时

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `VITE_API_REQUEST_TIMEOUT` | 15000 | 通用 API 请求超时（ms） |
| `VITE_AS400_REQUEST_TIMEOUT` | 60000 | AS400 远程连接请求超时（ms） |

#### 表格配置

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `VITE_TABLE_ROW_HEIGHT` | 48 | 表格行高（px），useTablePage 高度计算用 |
| `VITE_TABLE_PADDING` | 120 | 表格底部预留空间（px） |
| `VITE_DEFAULT_PAGE_SIZE` | 10 | 默认每页条数 |
| `VITE_PAGE_SIZE_OPTIONS` | 10,20,50,100 | 可选每页条数（逗号分隔） |
| `VITE_TABLE_MAX_HEIGHT` | 400 | 默认表格最大高度（px） |
| `VITE_CLASSICS_TABLE_ROW_HEIGHT` | 44 | classics 页面表格行高（px） |
| `VITE_CLASSICS_TABLE_HEADER_HEIGHT` | 40 | classics 页面表头高度（px） |
| `VITE_CLASSICS_TABLE_MAX_ROWS` | 16 | classics 页面默认最大可见行数 |

#### 业务延迟

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `VITE_MUSIC_SEARCH_DEBOUNCE_MS` | 300 | 音乐播放器搜索防抖延迟（ms） |
| `VITE_FLOWCHART_INIT_RETRY_MS` | 50 | 流程图 DOM 就绪轮询间隔（ms） |

```javascript
// 使用方式：import.meta.env.VITE_xxx，带 fallback 默认值
const interval = Number(import.meta.env.VITE_NOTICE_POLL_INTERVAL) || 15000
```

**注意**: 修改 `.env.*` 后需重启 Vite 开发服务器。

### 3.2 后端配置（`application.yml`）

后端通过 `application.yml` 管理可配置参数，使用 `@Value` 注入（带 `${key:default}` 默认值）。

#### 安全配置

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `app.captcha.expire-ms` | 300000 | 验证码过期时间（ms） |
| `app.captcha.cleanup-interval-ms` | 60000 | 过期验证码清理间隔（ms） |
| `app.replay.time-window-ms` | 300000 | 防重放时间窗口（ms） |
| `app.replay.max-nonce-cache` | 10000 | nonce 缓存最大条目数 |

#### 爬虫配置

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `app.techblog.request-delay-ms` | 1000 | 页间请求延迟（ms） |
| `app.techblog.page-timeout-ms` | 15000 | 列表页抓取超时（ms） |
| `app.techblog.article-timeout-ms` | 30000 | 文章详情抓取超时（ms） |

#### 邮件配置

> 配置位于 `application.yml` 的 `app.mail.*` 节点，通过环境变量注入，`application-local.yml` 提供本地默认值。

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `app.mail.host` | smtp.163.com | SMTP 服务器地址（环境变量 `MAIL_HOST`） |
| `app.mail.port` | 465 | SMTP 端口，465=SSL（环境变量 `MAIL_PORT`） |
| `app.mail.username` | - | SMTP 认证用户名（环境变量 `MAIL_USERNAME`） |
| `app.mail.password` | - | SMTP 认证密码/授权码（环境变量 `MAIL_PASSWORD`） |
| `app.mail.from` | xubingzhen83@163.com | 发件人地址（环境变量 `MAIL_FROM`） |
| `app.mail.properties.mail.smtp.auth` | true | 启用 SMTP 认证（环境变量 `MAIL_SMTP_AUTH`） |
| `app.mail.properties.mail.smtp.starttls.enable` | false | STARTTLS（163 用 SSL 关闭此选项） |
| `app.mail.properties.mail.smtp.ssl.enable` | true | 启用 SSL（163 端口 465） |

```powershell
# 本地开发：激活 local profile 自动加载 application-local.yml 中的邮件配置
$env:SPRING_PROFILES_ACTIVE="local"

# 或手动设置环境变量
$env:MAIL_HOST="smtp.163.com"; $env:MAIL_PORT="465"; $env:MAIL_USERNAME="xubingzhen83@163.com"; $env:MAIL_PASSWORD="授权码"; $env:MAIL_FROM="xubingzhen83@163.com"; $env:MAIL_SMTP_AUTH="true"; $env:MAIL_SMTP_STARTTLS="false"; $env:MAIL_SMTP_SSL="true"
```

#### 已有配置（仅列出数值型）

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `app.slow-query.threshold-ms` | 2000 | 慢查询阈值（ms） |
| `sa-token.timeout` | 86400 | Token 过期时间（秒）= 24h |
| `sa-token.active-timeout` | 1800 | Token 活跃超时（秒）= 30min |
| `spring.servlet.multipart.max-file-size` | 50MB | 文件上传最大大小 |
| `server.tomcat.max-swallow-size` | 100MB | Tomcat 最大接收大小 |

---


## 4. 后端开发规范

### 4.1 实体类模板

```java
@Data
@EqualsAndHashCode(callSuper = true)    // ⚠️ 必须
@TableName("sys_xxx")                  // 显式表名
public class SysXxx extends BaseEntity { // ⚠️ 必须继承 BaseEntity

    @NotBlank(message = "名称不能为空")   // ⚠️ 加校验注解
    private String name;

    private Integer status;             // 1=启用, 0=禁用
}
```

**强制规则**:
- 继承 `BaseEntity`（自动获得 id, deleted, createTime, updateTime）
- 加 `@EqualsAndHashCode(callSuper = true)`
- 加 Bean Validation 注解（`@NotBlank`, `@Email` 等）
- 使用 `LocalDateTime`，禁止 `Date`
- 字段驼峰 → 数据库下划线自动映射

### 4.2 Mapper 模板

```java
@Mapper
public interface SysXxxMapper extends BaseMapper<SysXxx> {
    @Select("SELECT * FROM sys_xxx WHERE ...")
    List<SysXxx> customQuery(@Param("id") Long id);
}
```

- 必须 `@Mapper`
- 继承 `BaseMapper<Entity>`
- 复杂 SQL 用 `@Select/@Update/@Delete`，**禁止 XML 文件**

### 4.3 Service 模板

```java
// 接口
public interface SysXxxService extends IService<SysXxx> {
    PageResult<SysXxx> pageQuery(int page, int size, String keyword);
    void addXxx(SysXxx entity);
    void updateXxx(SysXxx entity);
}

// 实现
@Service
@RequiredArgsConstructor
public class SysXxxServiceImpl extends ServiceImpl<SysXxxMapper, SysXxx> implements SysXxxService {

    private final SysXxxMapper mapper;  // ⚠️ 构造器注入（@RequiredArgsConstructor）

    @Override
    @Transactional                      // ⚠️ 写操作加事务
    public void addXxx(SysXxx entity) {
        baseMapper.insert(entity);
    }

    @Override
    public PageResult<SysXxx> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<SysXxx> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysXxx::getName, keyword);
        }
        wrapper.orderByDesc(SysXxx::getCreateTime);
        Page<SysXxx> result = page(new Page<>(page, size), wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }
}
```

**强制规则**:
- 继承 `ServiceImpl<Mapper, Entity>` + 实现 `IService<Entity>`
- 使用 **构造器注入**（`@RequiredArgsConstructor`），禁止 `@Autowired`
- 分页返回 `PageResult<T>`，不用 MyBatis Plus 原生 `Page`
- 条件查询用 `LambdaQueryWrapper`（类型安全）
- 写操作加 `@Transactional`

### 4.4 Controller 模板

```java
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "用户分页")
    @GetMapping("/page")
    @SaCheckPermission("sys:user:query")   // ⚠️ 权限注解
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @SaCheckPermission("sys:user:add")
    @OperateLog(module = "用户管理", operation = "新增用户")  // ⚠️ 操作日志
    public Result<?> add(@RequestBody @Valid SysUser user) {  // ⚠️ @Valid
        userService.addUser(user);
        return Result.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/{ids}")
    @SaCheckPermission("sys:user:delete")
    @OperateLog(module = "用户管理", operation = "删除用户")
    public Result<?> delete(@PathVariable List<Long> ids) {
        userService.removeByIds(ids);
        return Result.success();
    }
}
```

**强制规则**:
- URL 前缀: `/api/{模块}/{实体}`
- RESTful: `GET` 查 / `POST` 增 / `PUT` 改 / `DELETE` 删
- `@RequiredArgsConstructor` 构造器注入
- `@RequestBody` 参数加 `@Valid`
- 每个接口加 `@SaCheckPermission`
- 写操作加 `@OperateLog`

### 4.5 安全认证

**权限码格式**: `{模块}:{实体}:{操作}`

| 操作 | 权限码 | 示例 |
|------|--------|------|
| 查询 | `module:entity:query` | `sys:user:query` |
| 新增 | `module:entity:add` | `sys:user:add` |
| 修改 | `module:entity:edit` | `sys:user:edit` |
| 删除 | `module:entity:delete` | `sys:user:delete` |

**Sa-Token 配置**: Token 7 天有效期、随机 UUID、允许并发登录、**内存模式**。

**安全机制**（全部已实现）:
- 登录限流: Guava RateLimiter 3次/秒/IP
- 失败锁定: 5 次失败锁定 30 分钟（LoginAttemptService）
- 操作日志: AOP 异步 + 敏感字段脱敏（password, token, secret）
- API 防重放: X-Timestamp + X-Nonce（ReplayAttackFilter）
- XSS 防护: Jackson 反序列化 HTML 转义（XssJacksonConfig）
- 验证码: BufferedImage + Graphics2D，开发环境固定 dev000
- IP 黑白名单: OncePerRequestFilter + yaml 配置（IpFilter）
- 踢出机制: StpUtil.kickout + 前端遮罩 + 5 秒倒计时

### 4.6 统一响应格式

```java
// Result<T>
Result.success(data)   // code=200
Result.fail("msg")     // code=非200

// PageResult<T>
PageResult.of(total, page, pageSize, records)
```

**全局异常处理**（10 种异常）: 401 未登录、403 无权限、400 参数校验、404 不存在、405 方法不允许、415 媒体类型、数据约束冲突、兜底异常。

---

## 5. 前端开发规范

### 5.1 页面组件模板

```vue
<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="关键词" clearable style="width:200px" />
      <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
      <el-button type="primary" @click="handleAdd">{{ $t('common.add') }}</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next,jumper" @change="fetchData" />
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <!-- 表单项 -->
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { xxxApi } from '@/api/xxx'
import { useTablePage } from '@/composables/useTablePage'

defineOptions({ name: 'ModulePage' })  // ⚠️ 必须声明，与 componentMap 一致

const { loading, keyword, page, size, total, tableData, fetchData, handleSearch } = useTablePage(xxxApi)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({})

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
// 使用 CSS 变量，不写硬编码颜色
</style>
```

**强制规则**:
1. ⚠️ **必须声明 `defineOptions({ name: 'PascalName' })`**，与 `componentMap.js` 一致
2. 用 `<script setup>` 语法
3. 用 `useTablePage` composable 管理表格（推荐）
4. 用 `useStorage` 管理 localStorage（禁止直接调用原生 API）
5. API 调用放 `try/catch/finally`，finally 关闭 loading
6. 删除操作必须有 `ElMessageBox.confirm` 二次确认
7. 弹窗关闭时 `@close` 回调重置表单
8. 表格操作列 `fixed="right"`
9. 页面结构: `.page-container` > `.search-bar` + `.table-container` + `.page-pagination`

### 5.2 路由规范

**核心**: `constantRoutes` 只保留 Login 和 Layout，所有业务路由由后端 `sys_menu` 驱动。

```javascript
// constantRoutes
const constantRoutes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue') },
  { path: '/', name: 'Layout', component: Layout, children: [] }
]
```

**componentMap 映射规则**:
```javascript
// key = views/ 下路径（与 sys_menu.component 完全一致）
// value = { component: () => import(...), name: 'PascalName' }
'system/user/index': { component: () => import('@/views/system/user/index.vue'), name: 'SystemUser' }
```

**新增页面步骤**（无需改 router/index.js）:
1. 创建 `views/xxx/index.vue`（声明 `defineOptions name`）
2. 在 `componentMap.js` 追加 1 行映射
3. 创建 `api/xxx.js`
4. 在 `sys_menu` 表插入菜单记录

### 5.3 Store 规范

```javascript
// stores/user.js
export const useUserStore = defineStore('user', () => {
  // ⚠️ 使用 useStorage 管理所有持久化
  const tokenStorage = useStorage(STORAGE_KEYS.TOKEN)
  const token = ref(tokenStorage.get() || '')

  // ⚠️ login() 中预加载 fetchUserInfo() + fetchRouters()
  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    tokenStorage.set(token.value)
    await fetchUserInfo()
    await fetchRouters()
  }

  // ⚠️ logout() 清理所有状态和 localStorage
  function logout() {
    token.value = ''
    // 清除所有 STORAGE_KEYS
  }
})
```

### 5.4 useStorage 规范

```javascript
// ⚠️ Token 存储自动 Base64+XOR 混淆
const TOKEN_XOR_KEY = 0xA3
function encodeToken(raw) { /* Base64 + XOR */ }
function decodeToken(encoded) { /* XOR + Base64 decode */ }

// 使用
const token = useStorage(STORAGE_KEYS.TOKEN).get()
```

**命名空间**: 所有 key 使用 `rx_admin_` 前缀 | 通过 `STORAGE_KEYS` 常量引用 | 自动 JSON 序列化

### 5.5 Axios 封装

- `baseURL: '/api'`（配合 Vite 代理）
- Token 放 `Authorization` 请求头
- 统一拦截 `code !== 200` 错误并 `ElMessage.error`
- NProgress 进度条（请求拦截器检查 `config._skipNProgress`，为 `true` 时跳过）
- 心跳: 每 10 秒 GET `/api/auth/ping`（`_skipNProgress: true`）
- 防重放: 写操作自动加 `X-Timestamp` + `X-Nonce`

**⚠️ 后台定时轮询必须跳过进度条**:
```javascript
// API 定义时预留 options 参数
export function getXxxApi(params, options = {}) {
  return request({ url: '/xxx', method: 'get', params, ...options })
}

// 定时轮询调用时传入 _skipNProgress: true
getXxxApi(params, { _skipNProgress: true })
```
否则每次轮询都会触发页面顶部进度条闪烁，用户体验很差。

### 5.6 API 模块命名

```javascript
// 每个模块一个文件（api/xxx.js）
export function getXxxPage(params)  // 分页
export function getXxxById(id)      // 详情
export function addXxx(data)        // 新增
export function updateXxx(data)     // 修改
export function deleteXxx(ids)      // 批量删除
```

### 5.7 v-html 安全规范

**所有 `v-html` 必须经过 DOMPurify 净化**:

```javascript
import { sanitizeHtml } from '@/utils/sanitize'
// 模板中: v-html="sanitizeHtml(content)"
```

**已接入 DOMPurify 的页面**: notice, docs, standards, techblog/detail, iservice, literature/works

---

## 6. CSS / 样式规范

### 6.1 主题系统

CSS 变量双主题方案（`html.dark` 切换）:

```scss
:root {
  --bg-page: #f0f2f5;
  --bg-container: #fff;
  --text-primary: #303133;
  --color-primary: #409eff;
  --border-color: #e4e7ed;
}
html.dark {
  --bg-page: #141414;
  --text-primary: #e5eaf3;
  color-scheme: dark;
}
```

### 6.2 变量命名规范

格式: `--{类别}-{属性}`（禁止 `--color-xxx-color` 冗余命名）

| 类别 | 示例 |
|------|------|
| `bg` | `--bg-page`, `--bg-container` |
| `text` | `--text-primary`, `--text-secondary` |
| `color` | `--color-primary`, `--color-success` |
| `border` | `--border-color` |
| `sidebar` | `--sidebar-bg`, `--sidebar-text` |

### 6.3 强制规则

- ❌ **禁止硬编码颜色值**（必须用 CSS 变量）
- ❌ **禁止 `--text-color-secondary` 等 Element Plus 风格命名**
- ✅ 通用样式类: `.page-container`, `.search-bar`, `.table-container`, `.page-pagination`
- ✅ 亮/暗双主题均需测试通过

---

## 7. API 接口规范

### 7.1 URL 设计

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/api/{module}/{entity}/page?page=&size=&keyword=` | 分页 |
| GET | `/api/{module}/{entity}/{id}` | 详情 |
| POST | `/api/{module}/{entity}` | 新增 |
| PUT | `/api/{module}/{entity}` | 修改 |
| DELETE | `/api/{module}/{entity}/{ids}` | 批量删除 |

### 7.2 响应格式

```json
// 成功
{ "code": 200, "msg": "操作成功", "data": { ... } }
// 分页
{ "code": 200, "msg": "操作成功", "data": { "list": [...], "total": 100, "page": 1, "pageSize": 10 } }
// 错误
{ "code": 500, "msg": "系统异常", "data": null }
```

---

## 8. 国际化规范

- Vue I18n `legacy: false`（Composition API 模式）
- 模板用 `{{ $t('key.path') }}`，脚本用 `const { t } = useI18n()`
- 语言切换: `i18n.global.locale.value = newLocale`（**禁止 `window.location.reload()`**）
- Element Plus 语言用 `el-config-provider :locale` 动态切换
- 用 `useStorage(STORAGE_KEYS.LOCALE)` 持久化

---

## 9. 数据库规范

### 表命名
- 系统表: `sys_` 前缀（`sys_user`, `sys_role`, `sys_menu`）
- 逻辑删除: 统一 `deleted` 字段 + `@TableLogic`
- 状态字段: `Integer` 类型，1=启用，0=禁用

### sys_menu 字段规范

| 字段 | 规范 | 示例 |
|------|------|------|
| path | 前端路由路径 | `/system/user` |
| component | views/ 下路径（与 componentMap key 一致） | `system/user/index` |
| menuType | 1=目录, 2=菜单, 3=按钮 | — |
| icon | Element Plus 图标名 | `UserFilled` |
| permission | 权限码 | `sys:user:query` |

---

## 10. 关键约束与注意事项

### 10.1 架构级决策（必须遵守）

| # | 决策 | 说明 |
|---|------|------|
| 1 | **不用 Redis** | Caffeine 本地缓存替代，重启数据丢失可接受 |
| 2 | **Sa-Token 内存模式** | 重启后需重新登录 |
| 3 | **禁止 MyBatis XML** | SQL 只用注解 |
| 4 | **禁止 @Autowired** | 用 @RequiredArgsConstructor 构造器注入 |
| 5 | **禁止 console.log** | 生产代码无调试日志 |
| 6 | **禁止硬编码颜色** | CSS 变量 |
| 7 | **禁止硬编码中文** | 用 `$t()` i18n |
| 8 | **禁止 localStorage 直接调用** | 用 useStorage |
| 9 | **必须声明 defineOptions name** | keep-alive 缓存依赖 |
| 10 | **v-html 必须 DOMPurify** | XSS 防护 |
| 11 | **后台轮询必须 `_skipNProgress: true`** | 避免进度条频繁闪烁 |
| 12 | **禁止硬编码魔法数字** | 超时/间隔/尺寸等一律 `.env` 或 `application.yml` 可配置 |

### 10.2 开发检查清单

新增模块时确保:

- [ ] 实体继承 `BaseEntity` + `@EqualsAndHashCode(callSuper = true)`
- [ ] 实体字段加 Bean Validation（`@NotBlank` 等）
- [ ] Controller 加 `@SaCheckPermission`
- [ ] Controller `@RequestBody` 加 `@Valid`
- [ ] 写操作加 `@Transactional` + `@OperateLog`
- [ ] 用 `@RequiredArgsConstructor`（禁止 `@Autowired`）
- [ ] 分页返回 `PageResult`（非 MP `Page`）
- [ ] 前端 `defineOptions({ name })` 与 componentMap 一致
- [ ] 前端 componentMap 追加映射
- [ ] 前端样式用 CSS 变量
- [ ] 前端 API 调用 try/catch/finally
- [ ] 前端删除有二次确认
- [ ] 前端弹窗关闭重置表单
- [ ] 前端用 useStorage 管理持久化
- [ ] v-html 使用 sanitizeHtml()
- [ ] `sys_menu` 表插入菜单记录
- [ ] 亮/暗主题均正常
- [ ] 后台轮询/定时器 API 调用传入 `_skipNProgress: true`
- [ ] 超时/间隔/尺寸等参数从 `.env` 或 `application.yml` 读取（禁止硬编码魔法数字）
- [ ] 重复的常量/工具函数抽取为共享 composable（如 useTableHeight）

### 10.3 启动前检查清单（全新部署必读）

全新部署或迁移数据库时必须检查以下项，否则会导致"系统繁忙，请稍后再试"等故障：

| # | 检查项 | 说明 |
|---|--------|------|
| 1 | **执行 `db/features_init.sql`** | 在目标 MySQL 库手动执行，创建 `sys_ip_rule`、`sys_message`、`sys_user_favorite` 三张表。该脚本不会自动执行！ |
| 2 | **执行 `db/features_menu.sql`** | 插入 8 个 v2.0 菜单及按钮权限记录 |
| 3 | **重启后端服务** | 新表创建后 MyBatis Plus 实体映射才能生效 |
| 4 | **清除浏览器 localStorage** | `rx_admin_menus` key 缓存旧的空菜单列表，需重新登录触发菜单拉取 |

> **常见故障1**: 如果登录后页面显示"系统繁忙，请稍后再试"，通常是 `sys_user_favorite` 表不存在导致。`FavoritesPanel.vue` 组件挂载时自动调用 `/api/system/favorite/list`，查询不存在的表触发 SQL 异常。执行 `features_init.sql` + 重启后端即可解决。详细排查步骤见 [rxadmin-optimization.md §7.12](./src/main/resources/docs/rxadmin-optimization.md#712-常见故障排查)。
>
> **常见故障2**: 如果登录后左侧菜单看不到"IP黑白名单""消息中心""代码生成器"等 v2.0 功能入口，说明 `features_menu.sql` 未执行，`sys_menu` 表中缺少对应菜单记录。执行 `features_menu.sql` + 重启后端 + 清除 localStorage 后重新登录即可。详见 [rxadmin-optimization.md §7.9](./src/main/resources/docs/rxadmin-optimization.md#79-v20-功能介绍以上所有功能均已就绪详见下方列表)。

---

## 11. 当前实现状态

### 已完成（全部安全加固 + 核心优化 + v2.0 新功能）
- 验证码, XSS防护, API防重放, 密码策略, 敏感数据脱敏
- IP黑白名单, v-html DOMPurify, Controller输入校验, 音乐流鉴权
- Token混淆存储, 登录默认凭据DEV判断
- Dashboard N+1优化, SSE线程池修复
- 慢查询监控, 虚拟滚动
- 在线用户管理, 踢出机制, 心跳, 登录限流, 失败锁定
- 操作日志审计, 通知公告增强, 定时任务管理, 文件管理
- antvX6 Bug修复
- 办公工具菜单重构（常用工具→办公工具），集成通用邮件服务（SMTP），Outlook风格邮件发送页面
- **数据导出系统（双模式：前端导出 + 后端导出）** [详见 §13](#13-数据导出系统)
- **v2.0 新功能**: 系统健康监控、IP黑白名单管理、站内消息中心、快捷收藏夹、系统公告弹窗、
  代码生成器、批量数据导入、操作日志可视化分析、API调试面板、数据库备份恢复、全局命令搜索Ctrl+K、多主题色切换 [详见 §14](#14-v20-新功能)

### 待实现
- WebSocket 通知

### 运维忽略（暂不执行）
- Docker 容器化, CI/CD, 健康检查, 日志聚合
- 工具链（ESLint/TypeScript/测试）

---

## 12. 新建模块速查

```
步骤1  数据库 → 创建表（sys_ 前缀）
步骤2  后端   → Entity (extends BaseEntity)
步骤3  后端   → Mapper (extends BaseMapper, @Mapper)
步骤4  后端   → Service (IService + ServiceImpl + @Transactional)
步骤5  后端   → Controller (CRUD + @SaCheckPermission + @Valid + @OperateLog)
步骤6  数据库 → sys_menu 表插入菜单记录
步骤7  前端   → api/xxx.js（5个标准函数）
步骤8  前端   → views/xxx/index.vue（defineOptions name + useTablePage + useStorage）
步骤9  前端   → componentMap.js 追加 1 行
```

---

> **引用文档**: 完整规范详见 [rxadmin-dev-skills.md](./src/main/resources/docs/rxadmin-dev-skills.md)，优化进度详见 [rxadmin-optimization.md](./src/main/resources/docs/rxadmin-optimization.md)，架构详情详见 [rxadmin.md](./src/main/resources/docs/rxadmin.md)。

---

## 14. v2.0 新功能

### 14.1 全局命令搜索 (Ctrl+K)

`components/CommandPalette.vue` - 类似 VS Code Ctrl+K 命令面板，融合原有 SearchBox 菜单搜索。

| 能力 | 说明 |
|------|------|
| 快捷键 | `Ctrl+K` 全局唤起 |
| 页面搜索 | 从 `userStore.menus` 递归提取所有菜单项模糊匹配 |
| 最近访问 | 从 `tagsStore.visitedViews` 显示最近5个标签页 |
| 快捷操作 | 暗黑模式切换、全屏、返回首页、退出登录 |
| 键盘导航 | 上下箭头选择，回车跳转/执行 |

**集成**: 在 `layout/index.vue` 顶栏添加了 Ctrl+K 快捷入口按钮，`CommandPalette` 挂载在 Layout 底部。

### 14.2 系统健康监控

**前端**: `views/monitor/health/index.vue` | **后端**: `HealthController.java` + `HealthService.java`

| 监控项 | 数据来源 |
|--------|----------|
| CPU使用率/核心数 | `OperatingSystemMXBean.getSystemLoadAverage()` |
| 内存使用率/总量 | `Runtime.getRuntime()` |
| JVM堆/非堆内存 | `MemoryMXBean` |
| 线程数/峰值/守护 | `ThreadMXBean` |
| 磁盘使用率 | `File.listRoots()` |
| GC统计 | `GarbageCollectorMXBean` |

- 每10秒自动刷新（`setInterval` + `onUnmounted` 清理）
- 顶部4个统计卡片 + ECharts 仪表盘 + JVM详情表格

### 14.3 IP黑白名单管理

**前端**: `views/system/ipRule/index.vue` | **后端**: `SysIpRuleController.java` + `SysIpRuleService.java`

| 表 | 字段 |
|----|------|
| `sys_ip_rule` | id, ip_address, rule_type(BLACK/WHITE), description, status |

- 支持黑名单/白名单/关闭三种模式切换
- IP规则CRUD + 分页搜索
- 模式切换写入 `sys_config` 表（`ip.filter.mode`）
- 模式切换提示：黑名单=禁止列表IP访问，白名单=仅允许列表IP访问

### 14.4 站内消息中心

**前端**: `views/content/message/index.vue` | **后端**: `SysMessageController.java` + `SysMessageService.java`

| 表 | 字段 |
|----|------|
| `sys_message` | id, sender_id, receiver_id, title, content, message_type, is_read, read_time, link_path |

- el-timeline 时间轴展示消息
- 类型筛选：全部/系统消息/通知/私信
- 标记已读/全部已读
- 未读红点 + 分类统计
- 消息可关联跳转链接

### 14.5 快捷收藏夹

| 组件 | 说明 |
|------|------|
| `FavoriteStar.vue` | 可复用的星标收藏组件（页面内使用） |
| `FavoritesPanel.vue` | 侧边栏收藏面板（已集成到 `layout/index.vue`） |

| 表 | 字段 |
|----|------|
| `sys_user_favorite` | id, user_id, menu_id, name, path, icon, sort_order |

- 点击星标切换收藏/取消（toggle模式）
- 侧边栏底部显示收藏列表，快速跳转
- localStorage 缓存收藏状态加速渲染

### 14.6 系统公告弹窗

| 组件 | 说明 |
|------|------|
| `AnnouncementPopup.vue` | 登录后自动弹窗，已集成到 `layout/index.vue` |

- 查询 `sys_notice` 中 `noticeType='2'` 且未读的公告
- 弹窗以 `el-dialog` 展示，一次弹一条
- 关闭后自动检测下一条
- 已关闭的公告记录到 `localStorage` 避免重复弹出

### 14.7 代码生成器

**前端**: `views/tool/gen/index.vue` | **后端**: `GenController.java`

三步向导式操作：
1. **选择表** - 从 `information_schema` 读取所有表 → 选中后展示字段列表
2. **配置** - 输入包名、模块名、作者、父菜单ID
3. **预览生成** - Tab切换查看6种文件预览（Entity/Mapper/Service/Controller/Vue/API）

**生成模板**：
- Entity → 继承 `BaseEntity`，Lombok注解
- Mapper → `BaseMapper<Entity>` + `@Mapper`
- Service → `ServiceImpl<Mapper, Entity>`
- Controller → `BaseCrudController<Service, Entity>`
- Vue → `useTablePage` 模式 CRUD 页面
- API → `request` 封装

**类型映射**: bigint→Long, int/tinyint→Integer, datetime→LocalDateTime, decimal→BigDecimal

### 14.8 批量数据导入

**前端**: `views/tool/importData/index.vue` | **后端**: `ImportController.java`

三步向导式操作：
1. **上传** - 选择目标表 + 拖拽上传 Excel/CSV
2. **预览** - 展示解析结果（总行数/有效行/列映射）+ 导入模式选择（新增/新增+更新）
3. **执行** - 进度条 + 成功/失败统计

### 14.9 操作日志可视化分析

**前端**: `views/monitor/logAnalysis/index.vue` | **后端**: `LogAnalysisController.java`

| 图表 | 类型 | 数据 |
|------|------|------|
| 统计卡片 | 4个 | 今日操作数、异常数、活跃用户、最高频操作 |
| 操作时段分布 | ECharts柱状图 | 24小时操作量 |
| 操作类型分布 | ECharts饼图 | 各操作类型占比 |
| 近7天趋势 | ECharts折线图 | 每日总数+异常数 |

### 14.10 API调试面板

**前端**: `views/tool/apiDebug/index.vue` | **后端**: `ApiDebugController.java`

- 左栏：所有 API 端点列表（从 `RequestMappingHandlerMapping` 扫描），支持搜索过滤
- 右栏：请求方法+URL+Query参数编辑 → 发送 → 响应展示
- 类似 Postman 的控制台风格界面
- 显示状态码 + 响应耗时

### 14.11 数据库备份与恢复

**前端**: `views/tool/backup/index.vue` | **后端**: `BackupController.java`

- 立即备份 → 执行 `mysqldump` → 保存到 `backups/` 目录
- 备份列表（文件名/大小/时间）
- 下载/删除备份文件

### 14.12 多主题色系统

`styles/themes.scss` + `composables/useLayoutSettings.js`

| 主题 | data-theme | 主色 |
|------|------------|------|
| 默认蓝 | (无) | #409EFF |
| 翡翠绿 | green | #67C23A |
| 深紫 | purple | #9C27B0 |
| 暖橙 | orange | #E6A23C |
| 青色 | cyan | #00BCD4 |

- 通过 `data-theme` 属性切换 CSS 变量 `--rx-primary`
- Element Plus 主色联动 `--el-color-primary`
- 由 `useLayoutSettings` composable + `useStorage` 持久化

### 14.13 新增文件清单

**数据库脚本 (3个)**:
- `db/features_init.sql` - 建表: sys_ip_rule, sys_message, sys_user_favorite
- `db/features_menu.sql` - 8个新菜单 + 按钮权限

**后端 (14个)**:
- 实体: `SysIpRule.java`, `SysMessage.java`, `SysUserFavorite.java`
- Mapper: `SysIpRuleMapper.java`, `SysMessageMapper.java`, `SysUserFavoriteMapper.java`
- Service: `SysIpRuleService.java`, `SysMessageService.java`, `SysUserFavoriteService.java`, `HealthService.java`
- Controller: `SysIpRuleController.java`, `SysMessageController.java`, `SysUserFavoriteController.java`, `HealthController.java`, `AnnouncementController.java`, `GenController.java`, `ImportController.java`, `ApiDebugController.java`, `LogAnalysisController.java`, `BackupController.java`

**前端 (20个)**:
- API: `health.js`, `ipRule.js`, `message.js`, `favorite.js`, `announcement.js`, `gen.js`, `importData.js`, `logAnalysis.js`, `apiDebug.js`, `backup.js`
- 页面: `monitor/health/index.vue`, `system/ipRule/index.vue`, `content/message/index.vue`, `tool/gen/index.vue`, `tool/importData/index.vue`, `monitor/logAnalysis/index.vue`, `tool/apiDebug/index.vue`, `tool/backup/index.vue`
- 组件: `CommandPalette.vue`, `FavoriteStar.vue`, `FavoritesPanel.vue`, `AnnouncementPopup.vue`
- 样式: `styles/themes.scss`
- Composable: `useLayoutSettings.js`

**集成修改 (3个)**:
- `router/componentMap.js` - 新增8个组件映射
- `layout/index.vue` - 集成 CommandPalette + AnnouncementPopup + FavoritesPanel + Ctrl+K按钮
- `main.js` - 导入 themes.scss

---

## 13. 数据导出系统

### 13.1 架构总览

```
┌──────────────────────────────────────────────────────────────┐
│                      ExportButton 组件                        │
│                     (props: data, columns, title, mode)       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  mode='client' (默认)              mode='server' (保留)        │
│  ┌─────────────────────┐          ┌─────────────────────┐     │
│  │ 前端直接生成文件       │          │ POST 到后端生成       │     │
│  │ 无网络请求，≈0.2秒    │          │ 有网络请求，需等后端   │     │
│  └────────┬────────────┘          └────────┬────────────┘     │
│           │                                │                  │
│     ┌─────┴─────┐                    ┌─────┴─────┐            │
│     │ Excel │ PDF │                    │ Excel │ PDF │            │
│     │exceljs│jspdf│                   │ Apache│PDFBox│           │
│     │       │html2│                    │ POI  │      │            │
│     │       │canvas│                   │      │      │            │
│     └───────┴──────┘                    └──────┴──────┘            │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

**设计原则**: 默认前端导出，数据已从后端查询到前端，不再发送重复请求。后端导出功能完整保留，通过 `mode="server"` 切换。

### 13.2 依赖清单

| 库 | 版本 | 用途 | 使用位置 |
|---|------|------|---------|
| `exceljs` | latest | 纯前端生成 .xlsx（样式/合并单元格/自动列宽/冻结表头） | `exportClient.js` |
| `jspdf` | latest | PDF 页面容器 | `exportClient.js` |
| `jspdf-autotable` | latest | PDF 表格插件（自动分页/表头重复） | `exportClient.js` |
| `html2canvas` | latest | HTML 表格截图（解决中文字体嵌入问题） | `exportClient.js` |
| Apache POI (ooxml) | 5.2.x | 后端 Excel 生成 | `ExportServiceImpl.java` |
| PDFBox | 3.0.x | 后端 PDF 生成 | `ExportServiceImpl.java` |

### 13.3 文件清单

| 文件 | 层级 | 说明 |
|------|------|------|
| `ui/src/utils/exportClient.js` | 前端工具 | `exportExcelClient()` / `exportPdfClient()` |
| `ui/src/components/ExportButton/index.vue` | 前端组件 | 导出按钮（支持 client/server 双模式） |
| `ui/src/api/export.js` | 前端 API | `getExportConfigApi()` / `exportExcelApi()` / `exportPdfApi()` |
| `com.rx.admin.controller.ExportController` | 后端控制器 | `/api/export/excel` / `/api/export/pdf` / `/api/export/config` |
| `com.rx.admin.service.impl.ExportServiceImpl` | 后端服务 | Apache POI 生成 Excel / PDFBox 生成 PDF |

### 13.4 前端导出实现

#### Excel (exceljs)

```
工作流程:
  1. 创建 Workbook，添加 Worksheet
  2. 标题行：合并单元格 + 加粗居中
  3. 表头行：蓝色背景 + 白色文字 + 边框
  4. 数据行：斑马纹 + 边框 + 自动列宽（中文按 2.2 倍宽计算）
  5. 冻结表头（ySplit: 2）
  6. workbook.xlsx.writeBuffer() → Blob → 浏览器下载
```

#### PDF (html2canvas + jspdf)

```
工作流程:
  1. 创建隐藏 div，构建干净 HTML 表格（Element Plus 配色）
  2. html2canvas 截图（scale:2 高清），使用浏览器系统字体
  3. jspdf A4 横向布局，分页插入图片
  4. pdf.save() 触发下载
```

> **选型理由**: html2canvas 方式无需嵌入大型中文字体文件（5-15MB），直接利用浏览器系统字体渲染，完美支持中文。

### 13.5 后端导出实现（保留）

| 端点 | 方法 | 说明 |
|------|------|------|
| `GET /api/export/config?path=/system/user` | GET | 查询页面是否启用导出及支持的导出类型 |
| `POST /api/export/excel` | POST | 后端 Apache POI 生成 Excel |
| `POST /api/export/pdf` | POST | 后端 PDFBox 生成 PDF |

### 13.6 使用方式

#### 默认前端导出（推荐）

```html
<!-- 用户管理页面 system/user/index.vue -->
<ExportButton
  :data="sortedTableData"
  :columns="exportColumns"
  title="用户管理"
/>

<!-- 无 mode 属性，默认 mode='client'，纯前端生成文件，无网络请求 -->
```

#### 切换为后端导出

```html
<!-- 只需添加 mode="server" -->
<ExportButton
  :data="sortedTableData"
  :columns="exportColumns"
  title="用户管理"
  mode="server"
/>
```

### 13.7 ExportButton Props

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `Array` | `[]` | 表格数据（响应式数组） |
| `columns` | `Array` | `[]` | 列定义 `[{ field, label }]` |
| `title` | `String` | `''` | 导出标题（默认取 `route.meta.title`） |
| `mode` | `String` | `'client'` | 导出模式：`'client'`（前端）/ `'server'`（后端） |

### 13.8 扩展新页面导出

```javascript
// 步骤 1：页面中定义导出列
const exportColumns = [
  { field: 'id', label: 'ID' },
  { field: 'name', label: '名称' },
  { field: 'createTime', label: '创建时间' }
]

// 步骤 2：在模板中加入 ExportButton
// <ExportButton :data="tableData" :columns="exportColumns" title="模块名" />

// 步骤 3（可选）：后端 sys_menu 配置导出类型的菜单权限（如无需后端导出可跳过）
```

### 13.9 注意事项

- **前端导出超时**: 走前端导出时不发网络请求，无超时问题
- **后端导出超时**: `export.js` 中已设 `timeout: 120000`（2 分钟）
- **PDF 中文**: 前端采用 html2canvas 截图方式，使用系统字体，无乱码
- **后端代码保留**: `ExportController` + `ExportServiceImpl` 完整保留，非后续需要优化的内容

### 13.10 config 请求去重机制

`GET /api/export/config?path=/system/xxx` 用于查询页面是否启用导出。此接口存在被重复调用的隐患，已通过三层防护彻底解决：

| 层级 | 机制 | 代码位置 | 效果 |
|------|------|---------|------|
| **① 触发源精简** | `watch(immediate:true)` → `onMounted()` | `index.vue:68` | 每次组件挂载只触发 1 次（不再 setup 阶段触发 + watch 路由变化触发） |
| **② 模块级缓存** | `const configCache = new Map()` | `index.vue:37` | path → types 映射，同一路径只请求一次，跨组件实例共享 |
| **③ 并发锁** | `let fetchingPath = ''` | `index.vue:38` | 同一路径正在请求中跳过，防止竞态重复 |

**修复历程**:
- **v1 问题**: `watch(immediate:true)` + `onMounted()` = 每次挂载 2 次请求
- **v2 问题**: 去掉 `onMounted` 留 `watch(immediate)` → setup 阶段触发 + 路由重定向再次触发 = 2 次
- **v3 问题**: `watch(immediate)` → `onMounted()+watch` → 导航时 onMounted 和 watch 几乎同时触发 = 2 次
- **v4 最终**: 仅 `onMounted()` + 模块级缓存 + 并发锁 → **每个路径首次 1 次请求，之后切标签 0 次**

**关键设计**: 缓存和锁变量定义在 `<script setup>` 外部（模块级），而非组件实例内。因为 `<keep-alive>` 的 `:key` 变化会重建组件实例，实例内变量会丢失。
