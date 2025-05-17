<template>
  <div class="reward-container">
    <!-- 用户信息卡片 -->
    <el-card class="user-card">
      <div class="user-profile">
        <el-avatar :src="user.avatar" :size="64" />
        <div class="user-details">
          <div class="user-name">{{ user.nickname }}</div>
          <div class="user-level">Lv.{{ user.level }}</div>
        </div>
        <div class="score-display">
          <el-icon><Star /></el-icon>
          <span class="score">{{ rewardStatement.totalScore }} 分</span>
          <el-button :icon="Refresh" circle @click="fetchReward" />
        </div>
      </div>
    </el-card>

    <!-- 进度卡片 -->
    <el-card class="progress-card">
      <div class="progress-header">
        <span class="title">成就进度</span>
        <div class="badges">
          <el-tooltip v-for="badge in badges" :key="badge.name" :content="badge.desc">
            <el-avatar :src="badge.icon" :size="32" />
          </el-tooltip>
        </div>
      </div>
      <el-progress :percentage="progressPercent" status="success" />
    </el-card>

    <!-- 奖励展示区 -->
    <div class="rewards-grid">
      <!-- 已解锁奖励卡片 -->
      <el-card class="reward-card">
        <template #header>
          <div class="card-header">
            <el-icon><Unlock /></el-icon>
            <span>已解锁奖励</span>
          </div>
        </template>
        <el-scrollbar height="200px">
          <ul class="reward-list">
            <li v-for="(reward, idx) in rewardStatement.unlockedRewards" :key="idx">
              <el-icon><Check /></el-icon>
              <span>{{ reward }}</span>
            </li>
          </ul>
        </el-scrollbar>
      </el-card>

      <!-- 今日推荐卡片 -->
      <el-card class="reward-card highlight">
        <template #header>
          <div class="card-header">
            <el-icon><Star /></el-icon>
            <span>今日推荐</span>
          </div>
        </template>
        <div class="highlight-content">
          {{ todayReward }}
        </div>
      </el-card>

      <!-- 待解锁奖励卡片 -->
      <el-card class="reward-card">
        <template #header>
          <div class="card-header">
            <el-icon><Lock /></el-icon>
            <span>待解锁奖励</span>
          </div>
        </template>
        <el-scrollbar height="200px">
          <ul class="reward-list">
            <li v-for="n in rewardStatement.lockedRewardsCount" :key="n">
              <el-icon><Lock /></el-icon>
              <span>神秘奖励 #{{ n }}</span>
            </li>
          </ul>
        </el-scrollbar>
      </el-card>
    </div>

    <!-- 奖励兑换区 -->
    <el-card class="exchange-card">
      <template #header>
        <div class="card-header">
          <el-icon><Present /></el-icon>
          <span>奖励兑换</span>
        </div>
      </template>
      <div class="exchange-content">
        <el-tabs v-model="activeTab" class="exchange-tabs">
          <!-- 积分商城 -->
          <el-tab-pane label="积分商城" name="shop">
            <div class="shop-grid">
              <el-card v-for="(item, index) in shopItems" :key="index" class="shop-item" :class="{ 'disabled': item.points > rewardStatement.totalScore }">
                <div class="item-image">
                  <el-avatar :src="item.image" :size="64" />
                </div>
                <div class="item-info">
                  <h3>{{ item.name }}</h3>
                  <p class="item-desc">{{ item.description }}</p>
                  <div class="item-footer">
                    <span class="points">{{ item.points }} 积分</span>
                    <el-button 
                      type="primary" 
                      size="small" 
                      :disabled="item.points > rewardStatement.totalScore"
                      @click="exchangeItem(item)"
                    >
                      兑换
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </el-tab-pane>

          <!-- 称号兑换 -->
          <el-tab-pane label="称号兑换" name="titles">
            <div class="shop-grid">
              <el-card v-for="(title, index) in titleItems" :key="index" class="shop-item" :class="{ 'disabled': title.points > rewardStatement.totalScore }">
                <div class="item-image">
                  <el-avatar :src="title.image" :size="64" />
                </div>
                <div class="item-info">
                  <h3>{{ title.name }}</h3>
                  <p class="item-desc">{{ title.description }}</p>
                  <div class="item-footer">
                    <span class="points">{{ title.points }} 积分</span>
                    <el-button 
                      type="primary" 
                      size="small" 
                      :disabled="title.points > rewardStatement.totalScore"
                      @click="exchangeTitle(title)"
                    >
                      兑换
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </el-tab-pane>

          <!-- 每日任务 -->
          <el-tab-pane label="每日任务" name="tasks">
            <div class="tasks-list">
              <el-card v-for="(task, index) in dailyTasks" :key="index" class="task-item">
                <div class="task-content">
                  <div class="task-info">
                    <h3>{{ task.name }}</h3>
                    <p>{{ task.description }}</p>
                  </div>
                  <div class="task-reward">
                    <span class="points">+{{ task.points }} 积分</span>
                    <el-button 
                      type="success" 
                      size="small" 
                      :disabled="task.completed"
                      @click="completeTask(task)"
                    >
                      {{ task.completed ? '已完成' : '领取' }}
                    </el-button>
                  </div>
                </div>
                <el-progress 
                  v-if="task.progress !== undefined" 
                  :percentage="task.progress" 
                  :status="task.completed ? 'success' : ''"
                />
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Star, Refresh, Lock, Unlock, Check, Present } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import store from '@/store'
import ToUrl from '@/api/api'

