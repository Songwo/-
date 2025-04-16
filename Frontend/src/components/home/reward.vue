<template>
  <div class="reward-page-bg">
    <!-- 顶部信息 -->
    <div class="top-bar">
      <el-avatar :src="user.avatar" size="large" />
      <div class="user-info">
        <div class="nickname">{{ user.nickname }}</div>
        <div class="level">Lv.{{ user.level }}</div>
      </div>
      <div class="score-info">
        <el-icon><Star /></el-icon>
        <span>{{ rewardStatement.totalScore }} 分</span>
        <el-button :icon="Refresh" circle @click="fetchReward" />
      </div>
    </div>

    <!-- 进度与成就 -->
    <div class="progress-achievement">
      <el-progress :percentage="progressPercent" status="success" style="width: 40vw;" />
      <div class="badges-scroll">
        <el-tooltip v-for="badge in badges" :key="badge.name" :content="badge.desc">
          <el-avatar :src="badge.icon" :size="36" />
        </el-tooltip>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左栏：奖励历史 -->
      <div class="left-panel">
        <h4>已解锁奖励</h4>
        <el-scrollbar height="260px">
          <ul>
            <li v-for="(reward, idx) in rewardStatement.unlockedRewards" :key="idx">
              <el-icon><Unlock /></el-icon>
              <span>{{ reward }}</span>
            </li>
          </ul>
        </el-scrollbar>
      </div>

      <!-- 中栏：今日推荐/最新奖励 -->
      <div class="center-panel">
        <el-card class="highlight-card">
          <div class="highlight-title">今日推荐</div>
          <div class="highlight-content">{{ todayReward }}</div>
        </el-card>
      </div>

      <!-- 右栏：待解锁奖励 -->
      <div class="right-panel">
        <h4>待解锁奖励</h4>
        <el-scrollbar height="260px">
          <ul>
            <li v-for="n in rewardStatement.lockedRewardsCount" :key="n">
              <el-icon><Lock /></el-icon>
              <span>神秘奖励 #{{ n }}</span>
            </li>
          </ul>
        </el-scrollbar>
      </div>
    </div>

    <!-- 底部功能区 -->
    <div class="bottom-bar">
      <el-button type="success" icon="el-icon-present">奖励兑换</el-button>
      <el-button type="warning" icon="el-icon-trophy">排行榜</el-button>
      <el-button type="info" icon="el-icon-date">每日签到</el-button>
      <el-button type="primary" icon="el-icon-share">分享奖励</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Star, Refresh, Lock, Unlock } from '@element-plus/icons-vue'
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

onMounted(fetchReward)
</script>

<style scoped>
.reward-page-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #769fcd 0%, #b9d7ea 100%); /* 更柔和的蓝调 */
  padding: 32px 0 0 0;
  box-sizing: border-box;
}
.top-bar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 24px;
  padding: 0 40px 16px 40px;
}
.user-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.nickname {
  font-size: 1.3em;
  font-weight: bold;
  color: #ffffff;
}
.level {
  font-size: 1em;
  color: #ffffff;
}
.score-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1em;
  color: #ff0000;
}
.progress-achievement {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 0 40px 24px 40px;
}
.badges-scroll {
  display: flex;
  align-items: center;
  gap: 8px;
}
.main-content {
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 32px;
  padding: 0 40px;
  margin-bottom: 32px;
}
.left-panel, .right-panel {
  background: #fffbe6;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  padding: 18px 16px;
  min-height: 320px;
}
.left-panel h4, .right-panel h4 {
  color: #d48806;
  margin-bottom: 12px;
}
.left-panel ul, .right-panel ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.left-panel li, .right-panel li {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 1em;
  color: #333;
}
.left-panel li:last-child, .right-panel li:last-child {
  margin-bottom: 0;
}
.center-panel {
  display: flex;
  align-items: center;
  justify-content: center;
}
.highlight-card {
  width: 100%;
  min-height: 220px;
  background: linear-gradient(135deg, #fff9e6 0%, #ffe599 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.highlight-title {
  font-size: 1.2em;
  color: #d48806;
  margin-bottom: 12px;
  font-weight: bold;
}
.highlight-content {
  font-size: 1.3em;
  color: #333;
  text-align: center;
  font-style: italic;
}
.bottom-bar {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 32px;
}
</style>