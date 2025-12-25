<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const docId = route.params.id
const content = ref('# 欢迎使用协同文档\n\n开始编辑你的内容...')
</script>

<template>
  <div class="document-edit">
    <div class="editor-header">
      <el-input v-model="content" placeholder="文档标题" size="large" style="max-width: 400px" />
      <div class="header-actions">
        <el-button>保存</el-button>
        <el-button type="primary" @click="$router.back()">返回</el-button>
      </div>
    </div>
    
    <div class="editor-container">
      <el-input
        v-model="content"
        type="textarea"
        :autosize="{ minRows: 20 }"
        placeholder="开始编辑..."
        class="editor-textarea"
      />
    </div>
    
    <div class="editor-tip">
      <el-icon><InfoFilled /></el-icon>
      协同编辑功能开发中...（将集成Yjs实时协作）
    </div>
  </div>
</template>

<style scoped>
.document-edit {
  background: white;
  border-radius: 12px;
  padding: 24px;
  min-height: calc(100vh - 200px);
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.editor-container {
  margin-bottom: 16px;
}

.editor-textarea :deep(textarea) {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.editor-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 13px;
}
</style>
