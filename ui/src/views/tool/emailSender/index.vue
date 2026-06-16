<template>
  <div class="page-container email-sender-page">
    <div class="page-header">
      <h3>{{ $t('tool.emailSender.title') }}</h3>
      <p class="page-desc">{{ $t('tool.emailSender.desc') }}</p>
    </div>

    <el-card class="email-compose-card" shadow="never">
      <!-- 邮件头区域 -->
      <div class="email-header">
        <!-- 发件人 -->
        <div class="email-row">
          <label class="email-label">{{ $t('tool.emailSender.from') }}</label>
          <div class="email-input-wrap">
            <el-input
              v-model="fromAddress"
              disabled
              placeholder="sender@example.com"
              class="email-input"
            />
          </div>
        </div>

        <!-- 收件人 -->
        <div class="email-row">
          <label class="email-label">{{ $t('tool.emailSender.to') }}</label>
          <div class="email-input-wrap">
            <div class="email-tag-input" @click="focusTagInput('to')">
              <el-tag
                v-for="(email, idx) in toList"
                :key="email"
                closable
                size="small"
                type="info"
                @close="toList.splice(idx, 1)"
              >{{ email }}</el-tag>
              <input
                ref="toInputRef"
                v-model="toInputValue"
                :placeholder="toList.length === 0 ? $t('tool.emailSender.toPlaceholder') : ''"
                class="email-tag-input-inner"
                @keydown="handleTagKeydown($event, toList, toInputValue, 'to')"
                @blur="addEmailTag(toList, toInputValue, 'to')"
                @paste="handleEmailPaste(toList, 'to', $event)"
              />
            </div>
          </div>
          <div class="email-cc-toggle">
            <el-button link type="primary" @click="showCc = !showCc" v-if="!showCc">{{ $t('tool.emailSender.cc') }}</el-button>
            <el-button link type="primary" @click="showBcc = !showBcc" v-if="!showBcc && showCc" class="bcc-toggle">{{ $t('tool.emailSender.bcc') }}</el-button>
          </div>
        </div>

        <!-- 抄送 -->
        <div class="email-row" v-if="showCc">
          <label class="email-label">{{ $t('tool.emailSender.cc') }}</label>
          <div class="email-input-wrap">
            <div class="email-tag-input" @click="focusTagInput('cc')">
              <el-tag
                v-for="(email, idx) in ccList"
                :key="email"
                closable
                size="small"
                type="info"
                @close="ccList.splice(idx, 1)"
              >{{ email }}</el-tag>
              <input
                ref="ccInputRef"
                v-model="ccInputValue"
                :placeholder="ccList.length === 0 ? $t('tool.emailSender.ccPlaceholder') : ''"
                class="email-tag-input-inner"
                @keydown="handleTagKeydown($event, ccList, ccInputValue, 'cc')"
                @blur="addEmailTag(ccList, ccInputValue, 'cc')"
                @paste="handleEmailPaste(ccList, 'cc', $event)"
              />
            </div>
          </div>
          <div class="email-cc-toggle" v-if="!showBcc">
            <el-button link type="primary" @click="showBcc = !showBcc">{{ $t('tool.emailSender.bcc') }}</el-button>
          </div>
        </div>

        <!-- 密送 -->
        <div class="email-row" v-if="showBcc">
          <label class="email-label">{{ $t('tool.emailSender.bcc') }}</label>
          <div class="email-input-wrap">
            <div class="email-tag-input" @click="focusTagInput('bcc')">
              <el-tag
                v-for="(email, idx) in bccList"
                :key="email"
                closable
                size="small"
                type="info"
                @close="bccList.splice(idx, 1)"
              >{{ email }}</el-tag>
              <input
                ref="bccInputRef"
                v-model="bccInputValue"
                :placeholder="bccList.length === 0 ? $t('tool.emailSender.bccPlaceholder') : ''"
                class="email-tag-input-inner"
                @keydown="handleTagKeydown($event, bccList, bccInputValue, 'bcc')"
                @blur="addEmailTag(bccList, bccInputValue, 'bcc')"
                @paste="handleEmailPaste(bccList, 'bcc', $event)"
              />
            </div>
          </div>
        </div>

        <!-- 主题 -->
        <div class="email-row">
          <label class="email-label">{{ $t('tool.emailSender.subject') }}</label>
          <div class="email-input-wrap">
            <el-input
              v-model="subject"
              :placeholder="$t('tool.emailSender.subjectPlaceholder')"
              class="email-input"
            />
          </div>
        </div>
      </div>

      <!-- 格式工具栏 -->
      <div class="email-toolbar">
        <div class="toolbar-left">
          <el-button-group class="format-toggle">
            <el-button
              :type="isHtml ? 'primary' : 'default'"
              size="small"
              @click="switchMode(true)"
            >
              <el-icon><Operation /></el-icon> HTML
            </el-button>
            <el-button
              :type="!isHtml ? 'primary' : 'default'"
              size="small"
              @click="switchMode(false)"
            >
              <el-icon><Document /></el-icon> {{ $t('tool.emailSender.plainText') }}
            </el-button>
          </el-button-group>
        </div>
        <div class="toolbar-right">
          <el-button size="small" @click="insertTemplate('greeting')" :disabled="sending">
            <el-icon><Sunny /></el-icon> {{ $t('tool.emailSender.templateGreeting') }}
          </el-button>
          <el-button size="small" @click="insertTemplate('notification')" :disabled="sending">
            <el-icon><Bell /></el-icon> {{ $t('tool.emailSender.templateNotification') }}
          </el-button>
        </div>
      </div>

      <!-- 编辑器区域 -->
      <div class="email-editor-wrap">
        <!-- HTML 富文本模式 -->
        <div v-if="isHtml" class="rich-editor">
          <Toolbar
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            mode="default"
          />
          <Editor
            v-model="htmlContent"
            :defaultConfig="editorConfig"
            mode="default"
            @onCreated="onEditorCreated"
          />
        </div>
        <!-- 纯文本模式 -->
        <el-input
          v-else
          v-model="plainTextContent"
          type="textarea"
          :rows="14"
          :placeholder="$t('tool.emailSender.editorPlaceholder')"
          class="plain-editor"
          resize="vertical"
        />
      </div>

      <!-- 附件区域 -->
      <div class="email-attachments" v-if="attachments.length > 0">
        <div class="attachment-header">
          <span class="attachment-title">
            <el-icon><Paperclip /></el-icon>
            {{ $t('tool.emailSender.attachments') }} ({{ attachments.length }})
          </span>
        </div>
        <div class="attachment-list">
          <div
            v-for="(att, idx) in attachments"
            :key="idx"
            class="attachment-item"
          >
            <div class="attachment-info">
              <el-icon class="att-icon" :style="{ color: getFileIconColor(att.fileName) }">
                <Document />
              </el-icon>
              <span class="att-name" :title="att.fileName">{{ att.fileName }}</span>
              <span class="att-size">{{ formatFileSize(att.fileSize) }}</span>
            </div>
            <div class="attachment-progress" v-if="att.uploading">
              <el-progress :percentage="att.progress || 0" :stroke-width="4" :show-text="false" />
            </div>
            <el-button
              link
              type="danger"
              size="small"
              class="att-remove"
              @click="removeAttachment(idx)"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="email-footer">
        <div class="footer-left">
          <el-upload
            ref="attachmentUploadRef"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleAttachmentAdd"
            accept="*"
            multiple
          >
            <el-button :disabled="sending">
              <el-icon><Paperclip /></el-icon>
              {{ $t('tool.emailSender.addAttachment') }}
            </el-button>
          </el-upload>
          <el-button @click="showSignatureDialog = true" :disabled="sending">
            <el-icon><Edit /></el-icon> 签名设置
          </el-button>
        </div>
        <div class="footer-right">
          <el-button @click="handleReset" :disabled="sending">
            {{ $t('tool.emailSender.reset') }}
          </el-button>
          <el-button type="primary" @click="handleSend" :loading="sending" :icon="Promotion">
            {{ $t('tool.emailSender.send') }}
          </el-button>
        </div>
      </div>

      <!-- 签名设置对话框 -->
      <el-dialog v-model="showSignatureDialog" title="签名设置" width="700px" :close-on-click-modal="false">
        <div class="signature-settings">
          <div class="signature-row">
            <label class="signature-label">是否启用签名</label>
            <el-switch v-model="signatureEnabled" />
          </div>
          <div class="signature-row" v-if="signatureEnabled">
            <label class="signature-label">发件人签名</label>
          </div>
          <div class="signature-editor-wrap" v-if="signatureEnabled">
            <Toolbar
              :editor="signatureEditorRef"
              :defaultConfig="signatureToolbarConfig"
              mode="simple"
            />
            <Editor
              v-model="signatureHtml"
              :defaultConfig="signatureEditorConfig"
              mode="simple"
              @onCreated="onSignatureEditorCreated"
            />
          </div>
        </div>
        <template #footer>
          <el-button @click="showSignatureDialog = false">取消</el-button>
          <el-button type="primary" @click="saveSignature">保存签名</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
