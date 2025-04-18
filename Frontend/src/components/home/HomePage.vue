<template>
    <div class="homepage">
      <!-- 轮播图容器 -->
      <div class="carousel-container">
        <el-carousel :interval="5000" arrow="always" height="600px">
          <el-carousel-item v-for="(item, index) in carouselImages" :key="index">
            <img :src="item" alt="carousel image" class="carousel-image"/>
          </el-carousel-item>
        </el-carousel>
      </div>
  
      <!-- 首页欢迎语 -->
      <div class="welcome-message">
        <h1>
          <img src="/src/assets/logo/logo/信息.png" alt="carousel image" class="logo-image"/>
          欢迎来到网络攻防安全学习平台
        </h1>
        <p>从这里开始，探索网络安全的世界</p>
      </div> 

      <!-- 特色功能区块 -->
      <div class="features-section">
        <div class="feature-card">
          <el-icon><Monitor /></el-icon>
          <h3>实时攻防演练</h3>
          <p>提供真实的攻防环境，让学习更贴近实战</p>
        </div>
        <div class="feature-card">
          <el-icon><Reading /></el-icon>
          <h3>系统化课程</h3>
          <p>从入门到进阶的完整学习体系</p>
        </div>
        <div class="feature-card">
          <el-icon><Trophy /></el-icon>
          <h3>技能认证</h3>
          <p>完成课程可获得专业认证证书</p>
        </div>
        <div class="feature-card">
          <el-icon><User /></el-icon>
          <h3>专家指导</h3>
          <p>行业专家一对一指导，答疑解惑</p>
        </div>
      </div>

      <!-- 热门课程 -->
      <div class="courses-section">
        <h2>热门课程</h2>
        <div class="course-grid">
          <div class="course-card" v-for="(course, index) in hotCourses" :key="index">
            <img :src="course.image" :alt="course.title">
            <div class="course-info">
              <h3>{{ course.title }}</h3>
              <p>{{ course.description }}</p>
              <div class="course-meta">
                <span>{{ course.level }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 学习路径 -->
      <div class="learning-path">
        <h2>学习路径</h2>
        <div class="path-timeline">
          <div class="path-step" v-for="(step, index) in learningPath" :key="index">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <h3>{{ step.title }}</h3>
              <p>{{ step.description }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 最新动态 -->
      <div class="news-section">
        <h2>最新动态</h2>
        <div class="news-grid">
          <div class="news-card" v-for="(news, index) in latestNews" :key="index">
            <div class="news-date">{{ news.date }}</div>
            <h3>{{ news.title }}</h3>
            <p>{{ news.summary }}</p>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { Monitor, Reading, Trophy, User } from '@element-plus/icons-vue'
  
// 使用 import 导入图片
import homePage1 from '@/assets/homepage2.jpg';
import homePage2 from '@/assets/homepage1.jpg';
import homePage3 from '@/assets/homepage3.jpg';
import homePage4 from '@/assets/homepage4.jpg';
import homePage5 from '@/assets/homepage5.jpg';

const carouselImages = ref([homePage1, homePage2, homePage3,homePage4,homePage5]);

// 热门课程数据
const hotCourses = ref([
  {
    title: 'Web安全基础',
    description: '学习Web应用安全基础知识，掌握常见漏洞原理与防御方法',
    image: homePage1,
    students: 1234,
    level: '入门'
  },
  {
    title: '渗透测试实战',
    description: '系统学习渗透测试方法论，掌握各类工具的使用技巧',
    image: homePage2,
    students: 856,
    level: '进阶'
  },
  {
    title: '安全开发实践',
    description: '学习安全开发规范，掌握代码审计与漏洞修复技术',
    image: homePage3,
    students: 567,
    level: '高级'
  }
]);

// 学习路径数据
const learningPath = ref([
  {
    title: '基础知识',
    description: '计算机网络、操作系统、编程语言等基础知识'
  },
  {
    title: '安全基础',
    description: '密码学、安全协议、安全架构等安全基础知识'
  },
  {
    title: '攻防技术',
    description: '渗透测试、漏洞利用、安全防护等实战技能'
  },
  {
    title: '高级主题',
    description: '安全开发、安全运维、应急响应等专业领域'
  }
]);

// 最新动态数据
const latestNews = ref([
  {
    date: '2024-03-20',
    title: '平台新增CTF训练营',
    summary: '为提升实战能力，平台新增CTF训练营，包含Web、PWN、Crypto等多个方向'
  },
  {
    date: '2024-03-18',
    title: '安全漏洞预警',
    summary: '最新安全漏洞分析与防御方案发布'
  },
  {
    date: '2024-03-15',
    title: 'AI智能答疑',
    summary: 'AI智能助手在线答疑解惑'
  }
]);
  </script>
  
  <style scoped>
  .homepage {
  font-family: 'Arial', sans-serif;
  color: #fff;
  min-height: calc(100vh - 60px);
  position: relative;
  padding: 20px;
}

.carousel-container {
  width: 100%;
  height: 600px;
  margin-top: 10px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.welcome-message {
  text-align: center;
  margin-top: -60px;
  position: relative;
  z-index: 2;
  background: rgba(0, 0, 0, 0.5);
  padding: 20px;
  border-radius: 10px;
  backdrop-filter: blur(5px);
}

.welcome-message h1 {
  font-size: 3rem;
  font-weight: 700;
  color: #d0ffdc;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.logo-image {
  width: 90px;
  height: auto;
  margin: 0;
  position: static;
}
  
  .carousel-container {
    width: 100%;
    height: 600px; /* 高度可根据需要调整 */
    margin-top: 10px;
    border-radius: 10px;
    overflow: hidden;
    object-fit: cover;
  }
  
  .carousel-image {
    width: 100%;
    height: 600px;
    object-fit: cover;
  }
  
  .welcome-message p {
    position: relative;
    top: 0px;
    left: 40px;
    font-size: 1.5rem;
    color: #ffffff;
    margin-bottom: 20px;
  }

  .features-section {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin: 40px 0;
    padding: 0 20px;
  }

  .feature-card {
    background: rgba(0, 0, 0, 0.6);
    padding: 20px;
    border-radius: 10px;
    text-align: center;
    transition: transform 0.3s;
  }

  .feature-card:hover {
    transform: translateY(-5px);
  }

  .feature-card .el-icon {
    font-size: 40px;
    color: #d0ffdc;
    margin-bottom: 15px;
  }

  .feature-card h3 {
    color: #d0ffdc;
    margin: 10px 0;
  }

  .feature-card p {
    color: #ffffff;
    font-size: 0.9rem;
  }

  .courses-section, .learning-path, .news-section {
    margin: 40px 0;
    padding: 20px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 10px;
  }

  h2 {
    color: #d0ffdc;
    text-align: center;
    margin-bottom: 30px;
    font-size: 2rem;
  }

  .course-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .course-card {
    background: rgba(0, 0, 0, 0.4);
    border-radius: 8px;
    overflow: hidden;
    transition: transform 0.3s;
  }

  .course-card:hover {
    transform: translateY(-5px);
  }

  .course-card img {
    width: 100%;
    height: 160px;
    object-fit: cover;
  }

  .course-info {
    padding: 15px;
  }

  .course-info h3 {
    color: #d0ffdc;
    margin-bottom: 10px;
  }

  .course-info p {
    color: #ffffff;
    font-size: 0.9rem;
    margin-bottom: 10px;
  }

  .course-meta {
    display: flex;
    justify-content: space-between;
    color: #a0a0a0;
    font-size: 0.8rem;
  }

  .path-timeline {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .path-step {
    display: flex;
    align-items: flex-start;
    gap: 20px;
  }

  .step-number {
    width: 40px;
    height: 40px;
    background: #d0ffdc;
    color: #000;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
  }

  .step-content {
    flex: 1;
  }

  .step-content h3 {
    color: #d0ffdc;
    margin-bottom: 5px;
  }

  .step-content p {
    color: #ffffff;
  }

  .news-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .news-card {
    background: rgba(0, 0, 0, 0.4);
    padding: 20px;
    border-radius: 8px;
  }

  .news-date {
    color: #a0a0a0;
    font-size: 0.8rem;
    margin-bottom: 10px;
  }

  .news-card h3 {
    color: #d0ffdc;
    margin-bottom: 10px;
  }

  .news-card p {
    color: #ffffff;
    font-size: 0.9rem;
  }
  </style>
  