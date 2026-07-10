<template>
  <div class="table-skeleton">
    <div v-if="showToolbar" class="skeleton-toolbar">
      <el-skeleton-item variant="button" style="width: 100px; height: 32px" />
      <el-skeleton-item variant="input" style="width: 200px; height: 32px; margin-left: auto" />
    </div>
    <el-skeleton :rows="rows" animated />
  </div>
</template>

<script setup>
defineOptions({ name: 'TableSkeleton' })
defineProps({
  rows: { type: Number, default: 8 },
  showToolbar: { type: Boolean, default: true }
})
</script>

<style scoped>
.table-skeleton {
  padding: 16px 0;
}
.skeleton-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