defineOptions({ name: 'ToolEmailSender' })
import { ref, reactive, onMounted, nextTick, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Operation, Document, Sunny, Bell, Paperclip,
  Close, Promotion, Edit
} from '@element-plus/icons-vue'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { sendEmailApi, uploadEmailAttachmentApi, getEmailConfigApi } from '@/api/commonTools'
import { useStorage, STORAGE_KEYS } from '@/composables/useStorage'
import { FONT_FAMILIES, FONT_SIZES, SIGNATURE_FONT_SIZES } from '@/config/font'
import { EMAIL_TEMPLATES } from '@/config/emailTemplates'
import { getFileIconColor } from '@/config/fileIcons'

// ── 签名本地存储 ──
const signatureStore = useStorage(STORAGE_KEYS.EMAIL_SIGNATURE)

// ── wangeditor 编辑器实例 ──
const editorRef = shallowRef()
const signatureEditorRef = shallowRef()

// ── 字体列表 ──
const fontFamilies = FONT_FAMILIES

// ── 主编辑器工具栏 ──
const toolbarConfig = {
  excludeKeys: [
    'group-video',
    'group-image',
    'fullScreen',
    'undo',
    'redo',
  ]
}

// ── 主编辑器配置 ──
const editorConfig = {
  placeholder: '请输入邮件内容...',
  MENU_CONF: {
    fontFamily: { fontFamilyList: fontFamilies },
    fontSize: {
      fontSizeList: FONT_SIZES
    }
  }
}

