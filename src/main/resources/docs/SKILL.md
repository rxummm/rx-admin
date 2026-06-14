# RX Admin — 开发规范与技能手册 (SKILL.md)

> **版本**: 1.5.0 | **更新日期**: 2026-06-13 | **适用对象**: 所有开发者
>
> **v1.5 更新**: Spring Boot 3.5.15 + MapStruct unmappedTargetPolicy 强制规范 + 构造器注入 + PageResult API 更新 + EmailService + 前端 composables 补充 + 新增模块开发示例

---

## 目录

1. [项目技术栈](#1-项目技术栈)
2. [后端开发规范](#2-后端开发规范)
   - [2.1 包结构约定](#21-包结构约定)
   - [2.2 依赖注入规范](#22-依赖注入规范)
   - [2.3 实体类规范](#23-实体类规范)
   - [2.4 Mapper 规范](#24-mapper-规范)
   - [2.5 Service 规范](#25-service-规范)
   - [2.6 Controller 规范](#26-controller-规范)
   - [2.7 DTO / VO / Convert 分层规范](#27-dto--vo--convert-分层规范)
   - [2.8 MapStruct 转换器强制规范](#28-mapstruct-转换器强制规范)
   - [2.9 统一响应规范](#29-统一响应规范)
   - [2.10 分页查询规范](#210-分页查询规范)
   - [2.11 异常处理规范](#211-异常处理规范)
   - [2.12 操作日志规范](#212-操作日志规范)
   - [2.13 数据权限规范](#213-数据权限规范)
   - [2.14 双数据源规范](#214-双数据源规范)
   - [2.15 密码与加密规范](#215-密码与加密规范)
   - [2.16 邮件发送规范](#216-邮件发送规范)
   - [2.17 代码生成器规范](#217-代码生成器规范)
3. [前端开发规范](#3-前端开发规范)
   - [3.1 目录结构约定](#31-目录结构约定)
   - [3.2 Vue 组件规范](#32-vue-组件规范)
   - [3.3 API 请求层规范](#33-api-请求层规范)
   - [3.4 路由配置规范](#34-路由配置规范)
   - [3.5 状态管理规范](#35-状态管理规范)
   - [3.6 国际化规范](#36-国际化规范)
   - [3.7 样式规范](#37-样式规范)
   - [3.8 组合式函数 (Composables)](#38-组合式函数-composables)
   - [3.9 表格页面标准模板](#39-表格页面标准模板)
   - [3.10 前端导出规范](#310-前端导出规范)
4. [Git 工作流规范](#4-git-工作流规范)
5. [代码审查清单](#5-代码审查清单)
6. [常见问题排查](#6-常见问题排查)
7. [新增模块完整示例](#7-新增模块完整示例)
   - [7.1 后端开发](#71-后端开发)
   - [7.2 前端开发](#72-前端开发)

---

## 1. 项目技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| **Java** | 17 | 运行环境 |
| **Spring Boot** | 3.5.15 | 应用框架 |
| **MyBatis Plus** | 3.5.5 | ORM |
| **MapStruct** | 1.5.5.Final | 对象映射 |
| **Sa-Token** | 1.37.0 | 认证授权 |
| **Knife4j** | 4.4.0 | API 文档 |
| **MySQL** | 8.x | 关系数据库 |
| **BCrypt** | (Spring Security) | 密码加密 |
| **Guava** | 33.0.0-jre | 限流 |
| **Caffeine** | (Spring Boot 内嵌) | 本地缓存 |
| **Spring Boot Mail** | — | 邮件发送 |
| **FastExcel** | 1.3.0 | Excel 解析 |
| **PDFBox** | 3.0.1 | PDF 操作 |
| **Jsoup** | 1.17.2 | HTML 解析 |
| **mp3agic** | 0.9.1 | MP3 元数据 |
| **JTOpen** | 20.0.8 | AS400 连接 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| **Vue 3** | ^3.4.0 | 前端框架 (Composition API) |
| **Vite** | ^5.0.10 | 构建工具 |
| **Vue Router** | ^4.2.5 | 路由 |
| **Pinia** | ^2.1.7 | 状态管理 |
| **Axios** | ^1.6.2 | HTTP 请求 |
| **Element Plus** | ^2.4.3 | UI 组件库 |
| **Vue I18n** | ^9.14.4 | 国际化 |
| **SCSS** | ^1.69.5 | CSS 预处理 |
| **ECharts** | ^6.1.0 | 图表 |
| **md-editor-v3** | ^6.5.1 | Markdown 编辑器 |
| **marked** | ^18.0.4 | Markdown 渲染 |
| **@vue-flow/core** | ^1.48.2 | 流程图引擎 |
| **@logicflow/core** | ^2.2.3 | 流程图引擎 |
| **@antv/x6** | ^3.1.7 | 流程图引擎 |
| **exceljs** | — | 前端 Excel 导出 |
| **NProgress** | ^0.2.0 | 进度条 |

### 开发工具

| 工具 | 说明 |
|------|------|
| **Maven** | 后端构建与依赖管理 |
| **build-helper-maven-plugin** | 声明 MapStruct generated-sources 为源码根 |
| **Lombok** | 减少样板代码 |
| **IntelliJ IDEA** | 推荐 IDE |
| **VS Code** | 前端开发 IDE |

---

## 2. 后端开发规范

### 2.1 包结构约定

```
com.rx.admin
├── RxAdminApplication.java              # 启动类（排除 DataSourceAutoConfiguration）
│
├── common/                               # 公共模块
│   ├── annotation/                       # 自定义注解
│   ├── result/                           # Result / PageResult
│   ├── exception/                        # 全局异常处理
│   ├── constant/                         # 常量
│   ├── utils/                            # 工具类
│   ├── security/                         # 安全组件（过滤器）
│   ├── base/                             # BaseEntity / BaseCrudController
│   ├── aspect/                           # AOP 切面
│   └── handler/                          # MyBatis TypeHandler / 拦截器
│
├── framework/                            # 框架层（基础设施配置）
│   ├── security/                         # SaTokenConfig / StpInterfaceImpl
│   ├── datasource/                       # PrimaryDataSourceConfig / SecondDataSourceConfig / @SecondDB
│   ├── mybatis/                          # MybatisPlusConfig / MetaObjectHandlerConfig
│   ├── async/                            # AsyncConfig
│   ├── cache/                            # CacheConfig
│   └── web/                              # CorsConfig / RateLimiterConfig
│
├── modules/                              # 业务模块层（领域化单体）
│   ├── system/                           # user/ role/ menu/ dept/ config/ dict/ ipRule/ file/ favorite
│   ├── monitor/                          # log/ loginlog/ job/ slowquery
│   ├── content/                          # notice/ message
│   └── as400/                            # techblog
│
├── entity/                               # 实体定义
├── controller/                           # 控制器
├── service/                              # 服务层
└── mapper/                               # 数据访问层
```

### 2.2 依赖注入规范

**强制使用构造器注入**，禁止使用 `@Autowired` 字段注入。

```java
// ✅ 正确：构造器注入
@RestController
public class SysUserController extends BaseCrudController<SysUserService, SysUser> {

    private final SysRoleService sysRoleService;

    public SysUserController(SysUserService sysUserService, SysRoleService sysRoleService) {
        super(sysUserService);
        this.sysRoleService = sysRoleService;
    }
}

// ❌ 错误：字段注入
@RestController
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;  // 禁止！
}
```

**BaseCrudController 基类同样使用构造器注入**：

```java
// common/base/BaseCrudController.java
public abstract class BaseCrudController<S extends IService<T>, T> {

    protected final S baseService;

    public BaseCrudController(S baseService) {
        this.baseService = baseService;
    }
}
```

### 2.3 实体类规范

```java
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;      // 唯一
    private String password;      // BCrypt 加密
    private String nickname;
    private String email;         // AES 加密存储

    @TableField(exist = false)
    private List<Long> roleIds;   // 非数据库字段
}
```

**命名约定**:
- 表名前缀: `sys_`（系统表）、`tech_blog_`（技术博客）、`honglou_`/`sanguo_`/`shuihu_`/`xiyou_`（四大名著）、`literature_`（历代文学）
- 实体类名: 驼峰式，与表名对应
- 公共字段: `id`, `deleted`, `createTime`, `updateTime`（继承 `BaseEntity`）
- 非数据库字段: 使用 `@TableField(exist = false)` 标记

### 2.4 Mapper 规范

```java
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 自定义 SQL 写在对应的 XML 中
    // resources/mapper/SysUserMapper.xml
}
```

**规则**:
- 必须标记 `@Mapper` 注解
- 第二数据源（四大名著）需额外标记 `@SecondDB` 注解
- XML 文件放在 `src/main/resources/mapper/` 目录下

### 2.5 Service 规范

```java
public interface SysUserService extends IService<SysUser> {
    void assignRoles(Long userId, List<Long> roleIds);
    PageResult<SysUser> queryPage(SysUserQueryDTO dto);
}

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    // 业务逻辑实现
}
```

**规则**:
- 接口继承 `IService<T>`，实现类继承 `ServiceImpl<M, T>`
- 复杂的业务逻辑写在 Service 层，Controller 层保持简洁
- 事务操作使用 `@Transactional(rollbackFor = Exception.class)`

### 2.6 Controller 规范

```java
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends BaseCrudController<SysUserService, SysUser> {

    private final SysRoleService sysRoleService;

    public SysUserController(SysUserService sysUserService, SysRoleService sysRoleService) {
        super(sysUserService);
        this.sysRoleService = sysRoleService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysUserVO>> page(SysUserQueryDTO dto) {
        return Result.ok(baseService.queryPage(dto));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody SysUserCreateDTO dto) {
        baseService.save(userConvert.toEntity(dto));
        return Result.ok();
    }
}
```

**规则**:
- 类名: `{模块}Controller`，路径前缀: `/sys/{模块}` 或 `/api/{模块}`
- 使用 `@Valid` 进行参数校验
- 公共 CRUD 操作继承 `BaseCrudController`（构造器注入）
- 入参使用 DTO，出参使用 VO

### 2.7 DTO / VO / Convert 分层规范

| 层级 | 包路径 | 后缀 | 职责 |
|------|--------|------|------|
| **DTO** | `modules/{domain}/{entity}/dto/` | `CreateDTO` / `UpdateDTO` / `QueryDTO` | 请求参数封装 |
| **VO** | `modules/{domain}/{entity}/vo/` | `VO` | 响应视图对象 |
| **Convert** | `modules/{domain}/{entity}/convert/` | `Convert` | MapStruct 对象转换 |

**DTO 设计原则**：
- CreateDTO 包含新增必需的字段
- UpdateDTO 包含可修改的字段
- QueryDTO 包含查询/分页条件
- 均使用 `@Data` 注解

**VO 设计原则**：
- 排除敏感字段（如 password）
- 可包含关联数据的展示字段（如 deptName 代替 deptId）
- 使用 `@Data` 注解

### 2.8 MapStruct 转换器强制规范

**所有 Convert 接口严格遵守以下规范**：

```java
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvert {

    SysUser toEntity(SysUserCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SysUserUpdateDTO dto, @MappingTarget SysUser entity);

    SysUserVO toVO(SysUser entity);

    List<SysUserVO> toVOList(List<SysUser> list);
}
```

**强制规则**：

| 规则 | 说明 |
|------|------|
| `componentModel = "spring"` | 必须指定，让 MapStruct 生成 Spring Bean |
| `unmappedTargetPolicy = ReportingPolicy.IGNORE` | **必须添加**，忽略 DTO 中不存在的目标字段（如 `id`、`deleted`、`createTime`），避免编译警告 |
| `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` | updateEntity 方法**必须添加**，支持部分更新（只更新非 null 字段） |
| `@MappingTarget` | updateEntity 方法的第二个参数**必须标注** |
| 禁止使用 `Mappers.getMapper()` | 始终通过 Spring 注入使用 Convert |

**完整标准模板**：

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface XxxConvert {

    XxxEntity toEntity(XxxCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(XxxUpdateDTO dto, @MappingTarget XxxEntity entity);

    XxxVO toVO(XxxEntity entity);

    List<XxxVO> toVOList(List<XxxEntity> list);
}
```

### 2.9 统一响应规范

```java
// 成功响应
Result.ok(data)
Result.ok()

// 失败响应
Result.fail("错误信息")
Result.fail(ResultCode.xxx)

// 响应结构
{
    "code": 200,
    "msg": "操作成功",
    "data": { ... }
}
```

### 2.10 分页查询规范

```java
// ✅ 推荐：使用 PageResult.of(IPage) 方法
@GetMapping("/page")
public Result<PageResult<SysUserVO>> page(SysUserQueryDTO dto) {
    IPage<SysUser> page = baseService.page(
        new Page<>(dto.getPage(), dto.getPageSize()),
        buildQueryWrapper(dto)
    );
    return Result.ok(PageResult.of(page));
}

// ✅ 也可使用：PageResult.of(long, long, long, List)
PageResult<SysUserVO> result = PageResult.of(total, page, pageSize, voList);
```

**前端分页参数约定**:
- 页码: `page` (从 1 开始)
- 每页条数: `pageSize` (默认 10)
- 响应字段: `records`, `total`, `page`, `size`

### 2.11 异常处理规范

所有异常由 `GlobalExceptionHandler` 统一处理，不要在 Controller 中 try-catch 业务异常。

```java
// ✅ 正确：抛出业务异常
if (user == null) {
    throw new BusinessException("用户不存在");
}

// ❌ 错误：在 Controller 中 try-catch
try {
    // ...
} catch (Exception e) {
    return Result.fail(e.getMessage());
}
```

### 2.12 操作日志规范

```java
// 使用 @OperateLog 注解记录操作日志
@OperateLog(module = "用户管理", operation = "新增用户")
@PostMapping
public Result<Void> add(@Valid @RequestBody SysUserCreateDTO dto) {
    // ...
}
```

日志通过 `@Async` 异步记录，不阻塞主线程。敏感字段（password 等）自动脱敏。

### 2.13 数据权限规范

```java
// 使用 @DataScope 注解进行数据权限过滤
@GetMapping("/page")
@DataScope(deptAlias = "d", userAlias = "u")
public Result<PageResult<SysUserVO>> page(SysUserQueryDTO dto) {
    // ...
}
```

### 2.14 双数据源规范

| 数据源 | 注解 | 数据库 | 用途 |
|--------|------|--------|------|
| 主数据源 | 无需额外注解 | `rx_admin` | 系统管理表 |
| 第二数据源 | `@SecondDB` | `rxusysadmin` | 四大名著业务表 |

```java
// 使用第二数据源的 Mapper 必须标注 @SecondDB
@Mapper
@SecondDB
public interface HonglouCharacterMapper extends BaseMapper<HonglouCharacter> {
}
```

### 2.15 密码与加密规范

- **密码**: 使用 `BCryptPasswordEncoder` 加密，不允许明文存储
- **邮箱/手机号**: 使用 `AesTypeHandler` AES 加密存储
- **登录失败追踪**: 5 次失败锁定 30 分钟 (LoginAttemptService)

### 2.16 邮件发送规范

```java
@Service
public class EmailService {
    // 通过 Spring Boot Mail JavaMailSender 发送
    // 支持 HTML 富文本内容
    // 发送历史记录到 sys_notify_record 表
}
```

### 2.17 代码生成器规范

使用内置代码生成器（`/api/tool/gen`）可快速生成：
- Entity / Mapper / Mapper.xml / Service / ServiceImpl / Controller
- 前端 Vue 页面 + API 模块

生成后需手动调整：
- 添加 `unmappedTargetPolicy = ReportingPolicy.IGNORE` 到 Convert
- 将 `@Autowired` 字段注入改为构造器注入
- 调整 DTO 字段名与 Entity 对齐

---

## 3. 前端开发规范

### 3.1 目录结构约定

```
ui/src/
├── api/                    # API 请求模块（50+ 个文件）
│   ├── modules/            # 模块化聚合入口（v3 新增）
│   │   ├── auth/           # 认证模块 API
│   │   ├── system/         # 系统管理模块 API
│   │   ├── monitor/        # 系统监控模块 API
│   │   ├── content/        # 内容管理模块 API
│   │   ├── tool/           # 工具集模块 API
│   │   ├── as400/          # AS400 模块 API
│   │   └── classics/       # 四大名著模块 API
│   ├── auth.js / user.js / role.js / menu.js / dept.js
│   ├── dict.js / notice.js / log.js / online.js
│   ├── dashboard.js / analysis.js / region.js
│   ├── as400.js / iService.js / techBlog.js
│   ├── music.js / commonTools.js / permission.js
│   ├── job.js / file.js / slowQuery.js
│   ├── health.js / loginLog.js / exportLog.js / jobLog.js
│   ├── message.js / favorite.js / ipRule.js
│   ├── cacheManage.js / notifyCenter.js / devTools.js / dbTool.js
│   ├── gen.js / importData.js / logAnalysis.js
│   └── apiDebug.js / backup.js / announcement.js / export.js
├── composables/            # 组合式函数
│   ├── useStorage.js       # localStorage 统一管理
│   ├── useTablePage.js     # 通用表格分页
│   ├── useTheme.js         # 主题切换
│   ├── useMenuI18n.js      # 菜单国际化
│   ├── usePasswordStrength.js # 密码强度检测
│   ├── useTableHeight.js   # 表格高度自适应
│   └── useLayoutSettings.js # 布局设置
├── i18n/                   # 国际化
│   ├── index.js
│   └── lang/
│       ├── zh-CN.js        # 中文语言包
│       └── en-US.js        # 英文语言包
├── layout/                 # 布局组件
│   ├── index.vue           # 主布局
│   ├── SubMenu.vue         # 递归子菜单
│   ├── TagsView.vue        # 标签页导航
│   ├── SearchBox.vue       # 全局搜索框
│   └── NoticePopover.vue   # 通知公告弹窗
├── components/             # 公共组件
│   ├── CommandPalette.vue  # Ctrl+K 命令面板
│   ├── FavoriteStar.vue    # 收藏星标
│   ├── FavoritesPanel.vue   # 收藏面板
│   ├── AnnouncementPopup.vue # 公告弹窗
│   └── ExportButton/       # 导出按钮组件
├── router/
│   ├── index.js            # 路由配置（动态路由）
│   └── componentMap.js     # 组件映射表（50+ 条目）
├── stores/                 # Pinia 状态管理
│   ├── user.js             # 用户状态
│   └── tags.js             # 标签页状态
├── styles/                 # 全局样式
│   ├── global.scss         # 全局样式
│   ├── variables.scss      # CSS 变量（亮/暗双主题）
│   └── themes.scss         # 5套主题色
├── utils/
│   ├── request.js          # Axios 封装
│   └── index.js            # 工具函数
└── views/                  # 页面视图（50+ 个页面）
    ├── login/
    ├── dashboard/          # 仪表盘 + 知识图谱
    ├── profile/
    ├── system/             # 用户/角色/菜单/部门/配置/文件/IP规则
    ├── tool/               # 字典/行政区划/接口分析/代码生成/批量导入/API调试/数据备份/数据库工具/开发工具/邮件发送/Excel解析/文档转换/文档共享/流程图/音乐播放器/项目文档/开发规范
    ├── content/            # 通知公告/消息中心/通知中心
    ├── monitor/            # 操作日志/在线用户/定时任务/慢查询/健康/日志分析/登录日志/导出审计/任务日志/缓存管理
    ├── permission/         # 权限申请
    ├── as400/              # 对象浏览/IService/技术博客
    └── classics/           # 红楼梦/三国/水浒/西游/历代文学
```

### 3.2 Vue 组件规范

```vue
<script setup>
// 1. 导入依赖
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getPage, add, update, remove } from '@/api/moduleName'

// 2. 组合式函数
const { t } = useI18n()
const router = useRouter()

// 3. 响应式数据
const loading = ref(false)
const tableData = ref([])

// 4. 方法
const fetchData = async () => { /* ... */ }

// 5. 生命周期
onMounted(() => { fetchData() })
</script>

<template>
  <!-- 使用 t() 函数做国际化 -->
  <el-button>{{ t('common.search') }}</el-button>
</template>

<style scoped lang="scss">
// 嵌套层级不超过 4 层
</style>
```

**命名约定**:
- 组件名: PascalCase (如 `UserList.vue`)
- 页面组件: 放在 `views/` 下，使用 `index.vue` 作为入口
- 事件处理: `handleXxx` (如 `handleSearch`, `handleDelete`)
- 数据获取: `fetchXxx` (如 `fetchData`, `fetchDetail`)
- 布尔变量: `isXxx` / `hasXxx` (如 `isLoading`, `hasPermission`)

### 3.3 API 请求层规范

**API 模块文件** (`api/xxx.js`):

```javascript
import request from '@/utils/request'

export function getPage(params) {
  return request({ url: '/api/module', method: 'get', params })
}

export function getById(id) {
  return request({ url: `/api/module/${id}`, method: 'get' })
}

export function add(data) {
  return request({ url: '/api/module', method: 'post', data })
}

export function update(data) {
  return request({ url: '/api/module', method: 'put', data })
}

export function remove(id) {
  return request({ url: `/api/module/${id}`, method: 'delete' })
}
```

**规则**:
- 一个 API 文件对应一个后端 Controller
- 使用 `request.js` 封装的 Axios 实例（自动处理 Token/心跳/错误）
- URL 不以 `/` 结尾
- 导入路径统一使用 `@/api/` 别名

### 3.4 路由配置规范

项目采用**完全动态路由**，所有业务路由在登录后从后端菜单树动态生成。

```javascript
// router/index.js
const constantRoutes = [
  { path: '/login', component: () => import('@/views/login/index.vue') },
  { path: '/', component: Layout, redirect: '/dashboard' }
]

// 登录后动态注入
await generateDynamicRoutes()
```

**componentMap 需要维护**：新增页面时，必须在 `router/componentMap.js` 中添加映射项。

```javascript
// router/componentMap.js
export const componentMap = {
  // 格式: '菜单路径': () => import('组件路径')
  'dashboard': () => import('@/views/dashboard/index.vue'),
  'system/user': () => import('@/views/system/user/index.vue'),
  // ... 50+ 个映射项
}
```

### 3.5 状态管理规范

```javascript
// stores/user.js
import { defineStore } from 'pinia'
import { useStorage } from '@/composables/useStorage'

export const useUserStore = defineStore('user', () => {
  const token = useStorage('token', '')
  const userInfo = ref(null)

  const login = async (credentials) => { /* ... */ }
  const logout = () => { /* ... */ }

  return { token, userInfo, login, logout }
})
```

**规则**:
- 使用 Setup Store 语法（推荐）
- Token 使用 `useStorage` 持久化到 localStorage
- Store 之间不互相引用，防止循环依赖

### 3.6 国际化规范

```javascript
// 使用 useI18n
const { t } = useI18n()

// 模板中使用
{{ t('user.username') }}

// 脚本中使用
ElMessage.success(t('common.addSuccess'))
```

**规则**:
- 所有用户可见文本必须使用 `t()` 函数
- 语言包 key 使用点号分隔：`模块.字段`
- 新增文案需同时添加到 `zh-CN.js` 和 `en-US.js`

### 3.7 样式规范

```scss
// 使用 CSS 变量支持主题切换
.page-container {
  background-color: var(--bg-color);
  color: var(--text-color);
  padding: 16px;
}

// 暗黑模式
html.dark {
  --bg-color: #1a1a2e;
  --text-color: #e0e0e0;
}
```

**规则**:
- 使用 SCSS 编写样式
- 颜色使用 CSS 变量，支持亮/暗双主题
- 使用 `<style scoped>` 避免样式污染
- 全局样式放在 `styles/global.scss`

### 3.8 组合式函数 (Composables)

| 函数 | 用途 |
|------|------|
| `useStorage(key, defaultValue)` | localStorage 统一管理，支持 ref 响应式 |
| `useTablePage(fetchFn)` | 通用表格分页（loading/数据/分页参数/搜索/重置） |
| `useTheme()` | 主题切换（亮/暗 + 5 套主题色） |
| `useMenuI18n()` | 菜单国际化名称映射 |
| `usePasswordStrength()` | 密码强度检测（弱/中/强） |
| `useTableHeight()` | 表格高度自适应（减去搜索栏/分页栏高度） |
| `useLayoutSettings()` | 布局设置（侧边栏折叠/标签页/固定顶栏） |

**useTablePage 使用示例**：

```javascript
import { useTablePage } from '@/composables/useTablePage'
import { getPage } from '@/api/moduleName'

const { loading, tableData, total, queryParams, handleSearch, handleReset, handlePageChange } =
  useTablePage(getPage, { status: '' })
```

### 3.9 表格页面标准模板

```vue
<script setup>
import { useTablePage } from '@/composables/useTablePage'
import { getPage, add, update, remove } from '@/api/moduleName'
import { ElMessage, ElMessageBox } from 'element-plus'

const { loading, tableData, total, queryParams, handleSearch, handleReset, handlePageChange } =
  useTablePage(getPage, { name: '' })

const dialogVisible = ref(false)
const formData = ref({})

const handleAdd = () => { dialogVisible.value = true; formData.value = {} }
const handleEdit = (row) => { dialogVisible.value = true; formData.value = { ...row } }
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除？')
  await remove(id)
  ElMessage.success('删除成功')
  handleSearch()
}
const handleSubmit = async () => {
  await (formData.value.id ? update(formData.value) : add(formData.value))
  ElMessage.success('保存成功')
  dialogVisible.value = false
  handleSearch()
}
</script>

<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="queryParams.name" placeholder="名称" clearable />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleAdd">新增</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="page-pagination">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        @change="handlePageChange"
      />
    </div>
    <el-dialog v-model="dialogVisible" title="表单">
      <el-form :model="formData">
        <el-form-item label="名称">
          <el-input v-model="formData.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>
```

### 3.10 前端导出规范

```javascript
// 使用 ExportButton 组件
import ExportButton from '@/components/ExportButton/index.vue'

// 或使用 exceljs 直接导出
import ExcelJS from 'exceljs'
```

---

## 4. Git 工作流规范

### 分支命名

| 分支 | 用途 |
|------|------|
| `main` | 生产分支，仅通过 PR 合并 |
| `develop` | 开发分支 |
| `feature/xxx` | 功能分支 |
| `bugfix/xxx` | Bug 修复分支 |
| `hotfix/xxx` | 紧急修复分支 |

### 提交信息

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**type 类型**: feat, fix, docs, style, refactor, perf, test, chore, build, ci

### 提交前检查

- 代码格式化
- 无 ESLint 错误
- 无编译错误
- 无 MapStruct "Unmapped target properties" 警告

---

## 5. 代码审查清单

### 后端

- [ ] 使用构造器注入，无 `@Autowired` 字段注入
- [ ] Convert 接口已添加 `unmappedTargetPolicy = ReportingPolicy.IGNORE`
- [ ] updateEntity 方法已添加 `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`
- [ ] 入参使用 DTO，出参使用 VO
- [ ] 分页使用 `PageResult.of(IPage)` 方法
- [ ] 异常不通过 try-catch 返回 Result.fail
- [ ] 敏感字段（密码）不返回给前端
- [ ] 第二数据源 Mapper 已标注 `@SecondDB`
- [ ] 无 MapStruct 编译警告
- [ ] 无 IDE 假阳性错误（build-helper-maven-plugin 已配置）

### 前端

- [ ] 所有用户可见文本使用 `t()` 国际化
- [ ] 新增页面已在 `componentMap.js` 注册
- [ ] 使用 `useTablePage` 组合式函数处理分页
- [ ] API 请求使用 `@/utils/request` 封装
- [ ] 样式使用 CSS 变量支持主题切换
- [ ] 使用 `<style scoped>` 避免样式污染
- [ ] 无 ESLint 错误

---

## 6. 常见问题排查

### MapStruct 编译错误

**问题**: `No implementation was created for XxxConvert due to having a problem in the erroneous element`

**解决**:
1. 检查 DTO/VO 字段名是否与 Entity 完全一致
2. 确保 Convert 接口添加了 `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`
3. 执行 `mvn clean compile`

### IDE 无法识别 MapStruct 生成代码

**问题**: `The import com.rx.admin.modules.xxx.convert.XxxConvertImpl cannot be resolved`

**解决**:
1. 确认 `pom.xml` 已配置 `build-helper-maven-plugin` 声明 `generated-sources` 目录
2. 执行 `mvn clean compile`
3. 在 VS Code 中执行 `Java: Clean Java Language Server Workspace`

### MapStruct "Unmapped target properties" 警告

**解决**: 为所有 Convert 接口添加 `unmappedTargetPolicy = ReportingPolicy.IGNORE`

### 前端路由不生效

**解决**:
1. 检查 `componentMap.js` 中是否已注册组件映射
2. 确认后端菜单表中 `component` 字段与 `componentMap` key 一致
3. 检查浏览器控制台是否有 404 错误

---

## 7. 新增模块完整示例

以新增 "技术博客文章" 模块为例（已实现，仅作参考）。

### 7.1 后端开发

**Step 1: 创建 Entity**

```java
// entity/TechBlogArticle.java
@Data
@TableName("tech_blog_article")
public class TechBlogArticle extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String slug;
    private String sourceUrl;
    private String author;
    private LocalDate publishDate;
    private String categories;
    private String excerptText;
    private String contentHtml;
    private String contentText;
    private String coverImage;
    private Integer sort;
    private Integer viewCount;
    private String source;
}
```

**Step 2: 创建 Mapper**

```java
@Mapper
public interface TechBlogArticleMapper extends BaseMapper<TechBlogArticle> {
}
```

**Step 3: 创建 Service**

```java
public interface TechBlogArticleService extends IService<TechBlogArticle> {
    PageResult<TechBlogVO> queryPage(TechBlogQueryDTO dto);
}

@Service
public class TechBlogArticleServiceImpl extends ServiceImpl<TechBlogArticleMapper, TechBlogArticle>
    implements TechBlogArticleService {
    // ...
}
```

**Step 4: 创建 DTO / VO / Convert**

```java
// modules/as400/techblog/dto/TechBlogCreateDTO.java
@Data
public class TechBlogCreateDTO {
    private String title;
    private String slug;
    private String sourceUrl;
    // ...
}

// modules/as400/techblog/vo/TechBlogVO.java
@Data
public class TechBlogVO {
    private Long id;
    private String title;
    // ...
}

// modules/as400/techblog/convert/TechBlogConvert.java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechBlogConvert {
    TechBlogArticle toEntity(TechBlogCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TechBlogUpdateDTO dto, @MappingTarget TechBlogArticle entity);

    TechBlogVO toVO(TechBlogArticle entity);
    List<TechBlogVO> toVOList(List<TechBlogArticle> list);
}
```

**Step 5: 创建 Controller**

```java
@RestController
@RequestMapping("/api/techblog")
public class TechBlogController extends BaseCrudController<TechBlogArticleService, TechBlogArticle> {

    private final TechBlogConvert techBlogConvert;

    public TechBlogController(TechBlogArticleService service, TechBlogConvert techBlogConvert) {
        super(service);
        this.techBlogConvert = techBlogConvert;
    }

    @GetMapping("/articles")
    public Result<PageResult<TechBlogVO>> page(TechBlogQueryDTO dto) {
        IPage<TechBlogArticle> page = baseService.queryPage(dto);
        return Result.ok(PageResult.of(page));
    }

    @PostMapping("/articles")
    public Result<Void> add(@Valid @RequestBody TechBlogCreateDTO dto) {
        baseService.save(techBlogConvert.toEntity(dto));
        return Result.ok();
    }
}
```

### 7.2 前端开发

**Step 1: 创建 API 模块**

```javascript
// api/techBlog.js
import request from '@/utils/request'

export function getArticlePage(params) {
  return request({ url: '/api/techblog/articles', method: 'get', params })
}

export function addArticle(data) {
  return request({ url: '/api/techblog/articles', method: 'post', data })
}

export function updateArticle(data) {
  return request({ url: '/api/techblog/articles', method: 'put', data })
}

export function deleteArticle(id) {
  return request({ url: `/api/techblog/articles/${id}`, method: 'delete' })
}
```

**Step 2: 创建页面组件**

```vue
<!-- views/as400/techblog/index.vue -->
<script setup>
import { useTablePage } from '@/composables/useTablePage'
import { getArticlePage, addArticle, updateArticle, deleteArticle } from '@/api/techBlog'
// ... 标准表格页面模板
</script>
```

**Step 3: 注册路由映射**

```javascript
// router/componentMap.js
export const componentMap = {
  // ... 现有映射
  'as400/techblog': () => import('@/views/as400/techblog/index.vue'),
  'as400/techblog/detail': () => import('@/views/as400/techblog/detail.vue'),
}
```

**Step 4: 在数据库 sys_menu 表中添加菜单记录**

```sql
INSERT INTO sys_menu (name, path, component, icon, parent_id, type, permission, sort)
VALUES ('技术博客', '/techblog', 'as400/techblog', 'Document', 9, 'menu', 'techblog:view', 3);
```

---

> **文档维护**: 本文档为 RX Admin 项目开发规范与技能手册，所有开发者必须遵守。
> **历史版本**: v1.4.0 (2026-06-10) → v1.5.0 (2026-06-13): Spring Boot 3.5.15 + MapStruct unmappedTargetPolicy 强制规范 + 构造器注入 + PageResult API 更新 + EmailService + 前端 composables + TechBlog 完整示例