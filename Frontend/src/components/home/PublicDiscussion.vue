<template>
  <div class="public-discussion">
    <div class="discussion-header">
      <h1>公共讨论区</h1>
      <div class="header-actions">
        <el-button type="success" @click="showCreateRoomDialog" :icon="Plus">创建房间</el-button>
        <el-button type="primary" @click="createNewTopic" :icon="Plus">发起新话题</el-button>
      </div>
    </div>

    <!-- 话题分类标签 -->
    <div class="topic-categories">
      <el-tag
        v-for="category in categories"
        :key="category.value"
        :type="category.type"
        effect="dark"
        class="category-tag"
        @click="selectCategory(category.value)"
        :class="{ active: selectedCategory === category.value }"
      >
        <el-icon class="tag-icon"><component :is="category.icon" /></el-icon>
        {{ category.label }}
        <span class="room-count" v-if="category.roomCount">({{ category.roomCount }})</span>
      </el-tag>
    </div>

    <!-- 房间列表 -->
    <div class="rooms-list" v-if="selectedCategory !== 'all'">
      <el-card v-for="room in filteredRooms" :key="room.id" class="room-card" :class="{ active: selectedRoom?.id === room.id }">
        <div class="room-info" @click="selectRoom(room)">
          <div class="room-header">
            <h3>{{ room.name }}</h3>
            <el-tag size="small" :type="room.type">{{ room.category }}</el-tag>
          </div>
          <div class="room-meta">
            <span class="online-count">
              <el-icon><User /></el-icon>
              {{ room.onlineCount }}人在线
            </span>
            <span class="message-count">
              <el-icon><ChatDotRound /></el-icon>
              {{ room.messageCount }}条消息
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container" v-if="selectedRoom" :class="{ 'room-expanded': selectedRoom }">
      <div class="chat-header">
        <div class="room-title">
          <h2>{{ selectedRoom.name }}</h2>
          <div class="room-stats">
            <span class="stat-item">
              <el-icon><User /></el-icon>
              {{ selectedRoom.onlineCount }}人在线
            </span>
            <span class="stat-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ selectedRoom.messageCount }}条消息
            </span>
            <el-tag size="small" :type="selectedRoom.type">{{ selectedRoom.category }}</el-tag>
          </div>
        </div>
        <div class="room-actions">
          <el-button type="text" @click="toggleRoomInfo">
            <el-icon><InfoFilled /></el-icon>
            房间信息
          </el-button>
          <el-button type="text" @click="leaveRoom" v-if="isRoomCreator">
            <el-icon><Delete /></el-icon>
            解散房间
          </el-button>
          <el-button type="text" class="exit-button" @click="exitRoom">
            <el-icon><Close /></el-icon>
            退出房间
          </el-button>
        </div>
      </div>

      <div class="chat-messages" ref="chatMessages">
        <div v-for="(message, index) in messages" :key="index" class="message" :class="{ 'message-self': message.userId === currentUserId }">
          <div class="message-header">
            <el-avatar :size="32" :src="message.avatar" />
            <span class="username">{{ message.username }}</span>
            <span class="time">{{ formatTime(message.timestamp) }}</span>
          </div>
          <div class="message-content">{{ message.content }}</div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input
          v-model="newMessage"
          type="textarea"
          :rows="3"
          placeholder="输入消息..."
          @keyup.enter.ctrl="sendMessage"
        />
        <div class="input-actions">
          <span class="tip">按 Ctrl + Enter 发送</span>
          <el-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">发送</el-button>
        </div>
      </div>
    </div>

    <!-- 创建房间对话框 -->
    <el-dialog
      v-model="createRoomDialogVisible"
      title="创建新房间"
      width="500px"
    >
      <el-form :model="newRoom" label-width="80px" :rules="roomRules" ref="roomForm">
        <el-form-item label="房间名称" prop="name">
          <el-input v-model="newRoom.name" placeholder="请输入房间名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="newRoom.category" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="newRoom.description"
            type="textarea"
            :rows="3"
            placeholder="请输入房间描述"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="newRoom.password"
            type="password"
            placeholder="可选：设置房间密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createRoomDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createRoom">创建</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 房间信息抽屉 -->
    <el-drawer
      v-model="roomInfoVisible"
      title="房间信息"
      direction="rtl"
      size="300px"
    >
      <div class="room-info-content" v-if="selectedRoom">
        <h3>{{ selectedRoom.name }}</h3>
        <p class="description">{{ selectedRoom.description }}</p>
        <div class="info-section">
          <h4>在线成员 ({{ selectedRoom.onlineCount }})</h4>
          <div class="member-list">
            <div v-for="member in selectedRoom.members" :key="member.id" class="member-item">
              <el-avatar :size="24" :src="member.avatar" />
              <span>{{ member.username }}</span>
              <el-tag size="small" v-if="member.isCreator">创建者</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { 
  Plus, 
  User, 
  InfoFilled, 
  Delete, 
  Monitor, 
  Lock, 
  Tools, 
  Warning, 
  Share,
  Close,
  ChatDotRound
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useStore } from 'vuex'
import ToUrl from '@/api/api'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const store = useStore()
const currentUserId = computed(() => store.state.user)
const currentUserAvatar = computed(() => `${ToUrl.url}/${store.state.avatar}`)

