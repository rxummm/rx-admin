<template>
  <div class="as400-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select
        v-model="selectedLibrary"
        :placeholder="$t('as400.library')"
        clearable
        filterable
        allow-create
        default-first-option
        style="width: 200px"
        @change="handleLibraryChange"
      >
        <el-option v-for="lib in libraryOptions" :key="lib" :label="lib" :value="lib" />
      </el-select>
      <el-input
        v-model="keyword"
        :placeholder="$t('common.search')"
        clearable
        style="width: 240px"
        @keyup.enter="fetchData"
      />
      <el-button type="primary" :loading="loading" @click="fetchData">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <div style="flex: 1" />
      <el-button :loading="loading" @click="fetchData">
        <el-icon><Refresh /></el-icon> {{ $t('common.refresh') }}
      </el-button>
      <el-dropdown trigger="click" @command="toggleColumn">
        <el-button>
          <el-icon><Setting /></el-icon> {{ $t('common.columns') }}
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="col in columnOptions" :key="col.key" :command="col.key">
              <el-icon v-if="visibleColumns.includes(col.key)"><Check /></el-icon>
              <span :style="{ opacity: visibleColumns.includes(col.key) ? 1 : 0.4 }">{{ col.label }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 统计信息 -->
    <div class="stats-bar">
      <template v-if="libraryNotFound">
        <el-tag type="danger">{{ $t('as400.libraryNotFound', { library: selectedLibrary }) }}</el-tag>
      </template>
      <template v-else>
        <el-tag type="info">{{ $t('as400.totalObjects') }}: {{ filteredData.length }}</el-tag>
        <el-tag v-for="stat in typeStats" :key="stat.type" :type="stat.tagType" style="margin-left: 8px">
          {{ stat.type }}: {{ stat.count }}
        </el-tag>
      </template>
    </div>

    <!-- 数据表格 -->
    <div class="as400-table-wrapper">
      <el-table
        :data="pagedTableData"
        border
        stripe
        v-loading="loading"
        :max-height="tableMaxHeight"
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column
          v-if="visibleColumns.includes('library')"
          prop="library"
          :label="$t('as400.library')"
          width="120"
          sortable="custom"
        />
        <el-table-column
          v-if="visibleColumns.includes('objectName')"
          prop="objectName"
          :label="$t('as400.objectName')"
          width="150"
          show-overflow-tooltip
          sortable="custom"
        />
        <el-table-column
          v-if="visibleColumns.includes('objectType')"
          prop="objectType"
          :label="$t('as400.objectType')"
          width="120"
          sortable="custom"
        >
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeTag(row.objectType)">{{ row.objectType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="visibleColumns.includes('attribute')"
          prop="attribute"
          :label="$t('as400.attribute')"
          width="130"
        />
        <el-table-column
          v-if="visibleColumns.includes('text')"
          prop="text"
          :label="$t('as400.text')"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column v-if="visibleColumns.includes('owner')" prop="owner" :label="$t('as400.owner')" width="90" />
        <el-table-column
          v-if="visibleColumns.includes('createDate')"
          prop="createDate"
          :label="$t('as400.createDate')"
          width="180"
          sortable="custom"
        />
        <el-table-column
          v-if="visibleColumns.includes('size')"
          prop="size"
          :label="$t('as400.size')"
          width="100"
          sortable="custom"
        >
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页条 -->
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="filteredData.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="page-pagination"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'As400Objects' })
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAs400ObjectsByLibApi } from '@/api/as400'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Setting, Check } from '@element-plus/icons-vue'

const { t } = useI18n()

const loading = ref(false)
const keyword = ref('')
const selectedLibrary = ref('')
const tableData = ref([])
const tableMaxHeight = ref(600)
const page = ref(1)
const size = ref(20)
const libraryNotFound = ref(false) // 库不存在标志

const libraryOptions = ['A7RXUZZ1', 'A7RXUZZ2', 'A7RXUZZB']

// 列设置
const visibleColumns = ref(['library', 'objectName', 'objectType', 'attribute', 'text', 'owner', 'createDate', 'size'])
const columnOptions = [
  { key: 'library', label: computed(() => t('as400.library')) },
  { key: 'objectName', label: computed(() => t('as400.objectName')) },
  { key: 'objectType', label: computed(() => t('as400.objectType')) },
  { key: 'attribute', label: computed(() => t('as400.attribute')) },
  { key: 'text', label: computed(() => t('as400.text')) },
  { key: 'owner', label: computed(() => t('as400.owner')) },
  { key: 'createDate', label: computed(() => t('as400.createDate')) },
  { key: 'size', label: computed(() => t('as400.size')) }
]

