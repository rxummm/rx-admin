<template>
  <el-config-provider :locale="elCurrentLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

const { locale } = useI18n()
const elLocaleMap = { 'zh-CN': zhCn, 'en-US': en }
const elCurrentLocale = computed(() => elLocaleMap[locale.value] || zhCn)
</script>
