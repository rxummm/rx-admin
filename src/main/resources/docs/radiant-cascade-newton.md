# RX 项目优化方案

## 项目现状
- Spring Boot 3.5.15 + Vue 3 + Vite + MyBatis-Plus 3.5.5 + Sa-Token 1.37.0
- 路径：`D:\vueprojects\RX`
- 评分：5.8/10
- 累计发现 **34 类问题**（6 高危 + 10 中危 + 18 低危）

## 核心结论

| 阶段 | 工作量 | 风险 | 何时做 |
|------|--------|------|--------|
| 阶段一：安全加固 | 1-2 天 | 低-中 | 立即 |
| 阶段二：工程规范 | 3-5 天 | 低 | 本周 |
| 阶段三：代码质量 | 1-2 周 | 中 | 按节奏 |
| 阶段四：架构优化 | 持续 | 中-高 | 视情况 |

---

## 阶段一：紧急安全加固（立即做）

### 1.1 硬编码敏感信息外部化
**文件：**
- `src/main/resources/application-local.yml`（含明文 SMTP 密码 `LJgTDSaGQrBWqP9K`）
- `src/main/resources/application.yml`（数据库密码默认值 `root`）
- `src/main/java/com/rx/admin/common/handler/AesTypeHandler.java`（密钥 `RxAdmin!@#2026!!` 硬编码）

**做法：**
- SMTP 密码改用 `${MAIL_PASSWORD:}` 引用环境变量
- AES 密钥移至 yml，通过 `@Value` 注入；ECB 改 CBC（注意已有数据迁移）
- `application-local.yml` 加入 `.gitignore`
- 提交前 `git diff` 验证

### 1.2 CORS 配置修复
**文件：** `src/main/java/com/rx/admin/framework/web/CorsConfig.java`

**问题：** `allowCredentials(true)` + `allowedOriginPattern("*")` 安全漏洞

**修复：** 明确列出允许的 origin：
```java
.allowedOriginPatterns("http://localhost:5173", "https://yourdomain.com")
```

### 1.3 Windows 绝对路径外化
**问题：** `application.yml` 中 5+ 处硬编码 `D:/vueprojects/RX/...`

**修复：** 路径全部改为 `classpath:` 或可配置项

### 1.4 MyBatis 日志仅 local 开启
**文件：** `application.yml`

**修复：** `StdOutImpl` 移至 `application-local.yml`，主配置设为 `NoLoggingImpl`

**验收：** `git diff` 确认无明文敏感信息，CORS 无 `*` 通配符，路径全部可配置

---

## 阶段二：工程规范化（本周）

### 2.1 根目录清理
```
清理目标：
├── *.log (6个)         → 移入 logs/ 或 .gitignore
├── *.py (5个脚本)      → 移入 scripts/ 或删除
├── 修复报告 MD (12个)  → 移入 docs/fix-reports/archive/
├── temp/               → 删除
├── 根 node_modules/    → 清理（根 package.json 不需要）
└── 散乱临时文件         → 删除
```

### 2.2 .gitignore 完善
追加：
```
*.py
__pycache__/
temp/
apimy*.yaml
snap.yaml
application-local.yml
*.example
backend.log.*
```

### 2.3 日志改造
**新增** `src/main/resources/logback-spring.xml`：
- 按天滚动（`TimeBasedRollingPolicy`）
- 单文件最大 100MB
- 保留 30 天，总大小 ≤ 10GB
- 输出到 `logs/backend.log`

**修改** `application.yml`：`logging.level: com.rx.admin: info`

### 2.4 SQL 文件统一
**当前：** 32 个 SQL 文件分散在 `db/` `sql/` `docs/` 三个目录

**修复：** 统一移入 `docs/sql/`，新增 `docs/sql/README.md` 说明用途。Flyway 暂不引入（个人项目过度工程化）。

### 2.5 README 更新
当前 README 写的 SB 3.2.0，实际 3.5.15。修正版本号、技术栈、启动方式。

