# Git 版本控制功能集成方案

> 本文档为 RX Admin 项目 Git 版本控制功能集成方案的技术设计文档，包含完整的需求评估、技术选型、代码示例和实施步骤。

---

## 18. Git 版本控制功能集成方案（待实施）

> **状态**: 规划中 | **优先级**: 低 | **最后评估**: 2026-06-06

### 18.1 需求评估与定位

#### 18.1.1 需求概述

在 RX Admin 管理系统中集成 Git 操作能力，允许通过 Web 界面查看和管理 Git 仓库状态。

#### 18.1.2 适用场景分析

| 场景 | 是否有价值 | 说明 |
|------|-----------|------|
| **生产服务器无图形界面**，需确认运行版本 | ✅ 有价值 | SSH 只能命令行，Web 面板看 diff/log 更直观 |
| **团队多人协作**，管理员统一查看仓库状态 | ⚠️ 有限 | 通常用 GitHub/GitLab 自带界面即可 |
| **个人开发或小团队**，日常用 VS Code + 终端 | ❌ 无必要 | `git status` / `git log` / `git diff` 1 秒完成，更快更安全 |
| **自动化运维**，CI/CD 流程中触发 Git 操作 | ❌ 不适用 | 应由 CI/CD 工具（GitHub Actions/Jenkins）完成 |

#### 18.1.3 终端 vs Web 后台对比

| 操作 | 终端/VS Code | Web 后台 |
|------|-------------|----------|
| 查看状态 | `git status`（1 秒） | 打开浏览器 → 登录 → 导航 → 等加载 |
| 查看日志 | `git log --oneline -10`（1 秒） | 同上 |
| 查看差异 | `git diff 文件` 或 VS Code Git 面板 | 同上 → 点文件 → 弹窗 |
| 提交代码 | `git add . && git commit -m "xxx"`（3 秒） | 需 Token 认证 + 写消息 + 存在安全隐患 |
| 推送到 GitHub | `git push` 或 VS Code 点同步（1 秒） | 需额外配置 Token，安全风险高 |

**核心结论：对于日常用 VS Code + 终端的开发者，Web 后台 Git 功能是不必要的重复建设。** 该功能仅在服务器无人值守、无法直接执行命令的场景下有实际价值。

### 18.2 功能范围（推荐只做只读）

| 功能 | 说明 | 风险 | 是否推荐 |
|------|------|------|----------|
| **仓库状态查看** | 当前分支、修改/暂存/未跟踪文件列表 | 低 | ✅ |
| **提交历史** | 分页查看 commit log（作者/时间/消息） | 低 | ✅ |
| **文件差异对比** | 查看工作区 vs HEAD 的 diff | 低 | ✅ |
| **分支列表** | 本地/远程分支，高亮当前分支 | 低 | ✅ |
| **远程信息** | 查看 remote URL | 低 | ✅ |
| **拉取更新** | `git pull` 更新到最新 | 中 | ⚠️ 需 Token |
| **暂存提交** | `git add` + `git commit` | 中-高 | ❌ 不推荐 |
| **推送到远程** | `git push` 到 GitHub | 高 | ❌ 不推荐 |

### 18.2.1 完整 Commit+Push 流程（不推荐实现）

如果确实需要在 Web 界面实现完整的 `commit + push` 流程，额外需要：

**GitHub Token 获取方式：**

浏览器登录 github.com → 右上角头像 → Settings → Developer settings（左侧最下）→ Personal access tokens → Tokens (classic) → Generate new token → 勾选 `repo` → 生成后显示 `ghp_xxxxxxxxxx`（仅显示一次，立即复制保存）。

**JGit commit/push 核心代码：**

```java
// 暂存
git.add().addFilepattern(".").call();

// 提交
git.commit()
    .setAuthor(name, email)
    .setMessage("commit message")
    .call();

// 推送
git.push()
    .setCredentialsProvider(
        new UsernamePasswordCredentialsProvider(username, token))
    .call();
```

**需要的 API 接口：** `POST /api/git/add`、`POST /api/git/commit`、`POST /api/git/push`

**安全风险：** Token 泄露风险、误操作 push、commit 消息滥写、并发冲突。需限制仅超级管理员操作，Token 必须通过环境变量注入而非写入配置文件。

> **再次强调：日常开发用终端 `git add/commit/push` 比 Web 后台快 10 倍且更安全，不建议实现此功能。**

