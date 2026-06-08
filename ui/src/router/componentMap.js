/**
 * 组件映射表
 *
 * key = sys_menu.component 字段值（对应 views/ 下文件路径去掉 .vue 后缀）
 * value = { component: 懒加载函数, name: 组件 defineOptions name（用于 keep-alive 缓存匹配） }
 *
 * 新增页面时，在此文件中追加一行即可，无需修改 router/index.js。
 */
export const componentMap = {
  // 仪表盘 & 个人
  'dashboard/index':         { component: () => import('@/views/dashboard/index.vue'),           name: 'Dashboard' },
  'profile/index':           { component: () => import('@/views/profile/index.vue'),             name: 'Profile' },

  // 系统管理
  'system/user/index':       { component: () => import('@/views/system/user/index.vue'),         name: 'SystemUser' },
  'system/role/index':       { component: () => import('@/views/system/role/index.vue'),         name: 'SystemRole' },
  'system/menu/index':       { component: () => import('@/views/system/menu/index.vue'),         name: 'SystemMenu' },
  'system/config/index':         { component: () => import('@/views/system/config/index.vue'),           name: 'SystemConfig' },
  'system/dept/index':       { component: () => import('@/views/system/dept/index.vue'),         name: 'SystemDept' },

  // 系统工具
  'tool/dict/index':         { component: () => import('@/views/tool/dict/index.vue'),           name: 'ToolDict' },
  'tool/region/index':       { component: () => import('@/views/tool/region/index.vue'),         name: 'ToolRegion' },
  'tool/analysis/index':     { component: () => import('@/views/tool/analysis/index.vue'),       name: 'ToolAnalysis' },
  'tool/docs/index':         { component: () => import('@/views/tool/docs/index.vue'),           name: 'ToolDocs' },
  'tool/standards/index':    { component: () => import('@/views/tool/standards/index.vue'),      name: 'ToolStandards' },

  // 内容管理
  'content/notice/index':    { component: () => import('@/views/content/notice/index.vue'),      name: 'ContentNotice' },

  // AS400管理
  'as400/objects/index':     { component: () => import('@/views/as400/objects/index.vue'),       name: 'As400Objects' },
  'as400/iservice/index':    { component: () => import('@/views/as400/iservice/index.vue'),      name: 'As400IService' },
  'as400/techblog/index':    { component: () => import('@/views/as400/techblog/index.vue'),      name: 'TechBlogIndex' },
  'as400/techblog/detail':   { component: () => import('@/views/as400/techblog/detail.vue'),     name: 'TechBlogDetail' },

  // 系统监控
  'monitor/log/index':       { component: () => import('@/views/monitor/log/index.vue'),         name: 'MonitorLog' },
  'monitor/online/index':    { component: () => import('@/views/monitor/online/index.vue'),      name: 'MonitorOnline' },

  // 四大名著 - 红楼梦
  'classics/honglou/poems/index':      { component: () => import('@/views/classics/honglou/poems/index.vue'),      name: 'ClassicsHonglouPoems' },
  'classics/honglou/characters/index':  { component: () => import('@/views/classics/honglou/characters/index.vue'),  name: 'ClassicsHonglouCharacters' },
  'classics/honglou/relations/index':   { component: () => import('@/views/classics/honglou/relations/index.vue'),   name: 'ClassicsHonglouRelations' },

  // 四大名著 - 西游记
  'classics/xiyou/poems/index':        { component: () => import('@/views/classics/xiyou/poems/index.vue'),        name: 'ClassicsXiyouPoems' },
  'classics/xiyou/characters/index':    { component: () => import('@/views/classics/xiyou/characters/index.vue'),    name: 'ClassicsXiyouCharacters' },
  'classics/xiyou/events/index':        { component: () => import('@/views/classics/xiyou/events/index.vue'),        name: 'ClassicsXiyouEvents' },

  // 四大名著 - 三国演义
  'classics/sanguo/poems/index':       { component: () => import('@/views/classics/sanguo/poems/index.vue'),       name: 'ClassicsSanguoPoems' },
  'classics/sanguo/characters/index':   { component: () => import('@/views/classics/sanguo/characters/index.vue'),   name: 'ClassicsSanguoCharacters' },

  // 四大名著 - 水浒传
  'classics/shuihu/poems/index':       { component: () => import('@/views/classics/shuihu/poems/index.vue'),       name: 'ClassicsShuihuPoems' },
  'classics/shuihu/chapters/index':     { component: () => import('@/views/classics/shuihu/chapters/index.vue'),     name: 'ClassicsShuihuChapters' },

  // 历代文学
  'classics/literature/index':          { component: () => import('@/views/classics/literature/index.vue'),          name: 'ClassicsLiteratureIndex' },
  'classics/literature/works/index':    { component: () => import('@/views/classics/literature/works/index.vue'),      name: 'ClassicsLiteratureWorks' },

  // 权限申请
  'permission/request/index':           { component: () => import('@/views/permission/request/index.vue'),           name: 'PermissionRequest' },

  // 常用工具
  'tool/excelParser/index':    { component: () => import('@/views/tool/excelParser/index.vue'),    name: 'ToolExcelParser' },
  'tool/docConverter/index':   { component: () => import('@/views/tool/docConverter/index.vue'),   name: 'ToolDocConverter' },
  'tool/docUpload/index':      { component: () => import('@/views/tool/docUpload/index.vue'),      name: 'ToolDocUpload' },
  'tool/emailSender/index':    { component: () => import('@/views/tool/emailSender/index.vue'),    name: 'ToolEmailSender' },
  'tool/flowChart/index':      { component: () => import('@/views/tool/flowChart/index.vue'),      name: 'ToolFlowChart' },
  'tool/flowChart/logicFlow':  { component: () => import('@/views/tool/flowChart/logicFlow.vue'),  name: 'ToolLogicFlowChart' },
  'tool/flowChart/antvX6':     { component: () => import('@/views/tool/flowChart/antvX6.vue'),     name: 'ToolAntvX6Chart' },
  'tool/musicPlayer/index':    { component: () => import('@/views/tool/musicPlayer/index.vue'),    name: 'ToolMusicPlayer' },

  // 定时任务管理
  'monitor/job/index':       { component: () => import('@/views/monitor/job/index.vue'),         name: 'MonitorJob' },

  // 文件管理
  'monitor/slow-query/index':       { component: () => import('@/views/monitor/slow-query/index.vue'),         name: 'MonitorSlowQuery' },

  'system/file/index':       { component: () => import('@/views/system/file/index.vue'),         name: 'SystemFile' },

  // IP黑白名单
  'system/ipRule/index':     { component: () => import('@/views/system/ipRule/index.vue'),       name: 'SystemIpRule' },

  // 站内消息
  'content/message/index':   { component: () => import('@/views/content/message/index.vue'),     name: 'ContentMessage' },

  // 系统健康监控
  'monitor/health/index':    { component: () => import('@/views/monitor/health/index.vue'),      name: 'MonitorHealth' },

  // 日志分析
  'monitor/logAnalysis/index': { component: () => import('@/views/monitor/logAnalysis/index.vue'), name: 'MonitorLogAnalysis' },

  // 代码生成器
  'tool/gen/index':          { component: () => import('@/views/tool/gen/index.vue'),            name: 'ToolGen' },

  // 批量导入
  'tool/importData/index':   { component: () => import('@/views/tool/importData/index.vue'),     name: 'ToolImportData' },

  // API调试
  'tool/apiDebug/index':     { component: () => import('@/views/tool/apiDebug/index.vue'),       name: 'ToolApiDebug' },

  // 备份恢复
  'tool/backup/index':       { component: () => import('@/views/tool/backup/index.vue'),         name: 'ToolBackup' },

  // 登录日志
  'monitor/login-log/index':   { component: () => import('@/views/monitor/login-log/index.vue'),   name: 'MonitorLoginLog' },

  // 导出审计
  'monitor/export-log/index':  { component: () => import('@/views/monitor/export-log/index.vue'),  name: 'MonitorExportLog' },

  // 通知中心
  'content/notify-center/index': { component: () => import('@/views/content/notify-center/index.vue'), name: 'ContentNotifyCenter' },

  // 数据库控制台
  'tool/dbConsole/index':     { component: () => import('@/views/tool/dbConsole/index.vue'),      name: 'ToolDbConsole' },

  // 缓存管理
  'monitor/cache-manage/index': { component: () => import('@/views/monitor/cache-manage/index.vue'), name: 'MonitorCacheManage' },

  // 任务日志
  'monitor/job-log/index':    { component: () => import('@/views/monitor/job-log/index.vue'),     name: 'MonitorJobLog' },

  // 开发工具
  'tool/devTools/index':      { component: () => import('@/views/tool/devTools/index.vue'),       name: 'ToolDevTools' },
}
