import sys
sys.stdout.reconfigure(encoding="utf-8")
nl = "\r\n"

# ============================================================
# Update rxadmin.md - add new feature sections
# ============================================================
path = "D:/vueprojects/RX/src/main/resources/docs/rxadmin.md"
c = open(path, "r", encoding="utf-8").read()

# 1. Update version
c = c.replace("**版本**: 1.4.0", "**版本**: 1.5.0")

# 2. Add SSE section after 14.8 if not already there, or update it
if "慢查询监控" not in c:
    # Find the end of 14.8 SSE section and add 14.9 Slow Query
    sse_end = c.find("### 14.9", c.find("### 14.8 SSE"))
    if sse_end == -1:
        # No 14.9 yet, append after 14.8 content
        insert_point = c.find("### 15.", c.find("### 14.8 SSE"))
        if insert_point == -1:
            insert_point = len(c)
        
        new_sections = """
### 14.9 慢查询监控

**方案**: MyBatis StatementHandler 拦截器自动检测超过 2 秒的 SQL 查询，记录到 `sys_slow_query` 表。

**后端文件**:
- `SlowQueryInterceptor.java` — MyBatis 插件，拦截 StatementHandler.update/query
- `SysSlowQuery.java` / `SysSlowQueryMapper.java` / `SysSlowQueryService.java` / `SysSlowQueryController.java`

**前端页面**: `monitor/slow-query/index.vue` — 使用 `vue-virtual-scroller` RecycleScroller 实现虚拟滚动，支持按类型筛选、删除、清空。

### 14.10 定时任务管理

**方案**: `sys_job` 表 + CRUD 接口 + 前端管理页面，支持新增/编辑/删除/单次执行/状态切换。

**后端**: `SysJobService` / `SysJobController`（`/api/monitor/job`）
**前端**: `monitor/job/index.vue` — 标准 CRUD 页面，含"执行一次"按钮。

### 14.11 文件管理

**方案**: `sys_file` 表 + 本地磁盘文件上传/下载/删除，支持文件类型分类。

**后端**: `SysFileService` / `SysFileController`（`/api/system/file`）
**前端**: `system/file/index.vue` — 上传按钮 + 文件表格 + 下载/删除操作。

### 14.12 通知公告自动刷新

**方案**: `NoticePopover.vue` 每 15 秒自动调用 `fetchNotices()` 刷新未读计数和列表，确保权限申请等操作后铃铛数字及时更新。

### 14.13 操作日志查询增强

**方案**: 日志查询增加状态筛选和时间范围筛选。
- 后端 `SysLogService.pageQuery()` 增加 `status`、`startTime`、`endTime` 参数
- 前端增加状态下拉框和日期范围选择器

### 14.14 国际化补全

**方案**: 补充 job、file、slow-query 模块的 i18n 中英文翻译。
- `zh-CN.js` / `en-US.js` 增加 `job.*`、`file.*` 等翻译键

### 14.15 批量操作支持

**方案**: 用户、角色、日志页面已支持表格首列 selection + 批量删除按钮。

### 14.16 前端虚拟滚动

**方案**: 使用 `vue-virtual-scroller` RecycleScroller 组件实现虚拟滚动。
- 慢查询监控页面已率先接入 RecycleScroller，仅渲染可见区域 DOM
- 其他大列表页面可参考该模式接入
"""
        c = c[:insert_point] + new_sections + nl + c[insert_point:]
        print("Added new sections 14.9-14.16")
else:
    print("Sections already exist")

# 3. Update API route table in section 3.7 to include new controllers
old_routes = "| `SysOnlineController` | `/sys/online` | 在线用户列表 |"
if "SysJobController" not in c:
    c = c.replace(old_routes, old_routes + nl + 
        "| `SysJobController` | `/monitor/job` | 定时任务管理 |" + nl +
        "| `SysFileController` | `/system/file` | 文件管理 |" + nl +
        "| `SysSlowQueryController` | `/monitor/slow-query` | 慢查询监控 |")
    print("Added controller routes")

# 4. Update views count in section 4.6
old_views = "| **在线用户** | `monitor/online/index.vue` | 在线用户列表 |"
if "monitor/slow-query/index.vue" not in c:
    c = c.replace(old_views, old_views + nl + 
        "| **慢查询监控** | `monitor/slow-query/index.vue` | 慢查询列表（虚拟滚动） |" + nl +
        "| **定时任务** | `monitor/job/index.vue` | 定时任务管理 |" + nl +
        "| **文件管理** | `system/file/index.vue` | 文件上传/下载 |")
    print("Added view entries")

# 5. Update the frontend component count
old_count = "| 前端 Vue 组件 | 33 | 28 个 views + 5 个 layout 组件（含 SearchBox、NoticePopover） |"
c = c.replace(old_count, "| 前端 Vue 组件 | 36 | 31 个 views + 5 个 layout 组件（含 SearchBox、NoticePopover） |")

# 6. Update the API interface list in section 3.10
old_api_list = "| 在线用户 | `/sys/online/list` | GET 在线用户列表 |"
if "monitor/slow-query/page" not in c:
    c = c.replace(old_api_list, old_api_list + nl +
        "| 慢查询分页 | `/monitor/slow-query/page` | GET 慢查询列表 |" + nl +
        "| 删除慢查询 | `/monitor/slow-query/{id}` | DELETE 删除记录 |" + nl +
        "| 清空慢查询 | `/monitor/slow-query/clear` | DELETE 清空所有 |" + nl +
        "| 定时任务分页 | `/monitor/job/page` | GET 任务列表 |" + nl +
        "| 新增定时任务 | `/monitor/job` | POST 新增 |" + nl +
        "| 修改定时任务 | `/monitor/job` | PUT 修改 |" + nl +
        "| 删除定时任务 | `/monitor/job/{id}` | DELETE 删除 |" + nl +
        "| 切换任务状态 | `/monitor/job/status/{id}` | PUT 启用/暂停 |" + nl +
        "| 执行一次任务 | `/monitor/job/run/{id}` | PUT 单次执行 |" + nl +
        "| 文件分页 | `/system/file/page` | GET 文件列表 |" + nl +
        "| 上传文件 | `/system/file/upload` | POST 上传 |" + nl +
        "| 下载文件 | `/system/file/{id}` | GET 下载 |" + nl +
        "| 删除文件 | `/system/file/{id}` | DELETE 删除 |")
    print("Added API entries")

# 7. Update nav menu listing
old_nav = "系统监控（操作日志/在线用户）"
if "慢查询监控" not in c:
    c = c.replace(old_nav, "系统监控（操作日志/在线用户/慢查询监控）")
    print("Updated nav listing")

# 8. Update section 4.8 待办事项提醒 to mention auto-refresh
old_todo = "### 4.8 待办事项提醒"
if "15 秒" not in c:
    c = c.replace(old_todo + nl, old_todo + nl + nl + 
        "**自动刷新**: 通知弹窗每 15 秒自动刷新未读计数和列表，提交权限申请后铃铛数字无需手动刷新即可更新。" + nl)
    print("Added auto-refresh note")

open(path, "w", encoding="utf-8").write(c)
print("rxadmin.md updated!")
