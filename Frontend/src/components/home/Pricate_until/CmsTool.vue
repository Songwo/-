<template>
  <div class="cms-tool">
    <h2>在线 CMS 指纹识别工具</h2>
    <p>输入网站 URL,识别其 CMS 类型，并获取详细信息（归属地、端口等）</p>

    <!-- 输入框 -->
    <el-input v-model="websiteUrl" placeholder="请输入网站 URL" class="input-box" />

    <!-- 按钮 -->
    <el-button type="primary" @click="fetch()" :loading="loading">开始识别</el-button>

    <!-- 识别结果 -->
    <div v-if="cmsResult" class="result">
      <h3>识别结果：</h3>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column label="信息类型" prop="label"></el-table-column>
        <el-table-column label="内容" prop="content"></el-table-column>
      </el-table>

      <!-- 端口信息 -->
      <el-table :data="cmsResult.details" style="width: 100%">
        <el-table-column label="端口" prop="port"></el-table-column>
        <el-table-column label="状态" prop="status"></el-table-column>
      </el-table>
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-message">
      <p>{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script setup>
import store from '@/store';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ref,computed } from 'vue';
import ToUrl from '@/api/api';

//定义响应式数据
const websiteUrl=ref()
const returnDate=ref()
//检索输入的ip
const fetch=async () => {
  try{
    const res = await axios.get(ToUrl.url+'/cms/detect', {
      params: { url: websiteUrl.value }, // 这里放入 params
      headers: { 
        'Authorization': `Bearer ${store.state.token}` 
      }
    });
    returnDate.value=res.data.data;
    // console.log(returnDate.value);
  }catch(error){
    ElMessage.error(error)
  }
}

</script>

<style scoped>
.cms-tool {
  text-align: center;
  padding: 20px;
  font-size: 18px; /* 增大字体 */
  height: 83vh; /* 让容器撑满整个页面 */
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.input-box {
  margin-bottom: 20px;
  width: 400px;
  height: 42px;
  font-size: 16px; /* 增大输入框字体 */
  margin-left: 580px;
}

.el-button {
  font-size: 16px; /* 增大按钮字体 */
}

.result {
  margin-top: 20px;
  text-align: left;
  font-size: 18px; /* 增大表格内容的字体 */
}

.error-message {
  margin-top: 20px;
  color: red;
  font-size: 18px; /* 增大错误信息字体 */
}

.el-table {
  margin-top: 20px;
  font-size: 18px; /* 增大表格字体 */
}
</style>
