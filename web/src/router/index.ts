import store from '@/store'
import { Tool } from '@/util/tool'
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
    path: '/admin/user',
    name: 'User',
    component: () => import(/* webpackChunkName: "user" */ '../views/admin/User.vue'),
    meta: {
      loginRequire: true
    }
  },
  {
    path: '/admin/ebook',
    name: 'Ebook',
    component: () => import(/* webpackChunkName: "ebook" */ '../views/admin/Ebook.vue'),
    meta: {
      loginRequire: true
    }
  },
  {
    path: '/admin/category',
    name: 'Category',
    component: () => import(/* webpackChunkName: "category" */ '../views/admin/Category.vue'),
    meta: {
      loginRequire: true
    }
  },
  {
    path: '/admin/doc',
    name: 'Doc',
    component: () => import(/* webpackChunkName: "doc" */ '../views/admin/Doc.vue'),
    meta: {
      loginRequire: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由登录拦截
router.beforeEach((to, from, next) => {
  // 判断是否需要做拦截处理（通过meta.loginRequire属性）
  if (to.matched.some((item) => {
    console.log(item, "是否需要登录校验：", item.meta.loginRequire);
    return item.meta.loginRequire;
  })) {
    const loginUser = store.state.user;
    if (Tool.isEmpty(loginUser)) {
      console.log("用户未登录！");
      next('/');
    } else {
      next();
    }
  } else {
    next();
  }
})

export default router
