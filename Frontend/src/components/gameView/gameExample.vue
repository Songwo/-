<template>
  <el-container style="height:100vh">
    <!-- 左侧分类导航 -->
    <el-aside width="250px" style="background-color: #f2f2f2; padding: 20px">
      <el-menu :default-active="currentCategoryKey" @select="handleCategorySelect">
        <el-menu-item v-for="category in categories" :key="category.key" :index="category.key"
          :disabled="!category.unlocked">
          <template #title>
            <el-icon v-if="category.unlocked">
              <Unlock />
            </el-icon>
            <el-icon v-else>
              <Lock />
            </el-icon>
            <span style="margin-left: 8px">{{ category.name }}</span>
          </template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧内容区域 -->
    <el-main style="padding: 20px">
      <el-row :gutter="20">
        <el-col v-for="(challenge, index) in currentChallenges" :key="challenge.id" :span="8"
          style="margin-bottom: 20px">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>{{ challenge.title }}</span>
                <el-tag :type="getDifficultyType(challenge.difficulty)">
                  ★{{ challenge.difficulty }}
                </el-tag>
              </div>
            </template>

            <div class="card-body">
              <p>{{ challenge.description }}</p>
              <p><strong>任务：</strong>{{ challenge.task }}</p>
              <p><strong>进度：</strong>{{ getUnlockProgress(challenge) }}</p>
            </div>

            <div class="card-actions" style="margin-top: 10px">
              <el-button type="primary" size="small" @click="startLab(challenge)" :loading="challenge.loading"
                :disabled="challenge.disabled || !challenge.unlocked || challenge.isCompleted || isAnyLabRunning() && !challenge.labUrl">
                启动靶场
              </el-button>
              <el-button type="success" size="small" @click="verifyFlag(challenge)"
                :disabled="!challenge.labUrl || challenge.isCompleted">
                验证FLAG
              </el-button>
            </div>

            <div v-if="challenge.labUrl">
              <el-link :href="challenge.labUrl" target="_blank">
                前往靶场
              </el-link>
              <span v-if="runningLabInfo && runningLabInfo.challengeId === challenge.id">
                (剩余时间: {{ Math.floor(runningLabInfo.remainingSeconds / 1000 / 60) }}分{{
                  Math.floor((runningLabInfo.remainingSeconds / 1000) % 60)
                }}秒)
              </span>
            </div>

            <div v-if="challenge.isCompleted" style="margin-top:10px">
              <el-alert type="success" description="挑战通关！" show-icon />
              <p>完成时间：{{ formatTime(challenge.completionTime) }}</p>
              <p>得分：{{ challenge.score }}/100</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
    <el-dialog v-model="showFlagDialog" title="验证FLAG">
      <el-input v-model="inputFlag" placeholder="请输入FLAG" />
      <template #footer>
        <el-button @click="showFlagDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmVerify">确认</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import ToUrl from '@/api/api'
import { Lock, Unlock } from '@element-plus/icons-vue'
import { useStore } from 'vuex'

const store = useStore();

// 挑战数据（每个分类内包含关卡数组）
const categories = ref([])
//加载靶场漏洞
const fetchChallenges = async () => {
  console.log('token:', JSON.stringify(store.state.token));
  try {
    const response = await axios.get(ToUrl.url + '/api/challenges', {
      headers: { 'Authorization': `Bearer ${store.state.token.trim()}` }
    });
    // 假设后端返回的就是一个 challenge 列表
    const challengeList = response.data;

    console.log('token:', JSON.stringify(store.state.token));
    // 你可以根据 difficulty 分组
    const low = [], medium = [], high = [];
    challengeList.forEach(ch => {
      if (ch.difficulty === 1) low.push(mapChallenge(ch));
      else if (ch.difficulty === 2) medium.push(mapChallenge(ch));
      else if (ch.difficulty === 3) high.push(mapChallenge(ch));
    });

    categories.value = [
      { key: 'low', name: '低级挑战', unlocked: true, challenges: low },
      { key: 'medium', name: '中级挑战', unlocked: false, challenges: medium },
      { key: 'high', name: '高级挑战', unlocked: false, challenges: high }
    ];
  } catch (e) {
    ElMessage.error('获取挑战数据失败');
  }
};

const runningLabInfo = ref(null)
let countdownTimer = null