function onEditorCreated(editor) {
  editorRef.value = editor
}

// ── 表单数据 ──
const fromAddress = ref('')
const toList = ref([])
const ccList = ref([])
const bccList = ref([])
const subject = ref('')
const isHtml = ref(true)
const plainTextContent = ref('')
const htmlContent = ref('')
const sending = ref(false)
const showCc = ref(false)
const showBcc = ref(false)

// ── 签名 ──
const showSignatureDialog = ref(false)
const signatureEnabled = ref(false)
const signatureHtml = ref('')

const signatureToolbarConfig = {
  toolbarKeys: ['fontFamily', 'fontSize', '|', 'bold', 'italic', 'underline', 'color', 'bgColor', '|', 'clearStyle']
}

const signatureEditorConfig = {
  placeholder: '请输入签名内容...',
  MENU_CONF: {
    fontFamily: { fontFamilyList: fontFamilies },
    fontSize: {
      fontSizeList: SIGNATURE_FONT_SIZES
    }
  }
}

function onSignatureEditorCreated(editor) {
  signatureEditorRef.value = editor
}

// 从 signatureStore 加载签名
function loadSignature() {
  try {
    const data = signatureStore.get()
    if (data) {
      signatureEnabled.value = data.enabled || false
      signatureHtml.value = data.html || ''
    }
  } catch (e) {
    // ignore
  }
}

function saveSignature() {
  try {
    signatureStore.set({
      enabled: signatureEnabled.value,
      html: signatureHtml.value
    })
    ElMessage.success('签名已保存')
    showSignatureDialog.value = false
  } catch (e) {
    ElMessage.error('签名保存失败')
  }
}

