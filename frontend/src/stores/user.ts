import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register, getUserInfo } from '../api/auth'

interface User {
    userId: number
    username: string
    nickname: string
    avatar: string | null
    role: string
}

export const useUserStore = defineStore('user', () => {
    const user = ref<User | null>(null)
    const token = ref<string>(localStorage.getItem('token') || '')

    async function loginAction(username: string, password: string) {
        const res = await login(username, password)
        user.value = res
        token.value = res.token
        localStorage.setItem('token', res.token)
        return res
    }

    async function registerAction(data: { username: string; password: string; nickname: string; role: string }) {
        await register(data)
    }

    async function fetchUserInfo() {
        if (!token.value) return
        try {
            const res = await getUserInfo()
            user.value = res
        } catch {
            logout()
        }
    }

    function logout() {
        user.value = null
        token.value = ''
        localStorage.removeItem('token')
    }

    return { user, token, loginAction, registerAction, fetchUserInfo, logout }
})
