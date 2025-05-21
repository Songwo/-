<template>
  <div class="challenge-container">
    <!-- 标题栏 - 整合VIP切换功能 -->
    <div class="header">
      <div class="title">网络安全靶场</div>
      <div class="header-buttons">
        <el-button type="text" class="switch-btn" @click="handleSwitchView">
          {{ viewMode === 'normal' ? '切换到VIP版' : '切换到普通版' }}
        </el-button>
        <el-button class="tutorial-button" @click="showTutorial">
          <el-icon><Guide /></el-icon>
          查看教程
        </el-button>
      </div>
    </div>

    <!-- 非VIP版本 -->
    <template v-if="viewMode === 'normal'">
      <div class="normal-version">
        <!-- 顶部数据统计 -->
        <el-row :gutter="16" class="dashboard-stats">
          <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="(stat, index) in dashboardStats" :key="index">
            <div class="stat-card">
              <div class="stat-icon"><el-icon><component :is="stat.icon" /></el-icon></div>
              <div class="stat-content">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 挑战列表 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card v-for="(challenge, index) in currentChallenges" :key="challenge.id" class="challenge-card">
              <template #header>
                <div class="card-header">
                  <div class="title-section">
                    <h2>{{ challenge.title }}</h2>
                    <el-tag :type="getDifficultyType(challenge.difficulty)" class="difficulty-tag">
                      ★{{ challenge.difficulty }}
                    </el-tag>
                  </div>
                </div>
              </template>

              <div class="card-content">
                <el-row :gutter="20">
                  <el-col :span="16">
                    <div class="left-content">
                      <div class="task-section">
                        <h3>
                          <el-icon><Flag /></el-icon>
                          任务目标
                        </h3>
                        <p>{{ challenge.task }}</p>
                      </div>
                    </div>
                  </el-col>

                  <el-col :span="8">
                    <div class="right-content">
                      <div class="action-section">
                        <div class="action-buttons">
                          <el-button type="primary" size="large" @click="startLab(challenge)" 
                            :loading="challenge.loading"
                            :disabled="challenge.disabled || !challenge.unlocked || challenge.completed || isAnyLabRunning() && !challenge.labUrl">
                            启动靶场
                          </el-button>
                          <el-button type="success" size="large" @click="verifyFlag(challenge)"
                            :disabled="!challenge.labUrl || challenge.completed">
                            验证FLAG
                          </el-button>
                        </div>

                        <div v-if="challenge.loading" class="startup-progress">
                          <el-progress 
                            :percentage="challenge.startProgress"
                            :status="challenge.startProgress === 100 ? 'success' : ''"
                            :stroke-width="20"
                            :show-text="true">
                            <template #default="{ percentage }">
                              <span class="progress-text">
                                {{ percentage < 100 ? '启动中...' : '启动完成' }}
                                {{ percentage }}%
                              </span>
                            </template>
                          </el-progress>
                        </div>

                        <div v-if="challenge.labUrl" class="lab-info">
                          <el-link :href="challenge.labUrl" target="_blank" type="primary" class="lab-link">
                            前往靶场
                          </el-link>
                          <span v-if="runningLabInfo && runningLabInfo.challengeId === challenge.id" class="timer">
                            (剩余时间: {{ Math.floor(runningLabInfo.remainingSeconds / 1000 / 60) }}分{{
                              Math.floor((runningLabInfo.remainingSeconds / 1000) % 60)
                            }}秒)
                          </span>
                        </div>

                        <div v-if="challenge.completed" class="completion-section">
                          <el-alert type="success" description="挑战通关！" show-icon />
                          <div class="completion-details">
                            <p>完成时间：{{ formatTime(challenge.completionTime) }}</p>
                            <p>得分：{{ challenge.score }}/100</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- VIP提示 -->
        <el-card class="vip-notice">
          <div class="vip-notice-content">
            <el-icon class="vip-icon"><Star /></el-icon>
            <div class="vip-text">
              <h3>升级VIP，享受独立靶场环境</h3>
              <p>VIP用户可享受独立靶场环境，避免多人同时使用导致的干扰，提供更稳定的学习体验。</p>
            </div>
            <el-button type="warning" @click="handleSwitchView">立即升级</el-button>
          </div>
        </el-card>
      </div>
    </template>

    <!-- VIP版本 -->
    <template v-else>
      <!-- 原有的VIP版本内容 -->
      <div class="cyber-security-dashboard">
        <!-- 顶部数据统计 - 两行四列 -->
        <el-row :gutter="16" class="dashboard-stats">
          <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="(stat, index) in dashboardStats" :key="index">
            <div class="stat-card">
              <div class="stat-icon"><el-icon><component :is="stat.icon" /></el-icon></div>
              <div class="stat-content">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 挑战分类选择 -->
        <div class="el-tabs el-tabs--top">
          <div class="el-tabs__header is-top">
            <div class="el-tabs__nav-wrap is-top">
              <div class="el-tabs__nav-scroll">
                <div class="el-tabs__nav is-top" role="tablist">
                  <div class="el-tabs__active-bar is-top"></div>
                  <div 
                    v-for="category in categories" 
                    :key="category.key"
                    :class="['el-tabs__item is-top', { 
                      'is-active': currentCategoryKey === category.key,
                      'is-disabled': !category.unlocked
                    }]"
                    :id="'tab-' + category.key"
                    :aria-controls="'pane-' + category.key"
                    role="tab"
                    :aria-selected="currentCategoryKey === category.key"
                    :tabindex="currentCategoryKey === category.key ? '0' : '-1'"
                    @click="category.unlocked && handleCategorySelect(category.key)"
                  >
                    {{ category.name }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <el-row :gutter="16">
          <!-- 主要内容区 - 挑战列表 -->
          <el-col :xs="24" :sm="24" :md="16" :lg="16">
            <challenge-list 
              :challenges="currentChallenges" 
              :categories="categories"
              :is-VIP="isVipUser"
              :running-lab-info="runningLabInfo"
              @start-challenge="startLab"
              @verify-flag="verifyFlag"
            />
          </el-col>
          
          <!-- 右侧边栏 -->
          <el-col :xs="24" :sm="24" :md="8" :lg="8">
            <!-- 学习进度卡片 -->
            <el-card class="sidebar-card learning-progress-card">
              <template #header>
                <div class="card-header">
                  <h3>
                    <el-icon><TrendCharts /></el-icon>
                    学习进度
                  </h3>
                </div>
              </template>
              <div class="progress-section">
                <div class="progress-header">
                  <el-tooltip
                    content="总体完成率，计算所有难度挑战的总体完成情况"
                    placement="top"
                    effect="light"
                  >
                    <span class="progress-info">总体进度</span>
                  </el-tooltip>
                </div>
                <el-progress 
                  :percentage="getOverallProgress()" 
                  :format="format => `${format}%`"
                  :stroke-width="20"
                  class="overall-progress"
                ></el-progress>
              </div>
              <div class="category-progress">
                <div class="category-item">
                  <div class="progress-header">
                    <span>{{ getCurrentCategoryName() }} </span>
                    <el-tooltip placement="top" effect="light">
                      <template #content>
                        <div class="progress-tooltip">
                          <div v-for="category in categories" :key="category.key" class="tooltip-category">
                            <div>{{ category.name }}: {{ getCategoryProgress(category) }}%</div>
                            <div class="mini-progress">
                              <div class="mini-progress-inner" 
                                  :style="{width: getCategoryProgress(category) + '%', 
                                  background: getCategoryProgressColor(category)}"></div>
                            </div>
                          </div>
                        </div>
                      </template>
                      <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                  </div>
                  <el-progress 
                    :percentage="getCurrentCategoryProgress()" 
                    :stroke-width="12"
                    :status="getCurrentCategoryStatus()"
                    class="category-progress-bar"
                  ></el-progress>
                </div>
              </div>
            </el-card>
            
            <!-- 安全小贴士 -->
            <el-card class="sidebar-card security-tips-card">
              <template #header>
                <div class="card-header">
                  <h3>
                    <el-icon><InfoFilled /></el-icon>
                    安全小贴士
                  </h3>
                </div>
              </template>
              <el-carousel height="150px" :interval="5000" indicator-position="none" arrow="never">
                <el-carousel-item v-for="(tip, index) in securityTips" :key="index">
                  <div class="tip-item">
                    <h4>{{ tip.title }}</h4>
                    <p>{{ tip.content }}</p>
                  </div>
                </el-carousel-item>
              </el-carousel>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </template>

    <!-- 教程对话框 -->
    <el-dialog
      v-model="showTutorialDialog"
      title="靶场教程"
      width="60%"
      class="tutorial-dialog"
    >
      <div class="tutorial-content">
        <el-carousel
          ref="tutorialCarousel"
          :interval="0"
          :autoplay="false"
          indicator-position="none"
          height="400px"
          class="tutorial-carousel"
        >
          <el-carousel-item v-for="(step, index) in tutorialSteps" :key="index">
            <div class="tutorial-step">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-info">
                <h3>{{ step.title }}</h3>
                <p>{{ step.description }}</p>
                <div class="step-image" @click="handleImageClick(step.image)">
                  <el-image 
                    :src="step.image" 
                    fit="cover" 
                    style="width: 100%; max-width: 400px; border-radius: 8px; margin-top: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.3); cursor: pointer;"
                  />
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
        
        <div class="tutorial-navigation">
          <el-button 
            :disabled="currentStep === 0"
            @click="prevStep"
            class="nav-button"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          
          <div class="step-indicators">
            <div 
              v-for="(step, index) in tutorialSteps" 
              :key="index"
              :class="['step-dot', { active: currentStep === index }]"
              @click="goToStep(index)"
            ></div>
          </div>
          
          <el-button 
            :disabled="currentStep === tutorialSteps.length - 1"
            @click="nextStep"
            class="nav-button"
          >
            下一步
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- FLAG验证对话框 -->
    <el-dialog v-model="showFlagDialog" title="验证FLAG" width="30%" class="flag-dialog">
      <el-input v-model="inputFlag" placeholder="请输入FLAG" />
      <template #footer>
        <el-button @click="showFlagDialog = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="confirmVerify">确认</el-button>
      </template>
    </el-dialog>

    <!-- 添加图片预览对话框 -->
    <el-dialog
      v-model="showImagePreview"
      width="80%"
      class="image-preview-dialog"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="preview-container">
        <el-image 
          :src="currentPreviewImage" 
          fit="contain"
          style="width: 100%; height: 80vh;"
        />
      </div>
    </el-dialog>

    <!-- 在tips-section之后添加新的靶场实战功能区域 -->
    <el-row :gutter="20" class="interactive-features" v-if="viewMode === 'vip'">
      <el-col :span="24">
        <h2 class="section-title">
          <el-icon><Cpu /></el-icon>
          靶场实战创新功能
        </h2>
        
        <!-- 靶场特色功能展示 -->
        <el-card class="feature-cards">
          <el-row :gutter="20">
            <!-- 攻击链分析功能 -->
            <el-col :span="6">
              <div class="feature-card">
                <div class="feature-icon"><el-icon><Link /></el-icon></div>
                <h3>攻击链分析</h3>
                <p>可视化展示漏洞利用全过程，了解从发现到利用的完整攻击链路</p>
                <div class="feature-footer">
                  <el-tag type="danger">高级特性</el-tag>
                  <el-button type="primary" plain size="small">体验功能</el-button>
  </div>
              </div>
            </el-col>
            
            <!-- 靶场录像回放 -->
            <el-col :span="6">
              <div class="feature-card">
                <div class="feature-icon"><el-icon><VideoCamera /></el-icon></div>
                <h3>靶场录像回放</h3>
                <p>记录所有操作过程，支持慢速回放和关键点标记，方便事后学习分析</p>
                <div class="feature-footer">
                  <el-tag type="success">新功能</el-tag>
                  <el-button type="primary" plain size="small">开始录制</el-button>
                </div>
              </div>
            </el-col>
            
            <!-- 漏洞评分系统 -->
            <el-col :span="6">
              <div class="feature-card">
                <div class="feature-icon"><el-icon><DataAnalysis /></el-icon></div>
                <h3>漏洞评分系统</h3>
                <p>基于CVSS评分标准，学习真实漏洞评估方法，提高威胁分析能力</p>
                <div class="feature-footer">
                  <el-tag type="warning">进阶学习</el-tag>
                  <el-button type="primary" plain size="small">评估漏洞</el-button>
                </div>
              </div>
            </el-col>
            
            <!-- 团队协作功能 -->
            <el-col :span="6">
              <div class="feature-card">
                <div class="feature-icon"><el-icon><Service /></el-icon></div>
                <h3>团队协作靶场</h3>
                <p>邀请好友共同攻破复杂靶场，模拟红蓝对抗，体验真实渗透测试团队协作</p>
                <div class="feature-footer">
                  <el-tag type="info">即将上线</el-tag>
                  <el-button type="primary" plain size="small">创建团队</el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 非VIP用户看到的功能预览 -->
    <el-row :gutter="20" class="interactive-features" v-else>
      <el-col :span="24">
        <h2 class="section-title">
          <el-icon><Cpu /></el-icon>
          靶场实战创新功能
        </h2>
        
        <el-card class="feature-preview">
          <div class="preview-content">
            <div class="preview-header">
              <el-icon class="preview-icon"><Lock /></el-icon>
              <h3>升级VIP，解锁更多高级功能</h3>
            </div>
            <div class="preview-features">
              <div class="preview-feature-item">
                <el-icon><Link /></el-icon>
                <span>攻击链分析</span>
              </div>
              <div class="preview-feature-item">
                <el-icon><VideoCamera /></el-icon>
                <span>靶场录像回放</span>
              </div>
              <div class="preview-feature-item">
                <el-icon><DataAnalysis /></el-icon>
                <span>漏洞评分系统</span>
              </div>
              <div class="preview-feature-item">
                <el-icon><Service /></el-icon>
                <span>团队协作靶场</span>
              </div>
            </div>
            <el-button type="warning" @click="handleSwitchView">立即升级VIP</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加靶场实战进阶功能区域 -->
    <el-row :gutter="20" class="advanced-training" v-if="viewMode === 'vip'">
      <el-col :span="12">
        <h2 class="section-title">
          <el-icon><Opportunity /></el-icon>
          漏洞实验室
        </h2>
        <el-card class="lab-card">
          <el-tabs tab-position="left">
            <el-tab-pane label="实时漏洞库">
              <div class="lab-content">
                <div class="lab-header">
                  <h3>最新安全漏洞</h3>
                  <el-button type="primary" size="small">查看全部</el-button>
                </div>
                
                <el-table :data="recentVulnerabilities" stripe style="width: 100%">
                  <el-table-column prop="cveId" label="CVE编号" width="130" />
                  <el-table-column prop="title" label="漏洞名称" />
                  <el-table-column prop="severity" label="危害级别" width="100">
                    <template #default="scope">
                      <el-tag :type="getSeverityType(scope.row.severity)">{{ scope.row.severity }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default>
                      <el-button link type="primary" size="small">开始复现</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>
            <el-tab-pane label="漏洞分析工具">
              <div class="lab-content">
                <div class="tools-grid">
                  <div class="tool-box">
                    <i class="tool-icon el-icon"><el-icon><Connection /></el-icon></i>
                    <h4>网络流量分析</h4>
                    <p>分析网络数据包，发现异常通信</p>
                  </div>
                  <div class="tool-box">
                    <i class="tool-icon el-icon"><el-icon><Cherry /></el-icon></i>
                    <h4>代码审计工具</h4>
                    <p>静态分析源代码中的安全缺陷</p>
                  </div>
                  <div class="tool-box">
                    <i class="tool-icon el-icon"><el-icon><Histogram /></el-icon></i>
                    <h4>内存取证分析</h4>
                    <p>检查内存转储文件查找恶意行为</p>
                  </div>
                  <div class="tool-box">
                    <i class="tool-icon el-icon"><el-icon><Search /></el-icon></i>
                    <h4>漏洞扫描器</h4>
                    <p>自动化发现系统中的安全漏洞</p>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="漏洞利用步骤">
              <div class="steps-container">
                <h3>漏洞利用通用流程</h3>
                <el-steps direction="vertical" :active="1">
                  <el-step title="信息收集" description="获取目标系统基本信息，识别操作系统类型、开放端口和服务版本" />
                  <el-step title="漏洞发现" description="使用漏洞扫描工具或手动测试发现潜在安全漏洞" />
                  <el-step title="漏洞分析" description="研究漏洞原理，确定利用可行性和潜在影响" />
                  <el-step title="漏洞利用" description="使用合适的利用工具或技术触发漏洞，获取系统访问权限" />
                  <el-step title="权限提升" description="从初始访问点扩大控制范围，提升系统权限" />
                  <el-step title="持久化" description="建立持久访问机制，确保后续可以重新获取系统访问" />
                  <el-step title="痕迹清理" description="移除攻击痕迹，避免被检测系统发现" />
                </el-steps>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <h2 class="section-title">
          <el-icon><School /></el-icon>
          真实案例实战
        </h2>
        <el-card class="case-study-card">
          <div class="case-header">
            <div>
              <h3>近期热门安全案例</h3>
              <p>基于真实漏洞事件的靶场环境，实践实战技能</p>
            </div>
            <el-radio-group v-model="caseFilter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="web">Web安全</el-radio-button>
              <el-radio-button label="api">API安全</el-radio-button>
              <el-radio-button label="cloud">云安全</el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="case-list">
            <div class="case-item" v-for="(item, index) in realWorldCases" :key="index">
              <div class="case-difficulty">{{ item.difficulty }}</div>
              <div class="case-info">
                <h4>{{ item.title }}</h4>
                <p>{{ item.description }}</p>
                <div class="case-tags">
                  <el-tag size="small" v-for="(tag, idx) in item.tags" :key="idx" :type="getTagType(tag)">{{ tag }}</el-tag>
                </div>
              </div>
              <div class="case-actions">
                <el-button type="success" size="small">开始实战</el-button>
                <el-button type="info" plain size="small">查看详情</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 非VIP用户看到的进阶功能预览 -->
    <el-row :gutter="20" class="advanced-training" v-else>
      <el-col :span="24">
        <h2 class="section-title">
          <el-icon><Opportunity /></el-icon>
          进阶学习功能
        </h2>
        <el-card class="advanced-preview">
          <div class="preview-content">
            <div class="preview-header">
              <el-icon class="preview-icon"><Lock /></el-icon>
              <h3>VIP专享进阶学习功能</h3>
            </div>
            <div class="preview-features">
              <div class="preview-feature-item">
                <el-icon><Opportunity /></el-icon>
                <div class="feature-info">
                  <h4>漏洞实验室</h4>
                  <p>实时漏洞库、漏洞分析工具、漏洞利用步骤详解</p>
                </div>
              </div>
              <div class="preview-feature-item">
                <el-icon><School /></el-icon>
                <div class="feature-info">
                  <h4>真实案例实战</h4>
                  <p>基于真实漏洞事件的靶场环境，实践实战技能</p>
                </div>
              </div>
            </div>
            <el-button type="warning" @click="handleSwitchView">升级VIP，开启进阶学习</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加社区互动板块 -->
    <el-row :gutter="20" class="community-section">
      <el-col :span="24">
        <h2 class="section-title">
          <el-icon><ChatDotRound /></el-icon>
          安全社区
        </h2>
        <el-card class="community-card">
          <el-tabs>
            <el-tab-pane label="靶场解题思路">
              <div class="solution-grid">
                <div class="solution-item" v-for="(solution, index) in communitySolutions" :key="index">
                  <div class="solution-avatar">
                    <el-avatar :size="50" :src="solution.avatar"></el-avatar>
                  </div>
                  <div class="solution-content">
                    <div class="solution-header">
                      <h4>{{ solution.challenge }}</h4>
                      <div class="solution-meta">
                        <span><el-icon><User /></el-icon> {{ solution.author }}</span>
                        <span><el-icon><Timer /></el-icon> {{ solution.date }}</span>
                        <span><el-icon><View /></el-icon> {{ solution.views }}</span>
                      </div>
                    </div>
                    <p>{{ solution.summary }}</p>
                    <div class="solution-footer">
                      <el-link type="primary" :underline="false">查看完整思路</el-link>
                      <div class="solution-likes">
                        <el-button size="small" plain circle><el-icon><Star /></el-icon></el-button>
                        <span>{{ solution.likes }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="学习小组">
              <div class="study-groups">
                <el-empty v-if="!isVipUser" description="升级VIP后可参与学习小组">
                  <el-button type="primary">立即升级</el-button>
                </el-empty>
                <div v-else class="group-list">
                  <div class="group-item" v-for="(group, index) in studyGroups" :key="index">
                    <div class="group-avatar">
                      <el-avatar :size="60" :src="group.avatar"></el-avatar>
                    </div>
                    <div class="group-info">
                      <h4>{{ group.name }}</h4>
                      <p>{{ group.description }}</p>
                      <div class="group-stats">
                        <span><el-icon><User /></el-icon> {{ group.members }}人</span>
                        <span><el-icon><Pointer /></el-icon> {{ group.level }}</span>
                      </div>
                    </div>
                    <div class="group-action">
                      <el-button type="primary" size="small">加入小组</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="寻找队友">
              <div class="teammates-finder">
                <el-empty v-if="!isVipUser" description="升级VIP后可寻找队友">
                  <el-button type="primary">立即升级</el-button>
                </el-empty>
                <div v-else class="teammate-search">
                  <div class="search-filters">
                    <el-form :inline="true">
                      <el-form-item label="技能方向">
                        <el-select v-model="teammateSkill" placeholder="选择技能方向">
                          <el-option label="Web渗透" value="web"></el-option>
                          <el-option label="逆向工程" value="reverse"></el-option>
                          <el-option label="二进制利用" value="pwn"></el-option>
                          <el-option label="密码学" value="crypto"></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item label="经验水平">
                        <el-select v-model="teammateLevel" placeholder="选择经验水平">
                          <el-option label="入门" value="beginner"></el-option>
                          <el-option label="中级" value="intermediate"></el-option>
                          <el-option label="高级" value="advanced"></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item>
                        <el-button type="primary">搜索</el-button>
                      </el-form-item>
                    </el-form>
                  </div>
                  
                  <div class="teammate-results">
                    <div class="teammate-card" v-for="(teammate, index) in potentialTeammates" :key="index">
                      <el-avatar :size="80" :src="teammate.avatar"></el-avatar>
                      <h4>{{ teammate.name }}</h4>
                      <div class="teammate-skill-tags">
                        <el-tag size="small" v-for="(skill, idx) in teammate.skills" :key="idx">{{ skill }}</el-tag>
                      </div>
                      <p>{{ teammate.bio }}</p>
                      <el-button type="primary" size="small">发送邀请</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import ToUrl from '@/api/api'
import { Lock, Unlock, Check, Loading, Document, Flag, Guide, ArrowLeft, ArrowRight, 
  VideoPlay, Trophy, Connection, TrendCharts, Tools, Reading, Monitor, 
  User, Clock, Medal, Timer, Rank, Aim, QuestionFilled, 
  Cpu, VideoCamera, DataAnalysis, Service, Opportunity, 
  School, ChatDotRound, Link, Cherry, Histogram, Search, 
  View, Star, Pointer, InfoFilled } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import gsap from 'gsap'
import { useRouter } from 'vue-router'

// 导入漏洞组件
import SQLInjection from './hole_coms/SQLInjection.vue'
import XSS from './hole_coms/XSS.vue'
import SSRF from './hole_coms/SSRF.vue'
import Log4Shell from './hole_coms/Log4Shell.vue'

// 导入新组件并使用
import ChallengeList from './components/ChallengeList.vue'

const store = useStore();
const router = useRouter()

// 添加缺失的响应式变量
const loading = ref(false)
const startProgress = ref(0)

// 挑战数据（每个分类内包含关卡数组）
const categories = ref([])
// 添加静态靶场漏洞数据
const staticChallenges = [
  {
    id: "oregret/shared-lab:latest",
    title: 'SQL注入基础',
    description: '学习基本的SQL注入技术，通过构造特殊的SQL语句来获取数据库中的敏感信息。',
    difficulty: '简单',
    task: '通过SQL注入获取管理员密码',
    flag: 'FLAG{SQL_INJECTION_BASIC_2024}',
    score: 100,
    type: 'sql',
    unlocked: true,
    completed: false,
    labConfig: {
      images: {
        frontend: { image: 'sql-injection-frontend:latest', port: 80 },
        backend: { image: 'sql-injection-backend:latest', port: 3000 }
      },
      duration: 30
    },
    vulnSteps: [
      {
        title: '漏洞原理',
        content: 'SQL注入是一种常见的Web安全漏洞，攻击者可以通过构造特殊的SQL语句来获取数据库中的敏感信息。',
        icon: '🔍',
        visible: true
      },
      {
        title: '攻击流程',
        content: '1. 分析登录表单的SQL查询语句\n2. 构造SQL注入语句绕过登录验证\n3. 获取管理员账号的密码',
        icon: '⚡',
        visible: true
      },
      {
        title: '防御措施',
        content: '1. 使用参数化查询\n2. 对用户输入进行严格过滤\n3. 使用ORM框架',
        icon: '🛡️',
        visible: true
      }
    ]
  },
  {
    id: 'xss-basic',
    title: 'XSS跨站脚本',
    description: '学习XSS攻击的基本原理，通过注入恶意脚本来获取用户信息。',
    difficulty: '简单',
    task: '通过XSS攻击获取用户Cookie',
    flag: 'FLAG{XSS_BASIC_2024}',
    score: 100,
    type: 'xss',
    unlocked: true,
    completed: false,
    labConfig: {
      images: {
        frontend: { image: 'xss-frontend:latest', port: 80 },
        backend: { image: 'xss-backend:latest', port: 3000 }
      },
      duration: 30
    },
    vulnSteps: [
      {
        title: '漏洞原理',
        content: 'XSS是一种常见的Web安全漏洞，攻击者可以在网页中注入恶意脚本，当其他用户访问该页面时，恶意脚本会在用户的浏览器中执行。',
        icon: '🔍',
        visible: true
      },
      {
        title: '攻击流程',
        content: '1. 分析评论系统的输入过滤机制\n2. 构造XSS payload绕过过滤\n3. 获取其他用户的Cookie信息',
        icon: '⚡',
        visible: true
      },
      {
        title: '防御措施',
        content: '1. 对用户输入进行HTML转义\n2. 使用Content Security Policy\n3. 设置HttpOnly Cookie',
        icon: '🛡️',
        visible: true
      }
    ]
  }
];

// 修改获取挑战的方法
const fetchChallenges = async () => {
  try {
    if (isVipUser.value) {
      // VIP用户从后端获取动态数据
      const response = await axios.get(ToUrl.url + '/challenges', {
        headers: { 'Authorization': `Bearer ${store.state.token}` }
      });
      if (response.data && Array.isArray(response.data)) {
        categories.value = response.data;
      } else {
        console.error('Invalid response format:', response.data);
        ElMessage.error('获取挑战数据格式错误');
      }
    } else {
      // 普通用户使用静态数据
      categories.value = [
        {
          key: 'low',
          title: '初级挑战',
          challenges: staticChallenges
        }
      ];
    }
  } catch (error) {
    console.error('获取挑战失败:', error);
    ElMessage.error('获取挑战数据失败');
  }
};

const runningLabInfo = ref(null)
let countdownTimer = null

watch(
  () => runningLabInfo.value && runningLabInfo.value.remainingSeconds,
  (newVal, oldVal) => {
    if (countdownTimer) clearInterval(countdownTimer)
    if (runningLabInfo.value && runningLabInfo.value.remainingSeconds > 0) {
      countdownTimer = setInterval(() => {
        if (runningLabInfo.value.remainingSeconds > 0) {
          runningLabInfo.value.remainingSeconds -= 1000 // 毫秒为单位
        }
        if (runningLabInfo.value.remainingSeconds <= 0) {
          runningLabInfo.value.remainingSeconds = 0
          clearInterval(countdownTimer)
          ElMessage.warning('靶场已到期，请重新启动！')
          // 自动刷新靶场状态
          checkLabStatus()
        }
      }, 1000)
    }
  },
  { immediate: true }
)

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

const checkLabStatus = async () => {
  try {
    for (const category of categories.value) {
      for (const challenge of category.challenges) {
        const res = await axios.get(ToUrl.stadUrl, {
          params: {
            userId: store.state.id,
            challengeId: challenge.id
          }
        });
        console.log(res.data.running)
        console.log(res.data)
        if (res.data.running) {
          runningLabInfo.value = {
            challengeId: challenge.id,
            labUrl: res.data.labUrl,
            remainingSeconds: res.data.remaining
          };
          // 标记当前 challenge
          challenge.labUrl = res.data.labUrl;
          challenge.running = true;
          // 让其他 challenge 不可点击-
          categories.value.forEach(cat => {
            cat.challenges.forEach(ch => {
              if (ch.id !== challenge.id) ch.disabled = true;
            });
          });
          return; // 只允许一个靶场运行
        }
      }
    }
    // 如果没有运行中的靶场
    runningLabInfo.value = null;
    categories.value.forEach(cat => {
      cat.challenges.forEach(ch => ch.disabled = false);
    });
  } catch (e) {
    console.error('靶场状态获取失败', e);
  }
};

onMounted(async () => {
  await fetchChallenges();
  await checkLabStatus();
  // 添加动画效果
  animateStatCards();
  // 更新active-bar
  updateActiveBar();
});

// 映射后端 challenge 到前端 challenge
function mapChallenge(ch) {
  return {
    id: ch.id,
    title: ch.title,
    description: ch.description,
    difficulty: ch.difficulty,
    task: ch.task,
    flag: ch.flag,
    score: ch.score,
    type: ch.type, // 添加漏洞类型
    labConfig: {
      images: ch.images,
      duration: ch.durationMinutes
    },
    unlocked: ch.unlocked,
    completed: ch.completed,
    completionTime: ch.completionTime,
    labUrl: '',
    loading: false,
    startProgress: 0,
    showVulnDetails: true,
    vulnSteps: [
      {
        title: '漏洞原理',
        content: '详细解释漏洞的技术原理和成因',
        icon: '🔍',
        visible: true
      },
      {
        title: '攻击流程',
        content: '分步骤展示攻击者如何利用该漏洞',
        icon: '⚡',
        visible: true
      },
      {
        title: '防御措施',
        content: '介绍如何修复和预防该漏洞',
        icon: '🛡️',
        visible: true
      }
    ]
  }
}

// 当前选中的分类（默认低级）
const currentCategoryKey = ref('low')
const currentChallenges = computed(() => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  return category ? category.challenges : []
})

// 添加响应式变量
const showFlagDialog = ref(false)
const inputFlag = ref('')
let currentVerifyChallenge = ref(null)

// 修改验证方法
const verifyFlag = (challenge) => {
  currentVerifyChallenge.value = challenge
  showFlagDialog.value = true
}

//验证是否通关
const confirmVerify = async () => {
  if (!inputFlag.value || !currentVerifyChallenge.value) return
  // 获取当前挑战对象
  const challenge = currentVerifyChallenge.value
  challenge.loading = true

  try {
    const response = await axios.post(
      ToUrl.url + '/lab/flag',
      {
        userId: store.state.id,
        imageName: challenge.labConfig.images.frontend.image,
        flag: inputFlag.value
      },
      { headers: { 'Authorization': `Bearer ${store.state.token}` } }
    )

    if (response.data.code == 200) {
      challenge.completed = true;
      challenge.completionTime = new Date();

      store.commit('completeChallenge', {
        categoryKey: currentCategoryKey.value,
        challengeId: challenge.id,
        score: 100
      })
      ElMessage.success('验证成功！挑战通关')
      unlockNextChallenge(challenge)
    } else {
      ElMessage.error('FLAG验证失败，请重试')
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error('验证请求失败：' + error.message)
  } finally {
    challenge.loading = false
    showFlagDialog.value = false
    inputFlag.value = ''
  }
}

// 处理侧边菜单切换
const handleCategorySelect = (key) => {
  currentCategoryKey.value = key
  updateActiveBar()
}

// 更新active-bar的位置
const updateActiveBar = () => {
  setTimeout(() => {
    const activeTab = document.querySelector('.el-tabs__item.is-active')
    const activeBar = document.querySelector('.el-tabs__active-bar')
    if (activeTab && activeBar) {
      const { offsetWidth, offsetLeft } = activeTab
      activeBar.style.width = offsetWidth + 'px'
      activeBar.style.transform = `translateX(${offsetLeft}px)`
    }
  }, 0)
}

// 监听当前分类变化，更新active-bar
watch(() => currentCategoryKey.value, () => {
  updateActiveBar()
})

// 根据难度返回 el-tag 类型
const getDifficultyType = (difficulty) => {
  if (difficulty === 1) return 'success'
  if (difficulty === 2) return 'warning'
  if (difficulty === 3) return 'danger'
  return 'info'
}

// 返回解锁进度，如"1/3"
const getUnlockProgress = (challenge) => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  if (!category) return ''
  const index = category.challenges.findIndex(c => c.id === challenge.id)
  return `${index + 1}/${category.challenges.length}`
}

// 获取某个分类已完成挑战数量
const getCompletedInCategory = (category) => {
  return category.challenges.filter(c => c.completed).length
}

// 获取分类进度百分比
const getCategoryProgress = (category) => {
  if (category.challenges.length === 0) return 0
  const completed = getCompletedInCategory(category)
  return Math.round((completed / category.challenges.length) * 100)
}

// 获取分类状态
const getCategoryStatus = (category) => {
  const progress = getCategoryProgress(category)
  if (progress === 100) return 'success'
  if (progress > 0) return 'warning'
  if (!category.unlocked) return 'exception'
  return ''
}

// 获取总体进度
const getOverallProgress = () => {
  let totalChallenges = 0
  let completedChallenges = 0
  
  categories.value.forEach(cat => {
    totalChallenges += cat.challenges.length
    completedChallenges += getCompletedInCategory(cat)
  })
  
  return totalChallenges > 0 ? Math.round((completedChallenges / totalChallenges) * 100) : 0
}

// 启动靶场方法
const startLab = async (challenge) => {
  if (!store.getters.isVIP) {
    loading.value = true;
    startProgress.value = 0;
    
    // 创建进度条动画
    const progressAnimation = gsap.to(startProgress, {
      duration: 30,
      value: 100,
      ease: "none",
      onUpdate: () => {
        if (startProgress.value >= 100) {
          progressAnimation.kill();
        }
      }
    });

    try {
      const response = await axios.post(ToUrl.url+'/lab/shared', {
        userId: store.state.id,
        vulnType: challenge.id,
        durationMinutes: 30
      }, {
        headers: { 'Authorization': `Bearer ${store.state.token}` }
      });

      if (response.data && response.data.backendUrl) {
        // 存储后端URL到store
        store.commit('setBackendUrl', response.data.backendUrl);
        // 跳转到sql页面
        router.push('/sql');
      } else {
        ElMessage.error('启动靶场失败：未获取到后端URL');
      }
    } catch (error) {
      console.error('启动靶场失败:', error);
      ElMessage.error('启动靶场失败，请稍后重试');
    } finally {
      progressAnimation.kill();
      loading.value = false;
    }
  } else {
    // ... existing VIP user code ...
  }
};

// 解锁下一关：如果当前分类内还有下一关，则解锁下一关；否则解锁下个分类的第一关
const unlockNextChallenge = (currentChallenge) => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  if (!category) return

  const index = category.challenges.findIndex(c => c.id === currentChallenge.id)
  if (index < category.challenges.length - 1) {
    category.challenges[index + 1].unlocked = true
    ElNotification({
      title: '新关卡解锁',
      message: `${category.challenges[index + 1].title} 已解锁！`,
      type: 'success'
    })
  } else {
    // 当前分类全部完成，解锁下一分类
    const currentIndex = categories.value.findIndex(c => c.key === currentCategoryKey.value)
    if (currentIndex < categories.value.length - 1) {
      const nextCategory = categories.value[currentIndex + 1]
      nextCategory.unlocked = true
      if (nextCategory.challenges.length > 0) {
        nextCategory.challenges[0].unlocked = true
      }
      ElNotification({
        title: '新分类解锁',
        message: `${nextCategory.name} 已解锁！`,
        type: 'success'
      })
    }
  }
}

