<template>
  <div class="app-container">
    <!-- 侧边栏，包含过滤器、统计信息和图表 -->
    <div class="sidebar">
      <!-- 过滤器 -->
      <div class="filters card">
        <h4><el-icon><Filter /></el-icon> 过滤选项</h4>
        <el-select v-model="selectedSeverity" placeholder="选择危险等级" clearable>
          <el-option label="所有危险等级" value="" />
          <el-option v-for="level in severityLevels" :key="level" :label="level.toUpperCase()" :value="level" />
        </el-select>
        <el-select v-model="selectedType" placeholder="选择漏洞类型" clearable>
          <el-option label="所有漏洞类型" value="" />
          <el-option v-for="type in vulnerabilityTypes" :key="type" :label="type" :value="type" />
        </el-select>
         <el-switch
            v-model="showBookmarks"
            active-text="仅看收藏"
            inactive-text="查看全部"
            class="bookmark-switch"
        />
      </div>

      <!-- 动态统计面板 -->
      <div class="stats-panel card">
         <h4><el-icon><DataAnalysis /></el-icon> 统计信息</h4>
         <div class="stat-grid">
            <div class="stat-item">
                <h5>总数</h5>
                <p>{{ totalVulnerabilitiesFromSource }}</p> <!-- 显示来源总数 -->
            </div>
            <div class="stat-item">
                <h5>严重</h5>
                <p>{{ BugHole ? BugHole.filter(v => v.severity === 'critical').length : 0 }}</p>
            </div>
             <div class="stat-item">
                <h5>高危</h5>
                <p>{{ BugHole ? BugHole.filter(v => v.severity === 'high').length : 0 }}</p>
            </div>
            <div class="stat-item">
                <h5>中危</h5>
                <p>{{ BugHole ? BugHole.filter(v => v.severity === 'medium').length : 0 }}</p>
            </div>
            <div class="stat-item">
                <h5>低危</h5>
                <p>{{ BugHole ? BugHole.filter(v => v.severity === 'low').length : 0 }}</p>
            </div>
            <div class="stat-item">
                <h5>当前显示</h5>
                <p>{{ totalFilteredVulnerabilities }}</p> <!-- 显示过滤后总数 -->
            </div>
         </div>
      </div>

       <!-- 图表容器 -->
      <div class="chart-container card">
         <h4><el-icon><PieChart /></el-icon> 严重性分布</h4>
         <div id="severityBarChart" style="height: 250px; width: 100%;"></div>
      </div>
    </div>

    <!-- 主内容区域，包含搜索和漏洞列表 -->
    <div class="main-content">
      <!-- 搜索输入框 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索CVE编号或漏洞名称"
          class="search-input"
          clearable
          size="large"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 漏洞列表 -->
      <div v-if="paginatedVulnerabilities.length > 0" class="vulnerability-list">
          <el-card
            v-for="vuln in paginatedVulnerabilities"
            :key="vuln.id"
            :class="['vulnerability-card', `severity-${vuln.severity}`]"
            shadow="hover"
            @click="showVulnDetails(vuln)"
          >
              <div class="card-header">
                  <div class="cve-info">
                      <span class="cve-id">{{ vuln.cve_Id }}</span>
                      <el-tag type="success" size="small" effect="light">CVSS: {{ vuln.cvss_score }}</el-tag>
                  </div>
                  <el-button @click.stop="toggleBookmark(vuln.cve_Id)" :type="isBookmarked(vuln.cve_Id) ? 'warning' : 'info'" circle size="small" plain>
                    <el-icon><StarFilled /></el-icon>
                  </el-button>
              </div>
              <h4 class="title">{{ vuln.title }}</h4>
              <div class="meta">
                  <el-tag size="small" effect="plain">{{ formatDate(vuln.published_date) }}</el-tag>
                  <el-tag :type="severityColor(vuln.severity)" size="small" effect="light">{{ vuln.severity.toUpperCase() }}</el-tag>
                  <el-tag type="info" size="small" effect="plain">{{ vuln.type }}</el-tag>
              </div>
          </el-card>
      </div>
       <el-empty v-else description="未找到相关漏洞" style="flex-grow: 1;"></el-empty>

      <!-- 分页 -->
      <el-pagination
        v-if="totalFilteredVulnerabilities > pageSize"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="totalFilteredVulnerabilities"
        layout="prev, pager, next"
        @current-change="handlePageChange"
        class="pagination-center"
        background
      ></el-pagination>
    </div>

    <!-- 漏洞详情对话框 -->
    <el-dialog v-model="dialogVisible" title="漏洞详情" width="60%" top="15vh"> <!-- 调整了 top 值 -->
      <div v-if="selectedVuln" class="dialog-content">
        <el-descriptions border :column="2">
          <el-descriptions-item label="CVE ID">{{ selectedVuln.cve_Id }}</el-descriptions-item>
          <el-descriptions-item label="危险等级">
            <el-tag :type="severityColor(selectedVuln.severity)">
              {{ selectedVuln.severity.toUpperCase() }}
            </el-tag>
          </el-descriptions-item>
           <el-descriptions-item label="CVSS Score">{{ selectedVuln.cvss_score }}</el-descriptions-item>
           <el-descriptions-item label="发布日期">{{ formatDate(selectedVuln.published_date) }}</el-descriptions-item>
          <el-descriptions-item label="漏洞类型">{{ selectedVuln.type }}</el-descriptions-item>
          <el-descriptions-item label="影响版本">
            <el-tag v-if="selectedVuln.affected_versions" type="info" size="small">
              {{ selectedVuln.affected_versions }}
            </el-tag>
            <span v-else>N/A</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h3>📜 漏洞描述</h3>
        <p class="description-text">{{ selectedVuln.description }}</p>

        <el-divider />
        <h3>🛡️ 解决方案</h3>
        <el-alert :title="selectedVuln.solutions || '暂无官方解决方案'" :type="selectedVuln.solutions ? 'success' : 'info'" :closable="false" show-icon />
      </div>
      <template #footer>
        <el-button type="primary" @click="closeDialog">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';
