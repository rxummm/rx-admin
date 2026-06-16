# 详细开发规范

> 本文档为 SKILL.md 的补充参考，包含各级开发的详细规范说明。

## 项目包结构

```
com.rx.admin
├── RxAs400Application.java           # 启动类 (exclude DataSourceAutoConfiguration)
├── common/                            # 公共模块
│   ├── base/                          # BaseEntity, BaseCrudController
│   ├── result/                        # Result<T>, PageResult<T>
│   ├── exception/                     # GlobalExceptionHandler (10种异常类型)
│   ├── annotation/                    # @OperateLog, @DataScope
│   ├── aspect/                        # OperateLogAspect (@Async 异步 + 参数脱敏)
│   ├── constant/                      # PageConstants 等
│   ├── handler/                       # AesTypeHandler, DataScopeInnerInterceptor
│   ├── security/                      # IpFilter, NotLoginFilter, ReplayAttackFilter
│   └── utils/                         # CaptchaUtil, DataMaskUtil
├── framework/                         # 框架配置模块 (Spring Boot 自动装配)
│   ├── datasource/                    # PrimaryDataSourceConfig / SecondDataSourceConfig
│   ├── mybatis/                       # MybatisPlusConfig / MetaObjectHandlerConfig
│   ├── security/                      # SaTokenConfig / StpInterfaceImpl
│   ├── async/                         # AsyncConfig (@EnableAsync)
│   ├── cache/                         # CacheConfig (Caffeine)
│   └── web/                           # CorsConfig / RateLimiterConfig
├── entity/                            # 实体
│   └── {module}/                      # 子模块 (classics/, as400/)
├── controller/                        # 控制器
├── service/                           # 服务层
│   └── impl/                          # 实现类
├── mapper/                            # Mapper
├── modules/                           # ⭐ 业务模块化分层 (DTO/VO/Convert)
│   └── {domain}/{entity}/
│       ├── dto/                       # CreateDTO / UpdateDTO / QueryDTO
│       ├── vo/                        # 视图对象 (响应专用)
│       └── convert/                   # MapStruct 转换器接口
└── config/                            # 遗留配置 (逐步迁移至 framework/)
```

## 前端目录结构

```
ui/src/
├── main.js                  # 入口
├── App.vue                  # 根组件
├── api/                     # API 模块 (每个模块一个文件)
├── composables/             # 组合式函数
│   ├── useStorage.js         # localStorage 统一管理 (命名空间 rx_admin_*)
│   ├── useTablePage.js       # 通用表格分页
│   ├── useTheme.js           # 亮/暗主题切换
│   ├── useMenuI18n.js        # 菜单国际化翻译
│   ├── usePasswordStrength.js # 密码强度检测
│   ├── useTableHeight.js     # 表格高度自适应
│   ├── useLayoutSettings.js  # 布局设置（含 ECharts 主题联动）
│   └── useMarkdownRenderer.js # Markdown 渲染器（marked + highlight.js）
├── i18n/                    # 国际化
├── layout/                  # 布局组件
├── components/               # 公共组件
├── router/
│   ├── index.js             # 路由配置
│   └── componentMap.js      # 组件映射表
├── stores/                  # Pinia 状态
├── styles/                  # 全局样式
├── utils/                   # 工具函数
│   └── request.js           # Axios 封装
└── views/                   # 页面视图
```

## 后端详细规范

### BaseEntity 基类

