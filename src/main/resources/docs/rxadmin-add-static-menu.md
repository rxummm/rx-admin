# rx-admin 前端新增纯静态菜单工作清单

> 适用场景：在「水浒传」下新增「水浒人物」菜单，**不涉及后端数据表（前端用 mock 数据或本地 JSON）**。
> 文档基于项目当前结构（2026-06-15）编写，所有改动点都已实际验证存在。

---

## 一、整体流程概览

```
┌──────────── 前端（5 处必改）────────────┐
│ 1. views/classics/shuihu/characters/    │  ← 新建页面 index.vue
│ 2. router/componentMap.js               │  ← 注册 component → 组件映射
│ 3. composables/useMenuI18n.js           │  ← 注册菜单名 → i18n key
│ 4. i18n/lang/zh-CN.js                   │  ← 加中文翻译
│ 5. i18n/lang/en-US.js                   │  ← 加英文翻译
└─────────────────────────────────────────┘
                  ↓
┌──────────── 后端（1 处必改）────────────┐
│ sys_menu 表 INSERT 三级菜单 + 按钮权限   │  ← SQL 插入
│ sys_role_menu 表 INSERT 分配给超管      │  ← 授权超级管理员
└─────────────────────────────────────────┘
```

> **关键认知**：本项目菜单是**后端驱动**的（`sys_menu` 表 + `/auth/routers` 接口返回的菜单树驱动前端动态路由）。  
> 即使你不写后端代码、不建表，**也必须在 `sys_menu` 表插入新菜单记录**，否则前端菜单树里根本看不到这个入口。

---

## 二、前端必做 5 步

### 步骤 1：创建 Vue 页面

**路径**：`ui/src/views/classics/shuihu/characters/index.vue`

参照已有的 `views/classics/honglou/characters/index.vue` 或 `views/classics/sanguo/characters/index.vue` 复制改写，**改名 `defineOptions({ name: 'ClassicsShuihuCharacters' })`**（与 componentMap 里的 name 必须一致，否则 keep-alive 缓存不命中）。

最小骨架（用本地 mock 数据，不调 API）：

```vue
<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索姓名 / 绰号 / 排名" clearable style="width: 240px" />
      <el-select v-model="filterRank" placeholder="排位筛选" clearable style="width: 130px">
        <el-option label="天罡星" :value="1" />
        <el-option label="地煞星" :value="2" />
      </el-select>
      <el-button type="primary">搜索</el-button>
    </div>

    <div class="classics-table-wrapper">
      <el-table :data="filteredList" border stripe :max-height="tableMaxHeight">
        <el-table-column prop="rank" label="排位" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="nickname" label="绰号" min-width="180" />
        <el-table-column prop="origin" label="籍贯" width="120" />
        <el-table-column prop="weapon" label="兵器" min-width="120" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsShuihuCharacters' })
import { ref, computed } from 'vue'
import { useTableHeight } from '@/composables/useTableHeight'

const keyword = ref('')
const filterRank = ref('')
const { tableMaxHeight } = useTableHeight('.classics-table-wrapper')

// 本地 mock 数据（108 将可全量写死，或单独 JSON 文件 import）
const list = ref([
  { rank: 1, name: '宋江', nickname: '呼保义', origin: '山东郓城', weapon: '无（善用刀）' },
  { rank: 2, name: '卢俊义', nickname: '玉麒麟', origin: '河北大名', weapon: '丈二钢枪' },
  // ... 其余 106 将
])

const filteredList = computed(() => {
  return list.value.filter(item => {
    if (keyword.value && !`${item.name}${item.nickname}`.includes(keyword.value)) return false
    if (filterRank.value === 1 && item.rank > 36) return false
    if (filterRank.value === 2 && item.rank <= 36) return false
    return true
  })
})
</script>

<style lang="scss" scoped>
.classics-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--table-container-bg);
  border-radius: var(--radius-sm);
  padding: 16px;
  overflow: hidden;
}
</style>
```

**要点**：
- 必须 `defineOptions({ name: 'ClassicsShuihuCharacters' })`，name 要与 componentMap 对应
- `useTableHeight` 自动算表格高度，**复用现成 composable**
- 本地数据量小（< 100 条）可不加分页，列表里有 > 100 条再加
- `<style scoped lang="scss">`，**必须写 lang="scss"**（否则 108 将列表里任何 `//` 注释会让整个页面编译失败，详见 2026-06-13 SCSS 教训）

