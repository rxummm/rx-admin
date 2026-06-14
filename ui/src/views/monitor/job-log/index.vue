<template>
  <div class="page-container">
    <div class="search-bar">
      <el-select v-model="searchForm.status" placeholder="执行状态" clearable style="width:130px">
        <el-option label="成功" :value="1" />
        <el-option label="失败" :value="0" />
      </el-select>
      <el-date-picker v-model="searchForm.timeRange" type="datetimerange" range-separator="至"
        start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" style="margin-left:8px" />
      <el-button type="primary" @click="handleSearch" style="margin-left:8px">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div style="flex:1" />
      <el-button type="danger" :disabled="selectedIds.length===0" @click="handleBatchDelete">批量删除</el-button>
    </div>
    <el-table :data="tableData" @selection-change="v=>selectedIds=v.map(r=>r.id)" v-loading="loading" stripe border>
      <el-table-column type="selection" width="45" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="jobName" label="任务名称" width="150" />
      <el-table-column prop="beanName" label="Bean" width="150" />
      <el-table-column prop="methodName" label="方法" width="120" />
      <el-table-column prop="durationMs" label="耗时(ms)" width="100">
        <template #default="{ row }"><span :style="{color:row.durationMs>5000?'#f56c6c':''}">{{ row.durationMs }}ms</span></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'成功':'失败' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="resultMsg" label="结果" min-width="160" show-overflow-tooltip />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column label="操作" width="70" fixed="right">
        <template #default="{ row }"><el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10,20,50,100]" :total="pagination.total" layout="total,sizes,prev,pager,next" @change="fetchData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobLogPageApi, deleteJobLogApi, deleteJobLogBatchApi } from '@/api/jobLog'

const loading = ref(false); const tableData = ref([]); const selectedIds = ref([])
const searchForm = reactive({ status: null, timeRange: null })
const pagination = reactive({ page:1, size:10, total:0 })

function buildParams() {
  const p = { page:pagination.page, size:pagination.size, status:searchForm.status }
  if (searchForm.timeRange && searchForm.timeRange.length===2) { p.startTime=searchForm.timeRange[0]; p.endTime=searchForm.timeRange[1] }
  return p
}
async function fetchData() {
  loading.value = true
  try { const res = await getJobLogPageApi(buildParams()); tableData.value = res.data.records||[]; pagination.total = res.data.total||0 }
  catch { ElMessage.error('加载失败') } finally { loading.value = false }
}
function handleSearch() { pagination.page=1; fetchData() }
function handleReset() { searchForm.status=null; searchForm.timeRange=null; handleSearch() }
async function handleDelete(id) { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteJobLogApi(id); ElMessage.success('删除成功'); fetchData() }
async function handleBatchDelete() { await ElMessageBox.confirm(`确认删除${selectedIds.value.length}条？`,'提示',{type:'warning'}); await deleteJobLogBatchApi(selectedIds.value); ElMessage.success('删除成功'); fetchData() }
onMounted(fetchData)
</script>


<style scoped>
.pagination-wrap {
  margin-top: 12px;
}
</style>
