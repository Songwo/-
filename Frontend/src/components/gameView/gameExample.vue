<template>
  <div class="challenge-container">
    <!-- 顶部导航栏 -->
    <div class="top-navigation">
      <el-menu :default-active="currentCategoryKey" mode="horizontal" @select="handleCategorySelect">
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
    </div>

    <!-- 教程按钮 -->
    <div class="tutorial-button-container">
      <el-button type="primary" @click="showTutorial" class="tutorial-button">
        <el-icon><Guide /></el-icon>
        <span style="margin-left: 4px">教程</span>
      </el-button>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card class="challenge-card" v-for="(challenge, index) in currentChallenges" :key="challenge.id">
            <template #header>
              <div class="card-header">
                <div class="title-section">
                  <h2>{{ challenge.title }}</h2>
                  <el-tag :type="getDifficultyType(challenge.difficulty)" class="difficulty-tag">
                    ★{{ challenge.difficulty }}
                  </el-tag>
                </div>
                <div class="progress-section">
                  <el-progress :percentage="getProgressPercentage(challenge)" :status="getProgressStatus(challenge)" />
                  <span class="progress-text">{{ getUnlockProgress(challenge) }}</span>
                </div>
              </div>
            </template>

            <div class="card-content">
              <el-row :gutter="20">
                <el-col :span="16">
                  <div class="left-content">
                    <div class="description-section">
                      <h3>
                        <el-icon><Document /></el-icon>
                        挑战描述
                      </h3>
                      <p>{{ challenge.description }}</p>
                    </div>

                    <div class="task-section">
                      <h3>
                        <el-icon><Flag /></el-icon>
                        任务目标
                      </h3>
                      <p>{{ challenge.task }}</p>
                    </div>

                    <div class="tutorial-section">
                      <h3>
                        <el-icon><Guide /></el-icon>
                        教程指引
                      </h3>
                      <div class="tutorial-steps">
                        <div v-for="(step, idx) in getTutorialSteps(challenge)" :key="idx" class="tutorial-step">
                          <el-icon><Check /></el-icon>
                          <span>{{ step }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-col>

                <el-col :span="8">
                  <div class="right-content">
                    <div class="vuln-overview">
                      <el-button type="info" link @click="toggleVulnDetails(challenge)" class="vuln-btn">
                        {{ challenge.showVulnDetails ? '收起漏洞概述' : '查看漏洞概述' }}
                      </el-button>
                      
                      <transition-group name="vuln-step" tag="div" class="vuln-details" v-show="challenge.showVulnDetails">
                        <div v-for="step in challenge.vulnSteps" :key="step.title"
                          class="vuln-step" v-show="step.visible">
                          <div class="step-icon">{{ step.icon }}</div>
                          <div class="step-content">
                            <h4>{{ step.title }}</h4>
                            <p>{{ step.content }}</p>
                          </div>
                        </div>
                      </transition-group>
                    </div>

                    <div class="action-section">
                      <div class="action-buttons">
                        <el-button type="primary" size="large" @click="startLab(challenge)" 
                          :loading="challenge.loading"
                          :disabled="challenge.disabled || !challenge.unlocked || challenge.isCompleted || isAnyLabRunning() && !challenge.labUrl">
                          启动靶场
                        </el-button>
                        <el-button type="success" size="large" @click="verifyFlag(challenge)"
                          :disabled="!challenge.labUrl || challenge.isCompleted">
                          验证FLAG
                        </el-button>
                      </div>
                      
                      <div v-if="challenge.loading" class="startup-progress">
                        <el-progress 
                          :percentage="challenge.startProgress"
                          :status="challenge.startProgress === 100 ? 'success' : ''"
                          :stroke-width="20"
                          :show-text="true">
                          <template #default="{ percentage }">
                            <span class="progress-text">
                              {{ percentage < 100 ? '启动中...' : '启动完成' }}
                              {{ percentage }}%
                            </span>
                          </template>
                        </el-progress>
                      </div>

                      <div v-if="challenge.labUrl" class="lab-info">
                        <el-link :href="challenge.labUrl" target="_blank" type="primary" class="lab-link">
                          前往靶场
                        </el-link>
                        <span v-if="runningLabInfo && runningLabInfo.challengeId === challenge.id" class="timer">
                          (剩余时间: {{ Math.floor(runningLabInfo.remainingSeconds / 1000 / 60) }}分{{
                            Math.floor((runningLabInfo.remainingSeconds / 1000) % 60)
                          }}秒)
                        </span>
                      </div>

                      <div v-if="challenge.isCompleted" class="completion-section">
                        <el-alert type="success" description="挑战通关！" show-icon />
                        <div class="completion-details">
                          <p>完成时间：{{ formatTime(challenge.completionTime) }}</p>
                          <p>得分：{{ challenge.score }}/100</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 教程对话框 -->
    <el-dialog
      v-model="showTutorialDialog"
      title="靶场教程"
      width="60%"
      class="tutorial-dialog"
    >
      <div class="tutorial-content">
        <el-carousel
          ref="tutorialCarousel"
          :interval="0"
          :autoplay="false"
          indicator-position="none"
          height="400px"
          class="tutorial-carousel"
        >
          <el-carousel-item v-for="(step, index) in tutorialSteps" :key="index">
            <div class="tutorial-step">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-info">
                <h3>{{ step.title }}</h3>
                <p>{{ step.description }}</p>
                <div class="step-image" @click="handleImageClick(step.image)">
                  <el-image 
                    :src="step.image" 
                    fit="cover" 
                    style="width: 100%; max-width: 400px; border-radius: 8px; margin-top: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.3); cursor: pointer;"
                  />
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
        
        <div class="tutorial-navigation">
          <el-button 
            :disabled="currentStep === 0"
            @click="prevStep"
            class="nav-button"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          
          <div class="step-indicators">
            <div 
              v-for="(step, index) in tutorialSteps" 
              :key="index"
              :class="['step-dot', { active: currentStep === index }]"
              @click="goToStep(index)"
            ></div>
          </div>
          
          <el-button 
            :disabled="currentStep === tutorialSteps.length - 1"
            @click="nextStep"
            class="nav-button"
          >
            下一步
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- FLAG验证对话框 -->
    <el-dialog v-model="showFlagDialog" title="验证FLAG" width="30%">
      <el-input v-model="inputFlag" placeholder="请输入FLAG" />
      <template #footer>
        <el-button @click="showFlagDialog = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="confirmVerify">确认</el-button>
      </template>
    </el-dialog>

    <!-- 添加图片预览对话框 -->
    <el-dialog
      v-model="showImagePreview"
      width="80%"
      class="image-preview-dialog"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="preview-container">
        <el-image 
          :src="currentPreviewImage" 
          fit="contain"
          style="width: 100%; height: 80vh;"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import ToUrl from '@/api/api'
import { Lock, Unlock, Check, Loading, Document, Flag, Guide, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import gsap from 'gsap'

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
          // 让其他 challenge 不可点击-
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
    loading: false,
    startProgress: 0,
    showVulnDetails: true, // 默认展开漏洞概述
    vulnSteps: [
      {
        title: '漏洞原理',
        content: '详细解释漏洞的技术原理和成因',
        icon: '🔍',
        visible: true
      },
      {
        title: '攻击流程',
        content: '分步骤展示攻击者如何利用该漏洞',
        icon: '⚡',
        visible: true
      },
      {
        title: '防御措施',
        content: '介绍如何修复和预防该漏洞',
        icon: '🛡️',
        visible: true
      }
    ]
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
  challenge.startProgress = 0
  
  // 创建进度条动画
  const progressAnimation = gsap.to(challenge, {
    startProgress: 100,
    duration: 50,
    ease: "linear",
    onUpdate: () => {
      challenge.startProgress = Math.round(challenge.startProgress)
    }
  })

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

    progressAnimation.kill() // 停止进度条动画
    challenge.startProgress = 100 // 设置为100%
    
    ElMessage.success('靶场启动成功！')
    const durationMs = (challenge.labConfig.duration || 30) * 60 * 1000
    runningLabInfo.value = {
      challengeId: challenge.id,
      labUrl: challenge.labUrl,
      remainingSeconds: durationMs
    }
  } catch (error) {
    progressAnimation.kill() // 停止进度条动画
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

// 获取教程步骤
const getTutorialSteps = (challenge) => {
  return [
    '了解漏洞的基本原理',
    '分析目标系统的安全机制',
    '尝试利用漏洞获取系统权限',
    '获取并提交正确的FLAG'
  ]
}

// 计算进度百分比
const getProgressPercentage = (challenge) => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  if (!category) return 0
  const index = category.challenges.findIndex(c => c.id === challenge.id)
  return Math.round((index / category.challenges.length) * 100)
}

// 获取进度状态
const getProgressStatus = (challenge) => {
  if (challenge.isCompleted) return 'success'
  if (challenge.unlocked) return 'warning'
  return 'exception'
}

// 触发漏洞详情动画
const toggleVulnDetails = (challenge) => {
  challenge.showVulnDetails = !challenge.showVulnDetails
  if (challenge.showVulnDetails) {
    // 依次显示每个步骤
    challenge.vulnSteps.forEach((step, index) => {
      setTimeout(() => {
        step.visible = true
      }, index * 500)
    })
  } else {
    // 隐藏所有步骤
    challenge.vulnSteps.forEach(step => step.visible = false)
  }
}

// 教程相关
const showTutorialDialog = ref(false)
const tutorialCarousel = ref(null)
const currentStep = ref(0)

const tutorialSteps = [
  {
    title: '启动靶场',
    description: '点击"启动靶场"按钮，系统会自动为你创建一个包含漏洞的靶场环境。启动过程可能需要一些时间，请耐心等待。',
    image: '/src/assets/Jiaoc/start.png'
  },
  {
    title: '访问靶场',
    description: '靶场启动成功后，点击"前往靶场"链接，系统会在新标签页中打开靶场环境。',
    image: '/src/assets/Jiaoc/前往靶场.png'
  },
  {
    title: '分析目标',
    description: '仔细阅读挑战描述和任务目标，分析目标系统中可能存在的漏洞。可以查看漏洞概述来了解相关漏洞的原理和利用方法。',
    image: '/src/assets/Jiaoc/分析.png'
  },
  {
    title: '获取FLAG',
    description: '利用发现的漏洞，尝试获取系统中的FLAG。FLAG通常是一串特定的字符串，可能隐藏在系统的某个位置。',
    image: '/src/assets/Jiaoc/通关.png'
  },
  {
    title: '提交验证',
    description: '获取到FLAG后，点击"验证FLAG"按钮，将FLAG提交给系统进行验证。验证成功后即可完成挑战。',
    image: '/src/assets/Jiaoc/验证.png'
  }
]

const showTutorial = () => {
  showTutorialDialog.value = true
  currentStep.value = 0
  if (tutorialCarousel.value) {
    tutorialCarousel.value.setActiveItem(0)
  }
}

const nextStep = () => {
  if (currentStep.value < tutorialSteps.length - 1) {
    currentStep.value++
    tutorialCarousel.value?.setActiveItem(currentStep.value)
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
    tutorialCarousel.value?.setActiveItem(currentStep.value)
  }
}

const goToStep = (index) => {
  currentStep.value = index
  tutorialCarousel.value?.setActiveItem(index)
}

const showImagePreview = ref(false)
const currentPreviewImage = ref('')

const handleImageClick = (imageUrl) => {
  currentPreviewImage.value = imageUrl
  showImagePreview.value = true
}

</script>

<style scoped>
.challenge-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: transparent;
  position: relative;
}