import { Search, StarFilled, Filter, DataAnalysis, PieChart } from '@element-plus/icons-vue'; // 导入必要的图标
import store from '@/store';
import ToUrl from '@/api/api';
import * as echarts from 'echarts'

// 响应式状态
const searchQuery = ref('');
const selectedSeverity = ref('');
const selectedType = ref('');
const selectedVuln = ref(null);
const showBookmarks = ref(false); // 切换仅显示书签的状态
const bookmarkedCves = ref(new Set());
const dialogVisible = ref(false);

// 分页状态
const currentPage = ref(1);
const pageSize = ref(12); // 3x3 网格布局
const totalFilteredVulnerabilities = ref(0); // 过滤后的总数
const totalVulnerabilitiesFromSource = ref(0); // API直接返回的总数
const BugHole = ref([]); // 存储来自 API 的所有漏洞

// 配置项
const severityLevels = ['critical', 'high', 'medium', 'low'];
const vulnerabilityTypes = ['RCE', 'Privilege Escalation', 'Directory Traversal', 'SQL Injection', 'Cross-Site Scripting (XSS)']; // 添加了更多类型

let barChartInstance = null; // 用于持有图表实例

// 初始化或更新图表
const initOrUpdateCharts = () => {
  if (!BugHole.value || BugHole.value.length === 0) return; // 确保数据存在

  const barChartDom = document.getElementById('severityBarChart');
  if (!barChartDom) return; // 确保 DOM 元素存在

  if (!barChartInstance) {
    barChartInstance = echarts.init(barChartDom);
     // 仅添加一次 resize 监听器
    window.addEventListener('resize', () => {
        if (barChartInstance) {
            barChartInstance.resize();
        }
    });
  }

  const barOption = {
    title: {
      text: '严重性分布', // 图表标题 (中文)
      left: 'center',
      textStyle: {
          fontSize: 14,
          fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: ['严重', '高危', '中危', '低危'], // X轴标签 (中文)
      axisLabel: { fontSize: 10 }
    },
    yAxis: { type: 'value' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }, // 图表边距
    series: [
      {
        data: [
          BugHole.value.filter(v => v.severity === 'critical').length,
          BugHole.value.filter(v => v.severity === 'high').length,
          BugHole.value.filter(v => v.severity === 'medium').length,
          BugHole.value.filter(v => v.severity === 'low').length
        ],
        type: 'bar',
        itemStyle: {
          color: function(params) {
            // 更柔和的图表色板
            const colors = ['#e74c3c', '#f1a340', '#5dade2', '#58d68d'];
            return colors[params.dataIndex];
          }
        }
      }
    ]
  };

  barChartInstance.setOption(barOption);
};

// 从 API 获取数据
const fetchVulnerabilities = async () => {
  try {
    const response = await axios.get(ToUrl.url + `/user/findAllHole`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    });
    BugHole.value = response.data.data || []; // 确保是数组
    totalVulnerabilitiesFromSource.value = BugHole.value.length; // 设置来源总数

    // 数据获取后初始化图表
    initOrUpdateCharts();

  } catch (error) {
    console.error('数据获取失败:', error);
    BugHole.value = []; // 错误时重置
    totalVulnerabilitiesFromSource.value = 0;
  }
};

