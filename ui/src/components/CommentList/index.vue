<template>
  <div class="comment-container">
    <!-- 评论输入框 -->
    <div class="comment-input">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="500"
        show-word-limit
      />
      <div class="input-actions">
        <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="!newComment.trim()">
          发表评论
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list" v-loading="loading">
      <div v-if="comments.length === 0" class="empty-tip">
        <el-empty description="暂无评论" :image-size="80" />
      </div>
      
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <el-avatar :size="32" :src="comment.avatar">
            {{ comment.username?.charAt(0) }}
          </el-avatar>
          <div class="comment-meta">
            <span class="comment-user">{{ comment.username }}</span>
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          </div>
          <el-button
            v-if="canDelete(comment)"
            type="danger"
            link
            size="small"
            @click="handleDelete(comment.id)"
          >
            删除
          </el-button>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadComments"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentPage, addComment, deleteComment } from '@/api/comment'

const props = defineProps({
  targetType: { type: String, required: true },
  targetId: { type: [Number, String], required: true }
})

const loading = ref(false)
const submitting = ref(false)
const newComment = ref('')
const comments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const canDelete = (comment) => {
  // TODO: 从 store 获取当前用户 ID
  const currentUserId = 1
  return comment.userId === currentUserId
}

const loadComments = async () => {
  loading.value = true
  try {
    const res = await getCommentPage({
      targetType: props.targetType,
      targetId: props.targetId,
      page: currentPage.value,
      size: pageSize.value
    })
    comments.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载评论失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!newComment.value.trim()) return
  
  submitting.value = true
  try {
    await addComment({
      targetType: props.targetType,
      targetId: props.targetId,
      content: newComment.value.trim()
    })
    ElMessage.success('评论发表成功')
    newComment.value = ''
    await loadComments()
  } catch (e) {
    console.error('发表评论失败:', e)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      type: 'warning'
    })
    await deleteComment(id)
    ElMessage.success('删除成功')
    await loadComments()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除评论失败:', e)
    }
  }
}

watch(() => [props.targetType, props.targetId], () => {
  currentPage.value = 1
  loadComments()
})

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-container {
  margin-top: 20px;
}

.comment-input {
  margin-bottom: 20px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.comment-list {
  min-height: 200px;
}

.empty-tip {
  padding: 40px 0;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-meta {
  flex: 1;
}

.comment-user {
  font-weight: 500;
  margin-right: 12px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  padding-left: 44px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
