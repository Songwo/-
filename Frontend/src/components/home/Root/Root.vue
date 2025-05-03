<template>
  <div class="container">
    <!-- Header -->
    <el-header class="header">
      <div class="logo">
        <img src="@/assets/logo/logo/信息.png" alt="白帽工坊logo" class="logo-img">
        白帽工坊
      </div>
      <!-- 移动端菜单按钮 -->
      <div class="mobile-menu-btn" @click="toggleMobileMenu">
        <el-icon>
          <Menu />
        </el-icon>
      </div>
      <!-- 导航菜单 -->
      <el-menu class="menu" mode="horizontal" :default-active="activeMenu" :class="{ 'mobile-menu': isMobileMenuOpen }">
        <el-menu-item v-for="(item, key) in filteredMenuItems" :key="key" :index="key">
          <RouterLink :to="`/root/${key}`" class="router-link">{{ item.label }}</RouterLink>
        </el-menu-item>
        <!-- 更多菜单项下的下拉菜单 -->
        <el-menu-item class="more">
          <el-dropdown trigger="click">
            <span>更多</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="downloadApp">App端下载</el-dropdown-item>
                <el-dropdown-item v-if="isLoggedIn" @click="aiAnswer">AI智能解答</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-menu-item>
      </el-menu>

      <!-- User Info -->
      <template v-if="isLoggedIn">
        <el-dropdown class="user-dropdown" trigger="click">
          <span class="user-info">
            <el-avatar :src="at.avatar" size="32" class="user-avatar" />
            你好，{{ at.user }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="Mine">个人信息</el-dropdown-item>
              <el-dropdown-item @click="laout">退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <div class="login-buttons">
          <el-button type="primary" @click="goToLogin">登录</el-button>
        </div>
      </template>
    </el-header>

    <!-- Main Content -->
    <div class="main-content">
      <RouterView></RouterView>
    </div>

    <!-- 页脚 -->
    <footer class="app-footer">
      <p>
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">
          ICP备2025024694号
        </a>
      </p>
      <p>
        联系我们: <a href="mailto:zhaoqsnyah@gmail.com">zhaoqsnyah@gmail.com</a>
      </p>
      <p class="disclaimer">
        警告：任何未经授权访问、攻击或窃取本网站数据的行为均属违法，必将追究法律责任。
      </p>
    </footer>
  </div>
</template>

<script setup name="Root">
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import { computed, ref, onMounted, watch } from 'vue'
import ToUrl from '@/api/api'
import { Menu } from '@element-plus/icons-vue'
import gsap from 'gsap'

// 移动端菜单状态
const isMobileMenuOpen = ref(false)

// 切换移动端菜单
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
  if (isMobileMenuOpen.value) {
    gsap.fromTo('.mobile-menu .el-menu-item', 
      { 
        opacity: 0,
        y: -20
      },
      { 
        opacity: 1,
        y: 0,
        duration: 0.5,
        stagger: 0.1,
        ease: 'power2.out'
      }
    )
  }
}

// 使用 route 路径匹配标签
const route = useRoute()
const router = useRouter()
const store = useStore()

// 动态加载用户名
const at = computed(() => ({
  token: store.state.token,
  user: store.state.user,
  avatar: `${ToUrl.url}/${store.state.avatar}`
}))

// 判断是否登录
const isLoggedIn = computed(() => {
  return store.state.token && store.state.token !== 'null' && store.state.token !== '';
});

// 菜单项定义
const menuItems = {
  home: { label: '首页', requireAuth: false },
  pricate: { label: '实战工具', requireAuth: true },
  atack: { label: '攻防课程', requireAuth: true },
  bughole: { label: '漏洞库', requireAuth: false },
  ConunityTalk: { label: '社区论坛', requireAuth: false },
  Question: { label: '答题测试', requireAuth: true },
  sortMine: { label: '个人排名', requireAuth: true },
  reward: { label: '奖励页面', requireAuth: true },
  game: { label: '游戏学习', requireAuth: true },
  aboutUs: { label: '关于我们', requireAuth: false }
};

