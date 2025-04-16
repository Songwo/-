<template>
  <div class="ai-answer-container">
    <div class="platform-title">
      <h1>智能问答</h1>
      <div class="cyber-line"></div>
    </div>

    <div class="ai-content">
      <!-- 问答区域 -->
      <div class="qa-section">
        <div class="input-area card-style">
          <el-input
            v-model="userInput"
            type="textarea"
            :rows="4"
            placeholder="请输入您的问题，Shift + Enter 换行..."
            resize="none"
            :disabled="isLoading"
            @keydown.enter.prevent="onEnterPress"
          />
          <div class="input-actions">
             <el-button
              type="primary"
              @click="sendQuestion"
              :loading="isLoading"
              :disabled="!userInput.trim() || isLoading"
              class="ask-btn"
             >
               <el-icon><Promotion /></el-icon>
               发送
             </el-button>
          </div>
        </div>

        <!-- 回答展示区域 -->
        <div v-if="activeChat.question || isLoading" class="answer-box card-style">
           <!-- 显示用户问题 -->
           <div v-if="activeChat.question" class="question-display">
              <div class="question-header">
                 <el-icon><UserFilled /></el-icon>
                 <span>你的问题</span>
              </div>
              <div class="question-content">{{ activeChat.question }}</div>
           </div>

           <!-- 分隔线 -->
           <el-divider v-if="activeChat.question && (activeChat.answer || isLoading)"/>

           <!-- 显示 AI 回答或加载状态 -->
           <div class="answer-display">
              <div class="answer-header">
                <div class="header-left">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>AI助手回答</span>
                </div>
                <el-button 
                  v-if="activeChat.answer && !activeChat.isLoading"
                  type="info" 
                  plain 
                  size="small" 
                  @click="handleCopyClick(activeChat.answer)" 
                  class="copy-btn"
                  title="复制回答内容"
                >
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>
              <!-- 加载动画 -->
              <div v-if="activeChat.isLoading" class="loading-answer">
                  <div class="dot-flashing"></div>
                  <span>AI 正在思考...</span>
              </div>
              <!-- Markdown 渲染的回答 -->
              <div 
                v-else-if="activeChat.answer" 
                class="answer-content markdown-body custom-scrollbar"
                v-html="formatReply(activeChat.answer)"
              ></div>
           </div>
        </div>
        
        <!-- 初始欢迎信息 -->
        <div v-else-if="!isLoading && historyList.length === 0" class="welcome-message card-style">
           <el-icon><MagicStick /></el-icon>
           <p>你好！有什么可以帮助你的吗？</p>
           <span>试着问我一些问题吧！</span>
        </div>

      </div>

      <!-- 历史记录区域 -->
      <div class="history-section card-style">
        <div class="history-header">
          <span><el-icon><Clock /></el-icon> 历史记录</span>
          <el-button
            type="danger"
            text
            bg
            size="small"
            @click="clearAllHistory"
            :disabled="historyList.length === 0"
            title="清空所有历史记录"
          >
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </div>

        <div class="history-list custom-scrollbar" v-if="historyList.length > 0">
          <div
            v-for="(chat, index) in historyList"
            :key="chat.time.toISOString() + index" 
            class="history-item"
            :class="{ 'active': activeIndex === index }"
            @click="selectHistoryItem(index)"
          >
            <div class="history-item-content">
                <div class="history-question">{{ shortenText(chat.question, 40) }}</div>
                <div class="history-time">{{ formatTimestamp(chat.time) }}</div>
            </div>
             <el-button 
                circle 
                type="danger" 
                plain
                size="small" 
                class="delete-item-btn"
                title="删除此条记录"
                @click.stop="deleteHistoryItem(index)"
             >
                <el-icon :size="12"><Close /></el-icon>
            </el-button>
          </div>
        </div>

        <div v-else class="empty-history">
          <el-empty description="暂无历史记录" :image-size="80" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Promotion, ChatDotRound, CopyDocument, Clock, Delete, MagicStick, UserFilled, Close
} from '@element-plus/icons-vue';
import axios from 'axios';
import ToUrl from '@/api/api';
import { copyText } from '@/utils/clipboard.js';
import store from '@/store';
import { marked } from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';

