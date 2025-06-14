package com.example.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class AdminStatsService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getStats() {
        // 用户统计
        String userSql = "SELECT COUNT(*) AS userTotal," +
                " SUM(CASE WHEN status=1 THEN 1 ELSE 0 END) AS userNormal," +
                " SUM(CASE WHEN status=0 THEN 1 ELSE 0 END) AS userBanned," +
                " SUM(CASE WHEN CONVERT(date, create_time) = CONVERT(date, GETDATE()) THEN 1 ELSE 0 END) AS userNewToday," +
                " SUM(CASE WHEN DATEPART(week, create_time) = DATEPART(week, GETDATE()) AND YEAR(create_time)=YEAR(GETDATE()) THEN 1 ELSE 0 END) AS userNewWeek," +
                " SUM(CASE WHEN MONTH(create_time) = MONTH(GETDATE()) AND YEAR(create_time)=YEAR(GETDATE()) THEN 1 ELSE 0 END) AS userNewMonth FROM users WHERE status IN (0,1)";
        Map<String, Object> userStats = jdbcTemplate.queryForMap(userSql);

        // 帖子统计
        String postSql = "SELECT COUNT(*) AS postTotal," +
                " SUM(CASE WHEN is_pinned=1 THEN 1 ELSE 0 END) AS postPinned," +
                " SUM(CASE WHEN status=0 THEN 1 ELSE 0 END) AS postDeleted," +
                " SUM(CASE WHEN CONVERT(date, post_time) = CONVERT(date, GETDATE()) THEN 1 ELSE 0 END) AS postNewToday," +
                " SUM(CASE WHEN DATEPART(week, post_time) = DATEPART(week, GETDATE()) AND YEAR(post_time)=YEAR(GETDATE()) THEN 1 ELSE 0 END) AS postNewWeek," +
                " SUM(CASE WHEN MONTH(post_time) = MONTH(GETDATE()) AND YEAR(post_time)=YEAR(GETDATE()) THEN 1 ELSE 0 END) AS postNewMonth FROM posts WHERE status IN (0,1)";
        Map<String, Object> postStats = jdbcTemplate.queryForMap(postSql);

        // 合并数据
        Map<String, Object> stats = new HashMap<>();
        stats.putAll(userStats);
        stats.putAll(postStats);

        System.out.println("返回数据: " + stats);



        return stats;
    }
}