// ── 标签输入 ──
const toInputValue = ref('')
const ccInputValue = ref('')
const bccInputValue = ref('')
const toInputRef = ref(null)
const ccInputRef = ref(null)
const bccInputRef = ref(null)

// ── 邮箱标签操作 ──
const inputClearMap = {
  to: () => { toInputValue.value = '' },
  cc: () => { ccInputValue.value = '' },
  bcc: () => { bccInputValue.value = '' }
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())
}

function addEmailTag(list, emailValue, clearKey) {
  const email = (emailValue || '').trim()
  if (!email || !isValidEmail(email)) return
  if (!list.includes(email)) {
    list.push(email)
  } else {
    ElMessage.warning('该邮箱已存在')
  }
  inputClearMap[clearKey]?.()
}

function handleTagBackspace(list, emailValue) {
  const val = emailValue || ''
  if (val === '' && list.length > 0) {
    list.pop()
  }
}

function handleTagKeydown(event, list, emailValue, clearKey) {
  if (event.key === 'Enter') {
    event.preventDefault()
    addEmailTag(list, emailValue, clearKey)
  } else if (event.key === 'Backspace') {
    handleTagBackspace(list, emailValue)
  } else if (event.key === ',' || event.key === ';') {
    event.preventDefault()
    addEmailTag(list, emailValue, clearKey)
  } else if (event.key === ' ') {
    // 空格：如果是完整邮箱则识别，否则当作普通空格输入
    const val = (emailValue || '').trim()
    if (val && isValidEmail(val)) {
      event.preventDefault()
      addEmailTag(list, emailValue, clearKey)
    }
    // 不是邮箱则不管，空格照常输入
  }
}

