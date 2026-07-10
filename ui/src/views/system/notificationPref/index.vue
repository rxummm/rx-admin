<template>
  <div class="page-container">
    <el-card>
      <template #header><span>通知偏好设置</span></template>
      <el-table :data="preferences" v-loading="loading" border stripe>
        <el-table-column prop="eventType" label="事件类型" width="200" />
        <el-table-column prop="emailEnabled" label="邮件通知" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.emailEnabled" :active-value="1" :inactive-value="0" @change="savePref(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="websocketEnabled" label="WebSocket通知" width="120">
          <template #default="{ row }">
            <el-switch v-model="row.websocketEnabled" :active-value="1" :inactive-value="0" @change="savePref(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="browserEnabled" label="浏览器通知" width="120">
          <template #default="{ row }">
            <el-switch v-model="row.browserEnabled" :active-value="1" :inactive-value="0" @change="savePref(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="quietStart" label="免打扰开始" width="100" />
        <el-table-column prop="quietEnd" label="免打扰结束" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
defineOptions({ name: 'SystemNotificationPref' })
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const preferences = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/system/notification-pref/list', method: 'get' })
    preferences.value = data || []
  } finally { loading.value = false }
}

const savePref = async (row) => {
  await request({ url: '/system/notification-pref', method: 'put', data: row })
  ElMessage.success('保存成功')
}

onMounted(loadData)
</script>