// 格式化时间显示
const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : 'N/A'
}

const isAnyLabRunning = () => {
  return categories.value.some(cat =>
    cat.challenges.some(ch => ch.labUrl && !ch.completed)
  )
}

// 获取教程步骤
const getTutorialSteps = (challenge) => {
  return [
    '了解漏洞的基本原理',
    '分析目标系统的安全机制',
    '尝试利用漏洞获取系统权限',
    '获取并提交正确的FLAG'
  ]
}

// 计算进度百分比
const getProgressPercentage = (challenge) => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  if (!category) return 0
  const index = category.challenges.findIndex(c => c.id === challenge.id)
  return Math.round((index / category.challenges.length) * 100)
}

// 获取进度状态
const getProgressStatus = (challenge) => {
  if (challenge.completed) return 'success'
  if (challenge.unlocked) return 'warning'
  return 'exception'
}

// 触发漏洞详情动画
const toggleVulnDetails = (challenge) => {
  challenge.showVulnDetails = !challenge.showVulnDetails
  if (challenge.showVulnDetails) {
    // 依次显示每个步骤
    challenge.vulnSteps.forEach((step, index) => {
      setTimeout(() => {
        step.visible = true
      }, index * 500)
    })
  } else {
    // 隐藏所有步骤
    challenge.vulnSteps.forEach(step => step.visible = false)
  }
}

