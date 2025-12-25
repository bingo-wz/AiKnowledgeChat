<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  role: 'student'
})

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.loginAction(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await userStore.registerAction({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname,
      role: registerForm.role
    })
    ElMessage.success('注册成功，请登录')
    isLogin.value = true
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="logo">
        <el-icon :size="48" color="#409eff"><Reading /></el-icon>
        <h1>AI智慧教研室</h1>
      </div>
      
      <!-- Login Form -->
      <el-form v-if="isLogin" @submit.prevent="handleLogin" class="form">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
          登 录
        </el-button>
        <div class="switch-mode">
          没有账号？<el-link type="primary" @click="isLogin = false">立即注册</el-link>
        </div>
      </el-form>

      <!-- Register Form -->
      <el-form v-else @submit.prevent="handleRegister" class="form">
        <el-form-item>
          <el-input v-model="registerForm.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="registerForm.nickname" placeholder="昵称" prefix-icon="UserFilled" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="registerForm.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-select v-model="registerForm.role" placeholder="角色" size="large" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width: 100%">
          注 册
        </el-button>
        <div class="switch-mode">
          已有账号？<el-link type="primary" @click="isLogin = true">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo h1 {
  margin: 12px 0 0;
  font-size: 24px;
  color: #303133;
}

.form {
  margin-top: 20px;
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
  color: #909399;
}
</style>
