<template>
  <div class="workspace-layout">
    <!-- 1. 顶部导航栏 -->
    <header class="top-header">
      <div class="brand">
        <span class="logo">🚀 SmartDoc-AI</span>
        <el-tag type="success" size="small" effect="dark" style="margin-left: 12px;">RAG Engine v1.0</el-tag>
      </div>
      <div class="user-area">
        <span class="welcome">👤 {{ userInfo.nickname || userInfo.username || '开发者' }}</span>
        <el-button type="danger" plain size="small" icon="SwitchButton" @click="handleLogout">退出登录</el-button>
      </div>
    </header>

    <!-- 2. 主体三栏工作区 -->
    <div class="main-body">
      <!-- 左栏：知识库管理 -->
      <aside class="kb-sidebar">
        <div class="panel-header">
          <span>📚 我的知识库</span>
          <el-button type="primary" size="small" icon="Plus" @click="showCreateKb = true">新建</el-button>
        </div>
        <div class="kb-list">
          <div
              v-for="kb in kbList"
              :key="kb.id"
              class="kb-item"
              :class="{ active: currentKb && currentKb.id === kb.id }"
              @click="selectKb(kb)"
          >
            <div class="kb-info">
              <div class="kb-name">{{ kb.name }}</div>
              <div class="kb-desc">{{ kb.description || '暂无描述' }}</div>
            </div>
            <el-button
                type="danger"
                link
                icon="Delete"
                @click.stop="handleDeleteKb(kb.id)"
            />
          </div>
          <el-empty v-if="kbList.length === 0" description="请先新建一个知识库" :image-size="60" />
        </div>
      </aside>

      <!-- 中栏：文档上传与切片管理 -->
      <section class="doc-panel" v-if="currentKb">
        <div class="panel-header">
          <span>📄 《{{ currentKb.name }}》文档库</span>
          <el-upload
              action="/api/doc/upload"
              :headers="{ satoken: token }"
              :data="{ kbId: currentKb.id }"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
          >
            <el-button type="success" size="small" icon="Upload">上传文档 (PDF/Word/TXT/MD)</el-button>
          </el-upload>
        </div>

        <el-table :data="docList" stripe style="width: 100%" empty-text="当前知识库暂无文档，请点击右上角上传">
          <el-table-column prop="name" label="文档名称" min-width="160" />
          <el-table-column prop="wordCount" label="字数" width="80" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 3 ? 'success' : 'info'" size="small">
                {{ row.status === 3 ? '已切片就绪' : '处理中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link size="small" icon="View" @click="viewChunks(row)">查看切片</el-button>
              <el-button type="danger" link size="small" icon="Delete" @click="handleDeleteDoc(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <!-- 右栏：RAG AI 智能问答区 -->
      <section class="chat-panel" v-if="currentKb">
        <div class="panel-header">
          <span>🤖 AI 知识库智能问答（基于《{{ currentKb.name }}》检索增强）</span>
        </div>

        <div class="chat-history" ref="chatBoxRef">
          <el-empty v-if="chatList.length === 0" description="向知识库提个问题吧（如：公司年假有几天？）" />
          <div v-for="msg in chatList" :key="msg.id" class="chat-turn">
            <!-- 用户提问气泡 -->
            <div class="bubble-row user-row">
              <div class="bubble user-bubble">{{ msg.question }}</div>
            </div>
            <!-- AI 回答气泡 -->
            <div class="bubble-row ai-row">
              <div class="bubble ai-bubble">
                <div class="ai-answer">{{ msg.answer }}</div>
                <div v-if="msg.matchedChunks" class="chunk-ref">
                  📌 <strong>RAG 溯源命中切片：</strong><br />
                  {{ msg.matchedChunks }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input-bar">
          <el-input
              v-model="questionInput"
              placeholder="输入问题按回车发送，AI 将自动检索知识切片作答..."
              size="large"
              @keyup.enter="handleAsk"
          >
            <template #append>
              <el-button type="primary" icon="Promotion" :loading="asking" @click="handleAsk">发送提问</el-button>
            </template>
          </el-input>
        </div>
      </section>

      <div v-else class="empty-workspace">
        <el-empty description="请在左侧选择或新建一个知识库开始使用" />
      </div>
    </div>

    <!-- 弹窗：新建知识库 -->
    <el-dialog v-model="showCreateKb" title="新建智能知识库" width="420px">
      <el-form :model="kbForm" label-position="top">
        <el-form-item label="知识库名称">
          <el-input v-model="kbForm.name" placeholder="例如：企业规章制度库" />
        </el-form-item>
        <el-form-item label="知识库描述">
          <el-input v-model="kbForm.description" type="textarea" rows="3" placeholder="简要描述该库存放的文档类型" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateKb = false">取消</el-button>
        <el-button type="primary" @click="handleCreateKb">确定创建</el-button>
      </template>
    </el-dialog>

    <!-- 抽屉：查看文档 RAG 切片卡片 -->
    <el-drawer v-model="showChunkDrawer" :title="`🧩 文档切片透视：${currentDocName}`" size="450px">
      <el-empty v-if="chunkList.length === 0" description="该文档暂无切片（请重新上传文档以触发切片）" />
      <el-card v-for="c in chunkList" :key="c.id" class="chunk-card" shadow="hover">
        <div class="chunk-card-header">
          <el-tag size="small" type="warning">切片 #{{ c.chunkIndex }}</el-tag>
          <span class="char-count">{{ c.charCount }} 字符</span>
        </div>
        <div class="chunk-content">{{ c.content }}</div>
      </el-card>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const token = localStorage.getItem('satoken') || ''
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

// 状态数据
const kbList = ref([])
const currentKb = ref(null)
const docList = ref([])
const chatList = ref([])
const chunkList = ref([])

// 弹窗与输入控制
const showCreateKb = ref(false)
const showChunkDrawer = ref(false)
const currentDocName = ref('')
const questionInput = ref('')
const asking = ref(false)
const chatBoxRef = ref(null)

const kbForm = reactive({
  name: '',
  description: '',
  isPublic: 0
})

// 1. 加载知识库列表
const loadKbList = async () => {
  const res = await request.get('/api/kb/list')
  kbList.value = res.data || []
  if (kbList.value.length > 0 && !currentKb.value) {
    selectKb(kbList.value[0])
  }
}

// 2. 切换选中知识库，联动加载文档列表与聊天记录
const selectKb = async (kb) => {
  currentKb.value = kb
  await Promise.all([loadDocList(kb.id), loadChatHistory(kb.id)])
}

const loadDocList = async (kbId) => {
  const res = await request.get(`/api/doc/list/${kbId}`)
  docList.value = res.data || []
}

const loadChatHistory = async (kbId) => {
  const res = await request.get(`/api/chat/history/${kbId}`)
  chatList.value = res.data || []
  scrollToBottom()
}

// 3. 创建与删除知识库
const handleCreateKb = async () => {
  if (!kbForm.name) {
    ElMessage.warning('请输入知识库名称')
    return
  }
  await request.post('/api/kb/create', kbForm)
  ElMessage.success('知识库创建成功！')
  showCreateKb.value = false
  kbForm.name = ''
  kbForm.description = ''
  await loadKbList()
}

const handleDeleteKb = async (id) => {
  await ElMessageBox.confirm('确定删除该知识库吗？', '提示', { type: 'warning' })
  await request.delete(`/api/kb/${id}`)
  ElMessage.success('删除成功')
  if (currentKb.value && currentKb.value.id === id) {
    currentKb.value = null
  }
  await loadKbList()
}

// 4. 文档上传、查看切片与删除
const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    ElMessage.success('文档上传并智能切片成功！')
    loadDocList(currentKb.value.id)
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('文档上传失败，请检查后端状态')
}

const viewChunks = async (doc) => {
  currentDocName.value = doc.name
  const res = await request.get(`/api/doc/chunks/${doc.id}`)
  chunkList.value = res.data || []
  showChunkDrawer.value = true
}

const handleDeleteDoc = async (id) => {
  await request.delete(`/api/doc/${id}`)
  ElMessage.success('文档已删除')
  loadDocList(currentKb.value.id)
}

// 5. RAG 智能问答发送
const handleAsk = async () => {
  if (!questionInput.value.trim() || asking.value) return
  asking.value = true
  try {
    const q = questionInput.value
    questionInput.value = ''
    await request.post('/api/chat/ask', {
      kbId: currentKb.value.id,
      question: q
    })
    await loadChatHistory(currentKb.value.id)
  } finally {
    asking.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBoxRef.value) {
      chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
    }
  })
}

