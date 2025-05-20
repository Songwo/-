<template>
  <div class="challenge-container">
    <div class="challenge-header">
      <img :src="logoUrl" alt="Logo" class="logo" />
      <h2>SQL注入基础</h2>
    </div>

    <el-card class="challenge-card">
      <template #header>
        <div class="card-header">
          <h3>挑战描述</h3>
          <el-tag type="success">难度：★☆☆☆☆</el-tag>
        </div>
      </template>

      <div class="challenge-content">
        <div class="description-section">
          <p>SQL注入是一种常见的Web安全漏洞，攻击者可以通过构造特殊的SQL语句来获取数据库中的敏感信息。本挑战将帮助你理解SQL注入的基本原理和防御方法。</p>
        </div>

        <div class="task-section">
          <h4>任务目标</h4>
          <ul>
            <li>分析登录表单的SQL查询语句</li>
            <li>构造SQL注入语句绕过登录验证</li>
            <li>获取管理员账号的密码</li>
          </ul>
        </div>

        <div class="action-section">
          <el-button type="primary" @click="startLab" :loading="loading">
            启动靶场
          </el-button>
          <el-button type="success" @click="verifyFlag" :disabled="!labUrl">
            验证FLAG
          </el-button>
        </div>

        <div v-if="labUrl" class="lab-info">
          <el-link :href="labUrl" target="_blank" type="primary">
            前往靶场
          </el-link>
          <span class="timer" v-if="remainingTime">
            剩余时间: {{ formatTime(remainingTime) }}
          </span>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="showFlagDialog" title="验证FLAG" width="30%">
      <el-input v-model="inputFlag" placeholder="请输入FLAG" />
      <template #footer>
        <el-button @click="showFlagDialog = false">取消</el-button>
        <el-button type="primary" @click="submitFlag">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ToUrl from '@/api/api'
import { useStore } from 'vuex'

const logoUrl = ref('@/assets/logo/logo.png')
const store = useStore()
const loading = ref(false)
const labUrl = ref('')
const showFlagDialog = ref(false)
const inputFlag = ref('')
const remainingTime = ref(null)
let timer = null

const startLab = async () => {
  loading.value = true
  try {
    const response = await axios.post(
      ToUrl.url + '/lab/create-compose',
      {
        services: [
          {
            serviceName: 'sql-injection-frontend',
            image: 'sql-injection-frontend:latest',
            ports: { '8080': '80' }
          },
          {
            serviceName: 'sql-injection-backend',
            image: 'sql-injection-backend:latest',
            ports: { '3000': '3000' }
          }
        ],
        duration: 30
      },
      { headers: { 'Authorization': `Bearer ${store.state.token}` } }
    )

    labUrl.value = `http://47.117.70.79:${response.data.frontendPort}`
    remainingTime.value = 30 * 60 * 1000 // 30分钟
    startTimer()
    ElMessage.success('靶场启动成功！')
  } catch (error) {
    ElMessage.error('靶场启动失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const verifyFlag = () => {
  showFlagDialog.value = true
}

const submitFlag = async () => {
  if (!inputFlag.value) {
    ElMessage.warning('请输入FLAG')
    return
  }

  try {
    const response = await axios.post(
      ToUrl.url + '/lab/flag',
      {
        userId: store.state.id,
        imageName: 'sql-injection-frontend',
        flag: inputFlag.value
      },
      { headers: { 'Authorization': `Bearer ${store.state.token}` } }
    )

    if (response.data.code === 200) {
      ElMessage.success('验证成功！')
      showFlagDialog.value = false
      inputFlag.value = ''
    } else {
      ElMessage.error('FLAG验证失败')
    }
  } catch (error) {
    ElMessage.error('验证请求失败：' + error.message)
  }
}

const startTimer = () => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value -= 1000
    } else {
      clearInterval(timer)
      labUrl.value = ''
      ElMessage.warning('靶场已过期，请重新启动')
    }
  }, 1000)
}

const formatTime = (ms) => {
  const minutes = Math.floor(ms / 60000)
  const seconds = Math.floor((ms % 60000) / 1000)
  return `${minutes}分${seconds}秒`
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.challenge-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.challenge-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.logo {
  height: 50px;
  width: auto;
}

.challenge-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.challenge-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.challenge-content {
  padding: 20px 0;
}

.description-section {
  margin-bottom: 20px;
}

.description-section p {
  color: #666;
  line-height: 1.6;
}

.task-section {
  margin-bottom: 30px;
}

.task-section h4 {
  color: #333;
  margin-bottom: 10px;
}

.task-section ul {
  padding-left: 20px;
  color: #666;
}

.task-section li {
  margin-bottom: 8px;
}

.action-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.lab-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.timer {
  color: #666;
  font-size: 14px;
}
</style> 