.top-navigation {
  background: transparent;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.top-navigation .el-menu {
  background: transparent;
  border-bottom: none;
  flex: 1;
}

.top-navigation .el-menu-item {
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  height: 60px;
  line-height: 60px;
  opacity: 0.8;
}

.top-navigation .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  opacity: 1;
}

.top-navigation .el-menu-item.is-active {
  color: #ffffff !important;
  border-bottom: 2px solid var(--el-color-primary);
  background: rgba(255, 255, 255, 0.1);
  opacity: 1;
}

.top-navigation .el-menu-item.is-disabled {
  color: rgba(255, 255, 255, 0.6);
  opacity: 0.8;
}

/* 确保选中状态下的文字颜色为白色 */
:deep(.el-menu-item.is-active) {
  color: #ffffff !important;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #ffffff !important;
}

:deep(.el-menu-item:not(.is-active)) {
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.el-menu-item:not(.is-active) .el-icon) {
  color: rgba(255, 255, 255, 0.8) !important;
}

.tutorial-button-container {
  position: fixed;
  top: 78px;
  right: 70px;
  z-index: 1000;
  height: 60px;
  display: flex;
  align-items: center;
}

.tutorial-button {
  background: linear-gradient(135deg, #6e45e2 0%, #88d3ce 100%);
  border: none;
  color: #ffffff;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(110, 69, 226, 0.3);
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
}

.tutorial-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(110, 69, 226, 0.4);
  background: linear-gradient(135deg, #7d52e8 0%, #93d8d3 100%);
}

.tutorial-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(110, 69, 226, 0.3);
}

