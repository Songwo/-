<template>
  <el-card class="title-card glass-effect">
    <template #header>
      <div class="card-header">
        <el-icon><Trophy /></el-icon>
        <span>用户称号</span>
      </div>
    </template>

    <div class="title-content">
      <div class="current-title">
        <span class="label">当前称号：</span>
        <el-tag :type="currentTitle.type" effect="dark" class="title-tag">
          {{ currentTitle.name }}
        </el-tag>
      </div>

      <div class="title-list">
        <div class="title-list-header">
          <span>可选称号</span>
          <el-button type="primary" size="small" @click="refreshTitles">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="title-grid">
          <div 
            v-for="title in availableTitles" 
            :key="title.id"
            class="title-item"
            :class="{ 'selected': title.id === currentTitle.id }"
            @click="selectTitle(title)"
          >
            <el-tag :type="title.type" effect="dark" class="title-tag">
              {{ title.name }}
            </el-tag>
            <div class="title-info">
              <span class="title-desc">{{ title.description }}</span>
              <span class="title-requirement">要求：{{ title.requirement }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Trophy, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ToUrl from '@/api/api'
import store from '@/store'

const currentTitle = ref({
  id: '',
  name: '未设置',
  type: 'info',
  description: '',
  requirement: ''
})

const availableTitles = ref([])

// 加载用户当前称号
const loadCurrentTitle = async () => {
  try {
    const response = await axios.get(`${ToUrl.url}/user/title/current`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    currentTitle.value = response.data.data
  } catch (error) {
    ElMessage.error('加载当前称号失败')
  }
}

// 加载可用称号列表
const loadAvailableTitles = async () => {
  try {
    const response = await axios.get(`${ToUrl.url}/user/titles`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    availableTitles.value = response.data.data
  } catch (error) {
    ElMessage.error('加载称号列表失败')
  }
}

// 选择称号
const selectTitle = async (title) => {
  try {
    await axios.post(`${ToUrl.url}/user/title/select`, {
      titleId: title.id
    }, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    currentTitle.value = title
    ElMessage.success('称号设置成功')
  } catch (error) {
    ElMessage.error('设置称号失败')
  }
}

// 刷新称号列表
const refreshTitles = () => {
  loadAvailableTitles()
  loadCurrentTitle()
}

onMounted(() => {
  loadCurrentTitle()
  loadAvailableTitles()
})
</script>

<style scoped>
.title-card {
  margin-bottom: 20px;
  border-radius: 15px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.title-content {
  padding: 20px;
}

.current-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.label {
  color: #fff;
  font-weight: 500;
}

.title-list {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 15px;
}

.title-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  color: #fff;
  font-weight: 500;
}

.title-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.title-item {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.title-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.15);
}

.title-item.selected {
  background: rgba(64, 158, 255, 0.2);
  border: 1px solid rgba(64, 158, 255, 0.5);
}

.title-tag {
  margin-bottom: 10px;
}

.title-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.title-desc {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.title-requirement {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

:deep(.el-button) {
  color: #fff;
}

@media (max-width: 768px) {
  .title-grid {
    grid-template-columns: 1fr;
  }
}
</style> 