<template>
  <div class="page-container" :class="{ 'timeline-mode': viewMode === 'timeline' }">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索标题 / 妖怪 / 地点" clearable style="width: 240px" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex: 1" />
      <el-button-group class="view-toggle">
        <el-button :type="viewMode === 'timeline' ? 'primary' : ''" @click="switchView('timeline')">
          <el-icon><Timer /></el-icon> 时间轴
        </el-button>
        <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="switchView('table')">
          <el-icon><List /></el-icon> 列表
        </el-button>
      </el-button-group>
      <el-button type="primary" @click="handleAdd" v-if="userStore.hasPerm('classics:xiyou:event:add')">
        <el-icon><Plus /></el-icon> 新增事件
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" v-if="userStore.hasPerm('classics:xiyou:event:delete')" :disabled="selectedIds.length === 0">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
      <el-dropdown v-if="viewMode === 'table'" trigger="click" @command="toggleColumn">
        <el-button>
          <el-icon><Setting /></el-icon> 列设置
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

    <!-- 时间轴视图 -->
    <div v-if="viewMode === 'timeline'" class="timeline-wrapper">
      <!-- 概览统计 -->
      <div class="timeline-stats" v-if="allEvents.length">
        <div class="stat-card">
          <div class="stat-icon stat-total">
            <el-icon><Flag /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num">{{ allEvents.length }}</span>
            <span class="stat-label">总计劫难</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-location">
            <el-icon><Location /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num">{{ uniqueLocations }}</span>
            <span class="stat-label">途经地点</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-monster">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num">{{ monstersWithData }}</span>
            <span class="stat-label">遭遇妖怪</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-difficulty">
            <el-icon><StarFilled /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num">{{ avgDifficulty }}</span>
            <span class="stat-label">平均难度</span>
          </div>
        </div>
      </div>

      <!-- 时间轴 -->
      <div class="timeline-container" v-loading="timelineLoading">
        <div class="timeline-line"></div>
        <div
          v-for="(event, index) in filteredTimelineEvents"
          :key="event.id"
          class="timeline-item"
          :class="{ 'timeline-left': index % 2 === 0, 'timeline-right': index % 2 === 1 }"
        >
          <div class="timeline-card" @click="handleView(event)">
            <!-- 卡片头部 -->
            <div class="card-header">
              <span class="card-num">{{ String(event.difficultyNum).padStart(2, '0') }}</span>
              <span class="card-dot"></span>
              <span class="card-title">{{ event.title }}</span>
            </div>
            <!-- 卡片内容 -->
            <div class="card-body">
              <div class="card-tags">
                <el-tag v-if="event.chapterNum" size="small" effect="dark" round>第{{ event.chapterNum }}回</el-tag>
                <el-tag v-if="event.eventType" size="small" effect="plain" :type="eventTypeColor(event.eventType)" round>{{ event.eventType }}</el-tag>
              </div>
              <div class="card-meta">
                <div class="meta-item" v-if="event.location">
                  <el-icon><Location /></el-icon>
                  <span>{{ event.location }}</span>
                </div>
                <div class="meta-item" v-if="event.monster">
                  <el-icon><Warning /></el-icon>
                  <span>{{ event.monster }}</span>
                  <span v-if="event.monsterWeapon" class="meta-weapon">· {{ event.monsterWeapon }}</span>
                </div>
                <div class="meta-item" v-if="event.helper">
                  <el-icon><Connection /></el-icon>
                  <span>{{ event.helper }}</span>
                </div>
              </div>
              <div class="card-difficulty" v-if="event.difficultyLevel">
                <span class="difficulty-label">难度</span>
                <el-rate :model-value="event.difficultyLevel" :max="10" disabled show-score size="small" />
              </div>
            </div>
            <!-- 卡片底部操作 -->
            <div class="card-footer" @click.stop>
              <el-button link type="primary" size="small" @click="handleView(event)">
                <el-icon><View /></el-icon> 详情
              </el-button>
              <el-button link type="primary" size="small" @click="handleEdit(event)" v-if="userStore.hasPerm('classics:xiyou:event:edit')">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(event)" v-if="userStore.hasPerm('classics:xiyou:event:delete')">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
          </div>
          <!-- 时间轴节点 -->
          <div class="timeline-node">
            <div class="node-icon" :style="{ background: nodeColor(event.difficultyLevel) }">
              <span class="node-num">{{ event.difficultyNum }}</span>
            </div>
          </div>
        </div>
        <div v-if="!filteredTimelineEvents.length && !timelineLoading" class="timeline-empty">
          <el-empty description="暂无匹配的劫难数据" />
        </div>
      </div>
    </div>

    <!-- 表格视图 -->
    <div v-else class="classics-table-wrapper">
      <el-table :data="sortedTableData" border stripe v-loading="loading" :max-height="tableMaxHeight" style="width: 100%"
        @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <el-table-column type="selection" width="45" />
        <el-table-column v-if="visibleColumns.includes('difficultyNum')" prop="difficultyNum" label="难数" width="70" sortable>
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.difficultyNum }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('chapterNum')" prop="chapterNum" label="回目" width="70" sortable />
        <el-table-column v-if="visibleColumns.includes('title')" prop="title" label="事件标题" min-width="200" show-overflow-tooltip sortable>
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('monster')" prop="monster" label="妖怪" width="120" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('location')" prop="location" label="地点" width="120" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('helper')" prop="helper" label="帮手/救兵" min-width="130" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('difficultyLevel')" prop="difficultyLevel" label="难度" width="80" sortable>
          <template #default="{ row }">
            <el-tag v-if="row.difficultyLevel" size="small" :type="row.difficultyLevel >= 8 ? 'danger' : row.difficultyLevel >= 5 ? 'warning' : 'success'">
              {{ row.difficultyLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('createdTime')" prop="createdTime" label="创建时间" width="170" sortable />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="userStore.hasPerm('classics:xiyou:event:edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.hasPerm('classics:xiyou:event:delete')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page" v-model:page-size="size" :total="total"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
        class="page-pagination"
        @size-change="fetchData" @current-change="fetchData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="第几难" prop="difficultyNum">
          <el-input-number v-model="form.difficultyNum" :min="1" :max="81" />
        </el-form-item>
        <el-form-item label="对应回目">
          <el-input-number v-model="form.chapterNum" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="事件标题" prop="title">
          <el-input v-model="form.title" placeholder="如：金蝉遭贬第一难" />
        </el-form-item>
        <el-form-item label="发生地点">
          <el-input v-model="form.location" placeholder="如：蛇盘山鹰愁涧" />
        </el-form-item>
        <el-form-item label="妖怪名称">
          <el-input v-model="form.monster" placeholder="如：白骨精" />
        </el-form-item>
        <el-form-item label="妖怪武器/法宝">
          <el-input v-model="form.monsterWeapon" placeholder="如：金刚琢" />
        </el-form-item>
        <el-form-item label="帮手/救兵">
          <el-input v-model="form.helper" placeholder="如：观音菩萨" />
        </el-form-item>
        <el-form-item label="解决方式">
          <el-input v-model="form.resolution" type="textarea" :rows="3" placeholder="如：孙悟空请来观音菩萨降服" />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.detail" type="textarea" :rows="4" placeholder="请输入事件详细描述" />
        </el-form-item>
        <el-form-item label="难度等级">
          <el-rate v-model="form.difficultyLevel" :max="10" show-score />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-input v-model="form.eventType" placeholder="如：收徒、斗妖、渡劫" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看抽屉（右侧滑出） -->
    <el-drawer v-model="viewVisible" title="事件详情" direction="rtl" size="520px" :close-on-click-modal="true">
      <template #header>
        <div class="drawer-header">
          <span class="drawer-header-title">事件详情</span>
          <span class="drawer-header-badge">第{{ viewData?.difficultyNum }}难</span>
        </div>
      </template>
      <div v-if="viewData" class="event-detail">
        <div class="detail-hero">
          <div class="detail-num">{{ String(viewData.difficultyNum).padStart(2, '0') }}</div>
          <h2 class="detail-title">{{ viewData.title }}</h2>
        </div>
        <div class="detail-tags">
          <el-tag type="danger" size="small" effect="dark" round>第{{ viewData.difficultyNum }}难</el-tag>
          <el-tag v-if="viewData.chapterNum" size="small" effect="plain" round>第{{ viewData.chapterNum }}回</el-tag>
          <el-tag v-if="viewData.eventType" size="small" :type="eventTypeColor(viewData.eventType)" effect="plain" round>{{ viewData.eventType }}</el-tag>
          <el-tag v-if="viewData.difficultyLevel" size="small" type="warning" effect="dark" round>
            <el-icon><StarFilled /></el-icon> 难度 {{ viewData.difficultyLevel }}
          </el-tag>
        </div>
        <div class="detail-section" v-if="viewData.location || viewData.monster">
          <div class="detail-section-title">
            <el-icon><InfoFilled /></el-icon> 基本信息
          </div>
          <div class="detail-grid">
            <div class="detail-item" v-if="viewData.location">
              <span class="detail-label"><el-icon><Location /></el-icon> 地点</span>
              <span class="detail-value">{{ viewData.location }}</span>
            </div>
            <div class="detail-item" v-if="viewData.monster">
              <span class="detail-label"><el-icon><Warning /></el-icon> 妖怪</span>
              <span class="detail-value">{{ viewData.monster }}</span>
            </div>
            <div class="detail-item" v-if="viewData.monsterWeapon">
              <span class="detail-label"><el-icon><Aim /></el-icon> 武器/法宝</span>
              <span class="detail-value">{{ viewData.monsterWeapon }}</span>
            </div>
            <div class="detail-item" v-if="viewData.helper">
              <span class="detail-label"><el-icon><Connection /></el-icon> 帮手</span>
              <span class="detail-value">{{ viewData.helper }}</span>
            </div>
          </div>
        </div>
        <div class="detail-section" v-if="viewData.resolution">
          <div class="detail-section-title">
            <el-icon><Check /></el-icon> 解决方式
          </div>
          <p class="detail-text">{{ viewData.resolution }}</p>
        </div>
        <div class="detail-section" v-if="viewData.detail">
          <div class="detail-section-title">
            <el-icon><Document /></el-icon> 详细描述
          </div>
          <p class="detail-text">{{ viewData.detail }}</p>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
defineOptions({ name: 'ClassicsXiyouEvents' })
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useTableHeight } from '@/composables/useTableHeight'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getXiyouEventAllApi,
  getXiyouEventPageApi,
  addXiyouEventApi,
  updateXiyouEventApi,
  deleteXiyouEventApi,
  batchDeleteXiyouEventApi
} from '@/api/xiyou'

const userStore = useUserStore()

// 视图模式
const viewMode = ref('timeline')

// 时间轴数据
const allEvents = ref([])
const timelineLoading = ref(false)

// 表格数据
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])
const sortField = ref('')
const sortOrder = ref('')

