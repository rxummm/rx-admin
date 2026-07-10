<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索模板名称" clearable style="width: 200px" @keyup.enter="loadData" />
      <el-select v-model="queryCategory" placeholder="选择分类" clearable style="width: 150px" @change="loadData">
        <el-option label="全部分类" value="" />
        <el-option label="邮件" value="email" />
        <el-option label="短信" value="sms" />
        <el-option label="站内信" value="message" />
      </el-select>
      <el-button type="primary" @click="loadData" :loading="loading">
        <el-icon><Search /></el-icon>
        查询
      </el-button>
      <el-button type="success" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增模板
      </el-button>
    </div>

    <!-- 模板列表 -->
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="模板名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">{{ getCategoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="主题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="variables" label="变量" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="v in parseVariables(row.content)" :key="v" size="small" style="margin-right: 4px">
              {{ v }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="handlePreview(row)">预览</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类">
            <el-option label="邮件" value="email" />
            <el-option label="短信" value="sms" />
            <el-option label="站内信" value="message" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题" prop="subject">
          <el-input v-model="formData.subject" placeholder="请输入主题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="8" placeholder="支持变量替换，如 {{name}}, {{date}}" />
        </el-form-item>
        <el-form-item label="变量说明">
          <div class="variables-tip">
            <p>可用变量：</p>
            <el-tag v-for="v in parseVariables(formData.content)" :key="v" size="small" style="margin-right: 4px">
              {{ v }}
            </el-tag>
            <span v-if="parseVariables(formData.content).length === 0" style="color: #909399">暂无变量</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="模板预览" width="600px">
      <div class="preview-content">
        <div class="preview-subject">
          <strong>主题：</strong>{{ previewData.subject }}
        </div>
        <div class="preview-body">
          <strong>内容：</strong>
          <div class="preview-text">{{ previewData.content }}</div>
        </div>
        <div class="preview-variables">
          <strong>变量值：</strong>
          <el-input v-model="previewVars" placeholder='{"name": "张三", "date": "2024-01-01"}' />
        </div>
        <div class="preview-result">
          <strong>渲染结果：</strong>
          <div class="result-text">{{ renderedContent }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getEmailTemplatePage, addEmailTemplate, updateEmailTemplate, deleteEmailTemplate } from '@/api/emailTemplate'

const loading = ref(false)
const submitting = ref(false)
const keyword = ref('')
const queryCategory = ref('')
const tableData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formData = ref({})
const formRef = ref(null)
const isEdit = ref(false)

const previewVisible = ref(false)
const previewData = ref({})
const previewVars = ref('{"name": "张三", "date": "2024-01-01", "title": "系统通知"}')

const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const renderedContent = computed(() => {
  if (!previewData.value.content) return ''
  try {
    const vars = JSON.parse(previewVars.value)
    let content = previewData.value.content
    for (const [key, value] of Object.entries(vars)) {
      content = content.replace(new RegExp(`\\{\\{${key}\\}\\}`, 'g'), value)
    }
    return content
  } catch (e) {
    return previewData.value.content
  }
})

const getCategoryType = (category) => {
  const types = { email: '', sms: 'success', message: 'warning' }
  return types[category] || 'info'
}

const getCategoryLabel = (category) => {
  const labels = { email: '邮件', sms: '短信', message: '站内信' }
  return labels[category] || category
}

const parseVariables = (content) => {
  if (!content) return []
  const matches = content.match(/\{\{(\w+)\}\}/g) || []
  return [...new Set(matches.map(m => m.replace(/\{\{|\}\}/g, '')))]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getEmailTemplatePage({
      page: 1,
      size: 100,
      keyword: keyword.value,
      category: queryCategory.value
    })
    tableData.value = res.data?.records || []
  } catch (e) {
    console.error('加载模板失败:', e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增模板'
  formData.value = { name: '', category: 'email', subject: '', content: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑模板'
  formData.value = { ...row }
  dialogVisible.value = true
}

const handlePreview = (row) => {
  previewData.value = row
  previewVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '确认删除', { type: 'warning' })
    await deleteEmailTemplate(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除失败:', e)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateEmailTemplate(formData.value)
    } else {
      await addEmailTemplate(formData.value)
    }
    
    ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    console.error('提交失败:', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.variables-tip {
  color: #606266;
  font-size: 14px;
}

.variables-tip p {
  margin-bottom: 8px;
}

.preview-content {
  line-height: 1.8;
}

.preview-subject,
.preview-body,
.preview-variables,
.preview-result {
  margin-bottom: 16px;
}

.preview-text,
.result-text {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-top: 8px;
  white-space: pre-wrap;
}
</style>