// 用户信息（可根据实际项目替换）
const user = ref({
  avatar: ToUrl.url+"/"+store.state.avatar,
  nickname: store.state.user,
  level: 0
})

const rewardStatement = ref({ unlockedRewards: [], lockedRewardsCount: 0, totalScore: 0 })
const loading = ref(false)
const error = ref('')
const todayReward = ref('')

const fetchReward = async () => {
  try {
    loading.value = true
    error.value = ''
    const { data } = await axios.get(ToUrl.url + '/user/reward', {
      headers: { Authorization: `Bearer ${store.state.token}` }
    })
    console.log(user.value);
    rewardStatement.value = data.data
    todayReward.value = data.data.unlockedRewards?.slice(-1)[0] || '暂无推荐奖励'
    console.log(data.data.totalScore);
    // 动态设置等级（10级，1200分封顶，每120分升一级）
    const score = data.data.totalScore || 0
    let level = Math.floor(score / 120) + 1
    if (level > 10) level = 10
    if (level < 1) level = 1
    user.value.level = level
    ElMessage.success('奖励已刷新!')
  } catch (err) {
    error.value = err.response?.data?.message || '获取奖励失败'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

const progressPercent = computed(() => {
  const unlocked = rewardStatement.value?.unlockedRewards?.length || 0
  const total = unlocked + (rewardStatement.value?.lockedRewardsCount || 0)
  return total > 0 ? Math.round((unlocked / total) * 100) : 0
})

const badges = computed(() => {
  const score = rewardStatement.value?.totalScore || 0
  const unlocked = rewardStatement.value?.unlockedRewards?.length || 0
  const badgeList = []
  if (score >= 100) badgeList.push({ name: 'gold', icon: 'https://img.icons8.com/color/48/000000/prize.png', desc: '黄金成就：总分100+' })
  if (unlocked >= 5) badgeList.push({ name: 'unlock5', icon: 'https://img.icons8.com/color/48/000000/medal2.png', desc: '解锁5条奖励' })
  if (unlocked >= 10) badgeList.push({ name: 'unlock10', icon: 'https://img.icons8.com/color/48/000000/trophy.png', desc: '解锁10条奖励' })
  if (score >= 200) badgeList.push({ name: 'diamond', icon: 'https://img.icons8.com/color/48/000000/diamond.png', desc: '钻石成就：总分200+' })
  return badgeList
})

const activeTab = ref('shop')

// 商城商品列表
const shopItems = ref([
  {
    name: '高级会员体验卡',
    description: '7天高级会员体验资格',
    points: 100,
    image: 'https://img.icons8.com/color/48/000000/vip.png'
  },
  {
    name: '专属头像框',
    description: '限时专属头像框',
    points: 50,
    image: 'https://img.icons8.com/color/48/000000/frame.png'
  },
  {
    name: '游戏道具礼包',
    description: '包含多种游戏道具',
    points: 80,
    image: 'https://img.icons8.com/color/48/000000/gift.png'
  },
  {
    name: '自定义称号',
    description: '自定义个性称号',
    points: 30,
    image: 'https://img.icons8.com/color/48/000000/medal2.png'
  }
])

// 称号列表
const titleItems = ref([
  {
    name: '🥉【白帽学徒】',
    description: '展示你的游戏实力',
    points: 150,
    image: 'https://img.icons8.com/color/48/000000/crown.png'
  },
  {
    name: '社交之星',
    description: '活跃的社区成员',
    points: 100,
    image: 'https://img.icons8.com/color/48/000000/star.png'
  },
  {
    name: '新手导师',
    description: '帮助新玩家的热心玩家',
    points: 200,
    image: 'https://img.icons8.com/color/48/000000/teacher.png'
  },
  {
    name: '幸运玩家',
    description: '运气爆棚的玩家',
    points: 80,
    image: 'https://img.icons8.com/color/48/000000/lucky.png'
  }
])

// 每日任务列表
const dailyTasks = ref([
  {
    name: '每日签到',
    description: '完成每日签到',
    points: 10,
    completed: false
  },
  {
    name: '分享游戏',
    description: '分享游戏到社交媒体',
    points: 20,
    completed: false
  },
  {
    name: '完成3场游戏',
    description: '完成3场游戏对局',
    points: 30,
    progress: 0,
    completed: false
  },
  {
    name: '邀请好友',
    description: '成功邀请1位好友',
    points: 50,
    completed: false
  }
])

// 兑换商品
const exchangeItem = (item) => {
  ElMessage.success(`成功兑换 ${item.name}！`)
  // TODO: 实现实际的兑换逻辑
}

// 兑换称号
const exchangeTitle = (title) => {
  ElMessage.success(`成功兑换称号：${title.name}！`)
  // TODO: 实现实际的称号兑换逻辑
}

// 完成任务
const completeTask = (task) => {
  task.completed = true
  ElMessage.success(`完成任务：${task.name}，获得 ${task.points} 积分！`)
  // TODO: 实现实际的任务完成逻辑
}

onMounted(fetchReward)
</script>

<style scoped>
.reward-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: transparent;
}

.user-card {
  margin-bottom: 24px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 1.4em;
  font-weight: bold;
  color: #ffffff;
}

.user-level {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9em;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.2em;
  color: #E6A23C;
}

.score {
  font-weight: bold;
  color: #ffffff;
}

.progress-card {
  margin-bottom: 24px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 1.2em;
  font-weight: bold;
  color: #ffffff;
}

.badges {
  display: flex;
  gap: 8px;
}

.rewards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.reward-card {
  height: 100%;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.reward-card.highlight {
  background: rgba(255, 249, 230, 0.1);
  border: 1px solid rgba(230, 162, 60, 0.2);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #ffffff;
}

.reward-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.reward-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: rgba(255, 255, 255, 0.8);
}

.highlight-content {
  text-align: center;
  font-size: 1.2em;
  color: #E6A23C;
  padding: 24px;
  font-style: italic;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 24px;
}

.exchange-card {
  margin-top: 24px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.exchange-content {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  color: rgba(255, 255, 255, 0.6);
  font-size: 1.1em;
  font-style: italic;
}

.exchange-tabs {
  width: 100%;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
  padding: 16px;
}

.shop-item {
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.05);
}

.shop-item.disabled {
  opacity: 0.6;
}

.item-image {
  text-align: center;
  margin-bottom: 12px;
}

.item-info {
  text-align: center;
}

.item-info h3 {
  color: #ffffff;
  margin: 8px 0;
}

.item-desc {
  color: #ffffff;
  font-size: 0.9em;
  margin-bottom: 12px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points {
  color: #E6A23C;
  font-weight: bold;
}

.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
}

.task-item {
  background: rgba(255, 255, 255, 0.05);
}

.task-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-info h3 {
  color: #ffffff;
  margin: 0 0 8px 0;
}

.task-info p {
  color: #ffffff;
  margin: 0;
}

.task-reward {
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 768px) {
  .rewards-grid {
    grid-template-columns: 1fr;
  }
  
  .user-profile {
    flex-direction: column;
    text-align: center;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }

  .shop-grid {
    grid-template-columns: 1fr;
  }
  
  .task-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

/* 添加按钮和标签页样式 */
:deep(.el-button) {
  color: #ffffff !important;
}

:deep(.el-button--primary) {
  color: #ffffff !important;
}

:deep(.el-button--success) {
  color: #ffffff !important;
}

:deep(.el-button.is-disabled) {
  color: rgb(81, 222, 109) !important;
}

:deep(.el-tabs__item) {
  color: #ffffff !important;
}

:deep(.el-tabs__item.is-active) {
  color: #49df4e !important;
}

:deep(.el-tabs__active-bar) {
  background-color: #4caf50 !important;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: rgb(111, 225, 86);
}
</style>