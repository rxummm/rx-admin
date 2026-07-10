<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button @click="$router.back()">{{ $t('common.back') }}</el-button>
      <el-button type="success" @click="showCreatePage">{{ $t('tool.wiki.createPage') }}</el-button>
    </div>
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card>
          <template #header><span>{{ $t('tool.wiki.pages') }}</span></template>
          <el-tree :data="pageTree" :props="{ label: 'title', children: 'children' }" @node-click="selectPage" />
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card v-if="currentPage">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ currentPage.title }}</span>
              <div>
                <el-button size="small" @click="savePage">{{ $t('common.save') }}</el-button>
              </div>
            </div>
          </template>
          <el-input v-model="currentPage.content" type="textarea" :rows="20" />
        </el-card>
        <el-empty v-else :description="$t('tool.wiki.selectPage')" />
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="$t('tool.wiki.createPage')" width="500px">
      <el-form :model="pageForm" label-width="100px">
        <el-form-item :label="$t('tool.wiki.title')"><el-input v-model="pageForm.title" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="createPage">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolWikiPage' })
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const route = useRoute()
const spaceId = ref(route.query.spaceId)
const pages = ref([])
const currentPage = ref(null)
const dialogVisible = ref(false)
const pageForm = ref({ title: '' })

const pageTree = computed(() => {
  const map = {}
  const roots = []
  pages.value.forEach(p => { map[p.id] = { ...p, children: [] } })
  pages.value.forEach(p => {
    if (p.parentId && map[p.parentId]) { map[p.parentId].children.push(map[p.id]) }
    else { roots.push(map[p.id]) }
  })
  return roots
})

const loadPages = async () => {
  const { data } = await request({ url: `/tool/wiki/spaces/${spaceId.value}/pages`, method: 'get' })
  pages.value = data || []
}

const selectPage = async (node) => {
  const { data } = await request({ url: `/tool/wiki/pages/${node.id}`, method: 'get' })
  currentPage.value = data
}

const showCreatePage = () => { pageForm.value = { title: '' }; dialogVisible.value = true }

const createPage = async () => {
  await request({ url: '/tool/wiki/pages', method: 'post', data: { spaceId: spaceId.value, title: pageForm.value.title } })
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadPages()
}

const savePage = async () => {
  await request({ url: `/tool/wiki/pages/${currentPage.value.id}`, method: 'put', data: currentPage.value })
  ElMessage.success(t('common.success'))
}

onMounted(loadPages)
</script>
