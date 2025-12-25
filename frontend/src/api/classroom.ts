import request from './request'

export function getClassrooms() {
    return request.get('/classroom/my')
}

export function getClassroomDetail(id: number) {
    return request.get(`/classroom/${id}`)
}

export function createClassroom(data: { name: string; description?: string }) {
    return request.post('/classroom', data)
}

export function updateClassroom(id: number, data: { name: string; description?: string }) {
    return request.put(`/classroom/${id}`, data)
}

export function deleteClassroom(id: number) {
    return request.delete(`/classroom/${id}`)
}

export function joinClassroom(id: number) {
    return request.post(`/classroom/${id}/join`)
}

export function leaveClassroom(id: number) {
    return request.post(`/classroom/${id}/leave`)
}

export function getClassroomMembers(id: number) {
    return request.get(`/classroom/${id}/members`)
}

export function getChatMessages(classroomId: number, page = 1, size = 50) {
    return request.get(`/chat/${classroomId}/messages`, { params: { page, size } })
}