// 教程相关
const showTutorialDialog = ref(false)
const tutorialCarousel = ref(null)
const currentStep = ref(0)

const tutorialSteps = [
  {
    title: '启动靶场',
    description: '点击"启动靶场"按钮，系统会自动为你创建一个包含漏洞的靶场环境。启动过程可能需要一些时间，请耐心等待。',
    image: '/src/assets/Jiaoc/start.png'
  },
  {
    title: '访问靶场',
    description: '靶场启动成功后，点击"前往靶场"链接，系统会在新标签页中打开靶场环境。',
    image: '/src/assets/Jiaoc/前往靶场.png'
  },
  {
    title: '分析目标',
    description: '仔细阅读挑战描述和任务目标，分析目标系统中可能存在的漏洞。可以查看漏洞概述来了解相关漏洞的原理和利用方法。',
    image: '/src/assets/Jiaoc/分析.png'
  },
  {
    title: '获取FLAG',
    description: '利用发现的漏洞，尝试获取系统中的FLAG。FLAG通常是一串特定的字符串，可能隐藏在系统的某个位置。',
    image: '/src/assets/Jiaoc/通关.png'
  },
  {
    title: '提交验证',
    description: '获取到FLAG后，点击"验证FLAG"按钮，将FLAG提交给系统进行验证。验证成功后即可完成挑战。',
    image: '/src/assets/Jiaoc/验证.png'
  }
]