// 根据登录状态过滤菜单项
const filteredMenuItems = computed(() => {
  const filtered = {};
  Object.entries(menuItems).forEach(([key, item]) => {
    if (!item.requireAuth || isLoggedIn.value) {
      filtered[key] = item;
    }
  });
  return filtered;
});

// 根据当前路由路径返回匹配的菜单 index
const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('/root/home')) return 'home'
  if (path.includes('/root/pricate')) return 'pricate'
  if (path.includes('/root/atack')) return 'atack'
  if (path.includes('/root/bughole')) return 'bughole'
  if (path.includes('/root/ConunityTalk')) return 'ConunityTalk'
  if (path.includes('/root/Question')) return 'Question'
  if (path.includes('/root/sortMine')) return 'sortMine'
  if (path.includes('/root/reward')) return 'reward'
  if (path.includes('/root/game')) return 'game'
  if (path.includes('/root/aboutUs')) return 'aboutUs'
  if (path.includes('/root/cms')) return 'pricate'
  if (path.includes('/root/base64')) return 'pricate'
  if (path.includes('/root/findIp')) return 'pricate'
  if (path.includes('/root/Crehash')) return 'pricate'
  if (path.includes('/root/CheckPwd')) return 'pricate'
  return 'home' //不存在返回home
})

// 添加监听activeMenu的变化
watch(activeMenu, (newValue, oldValue) => {
  if (oldValue) {
    // 移除旧的激活项的动画效果
    const oldMenuItem = document.querySelector(`.menu .el-menu-item[index="${oldValue}"]`)
    if (oldMenuItem) {
      gsap.to(oldMenuItem, {
        borderBottom: 'none',
        fontWeight: 'normal',
        color: '#ffffff',
        textShadow: '2px 2px 4px rgba(0, 0, 0, 0.2)',
        duration: 0.3
      })
    }
  }
  
  if (newValue) {
    // 获取当前激活的菜单项
    const activeMenuItem = document.querySelector(`.menu .el-menu-item[index="${newValue}"]`)
    if (activeMenuItem) {
      // 应用GSAP动画
      gsap.timeline()
        .to(activeMenuItem, {
          color: '#ffffff',
          fontWeight: 'bold',
          textShadow: '0 0 10px rgba(255, 255, 255, 0.7), 0 0 20px rgba(255, 255, 255, 0.4)', // 白色发光效果
          duration: 0.3,
          ease: 'power2.out'
        })
        .to(activeMenuItem, {
          borderBottom: '6px solid #ffffff',
          duration: 0.3,
          ease: 'elastic.out(1, 0.5)'
        }, '-=0.2')
    }
  }
})

// 退出登录
const laout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    store.dispatch('logout')
    router.push('/')
    ElMessage.success('退出成功')
  }).catch(() => { })
}

// 前往个人信息页
const Mine = () => {
  router.push('/root/mine')
}

// App端下载
const downloadApp = () => {
  // ElMessage.success('跳转到App端下载页面')
  window.open('/root/app', 'App_down')
}

// 前往登录页
const goToLogin = () => {
  router.push('/login');
};

// 修改 AI 智能解答方法
const aiAnswer = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后使用此功能');
    return;
  }
  router.push('/root/chat-wacyg');
};

// 在组件挂载后添加菜单项动画
onMounted(() => {
  // 为每个菜单项添加悬停动画
  const menuItems = document.querySelectorAll('.menu .el-menu-item')
  menuItems.forEach(item => {
    item.addEventListener('mouseenter', () => {
      gsap.to(item, {
        scale: 1.05,
        duration: 0.3,
        ease: 'power2.out'
      })
    })
    
    item.addEventListener('mouseleave', () => {
      gsap.to(item, {
        scale: 1,
        duration: 0.3,
        ease: 'power2.out'
      })
    })
  })

  // 为logo添加初始动画
  gsap.from('.logo', {
    opacity: 0,
    x: -50,
    duration: 1,
    ease: 'power2.out'
  })

  // 为用户信息添加初始动画
  gsap.from('.user-dropdown, .login-buttons', {
    opacity: 0,
    x: 50,
    duration: 1,
    ease: 'power2.out'
  })
})
</script>