.tutorial-button .el-icon {
  font-size: 20px;
  transition: transform 0.3s ease;
}

.tutorial-button:hover .el-icon {
  transform: scale(1.1);
}

.tutorial-button-container::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #6e45e2 0%, #88d3ce 100%);
  border-radius: 10px;
  z-index: -1;
  opacity: 0.5;
  filter: blur(8px);
  transition: all 0.3s ease;
}

.tutorial-button-container:hover::after {
  opacity: 0.8;
  filter: blur(12px);
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.challenge-card {
  margin-bottom: 20px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-section h2 {
  margin: 0;
  color: #ffffff;
}

.difficulty-tag {
  color: #ffffff;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.difficulty-tag.el-tag--success {
  border-color: var(--el-color-success);
  color: #ffffff;
}

.difficulty-tag.el-tag--warning {
  border-color: var(--el-color-warning);
  color: #ffffff;
}

.difficulty-tag.el-tag--danger {
  border-color: var(--el-color-danger);
  color: #ffffff;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-text {
  color: #ffffff;
  font-size: 14px;
}

.card-content {
  padding: 20px 0;
}

.left-content {
  padding-right: 20px;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
}

.right-content {
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.description-section,
.task-section,
.tutorial-section {
  margin-bottom: 20px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
  
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.description-section:hover,
.task-section:hover,
.tutorial-section:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
}

.description-section h3,
.task-section h3,
.tutorial-section h3 {
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 10px;
  font-size: 18px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.description-section h3 .el-icon,
.task-section h3 .el-icon,
.tutorial-section h3 .el-icon {
  font-size: 20px;
  color: var(--el-color-primary);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
}

.tutorial-steps {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.tutorial-step {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  justify-content: center;
  width: 100%;
  padding: 4px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.tutorial-step .el-icon {
  color: var(--el-color-success);
  font-size: 16px;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
}

/* 使用深度选择器来确保样式能够穿透scoped限制 */
:deep(.tutorial-step span) {
    color: rgb(255, 255, 255) !important;
    font-size: 14px;
    line-height: 1.6;
    text-align: center;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 备用选择器 */
.tutorial-step :deep(span) {
    color: rgb(255, 255, 255) !important;
    font-size: 14px;
    line-height: 1.6;
    text-align: center;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 全局样式覆盖 */
:global(.tutorial-step span) {
    color: rgb(255, 255, 255) !important;
    font-size: 14px;
    line-height: 1.6;
    text-align: center;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.vuln-overview {
  margin-bottom: 20px;
  width: 100%;
}

.vuln-btn {
  margin-bottom: 10px;
  width: 100%;
  text-align: center;
  color: #ffffff !important;
  opacity: 0.9;
}

.vuln-btn:hover {
  opacity: 1;
}

.vuln-details {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 15px;
  width: 100%;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.vuln-step {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 15px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.vuln-step:hover {
  background: rgba(255, 255, 255, 0.12);
  transform: translateX(5px);
}

.step-icon {
  font-size: 20px;
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
}

.step-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #ffffff;
}

.step-content p {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.action-buttons .el-button {
  width: 100%;
  height: 40px;
  margin-left: -0px;
  opacity: 0.9;
}

.action-buttons .el-button:hover {
  opacity: 1;
}

.action-buttons .el-button--primary {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #ffffff;
}

.action-buttons .el-button--success {
  background: var(--el-color-success);
  border-color: var(--el-color-success);
  color: #ffffff;
}

.action-buttons .el-button:disabled {
  opacity: 0.5;
}

.startup-progress {
  margin-top: 10px;
}

.lab-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  margin-top: 10px;
}

.lab-link {
  font-size: 16px;
  font-weight: bold;
  padding: 8px 16px;
  background: var(--el-color-primary);
  border-radius: 6px;
  color: #ffffff !important;
  text-decoration: none;
  transition: all 0.3s ease;
}

.lab-link:hover {
  background: var(--el-color-primary-light-3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

:deep(.lab-link .el-link__inner) {
  color: #ffffff !important;
}

.timer {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin-top: 4px;
}

.completion-section {
  margin-top: 10px;
}

.completion-details {
  margin-top: 8px;
  text-align: center;
}

@media (max-width: 768px) {
  .left-content {
    padding-right: 0;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    margin-bottom: 20px;
    padding-bottom: 20px;
  }

  .right-content {
    padding-left: 0;
  }

  .action-buttons {
    flex-direction: column;
  }

  .vuln-step {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}

.cancel-btn {
  color: #000000 !important;
}

:deep(.cancel-btn) {
  color: #000000 !important;
}

:deep(.cancel-btn:hover) {
  color: #000000 !important;
}

.tutorial-dialog {
  :deep(.el-dialog) {
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    max-height: 90vh;
    width: 80% !important;
    max-width: 1200px;
    margin-top: 5vh !important;
  }

  :deep(.el-dialog__title) {
    color: #000000;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #000000;
  }

  .tutorial-step {
    display: flex;
    align-items: flex-start;
    padding: 15px 25px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 12px;
    width: 100%;
    max-width: 1000px;
    margin: 0 auto;
    transition: all 0.3s ease;
    min-height: 450px;
  }

  .step-info {
    flex: 1;
    padding-right: 20px;
    display: flex;
    flex-direction: column;
  }

  .step-info h3 {
    color: #000000;
    margin: 0 0 15px 0;
    font-size: 24px;
    font-weight: bold;
  }

  .step-info p {
    color: #000000 !important;
    margin: 0;
    line-height: 1.8;
    font-size: 18px;
  }

  :deep(.el-carousel__item) {
    color: #000000;
  }

  :deep(.el-carousel__item p) {
    color: #000000 !important;
  }

  :deep(.el-carousel__item h3) {
    color: #000000 !important;
  }
}

.tutorial-content {
  padding: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tutorial-carousel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.step-number {
  width: 50px;
  height: 50px;
  background: rgba(85, 36, 173, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 20px;
  font-weight: bold;
  margin-right: 25px;
  flex-shrink: 0;
}

.step-info {
  flex: 1;
  max-height: 450px;
  overflow-y: auto;
  padding-right: 10px;
}

.step-info::-webkit-scrollbar {
  width: 6px;
}

.step-info::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.step-info::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.step-info::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.step-image {
  width: 100%;
  max-width: 400px;
  margin-top: 15px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
}

.tutorial-navigation {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  margin-top: 10px;
}

.nav-button {
  background: rgba(85, 36, 173, 0.6);
  border: none;
  color: #ffffff;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  font-size: 14px;
}

.nav-button:hover:not(:disabled) {
  background: rgba(85, 36, 173, 0.8);
  transform: translateY(-2px);
}

.nav-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.step-indicators {
  display: flex;
  gap: 12px;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.step-dot:hover {
  background: rgba(255, 255, 255, 0.4);
}

.step-dot.active {
  background: rgba(85, 36, 173, 0.8);
  transform: scale(1.2);
}

.description-section p[data-v-8c3679b0],
.task-section p[data-v-8c3679b0],
.tutorial-section p[data-v-8c3679b0] {
  color: rgba(255, 255, 255, 0.9) !important;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  line-height: 1.6;
  margin: 0;
}

/* 确保所有段落都使用浅色文字 */
p[data-v-8c3679b0] {
  color: rgba(255, 255, 255, 0.9) !important;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 添加图片预览相关样式 */
.image-preview-dialog {
  :deep(.el-dialog) {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    overflow: hidden;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  .preview-container {
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(0, 0, 0, 0.9);
  }
}

.step-image {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.step-image:hover {
  transform: scale(1.02);
}

.step-image:active {
  transform: scale(0.98);
}
</style>