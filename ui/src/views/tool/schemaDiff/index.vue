<template>
  <div class="page-container">
    <div class="search-bar">
      <el-select v-model="table1" :placeholder="$t('tool.schemaDiff.selectTable')" filterable style="width: 200px">
        <el-option v-for="t in tables" :key="t" :label="t" :value="t" />
      </el-select>
      <span style="margin: 0 12px">vs</span>
      <el-select v-model="table2" :placeholder="$t('tool.schemaDiff.selectTable')" filterable style="width: 200px">
        <el-option v-for="t in tables" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" @click="compare" :disabled="!table1 || !table2">{{ $t('tool.schemaDiff.compare') }}</el-button>
    </div>
    <el-row :gutter="16" v-if="diffResult">
      <el-col :span="8">
        <el-card>
          <template #header><span style="color: var(--color-primary)">仅在 {{ diffResult.table1 }} 中</span></template>
          <el-tag v-for="col in diffResult.onlyInTable1" :key="col" style="margin: 4px">{{ col }}</el-tag>
          <el-empty v-if="!diffResult.onlyInTable1?.length" :description="$t('tool.schemaDiff.same')" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span style="color: var(--color-success)">{{ $t('tool.schemaDiff.commonColumns') }}</span></template>
          <el-tag v-for="col in diffResult.commonColumns" :key="col" type="success" style="margin: 4px">{{ col }}</el-tag>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span style="color: var(--color-danger)">仅在 {{ diffResult.table2 }} 中</span></template>
          <el-tag v-for="col in diffResult.onlyInTable2" :key="col" type="danger" style="margin: 4px">{{ col }}</el-tag>
          <el-empty v-if="!diffResult.onlyInTable2?.length" :description="$t('tool.schemaDiff.same')" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolSchemaDiff' })
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const tables = ref([])
const table1 = ref('')
const table2 = ref('')
const diffResult = ref(null)

const loadTables = async () => {
  const { data } = await request({ url: '/tool/schema-diff/tables', method: 'get' })
  tables.value = data || []
}

const compare = async () => {
  const { data } = await request({ url: '/tool/schema-diff/compare', method: 'get', params: { table1: table1.value, table2: table2.value } })
  diffResult.value = data
}

onMounted(loadTables)
</script>
