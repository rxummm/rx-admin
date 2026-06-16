# AS400 迁移方案

将 RX Admin 从当前 Spring Boot 3.5 + Vue 3 + MySQL 架构迁移到纯 AS400 环境的完整方案。

---

## 1. 目标架构

```
US400A (部署服务器)
IBM i + Java 17
┌────────────────────────────────────────────────────┐
│  Spring Boot App (port 8088)                       │
│                                                     │
│  ┌────────────────┐  ┌──────────────────────────┐  │
│  │ DB2 for i      │  │  JT400 JDBC (多连接)      │  │
│  │ (US400A 本地)  │  │  → US400A (QSYS2 查询)    │  │
│  │                │  │  → US400B (业务数据查询)   │  │
│  │ 系统表          │  │  → US400C (业务数据查询)   │  │
│  │ AS400 模块表    │  └──────────────────────────┘  │
│  └────────────────┘                                 │
│                                                     │
│  IFS /app/rxadmin/                                 │
│  ├── rx-admin.jar                                  │
│  └── config/application.yml                        │
└────────────────────────────────────────────────────┘
```

### 约束条件

| 约束 | 说明 |
|------|------|
| US400A 不能运行 MySQL | AS400 上无法安装 MySQL |
| 需同时访问 US400B 和 US400C | AS400 连接支持多目标 |
| 删除四大名著/历代文学模块 | 专注 AS400 业务 |
| 删除音乐播放器 | 非核心功能 |
| 删除知识图谱 | 非核心功能 |

---

## 2. 数据库选型

最终方案：使用 **US400A 自带的 DB2 for i**（IBM DB2 for IBM i）。

IBM i 操作系统内置 DB2 数据库引擎，零安装、零额外许可费用。数据存储在 DB2 物理文件中，由 IBM i 的 SAVSYS/SAVLIB 日常备份自动保护，同时可被原生 RPG/CL 程序直接访问。

| 因素 | 说明 |
|------|------|
| 安装 | **已安装**（IBM i 内置），零操作 |
| 备份 | IBM i 日常备份自动覆盖（BRMS/SAVSYS） |
| 连接 | JT400 JDBC 已在项目中，driver 和当前 AS400 查询一致 |
| 运维 | 原生管理界面 WRKDBF / Navigator |
| 可被其他应用访问 | ✅ DB2 数据可以被 RPG、CL、SQL 客户端直接读写 |
| SQL 适配成本 | 中（DDL + 少量 @Select 注解中的 SQL 需改为 DB2 语法） |

### MySQL → DB2 for i 主要差异

```
DDL:
  id BIGINT AUTO_INCREMENT           → id BIGINT GENERATED AS IDENTITY
  ENGINE=InnoDB DEFAULT CHARSET=utf8 → （去掉）
  `` 反引号                          → 去掉或改为 "" 双引号

SQL（@Select 注解中）:
  NOW(), CURDATE()                    → CURRENT_TIMESTAMP, CURRENT_DATE
  LIMIT 1                             → FETCH FIRST 1 ROW ONLY

MyBatis-Plus 分页:
  配置 Db2Dialect 即可自动处理

MyBatis-Plus CRUD（LambdaQueryWrapper）:
  不受影响，MP 自动生成标准 SQL
```

---

## 3. 多 AS400 连接配置

### `application.yml`

```yaml
as400:
  systems:
    local:
      host: localhost              # US400A
      username: ${AS400A_USER:}
      password: ${AS400A_PASS:}
      default-libraries: A7RXUZZ1,A7RXUZZ2
    remoteB:
      host: 10.1.1.2               # US400B
      username: ${AS400B_USER:}
      password: ${AS400B_PASS:}
      default-libraries: MYLIB1,MYLIB2
    remoteC:
      host: 10.1.1.3               # US400C
      username: ${AS400C_USER:}
      password: ${AS400C_PASS:}
      default-libraries: MYLIB3,MYLIB4
```

### `As400Service` 扩展

当前 `As400Service` 单连接，改为多连接管理器：

