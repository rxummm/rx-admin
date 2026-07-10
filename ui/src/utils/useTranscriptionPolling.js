import { ref } from 'vue'
import request from '@/utils/request'

export function useTranscriptionPolling() {
  const polling = ref(false)
  let stopFlag = false
  let timer = null

  function startPolling(id, baseUrl, { interval = 2000, timeout = 600000 } = {}) {
    polling.value = true
    stopFlag = false
    const startTime = Date.now()

    return new Promise((resolve, reject) => {
      function poll() {
        if (stopFlag) return

        if (Date.now() - startTime > timeout) {
          polling.value = false
          reject(new Error('转写超时'))
          return
        }

        request({
          url: `/${baseUrl}/${id}`,
          method: 'get',
          _skipNProgress: true
        })
          .then((res) => {
            if (stopFlag) return
            const record = res.data
            if (record.status === 1) {
              polling.value = false
              resolve(record)
            } else if (record.status === 0) {
              polling.value = false
              reject(new Error(record.errorMessage || '转写失败'))
            } else {
              timer = setTimeout(poll, interval)
            }
          })
          .catch((err) => {
            polling.value = false
            reject(err)
          })
      }
      poll()
    })
  }

  function stopPolling() {
    stopFlag = true
    polling.value = false
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return { polling, startPolling, stopPolling }
}