<style scoped>
.container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
  overflow: hidden;
}

.container::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 60%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.header {
  background: rgba(102, 126, 234, 0.7);
  backdrop-filter: blur(10px);
  position: fixed;
  width: 100%;
  z-index: 1000;
  top: 0;
  left: 0;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.main-content {
  padding-top: 60px;
  min-height: calc(100vh - 60px);
  position: relative;
  z-index: 1;
}

.logo {
  color: #ffffff;
  font-size: 24px;
  font-weight: bold;
  margin-right: 40px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-img {
  height: 40px;
  width: auto;
}

.menu {
  background: transparent;
  border: none;
  flex-grow: 1;
  flex-shrink: 1;
  overflow: hidden;
}

.menu .el-menu-item {
  color: #ffffff;
  font-size: clamp(12px, 1.2vw, 16px);
  transition: color 0.3s ease;
  flex-shrink: 0;
  padding: 0 15px;
  position: relative;
}

.menu .el-menu-item.is-active {
  position: relative;
}

.menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.router-link {
  color: inherit;
  text-decoration: none;
}

.user-dropdown {
  margin-left: auto;
  padding-right: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #fff;
  padding: 5px 10px;
  border-radius: 5px;
  background-color: rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.user-avatar {
  margin-right: 8px;
  border: 2px solid rgba(255, 255, 255, 0.8);
}

/* 修改下拉菜单样式 */
:deep(.el-dropdown-menu) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(102, 126, 234, 0.2);
  min-width: 120px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

:deep(.el-dropdown-menu__item) {
  color: #2d3748;
  font-size: 14px;
  padding: 8px 16px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.more {
  position: relative;
  margin-left: 0px;
}

.more .el-dropdown {
  color: #fff;
  font-size: clamp(12px, 1.2vw, 16px);
  cursor: pointer;
  padding: 0 15px;
  height: 60px;
  line-height: 60px;
  display: flex;
  align-items: center;
}

.more .el-dropdown:hover {
  background: rgba(102, 126, 234, 0.2);
  color: #ffffff;
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  font-size: 24px;
  cursor: pointer;
  margin-left: auto;
  margin-right: 20px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header {
    padding: 0 10px;
  }

  .logo {
    font-size: 18px;
    margin-right: 10px;
  }

  .logo-img {
    height: 30px;
  }

  .mobile-menu-btn {
    display: block;
  }

  .menu {
    position: absolute;
    top: 60px;
    left: 0;
    width: 100%;
    background: rgba(102, 126, 234, 0.9);
    backdrop-filter: blur(10px);
    display: none;
    flex-direction: column;
  }

  .menu.mobile-menu {
    display: flex;
  }

  .menu .el-menu-item {
    width: 100%;
    text-align: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .user-dropdown {
    margin-left: 0;
    padding-right: 10px;
  }

  .user-info {
    padding: 3px 8px;
    font-size: 14px;
  }

  .user-avatar {
    margin-right: 5px;
  }
}
@media (min-width: 769px) and (max-width: 1024px) {
  .menu .el-menu-item {
    padding: 0 10px;
  }

  .more .el-dropdown {
    padding: 0 10px;
  }

  .logo {
    font-size: 20px;
    margin-right: 20px;
  }

  .user-info {
    font-size: clamp(12px, 1.1vw, 14px);
  }
}
/* 页脚样式 */
.app-footer {
  text-align: center;
  padding: 25px 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  line-height: 1.8;
  background-color: transparent;
  border-top: none;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.app-footer p {
  margin: 4px 0;
}

.app-footer a {
  color: inherit;
  text-decoration: none;
  transition: color 0.3s ease;
}

.app-footer a:hover {
  color: #ffffff;
  text-decoration: underline;
}

.disclaimer {
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
}

.login-buttons {
  display: flex;
  gap: 10px;
  margin-left: auto;
  padding-right: 20px;
}

.login-buttons .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s ease;
}

.login-buttons .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}
</style>