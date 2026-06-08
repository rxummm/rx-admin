/**
 * 纯前端导出工具 - Excel & PDF
 * 数据已从后端查询，无需再次请求，在前端直接生成文件
 *
 * 依赖: exceljs, jspdf, jspdf-autotable, html2canvas
 */
import ExcelJS from 'exceljs'
import jsPDF from 'jspdf'
import 'jspdf-autotable'
import html2canvas from 'html2canvas'

// ──────────────────── 通用工具 ────────────────────

/** 格式化日期为 YYYYMMDDHHmmss */
function formatDate() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
}

/** 触发浏览器下载 */
function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

/** 获取单元格值字符串 */
function cellText(row, field) {
  const val = row[field]
  if (val === null || val === undefined) return ''
  return String(val)
}

// ──────────────────── Excel 导出 ────────────────────

/**
 * 纯前端导出 Excel (.xlsx)
 * @param {{ title: string, columns: {field:string, label:string}[], data: object[] }} options
 */
export async function exportExcelClient({ title, columns, data }) {
  const workbook = new ExcelJS.Workbook()
  const sheet = workbook.addWorksheet(title || 'Sheet1')

  // 标题行（合并单元格，加粗居中）
  const titleRow = sheet.addRow([title + '  ' + new Date().toLocaleString()])
  if (columns.length > 1) {
    sheet.mergeCells(1, 1, 1, columns.length)
  }
  titleRow.height = 28
  titleRow.font = { bold: true, size: 14 }
  titleRow.alignment = { horizontal: 'center', vertical: 'middle' }

  // 表头行
  const headerRow = sheet.addRow(columns.map(c => c.label))
  headerRow.height = 22
  headerRow.eachCell(cell => {
    cell.font = { bold: true, size: 10, color: { argb: 'FFFFFFFF' } }
    cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF409EFF' } }
    cell.alignment = { horizontal: 'center', vertical: 'middle' }
    cell.border = {
      top: { style: 'thin' }, bottom: { style: 'thin' },
      left: { style: 'thin' }, right: { style: 'thin' }
    }
  })

  // 数据行（斑马纹）
  data.forEach((row, i) => {
    const dataRow = sheet.addRow(columns.map(c => cellText(row, c.field)))
    dataRow.eachCell(cell => {
      cell.font = { size: 10 }
      cell.alignment = { vertical: 'middle' }
      cell.border = {
        top: { style: 'thin' }, bottom: { style: 'thin' },
        left: { style: 'thin' }, right: { style: 'thin' }
      }
    })
    if (i % 2 === 1) {
      dataRow.eachCell(cell => {
        cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF5F7FA' } }
      })
    }
  })

  // 自动列宽
  columns.forEach((col, idx) => {
    const colIndex = idx + 1
    let maxLen = col.label.length
    data.forEach(row => {
      const txt = cellText(row, col.field)
      // 中文字符按 2 个字符宽度计算
      const len = [...txt].reduce((sum, ch) => sum + (ch.charCodeAt(0) > 255 ? 2.2 : 1), 0)
      if (len > maxLen) maxLen = len
    })
    sheet.getColumn(colIndex).width = Math.min(Math.max(maxLen + 3, 10), 40)
  })

  // 冻结表头
  sheet.views = [{ state: 'frozen', ySplit: 2 }]

  const buffer = await workbook.xlsx.writeBuffer()
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  downloadBlob(blob, `${title}_${formatDate()}.xlsx`)
}

// ──────────────────── PDF 导出 ────────────────────

/**
 * 纯前端导出 PDF
 * 通过渲染隐藏 HTML 表格再截图的方式，完美支持中文（使用系统字体）
 * @param {{ title: string, columns: {field:string, label:string}[], data: object[] }} options
 */
export async function exportPdfClient({ title, columns, data }) {
  // 1. 创建隐藏的 HTML 表格用于截图
  const container = document.createElement('div')
  container.style.cssText = `
    position: fixed; left: -9999px; top: 0; z-index: -1;
    background: #fff; padding: 20px 24px; font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  `
  document.body.appendChild(container)

  try {
    // 构建 HTML 表格
    const tableHtml = buildTableHtml(title, columns, data)
    container.innerHTML = tableHtml

    // 等字体渲染完成
    await new Promise(resolve => setTimeout(resolve, 100))

    // 2. html2canvas 截图
    const canvas = await html2canvas(container, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false
    })

    // 3. 插入 PDF（A4 横向）
    const pdf = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' })
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const margin = 6 // mm

    const imgWidth = pageWidth - margin * 2
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    const imgData = canvas.toDataURL('image/png')

    let heightLeft = imgHeight
    let position = margin
    let page = 0

    // 首页
    pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight)
    heightLeft -= (pageHeight - margin * 2)

    // 后续页（如果表格很长）
    while (heightLeft > 0) {
      position = margin - (imgHeight - (pageHeight - margin * 2)) + page * (pageHeight - margin * 2)
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight)
      heightLeft -= (pageHeight - margin * 2)
      page++
    }

    pdf.save(`${title}_${formatDate()}.pdf`)
  } finally {
    document.body.removeChild(container)
  }
}

/**
 * 构建导出用的干净 HTML 表格
 */
function buildTableHtml(title, columns, data) {
  const headerCells = columns.map(c => `<th>${escHtml(c.label)}</th>`).join('')
  const dataRows = data.map((row, i) => {
    const bg = i % 2 === 0 ? '#ffffff' : '#f5f7fa'
    const cells = columns.map(c => `<td>${escHtml(cellText(row, c.field))}</td>`).join('')
    return `<tr style="background:${bg}">${cells}</tr>`
  }).join('')

  return `
    <style>
      table { border-collapse: collapse; width: 100%; font-size: 12px; color: #303133; }
      th { background: #409EFF; color: #fff; font-weight: bold; padding: 7px 10px;
           border: 1px solid #337ECC; text-align: center; }
      td { padding: 6px 10px; border: 1px solid #DCDFE6; }
      .title { font-size: 16px; font-weight: bold; color: #303133; margin-bottom: 10px; }
      .subtitle { font-size: 11px; color: #909399; margin-bottom: 12px; }
    </style>
    <div class="title">${escHtml(title)}</div>
    <div class="subtitle">导出时间: ${new Date().toLocaleString()}</div>
    <table>
      <thead><tr>${headerCells}</tr></thead>
      <tbody>${dataRows}</tbody>
    </table>
  `
}

function escHtml(str) {
  const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }
  return String(str).replace(/[&<>"]/g, c => map[c] || c)
}
