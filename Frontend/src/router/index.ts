import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router'; // 使用 type 导入
import store from '../store';
import { ElMessage } from 'element-plus';

const routes: RouteRecordRaw[] = [  //使用RouteRecordRaw 类型
  {
    path: '/',
    redirect: '/root/home'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/components/Login_Register.vue')
  },
  {
    path: '/backMange',
    name: 'backMange',
    component: () => import('@/components/backMange/Root/root.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: 'home'
      },
      {
        path: 'home',
        name: 'backMangeHome',
        component: () => import('@/components/backMange/home.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'comment',
        name: 'comment',
        component: () => import('@/components/backMange/DateMange/CommentMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'data',
        name: 'data',
        component: () => import('@/components/backMange/UnderMange/DataMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user',
        name: 'user',
        component: () => import('@/components/backMange/UserMange/UserMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'video',
        name: 'video',
        component: () => import('@/components/backMange/DateMange/VideoMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'bug',
        name: 'bug',
        component: () => import('@/components/backMange/SystemMange/BugMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'category',
        name: 'category',
        component: () => import('@/components/backMange/SystemMange/Category.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'ques',
        name: 'ques',
        component: () => import('@/components/backMange/SystemMange/Quesstion.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'reword',
        name: 'reword',
        component: () => import('@/components/backMange/UserMange/Reword.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'announcement',
        name: 'announcement',
        component: () => import('@/components/backMange/DateMange/AnnouncementMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'trend',
        name: 'trend',
        component: () => import('@/components/backMange/UnderMange/trend.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('@/components/backMange/settings.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'help',
        name: 'help',
        component: () => import('@/components/backMange/help.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'document',
        name: 'document',
        component: () => import('@/components/backMange/document.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/components/backMange/profile.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/root',
    name: 'Root',
    component: () => import('@/components/home/Root/Root.vue'),
    children: [
      { path: 'home', name: 'home', component: () => import('@/components/home/HomePage.vue') },
      { path: 'aboutUs', name: 'aboutUs', component: () => import('@/components/home/aboutUs.vue') },
      { path: 'bughole', name: 'BugHole', component: () => import('@/components/home/BugHole.vue') },
      { path: 'app', name: 'app', component: () => import('@/components/app_download/Dow.vue') },
      { path: 'ConunityTalk', name: 'ConunityTalk', component: () => import('@/components/home/ConunityTalk.vue') },
      { path: 'pricate', name: 'pricate', component: () => import('@/components/home/Pricate.vue'), meta: { requiresAuth: true } },
      { path: 'atack', name: 'atack', component: () => import('@/components/home/Atack.vue'), meta: { requiresAuth: true } },
      { path: 'cms', name: 'cms', component: () => import('@/components/home/Pricate_until/CmsTool.vue'), meta: { requiresAuth: true } },
      { path: 'base64', name: 'base64', component: () => import('@/components/home/Pricate_until/Base64.vue'), meta: { requiresAuth: true } },
      { path: 'findIp', name: 'findIp', component: () => import('@/components/home/Pricate_until/FindIp.vue'), meta: { requiresAuth: true } },
      { path: 'Crehash', name: 'Crehash', component: () => import('@/components/home/Pricate_until/CreHash.vue'), meta: { requiresAuth: true } },
      { path: 'CheckPwd', name: 'CheckPwd', component: () => import('@/components/home/Pricate_until/CheckPwd.vue'), meta: { requiresAuth: true } },
      { path: 'mine', name: 'mine', component: () => import('@/components/home/MineMes.vue'), meta: { requiresAuth: true } },
      { path: 'question', name: 'question', component: () => import('@/components/home/Question.vue'), meta: { requiresAuth: true } },
      { path: 'sortMine', name: 'sortMine', component: () => import('@/components/home/SortMine.vue'), meta: { requiresAuth: true } },
      { path: 'reward', name: 'reward', component: () => import('@/components/home/reward.vue'), meta: { requiresAuth: true } },
      { path: 'game', name: 'game', component: () => import('@/components/gameView/gameExample.vue'), meta: { requiresAuth: true } },
      { path: 'chat-wacyg', name: 'chat-wacyg', component: () => import('@/components/ai_answer/aianswer.vue'), meta: { requiresAuth: true } },
      { path: 'subdomain', name: 'subdomain', component: () => import('@/components/home/Pricate_until/Subdomain.vue'), meta: { requiresAuth: true } },
      { path: 'portscan', name: 'portscan', component: () => import('@/components/home/Pricate_until/PortScan.vue'), meta: { requiresAuth: true } },
      { path: 'urlencode', name: 'urlencode', component: () => import('@/components/home/Pricate_until/UrlEncode.vue'), meta: { requiresAuth: true } },
      { path: 'caesar', name: 'caesar', component: () => import('@/components/home/Pricate_until/CaesarCipher.vue'), meta: { requiresAuth: true } },
      { path: 'unicode', name: 'unicode', component: () => import('@/components/home/Pricate_until/Unicode.vue'), meta: { requiresAuth: true } },
    ],
  },

];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 设置属性始终滚动到顶部
    return { top: 0 }
  }
});

//路由守卫
router.beforeEach((to, from, next) => {
  const token = store.state.token || localStorage.getItem('token');
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token || token === 'null' || token === '') {
      ElMessage.warning('该功能需要登录后才能使用');
      if (from.name === 'login') {
        next(false);
      } else {
        next('/login');
      }
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;