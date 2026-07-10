<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="tableName" placeholder="表名" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-input v-model="recordId" placeholder="记录ID" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="tableName" :label="$t('monitor.dataVersion.tableName')" />
        <el-table-column prop="recordId" :label="$t('monitor.dataVersion.recordId')" width="100" />
        <el-table-column prop="version" :label="$t('monitor.dataVersion.version')" width="80" />
        <el-table-column prop="operation" :label="$t('monitor.dataVersion.operation')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.operation === 'INSERT' ? 'success' : row.operation === 'UPDATE' ? 'warning' : 'danger'">{{ row.operation }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" :label="$t('monitor.dataVersion.operator')" />
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="180" />
      </el-table>
    </div>
    <div class="page-pagination">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="loadData" />
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'MonitorDataVersion' })
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableName = ref('')
const recordId = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/monitor/data-version/page', method: 'get', params: { tableName: tableName.value, recordId: recordId.value, page: page.value, size: size.value } })
    tableData.value = data.records || []
    total.value = data.total || 0
  } finally { loading.value = false }
}

const handleSearch = () => { page.value = 1; loadData() }
onMounted(loadData)
</script>
