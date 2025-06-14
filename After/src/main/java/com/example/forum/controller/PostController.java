package com.example.forum.controller;

import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import com.example.forum.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;

@CrossOrigin
@RestController
public class PostController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    // 发帖接口：接收 uid + title + content

    @Resource


    @Autowired
    private SensitiveWordService sensitiveWordService;

    // 发帖接口：接收 uid + title + content，带敏感词检测
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Map<String, Object> post) {
        try {
            int uid = Integer.parseInt(post.get("uid").toString());
            String title = (String) post.get("title");
            String content = (String) post.get("content");

            if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("标题或内容不能为空");
            }

            // 敏感词检测
            if (sensitiveWordService.containsSensitive(title) || sensitiveWordService.containsSensitive(content)) {
                return ResponseEntity.badRequest().body("标题或内容包含敏感词，禁止发帖！");
            }

            String sql = "INSERT INTO posts (uid, title, content, post_time, is_pinned, status) VALUES (?, ?, ?, GETDATE(), 0, 1)";
            int rows = jdbcTemplate.update(sql, uid, title, content);

            return rows > 0 ? ResponseEntity.ok("发布成功") : ResponseEntity.badRequest().body("发布失败");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("插入失败：" + e.getMessage());
        }
    }
//从posts表中查询帖子的ID、标题、内容和发布时间。
//通过左连接users表，获取帖子作者的昵称。
//只选择状态为1的帖子（可能是“已发布”的帖子）。
//按帖子发布时间降序排序，确保最新的帖子排在最前面。
    // 获取帖子列表接口：展示所有 status=1 的帖子，按发帖时间倒序
    @GetMapping("/posts")
    public List<Map<String, Object>> getAllPosts() {
        String sql = "SELECT p.post_id, p.title, p.content, p.post_time, u.nickname FROM posts p " +
                "LEFT JOIN users u ON p.uid = u.uid WHERE p.status = 1 ORDER BY p.post_time DESC";
        return jdbcTemplate.queryForList(sql);
    }

//分页查询功能，支持关键词搜索，返回当前页的帖子列表和总记录数

    @GetMapping("/admin/posts")
    public Map<String, Object> getPosts(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String keyword) {
        int offset = (page - 1) * size;
        // 联表查，同时查昵称
        String sql = "SELECT p.*, u.nickname FROM posts p LEFT JOIN users u ON p.uid = u.uid WHERE p.status IN (0,1) ";
        String countSql = "SELECT COUNT(*) FROM posts p LEFT JOIN users u ON p.uid = u.uid WHERE p.status IN (0,1) ";
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += "AND (p.content LIKE ? OR u.nickname LIKE ?) ";
            countSql += "AND (p.content LIKE ? OR u.nickname LIKE ?) ";
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        sql += "ORDER BY p.is_pinned DESC, p.post_time DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        params.add(offset);
        params.add(size);

        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql, params.toArray());
        // 统计参数只有前面的（不含offset/size）
        Object[] countParams = params.subList(0, params.size() - 2).toArray();
        int total = jdbcTemplate.queryForObject(countSql, countParams, Integer.class);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }

    //更新帖子的状态。它接收帖子ID和新的状态值，更新数据库中帖子的状态字段，并返回操作成功的结果。
    @PostMapping("/admin/post/status")
    public Map<String, Object> updatePostStatus(@RequestBody Map<String, Object> req) {
        int postId = (int) req.get("post_id");
        int status = (int) req.get("status");
        String sql = "UPDATE posts SET status=? WHERE post_id=?";
        jdbcTemplate.update(sql, status, postId);
        return Map.of("success", true);
    }
//更新帖子的置顶状态。它接收帖子ID和置顶状态值，更新数据库中对应的帖子记录，并返回操作成功的结果。
    @PostMapping("/admin/post/pin")
    public Map<String, Object> updatePostPin(@RequestBody Map<String, Object> req) {
        int postId = (int) req.get("post_id");
        int isPinned = (int) req.get("is_pinned");
        String sql = "UPDATE posts SET is_pinned=? WHERE post_id=?";
        jdbcTemplate.update(sql, isPinned, postId);
        return Map.of("success", true);
    }



    // 敏感词管理

    // 查询全部敏感词
    @GetMapping("/admin/sensitive")
    public List<Map<String, Object>> getSensitiveWords() {
        return jdbcTemplate.queryForList("SELECT * FROM sensitive_words");
    }

    // 添加敏感词
    @PostMapping("/admin/sensitive")
    public Map<String, Object> addSensitiveWord(@RequestBody Map<String, Object> req) {
        String word = req.get("word").toString();
        // 防重复
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sensitive_words WHERE word=?", Integer.class, word);
        if (exists != null && exists > 0) return Map.of("success", false, "msg", "敏感词已存在");
        jdbcTemplate.update("INSERT INTO sensitive_words (word) VALUES (?)", word);
        return Map.of("success", true);
    }

    // 删除敏感词
    @PostMapping("/admin/sensitive/delete")
    public Map<String, Object> deleteSensitiveWord(@RequestBody Map<String, Object> req) {
        int id = (int) req.get("id");
        jdbcTemplate.update("DELETE FROM sensitive_words WHERE id=?", id);
        return Map.of("success", true);
    }







}