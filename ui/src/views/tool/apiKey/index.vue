<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="showGenerate">{{ $t('tool.apiKey.generate') }}</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" :label="$t('tool.apiKey.name')" />
        <el-table-column prop="apiKey" :label="$t('tool.apiKey.key')" show-overflow-tooltip />
        <el-table-column prop="rateLimit" :label="$t('tool.apiKey.rateLimit')" />
        <el-table-column prop="useCount" :label="$t('tool.apiKey.useCount')" />
        <el-table-column prop="status" :label="$t('common.status')">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? $t('common.enabled') : $t('common.disabled') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="180" />
        <el-table-column :label="$t('common操作')" width="200">
          <template #default="{ row }">
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggle(row.id)">{{ row.status === 1 ? $t('common.disable') : $t('common.enable') }}</el-button>
            <el-popconfirm :title="$t('common.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="$t('tool.apiKey.generate')" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item :label="$t('tool.apiKey.name')"><el-input v-model="form.name" /></el-form-item>
        <el-form-item :label="$t('tool.apiKey.permissions')"><el-input v-model="form.permissions" placeholder="如: read,write" /></el-form-item>
        <el-form-item :label="$t('tool.apiKey.rateLimit')"><el-input-number v-model="form.rateLimit" :min="1" :max="10000" /></el-form-item>
        <el-form-item :label="$t('tool.apiKey.description')"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleGenerate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="keyDialogVisible" :title="$t('tool.apiKey.generated')" width="500px">
      <el-alert :title="$t('tool.apiKey.saveWarning')" type="warning" show-icon :closable="false" style="margin-bottom: 16px" />
      <el-form label-width="100px">
        <el-form-item :label="$t('tool.apiKey.key')"><el-input :model-value="generatedKey.apiKey" readonly /></el-form-item>
        <el-form-item :label="$t('tool.apiKey.secret')"><el-input :model-value="generatedKey.apiSecret" readonly /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="keyDialogVisible = false">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolApiKey' })
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const keyDialogVisible = ref(false)
const generatedKey = ref({})
const form = ref({ name: '', permissions: '', rateLimit: 100, description: '' })

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/tool/api-key/list', method: 'get' })
    tableData.value = data || []
  } finally { loading.value = false }
}

const showGenerate = () => { form.value = { name: '', permissions: '', rateLimit: 100, description: '' }; dialogVisible.value = true }

const handleGenerate = async () => {
  const { data } = await request({ url: '/tool/api-key', method: 'post', data: form.value })
  generatedKey.value = data
  dialogVisible.value = false
  keyDialogVisible.value = true
  loadData()
}

const handleToggle = async (id) => {
  await request({ url: `/tool/api-key/${id}/toggle`, method: 'put' })
  loadData()
}

const handleDelete = async (id) => {
  await request({ url: `/tool/api-key/${id}`, method: 'delete' })
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>
