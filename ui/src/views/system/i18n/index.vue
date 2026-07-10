<template>
  <div class="page-container">
    <div class="search-bar">
      <el-select v-model="localeFilter" :placeholder="$t('i18n.selectLocale')" clearable @change="loadData">
        <el-option v-for="loc in locales" :key="loc.code" :label="loc.name" :value="loc.code" />
      </el-select>
      <el-button type="primary" @click="loadData">{{ $t('common.search') }}</el-button>
      <el-button type="success" @click="showAddKey">{{ $t('i18n.addKey') }}</el-button>
    </div>
    <div class="table-container">
      <el-table :data="keys" v-loading="loading" border stripe>
        <el-table-column prop="keyPath" :label="$t('i18n.keyPath')" />
        <el-table-column prop="module" :label="$t('i18n.module')" />
        <el-table-column prop="description" :label="$t('i18n.description')" />
        <el-table-column :label="$t('i18n.translation')" v-if="localeFilter">
          <template #default="{ row }">
            <el-input v-model="row._translation" size="small" @blur="saveTranslation(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('common操作')" width="100">
          <template #default="{ row }">
            <el-popconfirm :title="$t('common.confirmDelete')" @confirm="deleteKey(row.id)">
              <template #reference>
                <el-button size="small" type="danger">{{ $t('common.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="$t('i18n.addKey')" width="500px">
      <el-form :model="keyForm" label-width="100px">
        <el-form-item :label="$t('i18n.keyPath')"><el-input v-model="keyForm.keyPath" /></el-form-item>
        <el-form-item :label="$t('i18n.module')"><el-input v-model="keyForm.module" /></el-form-item>
        <el-form-item :label="$t('i18n.description')"><el-input v-model="keyForm.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="addKey">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
defineOptions({ name: 'SystemI18n' })
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const keys = ref([])
const locales = ref([])
const localeFilter = ref('')
const dialogVisible = ref(false)
const keyForm = ref({ keyPath: '', module: '', description: '' })

const loadData = async () => {
  loading.value = true
  try {
    const { data: keysData } = await request({ url: '/sys/i18n/keys', method: 'get', params: { module: '' } })
    keys.value = (keysData || []).map(k => ({ ...k, _translation: '' }))
    if (localeFilter.value) {
      const { data: trans } = await request({ url: `/sys/i18n/translations/${localeFilter.value}`, method: 'get' })
      keys.value.forEach(k => { k._translation = trans[k.keyPath] || '' })
    }
  } finally { loading.value = false }
}

const loadLocales = async () => {
  const { data } = await request({ url: '/sys/i18n/locales', method: 'get' })
  locales.value = data || []
}

const showAddKey = () => { keyForm.value = { keyPath: '', module: '', description: '' }; dialogVisible.value = true }

const addKey = async () => {
  await request({ url: '/sys/i18n/keys', method: 'post', data: keyForm.value })
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadData()
}

const saveTranslation = async (row) => {
  if (!localeFilter.value || !row._translation) return
  await request({ url: '/sys/i18n/translations', method: 'post', data: { keyId: row.id, localeCode: localeFilter.value, translation: row._translation } })
}

const deleteKey = async (id) => {
  await request({ url: `/sys/i18n/keys/${id}`, method: 'delete' })
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(() => { loadLocales(); loadData() })
</script>
