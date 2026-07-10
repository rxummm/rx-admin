<template>
  <div class="page-container">
    <div class="page-header">
      <h3>文档上传</h3>
      <p class="page-desc">上传文档到指定目录，支持查看、搜索、删除已上传文件</p>
    </div>

    <!-- 上传区域 -->
    <el-card class="upload-card" shadow="never">
      <div class="upload-section">
        <div class="upload-area">
          <el-upload
            ref="uploadRef"
            class="doc-upload"
            drag
            :auto-upload="false"
            :limit="5"
            :on-change="handleFileChange"
            :on-remove="handleRemoveFile"
            :before-upload="() => false"
            multiple
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持任意格式文件，单文件最大50MB，可同时选择多个文件</div>
            </template>
          </el-upload>
        </div>

        <div class="upload-config">
          <div class="config-item">
            <label>存储目录：</label>
            <el-input v-model="targetDir" placeholder="输入存储目录路径" size="default" clearable />
            <el-button @click="loadDefaultDir" :loading="loadingDir"> 默认路径 </el-button>
          </div>
          <div class="selected-files" v-if="selectedFiles.length > 0">
            <el-tag
              v-for="(f, idx) in selectedFiles"
              :key="idx"
              closable
              @close="removeSelectedFile(idx)"
              style="margin: 4px"
            >
              {{ f.name }} ({{ formatFileSize(f.size) }})
            </el-tag>
          </div>
          <el-button
            type="primary"
            @click="handleBatchUpload"
            :loading="uploading"
            :disabled="selectedFiles.length === 0"
          >
            <el-icon><Upload /></el-icon> 上传全部 ({{ selectedFiles.length }})
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 文件列表 -->
    <el-card class="list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>已上传文件</span>
          <div class="header-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文件名..."
              clearable
              style="width: 240px"
              @clear="fetchList"
              @keyup.enter="fetchList"
            />
            <el-button type="primary" @click="fetchList" style="margin-left: 8px">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button @click="handleRefresh">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="fileList" border stripe v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" underline="never" @click="previewFile(row)">
              <el-icon style="margin-right: 4px"><View /></el-icon>
              {{ row.fileName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="110" align="center">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.fileType?.replace('.', '') || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="170" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>

      <el-empty v-if="fileList.length === 0 && !loading" description="暂无上传文件" />
    </el-card>

    <!-- 文件预览弹窗 -->
    <el-dialog v-model="previewVisible" :title="previewFileInfo?.fileName" width="700px">
      <div class="preview-info" v-if="previewFileInfo">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="文件名">{{ previewFileInfo.fileName }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(previewFileInfo.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">{{ previewFileInfo.fileType }}</el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ previewFileInfo.uploadTime }}</el-descriptions-item>
          <el-descriptions-item label="存储路径" :span="2">{{ previewFileInfo.filePath }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Upload, Search, Refresh, View, Delete } from '@element-plus/icons-vue'
import { uploadDocumentApi, getDocumentListApi, deleteDocumentApi, getDefaultDirApi } from '@/api/commonTools'

defineOptions({ name: 'ToolDocUpload' })

const { t } = useI18n()

// 上传
const uploadRef = ref(null)
const selectedFiles = ref([])
const targetDir = ref('')
const uploading = ref(false)
const loadingDir = ref(false)

// 列表
const fileList = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 预览
const previewVisible = ref(false)
const previewFileInfo = ref(null)

onMounted(() => {
  fetchList()
})

function handleFileChange(file) {
  selectedFiles.value.push(file.raw || file)
}

function handleRemoveFile(uploadFile) {
  const idx = selectedFiles.value.findIndex((f) => f.name === uploadFile.name && f.size === uploadFile.size)
  if (idx >= 0) selectedFiles.value.splice(idx, 1)
}

function removeSelectedFile(idx) {
  selectedFiles.value.splice(idx, 1)
  uploadRef.value?.handleRemove?.(null)
}

async function handleBatchUpload() {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning(t('tool.docUpload.selectFile'))
    return
  }
  uploading.value = true
  let success = 0
  let _fail = 0
  try {
    for (const file of selectedFiles.value) {
      try {
        const formData = new FormData()
        formData.append('file', file)
        if (targetDir.value) {
          formData.append('targetDir', targetDir.value)
        }
        await uploadDocumentApi(formData)
        success++
      } catch {
        fail++
      }
    }
    if (success > 0) ElMessage.success(t('tool.docUpload.uploadSuccess'))
    else ElMessage.error(t('tool.docUpload.uploadFailed'))
    selectedFiles.value = []
    fetchList()
  } finally {
    uploading.value = false
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getDocumentListApi({
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value || undefined
    })
    const data = res.data || res
    fileList.value = data.records || []
    total.value = data.total || 0
  } catch {
    ElMessage.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
}

function handleRefresh() {
  searchKeyword.value = ''
  page.value = 1
  fetchList()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${row.fileName}" 吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteDocumentApi(row.id)
    ElMessage.success(t('tool.docUpload.deleteSuccess'))
    fetchList()
  } catch {
    // 取消删除
  }
}

function previewFile(row) {
  previewFileInfo.value = row
  previewVisible.value = true
}

async function loadDefaultDir() {
  loadingDir.value = true
  try {
    const res = await getDefaultDirApi()
    const data = res.data || res
    targetDir.value = data.defaultDir || data
    ElMessage.success('已加载默认路径')
  } catch {
    ElMessage.error('获取默认路径失败')
  } finally {
    loadingDir.value = false
  }
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
.upload-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.upload-area {
  width: 100%;
}
.doc-upload {
  width: 100%;
}
:deep(.doc-upload .el-upload) {
  width: 100%;
}
:deep(.doc-upload .el-upload-dragger) {
  width: 100%;
}
.upload-config {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.config-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.config-item label {
  white-space: nowrap;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}
.config-item .el-input {
  flex: 1;
}
.selected-files {
  min-height: 32px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
}
.list-card {
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
  gap: 8px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.preview-info {
  padding: 4px 0;
}
</style>