---

### 步骤 2：注册 componentMap 路由映射

**文件**：`ui/src/router/componentMap.js`

找到「四大名著 - 水浒传」分组（约 56-58 行）：

```js
  // 四大名著 - 水浒传
  'classics/shuihu/poems/index':       { component: () => import('@/views/classics/shuihu/poems/index.vue'),       name: 'ClassicsShuihuPoems' },
  'classics/shuihu/chapters/index':     { component: () => import('@/views/classics/shuihu/chapters/index.vue'),     name: 'ClassicsShuihuChapters' },
```

**插入一行**（注意 path 顺序，alphabetical 排序以保持整洁）：

```js
  'classics/shuihu/characters/index':  { component: () => import('@/views/classics/shuihu/characters/index.vue'), name: 'ClassicsShuihuCharacters' },
```

**为什么必须改这里**：
- `router/index.js:80` 的 `generateDynamicRoutes()` 会拿后端返回的 `menu.component`（如 `'classics/shuihu/characters/index'`）到 `componentMap` 里查找，找不到就跳过这个菜单
- 即使后端菜单插入成功、`sys_role_menu` 分配了权限，前端不注册 componentMap → 菜单点开是空白
- **不注册就会出现"权限有了菜单不显示 / 显示了点击白屏"**

---

### 步骤 3：注册菜单名 → i18n key 映射

**文件**：`ui/src/composables/useMenuI18n.js`

找到水浒传分组（约 89-91 行）：

```js
  '水浒传': 'classics.shuihu.title',
  '水浒诗词': 'classics.shuihuPoems',
  '水浒章节': 'classics.shuihuChapters',
```

**插入一行**（按现有 alphabetical 顺序，`水浒诗词` → `水浒人物` 字母 c 在 h 前，应放在诗词之前；但项目里更看重与 componentMap 的对应顺序，建议紧跟水浒章节后）：

```js
  '水浒人物': 'classics.shuihuCharacters',
```

**为什么必须改**：
- 后端返回的菜单名是中文（"水浒人物"），前端需要通过这个映射表查 i18n key
- 没改的话菜单能显示但**永远是中文**，切到英文 locale 不会变
- 查不到时 fallback 显示原文（不报错），所以不强制；但**有 i18n 需求必须加**

---

### 步骤 4-5：i18n 双语翻译

**文件 A**：`ui/src/i18n/lang/zh-CN.js`  
**文件 B**：`ui/src/i18n/lang/en-US.js`

两文件都在第 746-748 行附近有水浒分组：

```js
// zh-CN.js
    shuihuPoems: '水浒诗词',
    shuihuChapters: '水浒章节',
    shuihu: { title: '水浒传' },
```

**插入**（zh-CN.js）：

```js
    shuihuPoems: '水浒诗词',
    shuihuChapters: '水浒章节',
    shuihuCharacters: '水浒人物',   // ← 新增
    shuihu: { title: '水浒传' },
```

**插入**（en-US.js）：

```js
    shuihuPoems: 'Poems',
    shuihuChapters: 'Chapters',
    shuihuCharacters: 'Characters',  // ← 新增
    shuihu: { title: 'Water Margin' },
```

**要点**：
- `shuihuCharacters` 是平铺 key（不带 `.title`）因为它对应一个菜单项而不是分组
- 英文不强制可读但**必须 1:1 对齐**（key 名一致，值可自由翻译）
- i18n 不匹配会让所有 locale 下回退显示中文（虽然不报错，但英文版会一整页中文菜单）

---

## 三、后端必做 2 步

> 即使"不建数据表"，**也必须在 sys_menu 插入菜单记录**，否则后端 `/auth/routers` 接口根本不会返回这个菜单，前端菜单树无入口。

### 步骤 6：插入菜单记录

**文件**：`src/main/resources/db/classics_menu.sql`（项目已用 `classics_menu.sql` 单独管理四大名著菜单，可继续追加）

参照水浒章节的 INSERT 模板（147-149 行）：

```sql
-- 三级菜单：水浒人物（parent_id=247 水浒传）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (331, 247, '水浒人物', 2, '/classics/shuihu/characters', 'classics/shuihu/characters/index', 'classics:shuihu:character:list', 'User', 3, 1, 1, 0, NOW(), NOW());
```

