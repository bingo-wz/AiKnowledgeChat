<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getClassroomDetail, getClassroomMembers, getChatMessages } from '../../api/classroom'
import { useUserStore } from '../../stores/user'

interface Message {
  id: number
  senderId: number
  senderName: string
  senderAvatar: string
  content: string
  msgType: string
  createTime: string
}

interface Member {
  userId: number
  nickname: string
  avatar: string
  role: string
}

const route = useRoute()
const userStore = useUserStore()
const classroomId = ref<number>(0)

const classroom = ref<any>(null)
const messages = ref<Message[]>([])
const members = ref<Member[]>([])
const newMessage = ref('')
const loading = ref(false)
let ws: WebSocket | null = null

async function loadData() {
  if (!classroomId.value || classroomId.value <= 0) {
    console.error('Invalid classroom ID')
    return
  }
  loading.value = true
  try {
    const [detail, memberList, msgPage] = await Promise.all([
      getClassroomDetail(classroomId.value),
      getClassroomMembers(classroomId.value),
      getChatMessages(classroomId.value)
    ])
    classroom.value = detail
    members.value = memberList || []
    messages.value = (msgPage?.records || []).reverse()
  } catch (e) {
    console.error('Failed to load data', e)
  } finally {
    loading.value = false
  }
}

function connectWebSocket() {
  if (!classroomId.value || classroomId.value <= 0) return
  
  ws = new WebSocket(`ws://localhost:8080/ws/chat/${classroomId.value}`)
  
  ws.onopen = () => {
    console.log('WebSocket connected')
  }
  
  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      messages.value.push(msg)
      scrollToBottom()
    } catch (e) {
      console.error('Failed to parse message', e)
    }
  }
  
  ws.onclose = () => {
    console.log('WebSocket disconnected')
  }
}

function sendMessage() {
  if (!newMessage.value.trim() || !ws) return
  
  const msg = {
    type: 'chat',
    content: newMessage.value,
    senderId: userStore.user?.userId,
    senderName: userStore.user?.nickname
  }
  
  ws.send(JSON.stringify(msg))
  newMessage.value = ''
}

function scrollToBottom() {
  setTimeout(() => {
    const container = document.querySelector('.chat-messages')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }, 50)
}

onMounted(() => {
  const id = Number(route.params.id)
  if (id && !isNaN(id) && id > 0) {
    classroomId.value = id
    loadData()
    connectWebSocket()
  } else {
    console.error('Invalid classroom ID from route:', route.params.id)
  }
})

onUnmounted(() => {
  ws?.close()
})
</script>

<template>
  <div class="classroom-detail">
    <el-row :gutter="20">
      <!-- Chat Area -->
      <el-col :span="18">
        <el-card class="chat-card">
          <template #header>
            <div class="chat-header">
              <h3>{{ classroom?.name || '课堂聊天' }}</h3>
              <span class="member-count">{{ members.length }}人在线</span>
            </div>
          </template>
          
          <div class="chat-messages" v-loading="loading">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-item"
              :class="{ 'is-self': msg.senderId === userStore.user?.userId }"
            >
              <el-avatar :size="36" :src="msg.senderAvatar || undefined">
                {{ msg.senderName?.charAt(0) }}
              </el-avatar>
              <div class="message-content">
                <div class="message-sender">{{ msg.senderName }}</div>
                <div class="message-text">{{ msg.content }}</div>
              </div>
            </div>
            <el-empty v-if="messages.length === 0" description="暂无消息" />
          </div>
          
          <div class="chat-input">
            <el-input
              v-model="newMessage"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button type="primary" @click="sendMessage">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </el-col>

      <!-- Members -->
      <el-col :span="6">
        <el-card class="members-card">
          <template #header>
            <span>课堂成员</span>
          </template>
          <div class="member-list">
            <div v-for="m in members" :key="m.userId" class="member-item">
              <el-avatar :size="32" :src="m.avatar || undefined">
                {{ m.nickname?.charAt(0) }}
              </el-avatar>
              <span class="member-name">{{ m.nickname }}</span>
              <el-tag v-if="m.role === 'teacher'" type="primary" size="small">教师</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.chat-card {
  height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
}

.member-count {
  color: #909399;
  font-size: 13px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  min-height: 400px;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-item.is-self {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
}

.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.is-self .message-sender {
  text-align: right;
}

.message-text {
  background: white;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  color: #303133;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.is-self .message-text {
  background: #409eff;
  color: white;
}

.chat-input {
  padding-top: 16px;
}

.members-card {
  height: calc(100vh - 160px);
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-name {
  flex: 1;
  font-size: 14px;
}
</style>
