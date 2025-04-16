<template>
  <el-container class="main-container">
    <!-- 左侧分类 -->
    <el-aside width="240px" class="category-side">
      <el-menu :default-active="activeCategory" @select="handleCategorySelect" class="category-menu" accordion>
        <el-sub-menu v-for="category in categories" :key="category.id" :index="category.id.toString()">
          <template #title>{{ category.name }}</template>
          <el-menu-item v-for="subcategory in category.subcategories" :key="subcategory.id"
            :index="subcategory.id.toString()">
            {{ subcategory.name }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 右侧内容 -->
    <el-main class="exam-main">
      <!-- 试卷列表 -->
      <div v-if="!currentExam" class="exam-list">
        <el-card v-for="exam in filteredExams" :key="exam.id" class="exam-card"
          :class="{ 'disabled-card': exam.isSubmitted }" @click="startExam(exam)">
          <div class="exam-header">
            <h3>{{ exam.title }}</h3>
            <el-tag v-if="exam.isSubmitted" type="success">已完成</el-tag>
          </div>
          <p>{{ exam.description }}</p>
          <div class="exam-meta">
            <span>时长: {{ exam.duration }}分钟</span>
            <span>题量: {{ exam.questionCount }}题</span>
          </div>
        </el-card>
      </div>

      <!-- 考试界面 -->
      <div v-else class="exam-interface">
        <div class="exam-header">
          <h2>{{ currentExam.title }}</h2>
          <div class="timer">
            <el-icon>
              <clock />
            </el-icon>
            剩余时间: {{ formattedTime }}
          </div>
        </div>

        <el-form :model="answers">
          <div v-for="question in currentExam.questions" :key="question.id" class="question-item">
            <h4>{{ question.title }}</h4>
            <el-form-item :prop="`answers[${question.id}].value`" :rules="[{ validator: validateAnswer(question) }]">
              <component :is="getQuestionComponent(question.type)" v-model="answers[question.id].value">
                <!-- 单选 -->
                <!-- 修改选项显示方式 -->
                <template v-if="question.type === 'single'">
                  <el-radio v-for="option in question.options" :key="option.value" :label="option.value" border>
                    {{ option.label }}
                  </el-radio>
                </template>
                <!-- 多选 -->
                <template v-if="question.type === 'multiple'">
                  <el-checkbox v-for="option in question.options" :key="option.value" :label="option.value" border>
                    {{ option.label }}
                  </el-checkbox>
                </template>
              </component>
            </el-form-item>
          </div>
        </el-form>

        <div class="action-buttons">
          <el-button type="primary" @click="submitExam">提交试卷</el-button>
          <el-button @click="cancelExam">取消考试</el-button>
        </div>
        <!-- 在action-buttons下方添加结果展示 -->
        <div v-if="currentExam?.isSubmitted" class="exam-results">
          <el-alert :title="`本次得分: ${scoreInfo.scoreDelta}`" type="success" show-icon />

          <div class="result-details">
            <div v-for="question in currentExam.questions" :key="question.id" class="result-item">
              <h4 :class="getResultClass(question.id)">
                {{ question.title }}
                <span class="result-tag">
                  {{ getResultTag(question.id) }}
                </span>
              </h4>

              <!-- 显示正确答案 -->
              <div v-if="resultDetails[question.id]" class="correct-answer">
                <span>正确答案: {{ formatCorrectAnswer(question) }}</span>
                <span class="score-delta">(得分变化: +{{ resultDetails[question.id].scoreDelta }})</span>
              </div>

              <!-- 显示用户答案 -->
              <div class="user-answer">
                您的答案: {{ formatUserAnswer(question.id) || "未作答" }}
              </div>

              <div class="result-message">
                {{ resultDetails[question.id]?.message }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onUnmounted, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
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

// 新增分类数据处理方法
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

// 新增答案校验逻辑
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
    exams.value = newExams; // 直接替换而不是过滤和添加

    activeCategory.value = subCategoryId;
    ElMessage.success('题目加载成功');
  } catch (error) {
    ElMessage.error('题目加载失败: ' + (error.response?.data?.message || error.message));
  }
};

// 修改后的开始考试逻辑
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
// 新增题目关联方法
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

onUnmounted(() => {
  clearInterval(timer.value)
})
</script>

