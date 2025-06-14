<template>

  <div class="posts-container">
    <h2>帖子大厅</h2>

    <!-- 发帖按钮 -->
    <div class="action-bar">
      <button @click="goToCreate">我要发帖</button>
      <button class="back-btn" @click="goToHome">返回登录</button>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list">
      <div v-for="post in posts" :key="post.post_id" class="post-card">
        <h3>{{ post.title || '无标题' }}</h3>
        <p class="author">作者：{{ post.nickname || '匿名' }} | 发布时间：{{ post.post_time }}</p>
        <p class="content">{{ post.content }}</p>
        <!-- 新增“查看详情”按钮 -->
        <button @click="openDetailModal(post)">查看详情</button>
      </div>
    </div>

    <!-- 帖子详情弹窗 -->
    <div v-if="showDetailModal" class="modal">
      <div class="modal-content">
        <h3>帖子详情</h3>
        <p><b>ID:</b> {{ detailPost.post_id }}</p>
        <p><b>作者:</b> {{ detailPost.nickname || '匿名' }}</p>
        <p><b>内容:</b> {{ detailPost.content }}</p>
        <p><b>发帖时间:</b> {{ detailPost.post_time }}</p>
        <button @click="showDetailModal = false">关闭</button>

        <h3>评论区</h3>
        <div v-for="reply in replies" :key="reply.reply_id" class="reply">
          <b>{{ reply.nickname }}</b>：{{ reply.content }} <span style="color:#999">{{ reply.reply_time }}</span>
        </div>
        <textarea v-model="replyContent" placeholder="写评论..." rows="3"></textarea>
        <button @click="submitReply">发布评论</button>
      </div>
    </div>
  </div>




</template>




<script setup>


  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import axios from '../utils/request'

  // 获取当前登录用户uid
  let currentUserUid = null
  const userStr = localStorage.getItem('user')
  if (userStr) {
  try {
  const user = JSON.parse(userStr)
  currentUserUid = user.uid
} catch(e) {
  currentUserUid = null
}
}

  // 帖子列表
  const posts = ref([])
  const router = useRouter()

  // 弹窗状态和详情
  const showDetailModal = ref(false)
  const detailPost = ref({})

  // 评论相关
  const replies = ref([])
  const replyContent = ref('')

  // 加载帖子列表
  const loadPosts = async () => {
  try {
  // 后端应该是 /api/posts/list
  const res = await axios.get('/posts')
  posts.value = res.data
} catch (err) {
  alert('加载帖子失败')
  console.error(err)
}
}

  // 页面挂载时自动加载帖子
  onMounted(() => {
  loadPosts()
})

  // 跳转到发帖页面
  const goToCreate = () => {
  router.push('/create-post')
}

  // 返回登录
  function goToHome() {
  localStorage.removeItem('token')
  router.push('/')
}

  // 打开帖子详情弹窗
  function openDetailModal(post) {
  detailPost.value = post
  showDetailModal.value = true
  fetchReplies(post.post_id)
}

  // 加载评论
  async function fetchReplies(postId) {
  try {
  const res = await axios.get(`/api/replies/list/${postId}`)
  replies.value = res.data
} catch (err) {
  alert('加载评论失败')
  console.error(err)
}
}

  // 发表评论
  async function submitReply() {
    if (!replyContent.value.trim()) {
      alert('评论不能为空')
      return
    }
    if (!currentUserUid) {
      alert('请先登录')
      return
    }
    try {
      await axios.post('/api/replies/create', {
        post_id: detailPost.value.post_id,
        uid: currentUserUid,
        content: replyContent.value
      })
      replyContent.value = ''
      fetchReplies(detailPost.value.post_id)
    } catch (err) {
      alert('catch到异常了！');
      if (err.response && err.response.data) {
        // 兼容对象与字符串
        if (typeof err.response.data === 'string') {
          try {
            // 有些后端直接返回JSON字符串（极少），做下解析
            const obj = JSON.parse(err.response.data);
            if (obj.msg) {
              alert(obj.msg);
            } else {
              alert(err.response.data);
            }
          } catch {
            alert(err.response.data);
          }
        } else if (err.response.data.msg) {
          alert(err.response.data.msg); // ← 你的情况就是这里
        } else {
          alert('发表评论失败');
        }
      } else {
        alert('发表评论失败');
      }
      console.error(err);
    }
  }
</script>



<style scoped>
.posts-container {
  max-width: 800px;
  margin: 30px auto;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.2);
}

.action-bar {
  text-align: right;
  margin-bottom: 20px;
}

.action-bar button {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  padding: 16px;
  border: 1px solid #ccc;
  border-radius: 10px;
  background-color: #f9f9f9;
}

.post-card .author {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.post-card .content {
  font-size: 15px;
  color: #333;
}



</style>