**字段说明**（sys_menu 表）：

| 字段 | 含义 | 必填 | 说明 |
|------|------|------|------|
| `id` | 菜单主键 | 是 | 必须全局唯一，已有范围 1-330，新菜单用 331 |
| `parent_id` | 上级菜单 id | 是 | 水浒传父菜单是 247（`parent_id=247`）|
| `menu_name` | 显示名 | 是 | 必须与 `useMenuI18n.js` 的 key 完全一致（"水浒人物"）|
| `menu_type` | 1=目录 2=菜单 3=按钮 | 是 | 这里是 2（菜单型，叶子节点）|
| `path` | 路由路径 | 是 | 必须以 `/` 开头，全站唯一（`/classics/shuihu/characters`）|
| `component` | 组件路径 | 是 | **必须与 componentMap.js 的 key 一字不差**：`classics/shuihu/characters/index` |
| `perms` | 权限标识 | 是 | 习惯格式 `业务:模块:操作:范围`，本菜单用 `classics:shuihu:character:list` |
| `icon` | 图标 | 否 | Element Plus 图标名（User / Star / Trophy 等）|
| `sort` | 排序 | 是 | 同级菜单的展示顺序，数字小靠前（水浒章节是 2，本菜单用 3）|
| `visible` | 1 显示 0 隐藏 | 是 | 1 |
| `status` | 1 启用 0 停用 | 是 | 1 |
| `deleted` | 逻辑删除 | 是 | 0 |
| `create_time` / `update_time` | 时间戳 | 是 | `NOW()` |

**关键风险**：
- `component` 字段拼错或与 componentMap.js 不匹配 → 菜单显示但点击白屏
- `id` 与已存在 id 冲突 → 插入失败（必须先查 `SELECT MAX(id) FROM sys_menu` 确认）
- `parent_id` 写错（写成 248 水浒诗词 id）→ 菜单跑到水浒诗词下而不是水浒传

### 步骤 7：分配权限给超级管理员

**同样在 `classics_menu.sql` 末尾追加**：

```sql
-- 按钮级权限：水浒人物（parent_id=331 水浒人物）
-- 注意：纯静态展示页面，按钮权限可不加（页面里也没新增/编辑/删除按钮）
-- 但建议至少加一个 query 权限以保持与其他页面一致
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status, deleted, create_time, update_time)
VALUES (332, 331, '人物查询', 3, '', '', 'classics:shuihu:character:query', '', 1, 1, 1, 0, NOW(), NOW());
```

**然后修改文件末尾的 role_menu 分配语句**（原 175 行）：

```sql
-- 原：
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE id BETWEEN 229 AND 330;
-- 改为：
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE id BETWEEN 229 AND 332;
```

**或者**追加一条独立的 INSERT：

```sql
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 331), (1, 332);
```

**为什么必须改**：
- `sys_role_menu` 是角色 ↔ 菜单的关联表
- 不分配 → 即使用户角色是超管（role_id=1）也看不到菜单（菜单树由 `/auth/routers` 按 `role_id` 过滤返回）
- 别的角色（如普通用户）需要这个菜单，重复 `INSERT (role_id, menu_id) VALUES (其他角色id, 331)`

**真实执行**：把 SQL 在 `rxusysadmin` 库（项目第二个数据源，存四大名著相关数据）执行，**不要在主库 rxadmin 执行**。具体哪个库？查 `application.yml` 的 `second-datasource` 配置。

---

## 四、执行清单（Checklist）

按顺序执行，每步完成打 ✅：

- [ ] **前端**
  - [ ] 1. 新建 `ui/src/views/classics/shuihu/characters/index.vue`（含 `defineOptions({ name: 'ClassicsShuihuCharacters' })`）
  - [ ] 2. `ui/src/router/componentMap.js` 加 `classics/shuihu/characters/index` 映射
  - [ ] 3. `ui/src/composables/useMenuI18n.js` 加 `'水浒人物': 'classics.shuihuCharacters'`
  - [ ] 4. `ui/src/i18n/lang/zh-CN.js` 加 `shuihuCharacters: '水浒人物'`
  - [ ] 5. `ui/src/i18n/lang/en-US.js` 加 `shuihuCharacters: 'Characters'`
  - [ ] 6. （可选）补充 mock 数据 JSON 文件 `ui/src/mock/shuihuCharacters.js` 再 import
