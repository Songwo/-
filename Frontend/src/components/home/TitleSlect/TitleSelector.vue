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
import { ref, watch, computed } from 'vue'
import { Trophy, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ToUrl from '@/api/api'
import store from '@/store'

// 接收父组件的props
const props = defineProps({
  honoraryTitle: {
    type: Object,
    required: true
  }
})

// 可选称号列表
const availableTitles = computed(() => {
  const typeMap = {
    '新星白帽': 'success',
    '安全先锋': 'warning',
    '攻防大师': 'danger',
    '漏洞猎人': 'info'
  }
  const descMap = {
    '新星白帽': '刚踏入安全圈，展现出极大潜力的新手白帽。',
    '安全先锋': '在社区中积极参与并推动信息安全进步的成员。',
    '攻防大师': '攻防大师称号',
    '漏洞猎人': '漏洞猎人称号'
  }
  const reqMap = {
    '新星白帽': '完成新手任务获得',
    '安全先锋': '完成安全基础课程',
    '攻防大师': '完成高级攻防课程',
    '漏洞猎人': '发现并报告漏洞'
  }
  const honoraryTitle = props.honoraryTitle || {}
  // console.log(honoraryTitle);
  return Object.keys(honoraryTitle).map(key => ({
    id: key,
    name: key,
    type: typeMap[key] || 'info',
    description: descMap[key] || '',
    requirement: reqMap[key] || ''
  }))
})

// 当前称号
const currentTitle = computed(() => {
  const honoraryTitle = props.honoraryTitle || {}
  const selectedTitle = Object.entries(honoraryTitle).find(([_, value]) => value === 1)
  
  if (selectedTitle) {
    const title = availableTitles.value.find(t => t.id === selectedTitle[0])
    if (title) {
      return title
    }
  }
  return {
    id: '',
    name: '未设置',
    type: 'info',
    description: '',
    requirement: ''
  }
})

// 选择称号
const selectTitle = async (title) => {
  try {
    // console.log('Selecting title:', title)
    const response = await axios.get(`${ToUrl.url}/user/selectHonoraryTitle`, {
      params: { HonoraryTitle: title.id },
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    
    if (response.data.code === 200) {
      // 更新本地状态
      const honoraryTitle = { ...props.honoraryTitle }
      Object.keys(honoraryTitle).forEach(key => {
        honoraryTitle[key] = key === title.id ? 1 : 0
      })
      // console.log('Updated honoraryTitle:', honoraryTitle)
      // 触发父组件更新
      emit('update:honoraryTitle', honoraryTitle)
      ElMessage.success('称号设置成功')
    } else {
      ElMessage.error(response.data.message || '设置称号失败')
    }
  } catch (error) {
    // console.error('Error selecting title:', error)
    ElMessage.error('设置称号失败')
  }
}

// 定义 emit
const emit = defineEmits(['update:honoraryTitle'])

// 刷新
const refreshTitles = () => {
  // No need to update currentTitleDisplay as it's handled by computed
}
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