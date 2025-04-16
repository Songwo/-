import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router'; // 使用 type 导入
import store from '../store';

const routes: RouteRecordRaw[] = [  // 指定 RouteRecordRaw 类型
  {
    path: '/',
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
        path: 'comment',
        name: 'comment',
        component: () => import('@/components/backMange/CommentMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'data',
        name: 'data',
        component: () => import('@/components/backMange/DataMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user',
        name: 'user',
        component: () => import('@/components/backMange/UserMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'video',
        name: 'video',
        component: () => import('@/components/backMange/VideoMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'bug',
        name: 'bug',
        component: () => import('@/components/backMange/BugMange.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'category',
        name: 'category',
        component: () => import('@/components/backMange/Category.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'ques',
        name: 'ques',
        component: () => import('@/components/backMange/Quesstion.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'reword',
        name: 'reword',
        component: () => import('@/components/backMange/Reword.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/root',
    name: 'Root',
    component: () => import('@/components/home/Root/Root.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'home', name: 'home', component: () => import('@/components/home/HomePage.vue'), meta: { requiresAuth: true } },
      { path: 'pricate', name: 'pricate', component: () => import('@/components/home/Pricate.vue'), meta: { requiresAuth: true } },
      { path: 'atack', name: 'atack', component: () => import('@/components/home/Atack.vue'), meta: { requiresAuth: true } },
      { path: 'ConunityTalk', name: 'ConunityTalk', component: () => import('@/components/home/ConunityTalk.vue'), meta: { requiresAuth: true } },
      { path: 'bughole', name: 'BugHole', component: () => import('@/components/home/BugHole.vue'), meta: { requiresAuth: true } },
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
      { path: 'app', name: 'app', component: () => import('@/components/app_download/Dow.vue'),meta: { requiresAuth: true } },
      { path: 'chat-wacyg', name: 'chat-wacyg', component: () => import('@/components/ai_answer/aianswer.vue'),meta: { requiresAuth: true } },
      { path: 'aboutUs', name: 'aboutUs', component: () => import('@/components/home/aboutUs.vue'),meta: { requiresAuth: true } }
    ],
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

//路由守卫
router.beforeEach((to, from, next) => {
  const token = store.state.token || localStorage.getItem('token');
/*   console.log('store:', store.state.token);
  console.log('localStorage:', localStorage.getItem('token'));
  console.log('Use 路由'); */
  
  // 严格检查 token 是否存在且不为 null 或空字符串
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token || token === 'null' || token === '') {  // 检查 token 是否无效
      console.log('[未登录或无效token] 跳转到登录页');
      next('/'); // 跳转到登录页
    } else {
      console.log('[已登录] 继续访问');
      next(); // 继续访问
    }
  } else {
    console.log('[公开页面] 允许访问');
    next(); // 公开页面直接访问
  }
});

export default router;