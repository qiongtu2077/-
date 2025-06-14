<template>
  <div :class="['layout', darkMode ? 'dark' : '']">
    <!-- 顶部栏 -->
    <header class="top-bar">
      <span>论坛管理系统</span>
      <span class="right">欢迎管理员 UID: {{ adminUid }}</span>
      <button class="toggle-btn" @click="toggleTheme">{{ darkMode ? '☀️ 日间' : '🌙 夜间' }}</button>
    </header>

    <div class="main">
      <!-- 侧边栏 -->
      <aside class="side-menu">
        <ul>
          <li @click="section = 'users'">👤 用户管理</li>
          <li @click="section = 'posts'">📄 帖子管理</li>
          <li @click="section = 'sensitive'">🔐 敏感词管理</li>
          <li @click="section = 'stats'">📊 数据统计</li>
          <li @click="goToHome">🏠 返回首页</li>
        </ul>
      </aside>

      <!-- 主内容区 -->
      <section class="content">
        <!-- 用户管理 -->
        <div v-if="section === 'users'">
          <h2>用户管理</h2>
          <div style="overflow-x:auto;">
            <table class="user-table">
              <thead>
                <tr>
                  <th>UID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>身份</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.uid">
                  <td>{{ user.uid }}</td>
                  <td>{{ user.username }}</td>
                  <td>{{ user.nickname }}</td>
                  <td>{{ user.role }}</td>
                  <td>{{ user.status == 1 ? '正常' : '封禁' }}</td>
                  <td>
                    <button
                      v-if="user.uid.toString() !== adminUid.toString()"
                      @click="toggleStatus(user)">
                      {{ user.status == 1 ? '封禁' : '解封' }}
                    </button>
                    <span v-else style="color: #bbb;">--</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 帖子管理 -->
        <div v-else-if="section === 'posts'">
          <h2>帖子管理</h2>
          <!-- 注意：这里是主内容区，不是侧栏！ -->
          <div class="sidebar-tool" style="max-width:400px;margin-bottom:16px;">
          <input
            v-model="searchKeyword"
            @keyup.enter="fetchPosts"
            placeholder="搜索内容或作者昵称"
              class="sidebar-search"
            />

            <div class="sidebar-pagination">
              <button :disabled="pageNum===1" @click="pageNum--, fetchPosts()">上一页</button>
              <span>第{{pageNum}}页/共{{totalPages}}页</span>
              <button :disabled="pageNum===totalPages" @click="pageNum++, fetchPosts()">下一页</button>
            </div>
          </div>
          <div style="overflow-x:auto;">
            <table class="user-table">
             <thead>
  <tr>
    <th>帖子ID</th>
    <th>作者UID</th>
    <th>作者昵称</th> <!-- 新增 -->
    <th>内容</th>
    <th>发帖时间</th>
    <th>是否置顶</th>
    <th>状态</th>
    <th>操作</th>
  </tr>
</thead>
<tbody>
  <tr v-for="post in posts" :key="post.post_id">
    <td>{{ post.post_id }}</td>
    <td>{{ post.uid }}</td>
    <td>{{ post.nickname }}</td> <!-- 新增 -->
    <td>
      <span @click="showDetail(post)" style="color:#3498db;cursor:pointer;">
        {{ post.content.length > 15 ? post.content.slice(0, 15) + '...' : post.content }}
      </span>
    </td>
    <td>{{ post.post_time }}</td>
    <td>{{ post.is_pinned ? '已置顶' : '未置顶' }}</td>
    <td>{{ post.status ? '正常' : '已删除' }}</td>
    <td>
      <button @click="togglePostStatus(post)">
        {{ post.status ? '删除' : '恢复' }}
      </button>
      <button @click="togglePin(post)">
        {{ post.is_pinned ? '取消置顶' : '置顶' }}
      </button>
    </td>
  </tr>
</tbody>

            </table>
          </div>
          <!-- 帖子详情弹窗 -->
          <div v-if="showDetailModal" class="modal">
            <div class="modal-content">
              <h3>帖子详情</h3>
              <p><b>ID:</b> {{ detailPost.post_id }}</p>
              <p><b>作者UID:</b> {{ detailPost.uid }}</p>
              <p><b>内容:</b> {{ detailPost.content }}</p>
              <p><b>发帖时间:</b> {{ detailPost.post_time }}</p>
              <p><b>是否置顶:</b> {{ detailPost.is_pinned ? '是' : '否' }}</p>
              <button @click="showDetailModal = false">关闭</button>
            </div>
          </div>
        </div>




  <!-- 敏感词管理 -->
