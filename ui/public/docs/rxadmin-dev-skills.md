# RX Admin 开发规范与技能手册

> **版本**: 1.0.0 | **更新日期**: 2026-05-31 | **适用项目**: 基于 Spring Boot 3 + Vue 3 的后台管理系统

---

## 目录

1. [技术栈选型标准](#1-技术栈选型标准)
2. [后端开发规范](#2-后端开发规范)
3. [前端开发规范](#3-前端开发规范)
4. [CSS 样式规范](#4-css-样式规范)
5. [API 接口规范](#5-api-接口规范)
6. [国际化规范](#6-国际化规范)
7. [代码质量规范](#7-代码质量规范)
8. [新模块开发流程](#8-新模块开发流程)
9. [常见问题与解决方案](#9-常见问题与解决方案)

---

## 1. 技术栈选型标准

### 1.1 强制技术栈

| 层级 | 技术 | 版本要求 | 说明 |
|------|------|---------|------|
| **运行环境** | Java / Node.js | Java 17+ / Node 18+ | LTS 版本 |
| **后端框架** | Spring Boot | 3.2.x | Jakarta EE 9+ |
| **ORM** | MyBatis Plus | 3.5.x | 继承 `BaseMapper<T>` + `ServiceImpl<M, T>` |
| **安全认证** | Sa-Token | 1.37+ | 替代 Spring Security / Shiro |
| **API 文档** | Knife4j | 4.4+ | OpenAPI 3 规范，`@Tag` / `@Operation` 注解 |
| **数据库** | MySQL | 8.0+ | utf8mb4 字符集 |
| **密码加密** | BCryptPasswordEncoder | — | Spring Security Crypto |
| **前端框架** | Vue 3 (Composition API) | ^3.4.0 | `<script setup>` 语法 |
| **构建工具** | Vite | ^5.0 | 替代 webpack |
| **路由** | Vue Router | ^4.2 | 动态路由 + `addRoute` |
| **状态管理** | Pinia | ^2.1 | Composition API 风格 |
| **HTTP 客户端** | Axios | ^1.6 | 统一拦截器封装 |
| **UI 组件库** | Element Plus | ^2.4 | 全量引入 + 暗黑模式 |
| **CSS 预处理** | SCSS (Dart Sass) | ^1.69 | 全局变量注入 |
| **国际化** | Vue I18n | ^9.14 | Composition API 模式 |
| **进度条** | NProgress | ^0.2 | 路由切换进度条 |

### 1.2 禁止引入的技术

- **Fastjson / Fastjson2**：统一使用 Jackson（Spring Boot 默认）
- **Spring Security**：使用 Sa-Token 替代
- **Shiro**：使用 Sa-Token 替代
- **JSP / Thymeleaf**：前后端完全分离，后端只返回 JSON
- **Vue 2 / Options API**：统一使用 Vue 3 Composition API
- **Vuex**：使用 Pinia 替代
- **Webpack**：使用 Vite 替代

---

## 2. 后端开发规范

### 2.1 项目坐标与包结构

```xml
<!-- pom.xml -->
<groupId>com.rx</groupId>
<artifactId>rx-admin</artifactId>
<version>1.0.0</version>
<java.version>17</java.version>
```

**包结构**（`com.rx.admin`）：

```
com.rx.admin
├── RxAdminApplication.java          # 启动类（放在根包）
├── common/                           # 公共模块
│   ├── BaseEntity.java              # 实体基类
│   ├── Result.java                  # 统一响应封装
│   ├── PageResult.java              # 分页响应封装
│   └── GlobalExceptionHandler.java  # 全局异常处理
├── config/                           # 配置模块
│   ├── CorsConfig.java              # CORS 跨域配置
│   ├── SaTokenConfig.java           # Sa-Token 路由拦截器
│   ├── StpInterfaceImpl.java        # 权限/角色加载
│   ├── MybatisPlusConfig.java       # MyBatis Plus 分页 & 自动填充
│   └── DataSourceConfig.java        # 数据源配置
├── entity/                           # 实体模块
│   └── {module}/                     # 子模块实体
├── controller/                       # 控制器模块
│   └── {module}/                     # 子模块控制器
├── service/                          # 服务层
│   ├── impl/                         # 实现类
│   └── {module}/                     # 子模块服务
└── mapper/                           # 数据访问层
    └── {module}/                     # 子模块 Mapper
```

### 2.2 实体类规范

#### BaseEntity 基类（必须继承）

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

#### 实体类编写规则

```java
@Data
@EqualsAndHashCode(callSuper = true)   // ⚠️ 必须添加
@TableName("sys_user")                // 显式指定表名
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;            // 状态: 1启用/0禁用
    private Long deptId;

}
```

**强制约定**：
1. **必须继承 `BaseEntity`**，获得 id、deleted、createTime、updateTime
2. **必须添加 `@EqualsAndHashCode(callSuper = true)`**
3. **使用 Lombok `@Data`**，禁止手写 getter/setter
4. **表名格式**：`sys_` 前缀 + 下划线命名（如 `sys_user`）
5. **字段命名**：Java 驼峰 → 数据库下划线（MyBatis Plus 自动映射）
6. **状态字段**：统一 `Integer` 类型，1=正常/启用，0=禁用
7. **时间字段**：使用 `LocalDateTime`，由 MyBatis Plus 自动填充
8. **逻辑删除**：使用 `@TableLogic` 注解，数据库字段 `deleted`

### 2.3 Mapper 层规范

```java
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    // 简单查询使用 MyBatis Plus 内置方法
    // 复杂查询使用 @Select 注解，不额外创建 XML

    @Select("SELECT ur.role_id FROM sys_user_role ur WHERE ur.user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);

}
```

**强制约定**：
1. 必须添加 `@Mapper` 注解
2. 继承 `BaseMapper<Entity>`
3. 复杂 SQL 使用 `@Select` / `@Update` / `@Delete` 注解
4. **禁止创建 XML 映射文件**（保持一致性）

### 2.4 Service 层规范

```java
// 接口
public interface SysUserService extends IService<SysUser> {
    PageResult<SysUser> pageQuery(int page, int size, String keyword);
    void addUser(SysUser user, List<Long> roleIds);
    void updateUser(SysUser user, List<Long> roleIds);
    void deleteUsers(List<Long> ids);
}

// 实现类
@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;  // 构造器注入

    public SysUserServiceImpl(SysUserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public PageResult<SysUser> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                   .or()
                   .like(SysUser::getNickname, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> pageResult = page(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }
}
```

**强制约定**：
1. 继承 `ServiceImpl<Mapper, Entity>` + 实现 `IService<Entity>`
2. **依赖注入使用构造器注入**：`private final` + 构造函数
3. **禁止 `@Autowired` 字段注入**
4. 分页查询返回 `PageResult<T>`（非 MyBatis Plus 原生 `Page`）
5. 条件查询使用 `LambdaQueryWrapper`（类型安全）
6. 所有写操作使用 `@Transactional` 注解

### 2.5 Controller 层规范

```java
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/sys/user")
public class SysUserController {

    private final SysUserService userService;  // 构造器注入

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户列表(分页)")
    @GetMapping("/page")
    @SaCheckPermission("sys:user:query")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @SaCheckPermission("sys:user:query")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @SaCheckPermission("sys:user:add")
    public Result<?> add(@RequestBody SysUser user,
                         @RequestParam(required = false) List<Long> roleIds) {
        userService.addUser(user, roleIds);
        return Result.success();
    }

    @Operation(summary = "更新用户")
    @PutMapping
    @SaCheckPermission("sys:user:edit")
    public Result<?> update(@RequestBody SysUser user,
                            @RequestParam(required = false) List<Long> roleIds) {
        userService.updateUser(user, roleIds);
        return Result.success();
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/{ids}")
    @SaCheckPermission("sys:user:delete")
    public Result<?> delete(@PathVariable List<Long> ids) {
        userService.deleteUsers(ids);
        return Result.success();
    }
}
```

**强制约定**：
1. **URL 前缀统一**：`/api/{模块}/{实体}`（如 `/api/sys/user`）
2. **RESTful 风格**：`GET` 查询、`POST` 新增、`PUT` 修改、`DELETE` 删除
3. **依赖注入**：构造器注入（`private final` + 构造函数）
4. **分页接口**：`GET /page?page=1&size=10&keyword=xxx`
5. **统一返回**：`Result.success(data)` / `Result.error(msg)`
6. **权限注解**：每个接口添加 `@SaCheckPermission("module:entity:action")`
7. **API 文档**：`@Tag(name)` 分组 + `@Operation(summary)` 描述
8. **批量删除**：路径参数 `{ids}` 接收 `List<Long>`

### 2.6 公共模块规范

#### Result 统一响应

```java
@Data
public class Result<T> {
    private int code;      // 200=成功, 非200=失败
    private String msg;    // 提示信息
    private T data;        // 响应数据

    public static <T> Result<T> success(T data) { ... }
    public static <T> Result<T> success() { return success(null); }
    public static <T> Result<T> error(String msg) { ... }
}
```

#### PageResult 分页响应

```java
@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) { ... }
}
```

#### GlobalExceptionHandler 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常：" + e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        return Result.error("未登录或登录已过期");
    }
}
```

### 2.7 配置类规范

#### CORS 跨域配置

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

#### Sa-Token 配置

```java
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/register", "/doc.html", "/swagger-ui/**", "/v3/api-docs/**");
    }
}
```

#### MyBatis Plus 配置

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### 2.8 安全认证规范

**权限码命名规范**：`{模块}:{实体}:{操作}`

| 操作 | 权限码格式 | 示例 |
|------|----------|------|
| 查询 | `module:entity:query` | `sys:user:query` |
| 新增 | `module:entity:add` | `sys:user:add` |
| 修改 | `module:entity:edit` | `sys:user:edit` |
| 删除 | `module:entity:delete` | `sys:user:delete` |

**Sa-Token 配置约定**：
- Token 名称：`{project}-token`（如 `rx-admin-token`）
- Token 有效期：7 天（604800 秒）
- Token 风格：随机 UUID
- 允许并发登录：是

---

## 3. 前端开发规范

### 3.1 目录结构

```
ui/src/
├── main.js                  # 入口：注册插件、全局样式
├── App.vue                  # 根组件（仅 <router-view />）
├── api/                     # API 请求模块（每个模块一个文件）
│   ├── auth.js
│   ├── user.js
│   └── ...
├── composables/             # 组合式函数（useXxx 命名）
│   ├── useTheme.js
│   └── useMenuI18n.js
├── i18n/                    # 国际化
│   ├── index.js
│   └── lang/
│       ├── zh-CN.js
│       └── en-US.js
├── layout/                  # 布局组件
│   ├── index.vue
│   ├── SubMenu.vue
│   └── TagsView.vue
├── router/
│   ├── index.js             # 路由配置
│   └── componentMap.js      # 组件映射表
├── stores/                  # Pinia 状态管理
│   ├── user.js
│   └── tags.js
├── styles/                  # 全局样式
│   ├── variables.scss       # CSS 变量（亮/暗双主题）
│   └── global.scss          # 全局重置 + 通用类
├── utils/
│   └── request.js           # Axios 封装
└── views/                   # 页面视图
    ├── login/
    ├── dashboard/
    ├── system/
    ├── monitor/
    ├── tool/
    └── ...
```

### 3.2 入口文件规范 (main.js)

```javascript
import { createApp } from 'vue'
import App from './App.vue'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// Router & Store
import router from './router'
import pinia from './stores'

// 全局样式
import './styles/global.scss'

// 国际化
import i18n from './i18n'

const app = createApp(App)

// 注册 Element Plus（含中文语言包）
app.use(ElementPlus, { locale: zhCn })

// 全局注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(pinia)
app.use(i18n)
app.mount('#app')
```

### 3.3 路由设计规范

#### 动态路由架构

**核心理念**：`constantRoutes` 只保留 Login 和 Layout 空壳，所有业务路由由后端菜单表驱动，通过 `router.addRoute` 动态注入。

```javascript
// constantRoutes — 仅外壳
const constantRoutes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue') },
  { path: '/', name: 'Layout', component: Layout, children: [] }
]
```

#### componentMap 映射表

```javascript
// router/componentMap.js
// key = views/ 下文件路径（不含 .vue），与 sys_menu.component 字段对齐
// value = { component: 懒加载函数, name: 组件 defineOptions name }

export const componentMap = {
  'dashboard/index':             { component: () => import('@/views/dashboard/index.vue'),             name: 'Dashboard' },
  'system/user/index':           { component: () => import('@/views/system/user/index.vue'),           name: 'SystemUser' },
  'system/role/index':           { component: () => import('@/views/system/role/index.vue'),           name: 'SystemRole' },
  'system/menu/index':           { component: () => import('@/views/system/menu/index.vue'),           name: 'SystemMenu' },
  'system/dept/index':           { component: () => import('@/views/system/dept/index.vue'),           name: 'SystemDept' },
  'tool/dict/index':             { component: () => import('@/views/tool/dict/index.vue'),             name: 'ToolDict' },
  'tool/region/index':           { component: () => import('@/views/tool/region/index.vue'),           name: 'ToolRegion' },
  'tool/analysis/index':         { component: () => import('@/views/tool/analysis/index.vue'),         name: 'ToolAnalysis' },
  'content/notice/index':        { component: () => import('@/views/content/notice/index.vue'),        name: 'ContentNotice' },
  'monitor/log/index':           { component: () => import('@/views/monitor/log/index.vue'),           name: 'MonitorLog' },
  'monitor/online/index':        { component: () => import('@/views/monitor/online/index.vue'),        name: 'MonitorOnline' },
  // ... 其他业务模块
}
```

**强制约定**：
1. `componentMap` 的 key 必须与 `sys_menu.component` 字段值完全一致
2. `name` 必须与页面组件的 `defineOptions({ name: 'xxx' })` 完全一致
3. `name` 使用英文 PascalCase 命名，确保 `keep-alive` 缓存生效
4. 文件路径格式：`{模块}/{页面}/index`（如 `system/user/index`）

#### 路由守卫规范

```javascript
let dynamicRoutesAdded = false

router.beforeEach(async (to, from, next) => {
  NProgress.start()

  // 登录页直接放行
  if (to.path === '/login') {
    next()
    return
  }

  // 未登录重定向
  if (!userStore.token) {
    next('/login')
    return
  }

  // 动态路由注册（仅首次）
  if (!dynamicRoutesAdded) {
    if (!userStore.menus.length) {
      await userStore.fetchRouters()
    }
    generateDynamicRoutes(userStore.menus)
    dynamicRoutesAdded = true
    next({ ...to, replace: true })
    return
  }

  next()
})
```

**关键约定**：
- `beforeEach` 保持**纯同步**，不在守卫中发异步请求
- `login()` 中预加载 `fetchUserInfo()` + `fetchRouters()`
- `menus`/`roles`/`perms` 持久化到 `localStorage`
- 使用 `dynamicRoutesAdded` 布尔标记防止重复注册
- 父级菜单路径（无 component）不出现在搜索结果中

### 3.4 页面组件规范

#### 页面组件模板

```vue
<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="关键词搜索" clearable style="width: 200px" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="handleAdd" v-if="hasPermission('xxx:xxx:add')">新增</el-button>
    </div>

    <!-- 表格容器 -->
    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
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
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @change="fetchData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { xxxApi } from '@/api/xxx'

defineOptions({ name: 'ModulePage' })  // ⚠️ 必须声明，用于 keep-alive

// 状态
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

// 弹窗状态
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = reactive({})
const rules = reactive({})

// 方法
async function fetchData() {
  loading.value = true
  try {
    const res = await xxxApi({ page: page.value, size: size.value, keyword: keyword.value })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
// 使用 CSS 变量，不写硬编码颜色
</style>
```

**强制约定**：
1. **必须声明 `defineOptions({ name: 'Xxx' })`**，使用英文 PascalCase，与 `componentMap` 中一致
2. 使用 `<script setup>` 语法
3. 页面结构：`.page-container` > `.search-bar` + `.table-container` + `.page-pagination`
4. 分页参数：`page`(currentPage)、`size`(pageSize)、`total`
5. 弹窗用 `el-dialog`，表单用 `el-form`
6. API 调用放在 `try/catch/finally` 中，`finally` 中关闭 loading
7. 表格操作列固定右侧：`fixed="right"`

### 3.5 Axios 封装规范

```javascript
// utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    NProgress.start()
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token  // ⚠️ 使用项目约定的 Token 名称
    }
    return config
  },
  error => {
    NProgress.done()
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    NProgress.done()
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    return res
  },
  error => {
    NProgress.done()
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
```

**强制约定**：
1. `baseURL` 设为 `/api`（配合 Vite 代理）
2. Token 放在 `Authorization` 请求头
3. 统一拦截 `code !== 200` 的错误
4. 使用 NProgress 显示请求进度

### 3.6 API 模块规范

```javascript
// api/user.js
import request from '@/utils/request'

export function getUserPage(params) {
  return request({ url: '/sys/user/page', method: 'get', params })
}

export function getUserById(id) {
  return request({ url: `/sys/user/${id}`, method: 'get' })
}

export function addUser(data) {
  return request({ url: '/sys/user', method: 'post', data })
}

export function updateUser(data) {
  return request({ url: '/sys/user', method: 'put', data })
}

export function deleteUser(ids) {
  return request({ url: `/sys/user/${ids}`, method: 'delete' })
}
```

**强制约定**：
1. 每个业务模块一个 API 文件
2. 函数命名：`getXxxPage`（分页）、`getXxxById`（详情）、`addXxx`（新增）、`updateXxx`（修改）、`deleteXxx`（删除）
3. GET 请求参数用 `params`，POST/PUT 请求参数用 `data`
4. 删除接口路径参数为 `ids`（支持批量）

### 3.7 Pinia Store 规范

```javascript
// stores/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, getUserInfoApi, getRoutersApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态（从 localStorage 恢复，保证刷新后数据不丢失）
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const perms = ref(JSON.parse(localStorage.getItem('perms') || '[]'))
  const menus = ref(JSON.parse(localStorage.getItem('menus') || '[]'))

  // 登录
  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', token.value)
    await fetchUserInfo()    // 预加载用户信息
    await fetchRouters()     // 预加载菜单路由
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data
    roles.value = res.data.roles || []
    perms.value = res.data.permissions || []
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('perms', JSON.stringify(perms.value))
  }

  // 获取路由菜单
  async function fetchRouters() {
    const res = await getRoutersApi()
    menus.value = res.data || []
    localStorage.setItem('menus', JSON.stringify(menus.value))
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    perms.value = []
    menus.value = []
    localStorage.clear()
  }

  return { token, userInfo, roles, perms, menus, login, fetchUserInfo, fetchRouters, logout }
})
```

**强制约定**：
1. 使用 Composition API 风格（`defineStore('name', () => { ... })`）
2. 状态必须持久化到 `localStorage`
3. 初始化时从 `localStorage` 恢复
4. `login()` 中预加载 `fetchUserInfo()` + `fetchRouters()`
5. `logout()` 清理所有状态和 localStorage

### 3.8 布局组件规范

**整体布局结构**（经典后台三件套）：

```
┌──────────────────────────────────────────┐
│  el-container (100vh 全屏)               │
│  ┌──────────┬───────────────────────────┐│
│  │ 侧边栏    │  右侧主体                  ││
│  │ 220px    │  ┌───────────────────────┐││
│  │          │  │ 顶栏 (50px)            │││
│  │          │  ├───────────────────────┤││
│  │          │  │ 标签栏 (36px)           │││
│  │          │  ├───────────────────────┤││
│  │          │  │ 内容区                  │││
│  │          │  │ <router-view />        │││
│  └──────────┴───────────────────────────┘│
└──────────────────────────────────────────┘
```

**顶栏功能**（从左到右）：
- 折叠按钮 + 面包屑导航
- 全局搜索框（仅搜索叶子菜单，忽略父级目录）
- 暗黑主题切换 | 语言切换 | 通知 | 全屏 | 用户头像下拉

---

## 4. CSS 样式规范

### 4.1 主题系统

采用 **CSS 变量双主题** 方案，通过 `html.dark` 切换：

```scss
// 亮色主题（默认）
:root {
  // 页面背景
  --bg-page: #f0f2f5;
  --bg-container: #fff;
  --bg-hover: #f5f7fa;
  --bg-active: #ecf5ff;
  --bg-highlight: #f0f7ff;
  --bg-highlight-hover: #e3f0ff;

  // 文字颜色
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;

  // 主题色
  --color-primary: #409eff;
  --color-primary-light: #66b1ff;

  // 边框
  --border-color: #e4e7ed;
  --border-light: #ebeef5;
  --border-lighter: #f2f6fc;

  // 侧边栏
  --sidebar-bg: #304156;
  --sidebar-text: #bfcbd9;
  --sidebar-text-active: #409eff;
  // ...

  // 阴影
  --shadow-card: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-header: 0 1px 4px rgba(0, 0, 0, 0.08);
}

// 暗色主题
html.dark {
  --bg-page: #141414;
  --bg-container: #1d1e1f;
  --text-primary: #e5eaf3;
  color-scheme: dark;
  // ...
}
```

### 4.2 CSS 变量命名规范

**格式**：`--{类别}-{属性}`

| 类别 | 说明 | 示例 |
|------|------|------|
| `bg` | 背景色 | `--bg-page`, `--bg-container` |
| `text` | 文字颜色 | `--text-primary`, `--text-secondary` |
| `color` | 主题色 | `--color-primary` |
| `border` | 边框 | `--border-color`, `--border-light` |
| `sidebar` | 侧边栏 | `--sidebar-bg`, `--sidebar-text` |
| `header` | 顶栏 | `--header-bg`, `--header-shadow` |
| `tags` | 标签栏 | `--tags-bg`, `--tags-item-bg` |
| `search` | 搜索框 | `--search-bg` |
| `shadow` | 阴影 | `--shadow-card` |
| `login` | 登录页 | `--login-bg` |

**⚠️ 强制约定**：
- **禁止使用 `--text-color-secondary` 等 Element Plus 风格命名**（正确写法：`--text-secondary`）
- **禁止使用 `--bg-color-page` 等冗余命名**（正确写法：`--bg-page`）
- 组件样式中**禁止硬编码颜色值**，必须使用 CSS 变量

### 4.3 通用样式类

```scss
// 页面容器
.page-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

// 搜索栏
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;
  background: var(--search-bar-bg, var(--bg-container));
  border-radius: 8px;
  margin-bottom: 16px;
}

// 表格容器
.table-container {
  flex: 1;
  overflow: auto;
}

// 分页
.page-pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
  flex-shrink: 0;
}
```

### 4.4 全局重置

```scss
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC',
               'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
```

### 4.5 过渡动画

```scss
// 搜索下拉
.search-dropdown-fade-enter-active,
.search-dropdown-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.search-dropdown-fade-enter-from,
.search-dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

// 页面切换
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
```

### 4.6 Element Plus 覆盖规范

```scss
// 侧边栏菜单激活项
.el-menu--vertical .el-menu-item.is-active {
  background: var(--sidebar-item-active-bg) !important;
  color: var(--sidebar-text-active) !important;
}

// NProgress 进度条
#nprogress .bar {
  background: var(--color-primary) !important;
  height: 3px !important;
}
```

---

## 5. API 接口规范

### 5.1 URL 设计

| 方法 | URL | 说明 |
|------|-----|------|
| `GET` | `/api/{module}/{entity}/page` | 分页查询 |
| `GET` | `/api/{module}/{entity}/{id}` | 获取详情 |
| `POST` | `/api/{module}/{entity}` | 新增 |
| `PUT` | `/api/{module}/{entity}` | 修改 |
| `DELETE` | `/api/{module}/{entity}/{ids}` | 批量删除 |

### 5.2 请求参数规范

**分页请求**：
```
GET /api/sys/user/page?page=1&size=10&keyword=xxx
```

**新增请求**：
```json
POST /api/sys/user
Content-Type: application/json

{
  "username": "admin",
  "password": "123456",
  "nickname": "管理员",
  "status": 1
}
```

### 5.3 响应格式规范

**成功响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

**分页响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}
```

**错误响应**：
```json
{
  "code": 500,
  "msg": "系统异常：xxx",
  "data": null
}
```

### 5.4 认证接口

| 方法 | URL | 说明 |
|------|-----|------|
| `POST` | `/api/auth/login` | 用户登录 |
| `POST` | `/api/auth/register` | 用户注册 |
| `GET` | `/api/auth/user/info` | 获取当前用户信息 |
| `GET` | `/api/auth/menu/routes` | 获取用户路由菜单 |

---

## 6. 国际化规范

### 6.1 目录结构

```
ui/src/i18n/
├── index.js          # Vue I18n 实例创建
└── lang/
    ├── zh-CN.js      # 中文语言包
    └── en-US.js      # 英文语言包
```

### 6.2 配置规范

```javascript
// i18n/index.js
import { createI18n } from 'vue-i18n'
import zhCN from './lang/zh-CN'
import enUS from './lang/en-US'

const i18n = createI18n({
  legacy: false,           // ⚠️ 必须设为 false，使用 Composition API 模式
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: { 'zh-CN': zhCN, 'en-US': enUS }
})

export default i18n
```

### 6.3 语言包规范

```javascript
// lang/zh-CN.js
export default {
  // 系统
  system: {
    title: '系统管理',
    user: '用户管理',
    role: '角色管理',
    menu: '菜单管理',
    dept: '部门管理'
  },
  // 通用
  common: {
    search: '搜索',
    reset: '重置',
    add: '新增',
    edit: '编辑',
    delete: '删除',
    confirm: '确定',
    cancel: '取消',
    success: '操作成功',
    error: '操作失败'
  },
  // 提示
  message: {
    deleteConfirm: '确认删除选中数据？',
    deleteSuccess: '删除成功'
  }
}
```

### 6.4 组件中使用

```vue
<template>
  <span>{{ $t('common.search') }}</span>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>
```

---

## 7. 代码质量规范

### 7.1 通用规范

1. **禁止冗余 import**：每个 import 必须有实际使用
2. **禁止未使用的变量/函数**：代码中不得存在定义但未使用的变量或函数
3. **禁止硬编码颜色**：所有颜色使用 CSS 变量（`var(--xxx)`）
4. **禁止 console.log**：生产代码不得保留调试日志
5. **命名规范**：
   - 文件/目录：小写 + 连字符（`sys-user`）或驼峰（`SysUser`）
   - 变量/函数：驼峰命名（`userName`, `fetchData`）
   - 组件名：PascalCase（`UserList`）
   - 常量：大写下划线（`MAX_SIZE`）

### 7.2 后端代码质量

1. **无用的 Maven 依赖必须删除**
2. **无用的 import 必须删除**
3. **未使用的 Service 方法必须删除**
4. **Controller 只做参数接收和结果返回**，业务逻辑在 Service 中
5. **使用 Lombok 简化代码**，禁止手写 getter/setter/toString

### 7.3 前端代码质量

1. **组件必须声明 `defineOptions({ name: 'Xxx' })`**
2. **API 调用必须放在 `try/catch/finally` 中**
3. **列表页加载时必须显示 loading 状态**
4. **删除操作必须有二次确认弹窗**（`ElMessageBox.confirm`）
5. **表单必须有校验规则**
6. **弹窗关闭时必须重置表单**

---

## 8. 新模块开发流程

### 8.1 开发步骤速查表

| 步骤 | 层级 | 操作 | 产出物 |
|------|------|------|--------|
| 1 | 数据库 | 设计并创建业务表 | DDL SQL |
| 2 | 后端 | 创建实体类（继承 BaseEntity） | `XxxEntity.java` |
| 3 | 后端 | 创建 Mapper 接口（继承 BaseMapper） | `XxxMapper.java` |
| 4 | 后端 | 创建 Service 接口 + 实现类 | `XxxService.java` + `Impl` |
| 5 | 后端 | 创建 Controller（CRUD 接口） | `XxxController.java` |
| 6 | 数据库 | 在 `sys_menu` 表插入菜单记录 | INSERT SQL |
| 7 | 数据库 | 初始化业务数据（可选） | 数据 SQL |
| 8 | 前端 | 创建 API 请求模块 | `api/xxx.js` |
| 9 | 前端 | 开发 `.vue` 页面组件 | `views/xxx/index.vue` |
| 10 | 前端 | 在 `componentMap.js` 追加映射 | 追加 1 行 |
| 11 | 验证 | 启动后端 → 启动前端 → 功能测试 | — |

### 8.2 前端新增页面对照

| 操作 | 是否需要 |
|------|---------|
| 修改 `router/index.js` | ❌ 不需要（完全动态路由） |
| 修改 `router/componentMap.js` | ✅ 追加 1 行映射 |
| 创建 `views/xxx/index.vue` | ✅ 开发页面组件 |
| 创建 `api/xxx.js` | ✅ API 请求模块 |
| 声明 `defineOptions name` | ✅ 必须（keep-alive 缓存） |
| 后端插入 `sys_menu` 记录 | ✅ 菜单管理 |

### 8.3 后端 `sys_menu` 字段规范

| 字段 | 规范 | 示例 |
|------|------|------|
| `path` | 前端路由路径 | `/system/user` |
| `component` | views/ 下文件路径（不含 .vue） | `system/user/index` |
| `menuType` | 1=目录, 2=菜单, 3=按钮 | 路由只处理 type=2 |
| `icon` | Element Plus 图标名 | `UserFilled` |
| `permission` | 权限码 | `sys:user:query` |

---

## 9. 常见问题与解决方案

### 9.1 路由无限循环

**问题**：`[Vue Router warn]: No match found for location with path "/xxx"` 无限循环

**原因**：
- 点击了没有 `component` 的父级菜单路径
- 路由守卫中 `next({ ...to, replace: true })` 导致死循环

**解决方案**：
1. 搜索结果只收集有 `component` 的叶子菜单
2. 路由守卫中使用 `router.resolve()` 验证路径是否存在
3. 添加冷却时间保护机制

### 9.2 keep-alive 缓存失效

**问题**：切换标签页时重复请求业务数据

**原因**：动态路由的 `name` 与组件 `defineOptions name` 不匹配

**解决方案**：
- `componentMap` 中 `name` 字段必须与组件 `defineOptions name` 完全一致
- 使用英文 PascalCase 命名

### 9.3 刷新后路由丢失

**问题**：页面刷新后无法访问之前打开的页面

**原因**：`menus` 只存在 Pinia 内存中

**解决方案**：
- `menus`/`roles`/`perms` 持久化到 `localStorage`
- Store 初始化时从 `localStorage` 恢复

### 9.4 搜索面板样式错乱

**问题**：菜单名称竖排显示

**原因**：`flex: 1` 在窄面板中导致文字换行

**解决方案**：
- 使用 `white-space: nowrap` 禁止换行
- 用 `max-width` + `text-overflow: ellipsis` 控制溢出
- 图标设置 `flex-shrink: 0`

### 9.5 CSS 变量命名不生效

**问题**：`--text-color-secondary` 不生效

**原因**：项目变量名是 `--text-secondary`，不是 Element Plus 风格

**解决方案**：
- 严格使用项目定义的变量名
- 参考 `ui/src/styles/variables.scss` 中的变量列表

---

## 附录：检查清单

### 新页面开发完成前检查

- [ ] 后端实体继承 `BaseEntity`，添加 `@EqualsAndHashCode(callSuper = true)`
- [ ] 后端 Controller 添加 `@Tag` / `@Operation` 注解
- [ ] 后端接口添加 `@SaCheckPermission` 权限注解
- [ ] 后端使用构造器注入，不使用 `@Autowired`
- [ ] 前端组件声明 `defineOptions({ name: 'Xxx' })`
- [ ] 前端 `componentMap.js` 追加映射（key 与 `sys_menu.component` 一致）
- [ ] 前端样式使用 CSS 变量，不硬编码颜色值
- [ ] 前端 API 调用使用 `try/catch/finally`
- [ ] 前端删除操作有二次确认
- [ ] 前端表单有校验规则
- [ ] 前端弹窗关闭时重置表单
- [ ] 数据库中 `sys_menu` 表插入对应菜单记录
- [ ] 无未使用的 import 或变量
- [ ] 亮色/暗色主题均显示正常

---

> **文档维护**: 本文档基于 RX Admin 项目实践提炼，适用于所有基于 Spring Boot 3 + Vue 3 + Element Plus 的后台管理系统开发。