---

## 阶段三：代码质量提升（1-2 周分批）

### 3.1 经典名著重复代码抽取
**问题：** 5 个 Controller 重复 ~85%，15 个 Service 重复 ~90%

**做法：**
- 利用已存在但未使用的 `common/base/BaseCrudController.java`
- 创建 `BaseBookService<M, T>` 泛型基类
- 每个 Controller 目标 ≤ 50 行

**工作量：** 2-3 天

### 3.2 N+1 查询修复
**问题位置：**
- `SysRoleService.listAll()` 每个角色查一次菜单
- `SysDeptService.collectChildrenIds()` 每层递归一次查询
- `SysUserService.addUser()` 逐条插入角色关联
- `SysPermissionManageService.addUserMenus()` 逐条插入菜单

**修复：**
- 联表查询一次加载
- 部门树用 `WITH RECURSIVE`（MySQL 8） 或 一次查全量
- 批量操作改用 `saveBatch()`

### 3.3 公共工具抽取
**重复方法 → 抽取目标：**
- `buildTree()` × 3 处 → `common/utils/TreeUtils.java`
- `collectDescendantIds()` × 2 处 → 同上
- `getClientIp()` × 2 处 → `common/utils/WebUtils.java`
- 分页查询模式 × 20+ 处 → `BasePageService`

### 3.4 缓存扩展
Caffeine 当前只覆盖 `config` + `menu`，扩展：
```java
@Cacheable("dict")    public List<SysDict> getDictByType(String)
@Cacheable("deptTree") public List<SysDept> getDeptTree()
@Cacheable("perms")   public Set<String> getUserPermissions(Long)
```

### 3.5 入参校验补全
- 经典名著 Controller 加 `@Valid`
- `AuthController.updateProfile` 用 DTO 替代 `Map<String,Object>`
- DTO 加 `javax.validation` 注解

### 3.6 单元测试从 0 到 1
- 先为 `SysUserService` / `SysRoleService` 补测试
- 用 H2 内存数据库
- 目标：核心覆盖率 > 60%

---

## 阶段四：架构优化（按需）

### 4.1 Sa-Token Redis 持久化
当前内存模式，重启丢会话。引入 `sa-token-dao-redis-jackson` 即可。

### 4.2 限流器内存泄漏
`AuthController.loginRateLimiters` 静态 Map 无限增长。改用 Caffeine 过期策略。

### 4.3 防重放 nonce 集群化
静态 HashMap 在集群下失效。改用 Redis（`SET key value NX EX 60`）。

### 4.4 统一业务异常
- 新建 `BusinessException(code, message)`
- 全局异常处理覆盖
- 替换 `System.err.println` → `log.error`

---

## 关键文件清单

**最高优先级修改：**
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/java/com/rx/admin/framework/web/CorsConfig.java`
- `src/main/java/com/rx/admin/common/handler/AesTypeHandler.java`

**新增文件：**
- `src/main/resources/logback-spring.xml`
- `docs/sql/README.md`
- `scripts/`（迁移 Python 工具）

**清理对象：**
- 根目录 6 个 `*.log`
- 根目录 5 个 `*.py`
- 根目录 12 个 `*.md` 修复报告
- `temp/` 目录
- 根 `node_modules/`

---

## 务实建议

按你的风格（个人开发者 + SRE + 偏好简化），建议执行顺序：

1. **今天：** 阶段一全部完成（半天到 1 天）
2. **本周：** 阶段二根目录清理 + .gitignore + 日志改造
3. **下周：** 阶段三 3.1 经典名著抽取（最大块头，收益最高）
4. **本月内：** 阶段三 3.2-3.6
5. **阶段四看心情：** 架构优化不影响日常使用

**不要做的事：**
- 引入 Flyway（过度工程化）
- 引入 Kubernetes / Service Mesh
- 微服务化（单体够用）
- 一次性重写（保持渐进式重构）
