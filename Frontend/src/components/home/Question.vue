<template>
  <div class="course-container">
    <!-- 左侧导航栏 -->
    <div class="course-sidebar">
      <div class="sidebar-header">
        <h3>课程分类</h3>
      </div>
      <el-menu :default-active="activeCategory" @select="handleCategorySelect" class="category-menu" accordion>
        <el-sub-menu v-for="category in categories" :key="category.id" :index="category.id.toString()">
          <template #title>
            <span class="menu-title">
              <el-icon><Folder /></el-icon>
              {{ category.name }}
            </span>
          </template>
          <el-menu-item v-for="subcategory in category.subcategories" :key="subcategory.id"
            :index="subcategory.id.toString()">
            <el-icon><Document /></el-icon>
            {{ subcategory.name }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="course-main">
      <!-- 试卷列表视图 -->
      <div v-if="!currentExam" class="course-content">
        <div class="content-header">
          <h2>测试题库</h2>
          <div class="search-bar">
            <el-input placeholder="搜索测试..." prefix-icon="Search" />
          </div>
        </div>
        
        <div class="exam-grid">
          <div v-for="exam in filteredExams" :key="exam.id" 
               class="exam-card" 
               :class="{ 'disabled-card': exam.isSubmitted }" 
               @click="startExam(exam)">
            <div class="card-cover">
              <el-image :src="getRandomCourseImage()" fit="cover" />
              <div class="card-tag" v-if="exam.isSubmitted">
                <el-tag type="success" effect="dark">已完成</el-tag>
              </div>
            </div>
            <div class="card-content">
              <h3>{{ exam.title }}</h3>
              <p>{{ exam.description }}</p>
              <div class="card-meta">
                <span><el-icon><Timer /></el-icon> {{ exam.duration }}分钟</span>
                <span><el-icon><Document /></el-icon> {{ exam.questionCount }}题</span>
              </div>
              <div class="card-footer">
                <el-button type="primary" plain>开始测试</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 考试界面 -->
      <div v-else class="exam-interface">
        <div class="exam-header">
          <div class="header-left">
            <el-button @click="cancelExam" circle>
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <h2>{{ currentExam.title }}</h2>
          </div>
          <div class="timer">
            <el-icon><Clock /></el-icon>
            <span>剩余时间: {{ formattedTime }}</span>
          </div>
        </div>

        <div class="exam-content">
          <el-form :model="answers">
            <div v-for="(question, index) in currentExam.questions" :key="question.id" class="question-item">
              <div class="question-header">
                <span class="question-number">第{{ index + 1 }}题</span>
                <h4>{{ question.title }}</h4>
              </div>
              <el-form-item :prop="`answers[${question.id}].value`" :rules="[{ validator: validateAnswer(question) }]">
                <component :is="getQuestionComponent(question.type)" v-model="answers[question.id].value">
                  <template v-if="question.type === 'single'">
                    <el-radio v-for="option in question.options" :key="option.value" :label="option.value">
                      <span class="option-content">{{ option.label }}</span>
                    </el-radio>
                  </template>
                  <template v-if="question.type === 'multiple'">
                    <el-checkbox v-for="option in question.options" :key="option.value" :label="option.value">
                      <span class="option-content">{{ option.label }}</span>
                    </el-checkbox>
                  </template>
                </component>
              </el-form-item>
            </div>
          </el-form>
        </div>

        <div class="exam-footer">
          <el-button type="primary" size="large" @click="submitExam">提交试卷</el-button>
        </div>

        <!-- 结果展示 -->
        <div v-if="currentExam?.isSubmitted" class="exam-results">
          <div class="result-header">
            <el-alert :title="`本次得分: ${scoreInfo.scoreDelta}`" type="success" show-icon />
          </div>
          
          <div class="result-content">
            <div v-for="question in currentExam.questions" :key="question.id" class="result-item">
              <div class="result-question">
                <h4 :class="getResultClass(question.id)">
                  {{ question.title }}
                  <span class="result-tag">{{ getResultTag(question.id) }}</span>
                </h4>
                
                <div class="answer-section">
                  <div class="correct-answer">
                    <span class="label">正确答案:</span>
                    <span class="value">{{ formatCorrectAnswer(question) }}</span>
                    <span class="score-delta">+{{ resultDetails[question.id]?.scoreDelta }}</span>
                  </div>
                  
                  <div class="user-answer">
                    <span class="label">您的答案:</span>
                    <span class="value">{{ formatUserAnswer(question.id) || "未作答" }}</span>
                  </div>
                </div>

                <div class="result-message" v-if="resultDetails[question.id]?.message">
                  {{ resultDetails[question.id].message }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Folder, Document, Timer, ArrowLeft, Search } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import axios from 'axios'
import ToUrl from '@/api/api'

const store = useStore()

// 响应式数据
const activeCategory = ref('')
const currentExam = ref(null)
const timeLeft = ref(0)
const answers = ref({})
const timer = ref(null)
const questions = ref([]) // 存储原始题目数据
const categories = ref([]) // 动态生成的分类结构
const resultDetails = ref({})
const scoreInfo = ref({
  scoreDelta: 0,
  totalScore: 0
})
const exams = ref([]) // 改为ref而不是computed

// 试卷数据
const examsData = computed(() => {
  return categories.value.flatMap(category =>
    category.subcategories.map(sub => ({
      id: sub.id,
      categoryId: category.id,
      title: `${category.name} - ${sub.name}`,
      description: `${sub.name}测试题库`,
      duration: 60,
      questionCount: 0, // 初始为0，加载后更新
      isSubmitted: false,
      questions: [] // 初始为空，点击后加载
    }))
  )
});

// 获取试题数据
const fetchQuestions = async () => {
  try {
    const response = await axios.get(ToUrl.url+'/api/questions', {
      headers: { Authorization: `Bearer ${store.state.token}` }
    })

    if (response.data.code === 200) {
      questions.value = response.data.data
      generateCategories()
    }
    // console.log(response.data.data);
    // console.log(questions.value);
  } catch (err) {
    ElMessage.error('试题加载失败: ' + err.message)
  }
}
//获取分类方法
const fetchCategories = async () => {
  try {
    const response = await axios.get(ToUrl.url+'/api/get_challenge', {
      headers: { Authorization: `Bearer ${store.state.token}` }
    })

    if (response.data.code === 200) {
      processCategories(response.data.data)
    }
  } catch (err) {
    ElMessage.error('分类加载失败: ' + err.message)
  }
}

// 分类数据处理方法
const processCategories = (data) => {
  // 转换分类数据结构
  const mainCategories = Object.entries(data)
    .filter(([key, value]) => key.startsWith('challengel') && value !== null)
    .map(([key, value]) => ({
      id: key,
      name: value,
      subcategories: [
        {
          id: `${key}-basic`,
          name: `${value}基础测试`,
          type: 'basic'
        },
        {
          id: `${key}-advanced`,
          name: `${value}进阶测试`,
          type: 'advanced'
        }
      ]
    }));

  categories.value = mainCategories;

  if (categories.value.length > 0) {
    activeCategory.value = categories.value[0].subcategories[0].id;
  }
};

// 动态生成分类结构
const generateCategories = () => {
  const categoryMap = new Map()

  questions.value.forEach(question => {
    const processedQuestion = {
      ...question,
      type: 'single',
      // 将选项转换为字母形式
      options: question.options.map((text, index) => ({
        value: String.fromCharCode(65 + index), // 0->A, 1->B...
        label: `${String.fromCharCode(65 + index)}. ${text}`
      })),
      // 转换正确答案为字母形式
      answer: String.fromCharCode(65 + question.options.findIndex(opt => opt === question.answer))
    }

    if (!categoryMap.has(question.category)) {
      categoryMap.set(question.category, {
        id: question.category,
        name: question.category,
        questions: [],
        subcategories: [{ id: 'default', name: '全部题目' }]
      })
    }
    categoryMap.get(question.category).questions.push(processedQuestion)
  })

  categories.value = Array.from(categoryMap.values())
  if (categories.value.length > 0) {
    activeCategory.value = categories.value[0].id
  }
}
// 题目处理方法
const processQuestion = (question) => ({
  ...question,
  type: 'single',
  options: question.options.map((text, i) => ({
    value: String.fromCharCode(65 + i),
    label: `${String.fromCharCode(65 + i)}. ${text}`
  })),
  answer: String.fromCharCode(65 + question.options.indexOf(question.answer))
})

// 计算属性
const filteredExams = computed(() => {
  return exams.value.filter(exam =>
    exam.categoryId === activeCategory.value.split('-')[0] && // 匹配分类ID
    !exam.isSubmitted
  )
})

// 答案校验逻辑
const allQuestionsAnswered = computed(() => {
  if (!currentExam.value) return false
  return currentExam.value.questions.every(question => {
    const answer = answers.value[question.id]?.value
    if (question.type === 'multiple') {
      return Array.isArray(answer) && answer.length > 0
    }
    return answer !== undefined && answer !== ''
  })
})

// 提交方法
async function submitExam() {
  try {
    if (!allQuestionsAnswered.value) {
      ElMessage.warning('请完成所有题目后再提交')
      return
    }

    const payload = {
      examId: currentExam.value.id,
      answers: Object.fromEntries(
        Object.entries(answers.value).map(([id, ans]) => [
          id.replace(/^q_/, '').split('-')[0],
          Array.isArray(ans.value) ? ans.value.join(',') : ans.value
        ])
      )
    }

    const response = await axios.post(ToUrl.url+'/api/submit', payload, {
      headers: { Authorization: `Bearer ${store.state.token}` }
    })

    if (response.data.code === 200) {
      // 存储结果数据
      scoreInfo.value = {
        scoreDelta: response.data.data.scoreDelta,
        totalScore: response.data.data.totalScore
      }

      response.data.data.details.forEach(detail => {
        resultDetails.value[detail.questionId] = detail
      })

      // 更新考试状态
      const examIndex = exams.value.findIndex(e => e.id === currentExam.value.id);
      if (examIndex !== -1) {
        exams.value[examIndex].isSubmitted = true;
      }

      currentExam.value.isSubmitted = true;
      ElMessage.success('提交成功');
    }
  } catch (err) {
    ElMessage.error('提交失败: ' + err.message);
  }
}

// 在onMounted中调整调用顺序
onMounted(async () => {
  await fetchQuestions();  // 先获取题目
  await fetchCategories(); // 再获取分类（分类处理需要题目数据）
  linkQuestionsToCategories(); // 最后关联题目到分类
  
  // 自动加载第一个分类的题目
  if (categories.value.length > 0 && categories.value[0].subcategories.length > 0) {
    const firstSubCategory = categories.value[0].subcategories[0].id;
    await handleCategorySelect(firstSubCategory);
  }
});

const formattedTime = computed(() => {
  const minutes = Math.floor(timeLeft.value / 60)
  const seconds = timeLeft.value % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
})

// 方法
const handleCategorySelect = async (subCategoryId) => {
  try {
    const loadingInstance = ElMessage({
      type: 'info',
      message: '正在加载题目...',
      duration: 0,
      icon: Clock,
    });

    const [categoryId, type] = subCategoryId.split('-');
    const category = categories.value.find(c => c.id === categoryId);

    // 初始化题目列表
    let allQuestions = [];
    let page = 0;
    const pageSize = 10;

    // 循环加载题目直到达到100个题目
    while (allQuestions.length < 100) {
      const response = await axios.get(ToUrl.url+'/api/questions', {
        params: {
          category: category.name,
          difficulty: type === 'basic' ? 1 : 2,
          count: pageSize,
          page: page
        },
        headers: { Authorization: `Bearer ${store.state.token}` }
      });

      const processedQuestions = response.data.data.map(q => ({
        ...q,
        id: q.id,
        type: 'single',
        options: q.options.map((text, i) => ({
          value: String.fromCharCode(65 + i),
          label: `${String.fromCharCode(65 + i)}. ${text}`
        })),
        answer: String.fromCharCode(65 + q.options.indexOf(q.answer))
      }));

      allQuestions = allQuestions.concat(processedQuestions);
      if (processedQuestions.length < pageSize) break;
      page++;
    }

    loadingInstance.close();

    // 创建10个卡片
    const newExams = [];
    for (let i = 0; i < 10; i++) {
      const startIndex = i * 10;
      const endIndex = startIndex + 10;
      const cardQuestions = allQuestions.slice(startIndex, endIndex);
      
      if (cardQuestions.length > 0) {
        newExams.push({
          id: `${subCategoryId}-card-${i + 1}`,
          categoryId: categoryId,
          title: `${category.name} - ${type === 'basic' ? '基础' : '进阶'}测试 ${i + 1}`,
          description: `${type === 'basic' ? '基础' : '进阶'}测试题库 第${i + 1}套`,
          duration: 60,
          questionCount: cardQuestions.length,
          isSubmitted: false,
          questions: cardQuestions
        });
      }
    }

    // 更新exams数组
    exams.value = newExams; // 直接替换

    activeCategory.value = subCategoryId;
    ElMessage.success('题目加载成功');
  } catch (error) {
    ElMessage.error('题目加载失败: ' + (error.response?.data?.message || error.message));
  }
};

// 开始考试逻辑
const startExam = (exam) => {
  if (exam.questions.length === 0) {
    ElMessage.warning('请先加载题目');
    return;
  }
  currentExam.value = {
    ...exam,
    questions: exam.questions.map(q => ({
      ...q,
      id: q.id // 直接使用原始ID
    }))
  };
  timeLeft.value = exam.duration * 60;
  initializeAnswers();
  startTimer();
};

function initializeAnswers() {
  answers.value = currentExam.value.questions.reduce((acc, question) => {
    acc[question.id] = {
      value: question.type === 'multiple' ? [] : '',
      questionId: question.id,
      timestamp: Date.now()
    }
    return acc
  }, {})
}

function getQuestionComponent(type) {
  return {
    single: 'el-radio-group',
    multiple: 'el-checkbox-group',
    input: 'el-input'
  }[type]
}

function validateAnswer(question) {
  return (_, value, callback) => {
    if (!value ||
      (question.type === 'multiple' && value.length === 0) ||
      (question.type !== 'multiple' && !value)) {
      callback(new Error('请完成此题作答'))
      return
    }
    callback()
  }
}

function startTimer() {
  timer.value = setInterval(() => {
    if (timeLeft.value <= 0) {
      submitExam()
      return
    }
    timeLeft.value--
  }, 1000)
}

function cancelExam() {
  clearInterval(timer.value)
  currentExam.value = null
}

// 新增工具方法
const getResultClass = (questionId) => {
  const result = resultDetails.value[questionId]
  if (!result) return 'invalid-question'
  return result.correct ? 'correct' : 'incorrect'
}

const getResultTag = (questionId) => {
  const result = resultDetails.value[questionId]
  if (!result) return '题目无效'
  return result.correct ? '正确' : '错误'
}

// 修改结果展示方法
const formatCorrectAnswer = (question) => {
  if (!question.options) return question.answer
  const correct = question.options.find(opt => opt.value === question.answer)
  return correct ? `${question.answer}. ${correct.label.split('. ')[1]}` : question.answer
}

const formatUserAnswer = (questionId) => {
  const answer = answers.value[questionId]?.value
  if (!answer) return ''

  const question = currentExam.value.questions.find(q => q.id === questionId)

  if (Array.isArray(answer)) {
    return answer.map(v => {
      const option = question.options.find(o => o.value === v)
      return option ? `${v}. ${option.label.split('. ')[1]}` : v
    }).join(', ')
  }

  const option = question.options.find(o => o.value === answer)
  return option ? `${answer}. ${option.label.split('. ')[1]}` : answer
}
// 题目关联方法
const linkQuestionsToCategories = () => {
  categories.value.forEach(category => {
    // 筛选本分类题目
    const categoryQuestions = questions.value
      .filter(q => q.category === category.name)
      .map(processQuestion)

    // 分配到主分类
    category.questions = categoryQuestions

    // 分配到子分类（示例：平均分配）
    category.subcategories.forEach((sub, index) => {
      const chunkSize = Math.ceil(categoryQuestions.length / category.subcategories.length)
      sub.questions = categoryQuestions.slice(
        index * chunkSize,
        (index + 1) * chunkSize
      )
    })
  })
}

// 随机课程图片方法
const getRandomCourseImage = () => {
  const images = [
    'https://images.unsplash.com/photo-1501504905252-473c47e087f8',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3',
    'https://images.unsplash.com/photo-1516321497487-e288fb19713f',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3'
  ]
  return images[Math.floor(Math.random() * images.length)]
}

onUnmounted(() => {
  clearInterval(timer.value)
})
</script>

<style lang="scss" scoped>
.course-container {
  display: flex;
  height: 100vh;
  background-color: transparent;
}

.course-sidebar {
  width: 280px;
  background: transparent;
  border-right: 1px solid rgba(228, 231, 237, 0.5);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  padding: 20px 0;

  .sidebar-header {
    padding: 0 20px 20px;
    border-bottom: 1px solid rgba(228, 231, 237, 0.5);
    
    h3 {
      margin: 0;
      color: #ffffff;
      font-size: 18px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      
      &::before {
        content: '';
        display: block;
        width: 4px;
        height: 16px;
        background: #8b5cf6;
        border-radius: 2px;
      }
    }
  }

  .category-menu {
    border-right: none;
    background: transparent;
    
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 44px;
      line-height: 44px;
      padding: 0 20px;
      color: #ffffff;
      font-size: 14px;
      background: transparent;
      
      .menu-title {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          font-size: 16px;
          color: rgba(255, 255, 255, 0.7);
        }
      }
      
      &:hover {
        background: rgba(0, 0, 0, 0.5);
        color: #ffffff;
        
        .el-icon {
          color: #ffffff;
        }
      }
    }
    
    :deep(.el-menu-item.is-active) {
      background: rgba(219, 166, 255, 0.6);
      color: #ffffff;
      font-weight: 500;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        height: 100%;
        width: 4px;
        background: #8b5cf6;
      }
      
      .el-icon {
        color: #ffffff;
      }
    }
    
    :deep(.el-sub-menu) {
      .el-menu-item {
        padding-left: 40px;
        height: 40px;
        line-height: 40px;
        font-size: 13px;
        color: #ffffff;
        background: rgba(0, 0, 0, 0.4) !important;
        
        &:hover {
          color: #ffffff;
          background: rgba(0, 0, 0, 0.5) !important;
        }
        
        &.is-active {
          color: #ffffff;
          background: rgba(0, 0, 0, 0.6) !important;
        }
        
        &::before {
          display: none;
        }
      }
      
      :deep(.el-sub-menu__title) {
        background: rgba(0, 0, 0, 0.3) !important;
        color: #ffffff;
        
        &:hover {
          color: #ffffff;
          background: rgba(0, 0, 0, 0.5) !important;
        }
      }
      
      :deep(.el-menu) {
        background: rgba(0, 0, 0, 0.3) !important;
        border-radius: 4px;
        margin: 4px 0;
        padding: 4px 0;
      }
      
      :deep(.el-menu--inline) {
        background: rgba(0, 0, 0, 0.3) !important;
      }
      
      :deep(.el-menu--popup) {
        background: rgba(0, 0, 0, 0.3) !important;
        backdrop-filter: blur(10px);
        border: none;
        border-radius: 8px;
        padding: 4px;
        
        .el-menu-item {
          color: #ffffff;
          border-radius: 4px;
          margin: 2px 0;
          
          &:hover {
            color: #ffffff;
            background: rgba(0, 0, 0, 0.5) !important;
          }
          
          &.is-active {
            background: rgba(0, 0, 0, 0.6) !important;
          }
        }
      }
    }
    
    // 添加展开状态的样式
    :deep(.el-sub-menu.is-opened) {
      > .el-sub-menu__title {
        color: #ffffff;
        background: rgba(0, 0, 0, 0.5) !important;
        
        .el-icon {
          color: #ffffff;
        }
      }
    }
  }
}

