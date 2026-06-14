# RX Admin 开发规范与技能手册

> **版本**: 1.5.0 | **更新日期**: 2026-06-15 | **适用项目**: 基于 Spring Boot 3 + Vue 3 的后台管理系统
>
> **v1.5 更新**: Spring Boot 3.5.15 + MapStruct unmappedTargetPolicy 强制规范 + 构造器注入 + PageResult API 更新 + EmailService + 新 composables + 主题色统一 + Sentry 升级 + 字体自托管 + 构建优化

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
10. [项目优化建议](#10-项目优化建议)

---

## 1. 技术栈选型标准

### 1.1 强制技术栈

| 层级 | 技术 | 版本要求 | 说明 |
|------|------|---------|------|
| **运行环境** | Java / Node.js | Java 17+ / Node 18+ | LTS 版本 |
| **后端框架** | Spring Boot | 3.5.x | Jakarta EE 9+ |
| **ORM** | MyBatis Plus | 3.5.x | 继承 `BaseMapper<T>` + `ServiceImpl<M, T>` |
| **安全认证** | Sa-Token | 1.37+ | 替代 Spring Security / Shiro |
| **API 文档** | Knife4j | 4.4+ | OpenAPI 3 规范，`@Tag` / `@Operation` 注解 |
| **数据库** | MySQL | 8.0+ | utf8mb4 字符集 |
| **密码加密** | BCryptPasswordEncoder | — | Spring Security Crypto |
| **限流** | Guava RateLimiter | 33.0+ | 登录接口每秒 3 次限制 |
| **本地缓存** | Caffeine | 3.x (Spring Boot 内嵌) | 系统配置、菜单树高频数据缓存 |
| **前端框架** | Vue 3 (Composition API) | ^3.4.0 | `<script setup>` 语法 |
| **构建工具** | Vite | ^5.0 | 替代 webpack |
| **路由** | Vue Router | ^4.2 | 动态路由 + `addRoute` |
| **状态管理** | Pinia | ^2.1 | Composition API 风格 |
| **HTTP 客户端** | Axios | ^1.6 | 统一拦截器封装 |
| **UI 组件库** | Element Plus | ^2.4 | 全量引入 + 暗黑模式 |
| **CSS 预处理** | SCSS (sass-embedded) | ^1.69 | 全局变量注入（替代 sass）|
| **国际化** | Vue I18n | ^9.14 | Composition API 模式 |
| **进度条** | NProgress | ^0.2 | 路由切换进度条 |
| **图表** | ECharts | ^6.1 | 仪表盘/知识图谱/日志分析/健康监控 |
| **错误监控** | @sentry/vue | ^10.0 | Sentry v10 + browserTracingIntegration |
| **自托管字体** | @fontsource/dm-sans / ibm-plex-sans / jetbrains-mono | ^5.x | 替代 Google Fonts CDN |

| **对象映射** | MapStruct | 1.5.x | 编译期对象转换，`unmappedTargetPolicy = IGNORE` |
| **邮件服务** | Spring Boot Mail | — | SMTP 邮件发送 |
| **Maven 插件** | build-helper-maven-plugin | 3.x | 声明 MapStruct generated-sources 为源码根 |
| **CSS 预处理** | SCSS (sass-embedded) | ^1.69 | 替代 sass（Dart Sass） |

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

**包结构**（`com.rx.admin`，v3 领域化单体）：

```
com.rx.admin
├── RxAdminApplication.java           # 启动类（排除 DataSourceAutoConfiguration）
├── common/                            # 公共模块（按职责拆分子包）
│   ├── annotation/                    # @OperateLog, @DataScope
│   ├── result/                        # Result<T>, PageResult<T>
│   ├── exception/                     # GlobalExceptionHandler（10种异常）
│   ├── constant/                      # PageConstants
│   ├── utils/                         # CaptchaUtil, DataMaskUtil
│   ├── security/                      # IpFilter, NotLoginFilter, ReplayAttackFilter, XssJacksonConfig
│   ├── base/                          # BaseEntity, BaseCrudController（构造器注入）
│   ├── aspect/                        # OperateLogAspect（@Async 异步 + 参数脱敏）
│   └── handler/                       # AesTypeHandler, DataScopeInnerInterceptor, SlowQueryInterceptor
├── framework/                         # 框架层配置（Spring Boot 自动装配）
│   ├── datasource/                    # PrimaryDataSourceConfig / SecondDataSourceConfig / @SecondDB
│   ├── mybatis/                       # MybatisPlusConfig / MetaObjectHandlerConfig
│   ├── security/                      # SaTokenConfig / StpInterfaceImpl（双源合并权限）
│   ├── async/                         # AsyncConfig
│   ├── cache/                         # CacheConfig（Caffeine）
│   └── web/                           # CorsConfig / RateLimiterConfig
├── modules/                           # ⭐ 业务模块层（领域化 DTO/VO/Convert）
│   ├── system/user/ role/ menu/ dept/ config/ dict/ ipRule/ file/ favorite/    # dto/ vo/ convert/
│   ├── monitor/log/ loginlog/ job/ slowquery/                                   # vo/ convert/
│   ├── content/notice/ message/                                                  # dto/ vo/ convert/
│   └── as400/techblog/                                                          # dto/ vo/ convert/
├── entity/                            # 实体定义（共用）
├── controller/                        # 控制器（共用，DTO 入参 → VO 出参）
├── service/                           # 服务层（共用）
└── mapper/                            # 数据访问层（共用，禁止 XML）
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
        // 推荐使用补全分页信息的方法
        return PageResult.of(pageResult.getTotal(), page, size, pageResult.getRecords());
        // 或直接从 MyBatis Plus Page 转换
        // return PageResult.of(pageResult);
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

### 2.5 DTO / VO / Convert 分层规范

#### MapStruct 转换器强制规范

所有 Convert 接口统一使用以下注解配置：

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

**强制规则**：

| 规则 | 说明 |
|------|------|
| `componentModel = "spring"` | 必须指定，让 MapStruct 生成 Spring Bean |
| `unmappedTargetPolicy = ReportingPolicy.IGNORE` | **必须添加**，忽略未映射字段编译警告 |
| `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` | 更新时 **必须添加**，null 值不覆盖已有字段 |
| `@MappingTarget` | 更新时 **必须标注**，在原对象上修改 |
| 禁止 `Mappers.getMapper()` | 统一使用 Spring 注入 |
| 禁止 `BeanUtils.copyProperties` | 全部通过 MapStruct Convert 转换 |
| 禁止 Entity 直接暴露 | DTO 入参 + VO 出参，Entity 仅 Service 层可见 |

### 2.6 Controller 层规范

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
3. **依赖注入**：构造器注入（`private final` + 构造函数），推荐使用 Lombok `@RequiredArgsConstructor`
4. **分页接口**：`GET /page?page=1&size=10&keyword=xxx`
5. **统一返回**：`Result.success(data)` / `Result.error(msg)`
6. **权限注解**：每个接口添加 `@SaCheckPermission("module:entity:action")`
7. **API 文档**：`@Tag(name)` 分组 + `@Operation(summary)` 描述
8. **批量删除**：路径参数 `{ids}` 接收 `List<Long>`
9. **限流**：关键接口（如登录）使用 `RateLimiterConfig` 提供的 `ConcurrentHashMap<String, RateLimiter>` 按 IP 限流
10. **登录安全**：`AuthController` 集成 `LoginAttemptService` 失败锁定检查

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
@Slf4j
public class GlobalExceptionHandler {

    // 401 未登录
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleNotLogin(NotLoginException e) {
        return Result.fail("未登录或登录已过期");
    }

    // 403 无权限
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleNotPermission(NotPermissionException e) {
        return Result.fail("无此权限");
    }

    // 400 参数校验失败
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(Exception e) {
        return Result.fail("参数校验失败");
    }

    // 404 资源不存在
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNotFound(NoResourceFoundException e) {
        return Result.fail("资源不存在");
    }

    // 405 方法不允许
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.fail("请求方法不允许");
    }

    // 415 媒体类型不支持
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return Result.fail("不支持的媒体类型");
    }

    // 数据库约束冲突
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据完整性异常", e);
        return Result.fail("数据操作冲突，请检查数据");
    }

    // 兜底异常（生产环境不泄露错误信息）
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统繁忙，请稍后再试");
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

### 2.9 安全规范

#### 登录限流与失败锁定

```java
// RateLimiterConfig.java — Guava RateLimiter，按 IP 区分
@Configuration
public class RateLimiterConfig {
    private static final double LOGIN_RATE_PER_SECOND = 3.0;

    @Bean
    public ConcurrentHashMap<String, RateLimiter> rateLimiters() {
        return new ConcurrentHashMap<>();
    }

    // 使用: rateLimiters.computeIfAbsent(ip, k -> RateLimiter.create(LOGIN_RATE_PER_SECOND)).tryAcquire()
}

// LoginAttemptService.java — 登录失败追踪
@Service
public class LoginAttemptService {
    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockTimes = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 30 * 60 * 1000; // 30分钟

    public void loginFailed(String username) { /* attempts + 1, >=5 锁定 */ }
    public boolean isLocked(String username) { /* 是否锁定中 */ }
    public long getRemainingLockSeconds(String username) { /* 剩余锁定秒数 */ }
    public void loginSucceeded(String username) { /* 清除失败计数 */ }
}
```

#### 操作日志规范（AOP 异步 + 脱敏）

```java
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperateLogAspect {
    private final SysLogService logService;

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint point, OperateLog operateLog) {
        // ... 记录开始时间
        try {
            Object result = point.proceed();
            // ... 设置操作结果
        } catch (Exception e) {
            // ... 设置错误信息（截断>500字符）
            throw e;
        } finally {
            saveLogAsync(sysLog); // @Async 异步保存，不阻塞主线程
        }
    }

    @Async
    public void saveLogAsync(SysLog sysLog) { /* 异步写入 */ }

    // 脱敏：过滤 password, token, secret, accessKey, secretKey 等敏感字段
    private String sanitizeParams(Object[] args) {
        // 遍历参数，将敏感字段值替换为 "******"
    }
}
```

**强制约定**：
- 操作日志**必须异步保存**（`@Async`），不阻塞业务主线程
- **必须对敏感参数脱敏**：`password`, `oldPassword`, `newPassword`, `confirmPassword`, `token`, `secret`, `accessKey`, `secretKey`
- 参数长度 > 2000 字符时截断，返回结果 > 1000 字符时截断，错误信息 > 500 字符时截断

### 2.7 代码生成器规范

使用内置代码生成器（`/api/tool/gen`）可快速生成 Entity/Mapper/Service/Controller/Vue/API。

生成后需手动调整：
- 添加 `unmappedTargetPolicy = ReportingPolicy.IGNORE` 到 Convert
- 将 `@Autowired` 字段注入改为构造器注入
- 调整 DTO 字段名与 Entity 对齐

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
│   ├── role.js
│   ├── menu.js
│   ├── dept.js
│   ├── dict.js
│   ├── notice.js
│   ├── log.js
│   ├── online.js
│   ├── dashboard.js
│   ├── analysis.js
│   ├── region.js
│   ├── literature.js
│   ├── honglou.js
│   ├── sanguo.js
│   ├── shuihu.js
│   ├── xiyou.js
│   ├── as400.js
│   ├── iService.js
│   ├── techBlog.js
│   ├── music.js
│   ├── commonTools.js
│   ├── permission.js
│   ├── job.js
│   ├── file.js
│   ├── export.js
│   ├── slowQuery.js
│   ├── health.js
│   ├── ipRule.js
│   ├── message.js
│   ├── favorite.js
│   ├── announcement.js
│   ├── gen.js
│   ├── importData.js
│   ├── logAnalysis.js
│   ├── apiDebug.js
│   └── backup.js
├── composables/             # 组合式函数（useXxx 命名）
│   ├── useStorage.js         # localStorage 统一管理（命名空间 rx_admin_*）
│   ├── useTablePage.js       # 通用表格分页（搜索/分页/排序/列配置/高度适配/多选）
│   ├── useTheme.js           # 亮/暗主题切换
│   ├── useMenuI18n.js        # 菜单国际化翻译映射
│   ├── usePasswordStrength.js # 密码强度检测
│   ├── useTableHeight.js     # classics 页面表格高度自适应
│   ├── useLayoutSettings.js  # 布局设置（主题色/侧边栏样式，含 ECharts 主题联动）
│   └── useMarkdownRenderer.js # Markdown 渲染器（marked + highlight.js 封装）
├── i18n/                    # 国际化
│   ├── index.js
│   └── lang/
│       ├── zh-CN.js
│       └── en-US.js
├── layout/                  # 布局组件
│   ├── index.vue             # 主布局（集成CommandPalette/AnnouncementPopup/FavoritesPanel）
│   ├── SearchBox.vue         # 全局搜索框
│   ├── NoticePopover.vue     # 通知公告弹窗
│   ├── SubMenu.vue           # 递归子菜单
│   └── TagsView.vue          # 标签栏
├── components/               # 公共组件
│   ├── CommandPalette.vue    # Ctrl+K 全局命令面板
│   ├── FavoriteStar.vue      # 收藏星标组件
│   ├── FavoritesPanel.vue    # 侧边栏收藏面板
│   ├── AnnouncementPopup.vue # 系统公告弹窗
│   └── ExportButton/         # 导出按钮组件
├── router/
│   ├── index.js             # 路由配置
│   └── componentMap.js      # 组件映射表（50+ 条目）
├── stores/                  # Pinia 状态管理
│   ├── user.js
│   └── tags.js
├── styles/                  # 全局样式
│   ├── variables.scss       # CSS 变量（亮/暗双主题）
│   ├── global.scss          # 全局重置 + 通用类
│   └── themes.scss          # 5套主题色（蓝/绿/紫/橙/青）
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

// Router & Store
import router from './router'
import pinia from './stores'

// 全局样式
import './styles/global.scss'

// 国际化
import i18n from './i18n'

// Composables
import { useStorage } from '@/composables/useStorage'

const app = createApp(App)

// 注册 Element Plus（含中文语言包，语言切换由 App.vue 中 el-config-provider 动态控制）
app.use(ElementPlus, { locale: zhCn })

// Element Plus 图标通过 unplugin-vue-components 自动按需导入，无需全量注册

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
  'profile/index':               { component: () => import('@/views/profile/index.vue'),               name: 'Profile' },
  'system/user/index':           { component: () => import('@/views/system/user/index.vue'),           name: 'SystemUser' },
  'system/role/index':           { component: () => import('@/views/system/role/index.vue'),           name: 'SystemRole' },
  'system/menu/index':           { component: () => import('@/views/system/menu/index.vue'),           name: 'SystemMenu' },
  'system/dept/index':           { component: () => import('@/views/system/dept/index.vue'),           name: 'SystemDept' },
  'system/config/index':         { component: () => import('@/views/system/config/index.vue'),         name: 'SystemConfig' },
  'system/file/index':           { component: () => import('@/views/system/file/index.vue'),           name: 'SystemFile' },

  // IP黑白名单
  'system/ipRule/index':         { component: () => import('@/views/system/ipRule/index.vue'),         name: 'SystemIpRule' },
  // 站内消息
  'content/message/index':       { component: () => import('@/views/content/message/index.vue'),       name: 'ContentMessage' },
  // 系统健康监控
  'monitor/health/index':        { component: () => import('@/views/monitor/health/index.vue'),        name: 'MonitorHealth' },
  // 日志分析
  'monitor/logAnalysis/index':   { component: () => import('@/views/monitor/logAnalysis/index.vue'),   name: 'MonitorLogAnalysis' },
  // 代码生成器
  'tool/gen/index':              { component: () => import('@/views/tool/gen/index.vue'),              name: 'ToolGen' },
  // 批量导入
  'tool/importData/index':       { component: () => import('@/views/tool/importData/index.vue'),       name: 'ToolImportData' },
  // API调试
  'tool/apiDebug/index':         { component: () => import('@/views/tool/apiDebug/index.vue'),         name: 'ToolApiDebug' },
  // 备份恢复
  'tool/backup/index':           { component: () => import('@/views/tool/backup/index.vue'),           name: 'ToolBackup' },
  'tool/dict/index':             { component: () => import('@/views/tool/dict/index.vue'),             name: 'ToolDict' },
  'tool/region/index':           { component: () => import('@/views/tool/region/index.vue'),           name: 'ToolRegion' },
  'tool/analysis/index':         { component: () => import('@/views/tool/analysis/index.vue'),         name: 'ToolAnalysis' },
  'tool/docs/index':             { component: () => import('@/views/tool/docs/index.vue'),             name: 'ToolDocs' },
  'tool/standards/index':        { component: () => import('@/views/tool/standards/index.vue'),        name: 'ToolStandards' },
  'tool/excelParser/index':      { component: () => import('@/views/tool/excelParser/index.vue'),      name: 'ToolExcelParser' },
  'tool/docConverter/index':     { component: () => import('@/views/tool/docConverter/index.vue'),     name: 'ToolDocConverter' },
  'tool/docUpload/index':        { component: () => import('@/views/tool/docUpload/index.vue'),        name: 'ToolDocUpload' },
  'tool/flowChart/index':        { component: () => import('@/views/tool/flowChart/index.vue'),        name: 'ToolFlowChart' },
  'tool/flowChart/logicFlow':    { component: () => import('@/views/tool/flowChart/logicFlow.vue'),    name: 'ToolLogicFlowChart' },
  'tool/flowChart/antvX6':       { component: () => import('@/views/tool/flowChart/antvX6.vue'),       name: 'ToolAntvX6Chart' },
  'tool/musicPlayer/index':      { component: () => import('@/views/tool/musicPlayer/index.vue'),      name: 'ToolMusicPlayer' },
  'content/notice/index':        { component: () => import('@/views/content/notice/index.vue'),        name: 'ContentNotice' },
  'monitor/log/index':           { component: () => import('@/views/monitor/log/index.vue'),           name: 'MonitorLog' },
  'monitor/online/index':        { component: () => import('@/views/monitor/online/index.vue'),        name: 'MonitorOnline' },
  'monitor/job/index':           { component: () => import('@/views/monitor/job/index.vue'),           name: 'MonitorJob' },
  'monitor/slow-query/index':    { component: () => import('@/views/monitor/slow-query/index.vue'),    name: 'MonitorSlowQuery' },
  'as400/objects/index':         { component: () => import('@/views/as400/objects/index.vue'),         name: 'As400Objects' },
  'as400/iservice/index':        { component: () => import('@/views/as400/iservice/index.vue'),        name: 'As400IService' },
  'as400/techblog/index':        { component: () => import('@/views/as400/techblog/index.vue'),        name: 'TechBlogIndex' },
  'as400/techblog/detail':       { component: () => import('@/views/as400/techblog/detail.vue'),       name: 'TechBlogDetail' },
  'permission/request/index':    { component: () => import('@/views/permission/request/index.vue'),    name: 'PermissionRequest' },
  'classics/honglou/poems/index':      { component: () => import('@/views/classics/honglou/poems/index.vue'),      name: 'ClassicsHonglouPoems' },
  'classics/honglou/characters/index':  { component: () => import('@/views/classics/honglou/characters/index.vue'),  name: 'ClassicsHonglouCharacters' },
  'classics/honglou/relations/index':   { component: () => import('@/views/classics/honglou/relations/index.vue'),   name: 'ClassicsHonglouRelations' },
  'classics/xiyou/poems/index':        { component: () => import('@/views/classics/xiyou/poems/index.vue'),        name: 'ClassicsXiyouPoems' },
  'classics/xiyou/characters/index':    { component: () => import('@/views/classics/xiyou/characters/index.vue'),    name: 'ClassicsXiyouCharacters' },
  'classics/xiyou/events/index':        { component: () => import('@/views/classics/xiyou/events/index.vue'),        name: 'ClassicsXiyouEvents' },
  'classics/sanguo/poems/index':       { component: () => import('@/views/classics/sanguo/poems/index.vue'),       name: 'ClassicsSanguoPoems' },
  'classics/sanguo/characters/index':   { component: () => import('@/views/classics/sanguo/characters/index.vue'),   name: 'ClassicsSanguoCharacters' },
  'classics/shuihu/poems/index':       { component: () => import('@/views/classics/shuihu/poems/index.vue'),       name: 'ClassicsShuihuPoems' },
  'classics/shuihu/chapters/index':     { component: () => import('@/views/classics/shuihu/chapters/index.vue'),     name: 'ClassicsShuihuChapters' },
  'classics/literature/index':          { component: () => import('@/views/classics/literature/index.vue'),          name: 'ClassicsLiteratureIndex' },
  'classics/literature/works/index':    { component: () => import('@/views/classics/literature/works/index.vue'),      name: 'ClassicsLiteratureWorks' },
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

### 3.4 useStorage Composable 规范

**必须使用 `useStorage` 管理所有 localStorage 操作**，禁止直接调用 `localStorage.getItem/setItem/removeItem`。

```javascript
// composables/useStorage.js
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
  // 自动 JSON 序列化/反序列化
  // 统一 try/catch 错误处理
  // 返回 ref，与 Vue 响应式系统集成

  function get() { /* JSON.parse(localStorage.getItem(key)) */ }
  function set(value) { /* localStorage.setItem(key, JSON.stringify(value)) */ }
  function remove() { /* localStorage.removeItem(key) */ }

  return { get, set, remove }
}
```

**使用示例**：
```javascript
// stores/user.js
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'
const tokenStorage = useStorage(STORAGE_KEYS.TOKEN)
tokenStorage.set('xxx-token')
const token = tokenStorage.get()

