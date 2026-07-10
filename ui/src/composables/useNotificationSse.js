import { ref } from 'vue'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

const tokenStore = useStorage(STORAGE_KEYS.TOKEN)

let eventSource = null
let connectCount = 0
const connected = ref(false)

export function useNotificationSse() {
  function connect() {
    connectCount++
    if (eventSource) return
    try {
      const token = tokenStore.get()
      const url = token
        ? '/api/v1/notification/stream?Authorization=' + encodeURIComponent(token)
        : '/api/v1/notification/stream'
      eventSource = new EventSource(url)
      eventSource.onopen = () => {
        connected.value = true
      }
      eventSource.onerror = () => {
        connected.value = false
      }
    } catch (e) {
      console.warn('SSE init error:', e)
    }
  }

  function disconnect() {
    connectCount = Math.max(0, connectCount - 1)
    if (connectCount > 0) return
    if (eventSource) {
      eventSource.close()
      eventSource = null
      connected.value = false
    }
  }

  function on(eventName, handler) {
    if (!eventSource) return
    eventSource.addEventListener(eventName, (event) => {
      try {
        const parsed = JSON.parse(event.data)
        handler(parsed)
      } catch (e) {
        console.warn('SSE parse error for', eventName, e)
      }
    })
  }

  return { connect, disconnect, on, connected }
}
