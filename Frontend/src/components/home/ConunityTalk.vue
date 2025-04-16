<template>
  <div class="forum-container">
    <!-- 头部 -->
    <el-card class="header-card">
      <div class="forum-header">
        <div class="forum-title-area">
          <h1>网络安全交流社区</h1>
          <p class="subtitle">与技术同行，与安全共生</p>
        </div>
        <div class="stats-area">
          <div class="stat-item">
            <el-icon><Document /></el-icon>
            <div class="stat-info">
              <span class="stat-count">{{ posts.length }}</span>
              <span class="stat-label">帖子总数</span>
            </div>
          </div>
          <div class="stat-item">
            <el-icon><User /></el-icon>
            <div class="stat-info">
              <span class="stat-count">{{ getUniqueAuthors() }}</span>
              <span class="stat-label">活跃用户</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体内容 -->
    <el-row :gutter="24" class="main-content">
      <!-- 帖子列表 -->
      <el-col :xs="24" :sm="16" class="post-column">
        <el-card shadow="hover">
          <template #header>
            <div class="filter-bar">
              <div class="filter-controls">
                <el-select v-model="filterSection" placeholder="全部板块" @change="fetchPosts">
                  <el-option label="全部板块" value="" />
                  <el-option v-for="section in forumSections" :key="section.title" :label="section.title"
                    :value="section.title" />
                </el-select>
                <el-select v-model="sortOption" placeholder="排序方式" @change="applySorting">
                  <el-option label="最新发布" value="newest" />
                  <el-option label="最多回复" value="mostReplies" />
                  <el-option label="最早发布" value="oldest" />
                </el-select>
                <el-input 
                  v-model="searchQuery" 
                  placeholder="搜索帖子" 
                  prefix-icon="Search"
                  clearable
                  @input="filterPosts"
                  class="search-input"
                />
              </div>
              <el-button type="primary" @click="openPostDialog">
                <el-icon class="mr-5"><Plus /></el-icon>新建帖子
              </el-button>
            </div>
          </template>

          <div v-loading="loadingPosts" class="post-list">
            <transition-group name="post-list">
              <el-card v-for="post in displayedPosts" :key="post.id" class="post-card" shadow="hover" @click="selectPost(post)">
                <template #header>
                  <div class="post-header">
                    <el-tag :color="getSectionColor(post.section)" effect="dark" class="section-tag">
                      {{ post.section }}
                    </el-tag>
                    <h3 class="post-title">{{ post.title }}</h3>
                    <el-button type="primary" size="small" class="detail-btn" @click.stop="openDetailDialog(post)">
                      查看详情
                    </el-button>
                  </div>
                </template>

                <div class="post-content">
                  <p class="post-excerpt">{{ truncateContent(post.content) }}</p>
                  <div class="post-meta">
                    <div class="meta-item">
                      <el-avatar :size="24" :src="ToUrl.url + '/' + post.avatar" />
                      <span class="author">{{ post.username }}</span>
                    </div>
                    <div class="meta-item">
                      <el-icon>
                        <Clock />
                      </el-icon>
                      <span>{{ formatTime(post.timestamp) }}</span>
                    </div>
                    <div class="meta-item">
                      <el-icon>
                        <ChatLineRound />
                      </el-icon>
                      <span>{{ post.replyCount }} 条回复</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </transition-group>

            <el-pagination
              v-if="filteredPosts.length > postsPerPage"
              :current-page="currentPage"
              :page-size="postsPerPage"
              :total="filteredPosts.length"
              layout="prev, pager, next"
              @current-change="handlePageChange"
              class="pagination"
            />
            <el-empty v-if="displayedPosts.length === 0" description="暂无相关帖子" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :xs="24" :sm="8" class="sidebar-column">
        <!-- 热门帖子 -->
        <el-card class="trending-card" shadow="hover">
          <template #header>
            <div class="trending-header">
              <h3><el-icon><Star /></el-icon> 热门帖子</h3>
            </div>
          </template>
          <div class="trending-list">
            <div v-for="(post, index) in trendingPosts" :key="post.id" class="trending-item" @click="selectPost(post)">
              <div class="trending-rank">{{ index + 1 }}</div>
              <div class="trending-content">
                <div class="trending-title">{{ post.title }}</div>
                <div class="trending-meta">
                  <span>{{ post.replyCount }} 回复</span>
                  <span>{{ formatTime(post.timestamp) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-if="trendingPosts.length === 0" description="暂无热门帖子" :image-size="60" />
          </div>
        </el-card>

        <!-- 用户板块 -->
        <el-card class="user-card" shadow="hover">
          <template #header>
            <div class="user-header">
              <h3><el-icon><User /></el-icon> 用户信息</h3>
            </div>
          </template>
          <div class="user-info">
            <el-avatar :size="64" :src="ToUrl.url+'/'+store.state.avatar" />
            <div class="user-details">
              <div class="user-name">{{ store.state.user }}</div>
              <div class="user-stats">
                <div class="stat">
                  <div class="stat-value">{{ userPostCount }}</div>
                  <div class="stat-label">帖子</div>
                </div>
                <div class="stat">
                  <div class="stat-value">{{ userCommentCount }}</div>
                  <div class="stat-label">评论</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <transition name="slide-fade">
          <el-card v-if="selectedPost" class="detail-card" shadow="hover" v-loading="loadingComments">
            <template #header>
              <div class="detail-header">
                <h3>帖子详情</h3>
                <el-button type="info" size="small" circle @click="selectedPost = null">
                  <el-icon>
                    <Close />
                  </el-icon>
                </el-button>
              </div>
            </template>

            <!-- 帖子内容 -->
            <div class="post-detail">
              <h4 class="detail-title">{{ selectedPost.title }}</h4>
              <div class="detail-meta">
                <el-avatar :size="32" :src="ToUrl.url + '/' + selectedPost.avatar" />
                <div class="meta-info">
                  <span class="author">{{ selectedPost.authorName }}</span>
                  <span class="time">{{ formatTime(selectedPost.timestamp) }}</span>
                </div>
              </div>
              <el-divider />
              <div class="detail-content">
                {{ selectedPost.content }}
              </div>
            </div>

            <!-- 评论区 -->
            <div class="comment-section">
              <h4 class="comment-title">评论（{{ selectedPost.comments?.length || 0 }}）</h4>
              <div v-loading="loadingComments" class="comment-list">
                <div v-for="comment in selectedPost.comments" :key="comment.id" class="comment-item">
                  <el-avatar :size="32" :src="ToUrl.url + '/' + comment.avatar" />
                  <div class="comment-content">
                    <div class="comment-header">
                      <span class="author">{{ comment.username }}</span>
                      <span class="time">{{ formatTime(comment.timestamp) }}</span>
                    </div>
                    <p class="comment-text">{{ comment.content }}</p>
                  </div>
                </div>

                <el-empty v-if="!selectedPost.comments?.length" description="暂无评论" />
              </div>

              <!-- 评论输入 -->
              <div class="comment-editor">
                <el-input v-model="newComment" type="textarea" :rows="3" placeholder="请输入您的评论..." resize="none" />
                <div class="editor-actions">
                  <el-button type="primary" :loading="postingComment" @click="submitComment">
                    发表评论
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 空状态 -->
          <el-card v-else class="empty-card" shadow="hover">
            <el-empty description="选择帖子查看详情" />
          </el-card>
        </transition>
      </el-col>
    </el-row>

    <!-- 新建帖子对话框 -->
    <el-dialog v-model="showPostDialog" title="新建帖子" width="800px">
      <el-form :model="newPost" :rules="postRules" label-width="80px" ref="postForm">
        <el-form-item label="标题" required prop="title">
          <el-input v-model="newPost.title" />
        </el-form-item>
        <el-form-item label="板块" required prop="section">
          <el-select v-model="newPost.section">
            <el-option v-for="section in forumSections" :key="section.title" :label="section.title"
              :value="section.title" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required prop="content">
          <el-input v-model="newPost.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" :loading="postingPost" @click="submitPost">
          立即发布
        </el-button>
      </template>
    </el-dialog>
     <!-- 帖子详情弹窗 -->
  <el-dialog 
    v-model="detailVisible" 
    width="1200px"
    class="post-detail-dialog"
    @closed="selectedPost = null"
    :show-close="false"
  >
    <template #header>
      <div class="dialog-header">
        <span>{{ selectedPost?.title }}</span>
        <el-button type="info" size="small" circle @click="detailVisible = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </template>
    <div v-loading="loadingComments" class="dialog-content">
      <!-- 帖子内容 -->
      <div class="post-content">
        <div class="post-meta">
          <el-avatar :size="32" :src="ToUrl.url+'/'+selectedPost.avatar" />
          <div class="meta-info">
            <span class="author">{{ selectedPost.username }}</span>
            <span class="time">{{ formatTime(selectedPost.timestamp) }}</span>
          </div>
        </div>
        <el-divider />
        <div class="content-text">
          {{ selectedPost.content }}
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-wrapper">
        <h4 class="comment-title">评论（{{ selectedPost.comments?.length || 0 }}）</h4>
        
        <div class="comment-list">
          <el-scrollbar height="300px">
            <div 
              v-for="comment in selectedPost.comments" 
              :key="comment.id" 
              class="comment-item"
            >
              <el-avatar :size="32" :src="ToUrl.url+'/'+comment.avatar" />
              <div class="comment-content">
                <div class="comment-header">
                  <span class="author">{{ comment.username }}</span>
                  <span class="time">{{ formatTime(comment.timestamp) }}</span>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
              </div>
            </div>
            <el-empty 
              v-if="!selectedPost.comments?.length" 
              description="暂无评论" 
              :image-size="80"
            />
          </el-scrollbar>
        </div>

        <!-- 评论输入 -->
        <div class="comment-editor">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="请输入您的评论..."
            resize="none"
          />
          <div class="editor-actions">
            <el-button 
              type="primary" 
              :loading="postingComment" 
              @click="submitComment"
            >
              发表评论
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watchEffect } from 'vue'
import {
  ElCard, ElButton, ElSelect, ElOption, ElTag,
  ElAvatar, ElIcon, ElEmpty, ElMessage, ElPagination, ElInput
} from 'element-plus'
import { 
  Clock, ChatLineRound, Close, Star, Document, 
  User, Plus, Search
} from '@element-plus/icons-vue'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'
import '@/assets/styles/forum-theme.css'

// 论坛板块
const forumSections = [
  { title: '安全漏洞讨论', description: '讨论各种网络安全漏洞及解决方案' },
  { title: '网络攻击案例', description: '分享并分析网络攻击的实际案例' },
  { title: '网络工具推荐', description: '分享并讨论网络安全工具的使用心得' },
  { title: '行业动态', description: '最新网络安全行业资讯分享' }
]

// 帖子相关状态
const posts = ref([])
const selectedPost = ref(null)
const filterSection = ref('')
const loadingPosts = ref(false)
const loadingComments = ref(false)
const postForm = ref(null)
// 评论相关状态
const newComment = ref('')
const postingComment = ref(false)
// 新建帖子相关状态
const showPostDialog = ref(false)
const newPost = ref({
  authorId: store.state.id,
  username: store.state.user,
  title: '',
  section: '',
  content: '',
  avatar: store.state.avatar
})
const postingPost = ref(false)

// 新增状态
const detailVisible = ref(false)
const sortOption = ref('newest')
const searchQuery = ref('')
const currentPage = ref(1)
const postsPerPage = 5
const userPostCount = ref(0)
const userCommentCount = ref(0)

// 修改打开详情方法
const openDetailDialog = async (post) => {
  selectedPost.value = post
  detailVisible.value = true
  
  // 获取评论
  if (post.replyCount > 0) {
    loadingComments.value = true
    try {
      const response = await axios.get(ToUrl.url+`/comments/find/${post.id}`, {
        headers: { 'Authorization': `Bearer ${store.state.token}` }
      })
      selectedPost.value.comments = response.data.data || []
    } catch (error) {
      ElMessage.error('获取评论失败')
    } finally {
      loadingComments.value = false
    }
  }
}

// 获取帖子方法增强
const fetchPosts = async () => {
  try {
    loadingPosts.value = true
    const response = await axios.get(ToUrl.url + '/post/findAll', {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    posts.value = response.data.data
    
    // 获取用户帖子和评论统计
    userPostCount.value = posts.value.filter(post => post.authorId === store.state.id).length
    fetchUserComments()
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    loadingPosts.value = false
  }
}

// 获取用户评论数
const fetchUserComments = async () => {
  try {
    const response = await axios.get(ToUrl.url + `/comments/user/${store.state.id}`, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    userCommentCount.value = response.data.data?.length || 0
  } catch (error) {
    console.error('获取用户评论失败', error)
  }
}

// 搜索过滤
const filterPosts = () => {
  currentPage.value = 1
}

// 排序功能
const applySorting = () => {
  currentPage.value = 1
}

// 列表过滤计算属性增强
const filteredPosts = computed(() => {
  if (!posts.value || posts.value.length === 0) return []
  
  let filtered = posts.value
  
  // 板块过滤
  if (filterSection.value) {
    filtered = filtered.filter(post => post.section === filterSection.value)
  }
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(post => 
      post.title.toLowerCase().includes(query) || 
      post.content.toLowerCase().includes(query) ||
      post.username.toLowerCase().includes(query)
    )
  }
  
  // 排序
  return filtered.sort((a, b) => {
    if (sortOption.value === 'newest') {
      return new Date(b.timestamp) - new Date(a.timestamp)
    } else if (sortOption.value === 'oldest') {
      return new Date(a.timestamp) - new Date(b.timestamp)
    } else if (sortOption.value === 'mostReplies') {
      return b.replyCount - a.replyCount
    }
    return 0
  })
})

// 分页后的帖子
const displayedPosts = computed(() => {
  const startIndex = (currentPage.value - 1) * postsPerPage
  return filteredPosts.value.slice(startIndex, startIndex + postsPerPage)
})

// 热门帖子
const trendingPosts = computed(() => {
  return [...posts.value]
    .sort((a, b) => b.replyCount - a.replyCount)
    .slice(0, 5)
})

// 获取独立作者数
const getUniqueAuthors = () => {
  if (!posts.value || posts.value.length === 0) return 0
  const uniqueAuthors = new Set(posts.value.map(post => post.authorId))
  return uniqueAuthors.size
}

// 修改后的时间格式化方法
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 修改后的selectPost方法
const selectPost = async (post) => {
  selectedPost.value = post;
  if (post.replyCount > 0) {
    loadingComments.value = true;
    try {
      const response = await axios.get(ToUrl.url + `/comments/find/${post.id}`, {
        headers: { 'Authorization': `Bearer ${store.state.token}` }
      });
      selectedPost.value.comments = response.data.data || [];
    } catch (error) {
      ElMessage.error('获取评论失败');
    } finally {
      loadingComments.value = false;
    }
  } else {
    selectedPost.value.comments = [];
  }
};

// 提交评论
const submitComment = async () => {
  try {
    if (!selectedPost.value?.id) {
      ElMessage.warning('请先选择要评论的帖子')
      return
    }

    if (!newComment.value.trim()) {
      ElMessage.warning('评论内容不能为空')
      return
    }

    postingComment.value = true
    const res = await axios.post(ToUrl.url + '/comments/insert', {
      authorId: store.state.id,
      username: store.state.user,
      postId: selectedPost.value.id,
      content: newComment.value,
      avatar: store.state.avatar
    }, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })

    // 更新本地数据
    selectedPost.value.comments.unshift({
      ...res.data.data,
      username: store.state.user,
      avatar: store.state.avatar,
    })
    selectedPost.value.replyCount++
    newComment.value = ''
  } catch (error) {
    ElMessage.error('评论发表失败')
  } finally {
    postingComment.value = false
  }
}

// 新建帖子
const openPostDialog = () => {
  showPostDialog.value = true
}
// 在setup()中添加验证规则
const postRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 50, message: '长度在5到50个字符', trigger: 'blur' }
  ],
  section: [
    { required: true, message: '请选择板块', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, message: '内容至少10个字符', trigger: 'blur' }
  ]
}

