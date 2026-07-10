<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="showAdd">{{ $t('common.add') }}</el-button>
    </div>
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" :label="$t('tool.emailTemplate.name')" />
        <el-table-column prop="code" :label="$t('tool.emailTemplate.code')" />
        <el-table-column prop="subject" :label="$t('tool.emailTemplate.subject')" show-overflow-tooltip />
        <el-table-column prop="category" :label="$t('tool.emailTemplate.category')" />
        <el-table-column prop="status" :label="$t('common.status')">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? $t('common.enabled') : $t('common.disabled') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common操作')" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-popconfirm :title="$t('common.confirmDelete')" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('common.edit') : $t('common.add')" width="700px">
      <el-form :model="form" label-width="120px">
        <el-form-item :label="$t('tool.emailTemplate.name')"><el-input v-model="form.name" /></el-form-item>
        <el-form-item :label="$t('tool.emailTemplate.code')"><el-input v-model="form.code" :disabled="isEdit" /></el-form-item>
        <el-form-item :label="$t('tool.emailTemplate.subject')"><el-input v-model="form.subject" /></el-form-item>
        <el-form-item :label="$t('tool.emailTemplate.body')"><el-input v-model="form.body" type="textarea" :rows="8" /></el-form-item>
        <el-form-item :label="$t('tool.emailTemplate.variables')"><el-input v-model="form.variables" placeholder="如: name,email" /></el-form-item>
        <el-form-item :label="$t('tool.emailTemplate.category')"><el-input v-model="form.category" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolEmailTemplate' })
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/tool/email-template/list', method: 'get' })
    tableData.value = data || []
  } finally { loading.value = false }
}

const showAdd = () => { isEdit.value = false; form.value = {}; dialogVisible.value = true }
const showEdit = (row) => { isEdit.value = true; form.value = { ...row }; dialogVisible.value = true }

const handleSubmit = async () => {
  if (isEdit.value) {
    await request({ url: '/tool/email-template', method: 'put', data: form.value })
  } else {
    await request({ url: '/tool/email-template', method: 'post', data: form.value })
  }
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await request({ url: `/tool/email-template/${id}`, method: 'delete' })
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>