```java
@Data
public class BaseEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### Entity 强制约定
1. 必须继承 `BaseEntity`，获得 id、deleted、createTime、updateTime
2. 必须添加 `@EqualsAndHashCode(callSuper = true)`
3. 使用 Lombok `@Data`，禁止手写 getter/setter
4. 表名格式: `sys_` 前缀 + 下划线命名 (如 `sys_user`)
5. 字段命名: Java 驼峰 → 数据库下划线 (MyBatis Plus 自动映射)
6. 状态字段: 统一 `Integer` 类型，1=正常/启用，0=禁用
7. 时间字段: 使用 `LocalDateTime`，由 MyBatis Plus 自动填充
8. 逻辑删除: 使用 `@TableLogic` 注解，数据库字段 `deleted`

### Mapper 强制约定
1. 必须添加 `@Mapper` 注解
2. 继承 `BaseMapper<Entity>`
3. 复杂 SQL 使用 `@Select` / `@Update` / `@Delete` 注解
4. **禁止创建 XML 映射文件**

### Service 强制约定
1. 继承 `ServiceImpl<Mapper, Entity>` + 实现 `IService<Entity>`
2. **依赖注入使用构造器注入**: `private final` + 构造函数，推荐 `@RequiredArgsConstructor`
3. **禁止 `@Autowired` 字段注入**
4. 分页查询返回 `PageResult<T>` (非 MyBatis Plus 原生 `Page`)
5. 条件查询使用 `LambdaQueryWrapper` (类型安全)
6. 所有写操作使用 `@Transactional` 注解

### Controller 强制约定
1. **URL 前缀统一**: `/api/{模块}/{实体}` (如 `/api/sys/user`)
2. **RESTful 风格**: `GET` 查询、`POST` 新增、`PUT` 修改、`DELETE` 删除
3. **依赖注入**: 构造器注入
4. **分页接口**: `GET /page?page=1&size=10&keyword=xxx`
5. **统一返回**: `Result.success(data)` / `Result.error(msg)`
6. **权限注解**: 每个接口 `@SaCheckPermission("module:entity:action")`
7. **API 文档**: `@Tag(name)` + `@Operation(summary)`
8. **批量删除**: 路径参数 `{ids}` 接收 `List<Long>`
9. **DTO 入参 + VO 出参**: 禁止 Entity 直接暴露到 Controller，用 DTO 接收请求 + VO 返回响应
10. **MapStruct 转换**: 所有 Entity ↔ DTO ↔ VO 转换通过 Convert 接口，禁止手动 BeanUtils.copyProperties

### DTO / VO / Convert 分层规范

**分层职责**:

| 层 | 作用 | 位置 | 示例 |
|----|------|------|------|
| **DTO** | 接收前端请求 | `modules/{domain}/{entity}/dto/` | `SysUserCreateDTO`, `SysUserQueryDTO` |
| **VO** | 返回前端响应 | `modules/{domain}/{entity}/vo/` | `SysUserVO` |
| **Convert** | DTO ↔ Entity ↔ VO 转换 | `modules/{domain}/{entity}/convert/` | `SysUserConvert` |

**MapStruct 强制规范**:

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysUserConvert {
    SysUser toEntity(UserCreateDTO dto);  // 新增转换

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserUpdateDTO dto, @MappingTarget SysUser entity);  // 更新转换

    UserVO toVO(SysUser entity);  // 实体 → VO
    List<UserVO> toVOList(List<SysUser> list);  // 批量转换
}
```

| 规则 | 必须 | 说明 |
|------|------|------|
| `componentModel = "spring"` | ✅ | 生成 Spring Bean |
| `unmappedTargetPolicy = ReportingPolicy.IGNORE` | ✅ | 忽略未映射字段，消除编译警告 |
| `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` | ✅ | 更新时 null 值不覆盖已有字段 |
| `@MappingTarget` | ✅ | 更新时在原对象上修改 |
| 禁止 `Mappers.getMapper()` | ✅ | 统一使用 Spring 注入 |

### 安全认证规范

**Sa-Token 配置**:
- Token 名称: `{project}-token` (如 `rx-admin-token`)
- Token 有效期: 7 天 (604800 秒)
- Token 风格: 随机 UUID
- 允许并发登录: 是

**登录限流**:
```java
// RateLimiterConfig.java — Guava RateLimiter 每 IP 3次/秒
ConcurrentHashMap<String, RateLimiter> rateLimiters;
```

**失败锁定**:
```java
// LoginAttemptService — 5次失败锁定30分钟
ConcurrentHashMap<String, Integer> attempts;
ConcurrentHashMap<String, Long> lockTimes;
```

**操作日志**:
- `@Async` 异步保存，不阻塞主线程
- 脱敏字段: `password`, `oldPassword`, `newPassword`, `confirmPassword`, `token`, `secret`, `accessKey`, `secretKey`
- 参数长度 > 2000 截断，返回结果 > 1000 截断，错误信息 > 500 截断

## 前端详细规范

### main.js 入口规范

```javascript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import router from './router'
import pinia from './stores'
import './styles/global.scss'
import i18n from './i18n'

const app = createApp(App)
app.use(ElementPlus, { locale: zhCn })
app.use(router).use(pinia).use(i18n)
app.mount('#app')
```