// 书签功能
const loadBookmarks = () => {
  const saved = localStorage.getItem('vulnBookmarks');
  if (saved) {
    try {
      bookmarkedCves.value = new Set(JSON.parse(saved));
    } catch (e) {
      console.error("从localStorage解析书签失败", e);
      bookmarkedCves.value = new Set(); // 解析失败时重置
      localStorage.removeItem('vulnBookmarks'); // 清除无效数据
    }
  }
};

const saveBookmarks = () => {
  localStorage.setItem('vulnBookmarks', JSON.stringify([...bookmarkedCves.value]));
};

const toggleBookmark = (cveId) => {
  if (bookmarkedCves.value.has(cveId)) {
    bookmarkedCves.value.delete(cveId);
  } else {
    bookmarkedCves.value.add(cveId);
  }
  saveBookmarks(); // 更改后立即保存
};

const isBookmarked = (cveId) => bookmarkedCves.value.has(cveId);

// 严重性颜色映射
const severityColor = (level) => ({
  critical: 'danger',
  high: 'warning',
  medium: 'primary', // 更改了中危颜色
  low: 'info'
}[level] || 'info');

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  try {
    const date = new Date(dateString);
    // 基本的无效日期检查
    if (isNaN(date.getTime())) {
        return '无效日期';
    }
    return date.toLocaleDateString('zh-CN', { // 使用 locale 获取更好的格式
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
  } catch (e) {
      console.error("格式化日期错误:", dateString, e);
      return '无效日期';
  }
};


// 显示漏洞详情
const showVulnDetails = (vuln) => {
  selectedVuln.value = vuln;
  dialogVisible.value = true;
};

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false;
  // 延迟清除 selectedVuln 以避免过渡期间内容闪烁
  setTimeout(() => {
      selectedVuln.value = null;
  }, 300);
};

// 初始数据加载
onMounted(() => {
  loadBookmarks();
  fetchVulnerabilities(); // 初始加载所有数据
});

// 计算过滤后的漏洞
const filteredVulnerabilities = computed(() => {
  let result = [...BugHole.value]; // 从所有漏洞开始

  // 应用搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase().trim();
    if (query) {
        result = result.filter(
          (vuln) =>
            vuln.cve_Id.toLowerCase().includes(query) ||
            vuln.title.toLowerCase().includes(query)
        );
    }
  }

  // 应用严重性过滤
  if (selectedSeverity.value) {
    result = result.filter((vuln) => vuln.severity === selectedSeverity.value);
  }

  // 应用类型过滤
  if (selectedType.value) {
    result = result.filter((vuln) => vuln.type === selectedType.value);
  }

  // 应用书签过滤
  if (showBookmarks.value) {
    result = result.filter((vuln) => bookmarkedCves.value.has(vuln.cve_Id));
  }

  // 过滤后更新总数
  totalFilteredVulnerabilities.value = result.length;

  return result; // 返回完整的过滤列表（分页前）
});

// 计算分页后的漏洞
const paginatedVulnerabilities = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return filteredVulnerabilities.value.slice(start, end);
});


// 处理页码变化
const handlePageChange = (page) => {
  currentPage.value = page;
  // 无需重新获取数据，只需更改页码
};

// 监听过滤器变化以重置分页，并可能更新图表（如果需要）
watch([searchQuery, selectedSeverity, selectedType, showBookmarks], () => {
  currentPage.value = 1; // 过滤器更改时重置到第一页
});

// 监听源数据变化以更新图表
watch(BugHole, (newValue) => {
  if (newValue && newValue.length > 0) {
    initOrUpdateCharts();
  }
}, { deep: true });


</script>

