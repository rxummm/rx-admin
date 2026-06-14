const fs = require('fs')
const path = require('path')

// 需要修正的页面：用 pagination-wrap 包裹分页器的
const pages = [
  'src/views/monitor/export-log/index.vue',
  'src/views/monitor/job-log/index.vue',
  'src/views/monitor/login-log/index.vue',
]

for (const filePath of pages) {
  const fullPath = path.join(__dirname, filePath)
  if (!fs.existsSync(fullPath)) {
    console.log('[跳过] ' + filePath)
    continue
  }

  let content = fs.readFileSync(fullPath, 'utf-8')

  // 替换错误的 .pagination-wrap 样式：
  // 把 { flex: 1; display: flex; flex-direction: column; min-height: 0; }
  // 改为 { margin-top: 12px; }
  const oldPattern = /\.pagination-wrap\s*\{[^}]*flex[^}]*display[^}]*flex-direction[^}]*min-height[^}]*\}/
  if (oldPattern.test(content)) {
    content = content.replace(oldPattern, '.pagination-wrap {\n  margin-top: 12px;\n}')
    console.log('[已修正] ' + filePath)
  } else {
    // 检查是否已有 pagination-wrap 样式但内容不对
    const anyPattern = /\.pagination-wrap\s*\{([^}]*)\}/
    const match = content.match(anyPattern)
    if (match) {
      const inner = match[1]
      if (!/margin-top/.test(inner)) {
        content = content.replace(anyPattern, '.pagination-wrap {\n  margin-top: 12px;\n}')
        console.log('[已修正] ' + filePath)
      } else {
        console.log('[OK] ' + filePath)
      }
    } else {
      // 没有样式块，添加一个
      content = content.replace(/<\/script>\s*\n/, '</script>\n\n<style scoped>\n.pagination-wrap {\n  margin-top: 12px;\n}\n</style>\n')
      console.log('[已添加] ' + filePath)
    }
  }

  fs.writeFileSync(fullPath, content, 'utf-8')
}

// 处理其他几个特殊页面：content/message, content/notice, content/notify-center, tool/dict, monitor/job, monitor/slow-query
const specialPages = [
  { file: 'src/views/content/message/index.vue', hasWrapper: false, paginationClass: '' },
  { file: 'src/views/content/notice/index.vue', hasWrapper: true, wrapperClass: 'notice-table-wrapper', paginationClass: 'notice-pagination' },
  { file: 'src/views/content/notify-center/index.vue', hasWrapper: false, paginationClass: '' },
  { file: 'src/views/tool/dict/index.vue', hasWrapper: false, paginationClass: '' },
  { file: 'src/views/monitor/job/index.vue', hasWrapper: false, paginationClass: '' },
  { file: 'src/views/monitor/slow-query/index.vue', hasWrapper: true, wrapperClass: 'virtual-table-wrapper', paginationClass: '' },
]

for (const page of specialPages) {
  const fullPath = path.join(__dirname, page.file)
  if (!fs.existsSync(fullPath)) {
    console.log('[跳过] ' + page.file)
    continue
  }

  let content = fs.readFileSync(fullPath, 'utf-8')

  // 查找分页器 class
  const paginationMatch = content.match(/<el-pagination[^>]*class="([^"]*)"/)
  const actualPaginationClass = paginationMatch ? paginationMatch[1] : ''

  // 查找 wrapper class
  const wrapperMatch = content.match(/class="([^"]*-table-wrapper|[^"]*wrapper)"/)
  const actualWrapperClass = wrapperMatch ? wrapperMatch[1] : ''

  // 如果有分页器 class，且没有对应的 margin-top 样式，添加
  if (actualPaginationClass) {
    const selector = '.' + actualPaginationClass.replace(/\./g, '\\.')
    const hasStyle = new RegExp(selector.replace(/\./g, '\\.') + '\\s*\\{').test(content)
    if (!hasStyle) {
      content = content.replace(/(<style[^>]*>)/, '$1\n' + selector + ' {\n  margin-top: 12px;\n}\n')
      console.log('[已添加分页器样式] ' + page.file)
    } else {
      console.log('[OK] ' + page.file)
    }
  } else if (actualWrapperClass) {
    // 没有分页器 class，但有 wrapper，检查是否有分页器的 margin-top
    const hasMarginTop = /el-pagination[^>]*>[\s\S]*?margin-top/.test(content)
    if (!hasMarginTop) {
      // 查找 el-pagination 是否有外层 div 的 class
      const paginationOuterMatch = content.match(/<div[^>]*class="([^"]*)"[^>]*>\s*<el-pagination/m)
      if (paginationOuterMatch) {
        const outerSelector = '.' + paginationOuterMatch[1]
        const hasOuterStyle = new RegExp(outerSelector.replace(/\./g, '\\.') + '\\s*\\{').test(content)
        if (!hasOuterStyle) {
          content = content.replace(/(<style[^>]*>)/, '$1\n' + outerSelector + ' {\n  margin-top: 12px;\n}\n')
          console.log('[已添加外层分页器样式] ' + page.file)
        } else {
          console.log('[OK] ' + page.file)
        }
      } else {
        console.log('[跳过-无分页器class] ' + page.file)
      }
    }
  } else {
    console.log('[跳过] ' + page.file)
  }

  fs.writeFileSync(fullPath, content, 'utf-8')
}

console.log('\n完成！')