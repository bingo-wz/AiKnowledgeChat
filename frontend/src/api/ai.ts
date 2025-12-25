import request from './request'

// 获取可用模型列表
export function getModels() {
    return request.get('/ai/models')
}

// 创建对话
export function createConversation(title: string, kbId?: number) {
    return request.post('/ai/conversation', { title, kbId })
}

// 获取对话列表
export function getConversations(page = 1, size = 20) {
    return request.get('/ai/conversations', { params: { page, size } })
}

// 获取对话消息
export function getConversationMessages(conversationId: number) {
    return request.get(`/ai/conversation/${conversationId}/messages`)
}

// 发送消息
export function sendChatMessage(conversationId: number, message: string, model: string) {
    return request.post('/ai/chat', { conversationId, message, model })
}

// 删除对话
export function deleteConversation(id: number) {
    return request.delete(`/ai/conversation/${id}`)
}
