<template>
  <el-container class="personal-container">
    <!-- 头部 -->
    <el-header class="header">
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

      <div class="user-id">用户ID: {{ userInfo.id }}</div>
    </el-header>

    <!-- 主体内容 -->
    <el-main>
      <el-row :gutter="20">
        <!-- 左侧信息栏 -->
        <el-col :xs="24" :sm="16" :md="18">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <el-avatar :size="60" :src="ToUrl.url+'/'+userInfo.avatar" @click="handleAvatarClick" />
                <input
                  type="file"
                  ref="avatarInput"
                  hidden
                  accept="image/*"
                  @change="uploadAvatar"
                />
                <div class="user-name">{{ userInfo.username }}</div>
              </div>
            </template>

            <div v-if="isEditing">
              <!-- 编辑状态：显示可编辑的表单 -->
              <el-form :model="editForm" ref="form" label-width="80px" class="edit-form">
                <el-form-item label="用户名">
                  <el-input v-model="editForm.username" placeholder="请输入用户名" />
                </el-form-item>

                <el-form-item label="电子邮箱">
                  <el-input v-model="editForm.email" placeholder="请输入电子邮箱" />
                </el-form-item>
              </el-form>

              <div class="form-actions">
                <el-button @click="saveChanges" type="primary" style="margin-top: 0px;">保存</el-button>
                <el-button @click="handleCancelEdit" style="margin-left: 10px;">取消</el-button>
              </div>
            </div>

            <!-- 非编辑状态：显示用户信息 -->
            <div v-else>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
                <el-descriptions-item label="电子邮箱">
                  {{ userInfo.email }}
                  <el-tag size="small" effect="dark" type="success">已验证</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">{{ formatTime(userInfo.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="最后修改时间">{{ formatTime(userInfo.updateTime) }}</el-descriptions-item>
                <el-descriptions-item label="个人积分">{{ userInfo.totalScore }}</el-descriptions-item>
              </el-descriptions>
              <el-button @click="handleEditProfile" type="primary" style="margin-top: 20px;">编辑资料</el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧安全设置 -->
        <el-col :xs="24" :sm="8" :md="6">
          <el-card class="security-card">
            <template #header>
              <div class="security-title">安全设置</div>
            </template>

            <div class="security-item">
              <el-icon><Message /></el-icon>
              <span>邮箱验证</span>
              <el-tag v-if="userInfo.emailVerified" type="success" effect="dark">已完成</el-tag>
              <el-tag v-else type="danger" effect="dark">未验证</el-tag>
              <el-button v-if="!userInfo.emailVerified" @click="handleVerifyEmail" size="small" type="primary" style="margin-left: 10px;">
                验证邮箱
              </el-button>
            </div>

            <div class="security-item">
              <el-icon><Lock /></el-icon>
              <span>登录密码</span>
              <el-button @click="handleChangePassword" type="primary" size="small">修改</el-button>
            </div>

            <div class="security-item">
              <el-icon><User /></el-icon>
              <span>账户绑定</span>
              <el-button @click="handleBindAccount" type="primary" size="small">管理</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" v-model="passwordDialogVisible" width="30%">
  <el-form label-width="100px">
    <el-form-item label="当前密码" prop="currentPassword" :rules="[{ required: true, message: '请输入当前密码', trigger: 'blur' }]">
      <el-input 
        v-model="passwordForm.currentPassword" 
        type="password"
        placeholder="请输入当前密码" 
      />
    </el-form-item>
    <el-form-item label="新密码" prop="newPassword" :rules="[{ required: true, message: '请输入新密码', trigger: 'blur' }]">
      <el-input 
        v-model="passwordForm.newPassword" 
        type="password" 
        placeholder="请输入新密码"
      />
    </el-form-item>
    <el-form-item label="确认新密码" prop="confirmPassword" :rules="[{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: confirmPasswordValidator, trigger: 'blur' }]">
      <el-input v-model="passwordForm.confirmPassword" 
      type="password" 
      placeholder="请确认新密码" />
    </el-form-item>
  </el-form>
  <div slot="footer" class="dialog-footer">
    <el-button @click="handleCancelPasswordChange">取消</el-button>
    <el-button type="primary" @click="savePasswordChanges">确定</el-button>
  </div>
</el-dialog>

    <!-- 账户绑定管理对话框 -->
    <el-dialog title="账户绑定管理" v-model="accountBindingDialogVisible" width="40%">
      <div v-if="bindedAccounts.length === 0">
        <p>您尚未绑定任何账户。</p>
      </div>
      <div v-else>
        <el-row :gutter="20">
          <el-col :span="12" v-for="(account, index) in bindedAccounts" :key="index">
            <el-card>
              <div>{{ account.name }}</div>
              <el-button @click="unbindAccount(account)" type="danger" size="small">解绑</el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="accountBindingDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup>
import {  ref,onMounted } from 'vue'
import { Message, Lock, User } from '@element-plus/icons-vue'
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
.back-button {
  position: absolute; /* 绝对定位按钮 */
  left: 150px; /* 按钮距离左边的距离 */
  top: 17%; /* 按钮垂直居中 */
  transform: translateY(-50%); /* 精确垂直居中 */
  font-size: 20px; /* 可以根据需要调整按钮的大小 */
}
.personal-container {
  margin-top: 30px;
  margin-left: 90px;
  border-radius: 50px ;
  height: 84vh;
  width: 87vw;
  background: #f5f7fa;
}

.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background: #fff;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  border-radius: 20px ;
  width: 87vw;
}

.user-id {
  font-size: 14px;
  color: #666;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-name {
  font-size: 24px;
  font-weight: 500;
}

.security-card {
  background: #fff;
}

.security-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.security-item:last-child {
  border-bottom: none;
}

.el-descriptions {
  margin-top: 20px;
}

.el-tag {
  margin-left: 10px;
}

/* 添加按钮文字样式 */
.el-button {
  color: #333;
  font-weight: 500;
}

.el-button--primary {
  color: #fff;
}

.el-button--danger {
  color: #fff;
}

.back-btn {
    position: absolute;
    left: 116px;
    top: 104px;
    padding: 0.5rem;
    transition: all 0.3s ease;
    color: #333;
    font-weight: 500;
  
    &:hover {
      transform: translateX(-3px);
      color: var(--el-color-primary);
      .icon {
        color: var(--el-color-primary);
      }
    }
  }
</style>