const showTutorial = () => {
  showTutorialDialog.value = true
  currentStep.value = 0
  if (tutorialCarousel.value) {
    tutorialCarousel.value.setActiveItem(0)
  }
}

const nextStep = () => {
  if (currentStep.value < tutorialSteps.length - 1) {
    currentStep.value++
    tutorialCarousel.value?.setActiveItem(currentStep.value)
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
    tutorialCarousel.value?.setActiveItem(currentStep.value)
  }
}

const goToStep = (index) => {
  currentStep.value = index
  tutorialCarousel.value?.setActiveItem(index)
}

const showImagePreview = ref(false)
const currentPreviewImage = ref('')

const handleImageClick = (imageUrl) => {
  currentPreviewImage.value = imageUrl
  showImagePreview.value = true
}

// Add viewMode ref after existing refs
const viewMode = ref('normal')

// Add handleSwitchView function after existing functions
const handleSwitchView = async () => {
  if (!isVipUser.value) {
    ElMessage.warning('请先升级为VIP用户');
    return;
  }
  
  if (viewMode.value === 'normal') {
    viewMode.value = 'vip';
  } else {
    viewMode.value = 'normal';
  }
  // 重新获取数据
  await fetchChallenges();
}

// Add new ref for animation control
const isAnimating = ref(false)

