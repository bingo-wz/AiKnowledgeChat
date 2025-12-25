<script setup lang="ts">
import { ref } from 'vue'

const knowledgeBases = ref([
  { id: 1, name: '教学资料库', description: '包含课程讲义和参考资料', docCount: 12, isPublic: true },
  { id: 2, name: '研究文献', description: '学术论文和研究报告', docCount: 8, isPublic: false }
])

const dialogVisible = ref(false)
const uploadDialogVisible = ref(false)
const currentKbId = ref<number | null>(null)
const formData = ref({ name: '', description: '', isPublic: false })
</script>

<template>
  <div class="kb-list">
    <div class="page-header">
      <h2>知识库</h2>
      <el-button type="primary" @click="dialogVisible = true">
        <el-icon><Plus /></el-icon>
        创建知识库
      </el-button>
    </div>

    <div class="kb-grid">
      <el-card v-for="kb in knowledgeBases" :key="kb.id" class="kb-card" shadow="hover">
        <div class="kb-icon">
          <el-icon :size="36" color="#409eff"><FolderOpened /></el-icon>
        </div>
        <div class="kb-info">
          <h3>{{ kb.name }}</h3>
          <p>{{ kb.description }}</p>
          <div class="kb-meta">
            <span><el-icon><Document /></el-icon> {{ kb.docCount }} 个文档</span>
            <el-tag :type="kb.isPublic ? 'success' : 'info'" size="small">
              {{ kb.isPublic ? '公开' : '私有' }}
            </el-tag>
          </div>
        </div>
        <div class="kb-actions">
          <el-button type="primary" size="small" @click="currentKbId = kb.id; uploadDialogVisible = true">
            <el-icon><Upload /></el-icon>
            上传文档
          </el-button>
          <el-button size="small" @click="$router.push('/ai')">
            <el-icon><ChatDotRound /></el-icon>
            开始对话
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="创建知识库" width="480px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="formData.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="是否公开">
          <el-switch v-model="formData.isPublic" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary">创建</el-button>
      </template>
    </el-dialog>

    <!-- Upload Dialog -->
    <el-dialog v-model="uploadDialogVisible" title="上传文档" width="500px">
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        accept=".pdf,.doc,.docx,.txt,.md"
      >
        <el-icon :size="48"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 PDF、Word、TXT、Markdown 格式</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.kb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.kb-card {
  display: flex;
  gap: 16px;
  padding: 20px;
}

.kb-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  background: #ecf5ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.kb-info {
  flex: 1;
}

.kb-info h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.kb-info p {
  margin: 0 0 12px;
  color: #909399;
  font-size: 13px;
}

.kb-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.kb-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.kb-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
