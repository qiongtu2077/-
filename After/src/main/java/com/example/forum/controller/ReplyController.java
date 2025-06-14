package com.example.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.example.forum.service.SensitiveWordService;



@CrossOrigin

@RestController
@RequestMapping("/api/replies")
public class ReplyController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SensitiveWordService sensitiveWordService;

    @CrossOrigin
    @PostMapping("/create")
    public Map<String, Object> createReply(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();



        Integer postId = (Integer) data.get("post_id");
        Integer uid = (Integer) data.get("uid");
        String content = (String) data.get("content");

        // 敏感词检测
        if (content == null || content.trim().isEmpty()) {
            result.put("success", false);
            result.put("msg", "回复内容不能为空");
            return result;
        }
        if (sensitiveWordService.containsSensitive(content)) {
            result.put("success", false);
            result.put("msg", "回复内容包含敏感词，禁止回复！");
            return result;
        }
//向replies(回复）表中插入一条新的回复记录，记录当前时间作为回复时间，将用户提供的回复内容、帖子ID、用户ID作为参数插入，并将状态设置为1
        String sql = "INSERT INTO replies (reply_time, content, post_id, uid, status) VALUES (getdate(), ?, ?, ?, 1)";
        jdbcTemplate.update(sql, content, postId, uid);

        result.put("success", true);
        result.put("msg", "回复成功");
        return result;
    }

    // 查询某帖所有回复
    @GetMapping("/list/{postId}")
    public List<Map<String, Object>> listReplies(@PathVariable("postId") int postId) {
        String sql = "SELECT r.reply_id, r.reply_time, r.content, r.uid, u.nickname FROM replies r JOIN users u ON r.uid = u.uid WHERE r.post_id = ? AND r.status = 1 ORDER BY r.reply_time ASC";
        return jdbcTemplate.queryForList(sql, postId);
    }

    // 管理员删除回复（逻辑删除）
    @PostMapping("/delete")
    public Map<String, Object> deleteReply(@RequestBody Map<String, Object> data) {
        Integer replyId = (Integer) data.get("reply_id");
        String sql = "UPDATE replies SET status = 0 WHERE reply_id = ?";
        jdbcTemplate.update(sql, replyId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }




}