const columnOptions = [
  { key: 'difficultyNum', label: '难数' },
  { key: 'chapterNum', label: '回目' },
  { key: 'title', label: '事件标题' },
  { key: 'monster', label: '妖怪' },
  { key: 'location', label: '地点' },
  { key: 'helper', label: '帮手' },
  { key: 'difficultyLevel', label: '难度' },
  { key: 'createdTime', label: '创建时间' }
]
const visibleColumns = ref(columnOptions.map(c => c.key))

function switchView(mode) {
  viewMode.value = mode
  if (mode === 'timeline') {
    fetchTimelineData()
  } else {
    fetchData()
  }
}

function toggleColumn(key) {
  const idx = visibleColumns.value.indexOf(key)
  if (idx > -1) {
    visibleColumns.value.splice(idx, 1)
  } else {
    visibleColumns.value.push(key)
  }
}

// 时间轴过滤数据
const filteredTimelineEvents = computed(() => {
  if (!keyword.value) return allEvents.value
  const kw = keyword.value.toLowerCase()
  return allEvents.value.filter(e =>
    (e.title && e.title.toLowerCase().includes(kw)) ||
    (e.monster && e.monster.toLowerCase().includes(kw)) ||
    (e.location && e.location.toLowerCase().includes(kw))
  )
})