// 添加统计卡片动画
const animateStatCards = () => {
  const cards = document.querySelectorAll('.stat-card')
  cards.forEach((card, index) => {
    gsap.from(card, {
      opacity: 0,
      y: 20,
      delay: index * 0.1,
      duration: 0.8,
      ease: 'back.out(1.7)'
    })
  })
}

// 是否为VIP用户
const isVipUser = computed(() => {
  const roles = store.state.roles || [];
  return roles.includes('ROLE_VIP') || roles.includes('ROLE_ADMIN');
});

// 过滤器
const caseFilter = ref('all')
const teammateSkill = ref('')
const teammateLevel = ref('')

// 最近漏洞数据
const recentVulnerabilities = [
  { cveId: 'CVE-2023-1234', title: 'Apache Log4j远程代码执行漏洞', severity: '严重' },
  { cveId: 'CVE-2023-5678', title: 'Spring Framework认证绕过漏洞', severity: '高危' },
  { cveId: 'CVE-2023-9101', title: 'MySQL权限提升漏洞', severity: '中危' },
  { cveId: 'CVE-2023-3344', title: 'Nginx配置错误导致信息泄露', severity: '低危' }
]

// 真实案例数据
const realWorldCases = [
  {
    title: '某电商平台API未授权访问',
    description: '模拟真实电商平台API接口存在的未授权访问漏洞，学习如何发现和利用此类漏洞。',
    difficulty: '★★☆☆☆',
    tags: ['API安全', 'OWASP Top 10', '未授权访问'],
    category: 'api'
  },
  {
    title: '银行网站XSS+CSRF组合攻击',
    description: '通过组合利用XSS和CSRF漏洞，实现对模拟银行网站的攻击，了解复杂攻击链的构建。',
    difficulty: '★★★★☆',
    tags: ['Web安全', 'XSS', 'CSRF', '攻击链'],
    category: 'web'
  },
  {
    title: '云服务器访问密钥泄露利用',
    description: '模拟云环境中的访问密钥泄露场景，学习如何通过泄露的密钥获取云资源访问权限。',
    difficulty: '★★★☆☆',
    tags: ['云安全', '密钥管理', '权限提升'],
    category: 'cloud'
  }
]

// 社区解题思路
const communitySolutions = [
  {
    challenge: 'SQL注入基础 - 通关思路分享',
    author: '安全小达人',
    date: '2023-05-01',
    views: 1234,
    likes: 89,
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    summary: '这个挑战主要考察基本的SQL注入技巧，通过分析登录表单，我发现input字段没有做任何过滤...'
  },
  {
    challenge: 'XSS跨站脚本 - 绕过过滤器技巧',
    author: 'WebHacker',
    date: '2023-04-28',
    views: 956,
    likes: 67,
    avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
    summary: '这个XSS挑战设置了基本的过滤，但存在一个逻辑漏洞，可以通过编码特殊字符来绕过防护...'
  }
]

// 学习小组
const studyGroups = [
  {
    name: 'Web安全精英团',
    description: '专注于Web安全漏洞的发现与利用，每周组织一次靶场实战演练',
    members: 28,
    level: '中级-高级',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
  },
  {
    name: '二进制安全研究小组',
    description: '研究栈溢出、堆利用等内存破坏类漏洞，定期分享最新漏洞分析',
    members: 15,
    level: '高级',
    avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png'
  }
]

// 潜在队友
const potentialTeammates = [
  {
    name: '张安全',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    skills: ['Web渗透', 'API安全', 'DevSecOps'],
    bio: '3年Web安全经验，擅长API安全测试和DevSecOps实践，正在寻找CTF队伍'
  },
  {
    name: '李黑客',
    avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
    skills: ['逆向工程', '恶意代码分析'],
    bio: '专注于逆向工程和恶意代码分析，参加过多次CTF比赛，希望找到志同道合的伙伴'
  }
]

// 根据严重程度返回标签类型
const getSeverityType = (severity) => {
  if (severity === '严重') return 'danger'
  if (severity === '高危') return 'warning'
  if (severity === '中危') return 'info'
  if (severity === '低危') return 'success'
  return 'info'
}

