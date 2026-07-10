<template>
  <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.path">
    <template #title>
      <el-icon v-if="elIconComponent">
        <component :is="elIconComponent" />
      </el-icon>
      <FontAwesomeIcon v-else-if="faIcon" :icon="faIcon" style="width: 14px; height: 14px" />
      <el-icon v-else><Menu /></el-icon>
      <span>{{ tMenu(menu.menuName) }}</span>
    </template>
    <SubMenu v-for="child in menu.children" :key="child.id" :menu="child" />
  </el-sub-menu>
  <el-menu-item v-else :index="menu.path">
    <el-icon v-if="elIconComponent">
      <component :is="elIconComponent" />
    </el-icon>
    <FontAwesomeIcon v-else-if="faIcon" :icon="faIcon" style="width: 14px; height: 14px" />
    <el-icon v-else><Menu /></el-icon>
    <span>{{ tMenu(menu.menuName) }}</span>
  </el-menu-item>
</template>

<script setup>
import { computed } from 'vue'
import { useMenuI18n } from '@/composables/useMenuI18n'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

defineOptions({ name: 'SubMenu' })
const props = defineProps({
  menu: { type: Object, required: true }
})

const { tMenu } = useMenuI18n()

const elIconComponent = computed(() => {
  const icon = props.menu.icon
  if (!icon || icon.startsWith('fa-')) return null
  const component = ElementPlusIconsVue[icon]
  return component || ElementPlusIconsVue.Menu
})

const faIcon = computed(() => {
  const icon = props.menu.icon
  if (!icon || !icon.startsWith('fa-')) return null
  const parts = icon.split(' ')
  return parts.length > 1 ? parts[1].replace(/^fa-/, '') : null
})
</script>
