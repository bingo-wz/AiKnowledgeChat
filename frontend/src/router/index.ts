import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        name: 'Layout',
        component: () => import('../views/Layout.vue'),
        redirect: '/classroom',
        meta: { requiresAuth: true },
        children: [
            {
                path: 'classroom',
                name: 'Classroom',
                component: () => import('../views/classroom/ClassroomList.vue')
            },
            {
                path: 'classroom/:id',
                name: 'ClassroomDetail',
                component: () => import('../views/classroom/ClassroomDetail.vue')
            },
            {
                path: 'document',
                name: 'Document',
                component: () => import('../views/document/DocumentList.vue')
            },
            {
                path: 'document/:id/edit',
                name: 'DocumentEdit',
                component: () => import('../views/document/DocumentEdit.vue')
            },
            {
                path: 'kb',
                name: 'KnowledgeBase',
                component: () => import('../views/kb/KnowledgeBaseList.vue')
            },
            {
                path: 'ai',
                name: 'AiChat',
                component: () => import('../views/ai/AiChat.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, _from, next) => {
    const token = localStorage.getItem('token')
    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.path === '/login' && token) {
        next('/')
    } else {
        next()
    }
})

export default router
