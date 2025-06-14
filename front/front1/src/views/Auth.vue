<template>
  <div class="auth-container">
    <div class="tab-buttons">
      <button :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
      <button :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
    </div>

    <div class="form">
      <!-- 登录身份选择 -->
      <select v-if="mode === 'login'" v-model="form.role">
        <option value="user">普通用户</option>
        <option value="admin">管理员</option>
      </select>

      <input v-model="form.username" placeholder="用户名（必填）" />
      <input v-model="form.password" type="password" placeholder="密码（必填）" />

      <template v-if="mode === 'register'">
        <input v-model="form.confirmPassword" type="password" placeholder="确认密码（建议）" />
        <input v-model="form.nickname" placeholder="昵称" />
        <input v-model="form.gender" placeholder="性别" />
        <input v-model="form.age" type="number" placeholder="年龄" />
        <input v-model="form.job" placeholder="职业" />
        <input v-model="form.hobby" placeholder="爱好" />
      </template>

      <button @click="handleSubmit">{{ mode === 'login' ? '登录' : '注册' }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/request'



const router = useRouter()
const mode = ref('login')

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  gender: '',
  age: null,
  job: '',
  hobby: '',
  role: 'user', // login 时区分 user/admin
  status: 1
})

const handleSubmit = async () => {
  if (!form.value.username || !form.value.password) {
    alert('用户名和密码不能为空')
    return
  }

  try {
    if (mode.value === 'register') {
      if (form.value.role === 'admin') {
        alert('管理员不允许自行注册，请联系系统管理员')
        return
      }
      if (form.value.password !== form.value.confirmPassword) {
        alert('两次密码输入不一致')
        return
      }
      await axios.post('/register', form.value)
      alert('注册成功！可切换到登录页登录')
    } else {
      const url = form.value.role === 'admin' ? '/admin/login' : '/login'

      const res = await axios.post(url, {
        username: form.value.username,
        password: form.value.password
      })
      if (res.data.msg === '登录成功') {
        if (form.value.role === 'admin') {
          localStorage.setItem('adminUid', res.data.uid)
          router.push('/admin/dashboard')
        } else {
          // 保存完整的用户信息对象
          localStorage.setItem('user', JSON.stringify(res.data))
          router.push('/posts')
        }
      } else {
        alert(res.data.msg)
      }


    }
  } catch (err) {
    alert('操作失败，请查看控制台')
    console.error(err)
  }
}
</script>

<style scoped>
.auth-container {
  width: 320px;
  margin: 0 auto;
  margin-top: 10vh;
  padding: 20px;
  text-align: center;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
  background-color: #fff;
  border-radius: 12px;
}

.tab-buttons {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.tab-buttons button {
  flex: 1;
  padding: 10px;
  border: none;
  cursor: pointer;
  font-weight: bold;
  background-color: #eee;
  transition: 0.2s;
}

.tab-buttons .active {
  background-color: #007bff;
  color: white;
}

.form input,
.form select {
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  box-sizing: border-box;
  font-size: 14px;
}

.form button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  font-size: 15px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.form button:hover {
  background-color: #0056b3;
}

body, html {
  background-color: #111;
  margin: 0;
  padding: 0;
}
</style>
