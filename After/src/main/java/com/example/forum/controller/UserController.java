package com.example.forum.controller;

import com.example.forum.model.User;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@CrossOrigin
@RestController
public class UserController {

    @Resource
    private JdbcTemplate jdbcTemplate;
    @CrossOrigin
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return "用户名和密码不能为空";
        }
        String sql = "INSERT INTO users (username, password, nickname, gender, age, job, hobby, status, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getGender(),
                user.getAge(),
                user.getJob(),
                user.getHobby(),
                user.getStatus() != null ? user.getStatus() : 1,
                "user" // 注册只能是普通用户
        );
        return "注册成功";
    }
    //用户登录接口，用于验证用户名和密码是否正确。如果验证通过，返回用户信息和“登录成功”消息；否则返回“用户名或密码错误”。
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        String sql = "SELECT uid, username, nickname, role FROM users WHERE username = ? AND password = ? AND status = 1";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, user.getUsername(), user.getPassword());

        if (!result.isEmpty()) {
            Map<String, Object> data = result.get(0);
            data.put("msg", "登录成功");
            return data;
        } else {
            return Map.of("msg", "用户名或密码错误");
        }
    }
//管理员登录接口，验证用户名和密码是否正确，并且用户角色是否为“admin”。成功返回用户信息和“登录成功”消息，失败返回错误信息。
    @PostMapping("/admin/login")
    public Map<String, Object> adminLogin(@RequestBody Map<String, Object> payload) {
        String username = payload.get("username").toString();
        String password = payload.get("password").toString();

        String sql = "SELECT uid, username, nickname, role FROM users WHERE username = ? AND password = ? AND role = 'admin' AND status = 1";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, username, password);

        if (!result.isEmpty()) {
            Map<String, Object> data = result.get(0);
            data.put("msg", "登录成功");
            return data;
        } else {
            return Map.of("msg", "用户名或密码错误，或非管理员身份");
        }
    }
//获取所有用户的基本信息（包括用户ID、用户名、昵称、角色和状态）
    @GetMapping("/admin/users")
    public List<Map<String, Object>> getAllUsers() {
        String sql = "SELECT uid, username, nickname, role, status FROM users";
        return jdbcTemplate.queryForList(sql);
    }
//更新用户的状态。接收用户ID和新的状态值，执行数据库更新操作，返回“更新成功”或“更新失败”。
    @PostMapping("/admin/user/status")
    public String updateUserStatus(@RequestBody Map<String, Object> data) {
        Integer uid = (Integer) data.get("uid");
        Integer status = (Integer) data.get("status");
        String sql = "UPDATE users SET status = ? WHERE uid = ?";
        int rows = jdbcTemplate.update(sql, status, uid);
        return rows > 0 ? "更新成功" : "更新失败";
    }




}

