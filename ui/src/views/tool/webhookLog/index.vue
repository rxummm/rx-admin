<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="Webhook名称" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-select v-model="statusFilter" placeholder="状态" clearable @change="handleSearch">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILED" />
        <el-option label="待处理" value="PENDING" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="webhookId" label="Webhook ID" width="100" />
        <el-table-column prop="event" label="事件" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseCode" label="响应码" width="80" />
        <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
        <el-table-column prop="retryCount" label="重试次数" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </div>
    <div class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolWebhookLog' })
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

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
    const { data } = await request({ url: '/tool/webhook-log/page', method: 'get', params: { keyword: keyword.value, status: statusFilter.value, page: page.value, size: size.value } })
    tableData.value = data?.records || []
    total.value = data?.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { page.value = 1; loadData() }
onMounted(loadData)
</script>
