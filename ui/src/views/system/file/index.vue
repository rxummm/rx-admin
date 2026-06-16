<template>
  <div class="page-container">
    <div class="search-bar">
      <el-upload :http-request="handleUpload" :show-file-list="false" accept="*">
        <el-button type="primary">
          <el-icon><Upload /></el-icon> {{ $t('file.upload') }}
        </el-button>
      </el-upload>
      <el-select v-model="categoryFilter" :placeholder="$t('file.category')" clearable style="width: 140px" @change="fetchData">
        <el-option :label="$t('common.all')" value="" />
        <el-option v-for="cat in categories" :key="cat" :label="$t('file.categories.' + cat)" :value="cat" />
      </el-select>
      <el-input v-model="keyword" :placeholder="$t('file.fileName')" clearable style="width: 200px" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <div style="flex: 1" />
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon> {{ $t('file.batchDelete') }}({{ selectedIds.length }})
      </el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="originalName" :label="$t('file.originalName')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="size" :label="$t('file.size')" width="100">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="mimeType" :label="$t('file.fileType')" width="100" />
        <el-table-column prop="category" :label="$t('file.category')" width="100" />
        <el-table-column prop="createTime" :label="$t('file.uploadTime')" width="170" />
        <el-table-column :label="$t('common.actions')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDownload(row)">{{ $t('file.download') }}</el-button>
            <el-popconfirm :title="$t('file.deleteConfirm')" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" class="page-pagination" @size-change="fetchData" @current-change="fetchData" />
  </div>
</template>

<script setup>
defineOptions({ name: 'SystemFile' })
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getFilePageApi, uploadFileApi, deleteFileApi, deleteFileBatchApi } from '@/api/file'
import { API } from '@/api/routes'

const { t } = useI18n()

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const categoryFilter = ref('')
const selectedIds = ref([])
const categories = ref(['image', 'document', 'video', 'audio', 'other'])

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, keyword: keyword.value }
    if (categoryFilter.value) params.category = categoryFilter.value
    const res = await getFilePageApi(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function resetSearch() {
  keyword.value = ''
  categoryFilter.value = ''
  page.value = 1
  fetchData()
}

async function handleUpload(options) {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    if (categoryFilter.value) formData.append('category', categoryFilter.value)
    await uploadFileApi(formData)
    ElMessage.success(t('file.uploadSuccess'))
    fetchData()
  } catch {}
}

async function handleDownload(row) {
  window.open(API.SYS.FILE.DOWNLOAD(row.id), '_blank')
}

async function handleDelete(id) {
  try {
    await deleteFileApi(id)
    ElMessage.success(t('file.deleteSuccess'))
    fetchData()
  } catch {}
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleBatchDelete() {
  try {
    await deleteFileBatchApi(selectedIds.value)
    ElMessage.success(t('common.batchDeleteSuccess'))
    selectedIds.value = []
    fetchData()
  } catch {}
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / 1048576).toFixed(1) + 'MB'
}

onMounted(() => fetchData())
</script>


<style scoped>
.page-pagination {
  margin-top: 12px;
}
</style>