function handleEmailPaste(list, clearKey, event) {
  event.preventDefault()
  const text = (event.clipboardData || window.clipboardData).getData('text/plain')
  if (!text) return
  // 用正则从粘贴文本中提取所有邮箱地址
  const regex = /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g
  let match
  let addedCount = 0
  let duplicateCount = 0
  while ((match = regex.exec(text)) !== null) {
    const email = match[0].trim()
    if (!email) continue
    if (!list.includes(email)) {
      list.push(email)
      addedCount++
    } else {
      duplicateCount++
    }
  }
  // 全部重复时提示用户
  if (addedCount === 0 && duplicateCount > 0) {
    ElMessage.warning(duplicateCount > 1 ? '邮箱已存在，请勿重复添加' : '该邮箱已存在')
  }
  // 如果粘贴内容中有非邮箱文本，保留到输入框
  const nonEmailText = text.replace(/[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g, '').trim()
  if (nonEmailText) {
    const valueMap = { to: toInputValue, cc: ccInputValue, bcc: bccInputValue }
    valueMap[clearKey].value = nonEmailText.replace(/[,;\s]+$/, '')
  }
}

function focusTagInput(type) {
  const refMap = { to: toInputRef, cc: ccInputRef, bcc: bccInputRef }
  const el = refMap[type]?.value
  if (el && typeof el.focus === 'function') {
    el.focus()
  } else if (el && el.$el) {
    el.$el.querySelector('input')?.focus()
  }
}

// ── 附件 ──
const attachments = reactive([])

// ── 邮件配置 ──
onMounted(async () => {
  try {
    const res = await getEmailConfigApi()
    if (res?.data?.from) {
      fromAddress.value = res.data.from
    }
  } catch (e) {
    // 配置请求失败时静默处理
  }
  loadSignature()
})

// ── 模式切换 ──
function switchMode(html) {
  if (html === isHtml.value) return
  if (!html) {
    // 切换到纯文本：提取HTML编辑器的文本内容
    const editor = editorRef.value
    if (editor) {
      plainTextContent.value = editor.getText().trim()
    }
  } else {
    // 切换到HTML：将纯文本写回编辑器
    const text = plainTextContent.value
    if (text) {
      htmlContent.value = text.replace(/\n/g, '<br>')
    }
  }
  isHtml.value = html
}

// ── 模板 ──
function insertTemplate(type) {
  if (!isHtml.value) {
    isHtml.value = true
    nextTick(() => insertTemplateContent(type))
    return
  }
  insertTemplateContent(type)
}

function insertTemplateContent(type) {
  const templates = EMAIL_TEMPLATES
  const html = templates[type] || ''
  const editor = editorRef.value
  if (html && editor) {
    editor.dangerouslyInsertHtml(html)
  }
}

// ── 附件处理 ──
async function handleAttachmentAdd(fileData) {
  const raw = fileData.raw
  if (!raw) return

  const attItem = reactive({
    fileName: raw.name,
    fileSize: raw.size,
    filePath: '',
    uploading: true,
    progress: 0
  })
  attachments.push(attItem)

  try {
    const form = new FormData()
    form.append('file', raw)
    const res = await uploadEmailAttachmentApi(form)
    if (res?.data?.filePath) {
      attItem.filePath = res.data.filePath
      attItem.uploading = false
      attItem.progress = 100
    } else {
      throw new Error('上传返回数据异常')
    }
  } catch (e) {
    ElMessage.error('附件上传失败: ' + (e?.message || '未知错误'))
    const idx = attachments.indexOf(attItem)
    if (idx >= 0) attachments.splice(idx, 1)
  }
}

function removeAttachment(idx) {
  attachments.splice(idx, 1)
}

// ── 发送 ──
async function handleSend() {
  if (toList.value.length === 0) {
    ElMessage.warning('请输入收件人')
    return
  }
  if (!subject.value.trim()) {
    ElMessage.warning('请输入邮件主题')
    return
  }

  let content = isHtml.value
    ? (getHtmlContent())
    : (plainTextContent.value || '')

  if (!content.trim() || content === '<p><br></p>') {
    ElMessage.warning('请输入邮件内容')
    return
  }

  // 自动追加签名（仅HTML模式且启用时）
  if (isHtml.value && signatureEnabled.value && signatureHtml.value) {
    content += `<hr style="border:none;border-top:1px solid #e0e0e0;margin-top:20px">${signatureHtml.value}`
  }

  sending.value = true
  try {
    const attachmentPaths = attachments
      .filter(a => a.filePath && !a.uploading)
      .map(a => a.filePath)
      .join(',')

    await sendEmailApi({
      to: toList.value.join(','),
      cc: ccList.value.length > 0 ? ccList.value.join(',') : '',
      bcc: bccList.value.length > 0 ? bccList.value.join(',') : '',
      subject: subject.value.trim(),
      content: content,
      isHtml: isHtml.value,
      attachmentPaths: attachmentPaths || ''
    })

    ElMessage.success('邮件发送成功！')
    handleReset()
  } catch (e) {
    ElMessage.error(parseSendError(e?.message || ''))
  } finally {
    sending.value = false
  }
}

function getHtmlContent() {
  // 从 wangeditor 获取 HTML
  const editor = editorRef.value
  if (editor) {
    const html = editor.getHtml().trim()
    if (html && html !== '<p><br></p>') {
      return html
    }
  }
  return htmlContent.value || ''
}

function parseSendError(msg) {
  if (!msg) return '邮件发送失败，请检查SMTP配置'
  // 550 User not found: xxx@yyy.com
  const userNotFoundMatch = msg.match(/550\s+User not found:\s*(\S+)/i)
  if (userNotFoundMatch) {
    return `收件人邮箱不存在：${userNotFoundMatch[1]}`
  }
  // 550的其他错误（如Mailbox not found等）
  const smtp550Match = msg.match(/550\s+([^;]+)/i)
  if (smtp550Match) {
    return `邮件被拒收：${smtp550Match[1].trim()}`
  }
  // Invalid Addresses
  if (msg.includes('Invalid Addresses') || msg.includes('Invalid Address')) {
    return '收件人邮箱地址格式无效，请检查后重试'
  }
  // 认证失败
  if (msg.includes('AuthenticationFailed') || msg.includes('authentication failed') || msg.includes('535')) {
    return '邮箱登录认证失败，请检查发件人账号密码配置'
  }
  // 连接超时
  if (msg.includes('timeout') || msg.includes('timed out') || msg.includes('connect')) {
    return '连接邮件服务器超时，请检查网络或SMTP配置'
  }
  return '邮件发送失败，请检查收件人地址和SMTP配置'
}

function handleReset() {
  toList.value = []
  ccList.value = []
  bccList.value = []
  subject.value = ''
  plainTextContent.value = ''
  htmlContent.value = ''
  // 清空 wangeditor 编辑器
  const editor = editorRef.value
  if (editor) {
    editor.clear()
  }
  attachments.splice(0, attachments.length)
  showCc.value = false
  showBcc.value = false
  isHtml.value = true
  toInputValue.value = ''
  ccInputValue.value = ''
  bccInputValue.value = ''
}

// ── 工具函数 ──
function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}
</script>

