<template>
  <div class="learning-guide">
    <el-dialog
      v-model="showNewUserGuide"
      title="欢迎来到学习之旅"
      width="60%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="guide-content">
        <p class="guide-text">为了给您提供更好的学习体验，请先完成以下问卷：</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
          <el-form-item label="您的技术等级" prop="skillLevel">
            <el-select v-model="form.skillLevel" placeholder="请选择您的技术等级" style="width: 100%">
              <el-option label="初学者" value="beginner"></el-option>
              <el-option label="中级" value="intermediate"></el-option>
              <el-option label="高级" value="advanced"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="您感兴趣的领域" prop="interests">
            <el-select v-model="form.interests" multiple collapse-tags placeholder="请选择您感兴趣的领域" style="width: 100%">
              <el-option v-for="item in interestOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="您的学习目标" prop="learningGoal">
            <el-input type="textarea" v-model="form.learningGoal" :rows="3" placeholder="请输入您的学习目标"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="submitProfile">开始学习</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import request from '@/axios/index';

// 访问 Vuex Store
const store = useStore();

// 响应式状态
const showNewUserGuide = ref(false);
const form = reactive({
  skillLevel: '',
  interests: [],
  learningGoal: ''
});
const recommendationTimer = ref(null);

// 模板引用
const formRef = ref(null);

// 静态数据
const rules = {
  skillLevel: [
    { required: true, message: '请选择您的技术等级', trigger: 'change' }
  ],
  interests: [
    { required: true, message: '请选择您感兴趣的领域', trigger: 'change' }
  ],
  learningGoal: [
    { required: true, message: '请输入您的学习目标', trigger: 'blur' }
  ]
};

const interestOptions = [
  { value: 'web', label: 'Web安全' },
  { value: 'mobile', label: '移动安全' },
  { value: 'system', label: '系统安全' },
  { value: 'crypto', label: '密码学' },
  { value: 'reverse', label: '逆向工程' }
];

// 计算属性
const isLoggedIn = computed(() => {
  const stu = store.state.token;
  // console.log("stu:111111111111"+stu);
  return !!stu;
});

const getRecommendations = async () => {
  try {
    const response = await request.get(`/api/recommendations/user/${store.state.id}`);
    const recommendations = response.data;
    if (recommendations && recommendations.length > 0) {
      // 随机选择一个推荐内容
      const randomRecommendation = recommendations[Math.floor(Math.random() * recommendations.length)];
      ElMessage({
        message: `推荐学习：${randomRecommendation.title}`,
        type: 'info',
        duration: 6000,
        showClose: true
      });
    }
    return recommendations;
  } catch (error) {
    console.error('获取推荐失败:', error);
    ElMessage.error('获取推荐内容失败');
    return [];
  }
};

const recordUserBehavior = async (behavior) => {
  try {
    await request.post('/api/recommendations/behavior', {
      userId: store.state.id,
      contentType: behavior.contentType, // VIDEO, QUIZ, VULNERABILITY
      contentId: behavior.contentId,
      interactionType: behavior.interactionType, // WATCH, ANSWER, SEARCH
      interactionTime: new Date(),
      duration: behavior.duration,
      score: behavior.score,
      category: behavior.category
    });
  } catch (error) {
    console.error('记录用户行为失败:', error);
  }
};

const showRecommendation = async () => {
  try {
    const recommendations = await getRecommendations();
    if (recommendations && recommendations.length > 0) {
      const randomRecommendation = recommendations[Math.floor(Math.random() * recommendations.length)];
      ElMessage({
        message: `推荐学习：${randomRecommendation.title}`,
        type: 'info',
        duration: 6000,
        showClose: true
      });
    }
  } catch (error) {
    console.error('显示推荐失败:', error);
  }
};

const startRecommendationTimer = () => {
  if (recommendationTimer.value) {
    clearInterval(recommendationTimer.value);
  }

  // 每30分钟显示一次推荐
  recommendationTimer.value = setInterval(() => {
    showRecommendation();
  }, 60 * 1000);

  // 立即显示一次推荐
  showRecommendation();
};

const checkUserProfile = async () => {
  try {
    // 从后端获取用户资料
    const response = await request.get(`/api/user/profile/${store.state.id}`);
    const userProfile = response.data;
    
    if (!userProfile) {
      showNewUserGuide.value = true;
    } else {
      // 如果已有资料，记录一次查看行为
      await recordUserBehavior({
        contentType: 'PROFILE',
        contentId: 'profile_view',
        interactionType: 'VIEW',
        duration: 0,
        score: null,
        category: userProfile.interests[0]
      });
      startRecommendationTimer();
    }
  } catch (error) {
    console.error('获取用户资料失败:', error);
    ElMessage.error('获取用户资料失败');
  }
};

const submitProfile = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 转换技能等级
        let skillLevel;
        switch (form.skillLevel) {
          case 'beginner':
            skillLevel = 1;
            break;
          case 'intermediate':
            skillLevel = 2;
            break;
          case 'advanced':
            skillLevel = 3;
            break;
          default:
            skillLevel = 1;
        }

        // 提交用户资料到后端
        await request.post('/api/user/profile', {
          userId: store.state.id,
          skillLevel: skillLevel,  // 直接使用转换后的数字
          interests: form.interests,
          learningPreferences: {
            goal: form.learningGoal
          }
        });
        
        // 记录初始学习行为
        await recordUserBehavior({
          contentType: 'PROFILE',
          contentId: 'initial_profile',
          interactionType: 'SUBMIT',
          duration: 0,
          score: 1.0,
          category: form.interests[0]
        });

        showNewUserGuide.value = false;
        startRecommendationTimer();
        ElMessage.success('个人信息已保存');
      } catch (error) {
        console.error('保存用户资料失败:', error);
        ElMessage.error('保存失败，请重试');
      }
    }
  });
};

// 监听器
watch(isLoggedIn, (newVal) => {
  if (newVal) {
    checkUserProfile();
  }
});

// 生命周期钩子
onMounted(() => {
  if (isLoggedIn.value) {
    checkUserProfile();
  }
});

onBeforeUnmount(() => {
  if (recommendationTimer.value) {
    clearInterval(recommendationTimer.value);
  }
});
</script>

<style scoped>
.learning-guide {
  position: fixed;
  z-index: 1000;
}

.guide-content {
  padding: 20px;
}

.guide-text {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.dialog-footer {
  text-align: right;
}
</style>