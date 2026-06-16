<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-size: 16px; font-weight: 600;">批量数据导入</span>
    </div>

    <el-steps :active="step" align-center finish-status="success" style="margin: 16px 0 24px;">
      <el-step title="上传文件" />
      <el-step title="预览数据" />
      <el-step title="执行导入" />
    </el-steps>

    <!-- Step 1 -->
    <div v-if="step === 0" style="max-width:600px; margin:24px auto; text-align:center;">
      <el-form label-width="100px">
        <el-form-item label="目标表">
          <el-select v-model="targetTable" placeholder="选择导入目标表" style="width:300px;" filterable>
            <el-option v-for="t in tableList" :key="t.tableName" :label="`${t.tableName} - ${t.tableComment || ''}`" :value="t.tableName" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-upload drag :auto-upload="false" :limit="1" :on-change="handleFileChange" accept=".xlsx,.xls,.csv"
        style="margin-top:24px;">
        <el-icon :size="48" style="color:var(--text-secondary,#c0c4cc);"><UploadFilled /></el-icon>
        <div style="margin-top:8px;color:var(--text-regular,#606266);">拖拽或点击上传 Excel/CSV 文件</div>
        <template #tip><div style="margin-top:8px;color:var(--text-secondary,#909399);font-size:12px;">支持 .xlsx .xls .csv 格式</div></template>
      </el-upload>
    </div>

    <!-- Step 2 -->
    <div v-if="step === 1" style="flex:1; min-height:0; display:flex; flex-direction:column;">
      <div style="margin-bottom:12px; display:flex; gap:16px; align-items:center;">
        <el-tag>总行数: {{ analyzeResult.totalRows }}</el-tag>
        <el-tag type="success">有效行: {{ analyzeResult.validRows }}</el-tag>
        <el-tag v-if="analyzeResult.errorRows?.length" type="danger">错误行: {{ analyzeResult.errorRows.length }}</el-tag>
        <el-radio-group v-model="importMode" size="small" style="margin-left:auto;">
          <el-radio-button value="INSERT">仅新增</el-radio-button>
          <el-radio-button value="UPDATE">新增+更新</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="analyzeResult.rows || []" border stripe max-height="400" size="small">
        <el-table-column type="index" width="50" label="#" />
        <el-table-column v-for="(col, i) in (analyzeResult.columns || [])" :key="i" :prop="String(i)" :label="col" min-width="120" show-overflow-tooltip />
      </el-table>
    </div>

    <!-- Step 3 -->
    <div v-if="step === 2" style="max-width:500px; margin:24px auto;">
      <el-progress :percentage="importProgress" :status="importProgress === 100 ? 'success' : ''" :stroke-width="20" />
      <div style="text-align:center; margin-top:16px; color:var(--text-secondary,#909399);">{{ importProgressText }}</div>
      <el-result v-if="importDone" icon="success" title="导入完成" :sub-title="`成功 ${importResult.success} 行`" />
    </div>

    <div style="display:flex; justify-content:center; gap:12px; margin-top:16px;">
      <el-button v-if="step > 0" @click="step--" :disabled="step === 2">上一步</el-button>
      <el-button v-if="step === 0" type="primary" @click="doAnalyze" :disabled="!canAnalyze" :loading="analyzing">下一步</el-button>
      <el-button v-if="step === 1" type="primary" @click="doImport" :loading="importing">开始导入</el-button>
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolImportData' })
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getTableListApi } from '@/api/gen'
import { analyzeFileApi, executeImportApi } from '@/api/importData'

const step = ref(0); const tableList = ref([]); const targetTable = ref('')
const uploadedFile = ref(null); const analyzing = ref(false)
const analyzeResult = ref({ columns: [], rows: [], totalRows: 0, validRows: 0, errorRows: [] })
const importMode = ref('INSERT'); const importing = ref(false)
const importProgress = ref(0); const importDone = ref(false)
const importResult = ref({ success: 0, fail: 0, errors: [] })

const canAnalyze = computed(() => targetTable.value && uploadedFile.value)
const importProgressText = computed(() => importProgress.value === 100 ? '完成' : '导入中...')

const handleFileChange = (file) => { uploadedFile.value = file.raw }

const doAnalyze = async () => {
  analyzing.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadedFile.value)
    fd.append('tableName', targetTable.value)
    const res = await analyzeFileApi(fd)
    analyzeResult.value = res.data || {}
    step.value = 1
  } catch (e) { ElMessage.error('解析失败') }
  finally { analyzing.value = false }
}

const doImport = async () => {
  importing.value = true; importProgress.value = 0
  try {
    const res = await executeImportApi({ tableName: targetTable.value, rows: analyzeResult.value.rows, importMode: importMode.value })
    importResult.value = res.data || {}
    importProgress.value = 100; importDone.value = true
    step.value = 2
  } catch (e) { ElMessage.error('导入失败') }
  finally { importing.value = false }
}

;(async () => {
  try { const res = await getTableListApi(); tableList.value = res.data || [] } catch (e) { /* */ }
})()
</script>