<style lang="scss" scoped>
/*
 * 邮件编辑器样式说明：
 * 本文件 !important 45 处，主要分布在三处：
 *   1. 暗黑模式覆盖（body.dark 下需覆盖 Element Plus 默认主题）
 *   2. :deep() 穿透 wangEditor 富文本编辑器（scoped 隔离后必须提升优先级）
 *   3. :deep() 覆盖 .el-input / .el-card 内部样式（Element Plus 主题色穿透）
 * 这是 vue scoped 机制 + 第三方组件库的合理用法，不要轻易去掉。
 * 后续如需重做，请用 :deep() 替代直接 .el-* 选择器。
 */
.email-sender-page {
  max-width: 1280px;
  margin: 0 auto;
}

.email-compose-card {
  border-radius: 12px;
  overflow: visible;

  :deep(.el-card__body) {
    padding: 0;
  }
}

// ── 邮件头 ──
.email-header {
  padding: 16px 20px 0;
  background: #fafbfc;
}

.email-row {
  display: flex;
  align-items: center;
  min-height: 42px;
  border-bottom: 1px solid #ebeef5;
  padding: 4px 0;

  &:last-child {
    border-bottom: none;
  }
}

.email-label {
  width: 56px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  text-align: right;
  padding-right: 12px;
  line-height: 32px;
}

.email-input-wrap {
  flex: 1;
  min-width: 0;
}

.email-input {
  :deep(.el-input__wrapper) {
    box-shadow: none;
    background: transparent;
    padding: 0 4px;
  }
  :deep(.el-input__inner) {
    font-size: 13px;
    height: 30px;
    line-height: 30px;
  }
}

.email-tag-input {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  min-height: 30px;
  padding: 2px 4px;
  cursor: text;

  :deep(.el-tag) {
    border-radius: 4px;
    height: 24px;
    line-height: 22px;
    font-size: 12px;
    margin: 0;
  }
}

.email-tag-input-inner {
  flex: 1;
  min-width: 120px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  height: 24px;
  line-height: 24px;
  color: #303133;
  padding: 0 2px;
  font-family: inherit;

  &::placeholder {
    color: #c0c4cc;
  }
}

.email-cc-toggle {
  flex-shrink: 0;
  padding-left: 8px;

  .bcc-toggle {
    margin-left: 8px;
  }

  :deep(.el-button) {
    font-size: 12px;
    padding: 2px 8px;
  }
}

// ── 工具栏 ──
.email-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
  flex-wrap: wrap;
  gap: 6px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  gap: 6px;
}

.format-toggle {
  :deep(.el-button) {
    font-size: 12px;
    padding: 4px 10px;
  }
}

// ── 编辑器 ──
.email-editor-wrap {
  min-height: 420px;
}

.rich-editor {
  display: flex;
  flex-direction: column;
  height: 520px !important;
  border: none;
}

.plain-editor {
  :deep(.el-textarea__inner) {
    border: none;
    border-radius: 0;
    box-shadow: none;
    resize: vertical;
    font-size: 14px;
    line-height: 1.8;
    padding: 16px 20px;
    min-height: 320px;

    &:focus {
      box-shadow: none;
    }
  }
}

