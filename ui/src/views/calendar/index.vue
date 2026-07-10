<template>
  <div class="calendar-page">
    <el-card shadow="never" class="calendar-card">
      <div class="calendar-header">
        <div class="calendar-nav">
          <el-button text @click="prevMonth">
            <el-icon><DArrowLeft /></el-icon>
          </el-button>
          <h2 class="calendar-title">
            <template v-if="viewMode === 'month'">{{ currentYear }}年{{ currentMonth }}月</template>
            <template v-else>
              {{ weekStart.getFullYear() }}年{{ weekStart.getMonth() + 1 }}月 {{ weekStart.getDate() }}日 ~
              {{ new Date(weekStart.getTime() + 6 * 86400000).getDate() }}日
            </template>
          </h2>
          <el-button text @click="nextMonth">
            <el-icon><DArrowRight /></el-icon>
          </el-button>
          <el-button class="today-btn" size="small" @click="goToday">{{ $t('calendar.today') }}</el-button>
          <el-button size="small" :type="viewMode === 'month' ? 'primary' : ''" @click="switchView('month')">
            <el-icon><Calendar /></el-icon>月
          </el-button>
          <el-button size="small" :type="viewMode === 'week' ? 'primary' : ''" @click="switchView('week')">
            <el-icon><Calendar /></el-icon>周
          </el-button>
        </div>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>{{ $t('calendar.addEvent') }}
        </el-button>
      </div>

      <div class="calendar-weekdays">
        <div v-for="(day, i) in weekdays" :key="day" class="weekday" :class="{ 'is-weekend': i === 0 || i === 6 }">
          {{ day }}
        </div>
      </div>

      <!-- 月视图 -->
      <div v-if="viewMode === 'month'" class="calendar-grid">
        <div
          v-for="(cell, index) in calendarCells"
          :key="index"
          class="calendar-cell"
          :class="[
            'is-' + cell.dayType,
            {
              'is-other-month': !cell.isCurrentMonth,
              'is-today': cell.isToday,
              'is-selected': selectedDate === cell.dateStr
            }
          ]"
          @click="selectDay(cell)"
          @dragover="handleDragOver"
          @drop="handleDrop($event, cell.dateStr)"
        >
          <div class="cell-day">{{ cell.day }}</div>
          <div class="cell-lunar">{{ cell.lunarDay }}</div>
          <div v-if="cell.dayType === 'compWorkday'" class="cell-comp-workday">班</div>
          <div v-else-if="cell.holiday" class="cell-holiday" :style="{ color: cell.holidayColor }">
            {{ cell.holiday }}
          </div>
          <div v-if="cell.events.length" class="cell-events">
            <span
              v-for="(evt, ei) in cell.events.slice(0, 3)"
              :key="ei"
              class="event-dot"
              :style="{ background: evt.color || '#409eff' }"
            />
            <span v-if="cell.events.length > 3" class="event-more">+{{ cell.events.length - 3 }}</span>
          </div>
        </div>
      </div>

      <!-- 周视图 -->
      <div v-else class="week-grid">
        <div
          v-for="cell in weekCells"
          :key="cell.dateStr"
          class="week-cell"
          :class="['is-' + cell.dayType, { 'is-today': cell.isToday }]"
          @dragover="handleDragOver"
          @drop="handleDrop($event, cell.dateStr)"
        >
          <div class="week-cell-header">
            <span class="week-cell-day">{{ cell.day }}</span>
            <span class="week-cell-lunar">{{ cell.lunarDay }}</span>
          </div>
          <div class="week-cell-events">
            <div
              v-for="evt in cell.events"
              :key="evt.id"
              class="week-event-item"
              :style="{ borderLeftColor: evt.color || '#409eff' }"
              draggable="true"
              @dragstart="handleDragStart($event, evt.id)"
              @click.stop="selectDay(cell)"
            >
              <span class="week-event-time">{{ evt.startTime || '' }}</span>
              <span class="week-event-title">{{ evt.title }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dayDialogVisible" :title="selectedDayTitle" width="500px" :close-on-click-modal="false">
      <div v-if="selectedDayEvents.length" class="day-event-list">
        <div v-for="evt in selectedDayEvents" :key="evt.id" class="day-event-item">
          <span class="event-color-bar" :style="{ background: evt.color || '#409eff' }" />
          <div class="event-info">
            <div class="event-title">{{ evt.title }}</div>
            <div class="event-meta">
              <span v-if="evt.startTime" class="event-time">{{ evt.startTime }}</span>
              <el-tag size="small" :type="eventTypeTag(evt.eventType)" effect="plain">{{
                eventTypeLabel(evt.eventType)
              }}</el-tag>
              <el-tag size="small" :type="statusTag(evt.status)" effect="plain">{{ statusLabel(evt.status) }}</el-tag>
            </div>
          </div>
          <div class="event-actions">
            <el-button text size="small" @click="openEditDialog(evt)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button text size="small" type="danger" @click="handleDelete(evt.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else :description="$t('calendar.noEvents')" />
      <template #footer>
        <el-button type="primary" @click="openAddDialogForDay">
          <el-icon><Plus /></el-icon>{{ $t('calendar.addEvent') }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="eventDialogVisible"
      :title="isEditing ? $t('calendar.editEvent') : $t('calendar.addEvent')"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="eventFormRef" :model="eventForm" :rules="eventRules" label-width="90px">
        <el-form-item :label="$t('calendar.titleLabel')" prop="title">
          <el-input v-model="eventForm.title" :placeholder="$t('calendar.titleLabel')" />
        </el-form-item>
        <el-form-item :label="$t('calendar.descriptionLabel')" prop="description">
          <el-input
            v-model="eventForm.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('calendar.descriptionLabel')"
          />
        </el-form-item>
        <el-form-item :label="$t('calendar.dateLabel')" prop="eventDate">
          <el-date-picker v-model="eventForm.eventDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('calendar.allDay')">
          <el-switch v-model="eventForm.isAllDay" />
        </el-form-item>
        <template v-if="!eventForm.isAllDay">
          <el-form-item :label="$t('calendar.startTime')" prop="startTime">
            <el-time-picker v-model="eventForm.startTime" format="HH:mm" style="width: 100%" />
          </el-form-item>
          <el-form-item :label="$t('calendar.endTime')" prop="endTime">
            <el-time-picker v-model="eventForm.endTime" format="HH:mm" style="width: 100%" />
          </el-form-item>
        </template>
        <el-form-item :label="$t('calendar.eventType')" prop="eventType">
          <el-select v-model="eventForm.eventType" style="width: 100%">
            <el-option label="Event" value="EVENT" />
            <el-option label="Reminder" value="REMINDER" />
            <el-option label="Todo" value="TODO" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('calendar.priority')" prop="priority">
          <el-radio-group v-model="eventForm.priority">
            <el-radio-button :value="0">{{ $t('calendar.lowPriority') }}</el-radio-button>
            <el-radio-button :value="1">{{ $t('calendar.mediumPriority') }}</el-radio-button>
            <el-radio-button :value="2">{{ $t('calendar.highPriority') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('calendar.color')" prop="color">
          <el-color-picker v-model="eventForm.color" show-alpha />
        </el-form-item>
        <el-form-item :label="$t('calendar.status')" prop="status">
          <el-select v-model="eventForm.status" style="width: 100%">
            <el-option :label="$t('calendar.pending')" :value="0" />
            <el-option :label="$t('calendar.completed')" :value="1" />
            <el-option :label="$t('calendar.cancelled')" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="eventDialogVisible = false" :disabled="saving">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveEvent">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, DArrowLeft, DArrowRight, Calendar } from '@element-plus/icons-vue'
import { Solar, HolidayUtil } from 'lunar-javascript'
import {
  getCalendarEventsByMonthApi,
  getCalendarEventsByRangeApi,
  createEventApi,
  updateEventApi,
  deleteEventApi,
  getEventByIdApi
} from '@/api/calendar'

defineOptions({ name: 'CalendarIndex' })

const { t } = useI18n()

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth() + 1)
const viewMode = ref('month')
const selectedDate = ref(formatDate(today))
const events = ref([])
const dayDialogVisible = ref(false)
const eventDialogVisible = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const eventFormRef = ref(null)
const selectedDayEvents = ref([])
const selectedDayTitle = ref('')
const dragEventId = ref(null)

const eventForm = ref({
  id: null,
  title: '',
  description: '',
  eventDate: null,
  startTime: null,
  endTime: null,
  eventType: 'EVENT',
  priority: 0,
  color: '#409eff',
  isAllDay: true,
  status: 0
})

const defaultForm = { ...eventForm.value, color: '#409eff' }

const eventRules = {
  title: [{ required: true, message: () => t('calendar.titleLabel') + '不能为空', trigger: 'blur' }],
  eventDate: [{ required: true, message: () => t('calendar.dateLabel') + '不能为空', trigger: 'blur' }]
}

function getLunarInfo(year, month, day) {
  try {
    const solar = Solar.fromYmd(year, month, day)
    const lunar = solar.getLunar()
    const lunarDay = lunar.getDayInChinese()
    const holiday = getHoliday(year, month, day)
    return { lunarDay, holiday: holiday ? holiday.name : null, isCompWorkday: holiday ? holiday.isWork : false }
  } catch {
    return { lunarDay: '', holiday: null, isCompWorkday: false }
  }
}

function getHoliday(year, month, day) {
  try {
    const h = HolidayUtil.getHoliday(year, month, day)
    if (!h) return null
    return { name: h.getName(), isWork: h.isWork() }
  } catch {
    return null
  }
}

function getHolidayColor(name) {
  const important = ['春节', '国庆节', '元旦', '劳动节', '清明节', '端午节', '中秋节']
  return important.includes(name) ? 'var(--el-color-danger)' : 'var(--el-color-warning)'
}

const calendarCells = computed(() => {
  const cells = []
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1)
  const lastDay = new Date(currentYear.value, currentMonth.value, 0)
  const startDayOfWeek = firstDay.getDay()
  const daysInMonth = lastDay.getDate()

  const prevMonthLastDay = new Date(currentYear.value, currentMonth.value - 1, 0).getDate()

  for (let i = startDayOfWeek - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i
    const date = new Date(currentYear.value, currentMonth.value - 2, day)
    const lunar = getLunarInfo(date.getFullYear(), date.getMonth() + 1, day)
    const dow = date.getDay()
    cells.push({
      day,
      lunarDay: lunar.lunarDay,
      holiday: null,
      holidayColor: null,
      isCurrentMonth: false,
      isToday: false,
      dateStr: formatDate(date),
      events: [],
      dayType: dow === 0 ? 'sunday' : dow === 6 ? 'saturday' : 'workday'
    })
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(currentYear.value, currentMonth.value - 1, day)
    const lunar = getLunarInfo(currentYear.value, currentMonth.value, day)
    const isToday = date.toDateString() === today.toDateString()
    const dateStr = formatDate(date)
    const dayEvents = events.value.filter((e) => e.eventDate === dateStr)
    const dow = date.getDay()
    let dayType = 'workday'
    if (lunar.isCompWorkday) {
      dayType = 'compWorkday'
    } else if (lunar.holiday) {
      dayType = 'holiday'
    } else if (dow === 0) {
      dayType = 'sunday'
    } else if (dow === 6) {
      dayType = 'saturday'
    }
    cells.push({
      day,
      lunarDay: lunar.lunarDay,
      holiday: lunar.holiday,
      holidayColor: lunar.holiday ? getHolidayColor(lunar.holiday) : null,
      isCurrentMonth: true,
      isToday,
      dateStr,
      events: dayEvents,
      dayType
    })
  }

  const remaining = 42 - cells.length
  for (let day = 1; day <= remaining; day++) {
    const date = new Date(currentYear.value, currentMonth.value, day)
    const lunar = getLunarInfo(date.getFullYear(), date.getMonth() + 1, day)
    const dow = date.getDay()
    cells.push({
      day,
      lunarDay: lunar.lunarDay,
      holiday: null,
      holidayColor: null,
      isCurrentMonth: false,
      isToday: false,
      dateStr: formatDate(date),
      events: [],
      dayType: dow === 0 ? 'sunday' : dow === 6 ? 'saturday' : 'workday'
    })
  }

  return cells
})

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function parseDate(str) {
  const [y, m, d] = str.split('-').map(Number)
  return new Date(y, m - 1, d)
}

const weekStart = computed(() => {
  const base = selectedDate.value ? parseDate(selectedDate.value) : new Date()
  const day = base.getDay()
  const diff = base.getDate() - day
  return new Date(base.getFullYear(), base.getMonth(), diff)
})

const weekCells = computed(() => {
  const cells = []
  const start = new Date(weekStart.value)
  for (let i = 0; i < 7; i++) {
    const date = new Date(start)
    date.setDate(start.getDate() + i)
    const dateStr = formatDate(date)
    const lunar = getLunarInfo(date.getFullYear(), date.getMonth() + 1, date.getDate())
    const isToday = date.toDateString() === today.toDateString()
    const dayEvents = events.value.filter((e) => e.eventDate === dateStr)
    const dow = date.getDay()
    let dayType = 'workday'
    if (lunar.isCompWorkday) {
      dayType = 'compWorkday'
    } else if (lunar.holiday) {
      dayType = 'holiday'
    } else if (dow === 0) {
      dayType = 'sunday'
    } else if (dow === 6) {
      dayType = 'saturday'
    }
    cells.push({
      day: date.getDate(),
      dateStr,
      lunarDay: lunar.lunarDay,
      holiday: lunar.holiday,
      holidayColor: lunar.holiday ? getHolidayColor(lunar.holiday) : null,
      dayType,
      isToday,
      events: dayEvents
    })
  }
  return cells
})

function prevWeek() {
  const d = parseDate(selectedDate.value)
  d.setDate(d.getDate() - 7)
  selectedDate.value = formatDate(d)
  fetchEvents()
}

function nextWeek() {
  const d = parseDate(selectedDate.value)
  d.setDate(d.getDate() + 7)
  selectedDate.value = formatDate(d)
  fetchEvents()
}

function goToday() {
  if (viewMode.value === 'week') {
    selectedDate.value = formatDate(today)
    fetchEvents()
  } else {
    currentYear.value = today.getFullYear()
    currentMonth.value = today.getMonth() + 1
    fetchEvents()
  }
}

function switchView(mode) {
  viewMode.value = mode
  if (mode === 'week' && !selectedDate.value) {
    selectedDate.value = formatDate(today)
  }
  fetchEvents()
}

function handleDrop(ev, dateStr) {
  ev.preventDefault()
  const eventId = dragEventId.value
  if (!eventId) return
  dragEventId.value = null
  const evt = events.value.find((e) => e.id === eventId)
  if (!evt || evt.eventDate === dateStr) return
  updateEventApi({ id: eventId, eventDate: dateStr })
    .then(() => {
      fetchEvents()
      ElMessage.success('日期已更新')
    })
    .catch(() => {
      ElMessage.error('更新失败')
    })
}

function handleDragStart(ev, eventId) {
  dragEventId.value = eventId
  ev.dataTransfer.effectAllowed = 'move'
}

function handleDragOver(ev) {
  ev.preventDefault()
  ev.dataTransfer.dropEffect = 'move'
}

function prevMonth() {
  if (viewMode.value === 'week') {
    prevWeek()
    return
  }
  if (currentMonth.value === 1) {
    currentYear.value--
    currentMonth.value = 12
  } else {
    currentMonth.value--
  }
  fetchEvents()
}

function nextMonth() {
  if (viewMode.value === 'week') {
    nextWeek()
    return
  }
  if (currentMonth.value === 12) {
    currentYear.value++
    currentMonth.value = 1
  } else {
    currentMonth.value++
  }
  fetchEvents()
}

function selectDay(cell) {
  selectedDate.value = cell.dateStr
  selectedDayEvents.value = cell.events
  const date = new Date(cell.dateStr)
  const solar = Solar.fromYmd(date.getFullYear(), date.getMonth() + 1, date.getDate())
  const lunar = solar.getLunar()
  const lunarStr = lunar.getMonthInChinese() + '月' + lunar.getDayInChinese()
  selectedDayTitle.value = `${cell.dateStr}  ${lunarStr}`
  dayDialogVisible.value = true
}

function openAddDialog() {
  isEditing.value = false
  eventForm.value = { ...defaultForm, eventDate: null, id: null, startTime: null, endTime: null }
  eventDialogVisible.value = true
}

function openAddDialogForDay() {
  isEditing.value = false
  eventForm.value = { ...defaultForm, eventDate: selectedDate.value, id: null, startTime: null, endTime: null }
  eventDialogVisible.value = true
  dayDialogVisible.value = false
}

async function openEditDialog(evt) {
  isEditing.value = true
  const res = await getEventByIdApi(evt.id)
  if (res.data) {
    eventForm.value = {
      id: res.data.id,
      title: res.data.title,
      description: res.data.description || '',
      eventDate: res.data.eventDate,
      startTime: res.data.startTime || null,
      endTime: res.data.endTime || null,
      eventType: res.data.eventType || 'EVENT',
      priority: res.data.priority ?? 0,
      color: res.data.color || '#409eff',
      isAllDay: res.data.isAllDay ?? true,
      status: res.data.status ?? 0
    }
  }
  eventDialogVisible.value = true
  dayDialogVisible.value = false
}

async function handleSaveEvent() {
  const valid = await eventFormRef.value.validate().catch(() => false)
  if (!valid || saving.value) return
  saving.value = true
  try {
    if (isEditing.value) {
      await updateEventApi(eventForm.value)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await createEventApi(eventForm.value)
      ElMessage.success(t('common.addSuccess'))
    }
    eventDialogVisible.value = false
    fetchEvents()
  } catch (e) {
    const msg = e?.response?.data?.message || t('common.operateFail')
    ElMessage.error(msg)
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm(t('calendar.deleteConfirm'), t('calendar.deleteEvent'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteEventApi(id)
    ElMessage.success(t('common.deleteSuccess'))
    dayDialogVisible.value = false
    fetchEvents()
  } catch {
    // cancelled
  }
}

function eventTypeLabel(type) {
  const map = { EVENT: t('calendar.typeEvent'), REMINDER: t('calendar.typeReminder'), TODO: t('calendar.typeTodo') }
  return map[type] || type
}

function eventTypeTag(type) {
  const map = { EVENT: 'primary', REMINDER: 'warning', TODO: 'info' }
  return map[type] || ''
}

function statusLabel(status) {
  const map = { 0: t('calendar.pending'), 1: t('calendar.completed'), 2: t('calendar.cancelled') }
  return map[status] || ''
}

function statusTag(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || ''
}

async function fetchEvents() {
  try {
    if (viewMode.value === 'week') {
      const start = formatDate(weekStart.value)
      const endDate = new Date(weekStart.value)
      endDate.setDate(endDate.getDate() + 6)
      const end = formatDate(endDate)
      const res = await getCalendarEventsByRangeApi(start, end)
      events.value = res.data || []
    } else {
      const res = await getCalendarEventsByMonthApi(currentYear.value, currentMonth.value)
      events.value = res.data || []
    }
  } catch {
    events.value = []
  }
}

onMounted(() => {
  fetchEvents()
})
</script>

<style scoped lang="scss">
.calendar-page {
  padding: 16px;
}

.calendar-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.calendar-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.calendar-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 12px;
  min-width: 130px;
  text-align: center;
  color: var(--color-text-primary, var(--el-text-color-primary));
}

.today-btn {
  margin-left: 8px;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: 2px;
}

.weekday {
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  padding: 8px 0;
  color: var(--color-text-secondary, var(--el-text-color-secondary));

  &.is-weekend {
    &:first-child {
      color: var(--el-color-danger);
    }
    &:last-child {
      color: var(--el-color-primary);
    }
  }
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

/* 周视图 */
.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  min-height: 400px;
}

.week-cell {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-color, var(--el-border-color-lighter));
  border-radius: 6px;
  background: var(--bg-page, var(--el-fill-color-blank));
  min-height: 300px;

  &.is-today {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  &.is-sunday .week-cell-day {
    color: var(--el-color-danger);
  }
  &.is-saturday .week-cell-day {
    color: var(--el-color-primary);
  }
  &.is-holiday {
    background: #fff5f5;
  }
  &.is-comp-workday {
    background: #f5f8ff;
  }
}

.week-cell-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid var(--border-color, var(--el-border-color-lighter));
}

.week-cell-day {
  font-size: 16px;
  font-weight: 600;
}

.week-cell-lunar {
  font-size: 10px;
  color: var(--color-text-tertiary, var(--el-text-color-placeholder));
}

.week-cell-events {
  flex: 1;
  padding: 4px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.week-event-item {
  padding: 4px 6px;
  border-left: 3px solid #409eff;
  border-radius: 3px;
  background: var(--el-fill-color-light);
  cursor: grab;
  font-size: 12px;
  line-height: 1.4;
  transition: background 0.15s;

  &:hover {
    background: var(--el-color-primary-light-9);
  }

  &:active {
    cursor: grabbing;
  }
}

.week-event-time {
  font-size: 10px;
  color: var(--color-text-tertiary, var(--el-text-color-placeholder));
  margin-right: 4px;
}

.week-event-title {
  color: var(--color-text-primary, var(--el-text-color-primary));
}

.calendar-cell {
  min-height: 90px;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  background: var(--bg-page, var(--el-fill-color-blank));
  border: 1px solid var(--border-color, var(--el-border-color-lighter));
  position: relative;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.is-other-month {
    opacity: 0.4;
  }

  &.is-today {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  &.is-selected {
    border-color: var(--el-color-primary);
    box-shadow: 0 0 0 2px var(--el-color-primary-light-5);
  }

  &.is-sunday .cell-day {
    color: var(--el-color-danger);
  }

  &.is-saturday .cell-day {
    color: var(--el-color-primary);
  }

  &.is-holiday {
    background: #fff5f5;
  }

  &.is-comp-workday {
    background: #f5f8ff;
  }
}

:global(html.dark) {
  .calendar-cell {
    &.is-holiday {
      background: rgba(245, 108, 108, 0.1);
    }
    &.is-comp-workday {
      background: rgba(64, 158, 255, 0.1);
    }
  }
}

.cell-day {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, var(--el-text-color-primary));
  margin-bottom: 2px;
}

.cell-lunar {
  font-size: 11px;
  color: var(--color-text-tertiary, var(--el-text-color-placeholder));
  margin-bottom: 2px;
}

.cell-holiday {
  font-size: 11px;
  font-weight: 500;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell-comp-workday {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  color: #fff;
  background: var(--el-color-primary);
  border-radius: 3px;
  padding: 0 4px;
  line-height: 16px;
  margin-bottom: 2px;
}

.cell-events {
  display: flex;
  gap: 3px;
  flex-wrap: wrap;
  margin-top: auto;
}

.event-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

.event-more {
  font-size: 10px;
  color: var(--color-text-tertiary, var(--el-text-color-placeholder));
}

.day-event-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.day-event-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid var(--border-color, var(--el-border-color-lighter));
  transition: background 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }
}

.event-color-bar {
  width: 4px;
  height: 100%;
  min-height: 40px;
  border-radius: 2px;
  flex-shrink: 0;
}

.event-info {
  flex: 1;
  min-width: 0;
}

.event-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: var(--color-text-primary, var(--el-text-color-primary));
}

.event-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.event-time {
  font-size: 12px;
  color: var(--color-text-secondary, var(--el-text-color-secondary));
}

.event-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

/* 响应式：平板 */
@media (max-width: 1024px) {
  .calendar-cell {
    min-height: 70px;
    padding: 4px 6px;
  }
  .cell-day {
    font-size: 14px;
  }
  .cell-lunar,
  .cell-holiday,
  .cell-comp-workday {
    font-size: 10px;
  }

  .week-cell {
    min-height: 200px;
  }
  .week-event-item {
    font-size: 11px;
    padding: 3px 4px;
  }
}

/* 响应式：手机 */
@media (max-width: 768px) {
  .calendar-page {
    padding: 8px;
  }
  .calendar-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  .calendar-nav {
    flex-wrap: wrap;
    justify-content: center;
  }
  .calendar-title {
    font-size: 15px;
    min-width: auto;
    margin: 0 8px;
  }
  .calendar-cell {
    min-height: 56px;
    padding: 3px 4px;
  }
  .cell-day {
    font-size: 13px;
  }
  .cell-lunar,
  .cell-holiday,
  .cell-comp-workday {
    display: none;
  }

  .week-grid {
    overflow-x: auto;
  }
  .week-cell {
    min-width: 100px;
    min-height: 150px;
  }
  .week-cell-lunar {
    display: none;
  }

  .el-dialog {
    width: 95% !important;
  }
}
</style>
