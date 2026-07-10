<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 左侧：上传 + 设置 -->
      <el-col :span="12">
        <el-card shadow="never" class="left-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('ocr.upload.title') }}</span>
            </div>
          </template>

          <div class="left-card-body">
            <el-upload
              ref="uploadRef"
              class="ocr-upload"
              drag
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleRemove"
              :before-upload="() => false"
              accept=".pdf,.docx,.doc,.xls,.xlsx,.png,.jpg,.jpeg,.bmp,.tiff,.tif,.gif,.txt,.md,.html,.htm,.xml,.csv,.json,.yaml,.yml,.sql,.java,.js,.ts,.py,.go,.css,.scss,.sh,.log"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                {{ $t('ocr.upload.dragHint') }} <em>{{ $t('ocr.upload.clickHint') }}</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  {{ $t('ocr.upload.supportedFormats') }}
                </div>
              </template>
            </el-upload>

            <div v-if="selectedFile" class="file-info">
              <el-tag :type="getFileTypeTag(selectedFile.name)" size="small">
                {{ getFileTypeLabel(selectedFile.name) }}
              </el-tag>
              <span class="file-name">{{ selectedFile.name }}</span>
              <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
            </div>

            <div class="ocr-settings">
              <el-select v-model="language" :placeholder="$t('ocr.settings.language')" style="width: 200px">
                <el-option label="简体中文 + 英文" value="chi_sim+eng" />
                <el-option label="简体中文" value="chi_sim" />
                <el-option label="英文" value="eng" />
                <el-option label="日文" value="jpn" />
              </el-select>

              <el-button
                type="primary"
                :loading="recognizing"
                :disabled="!selectedFile"
                @click="handleRecognize"
                style="margin-left: 12px"
              >
                <el-icon v-if="!recognizing"><Document /></el-icon>
                {{ recognizing ? $t('ocr.status.recognizing') : $t('ocr.action.recognize') }}
              </el-button>

              <el-button
                v-if="selectedFile"
                type="danger"
                :disabled="recognizing"
                @click="handleClearFile"
                style="margin-left: 8px"
              >
                <el-icon><Close /></el-icon>
                {{ $t('ocr.action.cancel') }}
              </el-button>
            </div>

            <div v-if="recognizing" class="recognizing-tip">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>{{ $t('ocr.status.recognizing') }}</span>
              <el-button type="danger" size="small" @click="handleCancel" style="margin-left: 12px">
                {{ $t('ocr.action.cancelRecognition') }}
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：识别结果预览 -->
      <el-col :span="12">
        <el-card shadow="never" class="result-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('ocr.result.title') }}</span>
            </div>
          </template>

          <div v-if="result" class="result-section">
            <div class="result-meta">
              <el-tag type="success" size="small">{{ $t('ocr.result.engine') }}: {{ result.ocrEngine }}</el-tag>
              <el-tag size="small">{{ $t('ocr.result.chars') }}: {{ result.charCount }}</el-tag>
              <el-tag size="small"
                >{{ $t('ocr.result.duration') }}: {{ (result.durationMs / 1000).toFixed(1) }}s</el-tag
              >
              <el-tag v-if="result.confidence" size="small"
                >{{ $t('ocr.result.confidence') }}: {{ result.confidence.toFixed(1) }}%</el-tag
              >
            </div>

            <el-input v-model="resultText" type="textarea" :rows="18" :readonly="true" class="result-text" />

            <div class="result-actions">
              <el-button @click="handleCopy">
                <el-icon><CopyDocument /></el-icon> {{ $t('ocr.action.copy') }}
              </el-button>
              <el-button @click="handleDownload">
                <el-icon><Download /></el-icon> {{ $t('ocr.action.download') }}
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshRight /></el-icon> {{ $t('ocr.action.retry') }}
              </el-button>
            </div>
          </div>

          <el-empty v-else :description="$t('ocr.result.empty')" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 下方：识别历史 -->
    <el-card shadow="never" class="history-card" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>{{ $t('ocr.history.title') }}</span>
          <el-button v-if="historyList.length > 0" type="danger" link size="small" @click="handleDeleteAll">
            <el-icon><Delete /></el-icon>
            {{ $t('ocr.history.deleteAll') }}
          </el-button>
        </div>
      </template>

      <div class="history-grid">
        <div v-for="row in historyList" :key="row.id" class="history-item">
          <div class="history-item-main">
            <div class="history-item-row">
              <el-tag :type="getFileTypeTag(row.fileName)" size="small">
                {{ getFileTypeLabel(row.fileName) }}
              </el-tag>
              <span class="history-item-name">{{ row.fileName }}</span>
            </div>
            <div class="history-item-row history-item-meta">
              <span>{{ $t('ocr.history.chars') }}: {{ row.charCount }}</span>
              <span
                >{{ $t('ocr.result.duration') }}:
                {{ row.durationMs ? (row.durationMs / 1000).toFixed(1) + 's' : '-' }}</span
              >
              <span>{{ formatTime(row.createTime) }}</span>
            </div>
          </div>
          <div class="history-item-actions">
            <el-button link type="primary" size="small" @click="viewDetail(row)">
              {{ $t('common.detail') }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              {{ $t('common.delete') }}
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="historyList.length === 0" :description="$t('ocr.history.empty')" />

      <el-pagination
        v-if="historyTotal > pageSize"
        v-model:current-page="historyPage"
        :page-size="pageSize"
        :total="historyTotal"
        layout="total, prev, pager, next"
        size="small"
        @current-change="fetchHistory"
        style="margin-top: 12px; justify-content: center"
      />
    </el-card>

    <el-dialog v-model="detailVisible" :title="$t('ocr.detail.title')" width="700px">
      <div v-if="detailData" class="detail-content">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item :label="$t('ocr.history.fileName')">{{ detailData.fileName }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ocr.result.engine')">{{ detailData.ocrEngine }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ocr.result.chars')">{{ detailData.charCount }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ocr.result.duration')"
            >{{ (detailData.durationMs / 1000).toFixed(1) }}s</el-descriptions-item
          >
        </el-descriptions>
        <el-input
          :model-value="detailData.resultText"
          type="textarea"
          :rows="12"
          :readonly="true"
          style="margin-top: 12px"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { recognizeOcrApi, getOcrPageApi, deleteOcrApi, deleteAllOcrApi } from '@/api/ocr'

defineOptions({ name: 'OcrRecognition' })

const { t } = useI18n()

const uploadRef = ref(null)
const selectedFile = ref(null)
const language = ref('chi_sim+eng')
const recognizing = ref(false)
const result = ref(null)
const resultText = ref('')

const historyList = ref([])
const historyTotal = ref(0)
const historyPage = ref(1)
const pageSize = 12

const detailVisible = ref(false)
const detailData = ref(null)

let cancelled = false

onMounted(() => {
  fetchHistory()
})

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function handleRemove() {
  selectedFile.value = null
}

function handleClearFile() {
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

async function handleRecognize() {
  if (!selectedFile.value) return

  recognizing.value = true
  result.value = null
  resultText.value = ''
  cancelled = false

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('language', language.value)

    const res = await recognizeOcrApi(formData)

    if (cancelled) return

    result.value = res.data
    resultText.value = res.data.resultText || ''
    ElMessage.success(t('ocr.status.success'))

    uploadRef.value?.clearFiles()
    selectedFile.value = null

    fetchHistory()
  } catch (e) {
    if (!cancelled) {
      ElMessage.error(e.message || t('ocr.status.failed'))
    }
  } finally {
    if (!cancelled) {
      recognizing.value = false
    }
  }
}

function handleCancel() {
  cancelled = true
  recognizing.value = false
  ElMessage.info(t('ocr.status.cancelled'))
}

function handleCopy() {
  navigator.clipboard.writeText(resultText.value).then(() => {
    ElMessage.success(t('ocr.action.copySuccess'))
  })
}

function handleDownload() {
  if (!result.value) return
  const blob = new Blob([resultText.value], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = result.value.fileName.replace(/\.[^.]+$/, '') + '_ocr.txt'
  a.click()
  URL.revokeObjectURL(url)
}

function handleReset() {
  selectedFile.value = null
  result.value = null
  resultText.value = ''
  uploadRef.value?.clearFiles()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('ocr.history.confirmDelete', { name: row.fileName }), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteOcrApi(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchHistory()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error(e.message || t('common.deleteFailed'))
    }
  }
}

async function handleDeleteAll() {
  try {
    await ElMessageBox.confirm(t('ocr.history.confirmDeleteAll'), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteAllOcrApi()
    ElMessage.success(t('common.deleteSuccess'))
    fetchHistory()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error(e.message || t('common.deleteFailed'))
    }
  }
}

async function fetchHistory() {
  try {
    const res = await getOcrPageApi({ page: historyPage.value, size: pageSize })
    historyList.value = res.data.records || []
    historyTotal.value = res.data.total || 0
  } catch {
    // ignore
  }
}

function viewDetail(row) {
  detailData.value = row
  detailVisible.value = true
}

function getFileTypeTag(name) {
  if (!name) return 'info'
  const ext = name.split('.').pop().toLowerCase()
  if (ext === 'pdf') return 'danger'
  if (ext === 'docx') return 'primary'
  return 'success'
}

function getFileTypeLabel(name) {
  if (!name) return ''
  return name.split('.').pop().toUpperCase()
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

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.left-card,
.result-card {
  height: 100%;
}
.left-card:hover,
.result-card:hover,
.history-card:hover {
  transform: none !important;
  box-shadow: none !important;
  border-color: var(--border-color) !important;
}
.left-card :deep(.el-card__body),
.result-card :deep(.el-card__body) {
  height: calc(100% - 56px);
  display: flex;
  flex-direction: column;
}
.left-card-body {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.ocr-upload {
  width: 100%;
}
.file-info {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.file-name {
  font-weight: 500;
}
.file-size {
  color: var(--color-text-secondary);
  font-size: 13px;
}
.ocr-settings {
  margin-top: 16px;
  display: flex;
  align-items: center;
}
.recognizing-tip {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-primary);
  font-size: 13px;
}
.result-section {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.result-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.result-text {
  font-family: monospace;
  flex: 1;
}
.result-text :deep(.el-textarea__inner) {
  height: 100% !important;
}
.result-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

/* 历史记录网格布局 */
.history-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  transition: none;
}
.history-item:hover {
  border-color: var(--el-border-color-lighter);
  transform: none;
  box-shadow: none;
}
.history-item-main {
  flex: 1;
  min-width: 0;
}
.history-item-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.history-item-name {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.history-item-meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-secondary);
  gap: 12px;
}
.history-item-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 8px;
}

.detail-content {
  max-height: 500px;
  overflow-y: auto;
}
</style>