// ── 附件 ──
.email-attachments {
  border-top: 1px solid #ebeef5;
  padding: 12px 20px;
  background: #fafbfc;
}

.attachment-header {
  margin-bottom: 8px;
}

.attachment-title {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}

.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  font-size: 12px;
  transition: all 0.2s;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  }
}

.attachment-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.att-icon {
  font-size: 18px;
}

.att-name {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #303133;
}

.att-size {
  color: #909399;
  white-space: nowrap;
}

.attachment-progress {
  width: 60px;
}

.att-remove {
  padding: 2px;
}

// ── 底部 ──
.email-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid #ebeef5;
  background: #fafbfc;
}

.footer-left, .footer-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

// ── 签名设置对话框 ──
.signature-settings {
  padding: 0 4px;
}

.signature-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.signature-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.signature-editor-wrap {
  margin-top: 4px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 220px;
}

// ── 全局覆盖 ──
.page-container {
  padding: 16px 20px;
}

.page-header {
  margin-bottom: 16px;

  h3 {
    margin: 0 0 4px;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }

  .page-desc {
    margin: 0;
    font-size: 13px;
    color: #909399;
  }
}

// 响应式
@media (max-width: 768px) {
  .email-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  .email-row {
    flex-wrap: wrap;
  }
  .email-label {
    width: 48px;
    font-size: 12px;
  }
}

