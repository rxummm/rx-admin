/**
 * 通知广播 —— 当其他组件（如审批页）操作完成后，
 * 通知 NoticePopover 立即刷新，无需等待下一次轮询。
 */
import { ref } from 'vue'

const refreshCounter = ref(0)

export function useNoticeBroadcast() {
  /** 触发刷新 */
  function triggerRefresh() {
    refreshCounter.value++
  }

  return { refreshCounter, triggerRefresh }
}