```java
// As400ConnectionManager.java — 新增
// 根据 systemId 返回 AS400JDBCConnection
// 支持连接池复用

// As400Service.java — 修改
// 调用: as400ConnectionManager.getConnection("remoteB")
// 查询: QSYS2.OBJECT_STATISTICS 等表函数
```

---

## 4. 需删除的文件

### 4.1 后端 — 整个 literature 模块（107 个文件）

```
src/main/java/com/rx/admin/modules/literature/
├── common/
│   ├── entity/        Author, Dynasty, Genre, ContentCategory, LiteraryWork
│   ├── mapper/        5 mappers
│   ├── service/       5 services
│   ├── controller/    LiteratureController
│   ├── convert/       5 converters
│   ├── vo/            5 VOs
│   └── dto/           15 DTOs
├── honglou/           (红楼梦)  ~19 files
│   ├── entity/        HonglouPoem, HonglouCharacter, HonglouCharacterRelation
│   ├── mapper/        3 mappers
│   ├── service/       3 services
│   ├── controller/    HonglouController
│   ├── convert/       3 converters
│   ├── vo/            3 VOs
│   └── dto/           9 DTOs
├── xiyou/             (西游记)  ~19 files (same structure)
├── sanguo/            (三国演义) ~14 files (same structure, no relations)
└── shuihu/            (水浒传)  ~14 files (same structure, no relations)
```

### 4.2 前端 — 12 个 Vue 页面 + 5 个 API 文件

```
ui/src/views/classics/
├── honglou/poems/index.vue
├── honglou/characters/index.vue
├── honglou/relations/index.vue
├── xiyou/poems/index.vue
├── xiyou/characters/index.vue
├── xiyou/events/index.vue
├── sanguo/poems/index.vue
├── sanguo/characters/index.vue
├── shuihu/poems/index.vue
├── shuihu/chapters/index.vue
├── literature/index.vue
└── literature/works/index.vue

ui/src/api/
├── honglou.js
├── xiyou.js
├── sanguo.js
├── shuihu.js
└── literature.js
```

### 4.3 后端 — 音乐播放器模块（13 个 Java 文件）

```
src/main/java/com/rx/admin/modules/tool/music/
├── controller/    MusicController.java
├── service/       MusicService.java, MusicServiceImpl.java
├── entity/        Song.java, PlayRecord.java
├── mapper/        SongMapper.java, PlayRecordMapper.java
├── dto/           SongCreateDTO.java, SongUpdateDTO.java
├── vo/            SongVO.java, PlayRecordVO.java
└── convert/       SongConvert.java, PlayRecordConvert.java
```

### 4.4 前端 — 音乐播放器 + 知识图谱

```
ui/src/views/tool/musicPlayer/index.vue         # 音乐播放页面
ui/src/views/dashboard/knowledgeGraph/index.vue  # 知识图谱页面
ui/src/api/music.js                              # 音乐 API

ui/src/public/data/knowledge-graph.json          # 知识图谱静态数据
```

### 4.5 SQL 脚本（已执行过，可删除）

```
src/main/resources/db/classics_menu.sql
src/main/resources/db/literature_menu.sql
src/main/resources/db/honglou_characters.sql
src/main/resources/db/honglou_relations.sql
src/main/resources/sql/music_player_menu.sql
src/main/resources/sql/music_player_init.sql
src/main/resources/sql/knowledge_graph_menu.sql
```

---

## 5. 需修改的文件

### 5.1 `pom.xml`

```xml
<!-- 删除 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- JT400 已有的，保留 -->
<dependency>
    <groupId>net.sf.jt400</groupId>
    <artifactId>jt400</artifactId>
    <version>${jt400.version}</version>
</dependency>
```

### 5.2 `application.yml`

```yaml
spring:
  datasource:
    driver-class-name: com.ibm.as400.access.AS400JDBCDriver
    jdbc-url: jdbc:as400://localhost;naming=sql;errors=full;libraries=RXADMIN
    username: ${DB_USER:}
    password: ${DB_PASS:}

mybatis-plus:
  configuration:
    jdbc-type-for-null: 'null'
  global-config:
    db-config:
      id-type: input  # DB2 自增列 GENERATED AS IDENTITY
  # 分页方言: MyBatis-Plus 内置 DB2Dialect，无需额外配置
```

