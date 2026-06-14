const fs = require('fs')
const path = require('path')

const viewDir = path.join(__dirname, 'src', 'views')
const modules = ['system', 'content', 'tool', 'monitor']
const results = []

for (const mod of modules) {
  const dir = path.join(viewDir, mod)
  if (!fs.existsSync(dir)) continue

  function walk(dir) {
    const entries = fs.readdirSync(dir, { withFileTypes: true })
    for (const entry of entries) {
      if (entry.isDirectory()) {
        walk(path.join(dir, entry.name))
      } else if (entry.name.endsWith('.vue')) {
        const filePath = path.join(dir, entry.name)
        const content = fs.readFileSync(filePath, 'utf-8')

        // 不关心 template/script/style 边界，直接全文搜索
        const hasElPagination = /el-pagination/.test(content)
        if (!hasElPagination) continue

        // 找 wrapper class 名（在模板中，最靠近 el-table 的那个 class 带 wrapper 字样）
        const wrapperMatches = content.match(/class="([^"]*-[^"]*wrapper[^"]*)"/g) || []
        const wrapperClasses = wrapperMatches.map(m => m.replace(/class="/, '').replace(/"/, ''))

        // 找分页器 class 名
        const paginationMatch = content.match(/<el-pagination[^>]*class="([^"]*)"/)
        const paginationClass = paginationMatch ? paginationMatch[1] : ''

        // 检查 margin-top:auto
        const hasMarginAuto = /margin-top\s*:\s*auto/.test(content)

        // 检查有没有 margin-top: <number>
        const marginTopMatch = content.match(/\.page-pagination[^{]*\{[^}]*margin-top[^}]*\}|\.pagination-wrap[^{]*\{[^}]*margin-top[^}]*\}/)
        const hasMarginTop = !!marginTopMatch

        // 检查 min-height:0
        const hasMinHeight0 = /min-height\s*:\s*0/.test(content)

        // 检查 useTableHeight
        const usesTableHeight = /useTableHeight/.test(content)

        // 检查 :max-height
        const hasMaxHeight = /:max-height="|max-height=/.test(content)

        results.push({
          file: path.relative(__dirname, filePath).replace(/\\/g, '/'),
          module: mod,
          wrapperClasses,
          paginationClass,
          hasMarginAuto,
          hasMarginTop,
          hasMinHeight0,
          usesTableHeight,
          hasMaxHeight,
        })
      }
    }
  }
  walk(dir)
}

console.log('===== 页面分页条分析报告 =====\n')
console.log('总页数:', results.length)
console.log()

const grouped = {}
for (const r of results) {
  if (!grouped[r.module]) grouped[r.module] = []
  grouped[r.module].push(r)
}

for (const mod of modules) {
  if (!grouped[mod]) continue
  console.log('=== ' + mod + ' 模块 (' + grouped[mod].length + ' 个页面) ===')
  for (const r of grouped[mod]) {
    const issues = []
    if (r.hasMarginAuto) issues.push('分页器用margin-top:auto(固定底部)')
    if (!r.hasMarginTop && !r.hasMarginAuto) issues.push('分页器无顶部间距')
    if (r.wrapperClasses.length > 0 && !r.hasMinHeight0) issues.push('wrapper缺少min-height:0')

    const status = issues.length === 0 ? '[OK]' : '[需修复] ' + issues.join('; ')
    console.log('  ' + r.file + ' - ' + status)
    if (r.wrapperClasses.length > 0) console.log('    wrappers: ' + r.wrapperClasses.join(', '))
    if (r.paginationClass) console.log('    pagination: ' + r.paginationClass)
    if (r.usesTableHeight) console.log('    useTableHeight: 是')
    if (r.hasMaxHeight) console.log('    max-height: 是')
  }
  console.log()
}

const needFix = results.filter(r => r.hasMarginAuto || (!r.hasMarginTop && !r.hasMarginAuto) || (r.wrapperClasses.length > 0 && !r.hasMinHeight0))

console.log('===== 需要修复的页面清单 (' + needFix.length + ') =====')
for (const r of needFix) {
  console.log('- ' + r.file)
}