// WebSocket 连接
let ws = null
const wsUrl = 'ws://your-websocket-server-url'

// 分类数据
const categories = [
  { label: '技术交流', value: 'tech', type: 'success', icon: 'Monitor', roomCount: 5 },
  { label: '安全讨论', value: 'security', type: 'warning', icon: 'Lock', roomCount: 3 },
  { label: '漏洞分析', value: 'vulnerability', type: 'danger', icon: 'Warning', roomCount: 2 },
  { label: '工具分享', value: 'tools', type: 'info', icon: 'Tools', roomCount: 4 },
  { label: '经验分享', value: 'experience', type: '', icon: 'Share', roomCount: 6 }
]

// 状态变量
const selectedCategory = ref('tech')
const selectedRoom = ref(null)
const messages = ref([])
const newMessage = ref('')
const chatMessages = ref(null)
const createRoomDialogVisible = ref(false)
const roomInfoVisible = ref(false)

// 房间列表
const rooms = ref([
  {
    id: 1,
    name: 'Web安全技术交流',
    category: 'tech',
    description: '讨论Web安全相关技术问题',
    onlineCount: 15,
    messageCount: 234,
    type: 'success',
    members: [
      { id: 1, username: '安全专家', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', isCreator: true },
      { id: 2, username: '白帽黑客', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', isCreator: false }
    ]
  }
])

// 新建房间表单
const newRoom = ref({
  name: '',
  category: '',
  description: '',
  password: ''
})

// 表单验证规则
const roomRules = {
  name: [
    { required: true, message: '请输入房间名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入房间描述', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

// 计算属性：根据分类筛选房间
const filteredRooms = computed(() => {
  return rooms.value.filter(room => room.category === selectedCategory.value)
})

// 判断是否为房间创建者
const isRoomCreator = computed(() => {
  if (!selectedRoom.value) return false
  return selectedRoom.value.members.some(member => 
    member.id === currentUserId.value && member.isCreator
  )
})

// 方法
const showCreateRoomDialog = () => {
  createRoomDialogVisible.value = true
}

const createRoom = () => {
  // TODO: 实现创建房间逻辑
  ElMessage.success('房间创建成功！')
  createRoomDialogVisible.value = false
}

const selectRoom = (room) => {
  if (selectedRoom.value?.id === room.id) return
  selectedRoom.value = room
  messages.value = [] // 清空消息历史
  joinRoom(room.id)
}

const toggleRoomInfo = () => {
  roomInfoVisible.value = !roomInfoVisible.value
}

const leaveRoom = () => {
  ElMessageBox.confirm('确定要解散该房间吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // TODO: 实现解散房间逻辑
    ElMessage.success('房间已解散')
    selectedRoom.value = null
  }).catch(() => {})
}

// WebSocket 相关方法
const connectWebSocket = () => {
  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    console.log('WebSocket connected')
    if (selectedRoom.value) {
      joinRoom(selectedRoom.value.id)
    }
  }

  ws.onmessage = (event) => {
    const message = JSON.parse(event.data)
    messages.value.push(message)
    scrollToBottom()
  }

  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
    ElMessage.error('聊天连接出错，请刷新页面重试')
  }

  ws.onclose = () => {
    console.log('WebSocket disconnected')
  }
}

const joinRoom = (roomId) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      type: 'join',
      roomId: roomId,
      userId: currentUserId.value,
      username: store.state.user,
      avatar: currentUserAvatar.value
    }))
  }
}

