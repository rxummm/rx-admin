# GitHub 推送操作指南

> 本文档记录将本地 RX Admin 项目代码推送到 GitHub 仓库（`rxummm/rx-admin`）的标准流程、推送前检查清单及常见报错处理。

---

## 1. 仓库信息

| 项目 | 值 |
|------|-----|
| 远程地址 | `https://github.com/rxummm/rx-admin.git` |
| 远程名称 | `rx-admin` |
| 默认分支 | `main`（按实际情况调整） |

确认远程已配置：

```powershell
git remote -v
```

预期输出：

```
rx-admin   https://github.com/rxummm/rx-admin.git (fetch)
rx-admin   https://github.com/rxummm/rx-admin.git (push)
```

---

## 2. 推送步骤

### 2.1 查看当前分支

```powershell
git branch --show-current
```

### 2.2 暂存所有变更

```powershell
git add -A
```

### 2.3 提交代码

```powershell
git commit -m "feat: 提交 RX Admin 后台管理系统"
```

### 2.4 推送到 GitHub

首次推送（设置上游分支）：

```powershell
git push -u rx-admin <当前分支名>
```

之后推送：

```powershell
git push
```

---

## 3. 推送前检查清单

### 3.1 .gitignore 验证

确认项目根目录存在 `.gitignore` 且包含以下条目，避免把构建产物、依赖、敏感文件推上去：

```
target/
.idea/
.vscode/
*.iml
ui/node_modules/
ui/dist/
.mvn/
HELP.md
logs/
*.log
```

验证命令：

```powershell
Test-Path .gitignore
```

### 3.2 敏感信息检查

确认以下文件**未被 Git 追踪**（应仅保留在本地）：

| 文件 | 说明 |
|------|------|
| `application-local.yml` | 本地数据库/邮件配置 |
| `ui/.env.local` | 本地环境变量 |
| `*.pem`、`*.key` | 密钥文件 |
| `secrets.json` | 任何含密码/Token 的文件 |

检查命令：

```powershell
git ls-files | Select-String -Pattern "(local|secret|\.env\.local|\.key|\.pem)"
```

### 3.3 远程认证方式

HTTPS 推送 GitHub **不再支持密码登录**，必须使用 Personal Access Token（PAT）。

**Token 生成路径：**

`GitHub 右上角头像 → Settings → Developer settings（左侧最下）→ Personal access tokens → Tokens (classic) → Generate new token → 勾选 repo 权限 → 生成`

生成的 Token 形如 `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`，**仅显示一次，请立即复制保存**。

使用方式（推送时弹出凭证框）：

- Username：GitHub 用户名
- Password：**粘贴 PAT**（不是 GitHub 密码）

> Windows 可通过 `Git Credential Manager` 记住 Token，避免每次输入。

---

## 4. 常见报错处理

| 报错信息 | 原因 | 解决方案 |
|----------|------|----------|
| `remote: Repository not found` | remote URL 错误或 PAT 无 `repo` 权限 | 检查 `git remote -v`；重新生成带 `repo` 权限的 PAT |
| `Authentication failed` | 密码错误或使用了 GitHub 登录密码 | 改用 PAT 作为密码；或切换为 SSH 协议 |
| `failed to push some refs` | 远端有本地未拉取的更新 | `git pull --rebase rx-admin <branch>` 后再 push |
| `src refspec main does not match any` | 当前分支无 commit | 先执行 `git add` + `git commit` |
| `Permission denied (publickey)` | SSH 协议密钥未配置 | `ssh-keygen -t eda25519` 后将公钥添加到 GitHub |
| `The current branch <X> has no upstream branch` | 首次推送未指定上游 | 使用 `git push -u rx-admin <X>` |

### 4.1 切换为 SSH 协议（推荐长期使用）

```powershell
# 生成 SSH 密钥（一路回车，可设密码）
ssh-keygen -t ed25519 -C "your_email@example.com"

# 复制公钥内容
Get-Content ~/.ssh/id_ed25519.pub | Set-Clipboard

# GitHub → Settings → SSH and GPG keys → New SSH key → 粘贴
# 切换远程地址
git remote set-url rx-admin git@github.com:rxummm/rx-admin.git
```

### 4.2 连接超时处理

**问题：** `Failed to connect to github.com port 443 after 21033 ms`

**解决方案：**

```powershell
# 增加超时时间到 600 秒
git config --global http.timeout 600

# 设置最低速度限制为 0（不限制）
git config --global http.lowSpeedLimit 0

# 设置上传缓冲区大小
git config --global http.postBuffer 524288000

# 重试推送
git push
```

**验证网络连接：**

```powershell
# 测试 GitHub 连接
Test-NetConnection github.com -Port 443

# 测试 DNS 解析
Resolve-DnsName github.com

# 测试 Git 连接
git ls-remote https://github.com/rxummm/rx-admin.git
```

