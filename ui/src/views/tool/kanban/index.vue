<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="showCreateBoard">{{ $t('tool.kanban.createBoard') }}</el-button>
    </div>
    <div v-if="boards.length === 0" style="text-align: center; padding: 40px; color: #999">
      {{ $t('tool.kanban.noBoards') }}
    </div>
    <el-row :gutter="16" v-else>
      <el-col :span="6" v-for="board in boards" :key="board.id">
        <el-card shadow="hover" style="cursor: pointer" @click="selectBoard(board)">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ board.name }}</span>
              <el-popconfirm :title="$t('common.confirmDelete')" @confirm.stop="deleteBoard(board.id)">
                <template #reference>
                  <el-button size="small" type="danger" @click.stop>{{ $t('common.delete') }}</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
          <p>{{ board.description }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="$t('tool.kanban.createBoard')" width="400px">
      <el-form :model="boardForm" label-width="100px">
        <el-form-item :label="$t('tool.kanban.name')"><el-input v-model="boardForm.name" /></el-form-item>
        <el-form-item :label="$t('tool.kanban.description')"><el-input v-model="boardForm.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="createBoard">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolKanban' })
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const boards = ref([])
const dialogVisible = ref(false)
const boardForm = ref({ name: '', description: '' })

const loadBoards = async () => {
  const { data } = await request({ url: '/tool/kanban/list', method: 'get' })
  boards.value = data || []
}

const showCreateBoard = () => { boardForm.value = { name: '', description: '' }; dialogVisible.value = true }

const createBoard = async () => {
  await request({ url: '/tool/kanban', method: 'post', data: boardForm.value })
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadBoards()
}

const deleteBoard = async (id) => {
  await request({ url: `/tool/kanban/${id}`, method: 'delete' })
  ElMessage.success(t('common.success'))
  loadBoards()
}

const selectBoard = (board) => { ElMessage.info(`选中看板: ${board.name}`) }

onMounted(loadBoards)
</script>