// composables/useTheme.js
const theme = useStorage(STORAGE_KEYS.THEME, 'light')
theme.set('dark')
```

**强制约定**：
1. 所有持久化 key 使用 `rx_admin_` 前缀命名空间
2. 必须通过 `STORAGE_KEYS` 常量引用，禁止硬编码字符串
3. 自动处理 JSON 序列化/反序列化
4. 异常静默处理（localStorage 不可用时降级为内存存储）

#### useMarkdownRenderer Composable（v3.2 新增）

```javascript
// composables/useMarkdownRenderer.js
import { marked } from 'marked'
import hljs from 'highlight.js'

export function useMarkdownRenderer() {
  // 配置 marked 使用 highlight.js 代码高亮
  // 返回 renderMarkdown(content) 函数

  const renderMarkdown = (content) => {
    if (!content) return ''
    return marked(content, { renderer })
  }

  return { renderMarkdown }
}
```

**用途**: 封装 marked + highlight.js，统一 Markdown 文档渲染，使用时无需重复导入配置。已在 `tool/docs` 和 `tool/standards` 页面中使用。

### 3.5 useTablePage Composable 规范

通用表格分页逻辑复用，封装了 14+ 个页面共用的表格 CRUD 模式。

```javascript
// composables/useTablePage.js
export function useTablePage(fetchApi, options = {}) {
  // 分页
  const loading = ref(false)
  const page = ref(1)
  const size = ref(10)
  const total = ref(0)
  const tableData = ref([])

  // 搜索
  const keyword = ref('')

  // 排序
  const sortField = ref('')
  const sortOrder = ref('')
  const sortedTableData = computed(() => { /* 前端排序逻辑 */ })

  // 列配置
  const columnOptions = ref([])
  const visibleColumns = ref([])
  const toggleColumn = (prop) => { /* 切换列可见性 */ }

  // 表格高度自适应
  const TABLE_ROW_HEIGHT = 48
  const tableMaxHeight = ref(400)
  const calcTableMaxHeight = () => { /* 计算可用高度 */ }

  // 多选
  const selectedRows = ref([])
  const handleSelectionChange = (rows) => { selectedRows.value = rows }

  // 数据获取
  const fetchData = async () => {
    loading.value = true
    try {
      const res = await fetchApi({ page, size, keyword, sortField, sortOrder })
      tableData.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => { page.value = 1; fetchData() }
  const handleSortChange = ({ prop, order }) => { /* 更新排序并重新请求 */ }

  return {
    loading, page, size, total, tableData, keyword,
    sortedTableData, sortField, sortOrder,
    columnOptions, visibleColumns, toggleColumn,
    tableMaxHeight, calcTableMaxHeight, TABLE_ROW_HEIGHT,
    selectedRows, handleSelectionChange,
    fetchData, handleSearch, handleSortChange,
  }
}
```

### 3.6 页面组件规范

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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { xxxApi } from '@/api/xxx'
import { useTablePage } from '@/composables/useTablePage'

defineOptions({ name: 'ModulePage' })  // ⚠️ 必须声明，用于 keep-alive

// 使用 useTablePage 统一管理表格状态（推荐）
const {
  loading, keyword, page, size, total, tableData,
  sortedTableData, handleSortChange,
  columnOptions, visibleColumns, toggleColumn,
  tableMaxHeight, calcTableMaxHeight,
  selectedRows, handleSelectionChange,
  fetchData, handleSearch
} = useTablePage(xxxApi)

// 弹窗状态
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = reactive({})
const rules = reactive({})

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
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const tokenStorage = useStorage(STORAGE_KEYS.TOKEN)

// 请求拦截器
service.interceptors.request.use(
  config => {
    NProgress.start()
    const token = tokenStorage.get()
    if (token) {
      config.headers['Authorization'] = token
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
2. Token 放在 `Authorization` 请求头，通过 `useStorage` 读写
3. 统一拦截 `code !== 200` 的错误
4. 使用 NProgress 显示请求进度
5. **禁止直接使用 `localStorage`**，必须通过 `useStorage` 访问
6. **后台定时轮询必须传入 `_skipNProgress: true`**，避免进度条频繁闪烁
7. **禁止硬编码魔法数字**：超时、间隔、尺寸等可配置参数必须通过 `import.meta.env.VITE_xxx` 读取（带 fallback 默认值），不能直接写死

```javascript
// ✅ 正确：API 定义预留 options 参数
export function getNoticePageApi(params, options = {}) {
  return request({ url: '/content/notice/page', method: 'get', params, ...options })
}

// ✅ 定时轮询调用：跳过进度条
getNoticePageApi({ page: 1, size: 50 }, { _skipNProgress: true })

// ❌ 错误：定时轮询不跳过进度条，每N秒触发一次进度条闪烁
setInterval(() => { getNoticePageApi({ page: 1, size: 50 }) }, 15000)

// ✅ 正确：超时/间隔从环境变量读取
const axios = axios.create({
  baseURL: '/api',
  timeout: Number(import.meta.env.VITE_API_REQUEST_TIMEOUT) || 15000
})

// ❌ 错误：硬编码魔法数字
const axios = axios.create({ timeout: 15000 })
```

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
5. **预留接口**：当前未使用但计划后续使用的 API 函数，添加 `@reserved` 注释说明用途，禁止直接删除

### 3.7 ExportButton 导出组件规范

通用数据导出按钮组件，支持 Excel 和 PDF 两种格式，内置双模式导出引擎。

#### 前端导出（默认）

```html
<!-- system/user/index.vue -->
<ExportButton :data="sortedTableData" :columns="exportColumns" title="用户管理" />
```

**工作流程**:
1. 组件 `onMounted` 时首次调用 `GET /api/export/config?path=/system/user` 查询导出配置（仅 1 次）
2. 用户点击下拉菜单选择 Excel 或 PDF
3. **Excel**: `exceljs` 纯前端生成 .xlsx（标题合并 + 蓝色表头 + 斑马纹 + 自动列宽 + 冻结表头）
4. **PDF**: `html2canvas` 渲染 HTML 表格 → 截图 → `jspdf` 生成 PDF（A4 横向 + 自动分页）

**依赖**: `exceljs`, `jspdf`, `jspdf-autotable`, `html2canvas`

**config 请求去重**: 通过三层防护确保每个路径只请求 1 次
```js
// 模块级（<script setup> 外部，跨组件实例共享）
const configCache = new Map()  // path → types
let fetchingPath = ''

// 仅 onMounted 触发（不再用 watch(immediate)，避免 setup 阶段 + 路由变化重复触发）
onMounted(fetchConfig)
```
| 层级 | 说明 |
|------|------|
| ① 唯一触发源 | `onMounted()` 替换 `watch(immediate:true)`，每次挂载只触发 1 次 |
| ② 模块级缓存 | `configCache` 同路径命中直接返回，切标签 0 请求 |
| ③ 并发锁 | `fetchingPath` 防止同一路径并发调用 |

#### 后端导出（保留）

```html
<ExportButton :data="sortedTableData" :columns="exportColumns" mode="server" />
```

**后端端点**: `POST /api/export/excel`（Apache POI）、`POST /api/export/pdf`（PDFBox）

#### Props

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `Array` | `[]` | 表格数据 |
| `columns` | `Array` | `[]` | 列定义 `[{ field, label }]` |
| `title` | `String` | `''` | 导出标题（默认取 `route.meta.title`） |
| `mode` | `String` | `'client'` | `'client'` 前端导出 / `'server'` 后端导出 |

#### 新页面接入

```javascript
// 步骤 1：定义导出列
const exportColumns = [
  { field: 'id', label: 'ID' },
  { field: 'name', label: '名称' },
  { field: 'createTime', label: '创建时间' }
]

// 步骤 2：模板中引入
// <ExportButton :data="tableData" :columns="exportColumns" title="模块名" />
```

#### 相关文件

| 文件 | 说明 |
|------|------|
| `ui/src/components/ExportButton/index.vue` | 导出按钮组件 |
| `ui/src/utils/exportClient.js` | 前端导出工具（`exportExcelClient` / `exportPdfClient`） |
| `ui/src/api/export.js` | 后端导出 API + 配置查询 |
| `ExportController.java` | 后端导出控制器 |
| `ExportServiceImpl.java` | 后端导出服务实现 |

### 3.8 Pinia Store 规范

```javascript
// stores/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, getUserInfoApi, getRoutersApi } from '@/api/auth'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

export const useUserStore = defineStore('user', () => {
  // 使用 useStorage 统一管理持久化
  const tokenStorage = useStorage(STORAGE_KEYS.TOKEN)
  const userInfoStorage = useStorage(STORAGE_KEYS.USER_INFO)
  const rolesStorage = useStorage(STORAGE_KEYS.ROLES)
  const permsStorage = useStorage(STORAGE_KEYS.PERMS)
  const menusStorage = useStorage(STORAGE_KEYS.MENUS)

  // 状态（从 localStorage 恢复，保证刷新后数据不丢失）
  const token = ref(tokenStorage.get() || '')
  const userInfo = ref(userInfoStorage.get())
  const roles = ref(rolesStorage.get() || [])
  const perms = ref(permsStorage.get() || [])
  const menus = ref(menusStorage.get() || [])

  // 登录
  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    tokenStorage.set(token.value)
    await fetchUserInfo()    // 预加载用户信息
    await fetchRouters()     // 预加载菜单路由
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data
    roles.value = res.data.roles || []
    perms.value = res.data.permissions || []
    userInfoStorage.set(userInfo.value)
    rolesStorage.set(roles.value)
    permsStorage.set(perms.value)
  }

  // 获取路由菜单
  async function fetchRouters() {
    const res = await getRoutersApi()
    menus.value = res.data || []
    menusStorage.set(menus.value)
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    perms.value = []
    menus.value = []
    // 清除所有认证相关数据
    [tokenStorage, userInfoStorage, rolesStorage, permsStorage, menusStorage].forEach(s => s.remove())
  }

  return { token, userInfo, roles, perms, menus, login, fetchUserInfo, fetchRouters, logout }
})
```

**强制约定**：
1. 使用 Composition API 风格（`defineStore('name', () => { ... })`）
2. 状态必须通过 `useStorage` 持久化到 `localStorage`，禁止直接操作
3. 初始化时从 `useStorage` 恢复数据
4. `login()` 中预加载 `fetchUserInfo()` + `fetchRouters()`
5. `logout()` 清理所有状态和 localStorage

### 3.9 国际化与语言切换规范

**无刷新语言切换**：通过 `el-config-provider` 实现 Element Plus 组件语言动态切换，无需整页刷新。

```vue
<!-- App.vue -->
<template>
  <el-config-provider :locale="elCurrentLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

const { locale } = useI18n()

const elCurrentLocale = computed(() => {
  return locale.value === 'zh-CN' ? zhCn : en
})
</script>
```

**强制约定**：
- 语言切换使用 `i18n.global.locale.value = newLocale`，禁止 `window.location.reload()`
- 使用 `useStorage(STORAGE_KEYS.LOCALE)` 持久化用户语言偏好

### 3.10 布局组件规范

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
- 全局搜索框（`SearchBox.vue`，仅搜索叶子菜单，忽略父级目录）
- **Ctrl+K 命令面板入口**（快捷搜索按钮，点击唤起 `CommandPalette.vue`）
- 暗黑主题切换 | 语言切换（无刷新） | 通知（`NoticePopover.vue`，默认显示未读） | 全屏 | 用户头像下拉

**布局组件拆分规范**：
- `layout/index.vue` — 主布局容器，集成 CommandPalette、AnnouncementPopup、FavoritesPanel 组件
- `layout/SearchBox.vue` — 全局搜索框（独立组件）
- `layout/NoticePopover.vue` — 通知公告弹窗（独立组件，分类 Tab + 默认未读）
- `layout/SubMenu.vue` — 递归子菜单（独立组件）
- `layout/TagsView.vue` — 标签页导航栏（独立组件）
- `components/CommandPalette.vue` — Ctrl+K 全局命令面板（独立组件）
- `components/FavoriteStar.vue` — 收藏星标组件（独立组件，可复用在任意页面）
- `components/FavoritesPanel.vue` — 侧边栏快捷收藏面板（独立组件，集成在 layout）
- `components/AnnouncementPopup.vue` — 系统公告弹窗（独立组件，登录自动检测）
- 禁止将所有逻辑堆在 `index.vue` 中，新功能优先抽取为子组件

---

## 4. CSS 样式规范

### 4.1 主题系统

采用 **CSS 变量双主题** 方案，通过 `html.dark` 切换，由 `useTheme` composable 统一管理：

```javascript
// composables/useTheme.js
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

export function useTheme() {
  const theme = useStorage(STORAGE_KEYS.THEME, 'light')

  const toggleTheme = () => {
    const isDark = !document.documentElement.classList.contains('dark')
    document.documentElement.classList.toggle('dark', isDark)
    theme.set(isDark ? 'dark' : 'light')
  }

  return { theme, toggleTheme }
}
```

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
6. **依赖注入使用构造器注入**，推荐 `@RequiredArgsConstructor`
7. **操作日志必须异步保存**（`@Async`），禁止同步阻塞
8. **操作日志参数必须脱敏**，过滤 `password`/`token`/`secret` 等敏感字段
9. **关键接口必须限流**（如登录），使用 `RateLimiterConfig`
10. **登录接口必须集成失败锁定**（`LoginAttemptService`）

### 7.3 前端代码质量

1. **组件必须声明 `defineOptions({ name: 'Xxx' })`**
2. **API 调用必须放在 `try/catch/finally` 中**
3. **列表页加载时必须显示 loading 状态**
4. **删除操作必须有二次确认弹窗**（`ElMessageBox.confirm`）
5. **表单必须有校验规则**
6. **弹窗关闭时必须重置表单**
7. **localStorage 操作必须使用 `useStorage`**，禁止直接调用原生 API
8. **Element Plus 图标禁止全量注册**，使用 `unplugin-vue-components` 自动按需导入
9. **POST 请求敏感参数使用 `data`**，禁止用 `params` 拼接 URL
10. **语言切换禁止 `window.location.reload()`**，使用 `i18n.global.locale.value` 动态切换
11. **布局逻辑优先抽取为子组件**，禁止在 `index.vue` 中堆砌
12. **表格页面推荐使用 `useTablePage` composable** 复用分页/排序/列配置逻辑
13. **未使用的 API 函数添加 `@reserved` 注释**标注用途，禁止直接删除

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
| 使用 `useTablePage` composable | ✅ 推荐（表格页面） |
| 使用 `useStorage` 管理持久化 | ✅ 必须（替代直接 localStorage） |

### 8.3 后端 `sys_menu` 字段规范

| 字段 | 规范 | 示例 |
|------|------|------|
| `path` | 前端路由路径 | `/system/user` |
| `component` | views/ 下文件路径（不含 .vue），与 `componentMap.js` key 一致 | `system/user/index` |
| `menuType` | 1=目录, 2=菜单, 3=按钮 | 路由只处理 type=1,2 |
| `icon` | Element Plus 图标名（自动按需导入） | `UserFilled` |
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

### 9.6 登录后显示"系统繁忙，请稍后再试"

**问题**：全新部署后登录成功，但页面显示"系统繁忙，请稍后再试"，所有功能无法正常使用。

**原因**：`FavoritesPanel.vue` 组件在挂载时自动调用 `GET /api/system/favorite/list`，该接口查询 `sys_user_favorite` 表。如果未手动执行 `db/features_init.sql` 建表脚本，该表不存在，SQL 抛 `Table doesn't exist` 异常，触发 `GlobalExceptionHandler` 兜底异常返回 "系统繁忙，请稍后再试"。

**排查步骤**：
1. 打开浏览器 DevTools → Network，查找返回 `code:500` 的请求
2. 查看后端日志 `backend.log`，定位 `SQLSyntaxErrorException: Table 'rx_admin.sys_user_favorite' doesn't exist`
3. 确认缺少的表名（通常为 `sys_user_favorite`、`sys_ip_rule`、`sys_message` 之一）

**解决方案**：
1. 在 MySQL 中执行 `db/features_init.sql` 创建 3 张新表
2. 执行 `db/features_menu.sql` 插入菜单记录
3. 重启后端服务
4. 清除浏览器缓存后刷新页面

> `features_init.sql` 和 `features_menu.sql` **不会自动执行**，每次全新部署或数据库迁移后都需要手动执行。

### 9.7 左侧菜单看不到 v2.0 新增功能

**问题**：代码已部署、后端已启动、登录成功，但左侧菜单栏中看不到"IP黑白名单""消息中心""代码生成器"等 v2.0 新增功能入口。

**原因**：这些功能的菜单入口是通过 `db/features_menu.sql` 脚本插入到 `sys_menu` 表的。该脚本**不会自动执行**，如果全新部署时跳过了此步骤，`sys_menu` 表中就没有这些菜单记录，`SysMenuService.getRouterMenus()` 自然查询不到。

**完整链路分析**：
1. 用户登录 → `userStore.fetchRouters()` → `GET /api/auth/routers` → `SysMenuService.getRouterMenus()`
2. `getRouterMenus()` 查询 `sys_menu` 表中 `status=1` 且 `menuType IN (1,2)` 的记录
3. 如果未执行 `features_menu.sql`，v2.0 菜单记录不存在 → 返回的菜单列表为空（对 v2.0 部分）→ 左侧无显示

**前端侧已完整就绪**：
- `componentMap.js` 中 8 个新组件的路由映射均已配置
- `SubMenu.vue` 无 `visible` 过滤，所有后端返回的菜单都会渲染
- 唯一阻塞因素就是数据库中的 `sys_menu` 表缺少记录

**解决方案**：
```bash
# 1. 执行菜单脚本
mysql -u root -p rx_admin < db/features_menu.sql

# 2. 重启后端服务

# 3. 清除前端缓存（localStorage 中的 rx_admin_menus key），或重新登录
```

> 补充：如果同时缺少 `sys_user_favorite` 等表，还需要执行 `db/features_init.sql`，否则会出现"系统繁忙，请稍后再试"（见 9.6）。

---

## 附录：检查清单

### 新页面开发完成前检查

- [ ] 后端实体继承 `BaseEntity`，添加 `@EqualsAndHashCode(callSuper = true)`
- [ ] 后端 Controller 添加 `@Tag` / `@Operation` 注解
- [ ] 后端接口添加 `@SaCheckPermission` 权限注解
- [ ] 后端使用构造器注入（推荐 `@RequiredArgsConstructor`），不使用 `@Autowired`
- [ ] 后端 `@OperateLog` 注解方法确保参数已脱敏
- [ ] 前端组件声明 `defineOptions({ name: 'Xxx' })`
- [ ] 前端 `componentMap.js` 追加映射（key 与 `sys_menu.component` 一致）
- [ ] 前端样式使用 CSS 变量，不硬编码颜色值
- [ ] 前端 API 调用使用 `try/catch/finally`
- [ ] 前端删除操作有二次确认
- [ ] 前端表单有校验规则
- [ ] 前端弹窗关闭时重置表单
- [ ] 前端 localStorage 操作使用 `useStorage`，禁止直接调用原生 API
- [ ] 表格页面使用 `useTablePage` composable 复用逻辑
- [ ] 数据库中 `sys_menu` 表插入对应菜单记录
- [ ] 无未使用的 import 或变量
- [ ] 亮色/暗色主题均显示正常
- [ ] 未使用的 API 函数添加 `@reserved` 注释

---

## 10. 项目优化建议

> 本章基于对 RX Admin 项目全面审查后的优化建议，供开发团队参考。详细分析与实施细节请参阅 [rxadmin.md 第12章](./rxadmin.md#12-项目优化建议)。**✅ 标记表示已完成**。

### 10.1 优化分级体系

| 优先级 | 含义 | 建议处理时限 |
|--------|------|-------------|
| **P0** | 紧急安全/功能问题，必须立即修复 | 1 天内 |
| **P1** | 高优先级，代码质量直接影响开发效率 | 1-2 周内 |
| **P2** | 中等优先级，性能或架构改进 | 1 个月内 |
| **P3** | 低优先级，工程化建设，长期持续 | 持续进行 |

### 10.2 已完成的优化项汇总

以下优化项已在 v1.3.0 中全部实施完成：

| 优先级 | 优化项 | 实现 |
|--------|--------|------|
| P0 | POST 请求参数修复 | `auth.js` 改用 `data` 传参 |
| P1 | 抽取通用表格 Composable | `useTablePage.js` — 分页/搜索/排序/列配置/高度适配/多选 |
| P1 | 拆分布局组件 | `SearchBox.vue` + `NoticePopover.vue`，主布局 ~644 行 |
| P1 | 清理未使用 API | 16 个函数添加 `@reserved` 注释 |
| P1 | 异常处理优化 | 10 种异常处理器（401/403/400/404/405/415/数据约束等） |
| P1 | 操作日志异步化 + 脱敏 | `@Async` + `sanitizeParams()` |
| P1 | PageResult 分页补全 | `of(total, page, size, records)` + `of(Page<T>)` |
| P2 | ECharts 分包 | `manualChunks` 独立 echarts chunk |
| P2 | Element Plus 图标按需引入 | `unplugin-vue-components` 自动导入 |
| P2 | Vite 构建分包 | `manualChunks: { echarts, element-plus }` |
| P2 | 生产环境 SQL 日志关闭 | `application-prod.yml` 使用 Slf4jImpl |
| P2 | 响应拦截器浅层优化 | `formatResponseData` 仅处理时间字段 |
| P2 | 语言切换无刷新 | `el-config-provider` + `i18n.global.locale.value` |
| P2 | localStorage 统一管理 | `useStorage.js` — 命名空间 `rx_admin_*` |
| P2 | 请求频率限制 | `RateLimiterConfig.java` — Guava RateLimiter 3次/秒 |
| P2 | 登录失败锁定 | `LoginAttemptService.java` — 5次失败锁定30分钟 |
| P3 | DashboardController DI 优化 | `@RequiredArgsConstructor` |
| P3 | 清理根目录杂项文件 | 11 个无用文件已删除 |

### 10.3 待实施优化（测试阶段暂保留）

| 优先级 | 优化项 | 说明 |
|--------|--------|------|
| P0 | 移除硬编码默认密码 | 测试阶段保留开发便利，发布前改为 `import.meta.env.DEV ? 'admin' : ''` |
| P0 | CORS 安全配置 | 测试阶段保留 `*`，发布前限制域名白名单 |
| P0 | 清理 init.sql 密码注释 | 发布前移除明文密码提示 |

### 10.4 后续可扩展优化

| 优化项 | 说明 |
|--------|------|
| TypeScript 迁移 | 渐进式：API 层 → Store 层 → 组件层 |
| ESLint + Prettier | 代码规范统一 |
| 单元测试 | 后端 JUnit 5 + 前端 Vitest |
| Sa-Token Redis 集成 | 生产环境多实例部署支持 |

---

> **文档维护**: 本文档基于 RX Admin 项目实践提炼，适用于所有基于 Spring Boot 3 + Vue 3 + Element Plus 的后台管理系统开发。
> **历史版本**: v1.4.0 (2026-06-05) → v1.5.0 (2026-06-15): MapStruct 规范 + 构造器注入 + 模块化架构 + EmailService + 主题色统一 + Sentry v10 + 字体自托管 + sass-embedded + 构建优化 + useMarkdownRenderer