### 18.3 技术方案

#### 18.3.1 依赖选择: JGit

| 方案 | 优点 | 缺点 | 推荐 |
|------|------|------|------|
| **JGit** (org.eclipse.jgit) | 纯 Java，无外部依赖，跨平台，API 完整 | 文档较少，版本需匹配 | ✅ **推荐** |
| Git 命令行 (`Runtime.exec()`) | 功能完整，文档丰富 | 依赖系统安装 Git，跨平台兼容性差，输出需解析 | ❌ 不推荐 |
| GitLab/GitHub API | RESTful，安全 | 依赖第三方平台，需 Token | ❌ 场景不匹配 |
| git2 (libgit2 绑定) | 性能高 | 需要本地编译 native库，部署复杂 | ❌ 不推荐 |

**选用 JGit 6.x**（要求 Java 11+，RX 项目使用 Java 17，完全兼容）。

#### 18.3.2 Maven 依赖 (pom.xml 新增)

```xml
<!-- JGit: 纯 Java Git 操作库 -->
<dependency>
    <groupId>org.eclipse.jgit</groupId>
    <artifactId>org.eclipse.jgit</artifactId>
    <version>6.9.0.202403050737-r</version>
</dependency>

<!-- JGit SSH 支持（如需通过 SSH 协议操作远程仓库） -->
<dependency>
    <groupId>org.eclipse.jgit</groupId>
    <artifactId>org.eclipse.jgit.ssh.apache</artifactId>
    <version>6.9.0.202403050737-r</version>
</dependency>
```

#### 18.3.3 前端依赖

**无需新增前端依赖**，使用现有的 Axios + Element Plus 即可。如需代码 diff 高亮展示，可选用 `diff2html`：

```bash
cd ui && npm install diff2html --save
```

### 18.4 后端实现

#### 18.4.1 配置项 (application.yml 新增)

```yaml
# Git 仓库配置
git:
  # 仓库根目录路径（必填，建议指向项目根目录或指定仓库路径）
  repository-path: ${user.dir}
  # 远程认证用户名（可选，仅 pull 时需要）
  remote-username: ${GIT_USERNAME:}
  # 远程认证密码/Token（可选，仅 pull 时需要，建议用环境变量）
  remote-password: ${GIT_PASSWORD:}
```

#### 18.4.2 包结构

```
com.rx.admin.git/
├── GitConfig.java              # Git 配置属性类 @ConfigurationProperties
├── GitService.java             # Git 操作服务接口
├── GitServiceImpl.java         # Git 操作服务实现
├── controller/
│   └── GitController.java      # Git API 控制器
└── dto/
    ├── GitStatusVO.java        # 仓库状态 VO
    ├── GitCommitVO.java        # 提交记录 VO
    ├── GitDiffVO.java          # 文件差异 VO
    ├── GitBranchVO.java        # 分支信息 VO
    └── GitPullResultVO.java    # Pull 结果 VO
```

#### 18.4.3 核心代码示例

**GitConfig.java** — 配置属性绑定：

```java
package com.rx.admin.git;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "git")
public class GitConfig {
    /** Git 仓库根目录路径 */
    private String repositoryPath;
    /** 远程认证用户名（可选） */
    private String remoteUsername;
    /** 远程认证密码/Token（可选） */
    private String remotePassword;
}
```

**GitServiceImpl.java** — 核心操作（关键方法）：

