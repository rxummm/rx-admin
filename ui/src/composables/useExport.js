import { ElMessage } from 'element-plus'
import { exportExcelApi, exportPdfApi } from '@/api/export'

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

export function useExport() {
  async function exportExcel(title, columns, data) {
    try {
      const res = await exportExcelApi({ title, columns, data })
      downloadBlob(res, `${title}.xlsx`)
      ElMessage.success('导出成功')
    } catch {
      ElMessage.error('导出失败')
    }
  }

  async function exportPdf(title, columns, data) {
    try {
      const res = await exportPdfApi({ title, columns, data })
      downloadBlob(res, `${title}.pdf`)
      ElMessage.success('导出成功')
    } catch {
      ElMessage.error('导出失败')
    }
  }

  return { exportExcel, exportPdf }
}