### useStorage Composable

必须使用 `useStorage` 管理所有 localStorage 操作，禁止直接调用原生 API。

```javascript
const STORAGE_PREFIX = 'rx_admin_'

export const STORAGE_KEYS = {
  TOKEN: 'rx_admin_token',
  USER_INFO: 'rx_admin_userInfo',
  ROLES: 'rx_admin_roles',
  PERMS: 'rx_admin_perms',
  MENUS: 'rx_admin_menus',
  LOCALE: 'rx_admin_locale',
  THEME: 'rx_admin_theme',
  READ_NOTICE_IDS: 'rx_admin_readNoticeIds',
}

export function useStorage(key, defaultValue) {
  function get() { /* JSON.parse */ }
  function set(value) { /* JSON.stringify + setItem */ }
  function remove() { /* removeItem */ }
  return { get, set, remove }
}
```

强制约定:
1. 所有 key 使用 `rx_admin_` 前缀
2. 必须通过 `STORAGE_KEYS` 常量引用
3. 自动 JSON 序列化/反序列化
4. 异常静默处理 (localStorage 不可用时降级为内存)

### Axios 封装规范

```javascript
const service = axios.create({
  baseURL: '/api',
  timeout: Number(import.meta.env.VITE_API_REQUEST_TIMEOUT) || 15000
})

// 请求拦截器: 添加 Token + NProgress.start()
// 响应拦截器: 统一处理 code !== 200 + NProgress.done()
```

强制约定:
1. `baseURL` 设为 `/api`
2. Token 放在 `Authorization` 请求头
3. 统一拦截 `code !== 200` 的错误
4. 使用 NProgress 显示请求进度
5. **后台定时轮询必须传入 `_skipNProgress: true`**
6. **禁止硬编码魔法数字**，通过 `import.meta.env.VITE_xxx` 读取

### Api 模块规范

```javascript
export function getXxxPage(params) {
  return request({ url: '/module/xxx/page', method: 'get', params })
}
export function addXxx(data) {
  return request({ url: '/module/xxx', method: 'post', data })
}
export function deleteXxx(ids) {
  return request({ url: `/module/xxx/${ids}`, method: 'delete' })
}
```

约定:
1. 每个业务模块一个 API 文件
2. 命名: `getXxxPage/getXxxById/addXxx/updateXxx/deleteXxx`
3. GET 用 `params`, POST/PUT 用 `data`
4. 未使用的 API 添加 `@reserved` 注释

### 布局组件规范

整体结构 (经典后台三件套):

```
el-container (100vh 全屏)
├── 侧边栏 (220px)
│   └── el-menu 递归渲染 (SubMenu.vue)
└── 右侧主体
    ├── 顶栏 (50px): 折叠按钮 + 面包屑 + 搜索框 + Ctrl+K + 主题切换 + 语言切换 + 通知 + 全屏 + 头像
    ├── 标签栏 (36px): TagsView.vue
    └── 内容区: <router-view />
```

布局组件拆分:
- `layout/index.vue` — 主布局容器
- `layout/SearchBox.vue` — 全局搜索框
- `layout/NoticePopover.vue` — 通知公告弹窗
- `layout/SubMenu.vue` — 递归子菜单
- `layout/TagsView.vue` — 标签页导航
- `components/CommandPalette.vue` — Ctrl+K 命令面板
- `components/FavoriteStar.vue` — 收藏星标
- `components/FavoritesPanel.vue` — 侧边栏收藏面板
- `components/AnnouncementPopup.vue` — 系统公告弹窗

### 国际化与语言切换

无刷新语言切换: 通过 `el-config-provider` 实现

```vue
<template>
  <el-config-provider :locale="elCurrentLocale">
    <router-view />
  </el-config-provider>
</template>
<script setup>
const elCurrentLocale = computed(() => locale.value === 'zh-CN' ? zhCn : en)
</script>
```

- 使用 `i18n.global.locale.value` 切换，禁止 `window.location.reload()`
- 使用 `useStorage(STORAGE_KEYS.LOCALE)` 持久化

## CSS 样式规范

### CSS 变量命名规范

格式: `--{类别}-{属性}`

