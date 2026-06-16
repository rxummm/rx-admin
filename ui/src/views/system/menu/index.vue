<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        :placeholder="$t('system.menu.menuName') + '/' + $t('system.menu.perm')"
        clearable
        style="width: 240px"
        @keyup.enter="fetchData"
      />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon> {{ $t('common.search') }}
      </el-button>
      <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
      <ExportButton :data="flattenedExportData" :columns="exportColumns" :title="$t('system.menu.title')" />
      <div style="flex: 1" />
      <el-button
        type="primary"
        @click="handleAdd({ parentId: 0, menuType: 1 })"
        v-if="userStore.hasPerm('sys:menu:add')"
      >
        <el-icon><Plus /></el-icon> {{ $t('common.add') + $t('system.menu.title') }}
      </el-button>
      <el-dropdown trigger="click" @command="toggleColumn">
        <el-button>
          <el-icon><Setting /></el-icon> {{ $t('common.columns') }}
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="col in columnOptions" :key="col.key" :command="col.key">
              <el-icon v-if="visibleColumns.includes(col.key)"><Check /></el-icon>
              <span :style="{ opacity: visibleColumns.includes(col.key) ? 1 : 0.4 }">{{ col.label }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="table-container">
      <el-table
        :data="filteredMenuTree"
        row-key="id"
        border
        stripe
        v-loading="loading"
        max-height="calc(100vh - 260px)"
        style="width: 100%"
      >
        <el-table-column
          v-if="visibleColumns.includes('menuName')"
          prop="menuName"
          :label="$t('system.menu.menuName')"
          min-width="200"
        />
        <el-table-column v-if="visibleColumns.includes('icon')" prop="icon" :label="$t('system.menu.icon')" width="80">
          <template #default="{ row }">
            <el-icon v-if="getElIconComponent(row.icon)"><component :is="getElIconComponent(row.icon)" /></el-icon>
            <FontAwesomeIcon
              v-else-if="getFaIcon(row.icon)"
              :icon="getFaIcon(row.icon)"
              style="width: 14px; height: 14px"
            />
          </template>
        </el-table-column>
        <el-table-column
          v-if="visibleColumns.includes('menuType')"
          prop="menuType"
          :label="$t('system.menu.type')"
          width="80"
        >
          <template #default="{ row }">
            <el-tag :type="row.menuType === 1 ? 'info' : row.menuType === 2 ? 'success' : 'warning'" size="small">
              {{
                row.menuType === 1
                  ? $t('system.menu.typeOptions.dir')
                  : row.menuType === 2
                    ? $t('system.menu.typeOptions.menu')
                    : $t('system.menu.typeOptions.button')
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="visibleColumns.includes('perms')"
          prop="perms"
          :label="$t('system.menu.perm')"
          width="160"
          show-overflow-tooltip
        />
        <el-table-column
          v-if="visibleColumns.includes('path')"
          prop="path"
          :label="$t('system.menu.path')"
          width="160"
          show-overflow-tooltip
        />
        <el-table-column
          v-if="visibleColumns.includes('component')"
          prop="component"
          :label="$t('system.menu.component')"
          width="200"
          show-overflow-tooltip
        />
        <el-table-column v-if="visibleColumns.includes('sort')" prop="sort" :label="$t('common.sort')" width="60" />
        <el-table-column v-if="visibleColumns.includes('status')" prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? $t('system.menu.statusOptions.enable') : $t('system.menu.statusOptions.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleAdd(row)"
              v-if="userStore.hasPerm('sys:menu:add') && row.menuType !== 3"
              >{{ $t('system.menu.addChild') }}</el-button
            >
            <el-button
              link
              type="primary"
              size="small"
              @click="handleEdit(row)"
              v-if="userStore.hasPerm('sys:menu:edit')"
              >{{ $t('common.edit') }}</el-button
            >
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
              v-if="userStore.hasPerm('sys:menu:delete')"
              >{{ $t('common.delete') }}</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item :label="$t('system.menu.type')">
          <el-radio-group v-model="form.menuType" :disabled="isEdit">
            <el-radio :value="1">{{ $t('system.menu.typeOptions.dir') }}</el-radio>
            <el-radio :value="2">{{ $t('system.menu.typeOptions.menu') }}</el-radio>
            <el-radio :value="3">{{ $t('system.menu.typeOptions.button') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('system.menu.parentMenu')">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTree"
            :props="{ label: 'menuName', value: 'id', children: 'children', disabled: (node) => node.menuType === 3 }"
            :placeholder="$t('common.select') + $t('system.menu.parentMenu')"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('system.menu.menuName')" prop="menuName">
          <el-input v-model="form.menuName" :placeholder="$t('common.input') + $t('system.menu.menuName')" />
        </el-form-item>
        <el-form-item :label="$t('system.menu.icon')" v-if="form.menuType !== 3">
          <div class="icon-picker-wrapper">
            <el-input
              v-model="form.icon"
              :placeholder="$t('common.input') + $t('system.menu.icon') + ' / ' + t('system.menu.iconPicker')"
            >
              <template #prefix>
                <el-icon v-if="getElIconComponent(form.icon)"
                  ><component :is="getElIconComponent(form.icon)"
                /></el-icon>
                <FontAwesomeIcon
                  v-else-if="form.icon && isFontAwesomeIcon(form.icon)"
                  :icon="getFaIcons(form.icon)"
                  style="font-size: 14px"
                />
                <el-icon v-else><Grid /></el-icon>
              </template>
            </el-input>
            <el-popover trigger="click" placement="bottom-start" :width="520" popper-class="icon-picker-popper">
              <template #reference>
                <el-button class="icon-picker-btn"
                  ><el-icon><MoreFilled /></el-icon
                ></el-button>
              </template>
              <div class="icon-picker-panel">
                <!-- Tab 切换 -->
                <div class="icon-picker-tabs">
                  <el-radio-group v-model="iconTab" size="small">
                    <el-radio-button value="ep">Element Plus</el-radio-button>
                    <el-radio-button value="fa">Font Awesome</el-radio-button>
                  </el-radio-group>
                  <el-input v-model="iconSearch" placeholder="搜索..." clearable size="small" style="width: 160px" />
                </div>
                <!-- Element Plus 图标列表 -->
                <div v-show="iconTab === 'ep'" class="icon-picker-list">
                  <div
                    v-for="name in filteredEpIcons"
                    :key="name"
                    :class="['icon-item', { active: form.icon === name }]"
                    @click="selectIcon(name)"
                  >
                    <el-icon :size="18"><component :is="getElIconComponent(name) || Grid" /></el-icon>
                    <span class="icon-name">{{ name }}</span>
                  </div>
                  <div v-if="filteredEpIcons.length === 0" class="icon-empty">无匹配图标</div>
                </div>
                <!-- Font Awesome 图标列表 -->
                <div v-show="iconTab === 'fa'" class="icon-picker-list">
                  <div
                    v-for="item in filteredFaIcons"
                    :key="item.name"
                    :class="['icon-item', { active: form.icon === item.fullName }]"
                    @click="selectIcon(item.fullName)"
                  >
                    <FontAwesomeIcon :icon="item.iconObj" :style="{ fontSize: '16px' }" />
                    <span class="icon-name">{{ item.name }}</span>
                  </div>
                  <div v-if="filteredFaIcons.length === 0" class="icon-empty">无匹配图标</div>
                </div>
              </div>
            </el-popover>
          </div>
        </el-form-item>
        <el-form-item :label="$t('system.menu.path')" v-if="form.menuType !== 3">
          <el-input v-model="form.path" :placeholder="$t('common.input') + $t('system.menu.path')" />
        </el-form-item>
        <el-form-item :label="$t('system.menu.component')" v-if="form.menuType === 2">
          <el-input v-model="form.component" :placeholder="$t('common.input') + $t('system.menu.component')" />
        </el-form-item>
        <el-form-item :label="$t('system.menu.perm')" v-if="form.menuType !== 1">
          <el-input v-model="form.perms" :placeholder="$t('common.input') + $t('system.menu.perm')" />
        </el-form-item>
        <el-form-item :label="$t('common.sort')">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'SystemMenu' })
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import {
  faHome,
  faUser,
  faUsers,
  faGear,
  faCog,
  faTools,
  faChartBar,
  faChartLine,
  faFile,
  faFolder,
  faFolderOpen,
  faTag,
  faBookmark,
  faStar,
  faHeart,
  faBell,
  faMessage,
  faEnvelope,
  faPaperPlane,
  faShareNodes,
  faLock,
  faKey,
  faShield,
  faEye,
  faEyeSlash,
  faPlus,
  faMinus,
  faXmark,
  faCheck,
  faEdit,
  faTrash,
  faSearch,
  faRefresh,
  faArrowRight,
  faArrowLeft,
  faArrowUp,
  faArrowDown,
  faRotateRight,
  faGlobe,
  faExpand,
  faCompress,
  faMaximize,
  faMinimize,
  faCalendar,
  faClock,
  faStopwatch,
  faDatabase,
  faServer,
  faCloud,
  faCode,
  faTerminal,
  faBug,
  faDesktop,
  faLaptop,
  faMobileScreen,
  faWifi,
  faPlug,
  faPuzzlePiece,
  faCircleInfo,
  faTriangleExclamation,
  faCircleCheck,
  faCircleXmark,
  faList,
  faTable,
  faGrip,
  faThLarge,
  faSquare,
  faLayerGroup,
  faCamera,
  faImage,
  faFilm,
  faMicrophone,
  faMusic,
  faMapLocationDot,
  faLocationPin,
  faCompass,
  faBook,
  faBookOpen,
  faGraduationCap,
  faPenNib,
  faBasketShopping,
  faCartShopping,
  faCreditCard,
  faDollarSign,
  faRocket,
  faBolt,
  faFire,
  faAward,
  faTrophy,
  faQuestion,
  faLightbulb,
  faWandMagicSparkles
} from '@fortawesome/free-solid-svg-icons'
import { useUserStore } from '@/stores/user'
import { getMenuTreeApi, addMenuApi, updateMenuApi, deleteMenuApi } from '@/api/menu'
import ExportButton from '@/components/ExportButton/index.vue'

// 注册 FA 图标到库
library.add(
  faHome,
  faUser,
  faUsers,
  faGear,
  faCog,
  faTools,
  faChartBar,
  faChartLine,
  faFile,
  faFolder,
  faFolderOpen,
  faTag,
  faBookmark,
  faStar,
  faHeart,
  faBell,
  faMessage,
  faEnvelope,
  faPaperPlane,
  faShareNodes,
  faLock,
  faKey,
  faShield,
  faEye,
  faEyeSlash,
  faPlus,
  faMinus,
  faXmark,
  faCheck,
  faEdit,
  faTrash,
  faSearch,
  faRefresh,
  faArrowRight,
  faArrowLeft,
  faArrowUp,
  faArrowDown,
  faRotateRight,
  faGlobe,
  faExpand,
  faCompress,
  faMaximize,
  faMinimize,
  faCalendar,
  faClock,
  faStopwatch,
  faDatabase,
  faServer,
  faCloud,
  faCode,
  faTerminal,
  faBug,
  faDesktop,
  faLaptop,
  faMobileScreen,
  faWifi,
  faPlug,
  faPuzzlePiece,
  faCircleInfo,
  faTriangleExclamation,
  faCircleCheck,
  faCircleXmark,
  faList,
  faTable,
  faGrip,
  faThLarge,
  faSquare,
  faLayerGroup,
  faCamera,
  faImage,
  faFilm,
  faMicrophone,
  faMusic,
  faMapLocationDot,
  faLocationPin,
  faCompass,
  faBook,
  faBookOpen,
  faGraduationCap,
  faPenNib,
  faBasketShopping,
  faCartShopping,
  faCreditCard,
  faDollarSign,
  faRocket,
  faBolt,
  faFire,
  faAward,
  faTrophy,
  faQuestion,
  faLightbulb,
  faWandMagicSparkles
)

const { t } = useI18n()
const userStore = useUserStore()

const menuTree = ref([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref(t('common.add') + t('system.menu.title'))
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// 列显示配置
const columnOptions = [
  { key: 'menuName', label: t('system.menu.menuName') },
  { key: 'icon', label: t('system.menu.icon') },
  { key: 'menuType', label: t('system.menu.type') },
  { key: 'perms', label: t('system.menu.perm') },
  { key: 'path', label: t('system.menu.path') },
  { key: 'component', label: t('system.menu.component') },
  { key: 'sort', label: t('common.sort') },
  { key: 'status', label: t('common.status') }
]
const visibleColumns = ref(columnOptions.map((c) => c.key))

function toggleColumn(key) {
  const idx = visibleColumns.value.indexOf(key)
  if (idx > -1) {
    visibleColumns.value.splice(idx, 1)
  } else {
    visibleColumns.value.push(key)
  }
}

// 搜索过滤树结构：保留匹配节点及其祖先
const filteredMenuTree = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return menuTree.value
  return filterTree(menuTree.value, kw)
})

function getElIconComponent(icon) {
  if (!icon || icon.startsWith('fa-')) return null
  return ElementPlusIconsVue[icon] || null
}

function getFaIcon(icon) {
  if (!icon || !icon.startsWith('fa-')) return null
  const parts = icon.split(' ')
  return parts.length > 1 ? parts[1].replace(/^fa-/, '') : null
}

function filterTree(nodes, kw) {
  return nodes.reduce((acc, node) => {
    const nameMatch = node.menuName && node.menuName.toLowerCase().includes(kw)
    const permMatch = node.perms && node.perms.toLowerCase().includes(kw)
    const filteredChildren = node.children ? filterTree(node.children, kw) : []
    if (nameMatch || permMatch || filteredChildren.length > 0) {
      acc.push({ ...node, children: filteredChildren })
    }
    return acc
  }, [])
}

function resetSearch() {
  keyword.value = ''
}

// 导出列定义
const exportColumns = [
  { field: 'menuName', label: t('system.menu.menuName') },
  { field: 'icon', label: t('system.menu.icon') },
  { field: 'menuType', label: t('system.menu.type') },
  { field: 'perms', label: t('system.menu.perm') },
  { field: 'path', label: t('system.menu.path') },
  { field: 'component', label: t('system.menu.component') },
  { field: 'sort', label: t('common.sort') },
  { field: 'status', label: t('common.status') }
]

// 将过滤后的树展平为导出数据
const flattenedExportData = computed(() => {
  const result = []
  flattenTree(filteredMenuTree.value, '', result)
  return result
})

function flattenTree(nodes, prefix, result) {
  for (const node of nodes) {
    const typeMap = {
      1: t('system.menu.typeOptions.dir'),
      2: t('system.menu.typeOptions.menu'),
      3: t('system.menu.typeOptions.button')
    }
    result.push({
      ...node,
      menuName: prefix + node.menuName,
      menuType: typeMap[node.menuType] || node.menuType,
      status: node.status === 1 ? t('common.enable') : t('common.disable')
    })
    if (node.children && node.children.length > 0) {
      flattenTree(node.children, prefix + '  ├─ ', result)
    }
  }
}

// ====== 图标选择器 ======
const iconTab = ref('ep')
const iconSearch = ref('')

// 所有 Element Plus 图标名（排序后的列表）
const allEpIcons = Object.keys(ElementPlusIconsVue)
  .filter((name) => name !== 'Icon' && name !== 'IconProps') // 过滤掉非图标组件
  .sort()

// Font Awesome 图标定义：{ name: 显示名, fullName: 'fa-solid 名称' (存后端), iconObj: 图标对象 (用于渲染) }
const allFaIcons = [
  { name: 'home', fullName: 'fa-solid fa-home', iconObj: faHome },
  { name: 'user', fullName: 'fa-solid fa-user', iconObj: faUser },
  { name: 'users', fullName: 'fa-solid fa-users', iconObj: faUsers },
  { name: 'gear', fullName: 'fa-solid fa-gear', iconObj: faGear },
  { name: 'cog', fullName: 'fa-solid fa-cog', iconObj: faCog },
  { name: 'tools', fullName: 'fa-solid fa-tools', iconObj: faTools },
  { name: 'chart-bar', fullName: 'fa-solid fa-chart-bar', iconObj: faChartBar },
  { name: 'chart-line', fullName: 'fa-solid fa-chart-line', iconObj: faChartLine },
  { name: 'file', fullName: 'fa-solid fa-file', iconObj: faFile },
  { name: 'folder', fullName: 'fa-solid fa-folder', iconObj: faFolder },
  { name: 'folder-open', fullName: 'fa-solid fa-folder-open', iconObj: faFolderOpen },
  { name: 'tag', fullName: 'fa-solid fa-tag', iconObj: faTag },
  { name: 'bookmark', fullName: 'fa-solid fa-bookmark', iconObj: faBookmark },
  { name: 'star', fullName: 'fa-solid fa-star', iconObj: faStar },
  { name: 'heart', fullName: 'fa-solid fa-heart', iconObj: faHeart },
  { name: 'bell', fullName: 'fa-solid fa-bell', iconObj: faBell },
  { name: 'message', fullName: 'fa-solid fa-message', iconObj: faMessage },
  { name: 'envelope', fullName: 'fa-solid fa-envelope', iconObj: faEnvelope },
  { name: 'paper-plane', fullName: 'fa-solid fa-paper-plane', iconObj: faPaperPlane },
  { name: 'share', fullName: 'fa-solid fa-share-nodes', iconObj: faShareNodes },
  { name: 'lock', fullName: 'fa-solid fa-lock', iconObj: faLock },
  { name: 'key', fullName: 'fa-solid fa-key', iconObj: faKey },
  { name: 'shield', fullName: 'fa-solid fa-shield', iconObj: faShield },
  { name: 'eye', fullName: 'fa-solid fa-eye', iconObj: faEye },
  { name: 'eye-slash', fullName: 'fa-solid fa-eye-slash', iconObj: faEyeSlash },
  { name: 'plus', fullName: 'fa-solid fa-plus', iconObj: faPlus },
  { name: 'minus', fullName: 'fa-solid fa-minus', iconObj: faMinus },
  { name: 'xmark', fullName: 'fa-solid fa-xmark', iconObj: faXmark },
  { name: 'check', fullName: 'fa-solid fa-check', iconObj: faCheck },
  { name: 'edit', fullName: 'fa-solid fa-edit', iconObj: faEdit },
  { name: 'trash', fullName: 'fa-solid fa-trash', iconObj: faTrash },
  { name: 'search', fullName: 'fa-solid fa-search', iconObj: faSearch },
  { name: 'refresh', fullName: 'fa-solid fa-refresh', iconObj: faRefresh },
  { name: 'arrow-right', fullName: 'fa-solid fa-arrow-right', iconObj: faArrowRight },
  { name: 'arrow-left', fullName: 'fa-solid fa-arrow-left', iconObj: faArrowLeft },
  { name: 'arrow-up', fullName: 'fa-solid fa-arrow-up', iconObj: faArrowUp },
  { name: 'arrow-down', fullName: 'fa-solid fa-arrow-down', iconObj: faArrowDown },
  { name: 'globe', fullName: 'fa-solid fa-globe', iconObj: faGlobe },
  { name: 'expand', fullName: 'fa-solid fa-expand', iconObj: faExpand },
  { name: 'compress', fullName: 'fa-solid fa-compress', iconObj: faCompress },
  { name: 'calendar', fullName: 'fa-solid fa-calendar', iconObj: faCalendar },
  { name: 'clock', fullName: 'fa-solid fa-clock', iconObj: faClock },
  { name: 'database', fullName: 'fa-solid fa-database', iconObj: faDatabase },
  { name: 'server', fullName: 'fa-solid fa-server', iconObj: faServer },
  { name: 'cloud', fullName: 'fa-solid fa-cloud', iconObj: faCloud },
  { name: 'code', fullName: 'fa-solid fa-code', iconObj: faCode },
  { name: 'terminal', fullName: 'fa-solid fa-terminal', iconObj: faTerminal },
  { name: 'bug', fullName: 'fa-solid fa-bug', iconObj: faBug },
  { name: 'monitor', fullName: 'fa-solid fa-desktop', iconObj: faDesktop },
  { name: 'laptop', fullName: 'fa-solid fa-laptop', iconObj: faLaptop },
  { name: 'mobile', fullName: 'fa-solid fa-mobile-screen', iconObj: faMobileScreen },
  { name: 'wifi', fullName: 'fa-solid fa-wifi', iconObj: faWifi },
  { name: 'plug', fullName: 'fa-solid fa-plug', iconObj: faPlug },
  { name: 'info', fullName: 'fa-solid fa-circle-info', iconObj: faCircleInfo },
  { name: 'warning', fullName: 'fa-solid fa-triangle-exclamation', iconObj: faTriangleExclamation },
  { name: 'check-circle', fullName: 'fa-solid fa-circle-check', iconObj: faCircleCheck },
  { name: 'xmark-circle', fullName: 'fa-solid fa-circle-xmark', iconObj: faCircleXmark },
  { name: 'list', fullName: 'fa-solid fa-list', iconObj: faList },
  { name: 'table', fullName: 'fa-solid fa-table', iconObj: faTable },
  { name: 'grid', fullName: 'fa-solid fa-grip', iconObj: faGrip },
  { name: 'th-large', fullName: 'fa-solid fa-th-large', iconObj: faThLarge },
  { name: 'layer-group', fullName: 'fa-solid fa-layer-group', iconObj: faLayerGroup },
  { name: 'camera', fullName: 'fa-solid fa-camera', iconObj: faCamera },
  { name: 'image', fullName: 'fa-solid fa-image', iconObj: faImage },
  { name: 'film', fullName: 'fa-solid fa-film', iconObj: faFilm },
  { name: 'map-pin', fullName: 'fa-solid fa-map-location-dot', iconObj: faMapLocationDot },
  { name: 'location', fullName: 'fa-solid fa-location-pin', iconObj: faLocationPin },
  { name: 'compass', fullName: 'fa-solid fa-compass', iconObj: faCompass },
  { name: 'book', fullName: 'fa-solid fa-book', iconObj: faBook },
  { name: 'book-open', fullName: 'fa-solid fa-book-open', iconObj: faBookOpen },
  { name: 'graduation-cap', fullName: 'fa-solid fa-graduation-cap', iconObj: faGraduationCap },
  { name: 'pen-nib', fullName: 'fa-solid fa-pen-nib', iconObj: faPenNib },
  { name: 'cart', fullName: 'fa-solid fa-cart-shopping', iconObj: faBasketShopping },
  { name: 'credit-card', fullName: 'fa-solid fa-credit-card', iconObj: faCreditCard },
  { name: 'dollar', fullName: 'fa-solid fa-dollar-sign', iconObj: faDollarSign },
  { name: 'rocket', fullName: 'fa-solid fa-rocket', iconObj: faRocket },
  { name: 'bolt', fullName: 'fa-solid fa-bolt', iconObj: faBolt },
  { name: 'fire', fullName: 'fa-solid fa-fire', iconObj: faFire },
  { name: 'award', fullName: 'fa-solid fa-award', iconObj: faAward },
  { name: 'trophy', fullName: 'fa-solid fa-trophy', iconObj: faTrophy },
  { name: 'question', fullName: 'fa-solid fa-question', iconObj: faQuestion },
  { name: 'lightbulb', fullName: 'fa-solid fa-lightbulb', iconObj: faLightbulb },
  { name: 'wand', fullName: 'fa-solid fa-wand-magic-sparkles', iconObj: faWandMagicSparkles }
]

const filteredEpIcons = computed(() => {
  if (!iconSearch.value) return allEpIcons
  const kw = iconSearch.value.toLowerCase()
  return allEpIcons.filter((n) => n.toLowerCase().includes(kw))
})

const filteredFaIcons = computed(() => {
  if (!iconSearch.value) return allFaIcons
  const kw = iconSearch.value.toLowerCase()
  return allFaIcons.filter((item) => item.name.includes(kw) || item.fullName.includes(kw))
})

function selectIcon(name) {
  form.icon = name
}

function isElementIcon(name) {
  return name && !name.startsWith('fa-')
}

function isFontAwesomeIcon(name) {
  return name && (name.startsWith('fa-solid ') || name.startsWith('fa-regular ') || name.startsWith('fa-brands '))
}

// 根据 fullName 字符串查找对应的图标对象（用于输入框前缀预览）
const faIconMap = Object.fromEntries(allFaIcons.map((item) => [item.fullName, item.iconObj]))
function getFaIcons(fullName) {
  return faIconMap[fullName] || null
}

const form = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  menuType: 1,
  path: '',
  component: '',
  perms: '',
  icon: '',
  sort: 0,
  visible: 1,
  status: 1
})

const formRules = {
  menuName: [{ required: true, message: t('common.input') + t('system.menu.menuName'), trigger: 'blur' }]
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getMenuTreeApi()
    menuTree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd(row) {
  isEdit.value = false
  dialogTitle.value = t('common.add') + t('system.menu.title')
  resetForm()
  form.parentId = row.id || 0
  form.menuType = row.menuType ? row.menuType : 1
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = t('common.edit') + t('system.menu.title')
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId,
    menuName: row.menuName,
    menuType: row.menuType,
    path: row.path || '',
    component: row.component || '',
    perms: row.perms || '',
    icon: row.icon || '',
    sort: row.sort,
    visible: row.visible,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('system.menu.deleteConfirm', { name: row.menuName }), t('common.tip'), {
      type: 'warning'
    })
    await deleteMenuApi(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchData()
    await userStore.refreshRouters()
  } catch {}
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateMenuApi({ ...form })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await addMenuApi({ ...form })
      ElMessage.success(t('common.addSuccess'))
    }
    dialogVisible.value = false
    fetchData()
    await userStore.refreshRouters()
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  form.id = null
  form.parentId = 0
  form.menuName = ''
  form.menuType = 1
  form.path = ''
  form.component = ''
  form.perms = ''
  form.icon = ''
  form.sort = 0
  form.visible = 1
  form.status = 1
}
</script>

<style lang="scss" scoped>
.icon-picker-wrapper {
  display: flex;
  gap: 8px;
  width: 100%;
  align-items: center;

  :deep(.el-input) {
    flex: 1;
  }
}

.icon-picker-btn {
  padding: 8px 10px;
  flex-shrink: 0;
}
</style>
