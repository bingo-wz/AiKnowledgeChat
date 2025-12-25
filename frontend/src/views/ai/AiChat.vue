<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { createConversation, sendChatMessage } from '../../api/ai'
import { ElMessage } from 'element-plus'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const messages = ref<Message[]>([])
const inputText = ref('')
const loading = ref(false)
const selectedModel = ref('zhipu')
const conversationId = ref<number | null>(null)

const models = [
  { value: 'zhipu', label: '智谱AI' },
  { value: 'qwen', label: '通义千问' },
  { value: 'deepseek', label: 'DeepSeek' }
]

async function initConversation() {
  if (conversationId.value) return
  try {
    const id = await createConversation('New Chat')
    conversationId.value = id
  } catch (e) {
    console.error('Failed to create conversation', e)
  }
}

async function sendMessage() {
  if (!inputText.value.trim() || loading.value) return
  
  const userMsg = inputText.value
  messages.value.push({ role: 'user', content: userMsg })
  inputText.value = ''
  loading.value = true
  
  scrollToBottom()
  
  try {
    // 如果没有对话ID，先创建
    if (!conversationId.value) {
      conversationId.value = await createConversation('New Chat')
    }
    
    // 调用真实API
    const response = await sendChatMessage(
      conversationId.value,
      userMsg,
      selectedModel.value
    )
    
    messages.value.push({
      role: 'assistant',
      content: response
    })
  } catch (e: any) {
    ElMessage.error(e.message || 'AI回复失败')
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI回复失败，请稍后重试。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    const container = document.querySelector('.chat-messages')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

function clearChat() {
  messages.value = []
  conversationId.value = null
}

onMounted(() => {
  initConversation()
})
</script>

<template>
  <div class="ai-chat">
    <el-row :gutter="20">
      <!-- Chat Area -->
      <el-col :span="18">
        <el-card class="chat-card">
          <template #header>
            <div class="chat-header">
              <h3><el-icon><ChatDotRound /></el-icon> AI教研助手</h3>
              <div class="header-actions">
                <el-select v-model="selectedModel" size="small" style="width: 140px">
                  <el-option v-for="m in models" :key="m.value" :label="m.label" :value="m.value" />
                </el-select>
                <el-button size="small" @click="clearChat">清空对话</el-button>
              </div>
            </div>
          </template>

          <div class="chat-messages">
            <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
              <div class="message-avatar">
                <el-avatar v-if="msg.role === 'user'" :size="36">U</el-avatar>
                <el-avatar v-else :size="36" style="background: linear-gradient(135deg, #667eea, #764ba2)">
                  <el-icon><MagicStick /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
              </div>
            </div>
            
            <div v-if="loading" class="message-item assistant">
              <div class="message-avatar">
                <el-avatar :size="36" style="background: linear-gradient(135deg, #667eea, #764ba2)">
                  <el-icon><MagicStick /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>

            <el-empty v-if="messages.length === 0 && !loading" description="开始与AI助手对话">
              <template #image>
                <el-icon :size="64" color="#c0c4cc"><ChatDotSquare /></el-icon>
              </template>
            </el-empty>
          </div>

          <div class="chat-input">
            <el-input
              v-model="inputText"
              placeholder="输入您的问题... (Enter发送)"
              :disabled="loading"
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button type="primary" :loading="loading" @click="sendMessage">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </el-col>

      <!-- Sidebar -->
      <el-col :span="6">
        <el-card class="sidebar-card">
          <template #header>对话历史</template>
          <div class="history-list">
            <div class="history-item active">
              <el-icon><ChatLineSquare /></el-icon>
              <span>当前对话</span>
            </div>
            <el-empty v-if="true" :image-size="60" description="暂无历史记录" />
          </div>
        </el-card>

        <el-card class="sidebar-card" style="margin-top: 16px">
          <template #header>关联知识库</template>
          <div class="kb-list">
            <el-checkbox>教学资料库</el-checkbox>
            <el-checkbox>研究文献</el-checkbox>
          </div>
          <div class="tip">
            <el-icon><InfoFilled /></el-icon>
            选择知识库后，AI将基于知识库内容回答
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
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.user .message-text {
  background: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.assistant .message-text {
  background: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 12px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

.chat-input {
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.sidebar-card {
  height: fit-content;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.history-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.kb-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 16px;
  padding: 10px;
  background: #fdf6ec;
  border-radius: 6px;
  font-size: 12px;
  color: #e6a23c;
}
</style>