```java
package com.rx.admin.git;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor  // 构造器注入（符合项目规范）
public class GitService {

    private final GitConfig gitConfig;

    /** 打开本地仓库 */
    private Git openRepo() throws Exception {
        return Git.open(new File(gitConfig.getRepositoryPath(), ".git"));
    }

    /** 1. 获取仓库状态：修改/新增/删除文件列表 */
    public GitStatusDTO status() {
        try (Git git = openRepo()) {
            Status status = git.status().call();
            return GitStatusDTO.builder()
                .branch(git.getRepository().getBranch())
                .modified(status.getModified())
                .added(status.getAdded())
                .removed(status.getRemoved())
                .untracked(status.getUntracked())
                .conflicting(status.getConflicting())
                .isClean(status.isClean())
                .build();
        } catch (Exception e) { throw new RuntimeException("获取状态失败", e); }
    }

    /** 2. 提交历史（分页） */
    public List<GitCommitDTO> log(int maxCount) {
        try (Git git = openRepo()) {
            List<GitCommitDTO> list = new ArrayList<>();
            for (RevCommit commit : git.log().setMaxCount(maxCount).call()) {
                list.add(GitCommitDTO.builder()
                    .hash(commit.getName())
                    .shortHash(commit.getName().substring(0, 7))
                    .author(commit.getAuthorIdent().getName())
                    .email(commit.getAuthorIdent().getEmailAddress())
                    .message(commit.getShortMessage())
                    .timestamp(commit.getCommitTime() * 1000L)
                    .build());
            }
            return list;
        } catch (Exception e) { throw new RuntimeException("获取日志失败", e); }
    }

    /** 3. 文件差异对比 */
    public String diff(String filePath) {
        try (Git git = openRepo()) {
            Repository repo = git.getRepository();
            // 获取 HEAD 的 tree
            ObjectId head = repo.resolve("HEAD^{tree}");
            if (head == null) return "无提交记录";
            // 工作区文件内容 vs HEAD
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffCommand diffCmd = git.diff()
                .setOutputStream(out)
                .setPathFilter(org.eclipse.jgit.treewalk.filter.PathFilter.create(filePath));
            diffCmd.call();
            return out.toString("UTF-8");
        } catch (Exception e) { throw new RuntimeException("获取差异失败", e); }
    }

    /** 4. 分支列表 */
    public List<GitBranchDTO> branches() {
        try (Git git = openRepo()) {
            String current = git.getRepository().getBranch();
            List<GitBranchDTO> list = new ArrayList<>();
            for (Ref ref : git.branchList().call()) {
                String name = Repository.shortenRefName(ref.getName());
                list.add(GitBranchDTO.builder()
                    .name(name).isCurrent(name.equals(current)).build());
            }
            return list;
        } catch (Exception e) { throw new RuntimeException("获取分支失败", e); }
    }

    /** 5. Pull 拉取更新 */
    public GitPullResultDTO pull() {
        try (Git git = openRepo()) {
            PullCommand pull = git.pull();
            String username = gitConfig.getRemoteUsername();
            String password = gitConfig.getRemotePassword();
            if (username != null && !username.isEmpty()) {
                pull.setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider(username, password));
            }
            PullResult result = pull.call();
            return GitPullResultDTO.builder()
                .successful(result.isSuccessful())
                .fetchResult(result.getFetchResult().getMessages())
                .mergeResult(result.getMergeResult().toString())
                .build();
        } catch (Exception e) { throw new RuntimeException("拉取失败", e); }
    }
}
```

#### 18.4.4 API 接口清单

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| `GET` | `/api/git/status` | 仓库状态（分支+变更文件） | 管理员 |
| `GET` | `/api/git/log?max=20` | 提交历史（分页） | 管理员 |
| `GET` | `/api/git/diff?file=xxx` | 文件差异对比 | 管理员 |
| `GET` | `/api/git/branches` | 分支列表 | 管理员 |
| `GET` | `/api/git/remotes` | 远程仓库信息 | 管理员 |
| `POST` | `/api/git/pull` | 拉取更新 | 超级管理员 |

#### 18.4.5 安全策略

```
1. 仅管理员角色可访问 GET 接口（Sa-Token @SaCheckRole("admin")）
2. pull 操作仅超级管理员可执行（@SaCheckPermission("git:pull")）
3. 仓库路径在配置文件中锁定，不可通过 API 参数修改，防止路径遍历攻击
4. 远程密码通过环境变量注入，不写入配置文件（生产环境推荐）
5. pull 操作记录到操作日志（@OperateLog），便于审计
```

### 18.5 前端实现

#### 18.5.1 页面路由

新增页面 `views/tool/git/index.vue`，在 `componentMap.js` 中注册：

```js
// 新增映射
'git': () => import('@/views/tool/git/index.vue'),
```

#### 18.5.2 API 模块 (api/git.js)

```js
import request from '@/utils/request'

export function getGitStatus() { return request.get('/api/git/status') }
export function getGitLog(max = 30) { return request.get('/api/git/log', { params: { max } }) }
export function getGitDiff(filePath) { return request.get('/api/git/diff', { params: { file: filePath } }) }
export function getGitBranches() { return request.get('/api/git/branches') }
export function gitPull() { return request.post('/api/git/pull') }
```

