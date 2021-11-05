import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Home from '../views/Home.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/doc',
    name: 'Docs',
    component: () => import(/* webpackChunkName: "docs" */ '../views/Docs.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import(/* webpackChunkName: "about" */ '../views/About.vue')
  },
  {
    path: '/admin/ebook',
    name: 'Ebook',
    component: () => import(/* webpackChunkName: "ebook" */ '../views/admin/Ebook.vue')
  },
  {
    path: '/admin/category',
    name: 'Category',
    component: () => import(/* webpackChunkName: "category" */ '../views/admin/Category.vue')
  },
  {
    path: '/admin/doc',
    name: 'Doc',
    component: () => import(/* webpackChunkName: "doc" */ '../views/admin/Doc.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
