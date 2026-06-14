const fs = require('fs')
const path = require('path')

// 修复配置：key = 文件相对路径，value = { wrapperName, paginationName }
// 如果页面没有专门的 wrapper，则 wrapperName 为空，只需加分页器样式
const pages = [
  // system 模块（没有专门 wrapper）
  { file: 'src/views/system/file/index.vue', wrapperName: '', paginationName: 'page-pagination' },
  { file: 'src/views/system/ipRule/index.vue', wrapperName: '', paginationName: 'page-pagination' },
  { file: 'src/views/system/user/index.vue', wrapperName: '', paginationName: 'page-pagination' },

  // content 模块
  { file: 'src/views/content/message/index.vue', wrapperName: '', paginationName: '' }, // 特殊处理
  { file: 'src/views/content/notice/index.vue', wrapperName: 'notice-table-wrapper', paginationName: 'notice-pagination' },
  { file: 'src/views/content/notify-center/index.vue', wrapperName: '', paginationName: '' }, // 特殊处理

  // tool 模块
  { file: 'src/views/tool/dict/index.vue', wrapperName: '', paginationName: '' },
  { file: 'src/views/tool/region/index.vue', wrapperName: 'region-table-wrapper', paginationName: 'region-pagination' },

  // monitor 模块
  { file: 'src/views/monitor/export-log/index.vue', wrapperName: 'pagination-wrap', paginationName: '' },
  { file: 'src/views/monitor/job/index.vue', wrapperName: '', paginationName: '' },
  { file: 'src/views/monitor/job-log/index.vue', wrapperName: 'pagination-wrap', paginationName: '' },
  { file: 'src/views/monitor/log/index.vue', wrapperName: 'log-table-wrapper', paginationName: 'page-pagination' },
  { file: 'src/views/monitor/login-log/index.vue', wrapperName: 'pagination-wrap', paginationName: '' },
  { file: 'src/views/monitor/slow-query/index.vue', wrapperName: 'virtual-table-wrapper', paginationName: '' },
]

let fixedCount = 0

for (const page of pages) {
  const filePath = path.join(__dirname, page.file)
  if (!fs.existsSync(filePath)) {
    console.log('[跳过] ' + page.file + ' (文件不存在)')
    continue
  }

  let content = fs.readFileSync(filePath, 'utf-8')
  let changed = false

  // 步骤1: 检查是否已有 <style scoped>
  const hasStyleScope = /<style[^>]*scoped[^>]*>/.test(content)
  const hasStyle = /<style[^>]*>/.test(content)

  // 查找分页器实际的 class
  const paginationClassMatch = content.match(/<el-pagination[^>]*class="([^"]*)"/)
  const actualPaginationClass = paginationClassMatch ? paginationClassMatch[1] : page.paginationName

  // 查找 wrapper 实际的 class
  const wrapperClassMatch = content.match(/class="([a-z0-9-]+-(?:table-wrapper|wrapper))"/i)
  const actualWrapperClass = wrapperClassMatch ? wrapperClassMatch[1] : page.wrapperName

  // 准备要添加的样式
  let additionalStyles = []

  // 给分页器加 margin-top
  if (actualPaginationClass) {
    const paginationSelector = '.' + actualPaginationClass
    const paginationRuleExists = new RegExp(paginationSelector.replace(/\./g, '\\.') + '\\s*\\{', 'm').test(content)
    if (!paginationRuleExists) {
      additionalStyles.push(paginationSelector + ' {\n  margin-top: 12px;\n}')
      changed = true
    } else {
      // 已有样式，检查是否有 margin-top:auto 需要替换
      const autoPattern = new RegExp('(' + paginationSelector.replace(/\./g, '\\.') + '[^{]*\\{[^}]*?)margin-top\\s*:\\s*auto', 'm')
      if (autoPattern.test(content)) {
        content = content.replace(autoPattern, '$1margin-top: 12px')
        changed = true
      } else {
        // 检查是否已有 margin-top: <数字>
        const marginTopPattern = new RegExp(paginationSelector.replace(/\./g, '\\.') + '[^{]*\\{[^}]*margin-top\\s*:\\s*\\d+', 'm')
        if (!marginTopPattern.test(content)) {
          // 给已有规则加 margin-top
          const addToPattern = new RegExp('(' + paginationSelector.replace(/\./g, '\\.') + '[^{]*\\{)')
          content = content.replace(addToPattern, '$1\n  margin-top: 12px;')
          changed = true
        }
      }
    }
  }

  // 给 wrapper 加 flex 布局（如果有 wrapper 且还没有这些样式）
  if (actualWrapperClass) {
    const wrapperSelector = '.' + actualWrapperClass
    const wrapperRuleExists = new RegExp(wrapperSelector.replace(/\./g, '\\.') + '\\s*\\{', 'm').test(content)

    if (!wrapperRuleExists) {
      additionalStyles.unshift(wrapperSelector + ' {\n  flex: 1;\n  display: flex;\n  flex-direction: column;\n  min-height: 0;\n}')
      changed = true
    } else {
      // 已有 wrapper 规则，检查是否缺少 min-height: 0
      const wrapperContentMatch = content.match(new RegExp(wrapperSelector.replace(/\./g, '\\.') + '\\s*\\{([^}]*)\\}', 'm'))
      if (wrapperContentMatch) {
        const ruleContent = wrapperContentMatch[1]
        if (!/min-height\s*:\s*0/.test(ruleContent)) {
          content = content.replace(
            new RegExp('(' + wrapperSelector.replace(/\./g, '\\.') + '\\s*\\{)'),
            '$1\n  min-height: 0;\n  flex: 1;'
          )
          changed = true
        }
      }
    }
  }

  // 添加新样式规则
  if (additionalStyles.length > 0) {
    if (hasStyleScope) {
      // 找到 </style> 之前插入
      content = content.replace(/(<style[^>]*scoped[^>]*>\s*)/, '$1' + additionalStyles.join('\n\n') + '\n\n')
    } else if (hasStyle) {
      // 有非 scoped 的 style，也用它
      content = content.replace(/(<style[^>]*>\s*)/, '$1' + additionalStyles.join('\n\n') + '\n\n')
    } else {
      // 没有 style，添加一个
      content = content + '\n\n<style scoped>\n' + additionalStyles.join('\n\n') + '\n</style>\n'
    }
  }

  if (changed) {
    fs.writeFileSync(filePath, content, 'utf-8')
    console.log('[已修复] ' + page.file)
    fixedCount++
  } else {
    console.log('[无需修改] ' + page.file)
  }
}

console.log('\n共修复 ' + fixedCount + ' 个页面')