marked.setOptions({
  highlight: function(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
    try {
      return hljs.highlight(code, { language, ignoreIllegals: true }).value;
    } catch (error) {
      console.error(`Highlighting failed for language ${lang}:`, error);
      return hljs.highlightAuto(code).value;
    }
  },
  langPrefix: 'hljs language-',
  gfm: true,
  breaks: true
});

const userInput = ref('');
const isLoading = ref(false);
const historyList = ref([]);
const activeIndex = ref(-1);

const activeChat = computed(() => {
  if (activeIndex.value >= 0 && activeIndex.value < historyList.value.length) {
    return historyList.value[activeIndex.value];
  }
  if (activeIndex.value === -1 && historyList.value.length > 0 && historyList.value[0].isLoading) {
     return historyList.value[0];
  }
  return { question: '', answer: '', time: null, isLoading: false }; 
});

// 发送用户的问题到 AI API
const sendQuestion = async () => {
  const trimmedInput = userInput.value.trim();
  if (!trimmedInput || isLoading.value) return;

  isLoading.value = true;
  const currentUserInput = userInput.value;
  userInput.value = '';

  const tempChatItem = {
    question: currentUserInput,
    answer: '',
    time: new Date(),
    isLoading: true
  };

  historyList.value.unshift(tempChatItem);
  activeIndex.value = 0;

  try {
    const response = await axios.post(ToUrl.aiUrl, {
       message: currentUserInput
     },{
       headers: {
         Authorization: 'Bearer '+store.state.token
       }
    });

    let receivedReply = response.data?.reply || '抱歉，未能获取到回答。'; 
    
    if (historyList.value[0]?.time === tempChatItem.time && historyList.value[0]?.question === currentUserInput) {
        historyList.value[0].answer = receivedReply; 
    } else {
        const chatToUpdate = historyList.value.find(chat => chat.time === tempChatItem.time && chat.question === currentUserInput);
         if (chatToUpdate) {
            chatToUpdate.answer = receivedReply;
         }
    }

    saveHistoryToLocal(); 

  } catch (error) {
    console.error('AI问答出错:', error);
    ElMessage.error('与AI服务连接失败，请稍后再试');
    if (historyList.value[0]?.time === tempChatItem.time && historyList.value[0]?.question === currentUserInput) {
        historyList.value[0].answer = '抱歉，获取回答时遇到网络错误。'; 
    } else {
        const chatToUpdate = historyList.value.find(chat => chat.time === tempChatItem.time && chat.question === currentUserInput);
         if (chatToUpdate) {
            chatToUpdate.answer = '抱歉，获取回答时遇到网络错误。';
         }
    }
     saveHistoryToLocal(); 
  } finally {
    if (historyList.value[0]?.time === tempChatItem.time && historyList.value[0]?.question === currentUserInput) {
        historyList.value[0].isLoading = false; 
    } else {
         const chatToUpdate = historyList.value.find(chat => chat.time === tempChatItem.time && chat.question === currentUserInput);
         if (chatToUpdate) {
            chatToUpdate.isLoading = false;
         }
    }
    isLoading.value = false;
  }
};

