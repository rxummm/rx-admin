<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card header="数据库表" shadow="hover">
          <el-input v-model="tableSearch" placeholder="搜索表名" size="small" clearable style="margin-bottom:8px" />
          <div style="max-height:500px;overflow-y:auto">
            <div v-for="t in filteredTables" :key="t.name" 
              :style="{padding:'6px 8px',cursor:'pointer',borderRadius:'4px',background: selectedTable===t.name? 'var(--bg-active, #fef9ee)' : '',marginBottom:'2px'}"
              @click="selectTable(t.name)">
              <span style="font-size:13px">{{ t.name }}</span>
              <span v-if="t.comment" style="color:var(--text-secondary,#909399);font-size:11px;margin-left:4px">{{ t.comment }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:600">SQL 控制台（只读）</span>
            <el-tag type="warning" size="small" style="margin-left:8px">仅SELECT/SHOW/DESCRIBE</el-tag>
          </template>
          <el-input v-model="sql" type="textarea" :rows="4" placeholder="SELECT * FROM sys_user LIMIT 10" style="margin-bottom:8px;font-family:monospace" />
          <el-button type="primary" @click="executeSql" :loading="execLoading">执行 (只读)</el-button>
          <span v-if="execResult" style="margin-left:12px;color:var(--text-secondary,#909399)">{{ execResult.rowCount ?? execResult.affected }} 条 | {{ execResult.elapsed }}ms</span>
          <span v-if="execResult?.truncated" style="color:var(--color-warning,#e6a23c);margin-left:8px">结果已截断（最多1000行）</span>
          
          <el-table v-if="execResult?.type==='query' && execResult.columns" :data="execResult.rows" border stripe size="small" style="margin-top:12px;max-height:400px;overflow:auto">
            <el-table-column v-for="col in execResult.columns" :key="col" :prop="col" :label="col" min-width="120" show-overflow-tooltip />
          </el-table>
          <div v-if="execError" style="color:var(--color-danger,#f56c6c);margin-top:8px;font-family:monospace;white-space:pre-wrap">{{ execError }}</div>
        </el-card>

        <el-card v-if="tableColumns" header="表结构" shadow="hover" style="margin-top:16px">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="表名">{{ tableName }}</el-descriptions-item>
            <el-descriptions-item label="主键">{{ tableColumns.primaryKeys?.join(', ') }}</el-descriptions-item>
          </el-descriptions>
          <el-table :data="tableColumns.columns" border stripe size="small" style="margin-top:8px">
            <el-table-column prop="name" label="字段名" width="160" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="size" label="长度" width="70" />
            <el-table-column prop="nullable" label="可空" width="60">
              <template #default="{row}">{{ row.nullable?'是':'否' }}</template>
            </el-table-column>
            <el-table-column prop="defaultValue" label="默认值" width="120" />
            <el-table-column prop="comment" label="注释" min-width="160" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card header="连接池状态 (HikariCP)" shadow="hover" style="margin-top:16px">
      <el-row :gutter="16" v-if="poolStatus">
        <el-col :span="4"><div class="stat-item"><span class="stat-label">活跃</span><span class="stat-value" style="color:var(--color-success,#67c23a)">{{ poolStatus.activeConnections }}</span></div></el-col>
        <el-col :span="4"><div class="stat-item"><span class="stat-label">空闲</span><span class="stat-value" style="color:var(--text-secondary,#909399)">{{ poolStatus.idleConnections }}</span></div></el-col>
        <el-col :span="4"><div class="stat-item"><span class="stat-label">总数</span><span class="stat-value">{{ poolStatus.totalConnections }}</span></div></el-col>
        <el-col :span="4"><div class="stat-item"><span class="stat-label">等待中</span><span class="stat-value" :style="{color:poolStatus.threadsAwaitingConnection>0? 'var(--color-danger,#f56c6c)':''}">{{ poolStatus.threadsAwaitingConnection }}</span></div></el-col>
        <el-col :span="4"><div class="stat-item"><span class="stat-label">最大连接</span><span class="stat-value">{{ poolStatus.maximumPoolSize }}</span></div></el-col>
      </el-row>
      <el-button size="small" @click="fetchPoolStatus" style="margin-top:8px">刷新</el-button>
    </el-card>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolDbConsole' })
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { executeSqlApi, getTablesApi, getTableColumnsApi, getPoolStatusApi } from '@/api/dbTool'

const sql = ref('SELECT * FROM sys_user LIMIT 10')
const execLoading = ref(false); const execResult = ref(null); const execError = ref('')
const tables = ref([]); const tableSearch = ref(''); const selectedTable = ref(null)
const tableColumns = ref(null); const tableName = ref('')
const poolStatus = ref(null)

const filteredTables = computed(() => {
  if (!tableSearch.value) return tables.value
  return tables.value.filter(t => t.name.toLowerCase().includes(tableSearch.value.toLowerCase()))
})

async function executeSql() {
  if (!sql.value.trim()) return
  execLoading.value = true; execResult.value = null; execError.value = ''
  try { const res = await executeSqlApi({ sql: sql.value }); execResult.value = res.data } 
  catch(e) { execError.value = e?.response?.data?.msg || e.message || '执行失败' } 
  finally { execLoading.value = false }
}

async function fetchTables() { try { const res = await getTablesApi(); tables.value = res.data || [] } catch {} }
async function selectTable(name) { selectedTable.value = name; tableName.value = name; sql.value = `SELECT * FROM ${name} LIMIT 10`; 
  try { const res = await getTableColumnsApi(name); tableColumns.value = res.data } catch {} }
async function fetchPoolStatus() { try { const res = await getPoolStatusApi(); poolStatus.value = res.data } catch {} }

onMounted(() => { fetchTables(); fetchPoolStatus() })
</script>

<style scoped>
.stat-item { text-align:center;padding:8px }
.stat-label { display:block;font-size:12px;color:var(--text-secondary,#909399) }
.stat-value { display:block;font-size:24px;font-weight:bold;margin-top:4px }
</style>