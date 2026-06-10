import { ref } from 'vue'

const refreshTick = ref(0)

export function useFavEvents() {
  function triggerRefresh() {
    refreshTick.value++
  }
  return { refreshTick, triggerRefresh }
}