// ── 暗黑模式 ──
:global(html.dark) {
  .email-compose-card {
    background: #1d1e1f;
    border-color: #3a3b3c;

    :deep(.el-card__body) {
      background-color: #1d1e1f;
      color: #e0e0e0;
    }
  }

  .email-header {
    background: #252627;

    .email-label { color: #c0c4cc; }
  }

  .email-row {
    border-bottom-color: #3a3b3c;

    .email-input {
      :deep(.el-input__wrapper) {
        background: transparent;
        box-shadow: none;
      }
      :deep(.el-input__inner) {
        background: transparent;
        color: #e0e0e0;

        &::placeholder { color: #6b6d70; }
      }
    }
  }

  .email-tag-input {
    background: transparent;
    border-radius: 4px;

    :deep(.el-tag) { color: #e0e0e0; }
    .email-tag-input-inner { 
      color: #e0e0e0;
      &::placeholder { color: #6b6d70; }
    }
  }

  .email-toolbar {
    background: #1d1e1f;
    border-top-color: #3a3b3c;
    border-bottom-color: #3a3b3c;
  }

  .rich-editor {
    background: #1d1e1f;
  }

  .plain-editor {
    :deep(.el-textarea__inner) {
      color: #e0e0e0;
      background: #1d1e1f;

      &::placeholder { color: #6b6d70; }
    }
  }

  .email-attachments {
    background: #252627;
    border-top-color: #3a3b3c;

    .attachment-title { color: #c0c4cc; }
    .attachment-item {
      background: #2d2e30;
      border-color: #3a3b3c;

      .att-name { color: #e0e0e0; }
      .att-size { color: #909399; }
    }
  }

  .email-footer {
    background: #252627;
    border-top-color: #3a3b3c;
  }

  .signature-editor-wrap {
    background: #252627;
    border-color: #3a3b3c;
  }

  .page-header h3 { color: #e0e0e0; }
}
</style>

<style lang="scss">
/* wangeditor 全局样式（非 scoped） */
.email-sender-page .rich-editor {
  height: 520px !important;

  .w-e-toolbar {
    flex-shrink: 0;
    border-top: none;
    border-left: none;
    border-right: none;
    background: #fafbfc;
    padding: 4px 8px;
    border-radius: 0;

    .w-e-bar-item {
      padding: 0 2px;
    }

    .w-e-bar-item button {
      height: 28px;
      width: 28px;
      padding: 4px;

      &:hover {
        background-color: #ebeef5;
      }
    }

    .w-e-bar .w-e-bar-item {
      svg {
        width: 16px;
        height: 16px;
      }
    }
  }

  /* 下拉面板：字体、字号、对齐等 - 防裁剪 */
  .w-e-bar,
  .w-e-bar-item {
    overflow: visible !important;
  }

  .w-e-drop-panel {
    min-width: 160px !important;
  }

  .w-e-panel-list-item {
    padding: 5px 14px !important;
    white-space: nowrap !important;
    font-size: 14px !important;
    line-height: 2 !important;
  }

  .w-e-text-container {
    flex: 1;
    border: none;
    background: #fff;

    .w-e-text-placeholder {
      font-style: normal;
      top: 16px;
      left: 20px;
      color: #c0c4cc;
      font-size: 14px;
    }

    [data-slate-editor] {
      padding: 16px 20px;
      font-size: 14px;
      line-height: 1.8;
      min-height: 440px;

      p {
        margin: 0 0 8px;
      }
    }
  }
}

/* 签名编辑器 */
.email-sender-page .signature-editor-wrap {
  .w-e-toolbar {
    border: none;
    border-bottom: 1px solid #ebeef5;
    background: #fafbfc;
    padding: 4px 8px;

    .w-e-bar-item button {
      height: 26px;
      width: 26px;
    }
  }

  .w-e-text-container {
    border: none;

    [data-slate-editor] {
      min-height: 160px;
      padding: 12px 16px;
    }
  }
}

// ── 暗黑模式 wangeditor ──
html.dark .email-sender-page .rich-editor {
  .w-e-toolbar {
    background: #252627 !important;
    border-bottom-color: #3a3b3c !important;

    .w-e-bar-item button:hover,
    .w-e-bar-item.active button {
      background-color: #3a3b3c !important;
      color: var(--rx-primary, #409EFF) !important;
    }

    .w-e-bar-item svg {
      fill: #c0c4cc !important;
    }

    .w-e-bar-item.active svg,
    .w-e-bar-item:hover svg {
      fill: var(--rx-primary, #409EFF) !important;
    }
  }

  .w-e-text-container {
    background: #1d1e1f !important;

    [data-slate-editor] {
      color: #e0e0e0 !important;
    }

    .w-e-text-placeholder[data-slate-placeholder]::after {
      color: #6b6d70 !important;
    }

    /* 下拉面板 */
    .w-e-drop-panel {
      background: #2d2e30 !important;
      border-color: #3a3b3c !important;
      box-shadow: 0 4px 12px rgba(0,0,0,0.5) !important;

      .w-e-panel-list-item {
        color: #c0c4cc !important;

        &:hover {
          background: #3a3b3c !important;
          color: #fff !important;
        }
      }
    }

    /* 颜色选择 */
    .w-e-color-picker-popup {
      background: #2d2e30 !important;
      border-color: #3a3b3c !important;
    }
  }
}

html.dark .email-sender-page .signature-editor-wrap {
  background: #252627 !important;
  border-color: #3a3b3c !important;

  .w-e-toolbar {
    background: #2d2e30 !important;
    border-bottom-color: #3a3b3c !important;
  }

  .w-e-text-container {
    background: #252627 !important;

    [data-slate-editor] { color: #e0e0e0 !important; }
  }
}

/* 暗黑模式：全局卡片和布局 */
html.dark .email-sender-page {
  /* el-card 本体背景 */
  .el-card {
    background-color: #1d1e1f !important;
    border-color: #3a3b3c !important;
    color: #e0e0e0;
  }

  .email-header {
    background: #252627 !important;
  }

  .email-toolbar {
    background: #1d1e1f !important;
    border-top-color: #3a3b3c !important;
    border-bottom-color: #3a3b3c !important;

    .el-button:not(.el-button--primary) {
      background: #2d2e30 !important;
      border-color: #3a3b3c !important;
      color: #c0c4cc !important;

      &:hover { color: #409EFF; }
    }
  }

  .email-footer {
    background: #252627 !important;
    border-top-color: #3a3b3c !important;

    .el-button:not(.el-button--primary) {
      background: #2d2e30 !important;
      border-color: #3a3b3c !important;
      color: #c0c4cc !important;

      &:hover { color: #409EFF; }
    }
  }
}
</style>