// 加载聊天记录
const loadHistoryData = async () => {
  isLoading.value = true; 
  let loadedFromApi = false;

  try {
    const response = await axios.get(ToUrl.aiUrl + '/history', {
       headers: {
         Authorization: 'Bearer '+store.state.token
       }
    });
    
    if (response.data && Array.isArray(response.data.history)) {
      const apiHistory = response.data.history;
      const processedHistory = [];
      
      for (let i = 0; i < apiHistory.length; i += 2) {
        const userEntry = apiHistory[i];
        const assistantEntry = apiHistory[i + 1];

        if (userEntry && userEntry.role === 'user' && assistantEntry && assistantEntry.role === 'assistant') {
          processedHistory.push({
            question: decodeUnicode(userEntry.content),
            answer: decodeUnicode(assistantEntry.content),
            time: new Date(),
            isLoading: false
          });
        } else {
          console.warn(`History data at index ${i} does not form a user/assistant pair.`, userEntry, assistantEntry);
        }
      }

      historyList.value = processedHistory.reverse(); 
      activeIndex.value = -1;
      saveHistoryToLocal();
      loadedFromApi = true;
      console.log(`历史记录已从 API 加载并处理 ${processedHistory.length} 条对话。`);
      
    } else {
       console.warn('API 加载历史记录失败或格式无效: history 数组未找到或格式不正确');
    }
  } catch (error) {
    console.error('API 加载历史记录错误:', error);
  }

  if (!loadedFromApi) {
    const localHistoryData = localStorage.getItem('aiChatHistory');
    if (localHistoryData) {
      try {
        historyList.value = JSON.parse(localHistoryData).map(chat => ({
          ...chat,
          time: new Date(chat.time),
          isLoading: false
        })).sort((a, b) => b.time - a.time); 
        activeIndex.value = -1;
        console.log('历史记录已从本地存储加载');
      } catch (parseError) {
        console.error('解析本地历史记录错误:', parseError);
        localStorage.removeItem('aiChatHistory');
        historyList.value = [];
        activeIndex.value = -1;
      }
    } else {
      historyList.value = [];
      activeIndex.value = -1;
      console.log('未找到聊天记录 (API 和本地存储均无)。');
    }
  }
  
  isLoading.value = false; 
};

// 确认后清空所有聊天记录
const clearAllHistory = () => {
  ElMessageBox.confirm(
    '确定要清空所有历史问答记录吗？此操作不可恢复。',
    '确认清空',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true,
    }
  ).then(() => {
      historyList.value = [];
      activeIndex.value = -1; 
      localStorage.removeItem('aiChatHistory');
      ElMessage.success('历史记录已清空');
  }).catch(() => {
    ElMessage.info('已取消清空操作');
  });
};

// 选择一个历史记录项进行查看
const selectHistoryItem = (index) => {
  activeIndex.value = index;
};

// 新增：删除单条历史记录
const deleteHistoryItem = (indexToDelete) => {
  const itemToDelete = historyList.value[indexToDelete];
  if (!itemToDelete) return;

  const questionPreview = shortenText(itemToDelete.question, 30);

  ElMessageBox.confirm(
    `确定要删除这条记录吗？\n问题: "${questionPreview}"`, 
    '确认删除',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true,
    }
  ).then(() => {
    historyList.value.splice(indexToDelete, 1);
    
    if (activeIndex.value === indexToDelete || activeIndex.value >= historyList.value.length) {
      activeIndex.value = historyList.value.length > 0 ? 0 : -1;
    }
    
    saveHistoryToLocal();
    ElMessage.success('记录已删除');

  }).catch(() => {
     ElMessage.info('已取消删除操作');
  });
};

// 将当前聊天记录 (不包括加载中的项) 保存到本地存储
const saveHistoryToLocal = () => {
  const historyForStorage = historyList.value
      .filter(chat => !chat.isLoading)
      .map(chat => ({
        question: chat.question,
        answer: chat.answer,
        time: chat.time instanceof Date ? chat.time.toISOString() : new Date().toISOString() 
      }));
  localStorage.setItem('aiChatHistory', JSON.stringify(historyForStorage));
};

// 处理文本输入框的回车键事件
const onEnterPress = (event) => {
  if (!event.shiftKey && !isLoading.value) { 
    sendQuestion();
  }
};

