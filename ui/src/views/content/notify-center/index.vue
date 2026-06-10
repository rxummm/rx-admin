<template>
  <div class="page-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="消息模板" name="templates">
        <div class="search-bar" style="margin-bottom:12px">
          <el-input v-model="tSearch.name" placeholder="模板名称" clearable style="width:200px" />
          <el-button type="primary" @click="fetchTemplates" style="margin-left:8px">搜索</el-button>
          <div style="flex:1" />
          <el-button type="primary" @click="openTemplateDialog()">新增模板</el-button>
        </div>
        <el-table :data="templates" v-loading="tLoading" stripe border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="模板名称" width="160" />
          <el-table-column prop="code" label="编码" width="150" />
          <el-table-column prop="titleTemplate" label="标题模板" min-width="200" show-overflow-tooltip />
          <el-table-column prop="channels" label="通道" width="180" />
          <el-table-column prop="status" label="状态" width="70">
            <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" link type="primary" @click="openTemplateDialog(row)">编辑</el-button>
              <el-button size="small" link type="danger" @click="deleteTemplate(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination v-model:current-page="tPage.page" v-model:page-size="tPage.size" :page-sizes="[10,20,50]"
            :total="tPage.total" layout="total,sizes,prev,pager,next" @change="fetchTemplates" />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="发送记录" name="records">
        <div class="search-bar" style="margin-bottom:12px">
          <el-select v-model="rSearch.channel" placeholder="通道" clearable style="width:130px">
            <el-option label="站内消息" value="message" />
            <el-option label="邮件" value="email" />
            <el-option label="企业微信" value="wecom" />
            <el-option label="钉钉" value="dingtalk" />
            <el-option label="飞书" value="feishu" />
          </el-select>
          <el-select v-model="rSearch.status" placeholder="状态" clearable style="width:100px;margin-left:8px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="2" />
            <el-option label="待发送" :value="0" />
          </el-select>
          <el-button type="primary" @click="fetchRecords" style="margin-left:8px">搜索</el-button>
          <div style="flex:1" />
          <el-button type="danger" :disabled="rSelected.length===0" @click="batchDeleteRecords">批量删除</el-button>
        </div>
        <el-table :data="records" v-loading="rLoading" @selection-change="v=>rSelected=v.map(r=>r.id)" stripe border>
          <el-table-column type="selection" width="45" />
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="channel" label="通道" width="100" />
          <el-table-column prop="receiver" label="接收人" width="150" />
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':row.status===2?'danger':'info'" size="small">{{ row.status===1?'成功':row.status===2?'失败':'待发' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="retryCount" label="重试次数" width="80" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status===2" size="small" link type="warning" @click="retry(row.id)">重发</el-button>
              <el-button size="small" link type="danger" @click="deleteRecord(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination v-model:current-page="rPage.page" v-model:page-size="rPage.size" :page-sizes="[10,20,50]"
            :total="rPage.total" layout="total,sizes,prev,pager,next" @change="fetchRecords" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 模板编辑弹窗 -->
    <el-dialog v-model="tDialogVisible" :title="editingTemplate?.id?'编辑模板':'新增模板'" width="600px">
      <el-form :model="tForm" label-width="80px">
        <el-form-item label="模板名称"><el-input v-model="tForm.name" /></el-form-item>
        <el-form-item label="模板编码"><el-input v-model="tForm.code" /></el-form-item>
        <el-form-item label="标题模板"><el-input v-model="tForm.titleTemplate" placeholder="您好{username}，..." /></el-form-item>
        <el-form-item label="内容模板"><el-input v-model="tForm.contentTemplate" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="通道"><el-input v-model="tForm.channels" placeholder="message,email" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="tForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTemplatePageApi, addTemplateApi, updateTemplateApi, deleteTemplateApi,
         getNotifyRecordPageApi, deleteNotifyRecordApi, deleteNotifyRecordsBatchApi, retryNotifyApi } from '@/api/notifyCenter'

const activeTab = ref('templates')

// ====== 模板 ======
const templates = ref([]); const tLoading = ref(false)
const tSearch = reactive({ name: '' }); const tPage = reactive({ page:1, size:10, total:0 })
const tDialogVisible = ref(false); const editingTemplate = ref(null)
const tForm = reactive({ name:'', code:'', titleTemplate:'', contentTemplate:'', channels:'message', status:1 })

async function fetchTemplates() {
  tLoading.value = true
  try { const res = await getTemplatePageApi({ page: tPage.page, size: tPage.size, name: tSearch.name || undefined }); templates.value = res.data.records||[]; tPage.total = res.data.total||0 } 
  catch { ElMessage.error('加载失败') } finally { tLoading.value = false }
}
function openTemplateDialog(row) {
  editingTemplate.value = row || null
  Object.assign(tForm, row ? { name:row.name, code:row.code, titleTemplate:row.titleTemplate, contentTemplate:row.contentTemplate, channels:row.channels, status:row.status } 
    : { name:'', code:'', titleTemplate:'', contentTemplate:'', channels:'message', status:1 })
  tDialogVisible.value = true
}
async function saveTemplate() {
  if (editingTemplate.value?.id) { tForm.id = editingTemplate.value.id; await updateTemplateApi(tForm) }
  else await addTemplateApi(tForm)
  ElMessage.success('保存成功'); tDialogVisible.value = false; fetchTemplates()
}
async function deleteTemplate(id) { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteTemplateApi(id); ElMessage.success('删除成功'); fetchTemplates() }

// ====== 记录 ======
const records = ref([]); const rLoading = ref(false); const rSelected = ref([])
const rSearch = reactive({ channel: null, status: null }); const rPage = reactive({ page:1, size:10, total:0 })
async function fetchRecords() {
  rLoading.value = true
  try { const res = await getNotifyRecordPageApi({ page: rPage.page, size: rPage.size, channel: rSearch.channel, status: rSearch.status }); records.value = res.data.records||[]; rPage.total = res.data.total||0 } 
  catch { ElMessage.error('加载失败') } finally { rLoading.value = false }
}
async function deleteRecord(id) { await ElMessageBox.confirm('确认删除？','提示',{type:'warning'}); await deleteNotifyRecordApi(id); ElMessage.success('删除成功'); fetchRecords() }
async function batchDeleteRecords() { await ElMessageBox.confirm(`确认删除${rSelected.value.length}条？`,'提示',{type:'warning'}); await deleteNotifyRecordsBatchApi(rSelected.value); ElMessage.success('删除成功'); fetchRecords() }
async function retry(id) { await retryNotifyApi(id); ElMessage.success('重发成功'); fetchRecords() }

onMounted(() => { fetchTemplates(); fetchRecords() })
</script>
