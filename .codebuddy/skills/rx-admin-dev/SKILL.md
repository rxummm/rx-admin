---
name: rx-admin-dev
description: This skill should be used when developing features for the RX Admin project — a Spring Boot 3.5.x + Vue 3 admin management system. It enforces project-specific coding standards, tech stack constraints, and development workflows. Trigger when writing Java backend code (Controller/Service/Mapper/Entity/DTO/VO/Convert), Vue 3 frontend code (Composition API with Element Plus), SQL migrations, or adding new CRUD modules to this project.
allowed-tools:
disable: false
---

# RX Admin 开发技能

基于 Spring Boot 3 + Vue 3 的后台管理系统开发规范与工作流。

## 快速参考

| 项目 | 值 |
|------|-----|
| GroupId | `com.rx` |
| ArtifactId | `rx-admin` |
| Java | 17+ |
| Node | 18+ |
| 后端根包 | `com.rx.admin` |
| 前端根目录 | `ui/src/` |
| 前端 baseURL | `/api` |
| 数据库 | MySQL 8.0, `rx_admin`, utf8mb4 |
| 认证框架 | Sa-Token |
| ORM | MyBatis Plus 3.5.x + MapStruct 1.5.x |
| 对象转换 | MapStruct（`unmappedTargetPolicy = IGNORE`） |
| UI 框架 | Element Plus 2.4+ |
| API 文档 | Knife4j (OpenAPI 3) |
| 构建工具 | Maven 3.8+（含 build-helper-maven-plugin） |

完整技术栈参考: `references/tech-stack.md`

> **关联技能**: 前端 UI 设计请同时参考 `../frontend-design/SKILL.md`，该技能提供设计思维、字体选择、色彩搭配、动效与空间构图的方法论，与本技能的开发规范互补使用。

## 开发工作流 (新模块)

按以下 12 步顺序执行（含 DTO/VO/Convert 分层）:

1. **DDL SQL** → 创建数据库表 (`sys_` 前缀, 含 `deleted`/`create_time`/`update_time`)
2. **Entity** → `com.rx.admin.entity.{module}/`, 继承 `BaseEntity`, 加 `@EqualsAndHashCode(callSuper = true)` + `@TableName`
3. **Mapper** → `com.rx.admin.mapper.{module}/`, 继承 `BaseMapper<Entity>`, 加 `@Mapper`, 复杂 SQL 用 `@Select/@Update` 注解, **禁止创建 XML**
4. **DTO** → `com.rx.admin.modules.{domain}.{entity}.dto/`, 创建 `CreateDTO` / `UpdateDTO` / `QueryDTO`
5. **VO** → `com.rx.admin.modules.{domain}.{entity}.vo/`, 创建视图对象（响应用）
6. **Convert** → `com.rx.admin.modules.{domain}.{entity}.convert/`, MapStruct 接口, `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`
7. **Service 接口** → 继承 `IService<Entity>`, 分页返回 `PageResult<T>`, 方法参数使用 DTO
8. **ServiceImpl** → 继承 `ServiceImpl<Mapper, Entity>`, 使用构造器注入 (`private final`), **禁止 `@Autowired`**, 通过 Convert 做 DTO→Entity→VO 转换
9. **Controller** → `@Tag/@Operation` 注解, URL 前缀 `/api/{module}/{entity}`, `@SaCheckPermission`, DTO 入参 → VO 出参, **禁止 Entity 直接暴露**
10. **sys_menu 记录** → 插入菜单数据, `component` 字段与 `componentMap.js` key 一致
11. **API 模块 (前端)** → `ui/src/api/{module}.js`, 函数命名 `getXxxPage/addXxx/updateXxx/deleteXxx`
12. **Vue 页面** → `ui/src/views/{module}/index.vue`, `<script setup>`, `defineOptions({ name: 'Xxx' })`, 推荐 `useTablePage` composable
13. **componentMap** → `ui/src/router/componentMap.js` 追加 1 行映射
14. **验证** → 启动后端 → 启动前端 → 功能测试

详细模板参考: `references/code-templates.md`

## 后端强制约定

### Entity
```java
@Data
@EqualsAndHashCode(callSuper = true)     // 必须
@TableName("sys_xxx")                    // 必须
public class SysXxx extends BaseEntity { // 必须继承 BaseEntity
    // id, deleted, createTime, updateTime 由 BaseEntity 提供
    private String name;
    private Integer status;  // 1=启用, 0=禁用
}
```

### Mapper
```java
@Mapper
public interface SysXxxMapper extends BaseMapper<SysXxx> {
    @Select("SELECT ...")  // 复杂 SQL 用注解, 禁止 XML
    List<Xxx> customQuery(...);
}
```

### Service
```java
@Service
@RequiredArgsConstructor  // 推荐, 生成构造器
public class SysXxxServiceImpl extends ServiceImpl<SysXxxMapper, SysXxx> implements SysXxxService {
    private final SysXxxMapper mapper;  // 构造器注入, final
}
```

