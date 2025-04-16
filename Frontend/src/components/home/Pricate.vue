<template>
  <div class="container">
    <h1 class="title">Network Security Tools</h1>
    
    <!-- 介绍部分 -->
    <div class="intro-section">
      <p class="intro-text">
        专业的网络安全工具集合，为您提供全方位的网络安全解决方案。
        从CMS识别到密码强度检测，从IP查询到哈希值生成，一站式满足您的安全需求。
      </p>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :span="6" v-for="stat in statistics" :key="stat.label">
        <div class="stat-card">
          <Icon :icon="stat.icon" class="stat-icon" />
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 搜索和分类 -->
    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索安全工具..."
        class="search-input"
        :prefix-icon="Search"
      />
      <el-radio-group v-model="selectedCategory" class="category-group">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="scan">扫描工具</el-radio-button>
        <el-radio-button label="encode">编码工具</el-radio-button>
        <el-radio-button label="analysis">分析工具</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 热门工具 -->
    <div class="hot-tools">
      <h2>热门工具</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="tool in hotTools" :key="tool.name">
          <el-card class="tool-card hot">
            <div class="hot-badge">热门</div>
            <Icon :icon="tool.icon" class="tool-icon" />
            <h3>{{ tool.name }}</h3>
            <p>{{ tool.description }}</p>
            <el-button type="primary" @click="goToTool(tool.link)">查看</el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 工具列表 -->
    <div class="tools-section">
      <h2>全部工具</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="tool in filteredTools" :key="tool.name">
          <el-card class="tool-card">
            <Icon :icon="tool.icon" class="tool-icon" />
            <h3>{{ tool.name }}</h3>
            <p>{{ tool.description }}</p>
            <el-button type="primary" @click="goToTool(tool.link)">查看</el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script lang="ts" setup name="Private">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import ToUrl from '@/api/api'
import store from '@/store'

const searchQuery = ref('')
const router = useRouter()
const selectedCategory = ref('all')
const stats = ref({
  dailyVisits: 0,
  userCount: 0
})

// 格式化数字显示
const formatNumber = (num: number) => {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k+'
  }
  return num.toString()
}

// 更新统计数据
const updateStats = async () => {
  try {
    const response = await axios.get(ToUrl.url+'/api/stats', {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    stats.value = response.data
  } catch (error) {
    console.error('Failed to fetch stats:', error)
    // 设置默认值，确保界面不会崩溃
    if (!stats.value.dailyVisits && !stats.value.userCount) {
      stats.value = { dailyVisits: 0, userCount: 0 }
    }
  }
}

// 记录访问
const recordVisit = async () => {
  try {
    await axios.post(ToUrl.url+'/api/stats/visits', {}, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    await updateStats() // 更新统计数据
  } catch (error) {
    console.error('Failed to record visit:', error)
    // 错误时不重试，继续执行
  }
}

// 定时更新统计数据
let statsInterval: number | null = null

onMounted(() => {
  // 加载数据
  updateStats() //更新用户和访问数量
  recordVisit() //增加访问量
  increaseUserCount() //增加用户
  
  // 每30秒更新一次数据
  statsInterval = window.setInterval(updateStats, 30000)
})

onUnmounted(() => {
  if (statsInterval) {
    clearInterval(statsInterval)
  }
})

const statistics = computed(() => [
  { label: '工具总数', value: '4+', icon: 'mdi:tools' },
  { label: '日访问量', value: formatNumber(stats.value.dailyVisits), icon: 'mdi:eye' },
  { label: '用户数量', value: formatNumber(stats.value.userCount), icon: 'mdi:account-group' },
  { label: '安全评分', value: '98%', icon: 'mdi:shield-check' }
])

const hotTools = ref([
  { 
    name: 'CMS', 
    description: '在线CMS指纹识别工具', 
    icon: 'mdi:shield-search',
    link: '/cms' 
  },
  { 
    name: 'IP查询', 
    description: '在线IP信息查询工具', 
    icon: 'mdi:ip-network',
    link: '/findIp' 
  },
  { 
    name: '密码强度检测', 
    description: '密码强度检测工具', 
    icon: 'mdi:shield-lock',
    link: '/CheckPwd' 
  }
])

const tools = ref([
  { 
    name: 'CMS', 
    description: '在线CMS指纹识别工具', 
    icon: 'mdi:shield-search',
    link: '/cms',
    category: 'scan'
  },
  { 
    name: 'base64编码', 
    description: '在线Base64解码工具', 
    icon: 'mdi:code-brackets',
    link: '/base64',
    category: 'encode'
  },
  { 
    name: 'IP查询', 
    description: '在线IP信息查询工具', 
    icon: 'mdi:ip-network',
    link: '/findIp',
    category: 'analysis'
  },
  { 
    name: '哈希值', 
    description: '在线哈希值生成工具', 
    icon: 'mdi:function-variant',
    link: '/Crehash',
    category: 'encode'
  },
  { 
    name: '密码强度检测工具', 
    description: '密码强度检测工具', 
    icon: 'mdi:shield-lock',
    link: '/CheckPwd',
    category: 'analysis'
  }
])

const filteredTools = computed(() => {
  return tools.value.filter(tool => {
    const matchesSearch = tool.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         tool.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesCategory = selectedCategory.value === 'all' || tool.category === selectedCategory.value
    return matchesSearch && matchesCategory
  })
})

const goToTool = (url: string) => {
  router.push('/root' + url)
}

const increaseUserCount = async () => {
  try {
    await axios.post(ToUrl.url+'/api/stats/users', {}, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    await updateStats() // 更新统计数据
  } catch (error) {
    console.error('Failed to increase user count:', error)
  }
}
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.title {
  text-align: center;
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 40px;
  background: linear-gradient(45deg, #6a1b9a, #9c27b0);
  -webkit-background-clip: text;
  -moz-background-clip: text;
  -ms-background-clip: text;
  -o-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.intro-section {
  text-align: center;
  margin-bottom: 40px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 10px;
}

.intro-text {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
}

.stats-section {
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 24px;
  color: #9c27b0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.search-section {
  margin-bottom: 30px;
  display: flex;
  gap: 20px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.category-group {
  margin-left: auto;
}

.hot-tools {
  margin-bottom: 40px;
}

.hot-tools h2 {
  margin-bottom: 20px;
  color: #303133;
}

.tool-card {
  background-color: #ffffff;
  padding: 20px;
  text-align: center;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 10px;
  border: 1px solid #ebeef5;
  position: relative;
}

.tool-card.hot {
  border: 2px solid #e6a23c;
}

.hot-badge {
  position: absolute;
  top: -10px;
  right: 10px;
  background: #e6a23c;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.tool-icon {
  width: 80px;
  height: 80px;
  color: #9c27b0;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.tool-card:hover .tool-icon {
  transform: rotate(10deg) scale(1.1);
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.15));
}

h3 {
  color: #303133;
  margin: 10px 0;
  font-size: 18px;
}

p {
  color: #606266;
  font-size: 14px;
  min-height: 40px;
  margin-bottom: 15px;
}

.tools-section h2 {
  margin-bottom: 20px;
  color: #303133;
}

.el-row {
  margin-top: 40px;
  justify-content: center;
}
</style>