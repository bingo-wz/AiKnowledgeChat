<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapsed = ref(false)

const menuItems = [
  { path: '/classroom', icon: 'School', label: '在线课堂' },
  { path: '/document', icon: 'Document', label: '协同文档' },
  { path: '/kb', icon: 'FolderOpened', label: '知识库' },
  { path: '/ai', icon: 'ChatDotRound', label: 'AI助手' }
]

const activeMenu = computed(() => {
  return '/' + (route.path.split('/')[1] || 'classroom')
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo" @click="isCollapsed = !isCollapsed">
        <el-icon :size="28" color="#fff"><Reading /></el-icon>
        <span v-if="!isCollapsed" class="logo-text">AI智慧教研室</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        background-color="#1e1e2d"
        text-color="#a2a3b7"
        active-text-color="#fff"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- Main Content -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ menuItems.find(m => m.path === activeMenu)?.label }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar || undefined">
                {{ userStore.user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.user?.nickname || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #1e1e2d;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.logo-text {
  color: white;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
}

.header {
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  color: #303133;
  font-size: 14px;
}

.main-content {
  background: #f0f2f5;
  padding: 24px;
}
</style>
