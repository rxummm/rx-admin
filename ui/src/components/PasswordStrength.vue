<template>
  <div class="password-strength" v-if="password">
    <div class="strength-bar">
      <div
        class="strength-bar-inner"
        :style="{ width: strength.percent + '%', backgroundColor: strength.color, transition: 'all 0.3s ease' }"
      />
    </div>
    <span class="strength-label" :style="{ color: strength.color }" v-if="strength.label">
      {{ $t(`profile.passwordStrength.${strength.label}`) }}
    </span>
  </div>
</template>

<script setup>
defineOptions({ name: 'PasswordStrength' })
import { computed } from 'vue'
import { checkPasswordStrength } from '@/composables/usePasswordStrength'

const props = defineProps({
  password: { type: String, default: '' }
})

const strength = computed(() => checkPasswordStrength(props.password))
</script>

<style scoped>
.password-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}
.strength-bar {
  flex: 1;
  height: 4px;
  background-color: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
}
.strength-bar-inner {
  height: 100%;
  border-radius: 2px;
}
.strength-label {
  font-size: 12px;
  white-space: nowrap;
}
</style>