// 根据标签返回类型
const getTagType = (tag) => {
  if (tag.includes('XSS') || tag.includes('注入') || tag.includes('OWASP')) return 'danger'
  if (tag.includes('Web') || tag.includes('API')) return 'primary'
  if (tag.includes('云安全') || tag.includes('权限')) return 'warning'
  return 'info'
}

// 获取所有挑战
const getAllChallenges = () => {
  const allChallenges = []
  categories.value.forEach(category => {
    if (category.challenges && category.challenges.length) {
      allChallenges.push(...category.challenges)
    }
  })
  return allChallenges
}

// 仪表盘数据
const dashboardStats = [
  {
    icon: 'Trophy',
    value: computed(() => getCompletedChallengesCount()),
    label: '已完成挑战'
  },
  {
    icon: 'Timer',
    value: computed(() => getTotalPracticeTime()),
    label: '练习总时长'
  },
  {
    icon: 'Rank',
    value: computed(() => getSkillLevel()),
    label: '安全技能等级'
  },
  {
    icon: 'Medal',
    value: computed(() => getBadgesCount()),
    label: '技能徽章'
  }
]

// 获取技能等级
const getSkillLevel = () => {
  const completedCount = getCompletedChallengesCount()
  if (completedCount >= 8) return '高级'
  if (completedCount >= 4) return '中级'
  return '初级'
}

// 获取徽章数量
const getBadgesCount = () => {
  return Math.min(getCompletedChallengesCount(), 10) + '项'
}

// 获取当前选中分类的进度
const getCurrentCategoryProgress = () => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  return category ? getCategoryProgress(category) : 0
}

// 获取当前选中分类的状态
const getCurrentCategoryStatus = () => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  return category ? getCategoryStatus(category) : ''
}

const getCurrentCategoryName = () => {
  const category = categories.value.find(c => c.key === currentCategoryKey.value)
  return category ? category.name : ''
}

const getCategoryProgressColor = (category) => {
  const progress = getCategoryProgress(category)
  if (progress === 100) return '#67c23a' // 成功绿色
  if (progress > 0) return '#e6a23c' // 警告黄色
  if (!category.unlocked) return '#f56c6c' // 危险红色
  return '#909399' // 信息灰色
}

// 获取已完成挑战数量
const getCompletedChallengesCount = () => {
  let count = 0
  categories.value.forEach(category => {
    category.challenges.forEach(challenge => {
      if (challenge.completed) count++
    })
  })
  return count
}

// 随机生成练习总时长
const getTotalPracticeTime = () => {
  return `${Math.floor(Math.random() * 20) + 5}小时`
}

// 在 template 中的非VIP版本挑战卡片部分添加漏洞详情组件
const getVulnComponent = (type) => {
  switch (type) {
    case 'sql_injection':
      return SQLInjection
    case 'xss':
      return XSS
    case 'ssrf':
      return SSRF
    case 'log4shell':
      return Log4Shell
    default:
      return null
  }
}

</script>

<style scoped>
.challenge-container {
  display: flex;
  flex-direction: column;
  min-height: 100%;  
  background: transparent;
  position: relative;
  margin-bottom: 50px; 
}

.header {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #7c3aed, #b5a0f4);
  color: #fff;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 24px;
  font-weight: bold;
}

.tutorial-button {
  background: #fff;
  color: #6e45e2;
  border: none;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(110, 69, 226, 0.3);
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
}

.tutorial-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(110, 69, 226, 0.4);
  background: #f0f0f0;
}