<style scoped>
/* 整体布局 */
.app-container {
  display: flex;
  gap: 1.5rem; /* 侧边栏和主内容之间的间隙 */
  padding: 1rem 1.5rem; /* 减少内边距 */
  /* background-color: #f9fafb; */ /* 被渐变替换 */
  background: linear-gradient(135deg, #769fcd 0%, #b9d7ea 100%); /* 更柔和的蓝调 */
  min-height: calc(100vh - 50px); /* 根据你的头部高度调整 */
}

/* 侧边栏 */
.sidebar {
  width: 260px; /* 稍窄的侧边栏 */
  flex-shrink: 0; /* 防止侧边栏收缩 */
  display: flex;
  flex-direction: column;
  gap: 1rem; /* 减少侧边栏项目之间的间隙 */
}

/* 主内容区域 */
.main-content {
  flex: 1; /* 允许主内容占据剩余空间 */
  display: flex;
  flex-direction: column;
  min-width: 0; /* 防止溢出问题 */
  gap: 1rem; /* 减少间隙 */
}

/* 侧边栏通用卡片样式 */
.sidebar .card {
  /* background-color: #fff; */ /* 被渐变替换 */
  background: linear-gradient(to bottom, #fefefe, #ffffff); /* 微妙的渐变 */
  padding: 1rem;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); /* 更柔和的阴影 */
  border: 1px solid #dfe6ee; /* 更柔和的边框 */
}

.sidebar .card h4 {
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1rem; /* 稍小的标题 */
  color: #4a5568; /* 稍柔和的深灰色 */
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-bottom: 0.6rem;
  border-bottom: 1px solid #e2e8f0; /* 更柔和的分隔线 */
  font-weight: 600; /* 半粗体标题 */
}

/* 过滤器 */
.filters .el-select {
  width: 100%;
  margin-bottom: 0.8rem;
}
.filters .bookmark-switch {
    margin-top: 0.5rem;
    display: flex; /* 使用 flex 获得更好的对齐 */
    justify-content: space-between; /* 分开标签和开关 */
    align-items: center;
}
/* 如果需要，调整开关标签样式 */
:deep(.filters .el-switch__label) {
    color: #718096; /* 更柔和的灰色标签 */
    font-size: 0.9em;
}
:deep(.filters .el-switch__label.is-active) {
    color: #5dade2; /* 匹配中等严重性蓝色 */
}


/* 统计面板 */
.stats-panel h5 {
  margin: 0 0 0.2rem 0;
  font-size: 0.75rem; /* 更小的标签 */
  color: #718096; /* 更柔和的中灰色 */
  text-align: center;
  font-weight: normal;
}

.stats-panel p {
  margin: 0;
  font-size: 1.2rem; /* 稍小的值 */
  font-weight: 600; /* 半粗体 */
  color: #2d3748; /* 稍柔和的深色文本 */
  text-align: center;
}

.stat-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem; /* 更小的间隙 */
}

.stat-item {
  background-color: #f7fafc; /* 非常浅的蓝灰色 */
  padding: 0.6rem; /* 减少内边距 */
  border-radius: 4px;
  border: 1px solid #e2e8f0; /* 更柔和的边框 */
}

/* 图表容器 */
.chart-container {
    padding-bottom: 0.5rem; /* 调整内边距 */
}

/* 搜索栏 */
.search-bar {
  /* background-color: #fff; */ /* 被渐变替换 */
  background: linear-gradient(to bottom, #fefefe, #ffffff); /* 微妙的渐变 */
  padding: 0.5rem 1rem; /* 调整内边距 */
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); /* 更柔和的阴影 */
  border: 1px solid #dfe6ee; /* 更柔和的边框 */
}

.search-input {
  border: none;
}
:deep(.search-input .el-input__wrapper) { 
  box-shadow: none !important;
  background-color: transparent !important;
  padding: 0; /* 如果需要，移除默认内边距 */
}
:deep(.search-input .el-input__inner) { 
    height: 38px; /* 如果使用 size large，确保高度一致 */
    line-height: 38px;
}


/* 漏洞列表 */
.vulnerability-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); /* 调整最小宽度 */
  gap: 1rem;
  flex-grow: 1; /* 允许列表增长 */
  align-content: start; /* 防止项目拉伸 */
}

.vulnerability-card {
  /* background-color: #fff; */ /* 被渐变替换 */
  background: linear-gradient(to bottom, #fefefe, #ffffff); /* 微妙的渐变 */
  border-radius: 5px;
  border: 1px solid #dfe6ee; /* 更柔和的边框 */
  border-left: 4px solid transparent; /* 基础左边框 */
  transition: all 0.25s ease-in-out;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); /* 略微调整的阴影 */
  cursor: pointer;
  display: flex;
  flex-direction: column;
  padding: 1rem;
}

