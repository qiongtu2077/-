// forum 系统完整后端接口 app.js（支持注册、登录、发帖、列表、点赞、回复、删除）
const express = require('express');
const sql = require('mssql');
const jwt = require('jsonwebtoken');
const app = express();
app.use(express.json());
app.use(express.static('public'));

const SECRET = 'forum_jwt_secret_key';

const config = {
  user: 'sa',
  password: 'sunchuyun0916',
  server: '127.0.0.1',
  database: '论坛管理系统',
  options: {
    port: 1433,
    trustServerCertificate: true
  }
};

// 注册
app.post('/register', async (req, res) => {
  try {
    await sql.connect(config);
    const { nickname, password } = req.body;
    const check = await sql.query`SELECT * FROM author WHERE nickname = ${nickname}`;
    if (check.recordset.length > 0) return res.status(400).send("昵称已存在");
    await sql.query`INSERT INTO author (nickname, password, is_admin) VALUES (${nickname}, ${password}, 0)`;
    res.send("注册成功！");
  } catch (err) {
    res.status(500).send("注册失败：" + err.message);
  }
});

// 登录
app.post('/login', async (req, res) => {
  try {
    await sql.connect(config);
    const { nickname, password } = req.body;
    const result = await sql.query`SELECT author_id, is_admin FROM author WHERE nickname = ${nickname} AND password = ${password}`;
    if (result.recordset.length === 0) return res.status(401).send("账号或密码错误");
    const { author_id, is_admin } = result.recordset[0];
    const token = jwt.sign({ author_id, nickname, is_admin }, SECRET, { expiresIn: '2h' });
    res.json({ token });
  } catch (err) {
    res.status(500).send("登录失败：" + err.message);
  }
});
// 重置密码
app.post('/reset', async (req, res) => {
  try {
    await sql.connect(config);
    const { nickname, password } = req.body;
    const check = await sql.query`SELECT * FROM author WHERE nickname = ${nickname}`;
    if (check.recordset.length === 0) {
      return res.status(400).send("该用户不存在");
    }
    await sql.query`UPDATE author SET password = ${password} WHERE nickname = ${nickname}`;
    res.send("密码重置成功！");
  } catch (err) {
    res.status(500).send("重置失败：" + err.message);
  }
});

// 发帖
app.post('/post', async (req, res) => {
  try {
    const token = req.headers.authorization?.split(" ")[1];
    const user = jwt.verify(token, SECRET);
    await sql.connect(config);
    const { title, content } = req.body;
    await sql.query`INSERT INTO post (author_id, title, content, post_date, level) VALUES (${user.author_id}, ${title}, ${content}, GETDATE(), 1)`;
    res.send("发帖成功！");
  } catch (err) {
    res.status(401).send("发帖失败/未登录：" + err.message);
  }
});

// 获取所有帖子（支持点赞排序）
app.get('/posts', async (req, res) => {
  try {
    await sql.connect(config);
    const sort = req.query.sort === 'like' ? 'like_count' : 'post_date';
    const result = await sql.query(`
      SELECT p.post_id, p.title, p.content, p.post_date, p.like_count, a.nickname
      FROM post p JOIN author a ON p.author_id = a.author_id
      ORDER BY ${sort} DESC
    `);
    res.json(result.recordset);
  } catch (err) {
    res.status(500).send("获取帖子失败：" + err.message);
  }
});

// 点赞 / 取消点赞
app.post('/like', async (req, res) => {
  try {
    const token = req.headers.authorization?.split(" ")[1];
    const user = jwt.verify(token, SECRET);
    await sql.connect(config);
    const { target_type, target_id } = req.body;
    const check = await sql.query`SELECT * FROM [like] WHERE author_id=${user.author_id} AND target_type=${target_type} AND target_id=${target_id}`;
    if (check.recordset.length > 0) {
      await sql.query`DELETE FROM [like] WHERE author_id=${user.author_id} AND target_type=${target_type} AND target_id=${target_id}`;
      await sql.query`UPDATE ${target_type} SET like_count = like_count - 1 WHERE ${target_type}_id = ${target_id}`;
      return res.send("已取消点赞");
    } else {
      await sql.query`INSERT INTO [like] (author_id, target_type, target_id, like_time) VALUES (${user.author_id}, ${target_type}, ${target_id}, GETDATE())`;
      await sql.query`UPDATE ${target_type} SET like_count = like_count + 1 WHERE ${target_type}_id = ${target_id}`;
      return res.send("点赞成功");
    }
  } catch (err) {
    res.status(401).send("点赞失败：" + err.message);
  }
});

// 回复
app.post('/reply', async (req, res) => {
  try {
    const token = req.headers.authorization?.split(" ")[1];
    const user = jwt.verify(token, SECRET);
    await sql.connect(config);
    const { post_id, content, parent_reply_id } = req.body;
    await sql.query`
      INSERT INTO reply (post_id, author_id, parent_reply_id, content, reply_date, like_count)
      VALUES (${post_id}, ${user.author_id}, ${parent_reply_id || null}, ${content}, GETDATE(), 0)`;
    res.send("回复成功");
  } catch (err) {
    res.status(401).send("回复失败：" + err.message);
  }
});

// 获取回复列表
app.get('/replies/:post_id', async (req, res) => {
  try {
    await sql.connect(config);
    const post_id = req.params.post_id;
    const result = await sql.query`
      SELECT r.reply_id, r.content, r.reply_date, r.like_count, r.parent_reply_id, a.nickname
      FROM reply r JOIN author a ON r.author_id = a.author_id
      WHERE r.post_id = ${post_id}
      ORDER BY r.reply_date ASC`;
    res.json(result.recordset);
  } catch (err) {
    res.status(500).send("获取回复失败：" + err.message);
  }
});

// 删除帖子（仅管理员）
app.delete('/post/:id', async (req, res) => {
  try {
    const token = req.headers.authorization?.split(" ")[1];
    const user = jwt.verify(token, SECRET);
    if (!user.is_admin) return res.status(403).send("你不是管理员");
    await sql.connect(config);
    await sql.query`DELETE FROM post WHERE post_id = ${req.params.id}`;
    res.send("帖子已删除");
  } catch (err) {
    res.status(401).send("删除失败：" + err.message);
  }
});

app.listen(2023, () => {
  console.log("服务器已启动：http://localhost:2023");
});