### 5.3 `RxAs400Application.java`

```java
@SpringBootApplication  // 去掉 exclude
@EnableScheduling
public class RxAs400Application {
```

### 5.4 数据源配置类

| 文件 | 操作 |
|------|------|
| `PrimaryDataSourceConfig.java` | 改为 PostgreSQL 单数据源 |
| `SecondDataSourceConfig.java` | **删除** |
| `SecondDB.java` | **删除** |

### 5.5 `ChinaRegionMapper.java`

删除 `@SecondDB` 注解，改为 `@Mapper`（纳入主数据源）：

```java
@Mapper  // ← 原 @SecondDB
public interface ChinaRegionMapper extends BaseMapper<ChinaRegion> {
```

### 5.6 `DashboardController.java`

- 删除 15 条 literature import
- 删除 literature/classics service 注入字段
- 删除 `computeStats()` 中的 literature 和 classics 块（约 130 行）
- 删除 `DashboardCache` 的 literature/classics dirty flag 调用

### 5.7 `DashboardCache.java`

```java
// 删除
private volatile boolean literatureDirty = true;
private volatile boolean classicsDirty = true;
// 保留
private volatile boolean techblogDirty = true;
```

### 5.8 `ApiAnalysisService.java`

- 删除 literature 实体路由到 `rxusysadmin` 的分析逻辑

### 5.9 前端路由

`ui/src/router/componentMap.js` — 删除：
- 12 个 `classics/` 条目
- `'tool/musicPlayer/index'` 条目
- `'dashboard/knowledgeGraph/index'` 条目

### 5.10 前端 API 路由

`ui/src/api/routes.js` — 删除：
- 整个 `CLASSICS` 对象（第 305-417 行）
- `API.MUSIC` 段（第 251-261 行）

### 5.11 前端 i18n

`ui/src/i18n/lang/zh-CN.js` + `en-US.js` — 删除：
- `classics:` 翻译块（第 692-753 行）
- `musicPlayer` 翻译块（第 496-519 行）

### 5.12 前端 Dashboard

`ui/src/views/dashboard/index.vue` — 删除：
- 文学统计卡片、四大名著图表、经典书籍卡片
- 音乐播放统计卡片
- `literatureStats` / `classicBooks` / `musicStats` computed 属性
- `MusicService` 注入（DashboardController.java）
- `computeStats()` 中的 `musicService.getPlayStats()` 调用
- 相关 CSS 样式

### 5.13 `application.yml` 清理

```yaml
# 删除
music:
  folder: C:/Users/admin/Downloads/music

# 删除（保留 SQL 目录本身）
src/main/resources/sql/  ← 确认不需要时清理

# 修改
common-tools:
  upload:
    dir: /app/rxadmin/shareddocs  # Windows 路径改为 IFS 路径
```

---

## 6. 无需修改的部分

| 模块 | 文件 | 说明 |
|------|------|------|
| AS400 | `modules/as400/` 全部 39 个 Java 文件 | 完整保留，只扩展多连接 |
| iService | 所有 catalog/parameter/column/example/authority | 完整保留 |
| TechBlog | 所有文章管理 + 爬虫 | 完整保留 |
| 前端 AS400 | `views/as400/` 4 个页面 | 完整保留 |
| 前端 API | `api/as400.js`, `api/iService.js`, `api/techBlog.js` | 完整保留 |
| 系统管理 | `modules/system/` 全部 | 用户/角色/菜单/部门/字典等完整保留 |
| 系统监控 | `modules/monitor/` 在线用户/日志/健康检查 | 完整保留 |
| 通知公告 | `modules/content/notice/` | 完整保留 |

## 7. `DashboardCache.java` 精简后状态

删除 literature/classics 脏标记后，仅保留 techblog：

