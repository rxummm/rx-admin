<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="$t('common.search')" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
      <el-button type="success" @click="showAdd">{{ $t('common.add') }}</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" :label="$t('tool.webhook.name')" />
        <el-table-column prop="url" :label="$t('tool.webhook.url')" show-overflow-tooltip />
        <el-table-column prop="events" :label="$t('tool.webhook.events')" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('common.status')">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? $t('common.enabled') : $t('common.disabled') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common操作')" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showEdit(row)">{{ $t('common.edit') }}</el-button>
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
    <div class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('common.edit') : $t('common.add')" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item :label="$t('tool.webhook.name')"><el-input v-model="form.name" /></el-form-item>
        <el-form-item :label="$t('tool.webhook.url')"><el-input v-model="form.url" /></el-form-item>
        <el-form-item :label="$t('tool.webhook.events')"><el-input v-model="form.events" placeholder="如: user.created,user.updated" /></el-form-item>
        <el-form-item :label="$t('tool.webhook.secret')"><el-input v-model="form.secret" /></el-form-item>
        <el-form-item :label="$t('tool.webhook.description')"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolWebhook' })
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/tool/webhook/page', method: 'get', params: { keyword: keyword.value, page: page.value, size: size.value } })
    tableData.value = data.records || []
    total.value = data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { page.value = 1; loadData() }
const showAdd = () => { isEdit.value = false; form.value = {}; dialogVisible.value = true }
const showEdit = (row) => { isEdit.value = true; form.value = { ...row }; dialogVisible.value = true }

const handleSubmit = async () => {
  if (isEdit.value) {
    await request({ url: '/tool/webhook', method: 'put', data: form.value })
  } else {
    await request({ url: '/tool/webhook', method: 'post', data: form.value })
  }
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadData()
}

const handleToggle = async (id) => {
  await request({ url: `/tool/webhook/${id}/toggle`, method: 'put' })
  loadData()
}

const handleDelete = async (id) => {
  await request({ url: `/tool/webhook/${id}`, method: 'delete' })
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>
