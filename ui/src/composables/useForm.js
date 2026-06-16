/**
 * 通用表单 Composable
 * 封装表单验证、重置、提交等通用逻辑
 *
 * @example
 * const {
 *   form, resetForm, validateForm, submitForm
 * } = useForm(initialData, {
 *   onSubmit: async (data) => await api.create(data),
 *   onSuccess: () => ElMessage.success('操作成功'),
 *   onError: (err) => ElMessage.error(err.message)
 * })
 */
import { reactive, ref } from 'vue'

export function useForm(initialData = {}, options = {}) {
  const {
    onSubmit = null,
    onSuccess = null,
    onError = null,
    beforeSubmit = null
  } = options

  const form = reactive({ ...initialData })
  const loading = ref(false)

  function resetForm(newData = initialData) {
    Object.keys(form).forEach(key => {
      form[key] = newData[key] !== undefined ? newData[key] : initialData[key] !== undefined ? initialData[key] : ''
    })
  }

  function setFormData(data) {
    if (!data) return
    Object.keys(data).forEach(key => {
      if (key in form) {
        form[key] = data[key]
      }
    })
  }

  async function validateForm(validateFn) {
    if (typeof validateFn === 'function') {
      return await validateFn()
    }
    return true
  }

  async function submitForm(validateFn) {
    loading.value = true
    try {
      const valid = await validateForm(validateFn)
      if (!valid) {
        loading.value = false
        return false
      }

      if (typeof beforeSubmit === 'function') {
        await beforeSubmit(form)
      }

      if (typeof onSubmit === 'function') {
        const result = await onSubmit({ ...form })
        if (typeof onSuccess === 'function') {
          await onSuccess(result)
        }
        return result
      }
      return true
    } catch (error) {
      if (typeof onError === 'function') {
        onError(error)
      }
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    form,
    loading,
    resetForm,
    setFormData,
    validateForm,
    submitForm
  }
}
