<template>
    <div class="reward-container">
      <!-- 卡片容器 -->
      <el-card 
        class="reward-card"
        :class="{ 'animated-card': showAnimation }"
        shadow="hover"
        @mouseenter="showAnimation = true"
        @mouseleave="showAnimation = false"
      >
        <!-- 加载状态 -->
        <el-skeleton :rows="2" animated v-if="loading" />
  
        <!-- 正常状态 -->
        <div v-else class="reward-content">
          <!-- 标题区 -->
          <div class="header">
            <div class="header-left">
              <el-icon class="trophy-icon"><Trophy /></el-icon>
              <h3>我的奖励</h3>
            </div>
            <el-button 
              type="primary" 
              :icon="Refresh" 
              circle 
              @click="fetchReward"
              class="refresh-btn"
            />
          </div>
  
          <!-- 奖励语句 -->
          <div class="rewards-grid">
            <div class="reward-item">
              <div class="reward-icon">
                <el-icon><Lock /></el-icon>
              </div>
              <div class="reward-info">
                <span class="reward-label">待解锁语句</span>
                <span class="reward-value">{{ rewardStatement.lockedRewardsCount }}</span>
              </div>
            </div>
  
            <div class="reward-item">
              <div class="reward-icon">
                <el-icon><Unlock /></el-icon>
              </div>
              <div class="reward-info">
                <span class="reward-label">已解锁语句</span>
                <div class="statement-content">{{ statement }}</div>
              </div>
            </div>
  
            <div class="reward-item">
              <div class="reward-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="reward-info">
                <span class="reward-label">当前总分</span>
                <span class="reward-value">{{ rewardStatement.totalScore }}</span>
              </div>
            </div>
          </div>
  
        </div>
  
        <!-- 错误状态 -->
        <el-alert 
          v-if="error"
          type="error"
          :title="error"
          show-icon
          :closable="false"
          class="error-alert"
        />
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { 
    Trophy,
    Refresh,
    Star,
    Lock,
    Unlock
  } from '@element-plus/icons-vue'
  import axios from 'axios'
  import { ElMessage } from 'element-plus'
  import store from '@/store'
import ToUrl from '@/api/api'
  
  // 响应式数据
  const rewardStatement = ref('')
  const loading = ref(false)
  const error = ref('')
  const showAnimation = ref(false)
  const statement=ref('')
  // 获取奖励语句
  const fetchReward = async () => {
    try {
      loading.value = true
      error.value = ''
      
      const { data } = await axios.get(ToUrl.url+'/user/reward', {
        headers: {
          Authorization: `Bearer ${store.state.token}`
        }
      })
      statement.value='';
      rewardStatement.value = data.data
      // console.log(data.data.lockedRewardsCount);
      if (Array.isArray(data.data.unlockedRewards)) {
        for (const reward of data.data.unlockedRewards) {
          statement.value += reward;
        }
      }
      // console.log(rewardStatement.value)
      // console.log(statement.value);
      // console.log(data.data.unlockedRewards[1]);
      // console.log(data.data.totalScore);
      ElMessage.success('奖励已刷新!')
    } catch (err) {
      error.value = err.response?.data?.message || '获取奖励失败'
      ElMessage.error(error.value)
    } finally {
      loading.value = false
    }
  }
  

  
  // 初始加载
  onMounted(fetchReward)
  </script>
  
  <style scoped>
  .unlocked{
    color: red;
    text-align: left;
  }
  .locked{
    color: rgb(195, 227, 148);
    text-align: left;
  }
  .reward-container {
    max-width: 600px;
    margin: 20px auto;
    height: 740px;
  }
  
  .reward-card {
    background: linear-gradient(135deg, #fff9e6 0%, #ffe599 100%);
    border-radius: 12px;
    transition: all 0.3s ease;
  }
  
  .animated-card {
    transform: translateY(-5px);
    box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  }
  
  .header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    gap: 10px;
    
    h3 {
      color: #d48806;
      margin: 0;
      font-size: 1.5em;
    }
  }
  
  .trophy-icon {
    font-size: 28px;
    color: #faad14;
  }
  
  .refresh-btn {
    margin-left: auto;
    color: #d48806;
  }
  
  .statement {
    position: relative;
    padding: 20px;
    background: white;
    border-radius: 8px;
    margin: 15px 0;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    
    p {
      font-size: 1.2em;
      color: #333;
      line-height: 1.6;
      margin: 0;
      text-align: center;
      font-style: italic;
    }
  }
  
  .quote-left {
    position: absolute;
    left: 10px;
    top: 10px;
    color: #ffd666;
  }
  
  .quote-right {
    position: absolute;
    right: 10px;
    bottom: 10px;
    color: #ffd666;
  }
  
  .actions {
    text-align: center;
    margin-top: 20px;
  }
  
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.5s ease;
  }
  
  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }
  
  .error-alert {
    margin-top: 15px;
  }
  </style>