<template>
  <div class="main-container">
    <!-- 左侧排名 -->
    <div class="left-ranking">
      <!-- 原有排名内容保持不动 -->
      <div class="ranking-container">
      <!-- 标题区 -->
      <div class="header">
        <h2>实时排行榜</h2>
        <el-button 
          type="info" 
          :icon="Refresh" 
          circle 
          @click="loadRankings"
        />
      </div>
  
      <!-- 加载状态 -->
      <el-skeleton :rows="5" animated v-if="loading" />
  
      <!-- 排名列表 -->
      <el-scrollbar v-else height="600px">
        <transition-group name="list" tag="div">
          <div 
            v-for="(item, index) in rankings" 
            :key="item.id"
            class="rank-item"
            :class="{
              'top-1': index === 0,
              'top-2': index === 1,
              'top-3': index === 2,
              'hover-effect': index > 2
            }"
          >
            <!-- 奖牌图标 -->
            <div class="medal">
              <el-icon v-if="index === 0" color="#ffd700"><Medal /></el-icon>
              <el-icon v-if="index === 1" color="#c0c0c0"><Medal /></el-icon>
              <el-icon v-if="index === 2" color="#cd7f32"><Medal /></el-icon>
              <span v-if="index > 2">{{ index + 1 }}</span>
            </div>
  
            <!-- 用户信息 -->
            <div class="user-info">
              <el-avatar :src="item.avatar" />
              <div class="detail">
                <span class="name">{{ item.username }}</span>
                <span class="department">{{ item.department }}</span>
              </div>
            </div>
  
            <!-- 分数 -->
            <div class="score">
              <el-tag :type="getScoreType(item.score)">
                {{ item.score }} 分
              </el-tag>
            </div>
          </div>
        </transition-group>
      </el-scrollbar>
  
      <!-- 最后更新时间 -->
      <div class="update-time">
        最后更新：{{ lastUpdate }}
      </div>
    </div>
  </div>

    <!-- 右侧信息栏 -->
    <div class="right-info">
      <!-- 最新公告 -->
      <div class="info-section announcement">
        <div class="info-header">
          <h3>📢 最新公告</h3>
          <el-tag type="danger" effect="dark" size="small">Hot</el-tag>
        </div>
        <el-scrollbar height="240px">
          <div class="announcement-list">
            <div 
              v-for="item in announcements" 
              :key="item.id" 
              class="announcement-item"
              @click="viewAnnouncement(item.id)"
            >
              <div class="announcement-time">{{ item.time }}</div>
              <div class="announcement-title">{{ item.title }}</div>
              <el-tag :type="item.type" size="mini" effect="plain">{{ item.tag }}</el-tag>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 公告详情对话框 -->
      <el-dialog
        v-model="dialogVisible"
        title="公告详情"
        width="50%"
      >
        <div v-if="currentAnnouncement">
          <h3>{{ currentAnnouncement.title }}</h3>
          <div class="announcement-meta">
            <el-tag :type="currentAnnouncement.type" size="small">{{ currentAnnouncement.tag }}</el-tag>
            <span class="time">{{ new Date(currentAnnouncement.createTime).toLocaleString() }}</span>
          </div>
          <div class="announcement-content">
            {{ currentAnnouncement.content }}
          </div>
        </div>
      </el-dialog>

      <!-- 更新内容 -->
      <div class="info-section changelog">
        <div class="info-header">
          <h3>🛠 版本更新</h3>
          <el-tag type="info" effect="dark" size="small">点击查看</el-tag>
        </div>
        <el-scrollbar height="320px">
          <div class="changelog-list">
            <div 
              v-for="(log, index) in changelogs" 
              :key="index" 
              class="changelog-item"
              @click="viewChangelog(log)"
            >
              <el-tag :type="getChangelogType(log.type)" size="small">
                {{ log.type === 'feature' ? '新功能' : log.type === 'fix' ? '修复' : '改进' }}
              </el-tag>
              <span class="changelog-time">{{ new Date(log.createTime).toLocaleDateString() }}</span>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 更新日志详情对话框 -->
      <el-dialog
        v-model="changelogDialogVisible"
        title="版本更新详情"
        width="50%"
      >
        <div v-if="currentChangelog">
          <div class="changelog-meta">
            <el-tag :type="getChangelogType(currentChangelog.type)" size="small">
              {{ currentChangelog.type === 'feature' ? '新功能' : currentChangelog.type === 'fix' ? '修复' : '改进' }}
            </el-tag>
            <span class="time">{{ new Date(currentChangelog.createTime).toLocaleString() }}</span>
          </div>
          <div class="changelog-content">
            {{ currentChangelog.content }}
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>
  
<script setup>
import { ref, onMounted } from 'vue'
import { Medal, Refresh, CirclePlus, Tools } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'
import { getAnnouncements, getAnnouncementDetail } from '@/api/announcement'
import { getChangelogs } from '@/api/changelog'

// 响应式数据
const rankings = ref([])
const loading = ref(true)
const lastUpdate = ref('')
const announcements = ref([])
const changelogs = ref([])
const dialogVisible = ref(false)
const changelogDialogVisible = ref(false)
const currentAnnouncement = ref(null)
const currentChangelog = ref(null)