// 格式化时间戳，用于在历史记录中显示
const formatTimestamp = (time) => {
  if (!time) return '';
  const now = new Date();
  const date = new Date(time);

  const diffSeconds = Math.floor((now - date) / 1000);
  const diffMinutes = Math.floor(diffSeconds / 60);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffSeconds < 60) {
    return '刚刚';
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`;
  } else if (diffHours < 24 && date.getDate() === now.getDate()) {
     return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  } else if (diffDays === 1 && now.getDate() - date.getDate() === 1) {
     return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  } else if (date.getFullYear() === now.getFullYear()) {
     return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
  } else {
    return date.toLocaleDateString('zh-CN');
  }
};

// 解码 Unicode 转义序列 (例如 \uXXXX)
const decodeUnicode = (str) => {
  if (!str) return '';
  try {
    const jsonString = `"${str.replace(/"/g, '\\"' )}"`;
    let decoded = JSON.parse(jsonString);
    return decoded;
  } catch (e) {
    console.error("Unicode 解码失败 for:", str, e);
    try {
        return str.replace(/\\u([0-9a-fA-F]{4})/g, (match, grp) => {
           return String.fromCharCode(parseInt(grp, 16)); 
        });
    } catch (regexError) {
        console.error("Regex Unicode decode failed too:", regexError);
        return str;
    }
  }
};

// 使用 marked 处理 Markdown 并应用高亮
const formatReply = (text) => {
  if (!text) return '';
  try {
    let decodedText = decodeUnicode(text);
    return marked(decodedText);
  } catch (error) {
    console.error("Markdown parsing/formatting failed:", error);
    return decodeUnicode(text).replace(/\n/g, '<br>');
  }
};

const shortenText = (text, maxLength) => {
  if (!text) return '';
  let decodedText = decodeUnicode(text);
  if (decodedText.length <= maxLength) return decodedText;
  return decodedText.substring(0, maxLength) + '...';
};

// 处理复制按钮点击事件，调用导入的 copyText
const handleCopyClick = (text) => {
  if (!text) return;
  let decodedText = decodeUnicode(text);
  copyText(decodedText)
    .then(() => {
      ElMessage.success('回答已复制到剪贴板');
    })
    .catch((err) => {
      console.error('复制失败:', err);
      ElMessage.error(err.message || '复制失败，请检查浏览器权限或环境'); 
    });
};

onMounted(() => {
  loadHistoryData();
});

</script>

<style scoped>
/* 添加 highlight.js 所需的 CSS 类 */
.markdown-body pre code.hljs {
  display: block;
  overflow-x: auto;
  padding: 1em;
  background: #f3f4f6;
  border-radius: 6px;
}
.markdown-body code:not(pre code) {
  background-color: rgba(175, 184, 193, 0.2);
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  border-radius: 3px;
}

.ai-answer-container {
  max-width: 1300px;
  margin: 20px auto;
  padding: 25px;
  box-sizing: border-box;
  color: #333;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.platform-title {
  text-align: center;
  margin-bottom: 40px;
}

.platform-title h1 {
  font-size: 2.2rem;
  margin-bottom: 15px;
  color: #2c3e50;
  font-weight: 600;
  letter-spacing: 1px;
}

