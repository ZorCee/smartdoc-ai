import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        redirect: '/workspace'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/workspace',
        name: 'Workspace',
        component: () => import('../views/Workspace.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局前置路由守卫：未携带 Token 房卡禁止进入工作台
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('satoken')
    if (to.path !== '/login' && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router