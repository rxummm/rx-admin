<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-size: 16px; font-weight: 600">API 调试面板</span>
    </div>
    <div style="display: flex; gap: 12px; flex: 1; min-height: 0">
      <!-- 左侧端点列表 -->
      <div style="width: 300px; display: flex; flex-direction: column">
        <el-input v-model="searchKeyword" placeholder="搜索API路径" size="small" clearable style="margin-bottom: 8px" />
        <div style="flex: 1; overflow-y: auto; border: 1px solid var(--border-light, #ebeef5); border-radius: 4px">
          <div
            v-for="item in filteredEndpoints"
            :key="item.paths?.[0] + item.methods?.[0]"
            :style="{
              padding: '8px 12px',
              cursor: 'pointer',
              borderBottom: '1px solid var(--border-light, #ebeef5)',
              background:
                selected?.paths?.[0] === item.paths?.[0] && selected?.methods?.[0] === item.methods?.[0]
                  ? 'var(--bg-active, #fef9ee)'
                  : ''
            }"
            @click="selectEndpoint(item)"
          >
            <div style="display: flex; align-items: center; gap: 6px">
              <el-tag :type="methodColor(item.methods?.[0])" size="small" style="min-width: 48px; text-align: center">{{
                item.methods?.[0]
              }}</el-tag>
              <span style="font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{
                item.paths?.[0]
              }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧调试区 -->
      <div style="flex: 1; display: flex; flex-direction: column; gap: 8px">
        <div style="display: flex; gap: 8px; align-items: center">
          <el-select v-model="debugMethod" style="width: 100px">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
          <el-input v-model="debugUrl" placeholder="/api/xxx" style="flex: 1" />
          <el-button type="primary" @click="doRequest" :loading="requesting">发送</el-button>
        </div>

        <el-tabs model-value="params" type="border-card" style="flex: 1; display: flex; flex-direction: column">
          <el-tab-pane label="Query参数" name="params">
            <el-table :data="queryParams" border>
              <el-table-column label="Key" width="180">
                <template #default="{ row }"><el-input v-model="row.key" size="small" placeholder="参数名" /></template>
              </el-table-column>
              <el-table-column label="Value">
                <template #default="{ row }"
                  ><el-input v-model="row.value" size="small" placeholder="参数值"
                /></template>
              </el-table-column>
              <el-table-column width="60">
                <template #default="{ $index }"
                  ><el-button link type="danger" @click="queryParams.splice($index, 1)">删除</el-button></template
                >
              </el-table-column>
            </el-table>
            <el-button link type="primary" @click="queryParams.push({ key: '', value: '' })" style="margin-top: 4px"
              >+ 添加参数</el-button
            >
          </el-tab-pane>
        </el-tabs>

        <div v-if="responseStatus !== null" style="background: #1e1e1e; border-radius: 4px; padding: 12px">
          <div style="color: #4ec9b0; font-size: 12px; margin-bottom: 4px">
            Status: {{ responseStatus }} | Time: {{ responseTime }}ms
          </div>
          <pre
            style="
              color: #d4d4d4;
              font-size: 12px;
              margin: 0;
              white-space: pre-wrap;
              word-break: break-all;
              max-height: 200px;
              overflow-y: auto;
            "
            >{{ responseBody }}</pre
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolApiDebug' })
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEndpointsApi } from '@/api/apiDebug'
import request from '@/utils/request'

const endpoints = ref([])
const searchKeyword = ref('')
const selected = ref(null)
const debugMethod = ref('GET')
const debugUrl = ref('')
const queryParams = ref([{ key: '', value: '' }])
const requesting = ref(false)
const responseStatus = ref(null)
const responseTime = ref(0)
const responseBody = ref('')

const filteredEndpoints = computed(() => {
  if (!searchKeyword.value) return endpoints.value
  const kw = searchKeyword.value.toLowerCase()
  return endpoints.value.filter((e) => e.paths?.[0]?.toLowerCase().includes(kw))
})

const methodColor = (m) =>
  m === 'GET' ? 'success' : m === 'POST' ? 'primary' : m === 'PUT' ? 'warning' : m === 'DELETE' ? 'danger' : 'info'

const selectEndpoint = (item) => {
  selected.value = item
  debugMethod.value = item.methods?.[0] || 'GET'
  debugUrl.value = item.paths?.[0] || ''
}

const doRequest = async () => {
  if (!debugUrl.value) {
    ElMessage.warning('请输入请求路径')
    return
  }
  requesting.value = true
  responseStatus.value = null
  const start = Date.now()
  try {
    const params = {}
    queryParams.value.filter((p) => p.key).forEach((p) => (params[p.key] = p.value))
    let res
    switch (debugMethod.value) {
      case 'POST':
        res = await request.post(debugUrl.value, params)
        break
      case 'PUT':
        res = await request.put(debugUrl.value, params)
        break
      case 'DELETE':
        res = await request.delete(debugUrl.value, { params })
        break
      default:
        res = await request.get(debugUrl.value, { params })
        break
    }
    responseStatus.value = 200
    responseBody.value = JSON.stringify(res, null, 2)
  } catch (e) {
    responseStatus.value = e.response?.status || 500
    responseBody.value = JSON.stringify(e.response?.data || e.message, null, 2)
  } finally {
    responseTime.value = Date.now() - start
    requesting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getEndpointsApi()
    endpoints.value = res.data || []
  } catch {
    /* */
  }
})
</script>