| 类别 | 说明 | 示例 |
|------|------|------|
| `bg` | 背景色 | `--bg-page`, `--bg-container` |
| `text` | 文字颜色 | `--text-primary`, `--text-secondary` |
| `color` | 主题色 | `--color-primary` |
| `border` | 边框 | `--border-color`, `--border-light` |
| `sidebar` | 侧边栏 | `--sidebar-bg`, `--sidebar-text` |
| `header` | 顶栏 | `--header-bg` |
| `tags` | 标签栏 | `--tags-bg` |
| `shadow` | 阴影 | `--shadow-card` |

禁止:
- `--text-color-secondary` 等 Element Plus 风格命名
- `--bg-color-page` 等冗余命名
- 硬编码颜色值

## API 接口规范

### URL 设计

| 方法 | URL | 说明 |
|------|-----|------|
| `GET` | `/api/{module}/{entity}/page` | 分页查询 |
| `GET` | `/api/{module}/{entity}/{id}` | 获取详情 |
| `POST` | `/api/{module}/{entity}` | 新增 |
| `PUT` | `/api/{module}/{entity}` | 修改 |
| `DELETE` | `/api/{module}/{entity}/{ids}` | 批量删除 |

### 响应格式

成功: `{"code": 200, "msg": "操作成功", "data": {...}}`
分页: `{"code": 200, "msg": "操作成功", "data": {"list": [...], "total": 100, "page": 1, "pageSize": 10}}`
错误: `{"code": 500, "msg": "系统异常：xxx", "data": null}`

## 代码质量规范

### 后端
1. 无用的 Maven 依赖必须删除
2. 无用的 import 必须删除
3. 未使用的 Service 方法必须删除
4. Controller 只做参数接收和结果返回
5. 构造器注入，推荐 `@RequiredArgsConstructor`
6. 操作日志异步保存 (`@Async`)
7. 操作日志参数脱敏

### 前端
1. 组件必须声明 `defineOptions({ name: 'Xxx' })`
2. API 调用放在 `try/catch/finally` 中
3. 列表页加载时显示 loading
4. 删除操作二次确认弹窗
5. 表单有校验规则
6. 弹窗关闭时重置表单
7. localStorage 使用 `useStorage`
8. 图标禁止全量注册
9. 布局逻辑抽取为子组件
10. 表格页面推荐 `useTablePage`

### Element Plus 受控 prop 值规范

当使用 Element Plus 组件的受限 prop（即 prop 只接受枚举值集合）时，返回该 prop 值的函数/计算属性**绝不能返回空字符串 `''`、`null` 或 `undefined`**，必须始终返回 prop 枚举中有效的一个值。

**受影响的常见 prop：**

| 组件 | prop | 允许值 |
|------|------|--------|
| `ElTag` | `type` | `primary`, `success`, `info`, `warning`, `danger` |
| `ElButton` | `type` | `primary`, `success`, `warning`, `danger`, `info`, `text`, `default` |
| `ElAlert` | `type` | `success`, `warning`, `info`, `error` |
| `ElMessage` | `type` | `success`, `warning`, `info`, `error` |
| `ElBadge` | `type` | `primary`, `success`, `warning`, `danger`, `info` |
| `ElProgress` | `status` | `success`, `warning`, `exception` |

**错误示例：**
```javascript
// ❌ 当 m 不在预期范围内时返回空字符串，触发 ElTag prop validation 警告
const methodColor = (m) => m === 'GET' ? 'success' : m === 'POST' ? 'primary' : m === 'PUT' ? 'warning' : m === 'DELETE' ? 'danger' : ''
```

**正确示例：**
```javascript
// ✅ 兜底返回 'info' 作为默认值
const methodColor = (m) => m === 'GET' ? 'success' : m === 'POST' ? 'primary' : m === 'PUT' ? 'warning' : m === 'DELETE' ? 'danger' : 'info'

// ✅ 更清晰的方式 — 先建立映射表，再兜底
const statusColorMap = {
  active: 'success',
  inactive: 'info',
  locked: 'warning',
  deleted: 'danger'
}
const statusColor = (s) => statusColorMap[s] || 'info'
```

**规则总结：**
1. 始终在条件链或映射表的末尾提供兜底值
2. 兜底值必须是 prop 允许的枚举值之一（推荐 `'info'` 或 `'default'`）
3. 优先使用映射表 + 默认值模式，比长条件链更清晰
4. 若 prop 具有 `validator` 校验，不满足校验会抛出控制台警告