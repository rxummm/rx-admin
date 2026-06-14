<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="searchForm.username" placeholder="用户名" clearable style="width:160px" />
      <el-select v-model="searchForm.exportType" placeholder="导出类型" clearable style="width:130px;margin-left:8px">
        <el-option label="Excel" value="excel" />
        <el-option label="PDF" value="pdf" />
      </el-select>
      <el-date-picker v-model="searchForm.timeRange" type="datetimerange" range-separator="至"
        start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" style="margin-left:8px" />
      <el-button type="primary" @click="handleSearch" style="margin-left:8px">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="exportType" label="导出类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.exportType==='excel'?'success':'warning'" size="small">{{ row.exportType?.toUpperCase() }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="exportTitle" label="导出标题" min-width="160" />
      <el-table-column prop="recordCount" label="记录数" width="80" />
      <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createTime" label="导出时间" width="170" />
    </el-table>
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10,20,50,100]" :total="pagination.total" layout="total,sizes,prev,pager,next" @change="fetchData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getExportLogPageApi } from '@/api/exportLog'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ username: '', exportType: null, timeRange: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

function buildParams() {
  const p = { page: pagination.page, size: pagination.size, username: searchForm.username || undefined, exportType: searchForm.exportType }
  if (searchForm.timeRange && searchForm.timeRange.length === 2) { p.startTime = searchForm.timeRange[0]; p.endTime = searchForm.timeRange[1] }
  return p
}

async function fetchData() {
  loading.value = true
  try { const res = await getExportLogPageApi(buildParams()); tableData.value = res.data.records || []; pagination.total = res.data.total || 0 } 
  catch { ElMessage.error('加载失败') } finally { loading.value = false }
}
function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.username = ''; searchForm.exportType = null; searchForm.timeRange = null; handleSearch() }
onMounted(fetchData)
</script>


<style scoped>
.pagination-wrap {
  margin-top: 12px;
}
</style>
