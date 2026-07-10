<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" :placeholder="$t('common.search')" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-select v-model="statusFilter" :placeholder="$t('common.status')" clearable @change="handleSearch">
        <el-option label="运行中" value="RUNNING" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="title" :label="$t('workflow.instance.title')" />
        <el-table-column prop="initiatorName" :label="$t('workflow.instance.initiator')" />
        <el-table-column prop="currentNode" :label="$t('workflow.instance.currentNode')" />
        <el-table-column prop="status" :label="$t('common.status')">
          <template #default="{ row }">
            <el-tag :type="row.status === 'RUNNING' ? 'primary' : row.status === 'COMPLETED' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" :label="$t('workflow.instance.startTime')" width="180" />
        <el-table-column :label="$t('common操作')" width="150">
          <template #default="{ row }">
            <el-popconfirm v-if="row.status === 'RUNNING'" :title="$t('common.confirmDelete')" @confirm="handleCancel(row.id)">
              <template #reference>
                <el-button size="small" type="warning">{{ $t('workflow.instance.cancel') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'WorkflowInstance' })
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
const statusFilter = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/wf/instance/page', method: 'get', params: { keyword: keyword.value, status: statusFilter.value, page: page.value, size: size.value } })
    tableData.value = data.records || []
    total.value = data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { page.value = 1; loadData() }
const handleCancel = async (id) => {
  await request({ url: `/wf/instance/${id}/cancel`, method: 'put' })
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>
