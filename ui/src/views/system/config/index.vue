<template>
  <div class="page-container">
    <div class="search-bar">
      <div style="flex:1" />
      <el-button type="primary" @click="fetchData">
        <el-icon><Refresh /></el-icon> {{ $t('common.refresh') }}
      </el-button>
    </div>

    <el-tabs v-model="activeGroup" @tab-change="fetchData">
      <el-tab-pane v-for="(configs, group) in groupedConfigs" :key="group" :label="$t('system.config.' + group + 'Settings') || group" :name="group" />
    </el-tabs>

    <div class="table-container">
      <el-table :data="currentConfigs" border stripe v-loading="loading">
        <el-table-column prop="configKey" :label="$t('system.config.configKey')" width="200" />
        <el-table-column :label="$t('system.config.configValue')" min-width="300">
          <template #default="{ row }">
            <template v-if="row.configType === 'boolean'">
              <el-switch :model-value="row.configValue === 'true'" @change="v => updateConfig(row, v ? 'true' : 'false')" />
            </template>
            <template v-else-if="editKey === row.configKey">
              <el-input v-model="editValue" size="small" style="width:200px" />
              <el-button type="primary" size="small" @click="saveConfig(row)">{{ $t('system.config.save') }}</el-button>
              <el-button size="small" @click="editKey = ''">{{ $t('system.config.cancel') }}</el-button>
            </template>
            <template v-else>
              <span>{{ row.configValue }}</span>
              <el-button link type="primary" size="small" @click="startEdit(row)">{{ $t('system.config.edit') }}</el-button>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('system.config.remark')" width="200" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'SystemConfig' })
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()

const loading = ref(false)
const groupedConfigs = ref({})
const activeGroup = ref('system')
const editKey = ref('')
const editValue = ref('')

const currentConfigs = computed(() => groupedConfigs.value[activeGroup.value] || [])

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await request({ url: '/system/config/grouped', method: 'get' })
    groupedConfigs.value = res.data || {}
    if (Object.keys(groupedConfigs.value).length > 0 && !groupedConfigs.value[activeGroup.value]) {
      activeGroup.value = Object.keys(groupedConfigs.value)[0]
    }
  } finally {
    loading.value = false
  }
}

function startEdit(row) {
  editKey.value = row.configKey
  editValue.value = row.configValue
}

async function saveConfig(row) {
  try {
    await request({ url: `/system/config/value/${row.configKey}`, method: 'put', data: { value: editValue.value } })
    ElMessage.success(t('common.saveSuccess'))
    editKey.value = ''
    await fetchData()
  } catch {}
}

async function updateConfig(row, value) {
  try {
    await request({ url: `/system/config/value/${row.configKey}`, method: 'put', data: { value } })
    ElMessage.success(t('common.updateSuccess'))
    await fetchData()
  } catch {}
}
</script>