<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="showCreateProject">{{ $t('tool.gantt.createProject') }}</el-button>
    </div>
    <div class="table-container">
      <el-table :data="projects" v-loading="loading" border stripe>
        <el-table-column prop="name" :label="$t('tool.gantt.projectName')" />
        <el-table-column prop="description" :label="$t('tool.gantt.description')" show-overflow-tooltip />
        <el-table-column prop="startDate" :label="$t('tool.gantt.startDate')" width="120" />
        <el-table-column prop="endDate" :label="$t('tool.gantt.endDate')" width="120" />
        <el-table-column prop="status" :label="$t('common.status')">
          <template #default="{ row }">
            <el-tag>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common操作')" width="150">
          <template #default="{ row }">
            <el-popconfirm :title="$t('common.confirmDelete')" @confirm="deleteProject(row.id)">
              <template #reference>
                <el-button size="small" type="danger">{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="$t('tool.gantt.createProject')" width="500px">
      <el-form :model="projectForm" label-width="100px">
        <el-form-item :label="$t('tool.gantt.projectName')"><el-input v-model="projectForm.name" /></el-form-item>
        <el-form-item :label="$t('tool.gantt.description')"><el-input v-model="projectForm.description" type="textarea" /></el-form-item>
        <el-form-item :label="$t('tool.gantt.startDate')"><el-date-picker v-model="projectForm.startDate" type="date" /></el-form-item>
        <el-form-item :label="$t('tool.gantt.endDate')"><el-date-picker v-model="projectForm.endDate" type="date" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="createProject">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolGantt' })
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const projects = ref([])
const dialogVisible = ref(false)
const projectForm = ref({ name: '', description: '', startDate: '', endDate: '' })

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/tool/gantt/projects', method: 'get' })
    projects.value = data || []
  } finally { loading.value = false }
}

const showCreateProject = () => { projectForm.value = { name: '', description: '', startDate: '', endDate: '' }; dialogVisible.value = true }

const createProject = async () => {
  await request({ url: '/tool/gantt/projects', method: 'post', data: projectForm.value })
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadData()
}

const deleteProject = async (id) => {
  await request({ url: `/tool/gantt/projects/${id}`, method: 'delete' })
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>