// 统计
const uniqueLocations = computed(() => {
  const locs = new Set(allEvents.value.map(e => e.location).filter(Boolean))
  return locs.size
})
const monstersWithData = computed(() => {
  return allEvents.value.filter(e => e.monster).length
})
const avgDifficulty = computed(() => {
  const withLevel = allEvents.value.filter(e => e.difficultyLevel)
  if (!withLevel.length) return 0
  const sum = withLevel.reduce((s, e) => s + e.difficultyLevel, 0)
  return (sum / withLevel.length).toFixed(1)
})

// 节点颜色
function nodeColor(level) {
  if (!level) return '#909399'
  if (level >= 8) return 'linear-gradient(135deg, #f56c6c, #e74c3c)'
  if (level >= 6) return 'linear-gradient(135deg, #e6a23c, #f39c12)'
  if (level >= 4) return 'linear-gradient(135deg, #f59e0b, #d97706)'
  return 'linear-gradient(135deg, #67c23a, #2ecc71)'
}

// 事件类型颜色
function eventTypeColor(type) {
  const map = { '收徒': 'success', '斗妖': 'danger', '渡劫': 'warning', '遇险': 'info' }
  return map[type] || 'info'
}

const sortedTableData = computed(() => {
  const data = [...tableData.value]
  if (!sortField.value || !sortOrder.value) return data
  return data.sort((a, b) => {
    let valA = a[sortField.value]
    let valB = b[sortField.value]
    if (valA == null) valA = ''
    if (valB == null) valB = ''
    if (typeof valA === 'string') valA = valA.toLowerCase()
    if (typeof valB === 'string') valB = valB.toLowerCase()
    if (valA < valB) return sortOrder.value === 'ascending' ? -1 : 1
    if (valA > valB) return sortOrder.value === 'ascending' ? 1 : -1
    return 0
  })
})