#### 18.5.3 页面布局设计

页面采用四区域布局：

```
┌──────────────────────────────────────────┐
│  Git 仓库管理                 [刷新] [拉取]  │
├──────────┬───────────────────┬───────────┤
│ 分支列表  │  文件状态 (Tab切换) │  提交历史  │
│ ┌──────┐ │ ┌───────────────┐ │ ┌───────┐ │
│ │main ✓│ │ │ 📝 modified/  │ │ │abc123 │ │
│ │dev   │ │ │ ➕ added/     │ │ │def456 │ │
│ │feat..│ │ │ ➖ removed/   │ │ │...    │ │
│ └──────┘ │ │ ❓ untracked  │ │ └───────┘ │
└──────────┴───────────────────┴───────────┘
```

文件差异弹窗（点击文件查看 diff，使用 diff2html 渲染）。

### 18.6 所需账号与凭据

#### 18.6.1 本地仓库（只读操作）

**无需任何额外账号**。JGit 直接读取本地 `.git` 目录，状态/日志/差异/分支均为纯本地操作。

#### 18.6.2 远程拉取（Pull 操作）

需要远程仓库认证凭据，根据远程仓库类型选择：

| 远程平台 | 认证方式 | 所需凭据 | 获取方式 |
|----------|----------|----------|----------|
| **GitHub** | Personal Access Token | `username` + `token` | GitHub → Settings → Developer settings → Personal access tokens → Generate (勾选 `repo` 权限) |
| **GitLab** | Personal Access Token | `username` + `token` | GitLab → Settings → Access Tokens → Create (勾选 `read_repository`) |
| **Gitee** | 密码 或 Token | `username` + `password/token` | Gitee → 设置 → 私人令牌 → 生成 |
| **私有 Git 服务器** | 密码 | `username` + `password` | 联系管理员 |
| **SSH 协议** | SSH 密钥 | 无需密码，需配置私钥路径 | `~/.ssh/id_rsa` 需可访问 |

> **推荐 GitHub**: 使用 **Fine-grained Personal Access Token**（仅 `Contents: Read` 权限 + 仓库选择），安全性最高。

#### 18.6.3 凭据配置方式

```yaml
# 方式一：开发环境（application-local.yml，不提交 Git）
git:
  remote-username: rx-admin
  remote-password: ghp_xxxxxxxxxxxxxxxxxxxx

# 方式二：生产环境（环境变量，推荐）
# 启动时：java -DGIT_USERNAME=rx-admin -DGIT_PASSWORD=ghp_xxx -jar app.jar
# 或：export GIT_USERNAME=rx-admin; export GIT_PASSWORD=ghp_xxx

# 方式三：K8s/Docker Secret（生产环境最安全）
# 通过 Secret 挂载为环境变量
```

### 18.7 数据库菜单配置

```sql
-- 在 sys_menu 表中新增 Git 管理菜单记录
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `icon`, `type`, 
  `sort`, `status`, `visible`, `permission`, `deleted`) 
VALUES 
(2, 'Git管理', '/tool/git', 'git', 'git:git-line', 1, 50, 1, 1, 'tool:git:list', 0);
```

### 18.8 实施步骤

| 步骤 | 操作 | 涉及文件 |
|------|------|----------|
| 1 | 在 `pom.xml` 添加 JGit 依赖 | `pom.xml` |
| 2 | 创建 `com.rx.admin.git` 包，编写 6 个 DTO/Service/Controller | 6 个 Java 文件 |
| 3 | 在 `application.yml` 添加 `git` 配置节点 | `application.yml`（及 `-local`、`-prod`） |
| 4 | 前端创建 `api/git.js` | `api/git.js` |
| 5 | 前端创建 `views/tool/git/index.vue` | `views/tool/git/index.vue` |
| 6 | 在 `componentMap.js` 注册路由 | `router/componentMap.js` |
| 7 | 执行 SQL 添加菜单（可选，也可通过菜单管理页面手动添加） | 数据库 |
| 8 | 获取远程仓库 Token（仅 pull 功能需要） | — |
| 9 | 测试只读功能（状态/日志/分支/差异） | — |
| 10 | 如需 Pull，配置凭据并测试 | — |

### 18.9 注意事项