.cyber-line {
  height: 4px;
  width: 80px;
  background: linear-gradient(90deg, #409EFF, #79bbff);
  border-radius: 2px;
  margin: 0 auto;
}

.ai-content {
  display: flex;
  gap: 25px;
}

.card-style {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  border: 1px solid #e4e7ed;
  transition: box-shadow 0.3s ease;
}
.card-style:hover {
   box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}


.qa-section {
  flex: 2.5;
  display: flex;
  flex-direction: column;
  gap: 25px;
  min-width: 0;
}

.history-section {
  flex: 1;
  min-width: 300px;
  max-width: 350px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 220px);
  max-height: 800px;
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.input-area :deep(.el-textarea__inner) {
  border-radius: 8px;
  padding: 12px 15px;
  line-height: 1.6;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

.ask-btn {
  width: 90px;
  transition: all 0.3s ease;
}
.ask-btn:hover {
   transform: translateY(-2px);
}


/* Styles for Question Display */
.question-display {
  margin-bottom: 15px;
}
.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #606266;
  font-size: 1rem;
}
.question-content {
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* Styles for Answer Display */
.answer-display {
  margin-top: 15px;
}
.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  /* padding-bottom: 10px; */
  /* border-bottom: 1px solid #f0f2f5; */ 
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #409EFF;
  font-size: 1.05rem;
}

.copy-btn {
   padding: 5px 8px;
}

.answer-content {
  line-height: 1.7;
  color: #303133;
  /* White space and word wrap handled by markdown parser */
  max-height: 65vh; /* Limit the maximum height */
  overflow-y: auto; /* Add vertical scrollbar when needed */
  padding-right: 10px; /* Space for the scrollbar */
  margin-right: -10px; /* Counteract padding for layout */
}

/* Ensure custom scrollbar styles apply here too */
.answer-content.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.answer-content.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
.answer-content.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}
.answer-content.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* Loading Animation Styles */
.loading-answer {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  color: #909399;
}
.dot-flashing {
  position: relative;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #909399;
  color: #909399;
  animation: dotFlashing 1s infinite linear alternate;
  animation-delay: .5s;
}
.dot-flashing::before,
.dot-flashing::after {
  content: '';
  display: inline-block;
  position: absolute;
  top: 0;
}
.dot-flashing::before {
  left: -10px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #909399;
  color: #909399;
  animation: dotFlashing 1s infinite alternate;
  animation-delay: 0s;
}
.dot-flashing::after {
  left: 10px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #909399;
  color: #909399;
  animation: dotFlashing 1s infinite alternate;
  animation-delay: 1s;
}
@keyframes dotFlashing {
  0% {
    background-color: #909399;
  }
  50%, 100% {
    background-color: #dcdfe6;
  }
}

/* Welcome message */
.welcome-message {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}
.welcome-message .el-icon {
  font-size: 3rem;
  color: #c0c4cc;
  margin-bottom: 15px;
}
.welcome-message p {
  font-size: 1.1rem;
  color: #606266;
  margin-bottom: 5px;
}
.welcome-message span {
  font-size: 0.9rem;
}


.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
  font-weight: 600;
  color: #303133;
  flex-shrink: 0;
}
.history-header > span {
  display: flex;
  align-items: center;
  gap: 6px;
}


.history-list {
  overflow-y: auto;
  flex-grow: 1;
  margin-right: -10px;
  padding-right: 10px;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #909399;
}


.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  background-color: #f9fafb;
  border: 1px solid transparent;
  position: relative;
}

.history-item:hover {
  background-color: #f0f5ff;
  transform: translateX(3px);
}

.history-item.active {
  background-color: #ecf5ff;
  border-color: #a0cfff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateX(0);
}

.history-item-content {
  flex-grow: 1;
  margin-right: 8px;
  overflow: hidden;
}

.history-question {
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}


.history-time {
  font-size: 0.75rem;
  color: #a8abb2;
}

.delete-item-btn {
  opacity: 0.6;
  transition: opacity 0.2s ease;
  flex-shrink: 0;
}

.history-item:hover .delete-item-btn {
   opacity: 1;
}

.delete-item-btn:hover {
   background-color: #fef0f0 !important;
   border-color: #fde2e2 !important;
   color: #f56c6c !important;
}


.empty-history {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-grow: 1;
  color: #c0c4cc;
}

/* Responsive */
@media (max-width: 992px) {
   .ai-content {
     flex-direction: column;
   }
   .history-section {
     max-width: none;
     height: 350px;
     max-height: 40vh;
   }
   .qa-section {
      flex: none;
   }
}

@media (max-width: 768px) {
  .ai-answer-container {
    padding: 15px;
  }
  .platform-title h1 {
    font-size: 1.8rem;
  }
  .card-style {
     padding: 15px;
     border-radius: 8px;
  }
   .history-section {
     height: 300px;
     max-height: 35vh;
   }
  .ask-btn {
     width: 80px;
  }
}
</style>