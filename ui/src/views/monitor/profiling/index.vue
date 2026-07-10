<template>
  <div class="page-container">
    <div class="search-bar">
      <el-date-picker v-model="startDate" type="date" :placeholder="$t('common.startDate')" @change="loadData" />
      <el-button type="primary" @click="loadData">{{ $t('common.search') }}</el-button>
    </div>
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card>
          <template #header><span>{{ $t('monitor.profiling.slowMethods') }}</span></template>
          <el-table :data="slowMethods" border stripe>
            <el-table-column prop="className" :label="$t('monitor.profiling.className')" show-overflow-tooltip />
            <el-table-column prop="methodName" :label="$t('monitor.profiling.methodName')" />
            <el-table-column prop="callCount" :label="$t('monitor.profiling.callCount')" />
            <el-table-column prop="avgTime" :label="$t('monitor.profiling.avgTime')">
              <template #default="{ row }">{{ row.avgTime }}ms</template>
            </el-table-column>
            <el-table-column prop="maxTime" :label="$t('monitor.profiling.maxTime')">
              <template #default="{ row }">{{ row.maxTime }}ms</template>
            </el-table-column>
            <el-table-column prop="totalTime" :label="$t('monitor.profiling.totalTime')">
              <template #default="{ row }">{{ row.totalTime }}ms</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineOptions({ name: 'MonitorProfiling' })
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const startDate = ref('')
const slowMethods = ref([])

const loadData = async () => {
  const { data } = await request({ url: '/monitor/profiling/stats', method: 'get', params: { startDate: startDate.value || new Date(Date.now() - 7 * 86400000).toISOString().slice(0, 10) } })
  slowMethods.value = data.slowMethods || []
}

onMounted(loadData)
</script>