.course-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.course-content {
  .content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h2 {
      margin: 0;
      color: #ffffff;
      font-size: 24px;
      font-weight: 600;
    }
    
    .search-bar {
      width: 300px;
    }
  }
}

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.exam-card {
  background: transparent;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(228, 231, 237, 0.5);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  &.disabled-card {
    opacity: 0.7;
    cursor: not-allowed;
  }
  
  .card-cover {
    position: relative;
    height: 160px;
    
    .el-image {
      width: 100%;
      height: 100%;
    }
    
    .card-tag {
      position: absolute;
      top: 12px;
      right: 12px;
    }
  }
  
  .card-content {
    padding: 20px;
    background: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(10px);
    
    h3 {
      margin: 0 0 12px;
      font-size: 18px;
      color: #ffffff;
    }
    
    p {
      color: rgba(255, 255, 255, 0.8);
      margin-bottom: 16px;
      line-height: 1.5;
    }
    
    .card-meta {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
      color: rgba(255, 255, 255, 0.7);
      font-size: 14px;
      
      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
    
    .card-footer {
      text-align: center;
      
      .el-button {
        background: rgba(139, 92, 246, 0.8);
        border: none;
        color: #ffffff;
        padding: 8px 24px;
        font-size: 14px;
        border-radius: 6px;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(139, 92, 246, 1);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
    }
  }
}

.exam-interface {
  background: transparent;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 24px;
  
  .exam-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e4e7ed;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      h2 {
        margin: 0;
        font-size: 24px;
        color: #2c3e50;
      }
    }
    
    .timer {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      background: #f0e6ff;
      border-radius: 20px;
      color: #8b5cf6;
      font-weight: 500;
    }
  }
  
  .question-item {
    margin-bottom: 32px;
    padding: 24px;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    border: 1px solid rgba(228, 231, 237, 0.5);
    
    .question-header {
      margin-bottom: 20px;
      
      .question-number {
        display: inline-block;
        padding: 4px 12px;
        background: #e4e7ed;
        border-radius: 12px;
        font-size: 14px;
        color: #6b7280;
        margin-bottom: 12px;
      }
      
      h4 {
        margin: 0;
        font-size: 18px;
        color: #2c3e50;
      }
    }
    
    .option-content {
      display: inline-block;
      padding: 8px 16px;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 8px;
      margin-left: 8px;
      border: 1px solid rgba(228, 231, 237, 0.5);
    }
  }
  
  .exam-footer {
    margin-top: 40px;
    text-align: center;
  }
}