- [ ] **后端**
  - [ ] 7. `src/main/resources/db/classics_menu.sql` 追加 2 条 INSERT（菜单 331 + 按钮 332）
  - [ ] 8. 同一文件末尾追加 sys_role_menu 分配（1, 331）和（1, 332）
  - [ ] 9. 在 rxusysadmin 库执行 SQL
- [ ] **验证**
  - [ ] 10. 重启 Spring Boot 后端
  - [ ] 11. 前端 dev server 启动，访问 `/classics/shuihu/characters` 不白屏
  - [ ] 12. **强制刷新或退出重新登录**（菜单树是登录时缓存的，旧 session 不会自动加载新菜单）
  - [ ] 13. 切英文 locale 验证 i18n

---

## 五、易踩的坑

### 坑 1：菜单不显示 / 白屏
**90% 是 componentMap 没注册**。检查 `router/index.js:80` 的 `componentMap[menu.component]` 是否能查到。

### 坑 2：菜单显示了但切语言不切换
`useMenuI18n.js` 没加映射。fallback 显示原文（不报错，但 i18n 失效）。

### 坑 3：用户退出重新登录才看到菜单
菜单树在登录时由 `/auth/routers` 返回并缓存在 `userStore.menus`，**新增菜单后必须重新登录**才能加载。建议在文档里也提醒用户。

### 坑 4：sys_role_menu 没分配 → 菜单永远看不到
即使是超管（role_id=1）也要在 sys_role_menu 里有记录才会返回菜单。

### 坑 5：component 字段值与 componentMap.js 的 key 必须一字不差
- 后端 SQL: `component = 'classics/shuihu/characters/index'`
- 前端 componentMap.js key: `'classics/shuihu/characters/index'`
- 前端 Vue 文件路径: `@/views/classics/shuihu/characters/index.vue`
- 前端 Vue 文件 `defineOptions name`: `'ClassicsShuihuCharacters'`
- 四个值**两两对应**（component = componentMap key；componentMap name = defineOptions name），拼错任意一处都会出问题

### 坑 6：Vue 页面 `<style>` 漏 `lang="scss"`
SCSS 块内有 `//` 注释或 `&` 嵌套时必须加 `lang="scss"`，否则 PostCSS 不识别 → 编译失败 → 整个页面 chunk 加载失败 → 标签页被吞（详见 2026-06-13 memory）。

### 坑 7：table 高度错位
一定要用 `useTableHeight` composable 而不是手写 `calc(100vh - 107px)`（2026-06-13 已统一为 `var(--layout-content-offset)`，但 composable 更稳）。

### 坑 8：mock 数据写在 setup 里导致 SSR / 多实例共享
本地小数据写在 `ref([])` 里没问题；如果数据多或要复用，独立成 `ui/src/mock/shuihuCharacters.js` export 数组再 import。

---

## 六、文件改动统计

| 类别 | 文件 | 改动量 |
|------|------|--------|
| 新建 | `ui/src/views/classics/shuihu/characters/index.vue` | ~150 行（无数据）~430 行（含 mock） |
| 修改 | `ui/src/router/componentMap.js` | +1 行 |
| 修改 | `ui/src/composables/useMenuI18n.js` | +1 行 |
| 修改 | `ui/src/i18n/lang/zh-CN.js` | +1 行 |
| 修改 | `ui/src/i18n/lang/en-US.js` | +1 行 |
| 修改 | `src/main/resources/db/classics_menu.sql` | +3 行 INSERT + 1 行 sys_role_menu |

**总改动**：5 文件 + 1 新文件，~150-430 行新代码 + 7 行配置。

---

## 七、相关文档索引

- `rxadmin-dev-skills.md` 第 736-738 行有 componentMap 注册规范
- `rxadmin.md` 第 350-351 行有水浒相关表结构（本次不涉及）
- `rxadmin-setup.md` 第 678 行说明四大名著相关表走第二数据源 `rxusysadmin`
- 2026-06-13 memory：路由 4 处兜底机制（router.onError / router.push catch / v-if fallback / globalErrorHandler 白名单）
- 2026-06-13 memory：H1 `*, *::before, *::after { max-width: 100vw }` 通配污染已修，新页面不要再用

---

**文档结束。** 复制本文档到任意 wiki/Notion 即可作为团队 SOP。
> **文档维护**: 2026-06-13 创建 | 2026-06-15 日期更新
