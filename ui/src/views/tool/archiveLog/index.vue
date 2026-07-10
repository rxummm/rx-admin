<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="primary" @click="loadData">刷新</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="tableName" label="表名" />
        <el-table-column prop="archivedCount" label="归档条数" width="100" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
      </el-table>
    </div>
    <div class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolArchiveLog' })
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/tool/archive-log/page', method: 'get', params: { page: page.value, size: size.value } })
    tableData.value = data?.records || []
    total.value = data?.total || 0
  } finally { loading.value = false }
}

onMounted(loadData)
</script>
