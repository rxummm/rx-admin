<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-size: 16px; font-weight: 600;">代码生成器</span>
    </div>

    <el-steps :active="step" align-center finish-status="success" style="margin: 16px 0 24px;">
      <el-step title="选择表" />
      <el-step title="配置" />
      <el-step title="预览生成" />
    </el-steps>

    <!-- Step 1: 选择表 -->
    <div v-if="step === 0" style="display:flex; gap:16px; flex:1; min-height:0;">
      <div style="width:35%;">
        <el-card shadow="hover" header="数据库表" :body-style="{ padding: '8px' }">
          <el-input v-model="tableSearch" placeholder="搜索表名" size="small" clearable style="margin-bottom:8px;" />
          <div style="max-height: 400px; overflow-y: auto;">
            <div v-for="t in filteredTables" :key="t.tableName"
              :style="{ padding: '8px 12px', cursor: 'pointer', borderRadius: '4px', background: selectedTable?.tableName === t.tableName ? 'var(--bg-active, #fef9ee)' : '' }"
              @click="selectTable(t)">
              <div style="font-weight:500; font-size:13px;">{{ t.tableName }}</div>
              <div style="color:var(--text-secondary,#909399);font-size:12px;">{{ t.tableComment || '无注释' }}</div>
            </div>
          </div>
        </el-card>
      </div>
      <div style="width:65%;">
        <el-card shadow="hover" header="字段列表" :body-style="{ padding: '0' }">
          <el-table :data="selectedColumns" max-height="400" border stripe size="small">
            <el-table-column type="index" width="50" />
            <el-table-column prop="columnName" label="字段名" width="160" />
            <el-table-column prop="dataType" label="类型" width="100" />
            <el-table-column prop="columnComment" label="注释" min-width="150" show-overflow-tooltip />
            <el-table-column prop="isNullable" label="可为空" width="80" />
            <el-table-column prop="columnKey" label="键" width="80" />
          </el-table>
        </el-card>
      </div>
    </div>

    <!-- Step 2: 配置 -->
    <div v-if="step === 1" style="max-width:600px; margin:0 auto;">
      <el-form :model="config" label-width="120px">
        <el-form-item label="基础包名">
          <el-input v-model="config.packageName" placeholder="com.rx.admin" />
        </el-form-item>
        <el-form-item label="模块名">
          <el-input v-model="config.moduleName" placeholder="如 system" />
          <div style="color:var(--text-secondary,#909399);font-size:12px;">URL前缀: /api/{模块名}/...</div>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="config.author" placeholder="可选" />
        </el-form-item>
        <el-form-item label="父菜单ID">
          <el-input v-model.number="config.menuParentId" placeholder="在哪个菜单下添加，0=顶级" style="width:200px;" />
        </el-form-item>
        <el-form-item label="实体名" v-if="selectedTable">
          <el-tag>{{ entityName }}</el-tag>
        </el-form-item>
      </el-form>
    </div>

    <!-- Step 3: 预览生成 -->
    <div v-if="step === 2" style="flex:1; min-height:0; display:flex; flex-direction:column;">
      <div style="margin-bottom:12px;">
        <el-button type="primary" :loading="generating" @click="doGenerate">生成代码</el-button>
      </div>
      <el-tabs v-model="previewTab" type="card" style="flex:1; display:flex; flex-direction:column;">
        <el-tab-pane v-for="f in previewFiles" :key="f.name" :label="f.name" :name="f.name">
          <el-input :model-value="f.content" type="textarea" readonly
            style="font-family: monospace; font-size: 12px;"
            :rows="20" resize="vertical" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <div style="display:flex; justify-content:center; gap:12px; margin-top:16px;">
      <el-button v-if="step > 0" @click="step--">上一步</el-button>
      <el-button v-if="step < 2" type="primary" @click="nextStep" :disabled="!canNext">下一步</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getTableListApi, getTableColumnsApi, previewCodeApi, generateCodeApi } from '@/api/gen'
import { DEFAULT_GENERATOR_CONFIG, TABLE_PREFIX_REGEX } from '@/config/generator'

const step = ref(0); const tables = ref([]); const tableSearch = ref('')
const selectedTable = ref(null); const selectedColumns = ref([])
const config = ref({ ...DEFAULT_GENERATOR_CONFIG })
const previewFiles = ref([]); const previewTab = ref(''); const generating = ref(false)

const filteredTables = computed(() => {
  if (!tableSearch.value) return tables.value
  return tables.value.filter(t => t.tableName.toLowerCase().includes(tableSearch.value.toLowerCase()))
})

const entityName = computed(() => {
  if (!selectedTable.value) return ''
  const name = selectedTable.value.tableName.replace(TABLE_PREFIX_REGEX, '')
  return name.replace(/_([a-z])/g, (_, c) => c.toUpperCase()).replace(/^./, c => c.toUpperCase())
})

const canNext = computed(() => {
  if (step.value === 0) return selectedTable.value !== null
  if (step.value === 1) return config.value.moduleName.trim() !== ''
  return true
})

const selectTable = async (t) => {
  selectedTable.value = t
  try {
    const res = await getTableColumnsApi(t.tableName)
    selectedColumns.value = res.data || []
  } catch (e) { selectedColumns.value = [] }
}

const nextStep = async () => {
  if (step.value === 1) {
    generatePreview()
  }
  step.value++
}

const generatePreview = async () => {
  try {
    const res = await previewCodeApi({
      tableName: selectedTable.value.tableName,
      ...config.value
    })
    previewFiles.value = res.data?.files || []
    previewTab.value = previewFiles.value[0]?.name || ''
  } catch (e) { ElMessage.error('预览失败') }
}

const doGenerate = async () => {
  generating.value = true
  try {
    const res = await generateCodeApi({
      tableName: selectedTable.value.tableName,
      ...config.value
    })
    ElMessage.success('代码生成成功! 请检查项目文件')
  } catch (e) { ElMessage.error('生成失败') }
  finally { generating.value = false }
}

;(async () => {
  try { const res = await getTableListApi(); tables.value = res.data || [] } catch (e) { /* */ }
})()
</script>