.switch-btn {
  color: white;
  font-weight: 500;
  background-color: rgba(255, 255, 255, 0.1);
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.switch-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.challenge-card {
  margin-bottom: 20px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-section h2 {
  margin: 0;
  color: #ffffff;
}

.difficulty-tag {
  color: #ffffff;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.difficulty-tag.el-tag--success {
  border-color: var(--el-color-success);
  color: #ffffff;
}

.difficulty-tag.el-tag--warning {
  border-color: var(--el-color-warning);
  color: #ffffff;
}

.difficulty-tag.el-tag--danger {
  border-color: var(--el-color-danger);
  color: #ffffff;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-text {
  color: #ffffff;
  font-size: 14px;
}

.card-content {
  padding: 20px 0;
}

.left-content {
  padding-right: 20px;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
}

.right-content {
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.description-section,
.task-section,
.tutorial-section {
  margin-bottom: 20px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
  
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.description-section:hover,
.task-section:hover,
.tutorial-section:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
}

.description-section h3,
.task-section h3,
.tutorial-section h3 {
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 10px;
  font-size: 18px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.description-section h3 .el-icon,
.task-section h3 .el-icon,
.tutorial-section h3 .el-icon {
  font-size: 20px;
  color: var(--el-color-primary);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
}

.tutorial-steps {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.tutorial-step {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  justify-content: center;
  width: 100%;
  padding: 4px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.tutorial-step .el-icon {
  color: var(--el-color-success);
  font-size: 16px;
  flex-shrink: 0;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
}

/* 使用深度选择器来确保样式能够穿透scoped限制 */
:deep(.tutorial-step span) {
    color: rgb(255, 255, 255) !important;
    font-size: 14px;
    line-height: 1.6;
    text-align: center;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 备用选择器 */
.tutorial-step :deep(span) {
    color: rgb(255, 255, 255) !important;
    font-size: 14px;
    line-height: 1.6;
    text-align: center;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 全局样式覆盖 */
:global(.tutorial-step span) {
    color: rgb(255, 255, 255) !important;
    font-size: 14px;
    line-height: 1.6;
    text-align: center;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.vuln-overview {
  margin-bottom: 20px;
  width: 100%;
}

.vuln-btn {
  margin-bottom: 10px;
  width: 100%;
  text-align: center;
  color: #ffffff !important;
  opacity: 0.9;
}

.vuln-btn:hover {
  opacity: 1;
}

.vuln-details {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 15px;
  width: 100%;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.vuln-step {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 15px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.vuln-step:hover {
  background: rgba(255, 255, 255, 0.12);
  transform: translateX(5px);
}

.step-icon {
  font-size: 20px;
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
}

.step-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #ffffff;
}

.step-content p {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.action-buttons .el-button {
  width: 100%;
  height: 40px;
  margin-left: -0px;
  opacity: 0.9;
}

.action-buttons .el-button:hover {
  opacity: 1;
}

.action-buttons .el-button--primary {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #ffffff;
}

.action-buttons .el-button--success {
  background: var(--el-color-success);
  border-color: var(--el-color-success);
  color: #ffffff;
}

.action-buttons .el-button:disabled {
  opacity: 0.5;
}

.startup-progress {
  margin-top: 10px;
}

.lab-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  margin-top: 10px;
}

.lab-link {
  font-size: 16px;
  font-weight: bold;
  padding: 8px 16px;
  background: var(--el-color-primary);
  border-radius: 6px;
  color: #ffffff !important;
  text-decoration: none;
  transition: all 0.3s ease;
}

.lab-link:hover {
  background: var(--el-color-primary-light-3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

:deep(.lab-link .el-link__inner) {
  color: #ffffff !important;
}

.timer {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin-top: 4px;
}

.completion-section {
  margin-top: 10px;
}

.completion-details {
  margin-top: 8px;
  text-align: center;
}

@media (max-width: 768px) {
  .left-content {
    padding-right: 0;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    margin-bottom: 20px;
    padding-bottom: 20px;
  }

  .right-content {
    padding-left: 0;
  }

  .action-buttons {
    flex-direction: column;
  }

  .vuln-step {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}

.cancel-btn {
  color: #000000 !important;
}

:deep(.cancel-btn) {
  color: #000000 !important;
}

:deep(.cancel-btn:hover) {
  color: #000000 !important;
}

.tutorial-dialog {
  :deep(.el-dialog) {
    background: rgba(30, 30, 30, 0.95);
    border-radius: 12px;
  }

  :deep(.el-dialog__title) {
    color: #ffffff;
    font-weight: bold;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #ffffff;
  }

  .tutorial-step {
    background: rgba(255, 255, 255, 0.08);
  }

  .step-info h3 {
    color: #ffffff;
  }

  .step-info p {
    color: rgba(255, 255, 255, 0.8);
  }
}

.tutorial-content {
  padding: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tutorial-carousel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.step-number {
  width: 50px;
  height: 50px;
  background: rgba(85, 36, 173, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 20px;
  font-weight: bold;
  margin-right: 25px;
  flex-shrink: 0;
}

.step-info {
  flex: 1;
  max-height: 450px;
  overflow-y: auto;
  padding-right: 10px;
}

.step-info::-webkit-scrollbar {
  width: 6px;
}

.step-info::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.step-info::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.step-info::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.step-image {
  width: 100%;
  max-width: 400px;
  margin-top: 15px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
}

.tutorial-navigation {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  margin-top: 10px;
}

.nav-button {
  background: rgba(85, 36, 173, 0.6);
  border: none;
  color: #ffffff;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  font-size: 14px;
}

.nav-button:hover:not(:disabled) {
  background: rgba(85, 36, 173, 0.8);
  transform: translateY(-2px);
}

.nav-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.step-indicators {
  display: flex;
  gap: 12px;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.step-dot:hover {
  background: rgba(255, 255, 255, 0.4);
}

.step-dot.active {
  background: rgba(85, 36, 173, 0.8);
  transform: scale(1.2);
}

.description-section p[data-v-8c3679b0],
.task-section p[data-v-8c3679b0],
.tutorial-section p[data-v-8c3679b0] {
  color: rgba(255, 255, 255, 0.9) !important;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  line-height: 1.6;
  margin: 0;
}

p[data-v-8c3679b0] {
  color: rgba(255, 255, 255, 0.9) !important;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.image-preview-dialog {
  :deep(.el-dialog) {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    overflow: hidden;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  .preview-container {
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(0, 0, 0, 0.9);
  }
}

.step-image {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.step-image:hover {
  transform: scale(1.02);
}

.step-image:active {
  transform: scale(0.98);
}

.normal-card {
  margin-bottom: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(110, 69, 226, 0.08);
  background: linear-gradient(135deg, #f8fafc 0%, #e0e7ff 100%);
  transition: all 0.3s;
  padding: 32px;
  position: relative;
  overflow: visible;
}

.normal-card:hover {
  box-shadow: 0 8px 32px rgba(110, 69, 226, 0.16);
  transform: translateY(-5px);
}

.normal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(110, 69, 226, 0.1);
}

.normal-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.normal-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.normal-desc, .normal-task, .normal-tutorial {
  padding: 12px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.normal-desc h4, .normal-task h4, .normal-tutorial h4 {
  margin-top: 0;
  margin-bottom: 8px;
  color: #333;
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.normal-desc p, .normal-task p, .normal-tutorial p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.normal-footer {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.progress-section {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.progress-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.start-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.start-button:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 58, 183, 0.3);
}

.env-tip {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #673ab7;
  font-size: 14px;
}

.completion-info {
  margin-top: 15px;
  padding: 15px;
  background-color: #f0f9eb;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
}

.completion-header {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 10px;
  color: #67c23a;
  font-weight: bold;
  font-size: 16px;
}

.completion-info p {
  margin: 5px 0;
  font-size: 14px;
}

.challenge-status-badge {
  position: absolute;
  top: -10px;
  right: 20px;
  background: #67c23a;
  color: white;
  padding: 5px 15px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
  box-shadow: 0 3px 6px rgba(0,0,0,0.1);
  z-index: 1;
}

.fade-in {
  animation: fadeIn 1s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.switch-btn:hover {
  color: #f4f14d;
  background-color: rgba(39, 174, 96, 0.1);
  border-radius: 4px;
  transition: all 0.3s ease;
}

.cyber-security-dashboard {
  padding: 20px 0;
}

.dashboard-stats {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  padding: 16px;
  color: white;
  display: flex;
  align-items: center;
  height: 100px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background-size: 200% 200%;
  animation: gradientBackground 5s ease infinite;
  margin-bottom: 16px;
}

.stat-card:nth-child(1) {
  background: linear-gradient(135deg, #673ab7 0%, #9c27b0 100%);
}

.stat-card:nth-child(2) {
  background: linear-gradient(135deg, #3f51b5 0%, #2196f3 100%);
}

.stat-card:nth-child(3) {
  background: linear-gradient(135deg, #009688 0%, #4caf50 100%);
}

.stat-card:nth-child(4) {
  background: linear-gradient(135deg, #ff9800 0%, #f44336 100%);
}

@keyframes gradientBackground {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
  opacity: 0.8;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(103, 58, 183, 0.1);
}

.section-title .el-icon {
  font-size: 24px;
  color: #673ab7;
}

/* 挑战卡片增强样式 */
.vuln-category-tag {
  margin-bottom: 15px;
}

.challenge-stats {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #606266;
}

.related-resources {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  padding: 15px;
}

.related-resources h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #333;
}

.related-resources ul {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.related-resources li {
  margin-bottom: 5px;
}

/* 侧边栏部分 */
.sidebar-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* 技能路线图 */
.skill-path-container {
  position: relative;
  padding-left: 20px;
}

.skill-path-container:before {
  content: '';
  position: absolute;
  left: 10px;
  top: 0;
  height: 100%;
  width: 2px;
  background: #e0e0e0;
}

.skill-path-node {
  position: relative;
  margin-bottom: 30px;
  padding-left: 20px;
}

.node-dot {
  position: absolute;
  left: -10px;
  top: 10px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e0e0e0;
  z-index: 1;
}

.skill-path-node.completed .node-dot {
  background: #67c23a;
}

.skill-path-node.active .node-dot {
  background: #409eff;
}

.skill-path-node.locked .node-dot {
  background: #909399;
}

.node-content {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.node-content h4 {
  margin-top: 0;
  margin-bottom: 5px;
  color: #333;
}

.node-content p {
  margin: 0 0 5px 0;
  color: #606266;
  font-size: 13px;
}

/* 工具卡片 */
.tool-card {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.tool-logo {
  width: 60px;
  height: 60px;
  object-fit: contain;
  margin-right: 15px;
}

.tool-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.tool-info p {
  margin: 0 0 5px 0;
  color: #606266;
  font-size: 13px;
}

/* 安全资讯 */
.news-container {
  max-height: 300px;
  overflow-y: auto;
}

.news-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.news-date {
  color: #909399;
  font-size: 12px;
}

.news-title {
  margin: 5px 0;
  color: #333;
  font-size: 16px;
}

.news-desc {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 13px;
}

/* 小测验 */
.quiz-container {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
}

.quiz-container h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #333;
}

.quiz-container p {
  margin: 0 0 15px 0;
  color: #606266;
}

.el-radio-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quiz-actions {
  margin-top: 15px;
  text-align: center;
}

/* 通关技巧部分 */
.tips-section {
  margin-top: 30px;
}

.tips-card {
  margin-bottom: 30px;
}

.tip-content {
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.tip-content h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
  font-size: 20px;
}

.tip-content p {
  margin: 0 0 15px 0;
  color: #606266;
  line-height: 1.6;
}

.tip-footer {
  margin-top: auto;
}

/* 靶场实战创新功能样式 */
.interactive-features, .advanced-training, .community-section {
  margin-top: 30px;
}

.feature-cards {
  margin-bottom: 30px;
}

.feature-card {
  height: 250px;
  background: white;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 40px;
  color: #409eff;
  margin-bottom: 15px;
}

.feature-card h3 {
  font-size: 18px;
  margin: 0 0 10px 0;
  color: #333;
}

.feature-card p {
  flex: 1;
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.feature-footer {
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 漏洞实验室样式 */
.lab-card, .case-study-card {
  height: 500px;
}

.lab-content {
  height: 100%;
  padding: 15px;
}

.lab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.lab-header h3 {
  margin: 0;
  color: #333;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.tool-box {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s;
}

.tool-box:hover {
  background: #ecf5ff;
  transform: translateY(-5px);
}

.tool-icon {
  font-size: 36px;
  color: #409eff;
  margin-bottom: 10px;
}

.tool-box h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.tool-box p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

.steps-container {
  padding: 20px;
}

.steps-container h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

/* 真实案例实战样式 */
.case-study-card {
  overflow: auto;
}

.case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
}

.case-header h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.case-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.case-list {
  padding: 0 15px;
}

.case-item {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  padding: 20px 0;
}

.case-difficulty {
  font-size: 16px;
  color: #409eff;
  flex-shrink: 0;
  margin-right: 15px;
}

.case-info {
  flex: 1;
}

.case-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.case-info p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}

.case-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.case-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-left: 15px;
}

/* 社区样式 */
.community-card {
  margin-bottom: 30px;
}

.solution-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.solution-item {
  display: flex;
  background: #f5f7fa;
  border-radius: 10px;
  overflow: hidden;
}

.solution-avatar {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(64, 158, 255, 0.1);
}

.solution-content {
  flex: 1;
  padding: 20px;
}

.solution-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.solution-header h4 {
  margin: 0;
  color: #333;
}

.solution-meta {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 13px;
}

.solution-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.solution-content p {
  margin: 0 0 15px 0;
  color: #606266;
}

.solution-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.solution-likes {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
}

.study-groups, .teammate-search {
  padding: 20px;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.group-item {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 10px;
  padding: 15px;
}

.group-avatar {
  margin-right: 20px;
}

.group-info {
  flex: 1;
}

.group-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.group-info p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}

.group-stats {
  display: flex;
  gap: 15px;
  color: #909399;
  font-size: 13px;
}

.group-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.teammate-search {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.teammate-results {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.teammate-card {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.teammate-card h4 {
  margin: 15px 0 10px 0;
  color: #333;
}

.teammate-skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  justify-content: center;
  margin-bottom: 10px;
}

.teammate-card p {
  margin: 0 0 15px 0;
  color: #606266;
  font-size: 13px;
}

/* 修改所有卡片使用透明背景 */
.feature-cards, .lab-card, .case-study-card, .community-card, .sidebar-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

/* 修改特色功能卡片 */
.feature-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.feature-card:hover {
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* 工具盒子 */
.tool-box {
  background: rgba(255, 255, 255, 0.05);
  transition: all 0.3s;
}

.tool-box:hover {
  background: rgba(255, 255, 255, 0.08);
}

/* 其他卡片内容 */
.case-item, .group-item, .solution-item, .teammate-card, .vuln-step {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.case-item:hover, .group-item:hover, .solution-item:hover, .teammate-card:hover, .vuln-step:hover {
  background: rgba(255, 255, 255, 0.08);
}

/* 修改教程对话框样式使文字更易读 */
.tutorial-dialog {
  :deep(.el-dialog) {
    background: rgba(30, 30, 30, 0.95);
    border-radius: 12px;
  }

  :deep(.el-dialog__title) {
    color: #ffffff;
    font-weight: bold;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #ffffff;
  }

  .tutorial-step {
    background: rgba(255, 255, 255, 0.08);
  }

  .step-info h3 {
    color: #ffffff;
  }

  .step-info p {
    color: rgba(255, 255, 255, 0.8);
  }
}

/* 确保FLAG验证对话框中的文字可见 */
:deep(.el-dialog) {
  background: rgba(30, 30, 30, 0.95);
}

:deep(.el-dialog__title) {
  color: #ffffff;
}

/* 所有区域中的文本颜色 */
.feature-card h3, .case-header h3, .lab-header h3, .section-title, 
.tool-box h4, .case-item h4, .solution-item h4, .group-item h4, .teammate-card h4 {
  color: #ffffff;
}

.feature-card p, .case-header p, .lab-content p, .case-item p, 
.solution-item p, .group-item p, .teammate-card p, .tool-box p {
  color: rgba(255, 255, 255, 0.8);
}

/* FLAG验证对话框样式 */
.flag-dialog {
  :deep(.el-dialog) {
    background: rgba(30, 30, 30, 0.95);
    border-radius: 10px;
    box-shadow: 0 0 30px rgba(0, 0, 0, 0.5);
  }

  :deep(.el-dialog__title) {
    color: #ffffff;
    font-size: 18px;
    font-weight: bold;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: rgba(255, 255, 255, 0.7);
  }

  :deep(.el-input__inner) {
    background-color: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.2);
    color: #ffffff;
  }

  :deep(.el-button) {
    color: #333;
  }

  :deep(.el-button--primary) {
    background-color: #409eff;
    border-color: #409eff;
    color: #ffffff;
  }
}

/* 图片预览对话框样式 */
.image-preview-dialog {
  :deep(.el-dialog) {
    background: rgba(30, 30, 30, 0.95);
    border-radius: 10px;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #ffffff;
  }
}

/* 自定义tabs样式 */
.el-tabs__nav-scroll {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
}

.el-tabs__nav {
  position: relative;
  display: flex;
  white-space: nowrap;
}

.el-tabs__active-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 2px;
  background-color: #409eff;
  z-index: 1;
  transition: transform .3s cubic-bezier(.645,.045,.355,1);
}

.el-tabs__item {
  padding: 0 20px;
  height: 40px;
  box-sizing: border-box;
  line-height: 40px;
  display: inline-block;
  list-style: none;
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  position: relative;
  cursor: pointer;
  transition: color .3s;
}

.el-tabs__item.is-active {
  color: #409eff;
}

.el-tabs__item.is-disabled {
  color: rgba(255, 255, 255, 0.4);
  cursor: not-allowed;
}

.el-tabs__item:hover:not(.is-disabled) {
  color: #f4f14d; /* 改为与激活状态相同的黄色 */
  text-shadow: 0 0 5px rgba(244, 241, 77, 0.3); /* 添加轻微光晕，但比激活状态弱 */
}

.challenge-tabs-content {
  padding: 10px 0;
}

.el-tabs__item.is-active {
  color: #f4f14d; /* 改为醒目的黄色，与hover状态颜色保持一致 */
  font-weight: bold;
  text-shadow: 0 0 10px rgba(244, 241, 77, 0.5); /* 添加光晕效果 */
}

.el-tabs__item:hover:not(.is-disabled) {
  color: #f4f14d; /* 改为与激活状态相同的黄色 */
  text-shadow: 0 0 5px rgba(244, 241, 77, 0.3); /* 添加轻微光晕，但比激活状态弱 */
}

/* 增加右侧边栏样式，提高文字可见度 */
.sidebar-card h3 {
  color: #ffffff !important;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}

.sidebar-card .card-header {
  margin-bottom: 15px;
}

.category-label {
  color: #ffffff !important;
  font-size: 15px;
  font-weight: bold;
  margin-bottom: 5px;
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
}

.tip-item {
  padding: 5px 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.tip-item h4 {
  color: #f4f14d !important; /* 使用醒目的黄色 */
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}

.tip-item p {
  color: #ffffff !important;
  font-size: 14px;
  line-height: 1.6;
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
}

/* 改进进度条文字显示 */
:deep(.el-progress__text) {
  color: #ffffff !important;
  font-weight: bold;
  text-shadow: 0 1px 3px rgba(0,0,0,0.7);
}

.progress-tooltip {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.tooltip-category {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mini-progress {
  width: 100px;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
}

.mini-progress-inner {
  height: 100%;
  border-radius: 5px;
}

.info-icon {
  margin-left: 5px;
  cursor: pointer;
}

.category-progress {
  margin-top: 20px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.progress-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-header span {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.progress-header .info-icon {
  cursor: pointer;
}

/* 进度区域样式 */
.progress-section {
  margin-bottom: 20px;
}

.progress-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.progress-info, .progress-header span {
  color: #ffffff !important;
  font-size: 14px;
  font-weight: bold;
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
}

.category-progress {
  margin-top: 15px;
}

.overall-progress, .category-progress-bar {
  width: 100%;
}

.category-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 5px;
}

.learning-progress-card {
  margin-bottom: 40px;
}

/* 添加新的样式 */
.normal-version {
  padding: 20px;
}

.vip-notice {
  margin-top: 30px;
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.1), rgba(255, 193, 7, 0.05));
  border: 1px solid rgba(255, 193, 7, 0.2);
}

.vip-notice-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.vip-icon {
  font-size: 40px;
  color: #ffc107;
}

.vip-text {
  flex: 1;
}

.vip-text h3 {
  color: #ffc107;
  margin: 0 0 10px 0;
  font-size: 20px;
}

.vip-text p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-size: 14px;
}

/* 修改非VIP版本的卡片样式 */
.normal-version .challenge-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.normal-version .card-header {
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.normal-version .title-section h2 {
  color: #ffffff;
  font-size: 18px;
  margin: 0;
}

.normal-version .description-section,
.normal-version .task-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}

.normal-version .description-section h3,
.normal-version .task-section h3 {
  color: #ffffff;
  font-size: 16px;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.normal-version .description-section p,
.normal-version .task-section p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
}

/* 添加新的预览样式 */
.feature-preview, .advanced-preview {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
}

.preview-content {
  padding: 30px;
  text-align: center;
}

.preview-header {
  margin-bottom: 30px;
}

.preview-icon {
  font-size: 48px;
  color: #ffc107;
  margin-bottom: 15px;
}

.preview-header h3 {
  color: #ffc107;
  font-size: 24px;
  margin: 0;
}

.preview-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.preview-feature-item {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
}

.preview-feature-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
}

.preview-feature-item .el-icon {
  font-size: 24px;
  color: #ffc107;
}

.preview-feature-item span {
  color: #ffffff;
  font-size: 16px;
}

.feature-info {
  text-align: left;
}

.feature-info h4 {
  color: #ffffff;
  margin: 0 0 5px 0;
  font-size: 16px;
}

.feature-info p {
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  font-size: 14px;
}

.advanced-preview .preview-features {
  grid-template-columns: 1fr;
}

.advanced-preview .preview-feature-item {
  padding: 25px;
}

.advanced-preview .el-button {
  margin-top: 20px;
}

/* 漏洞详情部分样式 */
.vuln-details-section {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.vuln-details-section:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
}

.vuln-details-section h3 {
  color: #ffffff;
  margin-bottom: 15px;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.vuln-details-section h3 .el-icon {
  color: var(--el-color-primary);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
}

/* 确保漏洞组件内容可见 */
:deep(.vuln-details-section *) {
  color: rgba(255, 255, 255, 0.9);
}

:deep(.vuln-details-section h4) {
  color: #ffffff;
  font-size: 16px;
  margin: 15px 0 10px 0;
}

:deep(.vuln-details-section p) {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 10px 0;
}

:deep(.vuln-details-section code) {
  background: rgba(0, 0, 0, 0.2);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

:deep(.vuln-details-section pre) {
  background: rgba(0, 0, 0, 0.2);
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 10px 0;
}

:deep(.vuln-details-section ul) {
  margin: 10px 0;
  padding-left: 20px;
}

:deep(.vuln-details-section li) {
  margin: 5px 0;
  color: rgba(255, 255, 255, 0.8);
}
</style>