<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="primary" @click="loadData">{{ $t('common.refresh') }}</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="instanceTitle" :label="$t('workflow.task.instanceTitle')" />
        <el-table-column prop="nodeName" :label="$t('workflow.task.nodeName')" />
        <el-table-column prop="assigneeName" :label="$t('workflow.task.assignee')" />
        <el-table-column prop="status" :label="$t('common.status')">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'COMPLETED' ? 'success' : 'danger'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="180" />
        <el-table-column :label="$t('common操作')" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="success" @click="handleApprove(row.id, 'approve')">{{ $t('workflow.task.approve') }}</el-button>
              <el-button size="small" type="danger" @click="handleApprove(row.id, 'reject')">{{ $t('workflow.task.reject') }}</el-button>
            </template>
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
defineOptions({ name: 'WorkflowTask' })
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

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/wf/task/my', method: 'get', params: { page: page.value, size: size.value } })
    tableData.value = data.records || []
    total.value = data.total || 0
  } finally { loading.value = false }
}

const handleApprove = async (taskId, action) => {
  await request({ url: '/wf/task/approve', method: 'put', data: { taskId, action } })
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>
