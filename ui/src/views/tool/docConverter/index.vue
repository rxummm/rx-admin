<template>
  <div class="page-container">
    <div class="page-header">
      <h3>文档格式转换</h3>
      <p class="page-desc">支持 PDF 与 Word 文档互相转换</p>
    </div>

    <el-row :gutter="20">
      <!-- PDF 转 Word -->
      <el-col :span="12">
        <el-card class="convert-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon color="#e74c3c" size="24"><Document /></el-icon>
              <span>PDF → Word</span>
            </div>
          </template>

          <el-upload
            ref="pdfUploadRef"
            class="convert-upload"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="(f) => handleFileChange(f, 'pdf')"
            :on-remove="() => handleRemove('pdf')"
            :before-upload="() => false"
            accept=".pdf"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽PDF文件到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">支持 .pdf 格式</div>
            </template>
          </el-upload>

          <div class="convert-actions" v-if="pdfFile">
            <div class="selected-file">
              <el-tag type="danger" size="small">PDF</el-tag>
              <span class="file-name">{{ pdfFile.name }}</span>
              <span class="file-size">{{ formatFileSize(pdfFile.size) }}</span>
            </div>
            <el-input
              v-model="pdfOutputDir"
              placeholder="输出目录（可选，默认系统目录）"
              size="small"
              style="margin-top: 8px"
            />
            <el-button
              type="primary"
              style="margin-top: 8px"
              @click="handleConvertPdfToWord"
              :loading="pdfConverting"
            >
              <el-icon><Switch /></el-icon> 开始转换
            </el-button>
          </div>

          <div class="convert-result" v-if="pdfResult">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>
                <span>转换成功！</span>
              </template>
              <div class="result-path">
                <el-tag type="success" size="small">输出文件</el-tag>
                <span class="path-text">{{ pdfResult.outputPath }}</span>
              </div>
            </el-alert>
          </div>
        </el-card>
      </el-col>

      <!-- Word 转 PDF -->
      <el-col :span="12">
        <el-card class="convert-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon color="#f59e0b" size="24"><Document /></el-icon>
              <span>Word → PDF</span>
            </div>
          </template>

          <el-upload
            ref="wordUploadRef"
            class="convert-upload"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="(f) => handleFileChange(f, 'word')"
            :on-remove="() => handleRemove('word')"
            :before-upload="() => false"
            accept=".docx"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽Word文件到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">支持 .docx 格式</div>
            </template>
          </el-upload>

          <div class="convert-actions" v-if="wordFile">
            <div class="selected-file">
              <el-tag type="primary" size="small">DOCX</el-tag>
              <span class="file-name">{{ wordFile.name }}</span>
              <span class="file-size">{{ formatFileSize(wordFile.size) }}</span>
            </div>
            <el-input
              v-model="wordOutputDir"
              placeholder="输出目录（可选，默认系统目录）"
              size="small"
              style="margin-top: 8px"
            />
            <el-button
              type="primary"
              style="margin-top: 8px"
              @click="handleConvertWordToPdf"
              :loading="wordConverting"
            >
              <el-icon><Switch /></el-icon> 开始转换
            </el-button>
          </div>

          <div class="convert-result" v-if="wordResult">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>
                <span>转换成功！</span>
              </template>
              <div class="result-path">
                <el-tag type="success" size="small">输出文件</el-tag>
                <span class="path-text">{{ wordResult.outputPath }}</span>
              </div>
            </el-alert>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document, Switch } from '@element-plus/icons-vue'
import { convertPdfToWordApi, convertWordToPdfApi } from '@/api/commonTools'

defineOptions({ name: 'ToolDocConverter' })

// PDF 转 Word
const pdfUploadRef = ref(null)
const pdfFile = ref(null)
const pdfConverting = ref(false)
const pdfOutputDir = ref('')
const pdfResult = ref(null)

// Word 转 PDF
const wordUploadRef = ref(null)
const wordFile = ref(null)
const wordConverting = ref(false)
const wordOutputDir = ref('')
const wordResult = ref(null)

function handleFileChange(file, type) {
  const raw = file.raw || file
  if (type === 'pdf') {
    pdfFile.value = raw
    pdfResult.value = null
  } else {
    wordFile.value = raw
    wordResult.value = null
  }
}

function handleRemove(type) {
  if (type === 'pdf') {
    pdfFile.value = null
    pdfResult.value = null
  } else {
    wordFile.value = null
    wordResult.value = null
  }
}

async function handleConvertPdfToWord() {
  if (!pdfFile.value) {
    ElMessage.warning('请先选择PDF文件')
    return
  }
  pdfConverting.value = true
  pdfResult.value = null
  try {
    const formData = new FormData()
    formData.append('file', pdfFile.value)
    if (pdfOutputDir.value) {
      formData.append('outputDir', pdfOutputDir.value)
    }
    const res = await convertPdfToWordApi(formData)
    pdfResult.value = res.data || res
    ElMessage.success('PDF转Word成功！')
  } catch (error) {
    ElMessage.error('转换失败：' + (error.message || '未知错误'))
  } finally {
    pdfConverting.value = false
  }
}

async function handleConvertWordToPdf() {
  if (!wordFile.value) {
    ElMessage.warning('请先选择Word文件')
    return
  }
  wordConverting.value = true
  wordResult.value = null
  try {
    const formData = new FormData()
    formData.append('file', wordFile.value)
    if (wordOutputDir.value) {
      formData.append('outputDir', wordOutputDir.value)
    }
    const res = await convertWordToPdfApi(formData)
    wordResult.value = res.data || res
    ElMessage.success('Word转PDF成功！')
  } catch (error) {
    ElMessage.error('转换失败：' + (error.message || '未知错误'))
  } finally {
    wordConverting.value = false
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
.convert-card {
  height: 100%;
}
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.convert-upload {
  width: 100%;
}
:deep(.convert-upload .el-upload) {
  width: 100%;
}
:deep(.convert-upload .el-upload-dragger) {
  width: 100%;
}
.convert-actions {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
}
.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.file-name {
  color: #303133;
  font-weight: 500;
}
.file-size {
  color: #909399;
}
.convert-result {
  margin-top: 12px;
}
.result-path {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.path-text {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}
</style>