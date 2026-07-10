<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        :placeholder="$t('audio.transcription.keyword')"
        clearable
        style="width: 240px"
        @keyup.enter="fetchData"
      />
      <el-select v-model="language" :placeholder="$t('audio.transcription.language')" clearable style="width: 120px">
        <el-option label="中文" value="zh" />
        <el-option label="English" value="en" />
      </el-select>
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <div style="flex: 1" />
      <el-button type="primary" @click="handleUpload" v-if="userStore.hasPerm('audio:transcription:upload')">
        <el-icon><Upload /></el-icon> {{ $t('audio.transcription.upload') }}
      </el-button>
      <el-button
        type="success"
        :icon="isRecording ? 'CircleClose' : 'Mic'"
        @click="toggleRecording"
        :disabled="isRecording && !canStopRecording"
        v-if="userStore.hasPerm('audio:transcription:upload')"
      >
        {{ isRecording ? '停止录音' : '麦克风录音' }}
      </el-button>
      <el-button
        type="danger"
        @click="handleBatchDelete"
        v-if="userStore.hasPerm('audio:transcription:delete')"
        :disabled="selectedIds.length === 0"
      >
        <el-icon><Delete /></el-icon> {{ $t('common.batchDelete') }}
      </el-button>
    </div>

    <div class="table-container">
      <el-table
        :data="tableData"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column
          prop="fileName"
          :label="$t('audio.transcription.fileName')"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="language" :label="$t('audio.transcription.language')" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.language === 'zh' ? '中文' : 'English' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" :label="$t('audio.transcription.modelName')" width="100" />
        <el-table-column prop="duration" :label="$t('audio.transcription.duration')" width="100">
          <template #default="{ row }">
            {{ row.duration != null ? Number(row.duration).toFixed(2) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)" size="small">
              {{ getStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('common.createTime')" width="170" />
        <el-table-column :label="$t('common.operation')" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              :disabled="transcribingIds.includes(row.id)"
              @click="showModelSelectDialog(row)"
              v-if="userStore.hasPerm('audio:transcription:upload')"
              >{{
                transcribingIds.includes(row.id) ? '转写中...' : row.status === 1 && row.fullText ? '重新转写' : '转写'
              }}</el-button
            >
            <el-button
              link
              type="primary"
              size="small"
              @click="handleView(row)"
              v-if="userStore.hasPerm('audio:transcription:view')"
              >{{ $t('common.view') }}</el-button
            >
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
              v-if="userStore.hasPerm('audio:transcription:delete')"
              >{{ $t('common.delete') }}</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="page-pagination"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <el-dialog
      v-model="uploadDialogVisible"
      :title="$t('audio.transcription.upload')"
      width="600px"
      :close-on-click-modal="false"
      draggable
    >
      <el-upload
        ref="uploadRef"
        :before-upload="beforeUpload"
        :on-change="handleFileChange"
        :on-exceed="handleUploadExceed"
        :on-remove="handleFileRemove"
        :limit="10"
        :auto-upload="false"
        drag
        multiple
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">{{ $t('audio.transcription.dragUpload') }}</div>
        <template #tip>
          <div class="el-upload__tip">
            {{ $t('audio.transcription.supportFormats') }}
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="cancelUpload">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading" :disabled="pendingFiles.length === 0">
          {{ uploading ? `上传中 (${currentUploadIndex + 1}/${uploadQueue.length})` : $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" :title="$t('audio.transcription.detail')" width="700px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">{{ $t('audio.transcription.fileName') }}：</span>
          <template v-if="editingFileName">
            <div class="file-name-edit-group">
              <el-input v-model="editFileNameValue" class="file-name-input" @keyup.enter="saveFileName" />
              <el-button size="small" type="primary" @click="saveFileName">保存</el-button>
              <el-button size="small" @click="cancelEditFileName">取消</el-button>
            </div>
          </template>
          <template v-else>
            <span>{{ detailData.fileName }}</span>
            <el-button size="small" @click="startEditFileName">编辑</el-button>
          </template>
        </div>
        <div class="detail-row">
          <span class="detail-label">{{ $t('audio.transcription.language') }}：</span>
          <el-tag size="small">{{ detailData.language === 'zh' ? '中文' : 'English' }}</el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">{{ $t('audio.transcription.modelName') }}：</span>
          <span>{{ detailData.modelName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">下载字幕：</span>
          <el-button size="small" @click="downloadSrtFromDetail">下载SRT</el-button>
          <el-button size="small" @click="downloadAssFromDetail">下载ASS</el-button>
        </div>
        <div class="detail-row">
          <span class="detail-label">{{ $t('audio.transcription.fullText') }}：</span>
        </div>
        <div class="full-text">{{ detailData.fullText }}</div>
        <div v-if="detailData.segments && detailData.segments.length" class="segments-section">
          <h4>{{ $t('audio.transcription.segments') }}</h4>
          <div v-for="(segment, index) in detailData.segments" :key="segment.id" class="segment-item">
            <span class="segment-time">[{{ formatTime(segment.startTime) }} - {{ formatTime(segment.endTime) }}]</span>
            <span v-if="segment.speakerName" class="segment-speaker">{{ segment.speakerName }}：</span>
            <span class="segment-text">{{ segment.text }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="modelSelectDialogVisible"
      title="选择模型"
      width="400px"
      :close-on-click-modal="false"
      draggable
    >
      <div class="model-select-content">
        <div class="model-select-tip">请选择用于转写的 Whisper 模型：</div>
        <div class="model-options">
          <div class="model-option" :class="{ selected: selectedModel === 'tiny' }" @click="selectedModel = 'tiny'">
            <div class="option-radio">
              <span v-if="selectedModel === 'tiny'" class="radio-inner"></span>
            </div>
            <div class="option-content">
              <span class="model-name">tiny</span>
              <span class="model-desc">速度快、CPU占用低，精度较低</span>
            </div>
          </div>
          <div class="model-option" :class="{ selected: selectedModel === 'small' }" @click="selectedModel = 'small'">
            <div class="option-radio">
              <span v-if="selectedModel === 'small'" class="radio-inner"></span>
            </div>
            <div class="option-content">
              <span class="model-name">small</span>
              <span class="model-desc">速度适中、精度较高（推荐）</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="modelSelectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTranscribeWithModel" :loading="transcribing">开始转写</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="transcribingDialogVisible"
      title="正在转写"
      width="420px"
      :close-on-click-modal="false"
      :show-close="false"
      draggable
    >
      <div class="transcribing-content">
        <div class="transcribing-icon">
          <svg viewBox="0 0 100 100" class="audio-wave">
            <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" stroke-width="2" opacity="0.3" />
            <circle cx="50" cy="50" r="35" fill="none" stroke="currentColor" stroke-width="2" opacity="0.5" />
            <circle cx="50" cy="50" r="25" fill="none" stroke="currentColor" stroke-width="2" opacity="0.7" />
            <rect x="47" y="5" width="6" height="10" fill="currentColor" class="wave-bar wave-1" />
            <rect x="47" y="85" width="6" height="10" fill="currentColor" class="wave-bar wave-2" />
            <rect x="5" y="47" width="10" height="6" fill="currentColor" class="wave-bar wave-3" />
            <rect x="85" y="47" width="10" height="6" fill="currentColor" class="wave-bar wave-4" />
          </svg>
        </div>
        <div class="transcribing-info">
          <div class="transcribing-file">{{ transcribingFileName }}</div>
          <div class="transcribing-tips">
            <span class="tip-dot"></span>
            <span>{{ transcribingBackground ? '后台转写中，您可以进行其他操作...' : '正在启动转写任务...' }}</span>
          </div>
          <div class="transcribing-time">
            <el-icon class="time-icon"><Clock /></el-icon>
            <span>已耗时：{{ formatDuration(transcribingDuration) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="permissionGuideVisible"
      title="麦克风权限"
      width="400px"
      :close-on-click-modal="false"
      draggable
    >
      <div class="permission-guide-content">
        <div class="permission-icon">
          <svg viewBox="0 0 64 64" class="mic-icon">
            <circle cx="32" cy="32" r="28" fill="none" stroke="currentColor" stroke-width="2" opacity="0.2" />
            <path d="M32 16v8" stroke="currentColor" stroke-width="3" stroke-linecap="round" />
            <path
              d="M24 24v-6a8 8 0 0 1 16 0v6"
              fill="none"
              stroke="currentColor"
              stroke-width="3"
              stroke-linecap="round"
            />
            <ellipse cx="32" cy="44" rx="12" ry="8" fill="currentColor" opacity="0.3" />
            <ellipse cx="32" cy="48" rx="8" ry="5" fill="currentColor" />
          </svg>
        </div>
        <div class="permission-title">需要麦克风权限</div>
        <div class="permission-desc">
          为了进行语音转写，我们需要访问您的麦克风来录制音频。<br />
          录制的音频仅用于转写，不会保存或上传到其他地方。
        </div>
        <div class="permission-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>请在下方弹出的浏览器提示中选择"允许"或"仅本次访问时允许"</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="permissionGuideVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPermission">知道了，开始录音</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Search, Upload, Delete, UploadFilled, Clock, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { useTranscriptionPolling } from '@/utils/useTranscriptionPolling'
import { API } from '@/api/routes'

const { polling: _polling, startPolling, stopPolling } = useTranscriptionPolling()

const userStore = useUserStore()

defineOptions({ name: 'AudioTranscription' })

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const language = ref('')
const selectedIds = ref([])

const uploadDialogVisible = ref(false)
const uploadRef = ref()
const uploading = ref(false)
const uploadLanguage = ref('zh')
const pendingFiles = ref([])
const uploadQueue = ref([])
const currentUploadIndex = ref(0)

const detailDialogVisible = ref(false)
const detailData = ref(null)

const modelSelectDialogVisible = ref(false)
const selectedModel = ref('small')
const transcribingRow = ref(null)
const transcribing = ref(false)

const transcribingDialogVisible = ref(false)
const transcribingFileName = ref('')
const transcribingStartTime = ref(0)
const transcribingDuration = ref(0)
const transcribingBackground = ref(false)

let transcribingTimer = null

const transcribingIds = ref([])

function getStatusType(row) {
  if (row.status === 0) return 'danger'
  if (row.status === 1 && row.fullText) return 'success'
  if (row.status === 2 && row.fullText) return 'warning'
  return 'info'
}

function getStatusLabel(row) {
  if (row.status === 0) return '失败'
  if (row.status === 1 && row.fullText) return '成功'
  if (row.status === 2 && row.fullText) return '转写中...'
  return '待转写'
}

const isRecording = ref(false)
const canStopRecording = ref(false)
const recordingDuration = ref(0)
let recordingTimer = null
let mediaRecorder = null
let audioChunks = []

const permissionGuideVisible = ref(false)

const editingFileName = ref(false)
const editFileNameValue = ref('')

async function fetchData() {
  loading.value = true
  try {
    const res = await request({
      url: API.AUDIO.TRANSCRIPTION.PAGE,
      method: 'get',
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        language: language.value
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch {
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  language.value = ''
  page.value = 1
  fetchData()
}

function handleSelectionChange(val) {
  selectedIds.value = val.map((item) => item.id)
}

function handleUpload() {
  uploadDialogVisible.value = true
  pendingFiles.value = []
  uploadQueue.value = []
  currentUploadIndex.value = 0
}

function validateAudioFile(file) {
  const audioTypes = ['audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/flac', 'audio/aac', 'audio/webm', 'audio/x-m4a']
  const isAudioByType = audioTypes.includes(file.type)
  const fileName = (file.name || '').toLowerCase()
  const isAudioByExt = /\.(mp3|wav|m4a|flac|aac|ogg|webm)$/.test(fileName)
  if (!isAudioByType && !isAudioByExt) {
    ElMessage.error('只能上传音频文件(mp3/wav/m4a/flac/aac/ogg/webm): ' + file.name)
    return false
  }
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过100MB: ' + file.name)
    return false
  }
  return true
}

function beforeUpload(file) {
  return validateAudioFile(file)
}

function handleFileChange(file, fileList) {
  pendingFiles.value = fileList.map((f) => f.raw || f).filter(Boolean)
}

function handleFileRemove(file, fileList) {
  pendingFiles.value = fileList.map((f) => f.raw || f).filter(Boolean)
}

function cancelUpload() {
  uploadDialogVisible.value = false
  pendingFiles.value = []
  uploadQueue.value = []
  currentUploadIndex.value = 0
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

async function submitUpload() {
  if (pendingFiles.value.length === 0) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }
  uploading.value = true
  uploadQueue.value = [...pendingFiles.value]
  currentUploadIndex.value = 0
  let successCount = 0
  let failCount = 0

  try {
    for (let i = 0; i < uploadQueue.value.length; i++) {
      currentUploadIndex.value = i
      const file = uploadQueue.value[i]
      try {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('language', uploadLanguage.value)

        await request({
          url: API.AUDIO.TRANSCRIPTION.UPLOAD_ONLY,
          method: 'post',
          data: formData
        })
        successCount++
      } catch {
        failCount++
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功上传 ${successCount} 个文件${failCount > 0 ? `，失败 ${failCount} 个` : ''}`)
    } else {
      ElMessage.error('上传失败')
    }

    uploadDialogVisible.value = false
    pendingFiles.value = []
    uploadQueue.value = []
    uploadRef.value.clearFiles()
    fetchData()
  } finally {
    uploading.value = false
  }
}

function showModelSelectDialog(row) {
  transcribingRow.value = row
  selectedModel.value = 'small'
  modelSelectDialogVisible.value = true
}

async function handleTranscribeWithModel() {
  const row = transcribingRow.value
  if (!row) return
  modelSelectDialogVisible.value = false
  transcribing.value = true
  transcribingIds.value = [...transcribingIds.value, row.id]
  transcribingFileName.value = row.fileName
  transcribingStartTime.value = Date.now()
  transcribingBackground.value = false
  transcribingDuration.value = 0
  transcribingDialogVisible.value = true

  if (transcribingTimer) {
    clearInterval(transcribingTimer)
  }
  transcribingTimer = setInterval(() => {
    transcribingDuration.value = Math.floor((Date.now() - transcribingStartTime.value) / 1000)
  }, 1000)

  try {
    await request({
      url: API.AUDIO.TRANSCRIPTION.TRANSCRIBE(row.id),
      method: 'post',
      params: { model: selectedModel.value }
    })
    transcribingBackground.value = true
    fetchData()

    startPolling(row.id, API.AUDIO.TRANSCRIPTION.BASE)
      .then((_record) => {
        ElMessage.success('转写成功')
        transcribingIds.value = transcribingIds.value.filter((id) => id !== row.id)
        fetchData()
        if (detailDialogVisible.value && detailData.value && detailData.value.id === row.id) {
          handleView(row)
        }
      })
      .catch((error) => {
        ElMessage.error('转写失败: ' + (error.message || '未知错误'))
        transcribingIds.value = transcribingIds.value.filter((id) => id !== row.id)
      })

    await new Promise((resolve) => setTimeout(resolve, 3000))
  } catch (error) {
    ElMessage.error('转写失败: ' + (error.message || '未知错误'))
  } finally {
    if (transcribingTimer) {
      clearInterval(transcribingTimer)
      transcribingTimer = null
    }
    transcribingDialogVisible.value = false
    transcribing.value = false
    transcribingIds.value = transcribingIds.value.filter((id) => id !== row.id)
  }
}

async function toggleRecording() {
  if (isRecording.value) {
    stopRecording()
  } else {
    permissionGuideVisible.value = true
  }
}

function confirmPermission() {
  permissionGuideVisible.value = false
  requestMicrophone()
}

async function requestMicrophone() {
  try {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      ElMessage.error('当前浏览器不支持麦克风访问，请使用 Chrome/Edge 浏览器')
      return
    }

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    startRecording(stream)
  } catch (error) {
    if (error.name === 'NotFoundError' || error.message?.includes('device')) {
      ElMessage.error('未检测到麦克风设备，请确认麦克风已连接且系统权限已开启')
    } else if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
      ElMessage.error('麦克风权限被拒绝，请在浏览器地址栏左侧点击锁图标，开启麦克风权限')
    } else {
      ElMessage.error('无法访问麦克风: ' + (error.message || '请检查权限设置'))
    }
  }
}

async function startRecording(stream) {
  try {
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data)
      }
    }

    mediaRecorder.onstop = async () => {
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      const fileName = `recording_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.webm`

      stream.getTracks().forEach((track) => track.stop())

      transcribingFileName.value = fileName
      transcribingStartTime.value = Date.now()
      transcribingDuration.value = 0
      transcribingDialogVisible.value = true

      if (transcribingTimer) {
        clearInterval(transcribingTimer)
      }
      transcribingTimer = setInterval(() => {
        transcribingDuration.value = Math.floor((Date.now() - transcribingStartTime.value) / 1000)
      }, 1000)

      try {
        const formData = new FormData()
        formData.append('file', blob, fileName)
        formData.append('language', uploadLanguage.value)

        const res = await request({
          url: API.AUDIO.TRANSCRIPTION.UPLOAD,
          method: 'post',
          data: formData
        })

        const recordId = res.data.id
        await startPolling(recordId, API.AUDIO.TRANSCRIPTION.BASE)

        ElMessage.success('录音转写成功')
        fetchData()
      } catch {
      } finally {
        if (transcribingTimer) {
          clearInterval(transcribingTimer)
          transcribingTimer = null
        }
        transcribingDialogVisible.value = false
      }
    }

    mediaRecorder.start()
    isRecording.value = true
    canStopRecording.value = true
    recordingDuration.value = 0

    recordingTimer = setInterval(() => {
      recordingDuration.value++
    }, 1000)

    ElMessage.success('开始录音...')
  } catch {}
}

function stopRecording() {
  if (mediaRecorder && mediaRecorder.state === 'recording') {
    mediaRecorder.stop()
    isRecording.value = false
    canStopRecording.value = false

    if (recordingTimer) {
      clearInterval(recordingTimer)
      recordingTimer = null
    }

    ElMessage.info(`录音结束，时长：${formatDuration(recordingDuration.value)}`)
  }
}

onUnmounted(() => {
  stopPolling()
  if (transcribingTimer) {
    clearInterval(transcribingTimer)
    transcribingTimer = null
  }
  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
})

function handleUploadExceed() {
  ElMessage.warning('每次最多上传10个文件')
}

function handleView(row) {
  request({
    url: API.AUDIO.TRANSCRIPTION.BY_ID(row.id),
    method: 'get'
  }).then((data) => {
    detailData.value = data.data
    editingFileName.value = false
    detailDialogVisible.value = true
  })
}

function downloadSrtFromDetail() {
  if (!detailData.value) return
  window.open(`/api/audio/transcription/${detailData.value.id}/download-srt`, '_blank')
}

function downloadAssFromDetail() {
  if (!detailData.value) return
  window.open(`/api/audio/transcription/${detailData.value.id}/download-ass`, '_blank')
}

function startEditFileName() {
  editFileNameValue.value = detailData.value.fileName
  editingFileName.value = true
}

function cancelEditFileName() {
  editingFileName.value = false
  editFileNameValue.value = ''
}

async function saveFileName() {
  if (!editFileNameValue.value.trim()) {
    ElMessage.warning('文件名不能为空')
    return
  }
  try {
    await request({
      url: API.AUDIO.TRANSCRIPTION.FILE_NAME(detailData.value.id),
      method: 'put',
      params: { fileName: editFileNameValue.value }
    })
    detailData.value.fileName = editFileNameValue.value.trim()
    editingFileName.value = false
    editFileNameValue.value = ''
    ElMessage.success('文件名修改成功')
    fetchData()
  } catch {
    ElMessage.error('文件名修改失败')
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该转写记录？', '提示', { type: 'warning' })
    .then(() =>
      request({
        url: API.AUDIO.TRANSCRIPTION.BY_ID(row.id),
        method: 'delete'
      })
    )
    .then(() => {
      ElMessage.success('删除成功')
      fetchData()
    })
}

function handleBatchDelete() {
  ElMessageBox.confirm('确定删除选中的转写记录？', '提示', { type: 'warning' })
    .then(() =>
      request({
        url: API.AUDIO.TRANSCRIPTION.BATCH_DELETE(selectedIds.value.join(',')),
        method: 'delete'
      })
    )
    .then(() => {
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      fetchData()
    })
}

function formatTime(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

function formatDuration(seconds) {
  if (seconds < 60) {
    return `${seconds}秒`
  }
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (mins < 60) {
    return `${mins}分${secs}秒`
  }
  const hours = Math.floor(mins / 60)
  const remainingMins = mins % 60
  return `${hours}时${remainingMins}分${secs}秒`
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.detail-content {
  padding: 10px 0;
}

.detail-row {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-label {
  font-weight: 600;
  color: #606266;
  white-space: nowrap;
}

.file-name-edit-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.file-name-input {
  width: 300px;
}

.full-text {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-top: 4px;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}

.segments-section {
  margin-top: 16px;
}

.segments-section h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.segment-item {
  padding: 8px;
  border-bottom: 1px dashed #ebeef5;
}

.segment-time {
  color: #909399;
  font-size: 12px;
  margin-right: 12px;
}

.segment-speaker {
  color: var(--el-color-primary);
  font-weight: 500;
  margin-right: 8px;
}

.segment-text {
  color: #303133;
}

.transcribing-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.transcribing-icon {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  margin-bottom: 20px;
}

.audio-wave {
  width: 100%;
  height: 100%;
  animation: rotate 4s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.wave-bar {
  animation: wave 1.5s ease-in-out infinite;
}

.wave-1 {
  animation-delay: 0s;
}
.wave-2 {
  animation-delay: 0.375s;
}
.wave-3 {
  animation-delay: 0.75s;
}
.wave-4 {
  animation-delay: 1.125s;
}

@keyframes wave {
  0%,
  100% {
    opacity: 0.3;
  }
  50% {
    opacity: 1;
  }
}

.transcribing-info {
  text-align: center;
}

.transcribing-file {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  word-break: break-all;
  max-width: 300px;
}

.transcribing-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.tip-dot {
  width: 8px;
  height: 8px;
  background-color: var(--el-color-primary);
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.6;
  }
}

.transcribing-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.time-icon {
  color: var(--el-color-primary);
}

.model-select-content {
  padding: 10px 0;
}

.model-select-tip {
  margin-bottom: 16px;
  color: #606266;
}

.model-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.model-option {
  display: flex;
  align-items: flex-start;
  padding: 14px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 12px;
}

.model-option:hover {
  border-color: var(--el-color-primary);
}

.model-option.selected {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.option-radio {
  width: 16px;
  height: 16px;
  border: 2px solid #dcdfe6;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 2px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.model-option:hover .option-radio,
.model-option.selected .option-radio {
  border-color: var(--el-color-primary);
}

.radio-inner {
  width: 8px;
  height: 8px;
  background-color: var(--el-color-primary);
  border-radius: 50%;
}

.option-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-name {
  font-weight: 500;
  color: #303133;
}

.model-desc {
  font-size: 12px;
  color: #909399;
}

.permission-guide-content {
  padding: 20px;
  text-align: center;
}

.permission-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  color: var(--el-color-primary);
}

.mic-icon {
  width: 100%;
  height: 100%;
}

.permission-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.permission-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 20px;
  line-height: 1.6;
}

.permission-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}
</style>
