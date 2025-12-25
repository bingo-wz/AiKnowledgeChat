import request from './request'

export function login(username: string, password: string) {
    return request.post('/auth/login', { username, password })
}

export function register(data: { username: string; password: string; nickname: string; role: string }) {
    return request.post('/auth/register', data)
}

export function getUserInfo() {
    return request.get('/auth/user')
}

export function logout() {
    return request.post('/auth/logout')
}