// 排序
const sortInfo = reactive({ prop: null, order: null })

// 过滤后的数据
const filteredData = computed(() => {
  let data = tableData.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    data = data.filter(
      (row) =>
        (row.objectName && row.objectName.toLowerCase().includes(kw)) ||
        (row.objectType && row.objectType.toLowerCase().includes(kw)) ||
        (row.library && row.library.toLowerCase().includes(kw)) ||
        (row.attribute && row.attribute.toLowerCase().includes(kw)) ||
        (row.text && row.text.toLowerCase().includes(kw)) ||
        (row.owner && row.owner.toLowerCase().includes(kw))
    )
  }
  return data
})

// 排序后的数据（全量，用于分页）
const sortedTableData = computed(() => {
  const data = [...filteredData.value]
  if (sortInfo.prop && sortInfo.order) {
    const prop = sortInfo.prop
    const order = sortInfo.order === 'ascending' ? 1 : -1
    data.sort((a, b) => {
      const valA = (a[prop] || '').toString().toLowerCase()
      const valB = (b[prop] || '').toString().toLowerCase()
      if (prop === 'size') {
        return ((a.size || 0) - (b.size || 0)) * order
      }
      return valA.localeCompare(valB) * order
    })
  }
  return data
})

// 当前页数据（分页切片）
const pagedTableData = computed(() => {
  const start = (page.value - 1) * size.value
  return sortedTableData.value.slice(start, start + size.value)
})

// 类型统计
const typeStats = computed(() => {
  const map = {}
  filteredData.value.forEach((row) => {
    const type = row.objectType || 'UNKNOWN'
    map[type] = (map[type] || 0) + 1
  })
  // 使用 info 作为兜底类型，避免空字符串导致 ElTag 警告
  const tagColors = {
    '*PGM': 'primary',
    '*FILE': 'success',
    '*MSGF': 'warning',
    '*DTAARA': 'info',
    '*CMD': 'danger',
    '*SRVPGM': 'info'
  }
  return Object.entries(map).map(([type, count]) => ({
    type,
    count,
    tagType: tagColors[type] || 'info'
  }))
})

function getTypeTag(type) {
  // 返回有效 ElTag type 值，避免空字符串警告
  const map = {
    '*PGM': 'primary',
    '*FILE': 'success',
    '*MSGF': 'warning',
    '*DTAARA': 'info',
    '*CMD': 'danger',
    '*SRVPGM': 'info'
  }
  return map[type] || 'info'
}

// 计算表格最大高度
function calcTableMaxHeight() {
  nextTick(() => {
    const windowHeight = window.innerHeight
    tableMaxHeight.value = Math.max(400, windowHeight - 310)
  })
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function handleSortChange({ prop, order }) {
  sortInfo.prop = prop
  sortInfo.order = order
}

function handleLibraryChange(val) {
  if (!val) {
    // 清空选择，清除表格数据
    tableData.value = []
    libraryNotFound.value = false
    return
  }
  // 直接查询（连接失败说明库不存在）
  fetchData()
}

function toggleColumn(key) {
  const idx = visibleColumns.value.indexOf(key)
  if (idx > -1) {
    visibleColumns.value.splice(idx, 1)
  } else {
    visibleColumns.value.push(key)
  }
}

function handlePageChange() {
  // 分页切换时重置到第1页
  page.value = 1
}

function resetSearch() {
  keyword.value = ''
  selectedLibrary.value = ''
  page.value = 1
  libraryNotFound.value = false
}

async function fetchData() {
  if (!selectedLibrary.value) {
    ElMessage.warning(t('as400.selectLibraryHint') || '请先选择库名')
    return
  }
  loading.value = true
  libraryNotFound.value = false
  try {
    const res = await getAs400ObjectsByLibApi(selectedLibrary.value)
    tableData.value = res.data || []
  } catch (e) {
    // 拦截器对 code !== 200 会 Promise.reject(Error(message))
    // 后端返回的 message 为 "库 xxx 不存在"，据此判断库不存在
    tableData.value = []
    const msg = e?.message || ''
    if (msg.includes('不存在')) {
      libraryNotFound.value = true
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  calcTableMaxHeight()
  window.addEventListener('resize', calcTableMaxHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', calcTableMaxHeight)
})
</script>

<style scoped lang="scss">
.as400-page {
  .search-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
    flex-wrap: wrap;
  }

  .stats-bar {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    flex-wrap: wrap;
    gap: 4px;
  }

  .as400-table-wrapper {
    background: var(--table-container-bg);
    border-radius: 4px;
    padding: 8px 0 0;
  }
}
</style>
