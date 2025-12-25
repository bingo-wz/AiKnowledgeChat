<script setup lang="ts">
import { ref } from 'vue'

const documents = ref([
  { id: 1, title: '项目方案', type: 'collab', updatedAt: '2024-12-24 10:00' },
  { id: 2, title: '研究报告', type: 'upload', updatedAt: '2024-12-23 15:30' }
])

const dialogVisible = ref(false)
const formData = ref({ title: '', type: 'collab' })
</script>

<template>
  <div class="document-list">
    <div class="page-header">
      <h2>协同文档</h2>
      <el-button type="primary" @click="dialogVisible = true">
        <el-icon><Plus /></el-icon>
        新建文档
      </el-button>
    </div>

    <el-table :data="documents" stripe style="width: 100%">
      <el-table-column prop="title" label="文档标题" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 'collab' ? 'primary' : 'success'">
            {{ row.type === 'collab' ? '协同编辑' : '上传文件' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="primary" @click="$router.push(`/document/${row.id}/edit`)">
            编辑
          </el-button>
          <el-button text type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新建文档" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="文档标题" required>
          <el-input v-model="formData.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="文档类型">
          <el-radio-group v-model="formData.type">
            <el-radio value="collab">协同编辑</el-radio>
            <el-radio value="upload">上传文件</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary">创建</el-button>
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
</style>