.exam-results {
  margin-top: 40px;
  
  .result-header {
    margin-bottom: 24px;
  }
  
  .result-item {
    margin-bottom: 24px;
    padding: 24px;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    border: 1px solid rgba(228, 231, 237, 0.5);
    
    .result-question {
      h4 {
        margin: 0 0 16px;
        font-size: 18px;
        color: #2c3e50;
        
        &.correct {
          color: #8b5cf6;
        }
        
        &.incorrect {
          color: #ef4444;
        }
      }
      
      .result-tag {
        float: right;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 14px;
        font-weight: 500;
        
        .correct & {
          background: #f0e6ff;
          color: #8b5cf6;
        }
        
        .incorrect & {
          background: #fee2e2;
          color: #ef4444;
        }
      }
    }
    
    .answer-section {
      margin: 16px 0;
      
      .correct-answer,
      .user-answer {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        
        .label {
          color: #6b7280;
          font-weight: 500;
        }
        
        .value {
          color: #2c3e50;
        }
        
        .score-delta {
          margin-left: auto;
          color: #8b5cf6;
          font-weight: 600;
        }
      }
    }
    
    .result-message {
      color: #6b7280;
      font-size: 14px;
      padding: 12px;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 8px;
      margin-top: 12px;
    }
  }
}

:deep(.el-radio),
:deep(.el-checkbox) {
  margin-right: 16px;
  margin-bottom: 12px;
  
  &.is-checked {
    .option-content {
      background: #f0e6ff;
      color: #8b5cf6;
    }
  }
}

:deep(.el-radio__input.is-checked .el-radio__inner),
:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #8b5cf6;
  border-color: #8b5cf6;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__error) {
  color: #ef4444;
}
</style>