const submitPost = async () => {
  try {
    postingPost.value = true
    await postForm.value.validate()
    const res = await axios.post(ToUrl.url + '/post/insertPost', newPost.value, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    // console.log(res.data.data);
    // 构造完整帖子对象
    const newPostData = {
      ...res.data.data,
      comments: [],       // 初始化评论数组
    }
    posts.value.unshift(newPostData)
    selectPost(newPostData) // 选中新帖子
    showPostDialog.value = false
    newPost.value = { title: '', section: '', content: '' }
    ElMessage.success('帖子发布成功')
  } catch (error) {
    if (error.name !== 'Error') { // 过滤验证错误
      ElMessage.error('帖子发布失败请按照规则进行填写！！')
    }
  } finally {
    postingPost.value = false
  }
}

// 工具函数
const truncateContent = (text) => {
  return text.length > 100 ? text.slice(0, 100) + '...' : text
}

const getSectionColor = (section) => {
  const index = forumSections.findIndex(s => s.title === section)
  const colors = ['#7c3aed', '#8b5cf6', '#a78bfa', '#c4b5fd']
  return colors[index % colors.length]
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  fetchPosts()
})
</script>

<style lang="scss" scoped>
.forum-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;

  .header-card {
    margin-bottom: 24px;
    background: linear-gradient(135deg, #67827c, #a9e9d6);
    border: none;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    :deep(.el-card__header) {
      padding: 30px 40px;
    }

    .forum-header {
      color: white;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .forum-title-area {
        h1 {
          margin: 0;
          font-size: 2.2rem;
          text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
        }

        .subtitle {
          margin: 10px 0 0;
          opacity: 0.9;
          font-size: 1.1rem;
        }
      }

      .stats-area {
        display: flex;
        gap: 24px;
        
        .stat-item {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .el-icon {
            font-size: 2rem;
            opacity: 0.9;
          }
          
          .stat-info {
            display: flex;
            flex-direction: column;
            
            .stat-count {
              font-size: 1.5rem;
              font-weight: bold;
            }
            
            .stat-label {
              font-size: 0.9rem;
              opacity: 0.9;
            }
          }
        }
      }
    }
  }

  .main-content {
    margin-top: 20px;

    .post-column {
      margin-bottom: 24px;
    }

    .filter-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      flex-wrap: wrap;
      gap: 12px;
      
      .filter-controls {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        
        .el-select {
          width: 140px;
        }
        
        .search-input {
          width: 200px;
        }
      }
    }
  }

  .post-list {
    .post-card {
      margin-bottom: 16px;
      transition: all 0.3s;
      cursor: pointer;
      border: none;
      border-radius: 8px;
      overflow: hidden;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
      }

      &.active {
        border-left: 4px solid #7c3aed;
        background-color: #f8faf9;
      }

      .post-header {
        display: flex;
        align-items: center;

        .section-tag {
          margin-right: 12px;
          border: none;
          border-radius: 4px;
        }

        .post-title {
          margin: 0;
          color: #333333 !important;
          font-weight: 600;
        }
      }

      .post-content {
        .post-excerpt {
          color: #333333 !important;
          line-height: 1.6;
          margin: 12px 0;
        }

        .post-meta {
          display: flex;
          gap: 20px;
          color: #666666 !important;
          font-size: 0.9em;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 6px;

            .el-avatar {
              margin-right: 6px;
              border: 2px solid #f0f0f0;
            }
          }
        }
      }
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
  
  .trending-card, .user-card {
    margin-bottom: 20px;
    border-radius: 8px;
    overflow: hidden;
    
    .trending-header, .user-header {
      display: flex;
      align-items: center;
      
      h3 {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 1.1rem;
      }
    }
    
    .trending-list {
      .trending-item {
        display: flex;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: all 0.2s;
        
        &:hover {
          background-color: #f9f9f9;
        }
        
        &:last-child {
          border-bottom: none;
        }
        
        .trending-rank {
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: #7c3aed;
          color: white;
          border-radius: 50%;
          font-weight: bold;
          font-size: 0.8rem;
          margin-right: 12px;
        }
        
        .trending-content {
          flex: 1;
          
          .trending-title {
            font-weight: 500;
            margin-bottom: 4px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .trending-meta {
            display: flex;
            justify-content: space-between;
            font-size: 0.8rem;
            color: #999;
          }
        }
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      padding: 16px 0;
      
      .el-avatar {
        border: 3px solid #f0f0f0;
        margin-right: 16px;
      }
      
      .user-details {
        flex: 1;
        
        .user-name {
          font-size: 1.2rem;
          font-weight: 600;
          margin-bottom: 8px;
        }
        
        .user-stats {
          display: flex;
          gap: 24px;
          
          .stat {
            text-align: center;
            
            .stat-value {
              font-size: 1.1rem;
              font-weight: 600;
              color: #7c3aed;
            }
            
            .stat-label {
              font-size: 0.8rem;
              color: #999;
            }
          }
        }
      }
    }
  }

  .detail-card {
    position: sticky;
    top: 20px;
    max-height: calc(100vh - 120px);
    overflow-y: auto;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .post-detail {
      .detail-title {
        color: #333333 !important;
        margin: 0 0 16px;
      }

      .detail-meta {
        display: flex;
        align-items: center;
        gap: 12px;

        .meta-info {
          display: flex;
          flex-direction: column;

          .author {
            font-weight: 500;
          }

          .time {
            color: #999;
            font-size: 0.9em;
          }
        }
      }

      .detail-content {
        line-height: 1.8;
        color: #333333 !important;
        white-space: pre-wrap;
        max-height: 300px;
        overflow-y: auto;
      }
    }

    .comment-section {
      margin-top: 32px;

      .comment-title {
        color: #333333 !important;
        margin-bottom: 20px;
      }

      .comment-list {
        .comment-item {
          display: flex;
          gap: 12px;
          padding: 16px 0;
          border-bottom: 1px solid #eee;

          .comment-content {
            flex: 1;

            .comment-header {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 6px;

              .author {
                font-weight: 500;
                color: #333333 !important;
              }

              .time {
                color: #999;
                font-size: 0.8em;
              }
            }

            .comment-text {
              margin: 0;
              color: #333333 !important;
              line-height: 1.6;
            }
          }
        }
      }

      .comment-editor {
        margin-top: 24px;

        .editor-actions {
          margin-top: 12px;
          text-align: right;
        }
      }
    }
  }

  .empty-card {
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.post-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }

  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-right: 20px;
  }

  .dialog-content {
    display: flex;
    flex-direction: column;
    height: 70vh;
    background-color: #ffffff;
    overflow-y: auto;
    padding: 20px;
  }

  .post-content {
    .post-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .meta-info {
        display: flex;
        flex-direction: column;
        
        .author {
          font-weight: 500;
          color: #333333 !important;
        }
        
        .time {
          color: #999;
          font-size: 0.9em;
        }
      }
    }

    .content-text {
      line-height: 1.8;
      color: #333333 !important;
      white-space: pre-wrap;
    }
  }

  .comment-wrapper {
    margin-top: 24px;
    flex: 1;
    display: flex;
    flex-direction: column;

    .comment-title {
      color: #333333 !important;
      margin: 0 0 16px;
    }

    .comment-list {
      flex: 1;
      margin-bottom: 20px;

      .comment-item {
        display: flex;
        gap: 12px;
        padding: 12px 0;
        border-bottom: 1px solid #eee;

        &:last-child {
          border-bottom: none;
        }

        .comment-content {
          flex: 1;

          .comment-header {
            display: flex;
            gap: 8px;
            align-items: center;
            margin-bottom: 4px;

            .author {
              font-weight: 500;
              color: #333333 !important;
            }

            .time {
              color: #999;
              font-size: 0.8em;
            }
          }

          .comment-text {
            margin: 0;
            color: #333333 !important;
            line-height: 1.6;
          }
        }
      }
    }

    .comment-editor {
      border-top: 1px solid #eee;
      padding-top: 16px;

      .editor-actions {
        margin-top: 12px;
        text-align: right;
      }
    }
  }
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;

  .detail-btn {
    margin-left: auto;
    padding: 6px 12px;
    font-weight: 500;
  }
}