function handleSortChange({ prop, order }) {
  sortField.value = prop || ''
  sortOrder.value = order || ''
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

// 动态表格高度（通过 useTableHeight 共享模块，.env 可配置行高/表头/最大行数）
const { tableMaxHeight, calcTableMaxHeight } = useTableHeight('.classics-table-wrapper')

const dialogVisible = ref(false)
const dialogTitle = ref('新增事件')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  difficultyNum: null,
  chapterNum: null,
  title: '',
  location: '',
  monster: '',
  monsterWeapon: '',
  helper: '',
  resolution: '',
  detail: '',
  difficultyLevel: null,
  eventType: ''
})

const formRules = {
  difficultyNum: [{ required: true, message: '请输入第几难', trigger: 'blur' }],
  title: [{ required: true, message: '请输入事件标题', trigger: 'blur' }]
}

const viewVisible = ref(false)
const viewData = ref(null)

onMounted(() => {
  calcTableMaxHeight()
  window.addEventListener('resize', calcTableMaxHeight)
  fetchTimelineData()
})

onUnmounted(() => {
  window.removeEventListener('resize', calcTableMaxHeight)
})

async function fetchTimelineData() {
  timelineLoading.value = true
  try {
    const res = await getXiyouEventAllApi()
    allEvents.value = res.data || []
  } finally {
    timelineLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, keyword: keyword.value }
    const res = await getXiyouEventPageApi(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  keyword.value = ''
  page.value = 1
  if (viewMode.value === 'timeline') {
    // timeline is filtered by computed, no need to refetch
  } else {
    fetchData()
  }
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增事件'
  resetForm()
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = row
  viewVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑事件'
  Object.assign(form, {
    id: row.id,
    difficultyNum: row.difficultyNum,
    chapterNum: row.chapterNum,
    title: row.title,
    location: row.location,
    monster: row.monster,
    monsterWeapon: row.monsterWeapon,
    helper: row.helper,
    resolution: row.resolution,
    detail: row.detail,
    difficultyLevel: row.difficultyLevel,
    eventType: row.eventType
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除事件 "${row.title}" 吗？`, '提示', { type: 'warning' })
    await deleteXiyouEventApi(row.id)
    ElMessage.success('删除成功')
    refreshCurrentView()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条事件吗？`, '批量删除', { type: 'warning' })
    await batchDeleteXiyouEventApi(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    refreshCurrentView()
  } catch {}
}

function refreshCurrentView() {
  if (viewMode.value === 'timeline') {
    fetchTimelineData()
  } else {
    fetchData()
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateXiyouEventApi({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addXiyouEventApi({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    refreshCurrentView()
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  form.id = null
  form.difficultyNum = null
  form.chapterNum = null
  form.title = ''
  form.location = ''
  form.monster = ''
  form.monsterWeapon = ''
  form.helper = ''
  form.resolution = ''
  form.detail = ''
  form.difficultyLevel = null
  form.eventType = ''
}
</script>

<style scoped>
/* ==================== 页面容器 ==================== */
.page-container.timeline-mode {
  background: linear-gradient(180deg, #f8f9fc 0%, #eef1f7 100%);
}

/* ==================== 视图切换按钮组 ==================== */
.view-toggle {
  margin-right: 12px;
}

/* ==================== 时间轴统计面板 ==================== */
.timeline-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  padding: 0 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}

.stat-total { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-location { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-monster { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-difficulty { background: linear-gradient(135deg, #fa709a, #fee140); }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

/* ==================== 时间轴容器 ==================== */
.timeline-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0 40px;
}

.timeline-container {
  position: relative;
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 0 60px;
}

/* 中间垂直线 */
.timeline-line {
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg,
    #667eea 0%,
    #764ba2 15%,
    #f093fb 30%,
    #4facfe 45%,
    #43e97b 60%,
    #fa709a 75%,
    #fee140 90%,
    #f56c6c 100%
  );
  transform: translateX(-50%);
  border-radius: 2px;
  opacity: 0.5;
}

/* ==================== 时间轴条目 ==================== */
.timeline-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  margin-bottom: 28px;
  min-height: 80px;
}

.timeline-item.timeline-left {
  flex-direction: row;
  padding-right: calc(50% + 40px);
}

.timeline-item.timeline-right {
  flex-direction: row-reverse;
  padding-left: calc(50% + 40px);
}

/* ==================== 时间轴卡片 ==================== */
.timeline-card {
  flex: 1;
  background: var(--bg-container);
  border-radius: 12px;
  padding: 18px 22px;
  box-shadow: 0 2px 16px var(--header-shadow);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--border-light);
  position: relative;
}

.timeline-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-color: #c6d0e6;
}

.timeline-card::before {
  content: '';
  position: absolute;
  top: 28px;
  width: 12px;
  height: 12px;
  background: #fff;
  border: 2px solid #c6d0e6;
  transform: rotate(45deg);
}

.timeline-left .timeline-card::before {
  right: -7px;
  border-left-color: transparent;
  border-bottom-color: transparent;
}

.timeline-right .timeline-card::before {
  left: -7px;
  border-right-color: transparent;
  border-top-color: transparent;
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.card-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

/* 卡片内容 */
.card-body {
  padding-left: 0;
}

.card-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.meta-item .el-icon {
  font-size: 14px;
  color: #909399;
}

.meta-weapon {
  color: #909399;
  font-size: 12px;
}

.card-difficulty {
  display: flex;
  align-items: center;
  gap: 8px;
}

.difficulty-label {
  font-size: 12px;
  color: #909399;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  gap: 4px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
}

/* ==================== 时间轴节点 ==================== */
.timeline-node {
  position: absolute;
  left: 50%;
  top: 20px;
  transform: translateX(-50%);
  z-index: var(--z-graphic, 100);
}

.node-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 6px #fff, 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.timeline-item:hover .node-icon {
  transform: scale(1.15);
  box-shadow: 0 0 0 8px #fff, 0 6px 20px rgba(0, 0, 0, 0.2);
}

.node-num {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

/* ==================== 空状态 ==================== */
.timeline-empty {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

/* ==================== 表格视图 ==================== */
.classics-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.classics-table-wrapper :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

/* ==================== 详情抽屉 ==================== */
.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.drawer-header-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}
.drawer-header-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.event-detail {
  padding: 0 4px;
}

/* 顶部英雄区 */
.detail-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0 20px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.detail-num {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  line-height: 1.4;
  margin: 0;
}

/* 标签区 */
.detail-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

/* 分区 */
.detail-section {
  margin-bottom: 22px;
}

.detail-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 2px;
}
.detail-section-title .el-icon {
  color: #667eea;
  font-size: 16px;
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: background 0.2s;
}
.detail-item:hover {
  background: #ebeef5;
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  min-width: 80px;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}
.detail-label .el-icon {
  font-size: 14px;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  word-break: break-all;
}

.detail-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.9;
  margin: 0;
  padding: 12px 14px;
  background: #fafbfc;
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .timeline-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  .timeline-item.timeline-left,
  .timeline-item.timeline-right {
    padding-left: 40px;
    padding-right: 0;
    flex-direction: row;
  }
  .timeline-line {
    left: 20px;
  }
  .timeline-node {
    left: 20px;
  }
  .timeline-card::before {
    left: -7px !important;
    right: auto !important;
    border-left-color: transparent !important;
    border-bottom-color: transparent !important;
    border-right-color: #c6d0e6 !important;
    border-top-color: #fff !important;
  }
}
</style>