<style lang="scss" scoped>
.main-container {
  height: 100vh;
  color: #2c3e50;

  .category-side {
    border-right: 1px solid #e4e7ed;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);

    .category-menu {
      border-right: none;
      
      :deep(.el-menu-item) {
        &.is-active {
          background-color: #f0e6ff;
          color: #8b5cf6;
        }
        
        &:hover {
          background-color: #f8f5ff;
        }
      }
    }
  }

  .exam-main {
    padding: 24px;

    .exam-list {
      display: grid;
      gap: 24px;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    }

    .exam-interface {
      .exam-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 32px;
        padding: 20px;
        background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
        border-radius: 12px;
        color: white;
        box-shadow: 0 4px 12px rgba(139, 92, 246, 0.2);

        h2 {
          margin: 0;
          font-size: 24px;
          font-weight: 600;
        }

        .timer {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 18px;
          font-weight: 500;
          background: rgba(255, 255, 255, 0.2);
          padding: 8px 16px;
          border-radius: 20px;
          backdrop-filter: blur(4px);
        }
      }

      .question-item {
        margin-bottom: 32px;
        padding: 24px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
        transition: transform 0.2s, box-shadow 0.2s;
        border: 1px solid #e4e7ed;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
        }

        h4 {
          margin: 0 0 20px 0;
          font-size: 18px;
          color: #2c3e50;
          font-weight: 600;
        }
      }
    }

    .action-buttons {
      margin-top: 40px;
      text-align: center;
      display: flex;
      gap: 16px;
      justify-content: center;

      .el-button {
        padding: 12px 32px;
        font-size: 16px;
        border-radius: 8px;
        transition: all 0.3s ease;

        &--primary {
          background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
          border: none;
          box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(139, 92, 246, 0.4);
          }
        }
      }
    }
  }
}

.exam-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  border: 1px solid #e4e7ed;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(139, 92, 246, 0.15);
  }

  &.disabled-card {
    opacity: 0.7;
    cursor: not-allowed;
    background: #f8f9fa;
  }

  .exam-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
    color: white;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }

    .el-tag {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      backdrop-filter: blur(4px);
    }
  }

  .el-card__body {
    padding: 20px;
  }

  p {
    color: #4b5563;
    margin: 12px 0;
    line-height: 1.6;
  }

  .exam-meta {
    display: flex;
    justify-content: space-between;
    margin-top: 16px;
    color: #6b7280;
    font-size: 0.95em;
  }
}

.exam-results {
  margin-top: 40px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid #e4e7ed;

  .el-alert {
    margin-bottom: 24px;
    border-radius: 8px;
    background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
    border: none;
    color: white;
  }
}

.result-item {
  margin: 20px 0;
  padding: 20px;
  border-radius: 12px;
  background: #f8f9fa;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;

  &:hover {
    transform: translateX(4px);
    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.1);
  }

  h4 {
    margin-bottom: 16px;
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;

    &.correct {
      color: #8b5cf6;
      border-left: 4px solid #8b5cf6;
      padding-left: 12px;
    }

    &.incorrect {
      color: #ef4444;
      border-left: 4px solid #ef4444;
      padding-left: 12px;
    }

    &.invalid-question {
      color: #6b7280;
      border-left: 4px solid #6b7280;
      padding-left: 12px;
    }
  }
}

.result-tag {
  float: right;
  font-size: 0.85em;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;

  .correct & {
    background: #f0e6ff;
    color: #8b5cf6;
  }

  .incorrect & {
    background: #fee2e2;
    color: #ef4444;
  }

  .invalid-question & {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.correct-answer {
  color: #8b5cf6;
  margin: 8px 0;
  font-weight: 500;

  .score-delta {
    margin-left: 12px;
    color: #6d28d9;
    font-weight: 600;
  }
}

.user-answer {
  color: #4b5563;
  margin: 8px 0;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.result-message {
  color: #6b7280;
  font-size: 0.95em;
  margin-top: 8px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

:deep(.el-radio),
:deep(.el-checkbox) {
  margin-right: 24px;
  margin-bottom: 16px;
  
  .el-radio__label,
  .el-checkbox__label {
    color: #4b5563;
  }
  
  &.is-checked {
    .el-radio__label,
    .el-checkbox__label {
      color: #8b5cf6;
    }
  }
}

:deep(.el-radio__input.is-checked .el-radio__inner),
:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #8b5cf6;
  border-color: #8b5cf6;
}

:deep(.el-radio__input.is-checked + .el-radio__label),
:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #8b5cf6;
}

:deep(.el-radio__inner:hover),
:deep(.el-checkbox__inner:hover) {
  border-color: #8b5cf6;
}

:deep(.el-radio__input.is-focus .el-radio__inner),
:deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: #8b5cf6;
  box-shadow: 0 0 0 2px rgba(139, 92, 246, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-form-item__label) {
  color: #4b5563;
}

:deep(.el-form-item__error) {
  color: #ef4444;
}
</style>