### 4.3 强制推送（覆盖远程分支）

**场景：** 需要完全覆盖远程分支，像第一次提交一样。

```powershell
# 确认本地状态
git status
git log --oneline -5

# 强制推送（⚠️ 会覆盖远程历史，不可恢复）
git push --force rx-admin main
```

### 4.4 修改 Commit 时间戳

**问题：** 推送成功后，GitHub 显示的时间还是上一次的，不是最新的。

**原因：** GitHub 显示的是 commit 的创建时间，而不是推送时间。

**解决方案：**

```powershell
# 修改最后一次 commit 的时间为当前时间
git commit --amend --no-edit --date="$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"

# 强制推送到远程
git push --force rx-admin main
```

**修改多个 commit 的时间：**

```powershell
# 修改最近 2 个 commit 的时间
git rebase -i HEAD~2
```

在编辑器中，将要修改的 commit 前面的 `pick` 改为 `edit` 或 `e`，保存后执行：

```powershell
# 修改当前 commit 的时间
git commit --amend --no-edit --date="$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"

# 继续修改下一个
git rebase --continue

# 强制推送
git push --force rx-admin main
```

---

## 5. 完整工作流示例

```powershell
# 1. 拉取最新代码（避免冲突）
git pull rx-admin main --rebase

# 2. 查看本次变更概况
git status
git diff --stat

# 3. 暂存并提交
git add -A
git commit -m "feat: 新增消息通知模块"

# 4. 推送
git push -u rx-admin main
```

---

## 6. 提交信息规范

遵循项目根目录 `.trae/rules/git-commit-message.md` 规范，示例：

```
feat: 新增用户收藏功能
fix: 修复仪表盘 SSE 连接断开问题
refactor: 重构 SysMenuService 树形查询
docs: 更新 README 部署说明
style: 统一代码格式
test: 增加 AuthService 单元测试
chore: 升级 element-plus 到 2.7.0
```

格式：`<type>(<scope>): <subject>`，subject 不超过 50 字符，使用中文描述。

---

## 7. 注意事项

- **首次推送前**先在 GitHub 仓库页面确认默认分支名称（`main` 或 `master`），与本地保持一致。
- **强制推送**（`git push --force`）会覆盖远端历史，**仅在自己独有的分支上使用**，禁止在 `main`/`master` 使用。
- 推送前确认 `target/` 和 `ui/node_modules/` 等大目录已在 `.gitignore` 中，避免仓库体积膨胀。
- 若使用 IDE（VS Code / IntelliJ）内置 Git 推送，建议仍熟悉命令行操作，便于排查问题。

---

## 8. 最近优化记录

### 8.1 2026-06-16 代码质量优化

**优化内容：**

1. **权限注解硬编码（高优先级）**
   - 将所有 Controller 中的 `@SaCheckPermission` 注解硬编码字符串替换为 `PermissionConstants` 常量
   - 涉及 15+ 个 Controller 文件

2. **Service 层接口缺失（中优先级）**
   - 创建 9 个 Service 接口，并修改实现类实现接口
   - 修改所有相关 Controller 和 Service，使其依赖接口而非实现类

3. **Controller 中硬编码错误码（低优先级）**
   - 在 `ErrorCode` 枚举中新增 `BLOG_NOT_FOUND(70002, "文章不存在")`
   - 将 10+ 个 Controller 中的硬编码错误码替换为 `ErrorCode` 枚举

4. **配置项硬编码（低优先级）**
   - 优化 `AppConfig`，新增 `CorsConfig`、`CaptchaConfig`、`TechBlogConfig`、`IpFilterConfig` 配置类

5. **消息常量提取**
   - 创建 `MessageConstants` 常量类，统一管理所有消息字符串
   - 替换 `SysUserService` 中的硬编码消息

**新增文件：**
- `ISysUserService.java`、`ISysRoleService.java`、`ISysDeptService.java`
- `ISysDictTypeService.java`、`ISysDictDataService.java`、`ISysConfigService.java`
- `ISysNoticeService.java`、`ISysMessageService.java`、`ITechBlogArticleService.java`
- `MessageConstants.java`

**修改文件：**
- 所有相关 Service 实现类
- 所有相关 Controller
- `AppConfig.java`
- `ErrorCode.java`

**提交信息：**
```
refactor: 优化代码质量 - 消除硬编码并引入Service接口层

- 权限注解使用PermissionConstants常量
- 创建9个Service接口并实现依赖倒置
- Controller错误码使用ErrorCode枚举
- 优化AppConfig配置结构
- 提取MessageConstants统一管理消息
```

---

## 9. 相关文档

- [RX Admin 优化指南](./rxadmin-optimization-guide.md)
- [RX Admin 开发技能](./rxadmin-dev-skills.md)
- [Git 提交信息规范](../../.trae/rules/git-commit-message.md)
