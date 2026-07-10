<template>
  <div class="page-container">
    <div class="search-bar">
      <span style="font-size: 16px; font-weight: 600">数据库备份与恢复</span>
      <div style="flex: 1" />
      <el-button type="primary" @click="doBackup" :loading="backingUp">
        <el-icon><FolderOpened /></el-icon> 立即备份
      </el-button>
    </div>

    <div class="table-container">
      <el-table :data="backupList" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="name" label="文件名" min-width="200" />
        <el-table-column prop="sizeDisplay" label="大小" width="120" />
        <el-table-column prop="createTime" label="备份时间" width="200" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="doDownload(row.name)">下载</el-button>
            <el-popconfirm title="确认删除此备份?" @confirm="doDelete(row.name)">
              <template #reference><el-button link type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && backupList.length === 0" description="暂无备份记录" />
    </div>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolBackup' })
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'
import { getBackupListApi, createBackupApi, deleteBackupApi, downloadBackupApi } from '@/api/backup'

const backupList = ref([])
const loading = ref(false)
const backingUp = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getBackupListApi()
    backupList.value = res.data || []
  } catch {
    /* */
  } finally {
    loading.value = false
  }
}

const doBackup = async () => {
  backingUp.value = true
  try {
    await createBackupApi()
    ElMessage.success('备份成功!')
    fetchList()
  } catch {
    ElMessage.error('备份失败')
  } finally {
    backingUp.value = false
  }
}

const doDownload = async (filename) => {
  try {
    const blob = await downloadBackupApi(filename)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('下载失败')
  }
}

const doDelete = async (filename) => {
  try {
    await deleteBackupApi(filename)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(fetchList)
</script>
