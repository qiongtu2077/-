<template>
  <div class="create-post-container">

    <h2>写帖子</h2>

    <input v-model="form.title" placeholder="请输入标题（必填）" />
    <textarea v-model="form.content" placeholder="请输入内容（必填）"></textarea>

    <button @click="submitPost">发布帖子</button>
    <button class="back" @click="router.push('/posts')">返回大厅</button>
  </div>




</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/request'

const router = useRouter()
const form = ref({
  title: '',
  content: ''
})


  const submitPost = async () => {
    // 先获取当前登录用户信息
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const uid = user.uid

    if (!uid || !form.value.content || !form.value.title) {
      alert('uid、标题或内容不能为空')
      return
    }
  try {
    await axios.post('/posts', {
      uid,
      title: form.value.title, // ✅ 标题单独传递
      content: form.value.content
    })
    alert('发布成功！')
    router.push('/posts')
  } catch (err) {
    // 检查后端是否有返回具体的错误信息（如敏感词等）

    if (err.response && err.response.data) {
      alert(err.response.data); // 显示后端自定义的消息
    } else {
      alert('发布失败');
    }
    console.error(err);
  }
}






</script>

<style scoped>
.create-post-container {
  max-width: 600px;
  margin: 30px auto;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.2);
}

input, textarea {
  width: 100%;
  margin-bottom: 15px;
  padding: 10px;
  font-size: 15px;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box;
}

textarea {
  height: 150px;
  resize: vertical;
}

button {
  margin-right: 10px;
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

button.back {
  background-color: #666;
}
</style>