.vulnerability-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.07); /* 稍柔和的悬停阴影 */
  border-color: #cdd7e5; /* 悬停时稍深的边框颜色 */
}

/* 严重性颜色 - 使用边框 (更柔和的色板) */
.severity-critical { border-left-color: #e74c3c; } /* 柔和红 */
.severity-high { border-left-color: #f1a340; } /* 暖橙 */
.severity-medium { border-left-color: #5dade2; } /* 柔和蓝 */
.severity-low { border-left-color: #58d68d; } /* 温和绿 */

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.6rem;
}

.cve-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    min-width: 0; /* 防止溢出 */
}

.cve-id {
  font-weight: 600; /* 半粗体 */
  color: #2d3748; /* 匹配统计面板深色文本 */
  font-size: 0.85rem;
  white-space: nowrap; /* 防止换行 */
  overflow: hidden;
  text-overflow: ellipsis;
}

.title {
  margin: 0 0 0.8rem 0;
  font-size: 0.95rem; /* 稍小的标题 */
  color: #2d3748; /* 匹配统计面板深色文本 */
  line-height: 1.4;
  font-weight: 600;
  /* 限制标题行数 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta {
  margin-top: auto; /* 将 meta 推到底部 */
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem; /* 标签之间更小的间隙 */
  padding-top: 0.6rem;
  border-top: 1px solid #eef2f7; /* 非常柔和的分隔线 */
}

.meta .el-tag {
    height: 22px; /* 一致的标签高度 */
    padding: 0 6px;
    line-height: 20px;
}

/* 分页 */
.pagination-center {
  padding-top: 1rem;
  display: flex;
  justify-content: center;
  margin-top: auto; /* 如果内容较短，推到底部 */
}

/* 对话框样式 */
.dialog-content {
    max-height: 75vh; /* 限制对话框高度 */
    overflow-y: auto; /* 如果需要，添加滚动条 */
    padding-right: 10px; /* 为滚动条留出空间 */
}

.el-dialog__body {
  padding: 1rem 1.5rem;
}

.el-descriptions {
  margin-bottom: 1.5rem;
}

.dialog-content h3 {
  font-size: 1.05rem;
  margin-top: 1.5rem;
  margin-bottom: 0.8rem;
  color: #2d3748; /* 匹配卡片文本颜色 */
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e2e8f0; /* 更柔和的分隔线 */
  font-weight: 600;
}
.description-text {
    line-height: 1.6;
    color: #4a5568; /* 更柔和的文本颜色 */
    font-size: 0.9rem;
}

/* 响应式调整 */
@media (max-width: 1200px) {
    .sidebar {
        width: 240px; /* 中等屏幕上更窄的侧边栏 */
    }
     .vulnerability-list {
        grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    }
}

@media (max-width: 992px) {
  .app-container {
    flex-direction: column; /* 在较小屏幕上堆叠侧边栏和主内容 */
  }
  .sidebar {
    width: 100%; /* 侧边栏占据全部宽度 */
    flex-direction: row; /* 水平布局侧边栏项目 */
    flex-wrap: wrap; /* 允许换行 */
    justify-content: space-between;
  }
  .sidebar .card {
      flex-basis: calc(50% - 0.5rem); /* 侧边栏卡片大致两列 */
      min-width: 200px; /* 防止卡片变得太小 */
  }
   .sidebar .chart-container {
       flex-basis: 100%; /* 图表占据全部宽度 */
       margin-top: 1rem;
   }
   .stats-grid {
       grid-template-columns: repeat(auto-fit, minmax(90px, 1fr)); /* 更自适应的统计网格 */
   }
   .main-content {
       gap: 1rem;
   }
}

@media (max-width: 768px) {
  .app-container {
      padding: 1rem; /* 在小屏幕上减少内边距 */
  }
  .sidebar .card {
      flex-basis: 100%; /* 堆叠侧边栏卡片 */
   }
   .vulnerability-list {
      grid-template-columns: 1fr; /* 单列列表 */
  }
   .el-dialog {
       width: 90% !important; /* 在移动设备上更宽的对话框 */
   }
    .dialog-content {
        max-height: 80vh;
    }
}

</style>