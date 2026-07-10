/**
 * 经典文学页面表格高度自适应 Composable
 * 所有 classics/* 页面共享此模块，避免硬编码重复
 *
 * 可通过 .env 环境变量覆盖默认值：
 *   VITE_CLASSICS_TABLE_ROW_HEIGHT     — 行高（默认 44）
 *   VITE_CLASSICS_TABLE_HEADER_HEIGHT  — 表头高度（默认 40）
 *   VITE_CLASSICS_TABLE_MAX_ROWS       — 默认最大可见行数（默认 16）
 *   VITE_CLASSICS_TABLE_PAGINATION_H   — 分页器高度（默认 44）
 *
 * @param {string} wrapperClass CSS 选择器，用于定位表格容器
 * @param {number|null} maxVisibleRows 覆盖最大行数（null 则使用环境变量默认值）
 * @returns {{ tableMaxHeight, calcTableMaxHeight }}
 *
 * @example
 * import { useTableHeight } from '@/composables/useTableHeight'
 * const { tableMaxHeight, calcTableMaxHeight } = useTableHeight('.honglou-table-wrapper')
 */
import { ref, nextTick } from 'vue'

const ROW_HEIGHT = Number(import.meta.env.VITE_CLASSICS_TABLE_ROW_HEIGHT) || 44
const HEADER_HEIGHT = Number(import.meta.env.VITE_CLASSICS_TABLE_HEADER_HEIGHT) || 40
const DEFAULT_MAX_ROWS = Number(import.meta.env.VITE_CLASSICS_TABLE_MAX_ROWS) || 20
const PAGINATION_HEIGHT = Number(import.meta.env.VITE_CLASSICS_TABLE_PAGINATION_H) || 44

export function useTableHeight(wrapperClass = '.table-wrapper', maxVisibleRows = null) {
  const maxRows = maxVisibleRows != null ? maxVisibleRows : DEFAULT_MAX_ROWS
  const tableMaxHeight = ref(0)

  function computeHeight() {
    const wrapper = document.querySelector(wrapperClass)
    const wrapperHeight = wrapper ? wrapper.clientHeight : 0
    const contentHeight = maxRows * ROW_HEIGHT + HEADER_HEIGHT
    const availableHeight = wrapperHeight - PAGINATION_HEIGHT
    if (wrapperHeight > 0 && availableHeight > 0) {
      tableMaxHeight.value = Math.max(200, Math.min(availableHeight, contentHeight))
    } else {
      tableMaxHeight.value = contentHeight
    }
  }

  function calcTableMaxHeight() {
    // 延迟到下一个 tick 和 animation frame，确保父级 flex 布局已完成高度计算
    nextTick(computeHeight)
    requestAnimationFrame(computeHeight)
  }

  return { tableMaxHeight, calcTableMaxHeight }
}
