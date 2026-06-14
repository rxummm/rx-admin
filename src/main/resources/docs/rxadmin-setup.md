# RX Admin — 项目搭建与新增模块指南

> **版本**: 1.2.0 | **更新日期**: 2026-06-15 | **适用对象**: 新加入开发者
>
> **v1.2 更新**: 补充 sass-embedded（非 Dart Sass）+ font self-hosting（@fontsource）+ build 优化（manualChunks）

---

## 目录

1. [环境准备](#1-环境准备)
2. [项目克隆与初始化](#2-项目克隆与初始化)
3. [数据库配置](#3-数据库配置)
4. [后端启动](#4-后端启动)
5. [前端启动](#5-前端启动)
6. [新增业务模块完整流程](#6-新增业务模块完整流程)
   - [6.1 数据库设计](#61-数据库设计)
   - [6.2 后端代码生成](#62-后端代码生成)
   - [6.3 后端手动调整](#63-后端手动调整)
   - [6.4 前端代码开发](#64-前端代码开发)
   - [6.5 菜单配置](#65-菜单配置)
   - [6.6 联调测试](#66-联调测试)
7. [使用代码生成器快速开发](#7-使用代码生成器快速开发)
8. [项目目录速查](#8-项目目录速查)
9. [常用命令](#9-常用命令)
10. [常见问题排查](#10-常见问题排查)

---

## 1. 环境准备

| 环境 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 17+ | 推荐 Adoptium Eclipse Temurin 17 |
| **Maven** | 3.8+ | 后端构建工具 |
| **Node.js** | 18+ | 前端运行时 |
| **npm** | 9+ | 前端包管理 |
| **MySQL** | 8.0+ | 主数据库 |
| **Git** | 2.30+ | 版本控制 |
| **IDE** | IntelliJ IDEA / VS Code | 推荐使用 IDE 进行开发 |

### 环境验证

```bash
java -version     # 应显示 17.x
mvn -v            # 应显示 3.8+
node -v           # 应显示 18.x+
npm -v            # 应显示 9.x+
mysql --version   # 应显示 8.0.x
```

---

## 2. 项目克隆与初始化

```bash
# 克隆项目
git clone <repository-url>
cd RX

# 后端编译（首次需要下载依赖，时间较长）
mvn clean compile -DskipTests

# 前端安装依赖
cd ui
npm install
```

---

## 3. 数据库配置

### 3.1 创建数据库

```sql
-- 主数据源：系统管理数据
CREATE DATABASE IF NOT EXISTS rx_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 第二数据源：四大名著业务数据
CREATE DATABASE IF NOT EXISTS rxusysadmin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3.2 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/rx_admin?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      username: root
      password: your_password
      driver-class-name: com.mysql.cj.jdbc.Driver
    second:
      url: jdbc:mysql://localhost:3306/rxusysadmin?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      username: root
      password: your_password
      driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3.3 初始化数据

导入 `docs/` 目录下的 SQL 初始化脚本（如有），至少需要 `sys_user` 表有初始管理员账号用于登录。

---

## 4. 后端启动

### 4.1 方式一：Maven 命令

```bash
# 在项目根目录执行
mvn spring-boot:run
```

### 4.2 方式二：IDE 运行

在 IntelliJ IDEA 中，直接运行 `RxAdminApplication.java` 的 `main` 方法。

### 4.3 验证启动成功

- 访问 `http://localhost:8088` 确认服务启动
- 访问 `http://localhost:8088/doc.html` 查看 Knife4j API 文档

### 4.4 启动类说明

```java
// RxAdminApplication.java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class RxAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(RxAdminApplication.class, args);
    }
}
```

> **注意**: 因为要手动配置双数据源，所以必须排除 `DataSourceAutoConfiguration`。

---

## 5. 前端启动

```bash
cd ui

# 开发模式启动（默认端口 5173）
npm run dev

# 生产构建
npm run build

# 分析构建产物体积（需先 npm run build）
npm run analyze
```

### 5.1 Vite 代理配置

`ui/vite.config.js` 中配置了代理，将 `/api` 请求转发到后端 `http://localhost:8088`：

```javascript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8088',
      changeOrigin: true
    }
  }
}
```

### 5.2 验证前端启动

- 访问 `http://localhost:5173` 进入登录页面
- 使用管理员账号登录（默认用户名/密码见数据库初始化脚本）

---

## 6. 新增业务模块完整流程

以新增一个"文章管理"模块为例，演示从零到一的全流程。

### 6.1 数据库设计

**Step 1: 创建数据库表**

```sql
CREATE TABLE `article` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title`       VARCHAR(200) NOT NULL                COMMENT '文章标题',
  `content`     TEXT                                 COMMENT '文章内容',
  `author`      VARCHAR(100)                         COMMENT '作者',
  `category`    VARCHAR(50)                          COMMENT '分类',
  `status`      TINYINT      DEFAULT 1               COMMENT '状态(1=正常/0=禁用)',
  `sort`        INT          DEFAULT 0               COMMENT '排序',
  `deleted`     TINYINT      DEFAULT 0               COMMENT '逻辑删除(0=未删除/1=已删除)',
  `create_time` DATETIME                             COMMENT '创建时间',
  `update_time` DATETIME                             COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章管理';
```

### 6.2 后端代码生成

**推荐方式：使用内置代码生成器**

访问系统菜单 → 系统工具 → 代码生成，三步完成：
1. 选择目标表 `article`
2. 配置生成选项（包名、作者、模块名等）
3. 预览并确认生成

生成后自动创建以下文件：

```
后端:
├── entity/Article.java
├── mapper/ArticleMapper.java
├── resources/mapper/ArticleMapper.xml
├── service/ArticleService.java
├── service/impl/ArticleServiceImpl.java
├── controller/ArticleController.java
└── modules/content/article/
    ├── dto/ArticleCreateDTO.java
    ├── dto/ArticleUpdateDTO.java
    ├── dto/ArticleQueryDTO.java
    ├── vo/ArticleVO.java
    └── convert/ArticleConvert.java

前端:
├── api/article.js
└── views/content/article/index.vue
```

### 6.3 后端手动调整

代码生成器生成后，需要手动调整以下内容以符合项目规范：

#### 6.3.1 修复 Convert 接口

```java
// modules/content/article/convert/ArticleConvert.java
// 必须添加 unmappedTargetPolicy 和 @BeanMapping 配置
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleConvert {

    Article toEntity(ArticleCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ArticleUpdateDTO dto, @MappingTarget Article entity);

    ArticleVO toVO(Article entity);

    List<ArticleVO> toVOList(List<Article> list);
}
```

#### 6.3.2 修复 Controller 依赖注入

```java
// controller/ArticleController.java
// 将 @Autowired 字段注入改为构造器注入
@RestController
@RequestMapping("/content/article")
public class ArticleController extends BaseCrudController<ArticleService, Article> {

    private final ArticleConvert articleConvert;

    public ArticleController(ArticleService articleService, ArticleConvert articleConvert) {
        super(articleService);
        this.articleConvert = articleConvert;
    }

    @GetMapping("/page")
    public Result<PageResult<ArticleVO>> page(ArticleQueryDTO dto) {
        IPage<Article> page = baseService.page(
            new Page<>(dto.getPage(), dto.getPageSize()),
            buildQueryWrapper(dto)
        );
        List<ArticleVO> voList = articleConvert.toVOList(page.getRecords());
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody ArticleCreateDTO dto) {
        baseService.save(articleConvert.toEntity(dto));
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ArticleUpdateDTO dto) {
        Article entity = baseService.getById(dto.getId());
        if (entity == null) {
            throw new BusinessException("文章不存在");
        }
        articleConvert.updateEntity(dto, entity);
        baseService.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        baseService.removeById(id);
        return Result.ok();
    }

    // 查询条件构建辅助方法
    private LambdaQueryWrapper<Article> buildQueryWrapper(ArticleQueryDTO dto) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getTitle()), Article::getTitle, dto.getTitle());
        wrapper.eq(dto.getStatus() != null, Article::getStatus, dto.getStatus());
        wrapper.orderByDesc(Article::getSort).orderByDesc(Article::getCreateTime);
        return wrapper;
    }
}
```

#### 6.3.3 添加操作日志注解（可选）

```java
@OperateLog(module = "文章管理", operation = "新增文章")
@PostMapping
public Result<Void> add(@Valid @RequestBody ArticleCreateDTO dto) {
    // ...
}
```

#### 6.3.4 编译验证

```bash
mvn clean compile
```

确保没有编译错误和 MapStruct "Unmapped target properties" 警告。

### 6.4 前端代码开发

#### 6.4.1 API 模块

```javascript
// ui/src/api/article.js
import request from '@/utils/request'

export function getArticlePage(params) {
  return request({ url: '/content/article/page', method: 'get', params })
}

export function getArticleById(id) {
  return request({ url: `/content/article/${id}`, method: 'get' })
}

export function addArticle(data) {
  return request({ url: '/content/article', method: 'post', data })
}

export function updateArticle(data) {
  return request({ url: '/content/article', method: 'put', data })
}

export function deleteArticle(id) {
  return request({ url: `/content/article/${id}`, method: 'delete' })
}
```

#### 6.4.2 页面组件

```vue
<!-- ui/src/views/content/article/index.vue -->
<script setup>
import { useTablePage } from '@/composables/useTablePage'
import { getArticlePage, addArticle, updateArticle, deleteArticle } from '@/api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ref } from 'vue'

const { loading, tableData, total, queryParams, handleSearch, handleReset, handlePageChange } =
  useTablePage(getArticlePage, { title: '', status: null })

const dialogVisible = ref(false)
const formData = ref({})

const handleAdd = () => {
  dialogVisible.value = true
  formData.value = { status: 1, sort: 0 }
}

const handleEdit = (row) => {
  dialogVisible.value = true
  formData.value = { ...row }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该文章？')
  await deleteArticle(id)
  ElMessage.success('删除成功')
  handleSearch()
}

const handleSubmit = async () => {
  await (formData.value.id ? updateArticle(formData.value) : addArticle(formData.value))
  ElMessage.success('保存成功')
  dialogVisible.value = false
  handleSearch()
}
</script>

<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="queryParams.title" placeholder="文章标题" clearable style="width: 200px" />
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleAdd">新增</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑文章' : '新增文章'" width="600px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="formData.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="formData.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="formData.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="formData.content" type="textarea" :rows="5" placeholder="请输入文章内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
// 样式使用 CSS 变量支持主题切换
</style>
```

#### 6.4.3 注册路由映射

```javascript
// ui/src/router/componentMap.js
// 在 componentMap 对象中添加新条目
export const componentMap = {
  // ... 现有 50+ 个映射
  'content/article': () => import('@/views/content/article/index.vue'),
}
```

### 6.5 菜单配置

在 `sys_menu` 表中插入菜单记录：

```sql
INSERT INTO sys_menu (name, path, component, icon, parent_id, type, permission, sort, status, create_time, update_time)
VALUES ('文章管理', '/article', 'content/article', 'Document', 4, 'menu', 'article:view', 1, 1, NOW(), NOW());
```

> **说明**: `parent_id` = 4 表示挂载到"内容管理"菜单下，`component` 值必须与 `componentMap` 的 key 一致。

### 6.6 联调测试

1. 重启后端服务
2. 刷新前端页面，确认新菜单出现
3. 测试 CRUD 操作：新增 → 搜索 → 编辑 → 删除
4. 检查操作日志是否正常记录
5. 检查 API 文档（Knife4j）是否正常显示新接口

---

## 7. 使用代码生成器快速开发

RX Admin 内置了完整的代码生成器，位于系统工具 → 代码生成菜单。

### 三步生成流程

| 步骤 | 操作 | 说明 |
|------|------|------|
| **Step 1: 选择表** | 勾选目标数据表 | 从数据库表列表中选择 |
| **Step 2: 配置选项** | 填写包名、模块名、作者 | 如 `com.rx.admin.modules.content.article` |
| **Step 3: 预览生成** | 预览代码并确认 | 展示将要生成的所有文件 |

### 生成后处理清单

- [ ] 修改 Convert 接口：添加 `unmappedTargetPolicy = ReportingPolicy.IGNORE`
- [ ] 修改 updateEntity 方法：添加 `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`
- [ ] 修改 Controller：将 `@Autowired` 改为构造器注入
- [ ] 修改 Controller：`add` 和 `update` 方法使用 Convert 转换
- [ ] 添加查询条件构建方法（`buildQueryWrapper`）
- [ ] 前端：在 `componentMap.js` 中注册路由映射
- [ ] 执行 `mvn clean compile` 验证编译通过

---

## 8. 项目目录速查

### 后端关键路径

| 路径 | 说明 |
|------|------|
| `src/main/java/com/rx/admin/RxAdminApplication.java` | 启动类 |
| `src/main/java/com/rx/admin/common/` | 公共模块 |
| `src/main/java/com/rx/admin/framework/` | 框架层配置 |
| `src/main/java/com/rx/admin/modules/` | 业务模块 DTO/VO/Convert |
| `src/main/java/com/rx/admin/entity/` | 实体定义 |
| `src/main/java/com/rx/admin/mapper/` | 数据访问层 |
| `src/main/java/com/rx/admin/service/` | 服务层 |
| `src/main/java/com/rx/admin/controller/` | 控制器 |
| `src/main/resources/application.yml` | 配置文件 |
| `src/main/resources/mapper/` | MyBatis XML 映射文件 |
| `src/main/resources/docs/` | 项目文档 |

### 前端关键路径

| 路径 | 说明 |
|------|------|
| `ui/src/main.js` | 应用入口 |
| `ui/src/App.vue` | 根组件 |
| `ui/src/api/` | API 请求模块 |
| `ui/src/api/modules/` | 模块化 API 聚合入口 |
| `ui/src/composables/` | 组合式函数 |
| `ui/src/router/index.js` | 路由配置 |
| `ui/src/router/componentMap.js` | 组件映射表 |
| `ui/src/stores/` | 状态管理 |
| `ui/src/views/` | 页面视图 |
| `ui/src/layout/` | 布局组件 |
| `ui/src/components/` | 公共组件 |
| `ui/src/i18n/` | 国际化 |
| `ui/src/styles/` | 全局样式 |
| `ui/vite.config.js` | Vite 配置 |

---

## 9. 常用命令

### 后端

```bash
mvn clean compile                          # 编译项目
mvn clean package -DskipTests              # 打包
mvn spring-boot:run                        # 启动服务
mvn clean                                  # 清理构建产物
```

### 前端

```bash
cd ui
npm install                                # 安装依赖
npm run dev                                # 启动开发服务器
npm run build                              # 生产构建（含 8 个 manualChunks 分包优化）
npm run analyze                            # 分析构建产物体积（依赖 rollup-plugin-visualizer）
npm run preview                            # 预览生产构建
```

### 数据库

```bash
# 备份
mysqldump -u root -p rx_admin > rx_admin_backup.sql

# 恢复
mysql -u root -p rx_admin < rx_admin_backup.sql
```

---

## 10. 常见问题排查

### 10.1 后端启动失败

**问题**: `Failed to configure a DataSource: 'url' attribute is not specified`

**解决**: 检查 `application.yml` 中的双数据源配置是否正确，特别是 `primary` 和 `second` 的 `url`、`username`、`password`。

### 10.2 MapStruct 编译错误

**问题**: `No implementation was created for ArticleConvert`

**解决**:
1. 确保 Convert 接口添加了 `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`
2. 检查 DTO 字段名是否与 Entity 一致
3. 执行 `mvn clean compile`

### 10.3 IDE 无法识别 MapStruct 生成代码

**问题**: `The import com.rx.admin.modules.xxx.convert.ArticleConvertImpl cannot be resolved`

**解决**:
1. 确认 `pom.xml` 中已配置 `build-helper-maven-plugin`：
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals><goal>add-source</goal></goals>
            <configuration>
                <sources>
                    <source>${project.build.directory}/generated-sources/annotations</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```
2. 执行 `mvn clean compile`
3. 在 VS Code 中执行命令面板 `Java: Clean Java Language Server Workspace`

### 10.4 前端菜单不显示

**解决**:
1. 确认 `sys_menu` 表中已添加菜单记录
2. 确认 `componentMap.js` 中已注册组件映射
3. 确认菜单的 `component` 值与 `componentMap` key 一致
4. 刷新页面后重新登录

### 10.5 前端 API 请求 404

**解决**:
1. 确认后端 Controller 的 `@RequestMapping` 路径与前端 API 文件一致
2. 确认 Vite 代理配置正确（`/api` → `http://localhost:8088`）
3. 检查后端服务是否正常启动

### 10.6 双数据源切换问题

**解决**:
- 系统管理相关表（以 `sys_` 开头）→ 主数据源 `rx_admin`，无需额外注解
- 四大名著相关表（`honglou_`/`sanguo_`/`shuihu_`/`xiyou_`/`literature_`/`china_regions`）→ 第二数据源 `rxusysadmin`，Mapper 必须标注 `@SecondDB`

---

> **文档维护**: 本文档为 RX Admin 项目搭建与新增模块指南，随项目迭代持续更新。
> **历史版本**: v1.0.0 (2026-06-10) → v1.1.0 (2026-06-13): 适配 Spring Boot 3.5.15 + MapStruct 规范 + 构造器注入 + build-helper-maven-plugin + 完整新增模块流程 → v1.2.0 (2026-06-15): sass-embedded + build 优化 + font self-hosting