```java
@Component
public class DashboardCache {

    private volatile boolean techblogDirty = true;

    public void markTechblogDirty() { this.techblogDirty = true; }
    public void clearTechblogDirty() { this.techblogDirty = false; }
    public boolean isTechblogDirty() { return techblogDirty; }
    public boolean hasAnyDirty() { return techblogDirty; }
    public void markAllDirty() { techblogDirty = true; }
}
```

---

## 7. 数据迁移

### 7.1 数据准备

在 US400A 上为系统表创建 DB2 库：

```sql
-- 在 US400A 的 DB2 上执行
CREATE SCHEMA RXADMIN;

-- 表结构需要手动创建（DB2 DDL 与原 MySQL DDL 差异见第 2 节）
-- 示例：
CREATE TABLE RXADMIN.SYS_USER (
    ID BIGINT GENERATED AS IDENTITY PRIMARY KEY,
    USERNAME VARCHAR(50) NOT NULL,
    PASSWORD VARCHAR(100) NOT NULL,
    ...
);
```

### 7.2 注意事项

| 差异点 | MySQL | DB2 for i |
|--------|-------|-----------|
| 自增 | `AUTO_INCREMENT` | `GENERATED AS IDENTITY` |
| 分页 | `LIMIT x OFFSET y` | `FETCH FIRST x ROWS ONLY` + `OFFSET` |
| 日期函数 | `NOW()`, `CURDATE()` | `CURRENT_TIMESTAMP`, `CURRENT_DATE` |
| 反引号 | `` ` `` | 去掉或改 `"` |
| JSON 操作 | `JSON_EXTRACT()` | `JSON_QUERY()` / `JSON_VALUE()` |
| 表名大小写 | 不敏感 | 敏感（建议全大写） |
| MyBatis-Plus 分页方言 | MySQLDialect 自动 | 需配置 `Db2Dialect` |

---

## 8. 部署到 US400A

### 8.1 环境要求

```
IBM i 版本: 7.4 或更高
Java 版本: 17 或更高 (5733-JV1 option 17)
IFS 空间: 至少 500MB
网络: 能访问 US400B + US400C（端口 8471 或 9471 JDBC）
DB2 库: 已创建 RXADMIN schema（或自定义名称）
```

### 8.2 部署步骤

```bash
# 1. 编译（开发机）
mvn clean package -DskipTests

# 2. 上传到 US400A IFS
#    目标: /app/rxadmin/rx-admin.jar
#    目标: /app/rxadmin/config/application.yml

# 3. 启动（US400A QSH 或 SSH）
cd /app/rxadmin
java -jar rx-admin.jar \
  --spring.config.additional-location=file:/app/rxadmin/config/application.yml

# 4. 验证
#    http://us400a:8088/doc.html  (Knife4j)
#    http://us400a:8088/h2-console (如启用)
```

### 8.3 其他配置适配

| 配置项 | 当前值（Windows） | 改后值（US400A IFS） |
|--------|------------------|---------------------|
| `common-tools.upload.dir` | `D:/vueprojects/RX/ui/public/shareddocs` | `/app/rxadmin/shareddocs` |
| `music.folder` | `C:/Users/admin/Downloads/music` | **已删除** |
| `logging.file.path` | 无 | `/app/rxadmin/logs` |

---

## 9. 其他考虑

| 关注点 | 建议 |
|--------|------|
| 文件上传 | 改用 IFS 路径 `/app/rxadmin/shareddocs` |
| 邮件 SMTP | 配置不变（SMTP 协议跨平台） |
| 缓存 | Caffeine 本地缓存，无需改 |
| Sa-Token | 内存模式，重启后重新登录 |
| 前端静态文件 | Vite 构建后，放入 IFS 由 Nginx 或 Java 内嵌容器 serve |
| 日志 | 输出到 IFS 文件 `/app/rxadmin/logs/` |

---

## 10. 归档文件列表

建议在 `docs/` 目录归档此方案不再适用的旧文档：

```
src/main/resources/docs/
  rxadmin.md                → 归档（原架构文档）
  rxadmin-setup.md          → 归档
  rxadmin-optimization.md   → 归档
  rxadmin-dev-skills.md     → 归档
  SKILL.md                  → 归档
  as400-migration-plan.md   ← 本文件（新架构文档）
```
