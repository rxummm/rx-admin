import { ref } from 'vue'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'

const tokenStore = useStorage(STORAGE_KEYS.TOKEN)

let ws = null
let reconnectTimer = null
let heartbeatTimer = null
const connected = ref(false)
let connectCount = 0
const listeners = new Map()

export function useNotificationWebSocket() {
  function connect() {
    connectCount++
    if (ws && ws.readyState <= window.WebSocket.OPEN) return

    try {
      const token = tokenStore.get()
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = window.location.host
      const url = `${protocol}//${host}/api/v1/ws/notification?token=${encodeURIComponent(token || '')}`

      ws = new window.WebSocket(url)

      ws.onopen = () => {
        connected.value = true
        startHeartbeat()
        reconnectTimer && clearTimeout(reconnectTimer)
        reconnectTimer = null
      }

      ws.onmessage = (event) => {
        try {
          const msg = JSON.parse(event.data)
          if (msg.event === 'pong') return
          const handler = listeners.get(msg.event)
          if (handler) {
            const data = typeof msg.data === 'string' ? JSON.parse(msg.data) : msg.data
            handler(data)
          }
        } catch {
          // ignore parse errors
        }
      }

      ws.onclose = () => {
        connected.value = false
        stopHeartbeat()
        scheduleReconnect()
      }

      ws.onerror = () => {
        connected.value = false
      }
    } catch {
      connected.value = false
      scheduleReconnect()
    }
  }

  function disconnect() {
    connectCount = Math.max(0, connectCount - 1)
    if (connectCount > 0) return
    stopHeartbeat()
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      ws.close()
      ws = null
    }
    connected.value = false
  }

  function on(eventName, handler) {
    listeners.set(eventName, handler)
  }

  function off(eventName) {
    listeners.delete(eventName)
  }

  function startHeartbeat() {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => {
      if (ws && ws.readyState === window.WebSocket.OPEN) {
        ws.send('ping')
      }
    }, 30000)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  function scheduleReconnect() {
    if (reconnectTimer || connectCount <= 0) return
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      if (connectCount > 0) {
        connect()
      }
    }, 3000)
  }

  return { connect, disconnect, on, off, connected }
}