watch(
  () => runningLabInfo.value && runningLabInfo.value.remainingSeconds,
  (newVal, oldVal) => {
    if (countdownTimer) clearInterval(countdownTimer)
    if (runningLabInfo.value && runningLabInfo.value.remainingSeconds > 0) {
      countdownTimer = setInterval(() => {
        if (runningLabInfo.value.remainingSeconds > 0) {
          runningLabInfo.value.remainingSeconds -= 1000 // 毫秒为单位
        }
        if (runningLabInfo.value.remainingSeconds <= 0) {
          runningLabInfo.value.remainingSeconds = 0
          clearInterval(countdownTimer)
          ElMessage.warning('靶场已到期，请重新启动！')
          // 自动刷新靶场状态
          checkLabStatus()
        }
      }, 1000)
    }
  },
  { immediate: true }
)

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

const checkLabStatus = async () => {
  try {
    for (const category of categories.value) {
      for (const challenge of category.challenges) {
        const res = await axios.get(ToUrl.stadUrl, {
          params: {
            userId: store.state.id,
            challengeId: challenge.id
          }
        });
        console.log(res.data.running)
        console.log(res.data)
        if (res.data.running) {
          runningLabInfo.value = {
            challengeId: challenge.id,
            labUrl: res.data.labUrl,
            remainingSeconds: res.data.remaining
          };
          // 标记当前 challenge
          challenge.labUrl = res.data.labUrl;
          challenge.running = true;
          // 让其他 challenge 不可点击
          categories.value.forEach(cat => {
            cat.challenges.forEach(ch => {
              if (ch.id !== challenge.id) ch.disabled = true;
            });
          });
          return; // 只允许一个靶场运行
        }
      }
    }
    // 如果没有运行中的靶场
    runningLabInfo.value = null;
    categories.value.forEach(cat => {
      cat.challenges.forEach(ch => ch.disabled = false);
    });
  } catch (e) {
    console.error('靶场状态获取失败', e);
  }
};

onMounted(async () => {
  await fetchChallenges();
  await checkLabStatus();
});


// 映射后端 challenge 到前端 challenge
function mapChallenge(ch) {
  return {
    id: ch.id,
    title: ch.title,
    description: ch.description,
    difficulty: ch.difficulty,
    task: ch.task,
    flag: ch.flag,
    score: ch.score,
    labConfig: {
      images: ch.images,
      duration: ch.durationMinutes
    },
    unlocked: ch.unlocked,
    isCompleted: ch.completed,
    completionTime: ch.completionTime,
    labUrl: '',
    loading: false
  }
}
// 当前选中的分类（默认低级）
const currentCategoryKey = ref('low')
const currentChallenges = computed(() => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  return category ? category.challenges : []
})
// 添加响应式变量
const showFlagDialog = ref(false)
const inputFlag = ref('')
let currentVerifyChallenge = ref(null)
// 修改验证方法
const verifyFlag = (challenge) => {
  currentVerifyChallenge.value = challenge
  showFlagDialog.value = true
}
//验证是否通关
const confirmVerify = async () => {
  if (!inputFlag.value || !currentVerifyChallenge.value) return
  // 获取当前挑战对象
  const challenge = currentVerifyChallenge.value
  challenge.loading = true // 需要确保challenge对象是响应式的

  try {
    const response = await axios.post(
      ToUrl.url + '/lab/flag',
      {
        userId: store.state.id,
        imageName: challenge.labConfig.images.frontend.image,
        flag: inputFlag.value
      },
      { headers: { 'Authorization': `Bearer ${store.state.token}` } }
    )

    if (response.data.code == 200) {

      challenge.isCompleted = true;
      challenge.completionTime = new Date();

      store.commit('completeChallenge', {
        categoryKey: currentCategoryKey.value,
        challengeId: challenge.id,
        score: 100
      })
      ElMessage.success('验证成功！挑战通关')
      unlockNextChallenge(challenge)
    } else {
      ElMessage.error('FLAG验证失败，请重试')
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error('验证请求失败：' + error.message)
  } finally {
    challenge.loading = false
    showFlagDialog.value = false
    inputFlag.value = ''
  }
}

// 处理侧边菜单切换
const handleCategorySelect = (key) => {
  currentCategoryKey.value = key
}

// 根据难度返回 el-tag 类型
const getDifficultyType = (difficulty) => {
  if (difficulty === 1) return 'success'
  if (difficulty === 2) return 'warning'
  if (difficulty === 3) return 'danger'
  return 'info'
}

// 返回解锁进度，如"1/3"
const getUnlockProgress = (challenge) => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  if (!category) return ''
  const index = category.challenges.findIndex(c => c.id === challenge.id)
  return `${index + 1}/${category.challenges.length}`
}

