/**
 * 通用表格分页 Composable
 * 封装搜索、分页、排序、列配置、表格高度自适应等通用逻辑
 *
 * @example
 * const {
 *   tableData, loading, keyword, page, size, total,
 *   sortedTableData, handleSortChange, handleSelectionChange, selectedIds,
 *   visibleColumns, columnOptions, toggleColumn,
 *   fetchData, handleSearch, resetSearch,
 *   tableMaxHeight, calcTableMaxHeight
 * } = useTablePage(fetchApi, { columns, defaultSize: 10 })
 */
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

const TABLE_ROW_HEIGHT = Number(import.meta.env.VITE_TABLE_ROW_HEIGHT) || 48
const TABLE_PADDING = Number(import.meta.env.VITE_TABLE_PADDING) || 120

function parsePageSizes(envValue, fallback) {
  if (envValue) {
    return envValue.split(',').map(Number).filter(n => !isNaN(n) && n > 0)
  }
  return fallback
}

export function useTablePage(fetchApi, options = {}) {
  const {
    columns = [],
    defaultSize = Number(import.meta.env.VITE_DEFAULT_PAGE_SIZE) || 10,
    enableResize = true,
    pageSizes = parsePageSizes(import.meta.env.VITE_PAGE_SIZE_OPTIONS, [10, 20, 50, 100]),
    transformResponse = null, // (data) => transformedData
  } = options

  const tableData = ref([])
  const loading = ref(false)
  const keyword = ref('')
  const page = ref(1)
  const size = ref(defaultSize)
  const total = ref(0)
  const sortField = ref('')
  const sortOrder = ref('')
  const selectedIds = ref([])

  // 列配置
  const columnOptions = columns
  const visibleColumns = ref(columnOptions.map(c => c.key))

  function toggleColumn(key) {
    const idx = visibleColumns.value.indexOf(key)
    if (idx > -1) {
      visibleColumns.value.splice(idx, 1)
    } else {
      visibleColumns.value.push(key)
    }
  }

  // 前端排序
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

  function handleSelectionChange(selection) {
    selectedIds.value = selection.map(item => item.id)
  }

  // 表格高度自适应
  const tableMaxHeight = ref(Number(import.meta.env.VITE_TABLE_MAX_HEIGHT) || 400)
  let resizeObserver = null

  function calcTableMaxHeight(el) {
    if (!el) return
    const rect = el.getBoundingClientRect()
    const top = rect.top
    const windowHeight = window.innerHeight
    const h = windowHeight - top - TABLE_PADDING
    tableMaxHeight.value = Math.max(200, Math.floor(h / TABLE_ROW_HEIGHT) * TABLE_ROW_HEIGHT)
  }

  function bindResizeObserver() {
    if (!enableResize) return
    nextTick(() => {
      const tableContainer = document.querySelector('.table-container')
      if (tableContainer) {
        calcTableMaxHeight(tableContainer)
        resizeObserver = new ResizeObserver(() => {
          calcTableMaxHeight(tableContainer)
        })
        resizeObserver.observe(tableContainer)
      }
      window.addEventListener('resize', onWindowResize)
    })
  }

  function onWindowResize() {
    const el = document.querySelector('.table-container')
    if (el) calcTableMaxHeight(el)
  }

  function unbindResizeObserver() {
    if (resizeObserver) {
      resizeObserver.disconnect()
      resizeObserver = null
    }
    window.removeEventListener('resize', onWindowResize)
  }

  // 数据获取
  async function fetchData(extraParams = {}) {
    loading.value = true
    try {
      const params = {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        ...extraParams
      }
      const res = await fetchApi(params)
      const data = transformResponse ? transformResponse(res.data) : res.data
      tableData.value = data.records || []
      total.value = data.total || 0
      selectedIds.value = []
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    page.value = 1
    fetchData()
  }

  function resetSearch() {
    keyword.value = ''
    page.value = 1
    fetchData()
  }

  function handlePageChange(val) {
    page.value = val
    fetchData()
  }

  function handleSizeChange(val) {
    size.value = val
    page.value = 1
    fetchData()
  }

  onMounted(() => {
    fetchData()
    bindResizeObserver()
  })

  onUnmounted(() => {
    unbindResizeObserver()
  })

  return {
    // 数据
    tableData, loading, keyword, page, size, total,
    // 排序
    sortedTableData, handleSortChange,
    // 选择
    selectedIds, handleSelectionChange,
    // 列配置
    visibleColumns, columnOptions, toggleColumn,
    // 数据获取
    fetchData, handleSearch, resetSearch,
    handlePageChange, handleSizeChange,
    // 表格高度
    tableMaxHeight, calcTableMaxHeight,
    // 分页配置
    pageSizes,
  }
}
