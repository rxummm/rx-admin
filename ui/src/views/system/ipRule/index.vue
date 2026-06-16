<template>
  <div class="page-container">
    <div class="search-bar">
      <el-radio-group v-model="filterMode" @change="loadMode" size="small">
        <el-radio-button value="OFF">{{ $t('system.ipRule.off') }}</el-radio-button>
        <el-radio-button value="BLACK">{{ $t('system.ipRule.blackMode') }}</el-radio-button>
        <el-radio-button value="WHITE">{{ $t('system.ipRule.whiteMode') }}</el-radio-button>
      </el-radio-group>
      <span style="color: #909399; font-size: 12px; margin-left: 12px;">
        {{ modeTip }}
      </span>
      <div style="flex: 1" />
      <el-input v-model="keyword" :placeholder="$t('system.ipRule.searchPlaceholder')" style="width: 180px;" @keyup.enter="fetchData" clearable />
      <el-select v-model="ruleType" :placeholder="$t('system.ipRule.ruleType')" style="width: 120px;" clearable @change="fetchData">
        <el-option :label="$t('system.ipRule.ruleTypeOptions.black')" value="BLACK" />
        <el-option :label="$t('system.ipRule.ruleTypeOptions.white')" value="WHITE" />
      </el-select>
      <el-button type="primary" @click="fetchData">{{ $t('common.search') }}</el-button>
      <el-button type="primary" @click="handleAdd">{{ $t('system.ipRule.addRule') }}</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="ipAddress" :label="$t('system.ipRule.ipAddress')" min-width="160" />
        <el-table-column prop="ruleType" :label="$t('system.ipRule.ruleType')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.ruleType === 'BLACK' ? 'danger' : 'success'" size="small">
              {{ row.ruleType === 'BLACK' ? $t('system.ipRule.ruleTypeOptions.black') : $t('system.ipRule.ruleTypeOptions.white') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('system.ipRule.description')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('common.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('common.enable') : $t('common.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="170" />
        <el-table-column :label="$t('common.operation')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-popconfirm :title="$t('system.ipRule.deleteConfirm')" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger">{{ $t('common.delete') }}</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="page-pagination" v-model:current-page="page" v-model:page-size="size"
        :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('system.ipRule.editRule') : $t('system.ipRule.addRuleTitle')" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item :label="$t('system.ipRule.ipAddress')" required>
          <el-input v-model="form.ipAddress" placeholder="192.168.1.100 / 192.168.1.0/24" />
        </el-form-item>
        <el-form-item :label="$t('system.ipRule.ruleType')" required>
          <el-select v-model="form.ruleType" style="width:100%">
            <el-option :label="$t('system.ipRule.ruleTypeOptions.black') + ' BLACK'" value="BLACK" />
            <el-option :label="$t('system.ipRule.ruleTypeOptions.white') + ' WHITE'" value="WHITE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('system.ipRule.description')">
          <el-input v-model="form.description" :placeholder="$t('common.input')" />
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" :active-text="$t('common.enable')" :inactive-text="$t('common.disable')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'SystemIpRule' })
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getIpRulePageApi, addIpRuleApi, updateIpRuleApi, deleteIpRuleApi, getIpRuleModeApi, setIpRuleModeApi } from '@/api/ipRule'

const { t } = useI18n()

const tableData = ref([]); const loading = ref(false)
const page = ref(1); const size = ref(10); const total = ref(0)
const keyword = ref(''); const ruleType = ref('')
const dialogVisible = ref(false); const isEdit = ref(false)
const form = ref({ status: 1, ruleType: 'BLACK' })
const filterMode = ref('OFF')

const modeTip = computed(() => {
  return filterMode.value === 'BLACK' ? t('system.ipRule.filterBlack') :
    filterMode.value === 'WHITE' ? t('system.ipRule.filterWhite') : t('system.ipRule.filterOff')
})

const loadMode = async () => {
  try { await setIpRuleModeApi(filterMode.value); ElMessage.success(t('system.ipRule.switchModeSuccess')) } catch (e) { /* */ }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getIpRulePageApi({ page: page.value, size: size.value, keyword: keyword.value, ruleType: ruleType.value })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

const handleAdd = () => { isEdit.value = false; form.value = { status: 1, ruleType: 'BLACK' }; dialogVisible.value = true }
const handleEdit = (row) => { isEdit.value = true; form.value = { ...row }; dialogVisible.value = true }
const handleDelete = async (row) => { await deleteIpRuleApi(row.id); ElMessage.success(t('system.ipRule.deleteSuccess')); fetchData() }
const handleSubmit = async () => {
  if (!form.value.ipAddress) { ElMessage.warning(t('system.ipRule.pleaseInputIp')); return }
  isEdit.value ? await updateIpRuleApi(form.value) : await addIpRuleApi(form.value)
  ElMessage.success(isEdit.value ? t('system.ipRule.modifySuccess') : t('system.ipRule.addSuccess')); dialogVisible.value = false; fetchData()
}

;(async () => {
  try { const res = await getIpRuleModeApi(); filterMode.value = res.data?.mode || 'OFF' } catch (e) { /* */ }
  fetchData()
})()
</script>


<style scoped>
.page-pagination {
  margin-top: 12px;
}
</style>
