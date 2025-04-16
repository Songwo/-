<template>
    <el-card class="tool-card">
      <h3><mdi:security /> 密码强度</h3>
      <el-input
        v-model="password"
        type="password"
        show-password
        placeholder="输入密码"
      />
      <el-progress 
        :percentage="strengthPercent" 
        :status="strengthStatus"
        :format="formatStrength"
      />
      <div class="rule-list">
        <el-check-tag 
          v-for="(rule, index) in rules" 
          :key="index"
          :checked="rule.valid"
          :type="rule.valid ? 'success' : 'info'"
        >
          {{ rule.label }}
        </el-check-tag>
      </div>
    </el-card>
  </template>
  
  <script setup>
  import { ref, computed } from 'vue'
  
  const password = ref('')
  
  const rules = computed(() => [
    { label: '≥8位', valid: password.value.length >= 8 },
    { label: '含大写字母', valid: /[A-Z]/.test(password.value) },
    { label: '含数字', valid: /\d/.test(password.value) },
    { label: '特殊字符', valid: /[!@#$%^&*]/.test(password.value) }
  ])
  
  const strengthPercent = computed(() => 
    Math.round((rules.value.filter(r => r.valid).length / rules.value.length) * 100)
  )
  
  const strengthStatus = computed(() => {
    const percent = strengthPercent.value
    return percent < 50 ? 'exception' : percent < 80 ? 'warning' : 'success'
  })
  </script>
  
<style scoped>
/* 全局工具卡片样式 */
.tool-card {
  margin: 20px;
  padding: 20px;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 8px 16px rgba(0,0,0,0.1);
    transform: translateY(-3px);
  }
}

.result-box {
  margin-top: 15px;
  font-family: monospace;
}

.button-group {
  margin: 15px 0;
  display: flex;
  gap: 10px;
}
</style>