<div v-else-if="section === 'sensitive'">
  <h2>敏感词管理</h2>
  <!-- 添加敏感词 -->
  <div style="margin-bottom:18px;">
    <input v-model="newWord" placeholder="输入敏感词" class="sidebar-search" style="width:220px;">
    <button @click="addSensitiveWord">添加</button>
  </div>
  <!-- 敏感词表 -->
  <table class="user-table" style="max-width:400px;">
    <thead>
      <tr>
        <th>ID</th>
        <th>敏感词</th>
        <th>操作</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="item in sensitiveWords" :key="item.id">
        <td>{{ item.id }}</td>
        <td>{{ item.word }}</td>
        <td>
          <button @click="deleteSensitiveWord(item.id)">删除</button>
         </td>
      </tr>
    </tbody>
  </table>
</div>

        <!-- 数据统计 -->
        <div v-else-if="section === 'stats'">
          <h2>数据统计</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ stats.userTotal }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">正常用户数</div>
              <div class="stat-value">{{ stats.userNormal }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">封禁用户数</div>
              <div class="stat-value">{{ stats.userBanned }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">今日新增用户</div>
              <div class="stat-value">{{ stats.userNewToday }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">帖子总数</div>
              <div class="stat-value">{{ stats.postTotal }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">置顶帖数</div>
              <div class="stat-value">{{ stats.postPinned }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">已删除帖数</div>
              <div class="stat-value">{{ stats.postDeleted }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">今日发帖数</div>
              <div class="stat-value">{{ stats.postNewToday }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">本周发帖数</div>
              <div class="stat-value">{{ stats.postNewWeek }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-label">本月发帖数</div>
              <div class="stat-value">{{ stats.postNewMonth }}</div>
            </div>
          </div>
        </div>


 <!-- 欢迎页 -->
        <div v-else>
          <h2>欢迎回来，管理员！</h2>
          <p>请选择左侧功能开始管理论坛。</p>
        </div>
      </section>
    </div></div>
</template>



<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from '../utils/request'

const adminUid = ref('')
const darkMode = ref(false)
const section = ref('')
const users = ref([])


onMounted(() => {
  adminUid.value = localStorage.getItem('adminUid') || '未知'
})

const toggleTheme = () => {
  darkMode.value = !darkMode.value
  document.documentElement.classList.toggle('dark', darkMode.value)
}

watch(section, async (val) => {
  if (val === 'users') {
    try {
      const res = await axios.get('/admin/users')
      users.value = res.data
    } catch (err) {
      alert('加载用户失败')
    }
  }
  if (val === 'posts') {
    fetchPosts()
  }
})

const toggleStatus = async (user) => {
  const targetStatus = user.status == 1 ? 0 : 1
  try {
    await axios.post('/admin/user/status', {
      uid: user.uid,
      status: targetStatus
    })
    section.value = ''
    setTimeout(() => section.value = 'users', 0)
  } catch (err) {
    alert('操作失败')
  }
}

const posts = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const searchKeyword = ref('')
const showDetailModal = ref(false)
const detailPost = ref({})

const fetchPosts = async () => {
  try {
    const res = await axios.get('/admin/posts', {
      params: {
        page: pageNum.value,
        size: pageSize.value,
        keyword: searchKeyword.value.trim()
      }
    })
    posts.value = res.data.records.map(item => ({
      ...item,
      status: item.status === true || item.status === 1 || item.status === "1",
      is_pinned: item.is_pinned === true || item.is_pinned === 1 || item.is_pinned === "1"
    }))
    totalPages.value = Math.ceil(res.data.total / pageSize.value) || 1
  } catch (err) {
    alert('加载帖子失败')
  }
}

const togglePostStatus = async (post) => {
  const targetStatus = post.status ? 0 : 1
  try {
    await axios.post('/admin/post/status', {
      post_id: post.post_id,
      status: targetStatus
    })
    fetchPosts()
  } catch (err) {
    alert('操作失败')
  }
}

const togglePin = async (post) => {
  const targetPin = post.is_pinned ? 0 : 1
  try {
    await axios.post('/admin/post/pin', {
      post_id: post.post_id,
      is_pinned: targetPin
    })
    fetchPosts()
  } catch (err) {
    alert('操作失败')
  }
}

const showDetail = (post) => {
  detailPost.value = { ...post }
  showDetailModal.value = true
}

// <script setup> 里的变量和方法

const sensitiveWords = ref([])
const newWord = ref('')

const fetchSensitiveWords = async () => {
  sensitiveWords.value = await axios.get('/admin/sensitive').then(res => res.data)
}
const addSensitiveWord = async () => {
  if (!newWord.value.trim()) return alert('敏感词不能为空')
  const res = await axios.post('/admin/sensitive', { word: newWord.value.trim() })
  if (res.data.success) {
    newWord.value = ''
    fetchSensitiveWords()
  } else {
    alert(res.data.msg || '添加失败')
  }
}
const deleteSensitiveWord = async (id) => {
  if (!confirm('确定要删除？')) return
  await axios.post('/admin/sensitive/delete', { id })
  fetchSensitiveWords()
}
// 页面加载和切换到敏感词管理时刷新
onMounted(fetchSensitiveWords)
watch(section, v => { if (v === 'sensitive') fetchSensitiveWords() })





// 统计数据
const stats = ref({
  userTotal: 0,
  userNormal: 0,
  userBanned: 0,
  userNewToday: 0,
  postTotal: 0,
  postPinned: 0,
  postDeleted: 0,
  postNewToday: 0,
  postNewWeek: 0,
  postNewMonth: 0
})

// 监听 section 变化，切换到 stats 时加载统计
watch(
  () => section.value || section,  // 兼容ref和普通变量
  (val) => {
    if (val === 'stats') {
      fetchStats()
    }
  },
  { immediate: true }
)

async function fetchStats() {
  try {
    const res = await axios.get('/admin/stats')
    console.log('stats接口返回的数据:', res.data)
    stats.value = res.data
  } catch (err) {
    alert('获取统计数据失败')
    console.error('stats接口报错:', err)
  }
}

//返回首页
import { useRouter } from 'vue-router'
const router = useRouter()
function goToHome() {
  localStorage.removeItem('token') // 有token的项目可以加这行
  router.push('/')
}


</script>




<style>
:root {
  --bg: #ffffff;
  --text: #222222;
  --menu-bg: #f7f7f7;
  --menu-hover: #dddddd;
}
html.dark {
  --bg: #121212;
  --text: #eeeeee;
  --menu-bg: #1e1e1e;
  --menu-hover: #333333;
}
html, body {
  margin: 0;
  padding: 0;
  background: var(--bg);
  color: var(--text);
  font-family: 'Microsoft Yahei', sans-serif;
  min-height: 100vh;
  font-size: 16px;
  box-sizing: border-box;
}
.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg);
  color: var(--text);
  min-width: 0;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--menu-bg);
  padding: 12px 20px;
  font-weight: bold;
  border-bottom: 1px solid #ccc;
  min-width: 0;
}
.toggle-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: var(--text);
}
.main {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-width: 0;
}
.side-menu {
  width: 220px;
  min-width: 120px;
  max-width: 280px;
  background: var(--menu-bg);
  padding: 20px 0 0 0;
  box-sizing: border-box;
  transition: width 0.2s;
  flex-shrink: 0;
  font-size: 1rem;
}
.side-menu ul {
  list-style: none;
  padding: 0 20px;
  margin: 0;
}
.side-menu li {
  padding: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  border-radius: 6px;
  background: var(--menu-bg);
  transition: background 0.2s;
  word-break: break-all;
}
.side-menu li:hover {
  background: var(--menu-hover);
}
.sidebar-tool {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 20px;
}
.sidebar-search {
  width: 100%;
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #aaa;
  font-size: 15px;
}
.sidebar-pagination {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}
.content {
  flex: 1;
  min-width: 0;
  padding: 30px;
  background: var(--bg);
  overflow-y: auto;
  box-sizing: border-box;
}
.user-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
  min-width: 700px;
  table-layout: auto;
}
.user-table th,
.user-table td {
  border: 1px solid #ccc;
  padding: 10px;
  text-align: left;
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-table td {
  background: var(--bg);
}
@media (max-width: 1000px) {
  .side-menu { width: 70px; font-size: 0.92rem; padding-left: 0; }
  .side-menu ul, .sidebar-tool { padding-left: 6px; padding-right: 6px; }
  .content { padding: 12px; }
}
@media (max-width: 700px) {
  .side-menu { display: none; }
  .main { padding: 0; }
  .content { padding: 4px; }
  .user-table { min-width: 420px; font-size: 0.94rem;}
}
@media (max-width: 500px) {
  .top-bar { flex-direction: column; align-items: flex-start; padding: 8px 2vw;}
  .user-table th, .user-table td { padding: 4px; font-size: 0.92em;}
  .content { padding: 2px;}
}
/* 表格溢出时可横向滑动 */
.user-table {
  display: block;
  overflow-x: auto;
}
.modal {
  position: fixed;
  left: 0;top: 0;right: 0;bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 9999;
}
.modal-content {
  background: #fff;
  border-radius: 10px;
  padding: 24px 36px;
  min-width: 260px;
  max-width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
  box-sizing: border-box;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 18px;
  margin-top: 18px;
}
.stat-card {
  background: #f7f8fa;
  border-radius: 14px;
  box-shadow: 0 2px 8px #ececec;
  padding: 18px 0;
  text-align: center;
}
.stat-label {
  font-size: 15px;
  color: #888;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #336;
}

</style>
