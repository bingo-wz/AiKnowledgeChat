<script setup lang="ts">
import { useUserStore } from './stores/user'
import { onMounted } from 'vue'

const userStore = useUserStore()

onMounted(() => {
  if (userStore.token) {
    userStore.fetchUserInfo()
  }
})
</script>

<template>
  <router-view />
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}
</style>