// 6. 退出登录
const handleLogout = async () => {
  try {
    await request.post('/api/auth/logout')
  } finally {
    localStorage.removeItem('satoken')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}

onMounted(() => {
  loadKbList()
})
</script>

<style scoped>
.workspace-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.top-header {
  height: 56px;
  background: #0f172a;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
}
.main-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}
.kb-sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}
.doc-panel {
  width: 460px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  padding: 0 12px;
}
.chat-panel {
  flex: 1;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
}
.panel-header {
  height: 52px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px;
  border-bottom: 1px solid #e2e8f0;
  font-weight: bold;
  color: #1e293b;
}
.kb-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}
.kb-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.kb-item:hover {
  background: #f1f5f9;
}
.kb-item.active {
  background: #eff6ff;
  border-color: #3b82f6;
}
.kb-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}
.kb-desc {
  font-size: 12px;
  color: #64748b;
}
.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.chat-turn {
  margin-bottom: 20px;
}
.bubble-row {
  display: flex;
  margin-bottom: 10px;
}
.user-row {
  justify-content: flex-end;
}
.ai-row {
  justify-content: flex-start;
}
.bubble {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}
.user-bubble {
  background: #2563eb;
  color: #fff;
  border-bottom-right-radius: 2px;
}
.ai-bubble {
  background: #fff;
  color: #1e293b;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 2px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
.chunk-ref {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f8fafc;
  border-left: 3px solid #f59e0b;
  font-size: 12px;
  color: #475569;
}
.chat-input-bar {
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #e2e8f0;
}
.chunk-card {
  margin-bottom: 12px;
}
.chunk-card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.char-count {
  font-size: 12px;
  color: #94a3b8;
}
.chunk-content {
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
}
.empty-workspace {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>