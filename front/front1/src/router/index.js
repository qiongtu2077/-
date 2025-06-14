import { createRouter, createWebHistory } from 'vue-router'
import Auth from '../views/Auth.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'auth',
      component: Auth
    },
    {
      path: '/posts',
      name: 'posts',
      component: () => import('../views/Posts.vue')
    },
    {
      path: '/create-post',
      name: 'create-post',
      component: () => import('../views/CreatePost.vue')
    },
    {
      path: '/admin/dashboard',                  // ✅ 管理员后台首页
      name: 'admin-dashboard',
      component: () => import('../views/AdminDashboard.vue')
    }
  ]
})

export default router