1. **仓库路径**: 默认为 `${user.dir}`（项目运行目录），确保该目录有 `.git` 子目录
2. **大仓库性能**: JGit 对大仓库（>1GB / >10000 文件）的 status 操作可能较慢，建议限制 `maxCount`
3. **文件锁定**: JGit 操作期间会锁定索引文件，避免并发写入操作
4. **编码**: diff 输出默认 UTF-8，中文文件路径和内容正常显示
5. **时间格式**: JGit 返回 Unix 时间戳（秒），前端需 `new Date(ts * 1000)` 转换
6. **Pull 并发**: 勿并发执行 pull，建议前端加防抖 + loading 状态
7. **凭据安全**: 密码/Token 务必通过环境变量注入，不要硬编码或写入配置文件提交到 Git

### 18.10 扩展可能

| 扩展功能 | 说明 | 复杂度 |
|----------|------|--------|
| **标签管理** | 查看/创建 tag 列表 | 低 |
| **文件回滚** | `git checkout -- <file>` 恢复单个文件 | 中 |
| **Stash 管理** | 查看/应用/删除 stash | 中 |
| **多仓库管理** | 配置多个仓库路径，下拉选择 | 中 |
| **Webhook 触发** | 接收远程 Webhook 后自动 pull | 高 |
| **commit 图表** | 类似 `git log --graph` 的可视化分支图 | 高 |
| **在线编辑并提交** | 编辑文件 → commit → push 完整流程 | 高（安全风险大） |

### 18.11 Git 命令行替代方案（不推荐但可备选）

如果 JGit 无法满足需求（如需要 `git lfs`、`git submodule` 等高级功能），可回退到命令行方案：

```java
// 通过 ProcessBuilder 执行系统 Git 命令（备选方案）
ProcessBuilder pb = new ProcessBuilder("git", "status", "--porcelain");
pb.directory(new File(gitConfig.getRepositoryPath()));
Process p = pb.start();
String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
```

但此方案有以下缺陷：
- 必须在服务器安装 Git（`apt install git` / `brew install git`）
- Windows/Linux/Mac 行为差异
- 输出需手动解析（不稳定）
- 无法捕获部分操作细节（merge 冲突等）

**结论：首选 JGit，只有遇到 JGit 不支持的功能时才考虑命令行。**


---



---

# 远期规划与实施优先级

> 以下内容从主文档第 19 节移入，包含多租户远期规划与实施优先级矩阵。

### 19.7 多租户扩展（远期规划）

如果未来需要 SaaS 化对外提供服务：

| 层面 | 方案 |
|------|------|
| **数据隔离** | 方案A：共用表+tenant_id字段隔离（轻量）；方案B：独立数据源（强隔离） |
| **租户管理** | 租户注册/审核、套餐管理、配额限制 |
| **功能开关** | 按租户套餐开关模块菜单 |
| **数据权限** | 在行级数据权限基础上增加 tenant 维度 |

```sql
-- 方案A：所有业务表增加租户字段
ALTER TABLE sys_user ADD COLUMN tenant_id BIGINT DEFAULT 0;
ALTER TABLE sys_role ADD COLUMN tenant_id BIGINT DEFAULT 0;
-- ... 其他表同理

-- 租户表
CREATE TABLE sys_tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    code VARCHAR(50) UNIQUE,
    status TINYINT DEFAULT 1,
    expire_time DATETIME,
    max_users INT DEFAULT 100
);
```

---

### 19.8 实施优先级矩阵（更新）

> 已排除 v2.1 中已完成的增强项。

```
                    紧急度
                 低        高
           ┌─────────┬─────────┐
    高     │ 全站搜索 │ 会话管理│
实         │ 报表PDF │ 二次确认│
施  ──────│─────────│─────────│
成    低   │ 多租户   │ 异常告警│
本         │ Cron可视│ 接口耗时│
           └─────────┴─────────┘
           │ 图表增强 │ 重试机制│
           └─────────┴─────────┘
```

**建议路线**（基于 v2.1 已实现后的下一步）：

1. **安全兜底**：高危操作二次确认 + 会话管理增强 → 约 2 天
2. **监控完善**：接口耗时 + 异常告警聚合 → 约 2-3 天
3. **体验增强**：任务重试 + Cron可视化 + 仪表盘图表 → 约 3-4 天
4. **远期规划**：全站搜索 + 报表PDF导出 + 多租户 → 按需实施

---