### Controller
```java
@Tag(name = "模块名")
@RestController
@RequestMapping("/api/{module}/{entity}")
@RequiredArgsConstructor
public class SysXxxController extends BaseCrudController<SysXxxService, SysXxx> {
    private final SysXxxConvert convert;

    public SysXxxController(SysXxxService service, SysXxxConvert convert) {
        super(service);
        this.convert = convert;
    }

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    @SaCheckPermission("module:entity:query")
    public Result<PageResult<SysXxxVO>> page(SysXxxQueryDTO dto) {
        IPage<SysXxx> page = service.pageQuery(dto);
        return Result.success(PageResult.of(page, convert::toVO));
    }

    @Operation(summary = "新增")
    @PostMapping
    @SaCheckPermission("module:entity:add")
    public Result<?> add(@RequestBody @Valid SysXxxCreateDTO dto) {
        return service.save(convert.toEntity(dto)) ? Result.success() : Result.fail();
    }
}
```

### DTO / VO / Convert (强制)

**禁止 Entity 直接暴露到 Controller 层**。必须使用 DTO 接收请求 + VO 返回响应 + MapStruct Convert 做转换。

```java
// DTO — 请求入参
// 模块路径: com.rx.admin.modules.{domain}.{entity}.dto/
@Data
public class SysXxxCreateDTO {
    @NotBlank private String name;
    private Integer status;
}

@Data
@EqualsAndHashCode(callSuper = true)
public class SysXxxQueryDTO extends PageDTO {
    private String keyword;
}

// VO — 响应出参
@Data
public class SysXxxVO {
    private Long id;
    private String name;
    private Integer status;
    private LocalDateTime createTime;
}

// Convert — MapStruct 转换器
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysXxxConvert {
    SysXxx toEntity(SysXxxCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SysXxxUpdateDTO dto, @MappingTarget SysXxx entity);

    SysXxxVO toVO(SysXxx entity);
    List<SysXxxVO> toVOList(List<SysXxx> list);
}
```

| 规则 | 必须 | 说明 |
|------|------|------|
| `unmappedTargetPolicy = IGNORE` | ✅ | 消除未映射字段编译警告 |
| `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` | ✅ | 更新时 null 不覆盖已有字段 |
| 禁止 `Mappers.getMapper()` | ✅ | 统一使用 Spring 注入 |

## 前端强制约定

- **命名**: `defineOptions({ name: 'XxxPage' })` (PascalCase, 与 componentMap 对应)
- **持久化**: 所有 localStorage 操作通过 `useStorage(STORAGE_KEYS.XXX)` 封装, 禁止原生 API
- **store**: Pinia Composition API, `defineStore('name', () => { ... })`
- **Axios**: 通过 `@/utils/request` 实例, GET 用 `params`, POST/PUT 用 `data`
- **定时轮询**: 传入 `{ _skipNProgress: true }` 避免进度条闪烁
- **CSS**: 使用 `var(--xxx)` CSS 变量, 禁止硬编码颜色
- **图标**: 自动按需导入 (unplugin-vue-components), 无需全量注册
- **语言切换**: `i18n.global.locale.value = 'en-US'`, 禁止 `window.location.reload()`
- **删除**: 必须 `ElMessageBox.confirm` 二次确认
- **表单**: 必须有 `rules` 校验规则, 弹窗关闭时重置
- **Element Plus 受控 prop**: 返回 prop 值的函数/计算属性（如 `ElTag` 的 `type`、`ElButton` 的 `type`、`ElAlert` 的 `type` 等）**禁止返回空字符串 `''` 或 `undefined`**，必须提供有效默认值（如 `'info'`、`'default'` 等），否则会触发控制台 prop validation 警告

## 权限码规范

`{module}:{entity}:{action}` — 如 `sys:user:query`, `sys:user:add`, `sys:user:edit`, `sys:user:delete`

## 安全规范

- **登录限流**: Guava RateLimiter, 每 IP 3次/秒
- **失败锁定**: 连续5次失败锁定30分钟
- **操作日志**: `@OperateLog` 注解, AOP 异步保存 (`@Async`), 参数脱敏 (过滤 password/token/secret)
- **密码加密**: BCryptPasswordEncoder
- **禁止技术**: Fastjson/Spring Security/Shiro/Vue 2/Vuex/Webpack

## 路由设计

- 完全动态路由: `constantRoutes` 只保留 Login + Layout 空壳
- 业务路由由 `sys_menu` 表驱动, 通过 `router.addRoute` 动态注入
- `componentMap.js` key 必须与 `sys_menu.component` 字段完全一致
- 路由守卫中 `dynamicRoutesAdded` 布尔标记防止重复注册

## 常见问题速查

| 问题 | 原因 | 解决 |
|------|------|------|
| 新功能左侧无菜单 | 未执行 `features_menu.sql` 插入 sys_menu | 执行 SQL 脚本后重启 |
| 登录后"系统繁忙" | 缺少 `sys_user_favorite` 等新表 | 执行 `features_init.sql` |
| 路由无限循环 | 父级菜单无 component 被搜索到 | 搜索仅收集叶子菜单 |
| keep-alive 缓存失效 | `componentMap.name` ≠ `defineOptions name` | 对齐 PascalCase 命名 |
| CSS 变量不生效 | 使用了 Element Plus 风格变量名 | 参考 `variables.scss` |

## 参考文档

- 完整开发规范: `references/dev-standards.md`
- 代码模板合集: `references/code-templates.md`
- 技术栈详解: `references/tech-stack.md`
- 组件映射表: `references/component-map.md`
- 项目优化建议: `references/optimization-plan.md`