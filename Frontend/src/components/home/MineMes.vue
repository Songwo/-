<template>
  <el-container class="personal-container">
    <!-- 头部 -->
    <el-header class="header">
      <div class="header-content">
        <el-button 
          class="back-btn" 
          type="text" 
          @click="$router.go(-1)"
        >
          <el-icon :size="24" class="icon">
            <ArrowLeft />
          </el-icon>
          返回
        </el-button>

        <div class="user-id">
          <el-icon><User /></el-icon>
          <span>用户ID: {{ userInfo.id }}</span>
        </div>
      </div>
    </el-header>

    <!-- 主体内容 -->
    <el-main>
      <el-row :gutter="30">
        <!-- 左侧信息栏 -->
        <el-col :xs="24" :sm="16" :md="18">
          <el-card class="info-card glass-effect">
            <template #header>
              <div class="card-header">
                <div class="avatar-container">
                  <el-avatar 
                    :size="80" 
                    :src="ToUrl.url+'/'+userInfo.avatar" 
                    @click="handleAvatarClick"
                    class="avatar-hover"
                  />
                  <div class="avatar-hint">点击更换头像</div>
                  <input
                    type="file"
                    ref="avatarInput"
                    hidden
                    accept="image/*"
                    @change="uploadAvatar"
                  />
                </div>
                <div class="user-info">
                  <div class="user-name">{{ userInfo.username }}</div>
                  <div class="user-email">{{ userInfo.email }}</div>
                </div>
              </div>
            </template>

            <div v-if="isEditing">
              <!-- 编辑状态：显示可编辑的表单 -->
              <el-form :model="editForm" ref="form" label-width="80px" class="edit-form">
                <el-form-item label="用户名">
                  <el-input v-model="editForm.username" placeholder="请输入用户名">
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="电子邮箱">
                  <el-input v-model="editForm.email" placeholder="请输入电子邮箱">
                    <template #prefix>
                      <el-icon><Message /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-form>

              <div class="form-actions">
                <el-button @click="saveChanges" type="primary" class="action-button">
                  <el-icon><Check /></el-icon>保存
                </el-button>
                <el-button @click="handleCancelEdit" class="action-button">
                  <el-icon><Close /></el-icon>取消
                </el-button>
              </div>
            </div>

            <!-- 非编辑状态：显示用户信息 -->
            <div v-else>
              <el-descriptions :column="1" border class="user-descriptions">
                <el-descriptions-item label="用户名">
                  <el-icon><User /></el-icon>
                  {{ userInfo.username }}
                </el-descriptions-item>
                <el-descriptions-item label="电子邮箱">
                  <el-icon><Message /></el-icon>
                  {{ userInfo.email }}
                  <el-tag size="small" effect="dark" type="success" class="verified-tag">已验证</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">
                  <el-icon><Calendar /></el-icon>
                  {{ formatTime(userInfo.createTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="最后修改时间">
                  <el-icon><Timer /></el-icon>
                  {{ formatTime(userInfo.updateTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="个人积分">
                  <el-icon><Star /></el-icon>
                  {{ userInfo.totalScore }}
                </el-descriptions-item>
              </el-descriptions>
              <el-button @click="handleEditProfile" type="primary" class="edit-button">
                <el-icon><Edit /></el-icon>编辑资料
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧安全设置 -->
        <el-col :xs="24" :sm="8" :md="6">
          <el-card class="security-card glass-effect">
            <template #header>
              <div class="security-title">
                <el-icon><Lock /></el-icon>
                安全设置
              </div>
            </template>

            <div class="security-items">
              <div class="security-item">
                <div class="security-item-header">
                  <el-icon><Message /></el-icon>
                  <span>邮箱验证</span>
                </div>
                <div class="security-item-content">
                  <el-tag v-if="userInfo.emailVerified" type="success" effect="dark">已完成</el-tag>
                  <el-tag v-else type="danger" effect="dark">未验证</el-tag>
                  <el-button 
                    v-if="!userInfo.emailVerified" 
                    @click="handleVerifyEmail" 
                    size="small" 
                    type="primary"
                  >
                    验证邮箱
                  </el-button>
                </div>
              </div>

              <div class="security-item">
                <div class="security-item-header">
                  <el-icon><Lock /></el-icon>
                  <span>登录密码</span>
                </div>
                <div class="security-item-content">
                  <el-button @click="handleChangePassword" type="primary" size="small">
                    修改密码
                  </el-button>
                </div>
              </div>

              <div class="security-item">
                <div class="security-item-header">
                  <el-icon><User /></el-icon>
                  <span>账户绑定</span>
                </div>
                <div class="security-item-content">
                  <el-button @click="handleBindAccount" type="primary" size="small">
                    管理绑定
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 添加新的统计卡片 -->
          <el-card class="stats-card glass-effect">
            <template #header>
              <div class="stats-title">
                <el-icon><DataLine /></el-icon>
                数据统计
              </div>
            </template>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-value">{{ userInfo.totalScore }}</div>
                <div class="stat-label">总积分</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">0</div>
                <div class="stat-label">收藏数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">0</div>
                <div class="stat-label">关注数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>

    <!-- 修改密码对话框 -->
    <el-dialog 
      title="修改密码" 
      v-model="passwordDialogVisible" 
      width="30%"
      class="custom-dialog"
    >
      <el-form label-width="100px" class="password-form">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input 
            v-model="passwordForm.currentPassword" 
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelPasswordChange">取消</el-button>
          <el-button type="primary" @click="savePasswordChanges">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 账户绑定管理对话框 -->
    <el-dialog 
      title="账户绑定管理" 
      v-model="accountBindingDialogVisible" 
      width="40%"
      class="custom-dialog"
    >
      <div v-if="bindedAccounts.length === 0" class="no-accounts">
        <el-empty description="您尚未绑定任何账户" />
      </div>
      <div v-else class="accounts-grid">
        <el-row :gutter="20">
          <el-col :span="12" v-for="(account, index) in bindedAccounts" :key="index">
            <el-card class="account-card">
              <div class="account-info">
                <el-icon :size="24"><Platform /></el-icon>
                <span>{{ account.name }}</span>
              </div>
              <el-button @click="unbindAccount(account)" type="danger" size="small">
                解绑
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="accountBindingDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import {  ref,onMounted } from 'vue'
import { Message, Lock, User, ArrowLeft, Calendar, Timer, Star, Edit, Close, DataLine, Platform } from '@element-plus/icons-vue'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElRow, ElCol, ElCard, ElTag } from 'element-plus';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router'
import axios from 'axios';
import ToUrl from '@/api/api';

const router=useRouter();

// 使用 vuex 获取用户信息
const store = useStore()

// 模拟用户数据
const userInfo = ref({
// 用户信息
  createTime: "",
  email: "",
  id: "",
  password: "",
  updateTime: "",
  username: "",
  avatar: '',
  totalScore:'',
  emailVerified: false
})

const passwordDialogVisible = ref(false);
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const accountBindingDialogVisible = ref(false);
const bindedAccounts = ref([]); // 假设从后端获取已绑定账户信息

// 使用 ref 来获取文件选择框
const avatarInput = ref(null);

// 触发文件选择框点击
const handleAvatarClick = () => {
  avatarInput.value.click();  // 点击文件选择框
};

// 上传头像
const uploadAvatar = async (e) => {
  const file = e.target.files[0];
  // console.log(111111111);
  if (!file) return;
  // console.log(22222222222);
  if(file.size > 500000) alert('太大');
  // 快速校验
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件');
    return;
  }
  // console.log(333333333);
  try {
    const formData = new FormData();
    formData.append('file', file);
    // console.log(444444444444);
    const res = await axios.post(ToUrl.url+`/user/avatar`, formData, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`,
        'Content-Type': 'multipart/form-data'
      }
    });
    // console.log(5555555555555);
    // console.log("aaaaaaaaaaaaaaaaa",res.data.data);
    // 更新头像
    userInfo.value.avatar = res.data.data;  // 更新图片路径
    await store.dispatch('setAvatar',res.data.data);
    // console.log("头像"+store.state.avatar);
    // console.log("路径如下:"+userInfo.value.avatar);
    ElMessage.success('头像更新成功');
  } catch (err) {
    ElMessage.error('上传失败');
  }
};
const handleChangePassword = () => {
  passwordDialogVisible.value = true;
};

const handleGoBack=()=>{
  ElMessage.info("返回");
  router.push("/root/home")
}

//加载用户信息
const userMinemes = async () => {
  if (store.state.user === "") {
    ElMessage.error("未登录噻");
    return;
  }
  try {
    const response = await axios.get(ToUrl.url+`/user/mes/${store.state.user}`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    });
    // console.log(response.data.data);
    userInfo.value = response.data.data;
    // console.log(userInfo.id);

    editForm.value = {
      id:userInfo.value.id,
      username: userInfo.value.username,
      email: userInfo.value.email,
    }; 

    ElMessage.success("加载成功");
  } catch (error) {
    ElMessage.error("加载失败");
  }
};

// 在组件挂载时加载用户信息
onMounted(() => {
  userMinemes();
});

// 修改密码
const savePasswordChanges = async () => {
  const { currentPassword, newPassword, confirmPassword } = passwordForm.value;
  
  // 验证密码为空
  if (!currentPassword || !newPassword || !confirmPassword) {
    ElMessage.error("密码不能为空");
    return;
  }
  // 验证新密码与确认密码一致性
  if (newPassword !== confirmPassword) {
    ElMessage.error("新密码与确认密码不一致");
    return;
  }
  try {
    await axios.put(ToUrl.url+"/user/changepwd", {
      id: store.state.id,
      currentPassword,
      newPassword
    }, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    });
    ElMessage.success("密码修改成功");
    passwordDialogVisible.value = false;
    // 清空密码表单
    passwordForm.value.currentPassword = '';
    passwordForm.value.newPassword = '';
    passwordForm.value.confirmPassword = '';
  } catch (error) {
    ElMessage.error("密码修改失败");
  }
};

// 取消修改密码
const handleCancelPasswordChange = () => {
  passwordDialogVisible.value = false;
  // 清空密码表单
  passwordForm.value.currentPassword = '';
  passwordForm.value.newPassword = '';
  passwordForm.value.confirmPassword = '';
};

// 编辑表单的数据模型
const editForm = ref({
  id:userInfo.id,
  username: userInfo.username,
  email: userInfo.email,
})

// 控制编辑模式
const isEditing = ref(false)

// 格式化时间
const formatTime = (timeString) => {
  return new Date(timeString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 进入编辑模式
const handleEditProfile = () => {
  isEditing.value = true
}

// 取消编辑模式
const handleCancelEdit = () => {
  userMinemes();
  isEditing.value = false
}
// 账户绑定管理
const handleBindAccount = () => {
  accountBindingDialogVisible.value = true;
};

const unbindAccount = (account) => {
  // 解绑账户的逻辑
  ElMessage.success(`已解绑账户: ${account.name}`);
};

// 密码确认验证
const confirmPasswordValidator = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('确认密码与新密码不一致'));
  } else {
    callback();
  }
};

// 修改保存信息的逻辑
const saveChanges = async () => {
  if (editForm.value.username === "" || editForm.value.email === "" ||
    (editForm.value.username === userInfo.value.username && editForm.value.email === userInfo.value.email)) {
    ElMessage.error("用户名或邮箱不能为空，或者未更改任何数据");
  } else {
    try {
      const response = await axios.put(ToUrl.url+"/user/update", {
        id: editForm.value.id,
        username: editForm.value.username,
        email: editForm.value.email
      }, {
        headers: {
          'Authorization': `Bearer ${store.state.token}`
        }
      });

      // 更新用户信息
      store.commit('setUser', editForm.value.username);
      userMinemes();  // 重新加载用户信息

      ElMessage.success('信息保存成功');
      isEditing.value = false;  // 退出编辑状态
    } catch (error) {
      ElMessage.error("信息保存失败");
    }
  }
};

// 验证邮箱
const handleVerifyEmail = () => {
  ElMessage.success('验证邮箱功能')
}

</script>

<style scoped>
.personal-container {
  min-height: 100vh;
  background: transparent;
  padding: 20px;
}

.header {
  background: transparent;
  padding: 0;
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #fff;
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateX(-5px);
  color: rgba(255, 255, 255, 0.8);
}

.user-id {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #fff;
}

.glass-effect {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.info-card {
  margin-bottom: 20px;
  border-radius: 15px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.avatar-container {
  position: relative;
  text-align: center;
}

.avatar-hover {
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-hover:hover {
  transform: scale(1.05);
}

.avatar-hint {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #666;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-container:hover .avatar-hint {
  opacity: 1;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.user-email {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.user-descriptions {
  margin-top: 20px;
}

/* 添加表格样式 */
:deep(.el-descriptions__body) {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  border-radius: 10px;
}

:deep(.el-descriptions__cell) {
  background: transparent !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: #fff;
}

:deep(.el-descriptions__label) {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: #fff;
}

.verified-tag {
  margin-left: 10px;
}

.edit-button {
  margin-top: 20px;
  width: 100%;
}

.security-card {
  margin-bottom: 20px;
  border-radius: 15px;
}

.security-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  padding: 15px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.5);
}

.security-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-weight: 500;
}

.security-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-card {
  border-radius: 15px;
}

.stats-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.stats-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  padding: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.custom-dialog {
  border-radius: 15px;
}

.password-form {
  padding: 20px;
}

.dialog-footer {
  padding: 20px;
  text-align: right;
}

.no-accounts {
  padding: 40px 0;
}

.accounts-grid {
  padding: 20px 0;
}

.account-card {
  margin-bottom: 15px;
  border-radius: 10px;
}

.account-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 5px;
}

@media (max-width: 768px) {
  .personal-container {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 修改按钮文字颜色 */
:deep(.el-button) {
  color: #fff;
}

:deep(.el-button--primary) {
  color: #fff;
}

:deep(.el-button--danger) {
  color: #fff;
}

:deep(.el-button--text) {
  color: #fff;
}

:deep(.el-button--text:hover) {
  color: rgba(255, 255, 255, 0.8);
}

/* 表格样式 */
:deep(.el-descriptions__body) {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  border-radius: 10px;
}

:deep(.el-descriptions__cell) {
  background: transparent !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: #fff;
}

:deep(.el-descriptions__label) {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: #fff;
}

/* 输入框文字颜色 */
:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.8);
}

/* 对话框标题和内容 */
:deep(.el-dialog__title) {
  color: #fff;
}

:deep(.el-dialog__body) {
  color: #fff;
}

/* 编辑表单样式 */
.edit-form {
  padding: 20px;
}

:deep(.el-form-item__label) {
  color: #fff !important;
  font-weight: 500;
}

:deep(.el-form-item__content) {
  margin-left: 120px !important;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.8);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding: 0 20px;
}

/* 取消按钮样式 */
:deep(.el-button--default) {
  background-color: #ffffff !important;
  border-color: #dcdfe6 !important;
  color: #333333 !important;
}

:deep(.el-button--default:hover) {
  background-color: #f5f7fa !important;
  border-color: #dcdfe6 !important;
  color: #333333 !important;
}

:deep(.el-button--default:active) {
  background-color: #f5f7fa !important;
  border-color: #dcdfe6 !important;
  color: #333333 !important;
}
</style>