<template>
  <div id="app">
    <div class="auth-container">
      <transition name="fade-transform" mode="out-in" @after-enter="handleTransitionEnd">
        <div class="auth-card" :key="isLogin ? 'login' : 'register'">
          <div class="platform-title">
            <h1>白帽工坊</h1>
            <div class="cyber-line"></div>
          </div>

          <!-- 登录表单 -->
          <el-form v-if="isLogin" :model="loginForm" :rules="loginRules" ref="loginFormRef"
            @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" clearable />
            </el-form-item>

            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                show-password />
            </el-form-item>

            <el-button type="primary" native-type="submit" class="auth-btn" :loading="loading">立即登录</el-button>
          </el-form>

          <!-- 注册表单 -->
          <el-form v-else :model="registerForm" :rules="registerRules" ref="registerFormRef"
            @submit.prevent="handleRegister">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" prefix-icon="User" clearable />
            </el-form-item>

            <el-form-item prop="email">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" prefix-icon="Message" clearable />
            </el-form-item>

            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                show-password />
            </el-form-item>

            <!-- 添加验证码 -->
            <el-form-item prop="captcha">
              <div class="captcha-container">
                <canvas ref="captchaCanvas" :width="captchaConfig.width" :height="captchaConfig.height"
                  @click="refreshCaptcha"></canvas>
                <el-input v-model="registerForm.captcha" placeholder="请输入验证码" clearable class="captcha-input" />
              </div>
            </el-form-item>

            <el-button type="primary" native-type="submit" class="auth-btn" :loading="loading">立即注册</el-button>
          </el-form>

          <div class="switch-text">
            {{ isLogin ? '没有账号？' : '已有账号？' }}
            <el-link type="primary" @click="isLogin = !isLogin">{{ isLogin ? '立即注册' : '立即登录' }}</el-link>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useStore } from 'vuex';
import axios from 'axios';
import ToUrl from '@/api/api';

const router = useRouter();
const store = useStore();
// 表单状态
const isLogin = ref(true);  // 初始是登录界面
const loading = ref(false);

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
});

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  captcha: '' // 加入验证码字段
});

// 验证规则
const loginRules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
};

const registerRules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, max: 16, message: '密码长度在6到16个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '验证码不能为空', trigger: 'blur' }
  ]
};

// 引用 el-form的表单
const loginFormRef = ref(null);
const registerFormRef = ref(null);

// 用于存储验证码答案
const captchaAnswer = ref('');

// 脚本部分
const captchaCanvas = ref(null)
const captchaConfig = reactive({
  width: 120,
  height: 40,
  fontSize: 24
})

