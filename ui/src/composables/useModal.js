/**
 * 通用弹窗 Composable
 * 封装弹窗的打开、关闭、表单重置等通用逻辑
 *
 * @example
 * const {
 *   visible, openModal, closeModal, resetAndOpen
 * } = useModal({
 *   onOpen: () => { },
 *   onClose: () => { }
 * })
 */
import { ref } from 'vue'

export function useModal(options = {}) {
  const { onOpen = null, onClose = null, onBeforeOpen = null, onBeforeClose = null } = options

  const visible = ref(false)

  async function openModal(data) {
    if (typeof onBeforeOpen === 'function') {
      const proceed = await onBeforeOpen(data)
      if (proceed === false) return
    }
    visible.value = true
    if (typeof onOpen === 'function') {
      onOpen(data)
    }
  }

  async function closeModal() {
    if (typeof onBeforeClose === 'function') {
      const proceed = await onBeforeClose()
      if (proceed === false) return
    }
    visible.value = false
    if (typeof onClose === 'function') {
      onClose()
    }
  }

  function resetAndOpen(data) {
    visible.value = false
    setTimeout(() => {
      openModal(data)
    }, 300)
  }

  return {
    visible,
    openModal,
    closeModal,
    resetAndOpen
  }
}

/**
 * 通用对话框 Composable（带确认功能）
 *
 * @example
 * const {
 *   showDialog, openDialog, confirmDialog, closeDialog
 * } = useDialog({
 *   onConfirm: async () => await api.delete(id),
 *   confirmText: '确定删除?'
 * })
 */
export function useDialog(options = {}) {
  const { onConfirm = null, onCancel = null, confirmText = '确定', cancelText = '取消' } = options

  const showDialog = ref(false)
  const loading = ref(false)
  const message = ref('')

  async function openDialog(msg) {
    message.value = msg || ''
    showDialog.value = true
  }

  async function confirmDialog() {
    loading.value = true
    try {
      if (typeof onConfirm === 'function') {
        await onConfirm()
      }
      showDialog.value = false
    } finally {
      loading.value = false
    }
  }

  function closeDialog() {
    showDialog.value = false
    if (typeof onCancel === 'function') {
      onCancel()
    }
  }

  return {
    showDialog,
    loading,
    message,
    confirmText,
    cancelText,
    openDialog,
    confirmDialog,
    closeDialog
  }
}