const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedRoom.value) return

  const message = {
    type: 'message',
    roomId: selectedRoom.value.id,
    content: newMessage.value,
    userId: currentUserId.value,
    username: store.state.user,
    avatar: currentUserAvatar.value,
    timestamp: new Date()
  }

  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(message))
    newMessage.value = ''
  } else {
    ElMessage.error('聊天连接已断开，请刷新页面重试')
  }
}

const selectCategory = (category) => {
  if (category === selectedCategory.value) return
  selectedCategory.value = category
  selectedRoom.value = null
  messages.value = []
}

const formatTime = (time) => {
  return dayjs(time).fromNow()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatMessages.value) {
    chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  }
}

// 添加退出房间方法
const exitRoom = () => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      type: 'leave',
      roomId: selectedRoom.value.id,
      userId: currentUserId.value
    }))
  }
  selectedRoom.value = null
  messages.value = []
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<style scoped>
.public-discussion {
  max-width: 100%;
  margin: 0;
  padding: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

.discussion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.discussion-header h1 {
  margin: 0;
  font-size: 28px;
  color: #2c3e50;
}

.topic-categories {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.category-tag {
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 16px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.category-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.category-tag.active {
  transform: scale(1.05);
  font-weight: bold;
}

.tag-icon {
  font-size: 16px;
}

.room-count {
  font-size: 12px;
  opacity: 0.8;
}

.rooms-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.room-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.room-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.room-card.active {
  border: 2px solid #409EFF;
}

.room-info {
  padding: 12px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.room-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2c3e50;
}

.room-meta {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 13px;
}

.room-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-container {
  position: fixed;
  top: 60px; /* 导航栏高度 */
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: center;
  opacity: 0;
  transform: scale(0.95);
}

.room-expanded {
  opacity: 1;
  transform: scale(1);
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(to right, #f8f9fa, #ffffff);
  position: relative;
  z-index: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}



.exit-button {
  color: #f56c6c;
  transition: all 0.3s ease;
}

.exit-button:hover {
  color: #f56c6c;
  transform: scale(1.1);
}

.room-title {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.room-title h2 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.room-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 13px;
}

.room-actions {
  display: flex;
  gap: 12px;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8f9fa;
  scroll-behavior: smooth;
}

.message {
  max-width: 80%;
  align-self: flex-start;
  animation: messageSlideIn 0.3s ease-out;
}

.message-self {
  align-self: flex-end;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.username {
  font-weight: 500;
  color: #2c3e50;
}

.time {
  font-size: 12px;
  color: #909399;
}

.message-content {
  background: #fff;
  padding: 12px;
  border-radius: 8px;
  color: #2c3e50;
  word-break: break-word;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.message-content:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-self .message-content {
  background: #409EFF;
  color: #fff;
}

.chat-input {
  padding: 20px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.chat-input:focus-within {
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.tip {
  color: #909399;
  font-size: 12px;
}

.room-info-content {
  padding: 20px;
}

.room-info-content h3 {
  margin: 0 0 12px 0;
  color: #2c3e50;
}

.description {
  color: #606266;
  margin-bottom: 24px;
  line-height: 1.6;
}

.info-section {
  margin-top: 24px;
}

.info-section h4 {
  margin: 0 0 12px 0;
  color: #2c3e50;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    top: 50px; /* 移动端导航栏可能更小 */
  }

  .chat-container::before {
    height: 50px;
  }

  .chat-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .room-stats {
    flex-wrap: wrap;
  }

  .room-actions {
    width: 100%;
    justify-content: space-between;
  }

  .message {
    max-width: 90%;
  }
}
</style> 