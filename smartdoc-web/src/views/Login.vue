<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <h2>🚀 SmartDoc-AI</h2>
        <p>企业级智能知识库与 RAG 协同问答平台</p>
      </div>

      <el-form :model="form" label-position="top" size="large">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名（如 zorcee）" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleSubmit"
          />
        </el-form-item>

        <!-- 切换到注册模式时，动态展示昵称输入框 (v-if 指令) -->
        <el-form-item v-if="isRegister" label="用户昵称">
          <el-input v-model="form.nickname" placeholder="请输入您的昵称（如 阿策）" prefix-icon="Postcard" />
        </el-form-item>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
          {{ isRegister ? '立即注册账号' : '登录进入工作台' }}
        </el-button>

        <div class="switch-mode">
          <el-link type="primary" :underline="false" @click="isRegister = !isRegister">
            {{ isRegister ? '已有账号？返回登录' : '没有账号？点击免费注册' }}
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const isRegister = ref(false)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  nickname: ''
})

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写完整的用户名和密码！')
    return
  }
  loading.value = true
  try {
    if (isRegister.value) {
      // 调用后端注册接口 POST /api/auth/register
      await request.post('/api/auth/register', form)
      ElMessage.success('注册成功！请直接登录')
      isRegister.value = false
    } else {
      // 调用后端登录接口 POST /api/auth/login
      const res = await request.post('/api/auth/login', {
        username: form.username,
        password: form.password
      })
      // 保存后端颁发的 satoken 房卡与用户信息
      localStorage.setItem('satoken', res.data.tokenValue)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      ElMessage.success('欢迎回来，' + (res.data.nickname || res.data.username) + '！')
      router.push('/workspace')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
}
.login-card {
  width: 420px;
  border-radius: 12px;
  padding: 10px 15px;
}
.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.login-header h2 {
  margin: 0 0 8px 0;
  color: #1e293b;
  font-size: 26px;
}
.login-header p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}
.submit-btn {
  width: 100%;
  margin-top: 10px;
  font-weight: bold;
}
.switch-mode {
  text-align: center;
  margin-top: 16px;
}
</style>