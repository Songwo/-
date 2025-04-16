<template>
  <el-container class="admin-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="admin-aside">
      <!-- 系统管理员展示 -->
      <div class="admin-header">
        <el-avatar :size="60" :src="computedAvatarUrl" />
        <span class="admin-name">系统管理员</span>
        <!-- 退出按钮 -->
        <el-button class="logout-btn" @click="logout">退出</el-button>
      </div>

      <el-menu 
        :default-active="activeMenu" 
        active-text-color="#409EFF" 
        background-color="#f0f8ff" 
        class="admin-menu"
      >
        <!-- 评论管理 -->
        <RouterLink :to="commentPath" class="router-link">
          <el-menu-item index="1">
            <el-icon>
              <ChatDotRound />
            </el-icon>
            <span>评论管理</span>
          </el-menu-item>
        </RouterLink>

        <!-- 用户管理 -->
        <RouterLink :to="userPath" class="router-link">
          <el-menu-item index="2">
            <el-icon>
              <User />
            </el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </RouterLink>

        <!-- 数据分析 -->
        <RouterLink :to="dataPath" class="router-link">
          <el-menu-item index="3">
            <el-icon>
              <DataAnalysis />
            </el-icon>
            <span>数据分析</span>
          </el-menu-item>
        </RouterLink>

        <!-- 视频管理 -->
        <RouterLink :to="videoPath" class="router-link">
          <el-menu-item index="4">
            <el-icon>
              <VideoCamera />
            </el-icon>
            <span>视频管理</span>
          </el-menu-item>
        </RouterLink>

        <!-- 漏洞管理 -->
        <RouterLink :to="bugPath" class="router-link">
          <el-menu-item index="5">
            <el-icon>
              <Warning />
            </el-icon>
            <span>漏洞管理</span>
          </el-menu-item>
        </RouterLink>

        <!-- 分类管理 -->
        <RouterLink :to="categoryPath" class="router-link">
          <el-menu-item index="6">
            <el-icon>
              <Folder />
            </el-icon>
            <span>分类管理</span>
          </el-menu-item>
        </RouterLink>

        <!-- 题目管理 -->
        <RouterLink :to="questionPath" class="router-link">
          <el-menu-item index="7">
            <el-icon>
              <EditPen />
            </el-icon>
            <span>题目管理</span>
          </el-menu-item>
        </RouterLink>

        <!-- 奖励管理 -->
        <RouterLink :to="rewardPath" class="router-link">
          <el-menu-item index="8">
            <el-icon>
              <Trophy />
            </el-icon>
            <span>奖励管理</span>
          </el-menu-item>
        </RouterLink>
      </el-menu>
    </el-aside>

    <el-main class="admin-main">
      <RouterView></RouterView>
    </el-main>
  </el-container>
</template>

<script setup>
import { useRoute,useRouter } from 'vue-router';
import ToUrl from '@/api/api';
import store from '@/store';
import {
  ChatDotRound,
  User,
  DataAnalysis,
  VideoCamera,
  Warning,
  Folder,
  EditPen,
  Trophy,
} from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import { ref, computed } from 'vue';


const router = useRouter()
// 获取当前路由
const route = useRoute();

// 动态路径
const commentPath = ref('/backMange/comment');
const userPath = ref('/backMange/user');
const dataPath = ref('/backMange/data');
const videoPath = ref('/backMange/video');
const bugPath = ref('/backMange/bug');
const categoryPath = ref('/backMange/category');
const questionPath = ref('/backMange/ques');
const rewardPath = ref('/backMange/reword');

// 头像动态绑定
const xava = ref("");  // 从 store 获取头像路径
xava.value=store.state.avatar;
/* console.log(xava.value) */
const computedAvatarUrl = computed(() => ToUrl.url + "/" + xava.value);
/* console.log(computedAvatarUrl.value); */

// 动态设置 default-active
const activeMenu = computed(() => {
  // 这里根据当前路由的路径来判断应该激活哪个菜单项
  switch (route.path) {
    case '/backMange/comment':
      return '1';
    case '/backMange/user':
      return '2';
    case '/backMange/data':
      return '3';
    case '/backMange/video':
      return '4';
    case '/backMange/bug':
      return '5';
    case '/backMange/category':
      return '6';
    case '/backMange/ques':
      return '7';
    case '/backMange/reword':
      return '8';
    default:
      return '1';  // 默认激活第一个菜单
  }
});
// 退出登录
const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除登录状态（根据实际项目调整）
    // 跳转到登录页
    // 退出登录
    store.dispatch('logout');  // 清除 token 和用户信息
    router.push('/')
    ElMessage.success('退出成功')
  }).catch(() => {})
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
  background: #f5f9ff;
}

.admin-aside {
  background-color: #f0f8ff;
  border-right: 1px solid #e6f0ff;
  display: flex;
  flex-direction: column;
}

.admin-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.admin-main {
  padding: 20px;
  background: #ffffff;
  border-radius: 8px;
  margin: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 系统管理员展示 */
.admin-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  background-color: #e6f7ff;
  border-bottom: 1px solid #d9ecff;
  position: relative;
}

.admin-header .el-avatar {
  margin-bottom: 10px;
}

.admin-name {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

/* 退出按钮样式 */
.logout-btn {
  position: absolute;
  top: 95px;
  right: -5px;
  background-color: #f56c6c;
  color: white;
  border: none;
  font-size: 14px;
}

.logout-btn:hover {
  background-color: #f44336;
}

/* 全局样式 */
:root {
  --el-color-primary: #6cb6ff;
  --el-color-primary-light-3: #8ec5ff;
}

.router-link {
  text-decoration: none;
}
</style>