// 验证码生成方法
const generateCaptcha = () => {
  const ctx = captchaCanvas.value?.getContext('2d')
  if (!ctx) return

  // 清空画布
  ctx.clearRect(0, 0, captchaConfig.width, captchaConfig.height)

  // 生成随机字符
  const chars = 'ABCDEFGHJKMNPQRSTWXYZ23456789'
  let captcha = ''
  for (let i = 0; i < 4; i++) { // 改为4位验证码
    captcha += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaAnswer.value = captcha

  // 绘制背景干扰线
  ctx.strokeStyle = '#eee'
  for (let i = 0; i < 6; i++) {
    ctx.beginPath()
    ctx.moveTo(
      Math.random() * captchaConfig.width,
      Math.random() * captchaConfig.height
    )
    ctx.lineTo(
      Math.random() * captchaConfig.width,
      Math.random() * captchaConfig.height
    )
    ctx.stroke()
  }

  // 绘制验证码文本
  ctx.font = `${captchaConfig.fontSize}px Comic Sans MS`
  ctx.fillStyle = '#2a2a4a'
  ctx.textBaseline = 'middle'
  for (let i = 0; i < captcha.length; i++) {
    ctx.save()
    ctx.translate(
      30 * i + 10,
      captchaConfig.height / 2 + (Math.random() - 0.5) * 8
    )
    ctx.rotate((Math.random() - 0.5) * 0.4)
    ctx.fillText(captcha[i], 0, 0)
    ctx.restore()
  }
}
// 添加过渡完成处理方法
const handleTransitionEnd = () => {
  if (!isLogin.value) {
    generateCaptcha()
  }
}

onMounted(() => {
  nextTick(() => {
    generateCaptcha(); // 确保在挂载后生成验证码
  });
});

// 修改watch监听方式
watch(isLogin, (newVal) => {
  if (!newVal) {
    // 增加双重保险
    setTimeout(() => {
      nextTick(() => {
        generateCaptcha()
      })
    }, 300) // 匹配过渡动画时长
  }
})

// 添加验证码刷新方法
const refreshCaptcha = () => {
  generateCaptcha()
}

// 登录逻辑判断
const handleLogin = async () => {
  loading.value = true;

  try {
    // 校验登录表单
    await loginFormRef.value?.validate();

    // 设置 API 和跳转路径
    const isAdmin = loginForm.username === 'Ajun';
    const apiPath = isAdmin ? "/admin/login" : "/user/login";
    const redirectPath = isAdmin ? "/backMange/comment" : "/root/home";

    // 发起登录请求
    const response = await axios.post(ToUrl.url + apiPath, {
      username: loginForm.username,
      password: loginForm.password
    });

/*     // console.log(response.data.data); // 调试查看返回数据 */

    if (response.data.code === 200) {
      ElMessage.success('登录成功');

      // 获取 token 并存储
      const token = response.data.data;
      await store.dispatch('setToken', token);
      localStorage.setItem('token', token);
      await store.dispatch('setUser', loginForm.username);
      await store.dispatch('setId', "67d7d7a15909eaf63c7ee5db");
      await store.dispatch('setAvatar', "avatar/6a103249-10fc-422d-883a-c5cf4bca4364_信息.png");
      /* // console.log("用户名:", loginForm.username); */
      // 如果不是管理员，则获取用户 ID 和头像
      if (!isAdmin) {
        try {
          const responseId = await axios.get(ToUrl.url + `/user/mes/${loginForm.username}`, {
            headers: { 'Authorization': `Bearer ${token}` }
          });

          if (responseId.data.code === 200) {
            const { id, avatar } = responseId.data.data;
            await store.dispatch('setId', id);
            await store.dispatch('setAvatar', avatar);
            // console.log('Token:', token, "id:", id, "avatar:", avatar);
          } else {
            console.warn('获取用户 ID 失败:', responseId.data.msg);
          }
        } catch (err) {
          console.error("获取用户 ID 失败:", err);
        }
      }

      // 跳转到对应页面
      router.push(redirectPath);
    } else {
      ElMessage.error(response.data.msg);
    }
  } catch (error) {
    console.error("登录请求错误:", error);
    ElMessage.error('登录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 处理注册
const handleRegister = async () => {
  try {
    loading.value = true;

    // 校验注册表单
    await registerFormRef.value?.validate();  // 校验表单

    // 判断验证码是否正确
    if (registerForm.captcha !== captchaAnswer.value) {
      ElMessage.error('验证码错误');
      refreshCaptcha();
      return;
    }

    // 发起注册请求
    const response = await axios.post(ToUrl.url + '/user/register', {
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    });

    if (response.data.code === 200) {
      ElMessage.success('注册成功');
      isLogin.value = true;  // 注册成功后切换回登录界面
      generateCaptcha();  // 注册成功后重新生成验证码
    } else {
      refreshCaptcha();
      ElMessage.error(response.data.msg);  // 显示错误消息
    }
  } catch (error) {
    ElMessage.error('注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
.auth-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
    animation: rotate 20s linear infinite;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.auth-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
  transform: translateY(0);
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-5px);
  }
}

.platform-title {
  text-align: center;
  margin-bottom: 40px;

  h1 {
    color: #2d3748;
    font-size: 28px;
    font-weight: 600;
    margin-bottom: 15px;
    letter-spacing: 1px;
  }

  .cyber-line {
    height: 3px;
    background: linear-gradient(90deg, transparent, #667eea, transparent);
    margin: 0 auto;
    width: 60%;
    border-radius: 3px;
  }
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 10px;
  padding: 12px;
  transition: all 0.3s ease;

  &:hover, &:focus-within {
    border-color: #667eea;
    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
  }
}

:deep(.el-input__inner) {
  height: 40px;
  font-size: 15px;
}

.auth-btn {
  width: 100%;
  margin-top: 25px;
  height: 45px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.switch-text {
  text-align: center;
  margin-top: 25px;
  color: #4a5568;
  font-size: 14px;

  .el-link {
    font-weight: 500;
    margin-left: 5px;
    vertical-align: baseline;
  }
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 12px;

  canvas {
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 10px;
    cursor: pointer;
    background: rgba(255, 255, 255, 0.8);
    transition: all 0.3s ease;

    &:hover {
      border-color: #667eea;
      box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
    }
  }

  .captcha-input {
    flex: 1;
  }
}

/* 过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-enter-from,
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>