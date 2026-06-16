<template>
  <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.path">
    <template #title>
      <el-icon><component :is="menu.icon" /></el-icon>
      <span>{{ tMenu(menu.menuName) }}</span>
    </template>
    <SubMenu v-for="child in menu.children" :key="child.id" :menu="child" />
  </el-sub-menu>
  <el-menu-item v-else :index="menu.path">
    <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
    <span>{{ tMenu(menu.menuName) }}</span>
  </el-menu-item>
</template>

<script setup>
import { useMenuI18n } from '@/composables/useMenuI18n'

defineOptions({ name: 'SubMenu' })
defineProps({
  menu: { type: Object, required: true }
})

const { tMenu } = useMenuI18n()
</script>
