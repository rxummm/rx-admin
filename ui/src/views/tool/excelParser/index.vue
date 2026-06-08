<template>
  <div class="page-container">
    <div class="page-header">
      <h3>Excel解析</h3>
      <p class="page-desc">上传Excel文件（.xlsx / .xls），自动解析并在表格中展示数据</p>
    </div>

    <!-- 上传区域 -->
    <el-card class="upload-card" shadow="never">
      <el-upload
        ref="uploadRef"
        class="excel-upload"
        drag
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-remove="handleRemove"
        :before-upload="() => false"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将Excel文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .xlsx 和 .xls 格式，文件大小不超过 50MB
          </div>
        </template>
      </el-upload>

      <div class="upload-actions" v-if="currentFile">
        <el-button type="primary" @click="handleParse" :loading="parsing">
          <el-icon><Search /></el-icon> 开始解析
        </el-button>
        <span class="file-info">
          当前文件：{{ currentFile.name }}
          <el-tag size="small" type="info" style="margin-left:8px">{{ formatFileSize(currentFile.size) }}</el-tag>
        </span>
      </div>
    </el-card>

    <!-- 搜索结果 -->
    <el-card class="result-card" shadow="never" v-if="tableData.length > 0">
      <template #header>
        <div class="card-header">
          <span>解析结果</span>
          <div class="header-right">
            <span class="result-stats">
              共 <strong>{{ filteredData.length }}</strong> 条数据（总计 {{ tableData.length }} 条）
            </span>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索表格内容..."
              clearable
              style="width: 240px; margin-left: 12px"
              :prefix-icon="Search"
            />
            <el-button style="margin-left: 8px" @click="exportToCSV" :disabled="filteredData.length === 0">
              <el-icon><Download /></el-icon> 导出CSV
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="pagedData"
        border
        stripe
        style="width: 100%"
        max-height="500"
        v-loading="parsing"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column
          v-for="col in columns"
          :key="col"
          :prop="col"
          :label="col"
          min-width="120"
          show-overflow-tooltip
        />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredData.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-card class="result-card" shadow="never" v-if="tableData.length === 0 && !parsing && hasSearched">
      <el-empty description="暂无数据，请上传Excel文件进行解析" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { UploadFilled, Search, Download } from '@element-plus/icons-vue'
import { parseExcelApi } from '@/api/commonTools'

defineOptions({ name: 'ToolExcelParser' })

const { t } = useI18n()

const uploadRef = ref(null)
const currentFile = ref(null)
const parsing = ref(false)
const hasSearched = ref(false)

const tableData = ref([])
const columns = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索过滤
const filteredData = computed(() => {
  if (!searchKeyword.value) return tableData.value
  const kw = searchKeyword.value.toLowerCase()
  return tableData.value.filter(row => {
    return Object.values(row).some(v => String(v).toLowerCase().includes(kw))
  })
})

// 分页数据
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

// 监听搜索关键词变化，重置分页
const resetPage = () => { currentPage.value = 1 }

// 文件变化
function handleFileChange(file) {
  currentFile.value = file.raw || file
  hasSearched.value = false
  tableData.value = []
  columns.value = []
}

function handleRemove() {
  currentFile.value = null
  tableData.value = []
  columns.value = []
}

async function handleParse() {
  if (!currentFile.value) {
    ElMessage.warning(t('tool.excelParser.selectFile'))
    return
  }
  parsing.value = true
  hasSearched.value = true
  try {
    const formData = new FormData()
    formData.append('file', currentFile.value)
    const res = await parseExcelApi(formData)
    if (res.data && res.data.rows) {
      tableData.value = res.data.rows || []
      columns.value = res.data.columns || []
      ElMessage.success(t('tool.excelParser.parseSuccess'))
    } else if (Array.isArray(res.data)) {
      tableData.value = res.data
      if (res.data.length > 0) {
        columns.value = Object.keys(res.data[0])
      }
      ElMessage.success(t('tool.excelParser.parseSuccess'))
    }
    resetPage()
  } catch (error) {
    ElMessage.error(t('tool.excelParser.parseFailed') + '：' + (error.message || ''))
  } finally {
    parsing.value = false
  }
}

function exportToCSV() {
  if (filteredData.value.length === 0) return
  const headers = columns.value
  let csv = '\uFEFF' + headers.join(',') + '\n'
  filteredData.value.forEach(row => {
    const rowData = headers.map(h => {
      let val = String(row[h] ?? '')
      if (val.includes(',') || val.includes('"') || val.includes('\n')) {
        val = '"' + val.replace(/"/g, '""') + '"'
      }
      return val
    })
    csv += rowData.join(',') + '\n'
  })
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'excel_data.csv'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(t('tool.excelParser.exportSuccess'))
}

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #303133;
}
.page-desc {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.upload-card {
  margin-bottom: 16px;
}
.excel-upload {
  width: 100%;
}
:deep(.excel-upload .el-upload) {
  width: 100%;
}
:deep(.excel-upload .el-upload-dragger) {
  width: 100%;
}
.upload-actions {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.file-info {
  color: #606266;
  font-size: 13px;
}
.result-card {
  margin-top: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.header-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.result-stats {
  color: #909399;
  font-size: 13px;
}
.result-stats strong {
  color: #409eff;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
