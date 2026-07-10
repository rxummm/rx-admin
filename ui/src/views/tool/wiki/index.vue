<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="showCreateSpace">{{ $t('tool.wiki.createSpace') }}</el-button>
    </div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="space in spaces" :key="space.id">
        <el-card shadow="hover" style="cursor: pointer" @click="openSpace(space)">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ space.name }}</span>
              <el-popconfirm :title="$t('common.confirmDelete')" @confirm.stop="deleteSpace(space.id)">
                <template #reference>
                  <el-button size="small" type="danger" @click.stop>{{ $t('common.delete') }}</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
          <p>{{ space.description }}</p>
          <el-tag size="small">{{ space.visibility }}</el-tag>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="$t('tool.wiki.createSpace')" width="500px">
      <el-form :model="spaceForm" label-width="100px">
        <el-form-item :label="$t('tool.wiki.name')"><el-input v-model="spaceForm.name" /></el-form-item>
        <el-form-item :label="$t('tool.wiki.description')"><el-input v-model="spaceForm.description" type="textarea" /></el-form-item>
        <el-form-item :label="$t('tool.wiki.visibility')">
          <el-select v-model="spaceForm.visibility">
            <el-option label="公开" value="PUBLIC" />
            <el-option label="私有" value="PRIVATE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="createSpace">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolWiki' })
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const router = useRouter()
const spaces = ref([])
const dialogVisible = ref(false)
const spaceForm = ref({ name: '', description: '', visibility: 'PUBLIC' })

const loadSpaces = async () => {
  const { data } = await request({ url: '/tool/wiki/spaces', method: 'get' })
  spaces.value = data || []
}

const showCreateSpace = () => { spaceForm.value = { name: '', description: '', visibility: 'PUBLIC' }; dialogVisible.value = true }

const createSpace = async () => {
  await request({ url: '/tool/wiki/spaces', method: 'post', data: spaceForm.value })
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadSpaces()
}

const deleteSpace = async (id) => {
  await request({ url: `/tool/wiki/spaces/${id}`, method: 'delete' })
  ElMessage.success(t('common.success'))
  loadSpaces()
}

const openSpace = (space) => { router.push(`/tool/wiki/page?spaceId=${space.id}`) }

onMounted(loadSpaces)
</script>