// 初始化加载
onMounted(() => {
  loadRankings()
  loadAnnouncements()
  loadChangelogs()
  // 每5分钟刷新一次数据
  setTimeout(() => {
    setInterval(loadRankings, 300000) //5*60*1000
    setInterval(loadAnnouncements, 300000)
    setInterval(loadChangelogs, 300000)
  }, 5000)
})

// 加载排名数据
const loadRankings = async () => {
  try {
    loading.value = true;
    const res = await axios.get(ToUrl.url+'/user/rank', {
      headers: {
        'Authorization': `Bearer ${store.state.token}`,
      }
    });

    // 数据转换处理
    rankings.value = res.data.data
      .map(item => ({
        id: item.id,        
        username: item.username, 
        score: item.totalScore,  
        department: item.id, 
        avatar: ToUrl.url+"/"+item.avatar || ToUrl.url+'/avatar/0736dfa5-a96f-45a7-8208-2e8e3b72161e_ab.jpg' 
      }))
      .sort((a, b) => b.score - a.score); // 按分数降序排列

    lastUpdate.value = new Date().toLocaleTimeString();
    // console.log(rankings.value);
  } catch (error) {
    ElMessage.error(`排名加载失败: ${error.message}`);
  } finally {
    loading.value = false;
  }
}

// 加载公告
const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncements()
    announcements.value = res.data.map(item => ({
      id: item.id,
      time: new Date(item.createTime).toLocaleDateString(),
      title: item.title,
      type: item.type,
      tag: item.tag
    }))
  } catch (error) {
    ElMessage.error('公告加载失败')
  }
}

// 加载更新日志
const loadChangelogs = async () => {
  try {
    const res = await getChangelogs()
    changelogs.value = res.data
  } catch (error) {
    ElMessage.error('更新日志加载失败')
  }
}

// 查看公告详情
const viewAnnouncement = async (id) => {
  try {
    const res = await getAnnouncementDetail(id)
    currentAnnouncement.value = res.data
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取公告详情失败')
  }
}

// 查看更新日志详情
const viewChangelog = (log) => {
  currentChangelog.value = log
  changelogDialogVisible.value = true
}

// 获取更新日志类型对应的标签类型
const getChangelogType = (type) => {
  switch (type) {
    case 'feature':
      return 'success'
    case 'fix':
      return 'warning'
    case 'improvement':
      return 'info'
    default:
      return 'info'
  }
}

// 分数标签类型
const getScoreType = (score) => {
  if (score >= 95) return 'success'
  if (score >= 90) return 'warning'
  return 'danger'
}
</script>
  
<style scoped>
.ranking-container {
  padding: 20px;
  color: black;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 10px;
  color: #f9f3f3;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin: 8px 0;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.medal {
  width: 40px;
  text-align: center;
  font-weight: bold;
  font-size: 18px;
}

.user-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
}

.detail {
  display: flex;
  flex-direction: column;
}

.name {
  font-weight: 600;
  margin-bottom: 4px;
}

.department {
  font-size: 0.8em;
  color: #666;
}

.score {
  margin-left: auto;
}

/* 前三名特殊样式 */
.top-1 { background: linear-gradient(90deg, #fff3e0 0%, #ffe0b2 100%); }
.top-2 { background: linear-gradient(90deg, #f5f5f5 0%, #eeeeee 100%); }
.top-3 { background: linear-gradient(90deg, #fff8e1 0%, #ffecb3 100%); }

/* 悬停效果 */
.hover-effect:hover {
  transform: translateX(10px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 过渡动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.update-time {
  margin-top: 15px;
  text-align: right;
  font-size: 1.2em;
  color: #ffffff;
}
.main-container {
  display: flex;
  gap: 20px;
  padding: 20px;
  height: calc(100vh - 140px); /* 根据实际高度调整 */
  color: black;
}

.left-ranking {
  flex: 1;
  min-width: 600px;
}

.right-info {
  width: 480px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 321px;
}

.info-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  padding: 15px;
  height: 320px;
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s;
  cursor: pointer;
}

.announcement-item:hover {
  transform: translateX(5px);
  background: #f1f3f5;
}

.announcement-time {
  font-size: 0.8em;
  color: #666;
  margin-bottom: 4px;
}

.announcement-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.changelog-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 10px 0;
}

.changelog-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.changelog-item:hover {
  transform: translateX(5px);
  background: #f1f3f5;
}

.changelog-time {
  font-size: 0.8em;
  color: #666;
}

/* 原有排名样式调整 */
.ranking-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.el-scrollbar {
  flex: 1;
}

.update-time {
  margin-top: auto;
}

.announcement-meta {
  margin: 10px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.announcement-content {
  margin-top: 20px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.announcement-item {
  cursor: pointer;
  transition: all 0.3s;
}

.announcement-item:hover {
  background: #f0f0f0;
}

.changelog-meta {
  margin: 10px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.changelog-content {
  margin-top: 20px;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>