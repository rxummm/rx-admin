# 组件映射表 (componentMap.js)

**文件位置**: `ui/src/router/componentMap.js`

路由是完全动态的(`constantRoutes`只保留 Login + Layout 空壳)，业务路由由 `sys_menu` 表驱动，
通过 `componentMap` 将 `sys_menu.component` 字段映射到实际的 Vue 组件。

## 映射格式

```javascript
'views相对路径(不含.vue)': {
  component: () => import('@/views/实际文件路径.vue'),
  name: 'PascalCase组件名' // 必须与 defineOptions({ name: 'xxx' }) 一致
}
```

## 完整映射表

```javascript
export const componentMap = {
  'dashboard/index':             { component: () => import('@/views/dashboard/index.vue'),             name: 'Dashboard' },
  'profile/index':               { component: () => import('@/views/profile/index.vue'),               name: 'Profile' },
  'system/user/index':           { component: () => import('@/views/system/user/index.vue'),           name: 'SystemUser' },
  'system/role/index':           { component: () => import('@/views/system/role/index.vue'),           name: 'SystemRole' },
  'system/menu/index':           { component: () => import('@/views/system/menu/index.vue'),           name: 'SystemMenu' },
  'system/dept/index':           { component: () => import('@/views/system/dept/index.vue'),           name: 'SystemDept' },
  'system/config/index':         { component: () => import('@/views/system/config/index.vue'),         name: 'SystemConfig' },
  'system/file/index':           { component: () => import('@/views/system/file/index.vue'),           name: 'SystemFile' },
  'system/ipRule/index':         { component: () => import('@/views/system/ipRule/index.vue'),         name: 'SystemIpRule' },
  'content/message/index':       { component: () => import('@/views/content/message/index.vue'),       name: 'ContentMessage' },
  'monitor/health/index':        { component: () => import('@/views/monitor/health/index.vue'),        name: 'MonitorHealth' },
  'monitor/logAnalysis/index':   { component: () => import('@/views/monitor/logAnalysis/index.vue'),   name: 'MonitorLogAnalysis' },
  'tool/gen/index':              { component: () => import('@/views/tool/gen/index.vue'),              name: 'ToolGen' },
  'tool/importData/index':       { component: () => import('@/views/tool/importData/index.vue'),       name: 'ToolImportData' },
  'tool/apiDebug/index':         { component: () => import('@/views/tool/apiDebug/index.vue'),         name: 'ToolApiDebug' },
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
  'monitor/login-log/index':     { component: () => import('@/views/monitor/login-log/index.vue'),     name: 'MonitorLoginLog' },
  'monitor/export-log/index':    { component: () => import('@/views/monitor/export-log/index.vue'),    name: 'MonitorExportLog' },
  'monitor/job-log/index':       { component: () => import('@/views/monitor/job-log/index.vue'),       name: 'MonitorJobLog' },
  'monitor/cache-manage/index':  { component: () => import('@/views/monitor/cache-manage/index.vue'),  name: 'MonitorCacheManage' },
  'content/notify-center/index': { component: () => import('@/views/content/notify-center/index.vue'), name: 'ContentNotifyCenter' },
  'tool/dbConsole/index':        { component: () => import('@/views/tool/dbConsole/index.vue'),        name: 'ToolDbConsole' },
  'tool/devTools/index':         { component: () => import('@/views/tool/devTools/index.vue'),         name: 'ToolDevTools' },
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
}
```

## 强制约定

1. key 必须与 `sys_menu.component` 字段值完全一致
2. `name` 必须与页面组件 `defineOptions({ name: 'xxx' })` 一致
3. `name` 使用英文 PascalCase，确保 `keep-alive` 缓存生效
4. 路径格式: `{modules}/{page}/index`（如 `system/user/index`）
5. 父级菜单路径（无 component）不出现在搜索结果中