// 添加弹窗动画
:deep(.el-dialog) {
  transition: all 0.3s ease;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #eee;
}

// 过渡动画
.post-list-enter-active,
.post-list-leave-active {
  transition: all 0.3s ease;
}

.post-list-enter-from,
.post-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}

// 覆盖Element主题色
:deep(.el-button--primary) {
  background-color: #7c3aed !important;
  border-color: #7c3aed !important;
  color: #ffffff !important;
  font-weight: 500;

  &:hover {
    background-color: #8b5cf6 !important;
    border-color: #8b5cf6 !important;
    color: #ffffff !important;
  }
}

:deep(.el-button--text) {
  color: #7c3aed !important;
  font-weight: 500;

  &:hover {
    color: #8b5cf6 !important;
  }
}

:deep(.el-button--info) {
  color: #7c3aed !important;
  font-weight: 500;

  &:hover {
    color: #8b5cf6 !important;
  }
}

:deep(.el-button--default) {
  --el-button-text-color: #333333 !important;
  --el-button-border-color: #7c3aed !important;
  --el-button-bg-color: #ffffff !important;
  --el-button-hover-text-color: #333333 !important;
  --el-button-hover-border-color: #8b5cf6 !important;
  --el-button-hover-bg-color: #f5f3ff !important;
  font-weight: 500;
}

.editor-actions {
  margin-top: 12px;
  text-align: right;

  .el-button {
    font-weight: 500;
  }
}
</style>