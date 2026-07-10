<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="showAdd">新增</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="tableName" label="源表名" />
        <el-table-column prop="archiveTable" label="归档表" />
        <el-table-column prop="conditionField" label="条件字段" />
        <el-table-column prop="retainDays" label="保留天数" width="100" />
        <el-table-column prop="batchSize" label="批量大小" width="100" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastArchiveTime" label="上次归档" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="showEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑' : '新增'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="源表名"><el-input v-model="form.tableName" /></el-form-item>
        <el-form-item label="归档表"><el-input v-model="form.archiveTable" /></el-form-item>
        <el-form-item label="条件字段"><el-input v-model="form.conditionField" /></el-form-item>
        <el-form-item label="保留天数"><el-input-number v-model="form.retainDays" :min="1" /></el-form-item>
        <el-form-item label="批量大小"><el-input-number v-model="form.batchSize" :min="100" :max="10000" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolArchive' })
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/tool/archive/list', method: 'get' })
    tableData.value = data || []
  } finally { loading.value = false }
}

const showAdd = () => { isEdit.value = false; form.value = {}; dialogVisible.value = true }
const showEdit = (row) => { isEdit.value = true; form.value = { ...row }; dialogVisible.value = true }

const handleSubmit = async () => {
  if (isEdit.value) {
    await request({ url: '/tool/archive', method: 'put', data: form.value })
  } else {
    await request({ url: '/tool/archive', method: 'post', data: form.value })
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await request({ url: `/tool/archive/${id}`, method: 'delete' })
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