// 启动靶场方法
const startLab = async (challenge) => {
  if (!challenge.unlocked || challenge.loading || challenge.isCompleted || isAnyLabRunning()) return

  challenge.loading = true
  try {
    // 生成随机服务名后缀和端口
    const randomSuffix = Math.floor(Math.random() * 100) + 1
    const frontendPort = Math.floor(Math.random() * 100) + 8081
    const backendPort = Math.floor(Math.random() * 100) + 3000
    const mysqlPort = Math.floor(Math.random() * 100) + 3300

    // 构建服务名称
    const frontendName = `frontend${randomSuffix}`
    const backendName = `backend${randomSuffix}`
    const mysqlName = `mysql${randomSuffix}`

    // 构建多服务请求
    const requestData = {
      services: [
        {
          serviceName: frontendName,
          image: challenge.labConfig.images.frontend.image,
          ports: { [frontendPort.toString()]: challenge.labConfig.images.frontend.port },
          env: {
            VITE_BACKEND_NAME: backendName
          },
          dependsOn: [backendName]
        },
        {
          serviceName: backendName,
          image: challenge.labConfig.images.backend.image,
          ports: { [backendPort.toString()]: challenge.labConfig.images.backend.port },
          env: {
            DB_HOST: mysqlName,
            DB_PORT: "3306",
            DB_NAME: "vulnerable_db",
            DB_USER: "root",
            DB_PASSWORD: "123456"
          },
          dependsOn: [mysqlName]
        },
        {
          serviceName: mysqlName,
          image: challenge.labConfig.images.mysql.image,
          ports: { [mysqlPort.toString()]: challenge.labConfig.images.mysql.port },
          env: {
            MYSQL_ROOT_PASSWORD: "123456",
            MYSQL_DATABASE: "vulnerable_db"
          }
        }
      ],
      duration: challenge.labConfig.duration || 30
    }

    // 发送请求
    const response = await axios.post(
      ToUrl.url + '/lab/create-compose',
      requestData,
      { headers: { 'Authorization': `Bearer ${store.state.token}` } }
    )

    // 更新数据 - 使用前端端口作为访问链接
    challenge.labUrl = `http://47.117.70.79:${frontendPort}`
    // 启动靶场后
    await axios.post(ToUrl.staUrl, {
      userId: store.state.id,
      labUrl: challenge.labUrl,
      startTime: Date.now(),
      duration: challenge.labConfig.duration,
      challengeId: challenge.id
    });
    ElMessage.success('靶场启动成功！')
    const durationMs = (challenge.labConfig.duration || 30) * 60 * 1000
    runningLabInfo.value = {
      challengeId: challenge.id,
      labUrl: challenge.labUrl,
      remainingSeconds: durationMs
    }
  } catch (error) {
    console.error('启动失败:', error)
    ElMessage.error(`靶场启动失败: ${error.response?.data?.message || error.message}`)
  } finally {
    challenge.loading = false
  }
}

// 解锁下一关：如果当前分类内还有下一关，则解锁下一关；否则解锁下个分类的第一关
const unlockNextChallenge = (currentChallenge) => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  if (!category) return

  const index = category.challenges.findIndex(c => c.id === currentChallenge.id)
  if (index < category.challenges.length - 1) {
    category.challenges[index + 1].unlocked = true
    ElNotification({
      title: '新关卡解锁',
      message: `${category.challenges[index + 1].title} 已解锁！`,
      type: 'success'
    })
  } else {
    // 当前分类全部完成，解锁下一分类
    const currentIndex = categories.value.findIndex(c => c.key === currentCategoryKey.value)
    if (currentIndex < categories.value.length - 1) {
      const nextCategory = categories.value[currentIndex + 1]
      nextCategory.unlocked = true
      if (nextCategory.challenges.length > 0) {
        nextCategory.challenges[0].unlocked = true
      }
      ElNotification({
        title: '新分类解锁',
        message: `${nextCategory.name} 已解锁！`,
        type: 'success'
      })
    }
  }
}

// 格式化时间显示
const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : 'N/A'
}

const isAnyLabRunning = () => {
  return categories.value.some(cat =>
    cat.challenges.some(ch => ch.labUrl && !ch.isCompleted)
  )
}

</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.card-body p {
  margin: 5px 0;
}

.el-card {
  min-height: 300px;
}

.el-aside {
  border-right: 1px solid #e0e0e0;
}
</style>