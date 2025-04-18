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
        <el-menu-item v-for="(label, index) in menuItems" :key="index" :index="index">
          <RouterLink :to="`/root/${index}`" class="router-link">{{ label }}</RouterLink>
        </el-menu-item>
        <!-- 更多菜单项下的下拉菜单 -->
        <el-menu-item class="more">
          <el-dropdown trigger="click">
            <span>更多</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="downloadApp">App端下载</el-dropdown-item>
                <el-dropdown-item @click="aiAnswer">AI智能解答</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-menu-item>
      </el-menu>

      <!-- User Info -->
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
import { computed, ref } from 'vue'
import ToUrl from '@/api/api'
import { Menu } from '@element-plus/icons-vue'

// 移动端菜单状态
const isMobileMenuOpen = ref(false)

// 切换移动端菜单
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

// 使用 route 路径匹配标签
const route = useRoute()
const router = useRouter()
const store = useStore()
const app_url = ToUrl.url

// 动态加载用户名
const at = computed(() => ({
  token: store.state.token,
  user: store.state.user,
  avatar: `${ToUrl.url}/${store.state.avatar}`
}))

// 菜单项
const menuItems = {
  home: '首页',
  pricate: '实战练习',
  atack: '攻防课程',
  bughole: '漏洞库',
  ConunityTalk: '社区论坛',
  Question: '答题测试',
  sortMine: '个人排名',
  reward: '奖励页面',
  game: '游戏学习',
  aboutUs: '关于我们'
}

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

// AI智能解答
const aiAnswer = () => {
  // ElMessage.success('进入AI智能解答页面')
  router.push('/root/chat-wacyg')
}
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
  font-weight: 500;
  transition: padding 0.3s ease, background-color 0.3s ease, font-size 0.3s ease;
  flex-shrink: 0;
  padding: 0 15px;
}

.menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.menu .el-menu-item.is-active {
  color: #eaeaea !important;
  font-weight: bold;
  border-bottom: 6px solid #9ca4d8 !important;
  background-color: #5b21b6 !important;
  position: relative;
  overflow: hidden;
}

.menu .el-menu-item.is-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #ffe066, transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }

  100% {
    transform: translateX(100%);
  }
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
</style>