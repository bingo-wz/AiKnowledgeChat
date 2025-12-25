<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getClassrooms, createClassroom, deleteClassroom } from '../../api/classroom'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Classroom {
  id: number
  name: string
  description: string
  teacherId: number
  teacherName: string
  memberCount: number
  createTime: string
}

const router = useRouter()
const classrooms = ref<Classroom[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const formData = ref({ name: '', description: '' })

async function loadClassrooms() {
  loading.value = true
  try {
    classrooms.value = await getClassrooms()
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!formData.value.name) {
    ElMessage.warning('请输入课堂名称')
    return
  }
  try {
    await createClassroom(formData.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    formData.value = { name: '', description: '' }
    loadClassrooms()
  } catch {
    // handled
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该课堂吗？', '提示', { type: 'warning' })
    await deleteClassroom(id)
    ElMessage.success('删除成功')
    loadClassrooms()
  } catch {
    // cancelled
  }
}

function enterClassroom(id: number) {
  router.push(`/classroom/${id}`)
}

onMounted(loadClassrooms)
</script>

<template>
  <div class="classroom-list">
    <div class="page-header">
      <h2>在线课堂</h2>
      <el-button type="primary" @click="dialogVisible = true">
        <el-icon><Plus /></el-icon>
        创建课堂
      </el-button>
    </div>

    <div v-loading="loading" class="card-grid">
      <el-card v-for="item in classrooms" :key="item.id" class="classroom-card" shadow="hover" @click="enterClassroom(item.id)">
        <div class="card-cover">
          <el-icon :size="48" color="#409eff"><School /></el-icon>
        </div>
        <div class="card-content">
          <h3>{{ item.name }}</h3>
          <p class="desc">{{ item.description || '暂无描述' }}</p>
          <div class="card-meta">
            <span><el-icon><User /></el-icon> {{ item.teacherName }}</span>
            <span><el-icon><UserFilled /></el-icon> {{ item.memberCount }}人</span>
          </div>
        </div>
        <div class="card-actions" @click.stop>
          <el-button text type="danger" size="small" @click="handleDelete(item.id)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </el-card>

      <el-empty v-if="!loading && classrooms.length === 0" description="暂无课堂" />
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="创建课堂" width="480px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="课堂名称" required>
          <el-input v-model="formData.name" placeholder="请输入课堂名称" />
        </el-form-item>
        <el-form-item label="课堂描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入课堂描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
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
  color: #303133;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.classroom-card {
  cursor: pointer;
  transition: transform 0.2s;
  position: relative;
}

.classroom-card:hover {
  transform: translateY(-4px);
}

.card-cover {
  height: 100px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin: -20px -20px 16px;
}

.card-cover .el-icon {
  color: white !important;
}

.card-content h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
}

.desc {
  color: #909399;
  font-size: 13px;
  margin: 0 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  position: absolute;
  top: 8px;
  right: 8px;
}
</style>
