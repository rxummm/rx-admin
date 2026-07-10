import { useI18n } from 'vue-i18n'

/**
 * 菜单名称 i18n 翻译映射表
 * key: 后端返回的中文 menuName
 * value: i18n 翻译 key 路径
 */
const menuNameI18nMap = {
  // ===== 布局 =====
  仪表盘: 'layout.dashboard',

  // ===== 一级目录 =====
  系统管理: 'system._group',
  系统工具: 'tool._group',
  内容管理: 'content._group',
  AS400管理: 'as400._group',
  监控管理: 'monitor._group',
  系统监控: 'monitor._group',

  // ===== 系统管理（子菜单）=====
  用户管理: 'system.user.title',
  角色管理: 'system.role.title',
  菜单管理: 'system.menu.title',
  部门管理: 'system.dept.title',
  系统配置: 'system.config.title',
  定时任务: 'job.title',
  文件管理: 'file.title',
  IP黑白名单: 'system.ipRule.title',

  // ===== 系统工具 =====
  字典管理: 'tool.dict.title',
  行政区划: 'tool.region.title',
  接口分析: 'tool.analysis.title',
  项目文档: 'tool.docs.title',
  开发规范: 'tool.standards.title',
  流程图: 'tool.flowChart.title',
  X6流程图: 'tool.flowChart.x6',
  LogicFlow流程图: 'tool.flowChart.logicFlow',
  常用工具: 'tool._group',
  办公工具: 'tool.officeTools._group',
  Excel解析: 'tool.excelParser.title',
  文档格式转换: 'tool.docConverter.title',
  文档上传: 'tool.docUpload.title',
  音乐播放: 'tool.musicPlayer.title',
  视频播放: 'tool.videoPlayer.title',
  邮件发送: 'tool.emailSender.title',
  代码生成: 'tool.gen.title',
  批量导入: 'tool.importData.title',
  API调试: 'tool.apiDebug.title',
  数据备份: 'tool.backup.title',
  数据库工具: 'tool.dbConsole._group',
  开发工具: 'tool.devTools.title',

  // ===== 内容管理 =====
  通知公告: 'content.notice.title',
  消息中心: 'content.message.title',
  通知中心: 'content.messageCenter',

  // ===== AS400管理 =====
  对象列表: 'as400.objects.title',
  'i-Service': 'as400.iservice.title',
  服务列表: 'as400.iservice.list',
  技术博客: 'as400.techBlog.title',
  文章列表: 'as400.techBlog.list',

  // ===== 监控管理 =====
  操作日志: 'monitor.log.title',
  在线用户: 'monitor.online.title',
  慢查询监控: 'monitor.slowQuery.title',
  健康监控: 'monitor.health.title',
  日志分析: 'monitor.logAnalysis.title',
  登录日志: 'monitor.loginLog.title',
  导出审计: 'monitor.exportAudit.title',
  任务执行日志: 'monitor.jobLog.title',
  缓存管理: 'monitor.cacheManage.title',

  // ===== 经典文学 =====
  历代文学: 'classics.literature.title',
  历代文学数据: 'classics.literature.title',
  文学作品: 'classics.literature.work.title',
  四大名著: 'classics._fourMasterpieces',
  西游记: 'classics.xiyou.title',
  西游诗词: 'classics.xiyouPoems',
  西游人物: 'classics.xiyouCharacters',
  八十一难: 'classics.xiyouEvents',
  三国演义: 'classics.sanguo.title',
  三国诗词: 'classics.sanguoPoems',
  三国人物: 'classics.sanguoCharacters',
  水浒传: 'classics.shuihu.title',
  水浒诗词: 'classics.shuihuPoems',
  水浒章节: 'classics.shuihuChapters',
  红楼梦: 'classics.honglou.title',
  红楼诗词: 'classics.honglouPoems',
  红楼人物: 'classics.honglouCharacters',
  人物关系: 'classics.honglouRelations',

  // ===== 个人中心（路由 meta.title）=====
  权限申请: 'permission.request.title',

  // ===== 个人中心 =====
  个人中心: 'profile.title'
}

/**
 * 翻译菜单名称
 * @param {string} menuName - 后端返回的中文菜单名称
 * @param {Function} t - i18n 的 t 函数
 * @returns {string} 翻译后的菜单名称（找不到映射则返回原文）
 */
export function translateMenuName(menuName, t) {
  if (!menuName) return ''
  const i18nKey = menuNameI18nMap[menuName]
  if (i18nKey) {
    const translated = t(i18nKey)
    // 如果翻译结果等于 key 本身（说明没找到翻译），返回原文名称
    if (translated && translated !== i18nKey) {
      return translated
    }
  }
  return menuName
}

/**
 * composable: 获取菜单翻译函数
 * @returns {{ tMenu: Function }}
 *
 * 用法：
 *   const { tMenu } = useMenuI18n()
 *   tMenu('用户管理')  // -> 当前语言下的翻译，或原文
 */
export function useMenuI18n() {
  const { t, locale } = useI18n()

  const tMenu = (menuName) => translateMenuName(menuName, t)

  return { tMenu, locale }
}
